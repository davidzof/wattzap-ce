Wattzap Home Trainer Virtual Reality Software
=============================================

WattzAp connects your home trainer to your computer to record
virtual rides and follow training programmes.

WattzAp can:

  * display heart rate, cadence, rider speed and power both graphically and as real time data
  * follow workouts that target power, cadence and heart rate
  * synchronize the playback of compatible videos (Tacx RLV, Tacx TTS, Tacx Ergo, GPX, PWR) with your speed
  * share your results on sites such as Strava etc via GPX export
  
WattzAp works with standard turbo trainers, rollers and Smart Trainers. You don't need a complicated VR turbo trainer.
WattzAp is an open platform, you can create your own videos and workouts and share these with other users.

This is the open source version of the Wattzap Turbo Trainer Software. It is provided "as is" and without support.

Some videos demoing Wattzap. https://www.youtube.com/watch?v=P5DpvG62SyI&list=PL6uvToCKj8yeJb1BikDlXhL75O01Obb6m

Road Map
========

  1. Seperate logic dealing with hardware (ANT+) and make
Wattzap just listen for telemetry data. That way people can
add support for ble, ftms or whatever. This also
decouples the data hardware support available in Java.
  2. Update to Java 21
  3. Move data analysis to separate project 
  1. Move Graphics to JavaFX

Latest Release
==============

Version 3.0.0 October 19th 2026

3.0.0 Release Notes
-------------------

* Upgrade to Java 21
* Add Smart Trainer support via a hardware bridge
* Add virtual gearing support via a cheap Media Controller

Java will run on Windows and Linux including on Raspberry 4 hardware. It requires
Java 21 and Python 3.13 for the Bridges.

In order to fix a FIT file import bug I have had to upgrade to the latest Garmin FIT library. This is only supported on Java >= 1.8. If you are running a lower version of Java FIT file imports won't work. 

