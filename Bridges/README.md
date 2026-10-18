# WattzAp Hardware Bridges

WattzAp uses small external Python programs to communicate with training hardware.

The purpose of the bridge layer is to keep WattzAp itself independent of ANT+, Bluetooth LE, FTMS and other hardware-specific protocols.

```text
ANT+ sensors ──────> ant_bridge.py ──┐
                                     │
BLE / FTMS devices -> ble_bridge.py ─┼─> TCP/JSON -> WattzAp
                                     │
Test simulator ───> trainer_test.py ──┘
```

WattzAp acts as a TCP server on:

```text
127.0.0.1:28773
```

The bridges connect as TCP clients and exchange newline-delimited JSON messages.

See the TCP/IP protocol documentation for the full message format.

---

## Python environment

The bridges are designed to run as ordinary Python scripts.

Using a virtual environment is recommended:

```bash
python3 -m venv .venv
source .venv/bin/activate
python -m pip install --upgrade pip
```

On Windows:

```text
.venv\Scripts\activate
```

The ANT+ and Bluetooth bridges require different Python packages. On Linux, virtual gear control from a BLE media remote also uses the `evdev` package because BlueZ exposes paired BLE HID remotes through the Linux input subsystem.

---

# ANT+ Bridge

```text
ant_bridge.py
```

The ANT+ bridge uses the `openant` Python library to discover and read ANT+ sensors.

Install the dependency with:

```bash
pip install openant
```

## Supported ANT+ devices

The bridge can work with:

* Speed sensors
* Speed + Cadence sensors
* Cadence sensors
* Heart-rate monitors
* Power meters

The bridge performs the ANT+-specific processing and converts the measurements into the hardware-neutral WattzAp TCP format.

For example, ANT+ wheel counters and 1/1024-second timestamps are converted into:

```json
{"rotations":1,"elapsedMs":384}
```

rather than exposing ANT+ counters directly to WattzAp.

Power, cadence and heart rate are sent directly:

```json
{"power":235}
```

```json
{"cadence":86}
```

```json
{"heartRate":142}
```

Measurements can also be combined where appropriate.

## Scanning for ANT+ devices

Run:

```bash
python ant_bridge.py --scan
```

This displays compatible ANT+ devices detected by the ANT USB adapter.

## Configuration

Run:

```bash
python ant_bridge.py --configure
```

The bridge allows the desired sensors to be selected.

The selected device IDs are stored in:

```text
~/.wattzap-ant-bridge.json
```

This avoids having to select the sensors again each time the bridge is started.

Power and speed are treated as alternative primary sources. If measured power is available, WattzAp normally uses that rather than calculating power from wheel speed.

## Running

Start WattzAp first, then run:

```bash
python ant_bridge.py
```

The bridge connects to WattzAp on:

```text
127.0.0.1:28773
```

and begins forwarding sensor measurements.

---

# Bluetooth / FTMS Bridge

```text
ble_bridge.py
```

The Bluetooth bridge uses the `bleak` Python library for BLE GATT devices
such as trainers, heart-rate straps and CSC sensors.

Install it with:

```bash
pip install bleak
```

On Linux, virtual gears can also be controlled with a paired BLE HID/media
remote. These remotes are normally claimed by BlueZ and exposed as
`/dev/input/event*` devices, so the bridge uses `evdev` for button input:

```bash
pip install evdev
```

The user running the bridge must have permission to read the input device.
On many Linux systems this means adding the user to the `input` group:

```bash
sudo usermod -aG input $USER
```

Log out and back in after changing group membership.

## Supported BLE devices

The bridge currently supports:

* Bluetooth heart-rate monitors
* Cycling Speed and Cadence (CSC) sensors
* FTMS smart trainers
* BLE HID/media remotes for virtual gear changes on Linux

Relevant Bluetooth services include:

```text
Heart Rate Service          0x180D
Cycling Speed and Cadence   0x1816
Fitness Machine Service     0x1826
```

## Scanning

Run:

```bash
python ble_bridge.py --scan
```

The bridge displays compatible Bluetooth devices that it can see.

## Configuration

Run:

```bash
python ble_bridge.py --configure
```

The selected devices are stored in:

```text
~/.wattzap-ble-bridge.json
```

