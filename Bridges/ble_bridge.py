#!/usr/bin/env python3
import argparse, asyncio, json, socket, struct, sys, threading, time
from dataclasses import dataclass
from pathlib import Path

try:
    from bleak import BleakClient, BleakScanner
except ImportError:
    print("Install Bleak with: pip install bleak", file=sys.stderr)
    raise SystemExit(2)

try:
    from evdev import InputDevice, list_devices, ecodes
except ImportError:
    InputDevice=None
    list_devices=None
    ecodes=None

HOST="127.0.0.1"; PORT=28773
CONFIG=Path.home()/".wattzap-ble-bridge.json"

HR_SVC="0000180d-0000-1000-8000-00805f9b34fb"
CSC_SVC="00001816-0000-1000-8000-00805f9b34fb"
FTMS_SVC="00001826-0000-1000-8000-00805f9b34fb"
HR_CHAR="00002a37-0000-1000-8000-00805f9b34fb"
CSC_CHAR="00002a5b-0000-1000-8000-00805f9b34fb"
FTMS_DATA="00002ad2-0000-1000-8000-00805f9b34fb"
FTMS_FEATURE="00002acc-0000-1000-8000-00805f9b34fb"
FTMS_CONTROL="00002ad9-0000-1000-8000-00805f9b34fb"
FTMS_RESISTANCE_RANGE="00002ad6-0000-1000-8000-00805f9b34fb"
HID_SVC="00001812-0000-1000-8000-00805f9b34fb"
HID_REPORT="00002a4d-0000-1000-8000-00805f9b34fb"

FTMS_REQUEST_CONTROL=0x00
FTMS_START_RESUME=0x07
FTMS_SET_TARGET_RESISTANCE=0x04
FTMS_SET_TARGET_POWER=0x05
FTMS_SET_SIMULATION=0x11
FTMS_RESPONSE_CODE=0x80
FTMS_SUCCESS=0x01

# Neutral simulation values. Gradient is supplied by WattzAp.
SIM_WIND_SPEED_MS=0.0
SIM_CRR=0.004
SIM_WIND_RESISTANCE=0.51

# Virtual gears driven by a BLE HID/media controller.
MIN_VIRTUAL_GEAR=1
MAX_VIRTUAL_GEAR=24
DEFAULT_VIRTUAL_GEAR=12

# At 0% gradient, the 24 gears occupy the middle 60% of the trainer's
# advertised resistance range. This deliberately leaves headroom for hills
# and descents. Each 1% gradient shifts resistance by 2% of the full range.
GEAR_RESISTANCE_LOW=0.20
GEAR_RESISTANCE_HIGH=0.80
GRADIENT_RESISTANCE_PER_PERCENT=0.02

# BlueZ may reject overlapping Device1.Connect calls with
# org.bluez.Error.InProgress. Serialise only connection setup.
BLE_CONNECT_LOCK=None


# Fitness Machine Feature characteristic: Target Setting Features bits.
FTMS_TARGET_FEATURES = {
    0: "Speed target",
    1: "Inclination target",
    2: "Resistance target",
    3: "Power target",
    4: "Heart-rate target",
    5: "Targeted expended energy",
    6: "Targeted step count",
    7: "Targeted stride count",
    8: "Targeted distance",
    9: "Targeted training time",
    10: "Targeted time in 2 HR zones",
    11: "Targeted time in 3 HR zones",
    12: "Targeted time in 5 HR zones",
    13: "Indoor bike simulation parameters",
    14: "Wheel circumference",
    15: "Spin-down control",
    16: "Target cadence",
}

def print_ftms_features(data):
    b=bytes(data)
    if len(b)<8:
        print(f"[FTMS] invalid Fitness Machine Feature value: {b.hex()}")
        return

    machine_features,target_features=struct.unpack_from("<II",b,0)
    print(f"[FTMS] Fitness Machine Features: 0x{machine_features:08x}")
    print(f"[FTMS] Target Setting Features:  0x{target_features:08x}")

    supported=[name for bit,name in FTMS_TARGET_FEATURES.items()
               if target_features & (1<<bit)]
    if supported:
        print("[FTMS] supported control features:")
        for name in supported:
            print(f"  - {name}")
    else:
        print("[FTMS] no target-setting features advertised")

    print("[FTMS] controls relevant to WattzAp:")
    print("  resistance target (0x04): "
          + ("yes" if target_features & (1<<2) else "no"))
    print("  power target      (0x05): "
          + ("yes" if target_features & (1<<3) else "no"))
    print("  simulation params (0x11): "
          + ("yes" if target_features & (1<<13) else "no"))

