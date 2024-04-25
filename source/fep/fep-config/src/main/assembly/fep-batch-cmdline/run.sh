#!/bin/bash

optUser=syscom
optUserDir=/home/$optUser
jarFile=$optUserDir/fep-app/${project.artifactId}${assembly-func}/${project.artifactId}${assembly-func}.jar

if [ "$1" = "" ] \
		|| [ "$1" = "h" ] \
		|| [ "$1" = "help" ] \
		|| [ "$1" = "?" ]; then
		java -jar -Dfile.encoding=UTF-8 $jarFile -h
else
		java -jar -Dfile.encoding=UTF-8 $jarFile -p $1 -a "$2"
fi