When virtual gears are going to be used, a BLE media controller can be
configured. The remote should first be paired and trusted with BlueZ. For example:

```bash
bluetoothctl
```

Then:

```text
agent on
default-agent
pair <device-address>
trust <device-address>
connect <device-address>
```

A correctly paired HID remote should normally appear as a Linux input device,
for example:

```text
/dev/input/event14: SmartRemote
```

`evtest` can be used to verify that the buttons produce media-key events such
as `KEY_VOLUMEUP`, `KEY_VOLUMEDOWN`, `KEY_NEXTSONG` and
`KEY_PREVIOUSSONG`.

## Running

Start WattzAp first, then run:

```bash
python ble_bridge.py
```

The bridge connects to WattzAp and begins forwarding sensor data.

By default the bridge assumes the trainer is being used with a normal physical
drivetrain/cassette. In this mode route gradients are applied with FTMS
simulation control.

To enable single-cog / virtual-shifting mode, run:

```bash
python ble_bridge.py --virtual-gears
```

In virtual-gears mode the configured BLE media controller is used for gear
changes and the bridge combines route gradient with virtual gear position to
produce an FTMS target resistance.

---


## FTMS calibration

If the configured trainer advertises FTMS Spin Down Control support, calibration
can be run directly from the BLE bridge. WattzAp does not need to be running.

Run:

```bash
python ble_bridge.py --calibrate
```

The bridge connects to the configured FTMS trainer, requests control and starts
the FTMS spin-down calibration procedure.

A typical session looks like:

```text
[CAL] connecting to trainer XX:XX:XX:XX:XX:XX
[CAL] FTMS control acquired
[CAL] spin-down started; accelerate to 19.50-23.00 km/h
[CAL] target speed reached - stop pedalling and coast
[CAL] calibration successful
```

The exact target-speed range is supplied by the trainer.

Calibration uses the standard FTMS Spin Down Control procedure (`0x13`) and
Fitness Machine Status notifications. It is only available when the trainer
advertises Spin Down Control support in its FTMS feature flags.

After a successful calibration, the bridge writes the UTC completion time to the
normal BLE configuration file:

```text
~/.wattzap-ble-bridge.json
```

For example:

```json
{
  "trainer": "XX:XX:XX:XX:XX:XX",
  "cadence": null,
  "heartRate": "YY:YY:YY:YY:YY:YY",
  "mediaController": null,
  "lastCalibration": "2026-10-15T21:35:42Z"
}
```

On normal startup, if a trainer is configured, the bridge checks this timestamp.
A warning is printed if no successful calibration has been recorded or if the
last calibration is at least 30 days old:

```text
[FTMS] WARNING: trainer calibration is 34 days old. Run ble_bridge.py --calibrate.
```

The warning is informational only; normal trainer operation continues.


## FTMS smart trainers

For an FTMS trainer, the bridge can receive:

* Power
* Cadence
* Heart rate, where supplied by the trainer

The trainer's reported instantaneous speed is deliberately not used for WattzAp route progression.

When measured power is available, WattzAp calculates its own virtual road speed using the rider's power, route and physics model.

This means trainer flywheel speed does not have to correspond to the simulated road speed.

---

## FTMS control

Unlike the ANT+ bridge, the FTMS bridge is bidirectional.

```text
Trainer -> BLE bridge -> WattzAp
WattzAp -> BLE bridge -> Trainer
```

WattzAp can therefore control a compatible smart trainer.

### Slope / virtual gear mode

For a normal GPX route WattzAp sends the current route gradient:

```json
{"type":"control","gradient":4.0}
```

When no virtual gear controller is configured, the bridge can translate this
into the FTMS Indoor Bike Simulation Parameters command.

When `--virtual-gears` is enabled, the bridge instead combines:

```text
route gradient
      +
current virtual gear
      |
      v
FTMS target resistance
```

The trainer's Supported Resistance Level Range characteristic is read at
startup so the bridge uses the resistance scale advertised by the trainer
rather than assuming a fixed range.

For example, a trainer may report:

```text
[FTMS] resistance range 0..100, increment 1
```

The rider starts in a middle virtual gear, currently `12/24`.

