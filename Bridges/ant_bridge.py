#!/usr/bin/env python3
"""
WattzAp ANT+ bridge.

Discovers ANT+ cycling sensors with OpenANT and forwards normalized data to
WattzAp as newline-delimited JSON over TCP.

Requirements:
    Python 3.8+
    pip install openant

Examples:
    python3 wattzap_ant_bridge.py --scan
    python3 wattzap_ant_bridge.py --configure
    python3 wattzap_ant_bridge.py --power 12345 --hr 54321 --save
    python3 wattzap_ant_bridge.py --speed-cadence 12345 --hr 54321 --save
    python3 wattzap_ant_bridge.py
"""

from __future__ import annotations

import argparse
import json
import socket
import sys
import threading
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Dict, Iterable, List, Optional

try:
    from openant.devices import ANTPLUS_NETWORK_KEY
    from openant.devices.bike_speed_cadence import BikeCadence, BikeSpeed, BikeSpeedCadence
    from openant.devices.common import DeviceType
    from openant.devices.heart_rate import HeartRate
    from openant.devices.power_meter import PowerMeter
    from openant.devices.scanner import Scanner
    from openant.easy.node import Node
except ImportError as exc:
    print("OpenANT is required. Install it with: pip install openant", file=sys.stderr)
    raise SystemExit(2) from exc

DEFAULT_HOST = "127.0.0.1"
DEFAULT_PORT = 28773
DEFAULT_SCAN_SECONDS = 10.0
DEFAULT_CONFIG = Path.home() / ".wattzap-ant-bridge.json"

SUPPORTED_TYPES = {
    DeviceType.PowerMeter.value: "power",
    DeviceType.HeartRate.value: "heartRate",
    DeviceType.BikeSpeed.value: "speed",
    DeviceType.BikeCadence.value: "cadence",
    DeviceType.BikeSpeedCadence.value: "speedCadence",
}

CONFIG_KEYS = ("power", "speed", "speedCadence", "cadence", "heartRate")


@dataclass(frozen=True)
class FoundDevice:
    device_id: int
    device_type: int
    transmission_type: int

    @property
    def ant_name(self) -> str:
        return DeviceType(self.device_type).name

    @property
    def role(self) -> Optional[str]:
        return SUPPORTED_TYPES.get(self.device_type)


class WattzApClient:
    def __init__(self, host: str, port: int, retry_seconds: float = 2.0):
        self.host = host
        self.port = port
        self.retry_seconds = retry_seconds
        self._socket: Optional[socket.socket] = None
        self._lock = threading.Lock()
        self._last_attempt = 0.0
        self._announced_failure = False

    def _connect_locked(self) -> bool:
        if self._socket is not None:
            return True

        now = time.monotonic()
        if now - self._last_attempt < self.retry_seconds:
            return False
        self._last_attempt = now

        try:
            self._socket = socket.create_connection((self.host, self.port), timeout=2.0)
            self._socket.settimeout(None)
            print(f"[WattzAp] connected to {self.host}:{self.port}")
            self._announced_failure = False
            return True
        except OSError as exc:
            if not self._announced_failure:
                print(f"[WattzAp] not available at {self.host}:{self.port} ({exc}); will retry")
                self._announced_failure = True
            self._socket = None
            return False

    def send(self, payload: Dict[str, Any]) -> None:
        if not payload:
            return
        encoded = (json.dumps(payload, separators=(",", ":")) + "\n").encode("utf-8")
        with self._lock:
            if not self._connect_locked():
                return
            try:
                assert self._socket is not None
                self._socket.sendall(encoded)
            except OSError:
                self._close_locked()

    def _close_locked(self) -> None:
        if self._socket is not None:
            try:
                self._socket.close()
            except OSError:
                pass
        self._socket = None

    def close(self) -> None:
        with self._lock:
            self._close_locked()


class WheelDelta:
    """Convert cumulative ANT+ wheel values to WattzAp deltas."""

    def __init__(self):
        self.last_event_time: Optional[float] = None
        self.last_revolutions: Optional[int] = None

    def update(self, data: Any) -> Optional[Dict[str, int]]:
        event_time = float(data.bike_speed_event_time[1])
        revolutions = int(data.cumulative_speed_revolution[1])

        if self.last_event_time is None or self.last_revolutions is None:
            self.last_event_time = event_time
            self.last_revolutions = revolutions
            return None

        elapsed = event_time - self.last_event_time
        if elapsed < 0:
            elapsed += 64.0
        rotation_delta = (revolutions - self.last_revolutions) & 0xFFFF

        self.last_event_time = event_time
        self.last_revolutions = revolutions

        if elapsed <= 0:
            return None

        elapsed_ms = int(round(elapsed * 1000.0))
        if elapsed_ms <= 0:
            return None

        return {"rotations": rotation_delta, "elapsedMs": elapsed_ms}