async def connect_ble(addr):
    """
    Resolve the configured BLE address explicitly before connecting.

    On Linux/BlueZ, constructing BleakClient directly from a string address can
    trigger an implicit discovery.  That can intermittently fail with
    "Device with address ... was not found", especially when several BLE
    devices are being managed at the same time.

    Serialising discovery + connection also avoids overlapping BlueZ
    Device1.Connect operations.
    """
    global BLE_CONNECT_LOCK
    if BLE_CONNECT_LOCK is None:
        BLE_CONNECT_LOCK=asyncio.Lock()

    async with BLE_CONNECT_LOCK:
        device=await BleakScanner.find_device_by_address(
            addr,
            timeout=5.0)

        if device is None:
            raise RuntimeError(
                f"BLE device {addr} not found during discovery")

        client=BleakClient(device)
        await client.connect()
        return client


class WattzAp:
    def __init__(self, host, port):
        self.host=host; self.port=port; self.sock=None; self.reader=None; self.last=0
        self.lock=threading.Lock()

    def connect(self):
        with self.lock:
            if self.sock: return True
            if time.monotonic()-self.last < 2: return False
            self.last=time.monotonic()
            try:
                sock=socket.create_connection((self.host,self.port),timeout=2)
                sock.settimeout(None)
                self.sock=sock
                self.reader=sock.makefile("r",encoding="utf-8")
                print(f"[WattzAp] connected {self.host}:{self.port}")
                return True
            except OSError as e:
                print(f"[WattzAp] unavailable: {e}")
                self.sock=None; self.reader=None
                return False

    def _disconnect(self, expected=None):
        with self.lock:
            if expected is not None and self.sock is not expected:
                return
            reader,self.reader=self.reader,None
            sock,self.sock=self.sock,None
        if reader:
            try: reader.close()
            except Exception: pass
        if sock:
            try: sock.close()
            except Exception: pass

    def send(self, obj):
        if not obj or not self.connect(): return
        sock=self.sock
        try:
            sock.sendall((json.dumps(obj,separators=(",",":"))+"\n").encode())
        except OSError:
            self._disconnect(sock)

    def receive(self):
        if not self.connect(): return None
        sock=self.sock; reader=self.reader
        try:
            line=reader.readline()
            if not line:
                self._disconnect(sock)
                return None
            return json.loads(line)
        except (OSError,ValueError):
            self._disconnect(sock)
            return None

    def close(self):
        self._disconnect()

@dataclass
class Dev:
    address:str
    name:str
    roles:tuple

def roles_for(uuids):
    u={x.lower() for x in (uuids or [])}; r=[]
    if FTMS_SVC in u: r.append("trainer")
    if CSC_SVC in u: r.append("cadence")
    if HR_SVC in u: r.append("heartRate")
    if HID_SVC in u: r.append("mediaController")
    return tuple(r)

async def scan(seconds):
    print(f"Scanning BLE for {seconds:.0f}s...")
    results=await BleakScanner.discover(timeout=seconds,return_adv=True)
    out=[]
    for _,(d,a) in results.items():
        r=roles_for(a.service_uuids)
        if r:
            out.append(Dev(d.address,a.local_name or d.name or "Unknown",r))
    for d in out:
        print(f"  {d.name:<28} {d.address} [{', '.join(d.roles)}]")
    return out

def load(path):
    cfg={"trainer":None,"cadence":None,"heartRate":None,"mediaController":None}
    if path.exists():
        try: cfg.update(json.loads(path.read_text()))
        except Exception: pass
    return cfg

def save(path,cfg):
    path.write_text(json.dumps(cfg,indent=2)+"\n")
    print(f"Saved {path}")