The route gradient provides the main change in load while the virtual gear
allows the rider to adjust the resistance around that load. On climbs the
rider can shift down; on descents the rider can shift up to retain enough
resistance to continue producing power.

Typical console output is:

```text
[GEAR] 12/24
[FTMS] resistance target 73
[GEAR] 12/24 gradient 3.00% -> resistance 73
[GEAR] 11/24
[FTMS] resistance target 70
```

The virtual gear controller currently maps common media buttons as follows:

```text
Volume Up / Next Track       -> gear up
Volume Down / Previous Track -> gear down
```

The bridge does not require cadence data for virtual gearing.

### Gradient scaling / trainer difficulty

Gradient scaling is controlled by the bridge rather than WattzAp:

```bash
python ble_bridge.py --gradient-scale 0.5
```

A value of `1.0` means 100% gradient feel and is the default. A value of `0.5`
means that an 8% route gradient is presented to the trainer as 4%.

The scaling applies to both physical-gearing simulation mode and virtual-gears
mode. WattzAp therefore continues to work with the true GPX gradient while the
bridge decides how strongly the attached trainer should reproduce it.

Examples:

```bash
# Physical cassette, full gradient
python ble_bridge.py

# Physical cassette, 50% trainer difficulty
python ble_bridge.py --gradient-scale 0.5

# Single cog / virtual gears, full gradient
python ble_bridge.py --virtual-gears

# Single cog / virtual gears, 50% trainer difficulty
python ble_bridge.py --virtual-gears --gradient-scale 0.5

# Virtual gears with a stronger gradient response
python ble_bridge.py \
  --virtual-gears \
  --gear-resistance-min 0.20 \
  --gear-resistance-max 0.65 \
  --gradient-resistance 0.035
```


### Virtual gear resistance mapping

When `--virtual-gears` is enabled, the bridge combines the selected virtual gear
with the route gradient to calculate an FTMS target resistance.

The default mapping is:

```text
gear 1 flat-road resistance:   0.20
gear 24 flat-road resistance:  0.80
gradient contribution:         0.02 per 1% gradient
```

These values are fractions of the trainer's reported resistance range. With a
trainer range of 0..100, the defaults therefore correspond approximately to:

```text
gear 1 on flat terrain  -> resistance 20
gear 24 on flat terrain -> resistance 80
each +1% gradient       -> +2 resistance points
```

The coefficients can be changed from the command line:

```bash
--gear-resistance-min FRACTION
--gear-resistance-max FRACTION
--gradient-resistance FRACTION_PER_PERCENT
```

For example, to reduce the total virtual-gear range while making gradients more
noticeable:

```bash
python ble_bridge.py \
  --virtual-gears \
  --gear-resistance-min 0.20 \
  --gear-resistance-max 0.65 \
  --gradient-resistance 0.035
```

This keeps gear 1 at 20% of the trainer resistance range, places gear 24 at 65%,
and adds 3.5% of the trainer range for each 1% of route gradient.

These settings are deliberately independent of `--gradient-scale`.

`--gradient-scale` changes the effective route gradient before trainer control
(for example `0.5` gives 50% trainer difficulty), while
`--gradient-resistance` controls how strongly that effective gradient changes
resistance in virtual-gears mode.

This makes it possible to tune the virtual drivetrain to a particular trainer
without changing the GPX route data or WattzAp itself.


The gear model is intended to provide a natural riding feel rather than claim
that a particular FTMS resistance level is an exact physical gradient. The
maximum slope a trainer can reproduce also depends on the trainer itself and,
for many trainers, flywheel speed.

### ERG mode

For structured workouts WattzAp can send:

```json
{"type":"control","targetPower":250}
```

The bridge translates this into an FTMS Set Target Power command.

The trainer then automatically changes resistance in order to keep the rider
close to the requested power.

Virtual gear changes do not alter ERG target power.

Repeated identical target-power values are ignored.

Conceptually:

```text
Physical gearing (default)
    route gradient
        -> optional --gradient-scale
        -> FTMS simulation parameters

Virtual gearing (--virtual-gears)
    route gradient
        -> optional --gradient-scale
        + virtual gear
        -> FTMS target resistance

ERG mode
    workout target power
        -> FTMS target power
```

