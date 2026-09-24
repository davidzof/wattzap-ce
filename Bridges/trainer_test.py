#!/usr/bin/env python3
import argparse
import json
import random
import socket
import time

HOST = "127.0.0.1"
PORT = 28773

WHEEL_SIZE_CM = 213.3

TARGET_SPEED_KMH = 20.0
TARGET_CADENCE = 85
TARGET_HEART_RATE = 140
TARGET_POWER = 200


def elapsed_ms_for_speed(speed_kmh, rotations=1):
    distance_km = rotations * WHEEL_SIZE_CM / 100000.0
    return int(round(distance_km * 3600000.0 / speed_kmh))


def parse_args():
    parser = argparse.ArgumentParser(
        description="Simple WattzAp trainer-input test generator"
    )
    parser.add_argument(
        "--power-only",
        type=int,
        metavar="WATTS",
        help=(
            "emit power, cadence and heart rate only, centred on WATTS; "
            "no rotations/speed fields are sent"
        ),
    )
    parser.add_argument(
        "--gear-count",
        type=int,
        default=24,
        help="number of virtual gears to simulate in power mode (default: 24)",
    )
    parser.add_argument(
        "--start-gear",
        type=int,
        default=12,
        help="starting virtual gear in power mode (default: 12)",
    )
    parser.add_argument(
        "--gear-change-seconds",
        type=float,
        default=5.0,
        help="seconds between simulated gear changes in power mode (default: 5)",
    )
    return parser.parse_args()


def main():
    args = parse_args()
    power_only = args.power_only is not None
    target_power = args.power_only if power_only else TARGET_POWER

    gear_count = max(1, args.gear_count)
    gear = max(1, min(gear_count, args.start_gear))
    gear_direction = 1
    gear_change_seconds = max(0.5, args.gear_change_seconds)
    next_gear_change = time.monotonic() + gear_change_seconds

    with socket.create_connection((HOST, PORT)) as sock:
        print(f"Connected to WattzAp at {HOST}:{PORT}")

        try:
            while True:
                power = max(0, int(round(random.gauss(target_power, 10))))
                cadence = max(0, int(round(random.gauss(TARGET_CADENCE, 3))))
                heart_rate = max(0, int(round(random.gauss(TARGET_HEART_RATE, 2))))

                if power_only:
                    now = time.monotonic()
                    if now >= next_gear_change:
                        gear += gear_direction
                        if gear >= gear_count:
                            gear = gear_count
                            gear_direction = -1
                        elif gear <= 1:
                            gear = 1
                            gear_direction = 1

                        next_gear_change = now + gear_change_seconds

                    sample = {
                        "power": power,
                        "cadence": cadence,
                        "heartRate": heart_rate,
                        "gear": gear,
                        "gearCount": gear_count,
                    }

                    message = json.dumps(sample, separators=(",", ":"))
                    print(
                        f"{power:3d} W  "
                        f"{cadence:3d} rpm  "
                        f"{heart_rate:3d} bpm  "
                        f"gear {gear}/{gear_count}  -> {message}"
                    )

                    sock.sendall((message + "\n").encode("utf-8"))
                    time.sleep(1.0)
                    continue

                speed = max(1.0, random.gauss(TARGET_SPEED_KMH, 0.6))
                rotations = 1
                elapsed_ms = elapsed_ms_for_speed(speed, rotations)

                sample = {
                    "rotations": rotations,
                    "elapsedMs": elapsed_ms,
                    "cadence": cadence,
                    "heartRate": heart_rate,
                }

                message = json.dumps(sample, separators=(",", ":"))
                print(
                    f"{speed:5.2f} km/h  "
                    f"{cadence:3d} rpm  "
                    f"{heart_rate:3d} bpm  -> {message}"
                )

                sock.sendall((message + "\n").encode("utf-8"))
                time.sleep(elapsed_ms / 1000.0)

        except KeyboardInterrupt:
            print("\nStopped.")


if __name__ == "__main__":
    main()