def choose(label,devices,role,current):
    c=[d for d in devices if role in d.roles]
    print(f"\n{label}")
    for i,d in enumerate(c,1): print(f"  {i}. {d.name} ({d.address})")
    s=input(f"Select [current={current or 'none'}; Enter keep, - clear]: ").strip()
    if not s: return current
    if s=="-": return None
    try:
        n=int(s)
        return c[n-1].address if 1<=n<=len(c) else current
    except ValueError:
        return s

class CSC:
    def __init__(self):
        self.wr=self.wt=self.cr=self.ct=None
    def parse(self,b):
        if not b:return {}
        flags=b[0]; o=1; out={}
        if flags&1 and len(b)>=o+6:
            wr=struct.unpack_from("<I",b,o)[0]; wt=struct.unpack_from("<H",b,o+4)[0]; o+=6
            if self.wr is not None:
                dr=(wr-self.wr)&0xffffffff; dt=(wt-self.wt)&0xffff
                if dt: out.update(rotations=dr,elapsedMs=round(dt*1000/1024))
            self.wr,self.wt=wr,wt
        if flags&2 and len(b)>=o+4:
            cr=struct.unpack_from("<H",b,o)[0]; ct=struct.unpack_from("<H",b,o+2)[0]
            if self.cr is not None:
                dr=(cr-self.cr)&0xffff; dt=(ct-self.ct)&0xffff
                if dt:
                    rpm=round(60*dr/(dt/1024))
                    if 0<=rpm<=300: out["cadence"]=rpm
            self.cr,self.ct=cr,ct
        return out

def parse_hr(b):
    if len(b)<2:return None
    return struct.unpack_from("<H",b,1)[0] if (b[0]&1 and len(b)>=3) else b[1]

def parse_ftms(b):
    if len(b)<2:return {}
    f=struct.unpack_from("<H",b,0)[0]; o=2; out={}
    def need(n):
        nonlocal o
        if len(b)<o+n:return False
        o+=n; return True
    if not(f&1):
        if not need(2): return out
    if f&(1<<1):
        if not need(2): return out
    if f&(1<<2):
        if len(b)<o+2:return out
        out["cadence"]=round(struct.unpack_from("<H",b,o)[0]/2); o+=2
    if f&(1<<3):
        if not need(2): return out
    if f&(1<<4):
        if not need(3): return out
    if f&(1<<5):
        if not need(2): return out
    if f&(1<<6):
        if len(b)<o+2:return out
        p=struct.unpack_from("<h",b,o)[0]; o+=2
        if p>=0: out["power"]=p
    if f&(1<<7):
        if not need(2): return out
    if f&(1<<8):
        if not need(5): return out
    if f&(1<<9) and len(b)>=o+1:
        out["heartRate"]=b[o]
    return out

class VirtualGears:
    """
    Small, deliberately trainer-independent virtual gear state.

    For the first implementation the BLE media controller only changes this
    value and prints it. Once the button mapping is verified on real hardware,
    the gear can be used by the trainer-control layer without changing the
    input handling.
    """
    def __init__(self, initial=DEFAULT_VIRTUAL_GEAR):
        self.gear=max(MIN_VIRTUAL_GEAR,min(MAX_VIRTUAL_GEAR,int(initial)))
        self.changed=asyncio.Event()

    def show(self):
        print(f"[GEAR] {self.gear}/{MAX_VIRTUAL_GEAR}")

    def up(self):
        old=self.gear
        self.gear=min(MAX_VIRTUAL_GEAR,self.gear+1)
        if self.gear!=old:
            self.show()
            self.changed.set()

    def down(self):
        old=self.gear
        self.gear=max(MIN_VIRTUAL_GEAR,self.gear-1)
        if self.gear!=old:
            self.show()
            self.changed.set()

    async def wait_changed(self):
        await self.changed.wait()
        self.changed.clear()


def media_button_from_report(data):
    """
    Decode the most common HID consumer-control reports used by inexpensive
    BLE media remotes.

    Consumer usages:
      0x00b5 next track
      0x00b6 previous track
      0x00e9 volume up
      0x00ea volume down

    Some remotes send the usage as a little-endian 16-bit value, others as
    a one-byte low usage value. Release packets (all zeroes) are ignored.

    Returns "up", "down" or None. Unknown non-zero reports are printed by the
    caller so a controller with a different report layout can easily be mapped.
    """
    b=bytes(data)
    if not b or not any(b):
        return None

    usages=set()
    for i in range(len(b)-1):
        usages.add(b[i] | (b[i+1] << 8))
    usages.update(b)

    if 0x00b5 in usages or 0x00e9 in usages:
        return "up"
    if 0x00b6 in usages or 0x00ea in usages:
        return "down"
    return None