`--virtual-gears` is an explicit drivetrain choice. The bridge does not try to
infer whether the trainer has a single cog or a physical cassette from FTMS
feature flags, because the trainer itself cannot know which drivetrain is
mounted.

The media controller is only used when `--virtual-gears` is supplied. Without
that flag the bridge does not start the media-controller input task and users
with a normal cassette can use their physical gears normally.

## FTMS feature detection

When an FTMS trainer connects, the bridge reads the trainer's Fitness Machine Feature characteristic.

This allows it to determine which control modes the trainer supports.

For example:

```text
[FTMS] controls relevant to WattzAp:
  resistance target (0x04): yes
  power target      (0x05): yes
  simulation params (0x11): yes
```

If resistance control is available, the bridge also reads the Supported
Resistance Level Range characteristic. This provides the trainer's minimum,
maximum and increment values used by the virtual gear mapping.

This is useful because FTMS trainers are not required to support every possible
control mode or resistance range.

---

# Test / Simulator Bridge

```text
trainer_test.py
```

The test bridge provides synthetic training data without requiring any physical
sensors or trainer.

It is useful for:

* Testing WattzAp without ANT+ or Bluetooth hardware
* Testing TCP connectivity
* Testing wheel-speed handling
* Testing power-based virtual speed
* Testing cadence and heart-rate display
* Testing virtual gear display and UI changes
* Reproducing bugs

The simulator connects to the same WattzAp TCP endpoint as the hardware
bridges:

```text
127.0.0.1:28773
```

This is intentional: from WattzAp's point of view there is no distinction
between simulated and real hardware.

## Speed mode

Run:

```bash
python trainer_test.py
```

Speed mode sends wheel rotation and elapsed-time data together with synthetic
cadence and heart rate.

For example:

```json
{
  "rotations":1,
  "elapsedMs":384,
  "cadence":85,
  "heartRate":140
}
```

Speed mode deliberately does **not** send a `power` field.

This allows the wheel-speed path in WattzAp to be tested without measured power
taking precedence over speed.

## Power mode

Run:

```bash
python trainer_test.py --power-only 200
```

Power mode sends synthetic power, cadence and heart rate without
`rotations`/`elapsedMs`.

It also simulates virtual gear changes so that gear handling and display can be
tested without a smart trainer or BLE media controller.

For example:

```json
{
  "power":201,
  "cadence":84,
  "heartRate":139,
  "gear":12,
  "gearCount":24
}
```

The gear moves automatically up and down through the configured range.

Available test options include:

```text
--power-only WATTS
--gear-count N
--start-gear N
--gear-change-seconds SECONDS
```

For example:

```bash
python trainer_test.py     --power-only 200     --gear-count 24     --start-gear 12     --gear-change-seconds 3
```

This makes it possible to test front-end rendering such as `12/24` without
requiring the FTMS trainer to be connected.

# Architecture

The bridge design deliberately separates hardware handling from WattzAp application logic.

The bridge is responsible for:

* Device discovery
* Pairing / device selection
* ANT+ protocol handling
* BLE service and characteristic handling
* Optional BLE HID/media remote input on Linux when `--virtual-gears` is used
* Virtual gear state and FTMS resistance mapping when virtual gearing is enabled
* Trainer difficulty / gradient scaling
* Counter rollover
* Timestamp conversion
* FTMS commands and acknowledgements
* FTMS spin-down calibration and calibration-age tracking
* Hardware reconnects
* Converting device-specific data into WattzAp JSON

WattzAp is responsible for:

* Route handling
* GPX gradient
* Virtual road speed
* Rider and bicycle mass
* Power modelling
* Video synchronisation
* Workouts
* ERG target selection
* Displaying virtual gear information
* Application UI and ride state

This separation makes it possible to add support for new hardware without adding hardware-specific code to WattzAp itself.

For example, a future bridge could support another BLE device, serial sensor, rowing machine or treadmill while still communicating with WattzAp through the same TCP interface.

---

# Typical startup

For ANT+:

```bash
source .venv/bin/activate
python ant_bridge.py
```

For Bluetooth / FTMS:

```bash
source .venv/bin/activate
python ble_bridge.py
```

WattzAp should normally be started before the bridge.

If the WattzAp connection is lost, the bridges are designed to reconnect rather than requiring the training hardware to be rediscovered.
