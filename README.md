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

Latest Release
==============

Version 2.8.0 April 10th 2020

Windows Download : https://drive.google.com/open?id=1x4wEG70LWf1sQiXIfRp9APIfxOXPvijE
Linux Download :
OSX Download :
User Manual : https://drive.google.com/open?id=1R_3j5_DBuqHOqQYc5LY4cxTJihBQHh0E
Training Programmes : https://drive.google.com/open?id=1Fm1BL2769SinFiN12xRxDsO5fJB53FES


Basic Architecture
==================

Wattzap is a Java Swing based application. An "Enterprise Bus" pattern is used for internal communication.

Mac OSX IMPORTANT
=================

Due to an issue with VLC and Java the 1.6r45 JVM is used on Mac OSX. Please make sure your changes are compatible with this version
of the compiler/libraries.

Building
========

Install Apache Ant and use the build.xml file

$ ant -f build.xml

Video Playback
==============

To play videos you will need VLC player. On Unix/Linux this can be installed via the application repository. On Windows download VLC.
Copy plugins, libvcl.dll and libvlccore.dll to the Wattzap home directory or use the installer above.

Working on the Project
======================

Feel free to contribute bug fixes, improvements etc to WattzAp. You can fork the project for your own purposes if you wish.