def parse_resistance_range(data):
    """
    FTMS Supported Resistance Level Range is three little-endian SINT16
    values: minimum, maximum and minimum increment. Keep the values in the
    trainer's native FTMS units so the same raw value can be sent back to the
    Set Target Resistance Level procedure.
    """
    b=bytes(data)
    if len(b)<6:
        raise ValueError(f"invalid resistance range: {b.hex()}")
    minimum,maximum,increment=struct.unpack_from("<hhh",b,0)
    if maximum<=minimum:
        raise ValueError(
            f"invalid resistance range {minimum}..{maximum}")
    if increment<=0:
        increment=1
    return minimum,maximum,increment


def resistance_for(gradient,gear,resistance_range):
    minimum,maximum,increment=resistance_range
    span=maximum-minimum

    if MAX_VIRTUAL_GEAR==MIN_VIRTUAL_GEAR:
        gear_fraction=0.5
    else:
        gear_fraction=(gear-MIN_VIRTUAL_GEAR)/(
            MAX_VIRTUAL_GEAR-MIN_VIRTUAL_GEAR)

    flat_fraction=(
        GEAR_RESISTANCE_LOW
        + gear_fraction*(GEAR_RESISTANCE_HIGH-GEAR_RESISTANCE_LOW)
    )
    target_fraction=(
        flat_fraction
        + float(gradient)*GRADIENT_RESISTANCE_PER_PERCENT
    )
    target_fraction=max(0.0,min(1.0,target_fraction))

    raw=minimum+target_fraction*span

    # Quantise to the trainer's advertised minimum increment.
    steps=round((raw-minimum)/increment)
    raw=minimum+steps*increment
    return int(max(minimum,min(maximum,raw)))


class ControlState:
    """
    Latest trainer-control state received from WattzAp.

    The most recently received control type selects the trainer mode:
      gradient    -> FTMS simulation mode (0x11)
      targetPower -> FTMS ERG mode (0x05)

    Repeated values are suppressed so WattzAp can safely include the same
    control value in consecutive return messages.
    """
    def __init__(self):
        self.mode=None
        self.value=None
        self.changed=asyncio.Event()

    def set_gradient(self, gradient):
        gradient=float(gradient)
        if (self.mode=="gradient" and self.value is not None
                and abs(gradient-self.value)<0.01):
            return
        self.mode="gradient"
        self.value=gradient
        self.changed.set()

    def set_target_power(self, watts):
        watts=int(watts)
        if self.mode=="targetPower" and self.value==watts:
            return
        self.mode="targetPower"
        self.value=watts
        self.changed.set()

    async def next_control(self):
        await self.changed.wait()
        self.changed.clear()
        return self.mode,self.value


