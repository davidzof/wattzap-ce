Wattzap Community Edition
=========================

This is the community edition of the Wattzap Turbo Trainer Software. It is provided "as is" and without support. It is primarily
intended for developers who want to add new features to Wattzap.

see: http://www.wattzap.com/ for more information


Basic Architecture
==================

Wattzap is a Java Swing based application. An "Enterprise Bus" pattern is used for
internal communication.

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
Copy plugins, libvcl.dll and libvlccore.dll to the Wattzap home directory.

Working on the Project
======================

Feel free to contribute bug fixes, improvements etc to WattzAp. You can fork the project for your own purposes if you wish.