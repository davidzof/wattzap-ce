# WattzAp Hardware Bridges

WattzAp uses small external Python programs to communicate with training hardware.

The purpose of the bridge layer is to keep WattzAp itself independent of ANT+, Bluetooth LE, FTMS and other hardware-specific protocols.

```text
ANT+ sensors ──────> ant_bridge.py ──┐
                                     │
BLE / FTMS devices -> ble_bridge.py ─┼─> TCP/JSON -> WattzAp
                                     │
Test simulator ──────────────────────┘
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

The ANT+ and Bluetooth bridges require different Python packages.

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

The Bluetooth bridge uses the `bleak` Python library.

Install it with:

```bash
pip install bleak
```

## Supported BLE devices

The bridge currently supports:

* Bluetooth heart-rate monitors
* Cycling Speed and Cadence (CSC) sensors
* FTMS smart trainers

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

## Running

Start WattzAp first, then run:

```bash
python ble_bridge.py
```

The bridge connects to WattzAp and begins forwarding sensor data.

---

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

### Gradient / simulation mode

WattzAp sends:

```json
{"type":"control","gradient":4.0}
```

The bridge translates this into the FTMS Indoor Bike Simulation Parameters command.

The gradient value has already been adjusted by WattzAp for any trainer-difficulty setting.

The bridge must therefore not apply gradient scaling again.

Repeated identical gradient values are ignored so that unnecessary FTMS control commands are not sent.

### ERG mode

For structured workouts WattzAp can send:

```json
{"type":"control","targetPower":250}
```

The bridge translates this into an FTMS Set Target Power command.

The trainer then automatically changes resistance in order to keep the rider close to the requested power.

Repeated identical target-power values are ignored.

Conceptually:

```text
Simulation mode
    route gradient -> FTMS simulation control

ERG mode
    workout target power -> FTMS target power
```

---

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

This is useful because FTMS trainers are not required to support every possible control mode.

---

# Test / Simulator Bridge

The test bridge provides synthetic training data without requiring any physical sensors or trainer.

It is useful for:

* Testing WattzAp without ANT+ or Bluetooth hardware
* Testing TCP connectivity
* Testing power-based virtual speed
* Testing cadence and heart-rate display
* Developing UI changes
* Reproducing bugs

A typical synthetic message might be:

```json
{
  "power":247,
  "cadence":86,
  "heartRate":141
}
```

or a simulated wheel-speed sample:

```json
{
  "rotations":1,
  "elapsedMs":384
}
```

The simulator connects to the same WattzAp TCP endpoint as the hardware bridges:

```text
127.0.0.1:28773
```

This is intentional: from WattzAp's point of view there is no distinction between simulated and real hardware.

---

# Architecture

The bridge design deliberately separates hardware handling from WattzAp application logic.

The bridge is responsible for:

* Device discovery
* Pairing / device selection
* ANT+ protocol handling
* BLE service and characteristic handling
* Counter rollover
* Timestamp conversion
* FTMS commands and acknowledgements
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
