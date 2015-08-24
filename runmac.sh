#!/bin/bash
#
# WattzAp Mac Startup File
# 6th May 2015
#
SEP=":"
LIBS=$(find lib -maxdepth 1 -name "*.jar" -print | tr '\n' ':')
LIBS="lib:properties:$LIBS"

export VLC_PLUGIN_PATH=/Applications/VLC.app/Contents/MacOS/plugins

#echo $LIBS

java -Djna.library.path=/Applications/VLC.app/Contents/MacOS/lib -cp $LIBS -Dlog4j.logger.level=INFO com.wattzap.Main
