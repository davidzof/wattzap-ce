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
    return parser.parse_args()


def main():
    args = parse_args()
    power_only = args.power_only is not None
    target_power = args.power_only if power_only else TARGET_POWER

    with socket.create_connection((HOST, PORT)) as sock:
        print(f"Connected to WattzAp at {HOST}:{PORT}")

        try:
            while True:
                power = max(0, int(round(random.gauss(target_power, 10))))
                cadence = max(0, int(round(random.gauss(TARGET_CADENCE, 3))))
                heart_rate = max(0, int(round(random.gauss(TARGET_HEART_RATE, 2))))

                if power_only:
                    sample = {
                        "power": power,
                        "cadence": cadence,
                        "heartRate": heart_rate,
                    }

                    message = json.dumps(sample, separators=(",", ":"))
                    print(
                        f"{power:3d} W  "
                        f"{cadence:3d} rpm  "
                        f"{heart_rate:3d} bpm  -> {message}"
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
                    "power": power,
                }

                message = json.dumps(sample, separators=(",", ":"))
                print(
                    f"{speed:5.2f} km/h  "
                    f"{cadence:3d} rpm  "
                    f"{heart_rate:3d} bpm  "
                    f"{power:3d} W  -> {message}"
                )

                sock.sendall((message + "\n").encode("utf-8"))
                time.sleep(elapsed_ms / 1000.0)

        except KeyboardInterrupt:
            print("\nStopped.")


if __name__ == "__main__":
    main()