[![Watch the video](https://img.youtube.com/vi/3yX-Ww8KPdg/maxresdefault.jpg)](https://youtu.be/3yX-Ww8KPdg)

  * User Manual : https://drive.google.com/open?id=1R_3j5_DBuqHOqQYc5LY4cxTJihBQHh0E
  * Training Programmes : https://drive.google.com/open?id=1Fm1BL2769SinFiN12xRxDsO5fJB53FES
  
2.9.0 Release Notes
-------------
  * Adds Lifeline TT02 Fluid Trainer Support
  * Corrects FIT file import for latest FIT versions

Cycling Videos
==============

[Videos](videos/README.md)

Video Playback
--------------

To play videos you will need VLC player. On Unix/Linux this can be installed via the application repository. VLC and a JVM is bundled in the Windows version but you will need to install an ANT USB driver (the windows version doesn't work). See the manual for details or watch this video

Development
===========

Basic Architecture
------------------

Wattzap is a Java Swing based application. An "Enterprise Bus" pattern is used for internal communication.

Mac OSX IMPORTANT
-----------------

Due to an issue with VLC and Java the 1.6r45 JVM is used on Mac OSX. Please make sure your changes are compatible with this version
of the compiler/libraries. Note the latest FIT binary is Java 1.8 only.

In order to support OSX on newer JVMs the video display will need to move to JavaFX in a later release. Otherwise run on JVM 1.6 but with no FIT import.


# Wattzap BLE Bridge — Windows Installation Guide

The Wattzap BLE bridge supports Bluetooth Low Energy (BLE) sensors and FTMS smart trainers on Windows 10 and 11.

It can connect to heart-rate monitors, cadence sensors and smart trainers, and supports virtual gear shifting using an inexpensive Bluetooth media controller.

The bridge can run on the same computer as Wattzap or communicate with Wattzap over a local network.

## 1. Install Python

**Python 3.13 (64-bit) is recommended and has been tested with the bridge on Windows 11.**

Download Python from:

https://www.python.org/downloads/windows/

Run the installer:

- Select **Install for me only**. Administrator privileges are not required.
- Enable **Add Python to PATH**, if available.
- Make sure pip is included.

Python 3.15 caused compatibility problems with the Windows dependencies of Bleak during testing and is not recommended for this setup.

Open a new Command Prompt and check the installation:

```bat
py -3.13 --version
```

If the Python launcher is unavailable, try:

```bat
"%LOCALAPPDATA%\Programs\Python\Python313\python.exe" --version
```

The latter assumes the default per-user installation directory.

## 2. Create a Python virtual environment

Open Command Prompt in the directory containing `ble_bridge_windows.py`.

Create a virtual environment:

```bat
py -3.13 -m venv .venv
```

Activate it:

```bat
.venv\Scripts\activate
```

The command prompt should now show `(.venv)`.

Install Bleak:

```bat
python -m pip install --upgrade pip
python -m pip install --only-binary=:all: bleak==2.1.1
```

The `--only-binary=:all:` option prevents pip from attempting to compile Windows dependencies from source.

No administrator privileges are required.

## 3. Test Bluetooth

Make sure Bluetooth is enabled in Windows.

Run:

```bat
python ble_bridge_windows.py --scan
```

The bridge scans for compatible BLE devices, including heart-rate sensors and FTMS smart trainers.

For example, a Polar Verity Sense heart-rate monitor should be detected when powered on and advertising.

If scanning works, the Python and Bleak installation is functioning.

## 4. Configure the sensors and trainer

Switch on your smart trainer and any additional sensors.

Run:

```bat
python ble_bridge_windows.py --configure
```

Select the appropriate devices from the list.

The configuration is saved in:

```text
%USERPROFILE%\.wattzap-ble-bridge.json
```

You normally only need to configure the devices once.

## 5. Pair the Bluetooth media controller

Virtual gear shifting is supported using Bluetooth media controllers with volume or track-selection buttons.

**On Windows, the media controller must first be paired with Windows itself.**

1. Open **Settings → Bluetooth & devices**.
2. Select **Add device → Bluetooth**.
3. Put the media controller into pairing mode.
4. Select the controller and complete pairing.

Unlike FTMS devices, the controller is handled by Windows as an input device. The bridge reads its media-key events rather than connecting to it directly using Bleak.

Consequently, the media controller does not need to appear in the bridge's BLE device scan.

### Test virtual gearing

Run:

```bat
python ble_bridge_windows.py --test-media-keys
```

Press the controller's volume buttons.

The console should display gear changes:

```text
[GEAR] 12/24
[GEAR] 13/24
[GEAR] 14/24
[GEAR] 13/24
```

The default mappings are:

| Media-controller button | Action |
|---|---|
| Volume Up / Next Track | Gear up |
| Volume Down / Previous Track | Gear down |

The system supports 24 virtual gears.

**Note:** The Windows listener currently receives global media-key events. Volume buttons on other keyboards may therefore also trigger gear changes, and the Windows system volume may change.

## 6. Run the bridge

Start Wattzap, then run:

```bat
python ble_bridge_windows.py --virtual-gears
```

The bridge connects to the configured sensors and trainer, sends telemetry to Wattzap, and receives resistance-control commands.

For a smart trainer, Wattzap calculates virtual speed from measured power, rider characteristics and route gradient.

The bridge controls trainer resistance using the gradient and selected virtual gear.

For ERG workouts, Wattzap sends a target power instead.

Without virtual gearing, start the bridge using:

```bat
python ble_bridge_windows.py
```

This uses the trainer's normal FTMS simulation control.

## 7. Run Wattzap on another computer

Wattzap and the BLE bridge do not have to run on the same computer.

For example:

- Windows laptop: BLE bridge, smart trainer and Bluetooth media controller.
- Linux computer: Wattzap application and video playback.

The bridge communicates with Wattzap using TCP/IP on **port 28773**.

Start the bridge with the IP address of the computer running Wattzap:

```bat
python ble_bridge_windows.py --host 192.168.1.100 --port 28773 --virtual-gears
```

Replace `192.168.1.100` with the correct address.

Port 28773 is the default, so `--port` can be omitted.

**Important:** Wattzap must be configured to accept network connections rather than listening only on `127.0.0.1`.

On Linux, check using:

```bash
ss -ltnp | grep 28773
```

A listening address of `127.0.0.1:28773` means local connections only. A wildcard address such as `0.0.0.0:28773` allows connections through network interfaces, subject to firewall rules.

Only enable network access on trusted networks.

If connections fail, check the firewall on the computer running Wattzap. On Windows, you can test TCP connectivity with PowerShell:

```powershell
Test-NetConnection 192.168.1.100 -Port 28773
```

The expected result is:

```text
TcpTestSucceeded : True
```

## 8. Troubleshooting

**`python` is not recognised**

Try `py -3.13`, or use the full path to `python.exe`. Once the virtual environment is activated, `python` should work normally.

**Bleak or winrt-runtime installation fails**

Check that you're using Python 3.13 and a new virtual environment. Avoid Python 3.15 for this tested configuration. Install Bleak using binary packages as shown above.

**No Bluetooth devices found**

Check that Bluetooth is enabled, the sensor is powered on, and another application isn't already connected to the device.

**Media controller doesn't change gears**

Pair it through Windows Bluetooth settings first. Test using `--test-media-keys` before connecting the trainer.

**Bridge cannot connect to Wattzap over the network**

Check the host IP address, TCP port 28773, Wattzap's listening address and firewall settings. A successful ping alone doesn't guarantee that the TCP connection is possible.

## Tested configuration

- Windows 11
- Python 3.13, 64-bit
- Bleak 2.1.1
- Van Rysel D100 FTMS smart trainer
- Polar Verity Sense BLE heart-rate sensor (discovery tested)
- Bluetooth media controller with volume buttons
- Wattzap running on a separate Linux machine over TCP/IP

BLE scanning, media-key detection, virtual gear changes and gradient-controlled trainer resistance have all been successfully tested in this setup.

Building
--------

Install Apache Ant and use the build.xml file

$ ant -f build.xml clean fatjar
$ ./wattzap.sh

You will need a Java v21 installed on your computer.

Working on the Project
----------------------

Feel free to contribute bug fixes, improvements etc to WattzAp. You can fork the project for your own purposes if you wish.