class FTMSController:
    def __init__(self, client):
        self.client=client
        self.lock=asyncio.Lock()
        self.pending=None

    def _indication(self, _, data):
        b=bytes(data)
        if len(b)<3 or b[0]!=FTMS_RESPONSE_CODE:
            return
        request_opcode=b[1]; result=b[2]
        if self.pending and not self.pending.done():
            self.pending.set_result((request_opcode,result))

    async def start(self):
        await self.client.start_notify(FTMS_CONTROL,self._indication)
        await self.command(bytes([FTMS_REQUEST_CONTROL]),"request control")
        await self.command(bytes([FTMS_START_RESUME]),"start/resume")

    async def command(self, payload, label):
        async with self.lock:
            loop=asyncio.get_running_loop()
            self.pending=loop.create_future()
            try:
                await self.client.write_gatt_char(FTMS_CONTROL,payload,response=True)
                request,result=await asyncio.wait_for(self.pending,3.0)
            finally:
                self.pending=None

            if request!=payload[0]:
                raise RuntimeError(
                    f"FTMS {label}: response opcode 0x{request:02x} "
                    f"does not match request 0x{payload[0]:02x}")
            if result!=FTMS_SUCCESS:
                raise RuntimeError(
                    f"FTMS {label} rejected: result 0x{result:02x}")

    async def set_gradient(self, gradient):
        gradient=max(-327.68,min(327.67,float(gradient)))
        wind=max(-32.768,min(32.767,SIM_WIND_SPEED_MS))
        crr=max(0,min(0.0255,SIM_CRR))
        cw=max(0,min(2.55,SIM_WIND_RESISTANCE))

        payload=struct.pack(
            "<BhhBB",
            FTMS_SET_SIMULATION,
            round(wind*1000),
            round(gradient*100),
            round(crr/0.0001),
            round(cw/0.01))
        await self.command(payload,f"set gradient {gradient:.2f}%")
        print(f"[FTMS] simulation gradient {gradient:.2f}%")

    async def set_resistance(self, resistance):
        resistance=max(-32768,min(32767,int(resistance)))
        payload=struct.pack(
            "<Bh",
            FTMS_SET_TARGET_RESISTANCE,
            resistance)
        await self.command(
            payload,
            f"set resistance {resistance}")
        print(f"[FTMS] resistance target {resistance}")

    async def set_target_power(self, watts):
        # FTMS Set Target Power uses a signed 16-bit value in watts.
        watts=max(-32768,min(32767,int(watts)))
        payload=struct.pack("<Bh",FTMS_SET_TARGET_POWER,watts)
        await self.command(payload,f"set target power {watts} W")
        print(f"[FTMS] ERG target power {watts} W")


async def receive_controls(w,state):
    while True:
        try:
            message=await asyncio.to_thread(w.receive)
            if not message:
                await asyncio.sleep(0.2)
                continue

            if message.get("type")!="control":
                continue

            # ERG takes precedence if both happen to be present.
            if "targetPower" in message:
                try:
                    watts=int(message["targetPower"])
                    if watts < 0:
                        raise ValueError
                    state.set_target_power(watts)
                except (TypeError,ValueError):
                    print("[WattzAp] invalid targetPower",
                          message.get("targetPower"))

            elif "gradient" in message:
                try:
                    state.set_gradient(float(message["gradient"]))
                except (TypeError,ValueError):
                    print("[WattzAp] invalid gradient",
                          message.get("gradient"))

        except asyncio.CancelledError:
            raise
        except Exception as e:
            print("[WattzAp] control receive error",e)
            await asyncio.sleep(1)


async def apply_controls(
        controller,state,gears,resistance_range,use_virtual_gears):
    async def apply(mode,value):
        if mode=="targetPower":
            # ERG remains exactly as before. Virtual gears deliberately do
            # nothing while WattzAp is prescribing target power.
            await controller.set_target_power(value)

        elif mode=="gradient":
            if use_virtual_gears and resistance_range is not None:
                resistance=resistance_for(
                    value,
                    gears.gear,
                    resistance_range)
                await controller.set_resistance(resistance)
                print(
                    f"[GEAR] {gears.gear}/{MAX_VIRTUAL_GEAR} "
                    f"gradient {value:.2f}% -> resistance {resistance}")
            else:
                # Preserve existing simulation behaviour if virtual gears are
                # not configured or the trainer did not expose a usable range.
                await controller.set_gradient(value)

    # Re-apply the last known WattzAp control after a BLE reconnect.
    if state.mode is not None:
        state.changed.clear()
        await apply(state.mode,state.value)

    while True:
        control_wait=asyncio.create_task(state.changed.wait())
        gear_wait=asyncio.create_task(gears.changed.wait())

        done,pending=await asyncio.wait(
            (control_wait,gear_wait),
            return_when=asyncio.FIRST_COMPLETED)

        for task in pending:
            task.cancel()
        await asyncio.gather(*pending,return_exceptions=True)

        if control_wait in done:
            state.changed.clear()
            if state.mode is not None:
                await apply(state.mode,state.value)

        if gear_wait in done:
            gears.changed.clear()
            # A gear shift only changes trainer load in slope mode.
            if state.mode=="gradient":
                await apply(state.mode,state.value)


