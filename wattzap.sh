#
#!/bin/bash
#
# WattzAp Linux Startup File
# 6th May 2015
#
SEP=":"
LIBS=$(find lib -maxdepth 1 -name "*.jar" -print | tr '\n' ':')
LIBS="lib:properties:$LIBS"


echo $LIBS

java -Djna.library.path=/Applications/VLC.app/Contents/MacOS/lib -cp $LIBS -Dlog4j.logger.level=INFO com.wattzap.Main
