Wattzap Home Trainer Virtual Reality Software
=============================================

WattzAp enables home trainer users (turbo trainers, rollers) to record details of their workout, follow training programmes all within a
virtual reality environment.

WattzAp can:

  * display heart rate, cadence, rider speed and power both graphically and as real time data
  * follow workouts that target power, cadence and heart rate
  * synchronize the playback of compatible videos (Tacx RLV, Tacx TTS, Tacx Ergo, GPX, PWR) with your speed
  * share your results on sites such as Strava, SelfLoops etc
  
WattzAp works with standard turbo trainers and rollers. You don't need a complicated VR turbo trainer.
WattzAp is an open platform, you can create your own videos and workouts and share these with other users.

This is the community edition of the Wattzap Turbo Trainer Software. It is provided "as is" and without support.

Some videos demoing Wattzap. https://www.youtube.com/watch?v=P5DpvG62SyI&list=PL6uvToCKj8yeJb1BikDlXhL75O01Obb6m

Latest Release
==============

Version 2.9.0 September 24th 2020

In order to fix the FIT file import bug I have had to upgrade to the latest Garmin FIT library. This is only supported on Java >= 1.8. If you are running a lower version of Java FIT file imports won't work. I've not found a Windows 32bit JVM (the one Oracle supply is compiled 64 bit !!!). However the 64 bit version is supplied here.

  * Windows 64 bit download: https://drive.google.com/file/d/1Dgg5WYAcJojyOxtSgnurchD8yU-z_hBB/view?usp=sharing  - How to install video: https://youtu.be/62beIV3c604
  * Linux Download : https://drive.google.com/file/d/1pi_coa0G8Ew34fCLBjmoEP-gVK03yZ0L/view?usp=sharing
  * Raspbien Buster (32bit) : Wattzap will run on an Raspberry Pi 4 (lower models don't have a powerful enough graphics chip). You will need to install [jna.jar](https://github.com/java-native-access/jna/blob/4.5.X/dist/jna.jar), [jna-platform.jar](https://github.com/java-native-access/jna/blob/4.5.X/dist/jna-platform.jar) and [android-aarch64.jar](https://github.com/java-native-access/jna/blob/4.5.X/dist/android-aarch64.jar) from the JNA website into the wattzap/lib directory. You will also need a JRE v8 on your machine
  * User Manual : https://drive.google.com/open?id=1R_3j5_DBuqHOqQYc5LY4cxTJihBQHh0E
  * Training Programmes : https://drive.google.com/open?id=1Fm1BL2769SinFiN12xRxDsO5fJB53FES
  
Release Notes
-------------
  * Adds Lifeline TT02 Fluid Trainer Support
  * Corrects FIT file import for latest FIT versions

Cycling Videos
==============

[Videos](videos/README.md)

Video Playback
--------------

To play videos you will need VLC player. On Unix/Linux this can be installed via the application repository. VLC and a JVM is bundled in the Windows version but you will need to install an ANT USB driver (the windows version doesn't work). See the manual for details or watch this video.


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

Building
--------

Install Apache Ant and use the build.xml file

$ ant -f build.xml

Working on the Project
----------------------

Feel free to contribute bug fixes, improvements etc to WattzAp. You can fork the project for your own purposes if you wish.
