#!/bin/bash

APPHOME=/home/syscom/IMSTestMain

CLASSPATH=.
for i in $(ls $APPHOME/*.jar); do
	CLASSPATH="$CLASSPATH":"$i"
done

# complie
javac -encoding utf-8 -cp "$CLASSPATH" IMSTesterMain.java

# check
count=`ps -ef |grep java|grep IMSTesterMain|wc -l`
if [ $count != 0 ];then
   echo "IMSTesterMain is already running..."
   exit 1
fi

#execute
java -Dfile.encoding=UTF-8 -cp "$CLASSPATH" IMSTesterMain