async def keep_media_controller(addr,gears,name=None):
    """
    Read media buttons from Linux evdev.

    BLE HID remotes are normally claimed by BlueZ's HID-over-GATT support and
    exposed as kernel input devices.  Reading /dev/input/event* is therefore
    more reliable than trying to subscribe to the HID GATT reports with Bleak.

    Common media mappings:
      KEY_NEXTSONG / KEY_VOLUMEUP   -> gear up
      KEY_PREVIOUSSONG / KEY_VOLUMEDOWN -> gear down
    """
    if InputDevice is None:
        print("[GEAR] python-evdev is required for BLE media controllers")
        print("[GEAR] install with: pip install evdev")
        return

    wanted=(name or "SmartRemote").lower()
    current_path=None
    device=None

    while True:
        try:
            # Re-discover the input node because /dev/input/eventN can change
            # whenever the remote reconnects.
            matches=[]
            for path in list_devices():
                try:
                    d=InputDevice(path)
                    if wanted in (d.name or "").lower():
                        matches.append(d)
                    else:
                        d.close()
                except Exception:
                    pass

            if not matches:
                if current_path is not None:
                    print(f"[GEAR] media controller disconnected: {name or 'SmartRemote'}")
                    current_path=None
                await asyncio.sleep(2)
                continue

            device=matches[0]
            for extra in matches[1:]:
                extra.close()

            if device.path!=current_path:
                current_path=device.path
                print(f"[GEAR] media controller ready: {device.name} ({device.path})")
                gears.show()

            async for event in device.async_read_loop():
                if event.type!=ecodes.EV_KEY:
                    continue

                # value 1 = key down, 2 = autorepeat, 0 = release.
                # Only act on the initial key press.
                if event.value!=1:
                    continue

                code=event.code

                if code in (
                        ecodes.KEY_NEXTSONG,
                        ecodes.KEY_VOLUMEUP,
                        getattr(ecodes, "KEY_FASTFORWARD", -1)):
                    gears.up()

                elif code in (
                        ecodes.KEY_PREVIOUSSONG,
                        ecodes.KEY_VOLUMEDOWN,
                        getattr(ecodes, "KEY_REWIND", -1)):
                    gears.down()

                else:
                    keyname=ecodes.KEY.get(code, str(code))
                    print(f"[GEAR] unmapped key {keyname}")

        except asyncio.CancelledError:
            raise
        except PermissionError as e:
            print("[GEAR] cannot read input device:",e)
            print("[GEAR] add the user to the 'input' group or run with suitable permissions")
            await asyncio.sleep(5)
        except OSError:
            # Typical when a wireless input device disconnects.
            current_path=None
            await asyncio.sleep(2)
        except Exception as e:
            print("[GEAR] media controller error",e)
            await asyncio.sleep(2)
        finally:
            if device is not None:
                try:
                    device.close()
                except Exception:
                    pass
                device=None


async def keep_hr(addr,w):
    while True:
        c=None
        try:
            print("[BLE] HR",addr)
            c=await connect_ble(addr)
            await c.start_notify(
                HR_CHAR,
                lambda _,b: w.send({"heartRate":parse_hr(bytes(b))}))
            while c.is_connected:
                await asyncio.sleep(1)
        except asyncio.CancelledError:
            raise
        except Exception as e:
            print("[BLE] HR error",e)
            await asyncio.sleep(3)
        finally:
            if c is not None:
                try:
                    if c.is_connected:
                        await c.disconnect()
                except Exception:
                    pass


async def keep_csc(addr,w):
    parser=CSC()
    while True:
        c=None
        try:
            print("[BLE] CSC",addr)
            c=await connect_ble(addr)
            await c.start_notify(
                CSC_CHAR,
                lambda _,b: w.send(parser.parse(bytes(b))))
            while c.is_connected:
                await asyncio.sleep(1)
        except asyncio.CancelledError:
            raise
        except Exception as e:
            print("[BLE] CSC error",e)
            await asyncio.sleep(3)
        finally:
            if c is not None:
                try:
                    if c.is_connected:
                        await c.disconnect()
                except Exception:
                    pass


