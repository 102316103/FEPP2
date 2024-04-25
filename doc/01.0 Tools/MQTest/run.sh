#!/bin/bash

APPHOME=/home/syscom/MQTest

CLASSPATH=.
for i in $(ls $APPHOME/lib/*.jar); do
	CLASSPATH="$CLASSPATH":"$i"
done

java -Dfile.encoding=UTF-8 -cp "$CLASSPATH" com.syscom.fep.tools.mq.MQMain