class CadenceDelta:
    """Calculate cadence from cumulative ANT+ crank values."""

    def __init__(self):
        self.last_event_time: Optional[float] = None
        self.last_revolutions: Optional[int] = None

    def update(self, data: Any) -> Optional[int]:
        event_time = float(data.bike_cadence_event_time[1])
        revolutions = int(data.cumulative_cadence_revolution[1])

        if self.last_event_time is None or self.last_revolutions is None:
            self.last_event_time = event_time
            self.last_revolutions = revolutions
            return None

        elapsed = event_time - self.last_event_time
        if elapsed < 0:
            elapsed += 64.0
        rotation_delta = (revolutions - self.last_revolutions) & 0xFFFF

        self.last_event_time = event_time
        self.last_revolutions = revolutions

        if elapsed <= 0:
            return None

        cadence = int(round(60.0 * rotation_delta / elapsed))
        return cadence if 0 <= cadence <= 300 else None


def new_node() -> Node:
    node = Node()
    node.set_network_key(0x00, ANTPLUS_NETWORK_KEY)
    return node


def scan_devices(seconds: float) -> List[FoundDevice]:
    node = new_node()
    scanner = Scanner(node)
    found: Dict[tuple, FoundDevice] = {}

    def on_found(device_tuple):
        device_id, device_type, transmission_type = device_tuple
        if device_type in SUPPORTED_TYPES:
            dev = FoundDevice(device_id, device_type, transmission_type)
            found[(device_id, device_type, transmission_type)] = dev
            print(f"  found {dev.ant_name:<18} id={device_id:<6} tx={transmission_type}")

    scanner.on_found = on_found
    thread = threading.Thread(target=node.start, name="ANT-Scanner", daemon=True)
    print(f"Scanning ANT+ for {seconds:.0f} seconds...")
    thread.start()

    try:
        time.sleep(seconds)
    finally:
        try:
            scanner.close_channel()
        except Exception:
            pass
        node.stop()
        thread.join(timeout=2.0)

    return sorted(found.values(), key=lambda d: (d.device_type, d.device_id))


def print_devices(devices: Iterable[FoundDevice]) -> None:
    devices = list(devices)
    if not devices:
        print("No supported ANT+ devices found.")
        return

    print("\nANT+ devices")
    print("-" * 58)
    print(f"{'Profile':<20} {'ID':>8} {'Tx':>5} {'WattzAp role':>18}")
    print("-" * 58)
    for dev in devices:
        print(f"{dev.ant_name:<20} {dev.device_id:>8} {dev.transmission_type:>5} {str(dev.role):>18}")


def load_config(path: Path) -> Dict[str, Optional[int]]:
    config: Dict[str, Optional[int]] = {key: None for key in CONFIG_KEYS}
    if not path.exists():
        return config
    try:
        raw = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        print(f"Could not read config {path}: {exc}", file=sys.stderr)
        return config
    for key in CONFIG_KEYS:
        value = raw.get(key)
        if value is not None:
            config[key] = int(value)
    return config


def validate_config(config: Dict[str, Optional[int]]) -> None:
    primary = [name for name in ("power", "speed", "speedCadence") if config.get(name) is not None]
    if len(primary) > 1:
        raise ValueError(
            "Power, speed, and speed+cadence are mutually exclusive primary sources; "
            f"selected: {', '.join(primary)}"
        )