async def keep_ftms(addr,w,cfg,control_state,gears):
    while True:
        c=None
        control_task=None
        try:
            print("[BLE] FTMS",addr)
            c=await connect_ble(addr)

            def cb(_,b):
                x=parse_ftms(bytes(b))
                if cfg.get("cadence"):
                    x.pop("cadence",None)
                if cfg.get("heartRate"):
                    x.pop("heartRate",None)

                # Include the current virtual gear with trainer telemetry so
                # WattzAp can display it (for example "12/24").
                if cfg.get("mediaController"):
                    x["gear"]=gears.gear
                    x["gearCount"]=MAX_VIRTUAL_GEAR

                if x:
                    w.send(x)

            resistance_range=None
            try:
                features=await c.read_gatt_char(FTMS_FEATURE)
                print_ftms_features(features)
            except Exception as e:
                print("[FTMS] unable to read feature flags",e)

            if cfg.get("mediaController"):
                try:
                    raw_range=await c.read_gatt_char(
                        FTMS_RESISTANCE_RANGE)
                    resistance_range=parse_resistance_range(raw_range)
                    rmin,rmax,rinc=resistance_range
                    print(
                        f"[FTMS] resistance range "
                        f"{rmin}..{rmax}, increment {rinc}")
                except Exception as e:
                    print(
                        "[FTMS] unable to read resistance range; "
                        "virtual gears will fall back to simulation mode:",
                        e)

            await c.start_notify(FTMS_DATA,cb)

            controller=FTMSController(c)
            await controller.start()
            print("[FTMS] control acquired")
            control_task=asyncio.create_task(
                apply_controls(
                    controller,
                    control_state,
                    gears,
                    resistance_range,
                    bool(cfg.get("mediaController"))))

            while c.is_connected:
                await asyncio.sleep(1)

        except asyncio.CancelledError:
            raise
        except Exception as e:
            print("[BLE] FTMS error",e)
            await asyncio.sleep(3)
        finally:
            if control_task:
                control_task.cancel()
                await asyncio.gather(
                    control_task,
                    return_exceptions=True)
            if c is not None:
                try:
                    if c.is_connected:
                        await c.disconnect()
                except Exception:
                    pass


async def main():
    ap=argparse.ArgumentParser()
    ap.add_argument("--scan",action="store_true")
    ap.add_argument("--configure",action="store_true")
    ap.add_argument("--scan-seconds",type=float,default=10)
    ap.add_argument("--config",type=Path,default=CONFIG)
    ap.add_argument("--trainer"); ap.add_argument("--cadence"); ap.add_argument("--hr")
    ap.add_argument("--media-controller")
    ap.add_argument("--save",action="store_true")
    ap.add_argument("--show-config",action="store_true")
    ap.add_argument("--host",default=HOST); ap.add_argument("--port",type=int,default=PORT)
    a=ap.parse_args(); cfg=load(a.config)

    if a.scan:
        await scan(a.scan_seconds); return
    if a.configure:
        ds=await scan(a.scan_seconds)
        cfg["trainer"]=choose("FTMS smart trainer",ds,"trainer",cfg.get("trainer"))
        cfg["cadence"]=choose("CSC cadence/speed sensor",ds,"cadence",cfg.get("cadence"))
        cfg["heartRate"]=choose("Heart-rate sensor",ds,"heartRate",cfg.get("heartRate"))
        cfg["mediaController"]=choose("BLE media controller",ds,"mediaController",cfg.get("mediaController"))
        if cfg.get("mediaController"):
            selected=next(
                (d for d in ds if d.address==cfg["mediaController"]),
                None)
            if selected:
                cfg["mediaControllerName"]=selected.name
        else:
            cfg["mediaControllerName"]=None
        save(a.config,cfg)
    if a.trainer: cfg["trainer"]=a.trainer
    if a.cadence: cfg["cadence"]=a.cadence
    if a.hr: cfg["heartRate"]=a.hr
    if a.media_controller: cfg["mediaController"]=a.media_controller
    if a.save: save(a.config,cfg)
    if a.show_config:
        print(json.dumps(cfg,indent=2)); return

    w=WattzAp(a.host,a.port); tasks=[]
    control_state=ControlState()
    gears=VirtualGears()
    tasks.append(asyncio.create_task(receive_controls(w,control_state)))
    if cfg.get("trainer"):
        tasks.append(asyncio.create_task(
            keep_ftms(
                cfg["trainer"],w,cfg,control_state,gears)))
        await asyncio.sleep(0.5)
    if cfg.get("cadence"):
        tasks.append(asyncio.create_task(
            keep_csc(cfg["cadence"],w)))
        await asyncio.sleep(0.5)
    if cfg.get("heartRate"):
        tasks.append(asyncio.create_task(
            keep_hr(cfg["heartRate"],w)))
        await asyncio.sleep(0.5)
    if cfg.get("mediaController"):
        tasks.append(asyncio.create_task(
            keep_media_controller(
                cfg["mediaController"],
                gears,
                cfg.get("mediaControllerName"))))
    if not any(cfg.get(k) for k in ("trainer","cadence","heartRate","mediaController")):
        print("No BLE devices configured.",file=sys.stderr)
        for t in tasks: t.cancel()
        await asyncio.gather(*tasks,return_exceptions=True)
        w.close()
        return
    try:
        await asyncio.gather(*tasks)
    finally:
        for t in tasks:t.cancel()
        await asyncio.gather(*tasks,return_exceptions=True)
        w.close()

