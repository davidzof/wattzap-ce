## External Trainer TCP/IP Protocol

WattzAp is hardware independent. It communicates with external sensors over a simple TCP/IP interface.
Several bridges supporting this protocol are provided in the Bridges directory.

ANT+, BLE, FTMS, custom sensors and test generators can all communicate with WattzAp through the same protocol.

### Connection

WattzAp acts as a TCP server.

Default endpoint:

```text
Host: 127.0.0.1
Port: 28773
```

An external bridge connects as a TCP client and keeps the connection open.

The protocol is bidirectional:

```text
Bridge  ---> WattzAp    sensor / trainer telemetry
Bridge  <--- WattzAp    trainer control commands
```

Messages are encoded as UTF-8 JSON.

Each message is a single JSON object terminated by a newline. This is therefore a newline-delimited JSON, or NDJSON, protocol.
A TCP packet may contain part of a message or several messages. Implementations should always read complete lines rather than assuming that one TCP packet corresponds to one JSON message.

---

## Telemetry: Bridge to WattzAp

All telemetry fields are optional.

A bridge should send only the measurements that are available in the current update.

### Power

```json
{"power":218}
```

`power` is instantaneous power in watts.

If measured power and wheel-speed data are both available, WattzAp gives measured power priority when calculating virtual road speed.

---

### Heart rate

```json
{"heartRate":142}
```

`heartRate` is measured in beats per minute.

Heart-rate updates may be sent independently of other telemetry.

---

### Cadence

```json
{"cadence":87}
```

`cadence` is measured in revolutions per minute.

Cadence updates may be sent independently of other telemetry.

---

### Wheel speed

Wheel speed is represented using wheel rotations and elapsed time:

```json
{"rotations":1,"elapsedMs":384}
```

Fields:

```text
rotations   Number of wheel rotations during the measurement interval
elapsedMs   Duration of that interval in milliseconds
```

`rotations` is a delta, not a cumulative wheel counter.

For example, with a wheel circumference of 213.3 cm:

```text
1 rotation in 384 ms ~= 20 km/h
```

The bridge should convert any hardware-specific counters or timestamps into this normali