def save_config(path: Path, config: Dict[str, Optional[int]]) -> None:
    validate_config(config)
    output = {key: config.get(key) for key in CONFIG_KEYS if config.get(key) is not None}
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(output, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print(f"Saved configuration to {path}")


def candidates_for_role(devices: Iterable[FoundDevice], role: str) -> List[FoundDevice]:
    return [device for device in devices if device.role == role]


def prompt_device(label: str, candidates: List[FoundDevice], current: Optional[int]) -> Optional[int]:
    print(f"\n{label}")
    if current is not None:
        print(f"  current: {current}")
    for index, device in enumerate(candidates, 1):
        print(f"  {index}. {device.ant_name} #{device.device_id}")
    response = input("  select number/device id (Enter keeps current, '-' clears): ").strip()
    if response == "":
        return current
    if response == "-":
        return None
    try:
        numeric = int(response)
    except ValueError:
        print("  invalid selection; keeping current")
        return current
    if 1 <= numeric <= len(candidates):
        return candidates[numeric - 1].device_id
    return numeric


def interactive_configure(devices: List[FoundDevice], config: Dict[str, Optional[int]]) -> Dict[str, Optional[int]]:
    print("\nConfigure WattzAp ANT+ Bridge")
    print("Power and wheel-speed sources are mutually exclusive.")

    primary_options = [
        ("none", "None", []),
        ("power", "Power meter", candidates_for_role(devices, "power")),
        ("speed", "Speed sensor", candidates_for_role(devices, "speed")),
        ("speedCadence", "Speed + cadence sensor", candidates_for_role(devices, "speedCadence")),
    ]

    print("\nPrimary source")
    for index, (_, label, candidates) in enumerate(primary_options, 1):
        ids = ", ".join(str(d.device_id) for d in candidates) or "none found"
        print(f"  {index}. {label} ({ids})")

    current_primary = next((key for key in ("power", "speed", "speedCadence") if config.get(key) is not None), None)
    response = input(f"  select [current={current_primary or 'none'}; Enter keeps]: ").strip()

    if response:
        try:
            selected_index = int(response) - 1
            key, _, candidates = primary_options[selected_index]
        except (ValueError, IndexError):
            print("  invalid selection; keeping current")
        else:
            for primary_key in ("power", "speed", "speedCadence"):
                config[primary_key] = None
            if key != "none":
                config[key] = prompt_device(f"Select {key}", candidates, None)

    config["cadence"] = prompt_device(
        "Cadence sensor", candidates_for_role(devices, "cadence"), config.get("cadence")
    )
    config["heartRate"] = prompt_device(
        "Heart-rate sensor", candidates_for_role(devices, "heartRate"), config.get("heartRate")
    )

    validate_config(config)
    return config


def apply_cli_overrides(config: Dict[str, Optional[int]], args: argparse.Namespace) -> bool:
    changed = False
    mapping = {
        "power": args.power,
        "speed": args.speed,
        "speedCadence": args.speed_cadence,
        "cadence": args.cadence,
        "heartRate": args.hr,
    }
    for key, value in mapping.items():
        if value is not None:
            config[key] = value
            changed = True
    validate_config(config)
    return changed


class Bridge:
    def __init__(self, config: Dict[str, Optional[int]], host: str, port: int):
        self.config = config
        self.client = WattzApClient(host, port)
        self.node = new_node()
        self.devices: List[Any] = []
        self._wheel = WheelDelta()
        self._combo_cadence = CadenceDelta()
        self._cadence = CadenceDelta()

    def _handle_speed(self, data: Any) -> None:
        payload = self._wheel.update(data)
        if payload is not None:
            self.client.send(payload)

    def _handle_cadence(self, data: Any, tracker: CadenceDelta) -> None:
        cadence = tracker.update(data)
        if cadence is not None:
            self.client.send({"cadence": cadence})

    def _handle_hr(self, data: Any) -> None:
        value = int(data.heart_rate)
        if 0 <= value <= 255:
            self.client.send({"heartRate": value})

    def _handle_power(self, data: Any) -> None:
        watts = int(data.instantaneous_power)
        if watts >= 0:
            payload: Dict[str, int] = {"power": watts}
            cadence = int(data.cadence)
            if self.config.get("cadence") is None and 0 <= cadence < 255:
                payload["cadence"] = cadence
            self.client.send(payload)

    def _add_power(self, device_id: int) -> None:
        dev = PowerMeter(self.node, device_id=device_id)

        def on_data(_page, page_name, data):
            if page_name == "standard_power":
                self._handle_power(data)

        dev.on_device_data = on_data
        self.devices.append(dev)

    def _add_speed(self, device_id: int) -> None:
        dev = BikeSpeed(self.node, device_id=device_id)
        dev.on_device_data = lambda _page, page_name, data: self._handle_speed(data) if page_name == "bike_speed" else None
        self.devices.append(dev)
        print(f"[ANT+] speed sensor #{device_id}")

    def _add_speed_cadence(self, device_id: int) -> None:
        dev = BikeSpeedCadence(self.node, device_id=device_id)
        def on_data(_page, page_name, data):
            if page_name == "bike_speed":
                self._handle_speed(data)
            elif page_name == "bike_cadence":
                self._handle_cadence(data, self._combo_cadence)
        dev.on_device_data = on_data
        self.devices.append(dev)
        print(f"[ANT+] speed+cadence sensor #{device_id}")

    def _add_cadence(self, device_id: int) -> None:
        dev = BikeCadence(self.node, device_id=device_id)
        dev.on_device_data = lambda _page, page_name, data: self._handle_cadence(data, self._cadence) if page_name == "bike_cadence" else None
        self.devices.append(dev)
        print(f"[ANT+] cadence sensor #{device_id}")

    def _add_hr(self, device_id: int) -> None:
        dev = HeartRate(self.node, device_id=device_id)

        def on_data(_page, page_name, data):
            if page_name == "heart_rate":
                self._handle_hr(data)

        dev.on_device_data = on_data
        self.devices.append(dev)

    def configure_devices(self) -> None:
        validate_config(self.config)
        if self.config.get("power") is not None:
            self._add_power(int(self.config["power"]))
        elif self.config.get("speed") is not None:
            self._add_speed(int(self.config["speed"]))
        elif self.config.get("speedCadence") is not None:
            self._add_speed_cadence(int(self.config["speedCadence"]))

        if self.config.get("cadence") is not None:
            self._add_cadence(int(self.config["cadence"]))
        if self.config.get("heartRate") is not None:
            self._add_hr(int(self.config["heartRate"]))

    def run(self) -> None:
        self.configure_devices()
        if not self.devices:
            raise RuntimeError("No ANT+ devices configured. Use --scan, --configure, or device-id options.")

        print("\nBridge running. Press Ctrl-C to stop.")
        try:
            self.node.start()
        except KeyboardInterrupt:
            print("\nStopping...")
        finally:
            for device in self.devices:
                try:
                    device.close_channel()
                except Exception:
                    pass
            self.node.stop()
            self.client.close()


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="ANT+ to WattzAp JSON/TCP bridge")
    parser.add_argument("--config", type=Path, default=DEFAULT_CONFIG)
    parser.add_argument("--host", default=DEFAULT_HOST)
    parser.add_argument("--port", type=int, default=DEFAULT_PORT)
    parser.add_argument("--scan", action="store_true", help="scan for supported ANT+ devices and exit")
    parser.add_argument("--scan-seconds", type=float, default=DEFAULT_SCAN_SECONDS)
    parser.add_argument("--configure", action="store_true", help="scan and interactively choose devices")
    parser.add_argument("--power", type=int, help="ANT+ power-meter device ID")
    parser.add_argument("--speed", type=int, help="ANT+ speed-sensor device ID")
    parser.add_argument("--speed-cadence", type=int, help="ANT+ combined speed/cadence device ID")
    parser.add_argument("--cadence", type=int, help="ANT+ cadence device ID")
    parser.add_argument("--hr", type=int, help="ANT+ heart-rate device ID")
    parser.add_argument("--save", action="store_true", help="save command-line selections")
    parser.add_argument("--show-config", action="store_true")
    return parser


def main() -> int:
    args = build_parser().parse_args()
    config = load_config(args.config)

    try:
        changed = apply_cli_overrides(config, args)
    except ValueError as exc:
        print(f"Configuration error: {exc}", file=sys.stderr)
        return 2

    if args.scan:
        print_devices(scan_devices(args.scan_seconds))
        return 0

    if args.configure:
        devices = scan_devices(args.scan_seconds)
        print_devices(devices)
        try:
            config = interactive_configure(devices, config)
        except ValueError as exc:
            print(f"Configuration error: {exc}", file=sys.stderr)
            return 2
        save_config(args.config, config)
        changed = False

    if args.save and changed:
        save_config(args.config, config)

    if args.show_config:
        print(json.dumps(config, indent=2, sort_keys=True))
        return 0

    try:
        validate_config(config)
        Bridge(config, args.host, args.port).run()
        return 0
    except (ValueError, RuntimeError) as exc:
        print(f"Error: {exc}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