if __name__=="__main__":
    try: asyncio.run(main())
    except KeyboardInterrupt: print("\nStopped.")


async def keep_media_controller(addr,gears,name=None):
    """
    Read media buttons from Linux evdev.

    BLE HID remotes are normally claimed by BlueZ's HID-over-GATT support and
    exposed as kernel input devices.  Reading /dev/input/event* is therefore
    more reliable than trying to subscribe to the HID GATT reports with Bleak.

    Common media mappings:
      KEY_NEXTSONG / KEY_VOLUMEUP   -> gear up
      KEY_PREVIOUSSONG / KEY_VOLUMEDOWN -> gear down
    """
    if InputDevice is None:
        print("[GEAR] python-evdev is required for BLE media controllers")
        print("[GEAR] install with: pip install evdev")
        return

    wanted=(name or "SmartRemote").lower()
    current_path=None
    device=None

    while True:
        try:
            # Re-discover the input node because /dev/input/eventN can change
            # whenever the remote reconnects.
            matches=[]
            for path in list_devices():
                try:
                    d=InputDevice(path)
                    if wanted in (d.name or "").lower():
                        matches.append(d)
                    else:
                        d.close()
                except Exception:
                    pass

            if not matches:
                if current_path is not None:
                    print(f"[GEAR] media controller disconnected: {name or 'SmartRemote'}")
                    current_path=None
                await asyncio.sleep(2)
                continue

            device=matches[0]
            for extra in matches[1:]:
                extra.close()

            if device.path!=current_path:
                current_path=device.path
                print(f"[GEAR] media controller ready: {device.name} ({device.path})")
                gears.show()

            async for event in device.async_read_loop():
                if event.type!=ecodes.EV_KEY:
                    continue

                # value 1 = key down, 2 = autorepeat, 0 = release.
                # Only act on the initial key press.
                if event.value!=1:
                    continue

                code=event.code

                if code in (
                        ecodes.KEY_NEXTSONG,
                        ecodes.KEY_VOLUMEUP,
                        getattr(ecodes, "KEY_FASTFORWARD", -1)):
                    gears.up()

                elif code in (
                        ecodes.KEY_PREVIOUSSONG,
                        ecodes.KEY_VOLUMEDOWN,
                        getattr(ecodes, "KEY_REWIND", -1)):
                    gears.down()

                else:
                    keyname=ecodes.KEY.get(code, str(code))
                    print(f"[GEAR] unmapped key {keyname}")

        except asyncio.CancelledError:
            raise
        except PermissionError as e:
            print("[GEAR] cannot read input device:",e)
            print("[GEAR] add the user to the 'input' group or run with suitable permissions")
            await asyncio.sleep(5)
        except OSError:
            # Typical when a wireless input device disconnects.
            current_path=None
            await asyncio.sleep(2)
        except Exception as e:
            print("[GEAR] media controller error",e)
            await asyncio.sleep(2)
        finally:
            if device is not None:
                try:
                    device.close()
                except Exception:
                    pass
                device=None
