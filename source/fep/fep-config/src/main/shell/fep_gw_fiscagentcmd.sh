#!/bin/bash

optUser=syscom
optUserDir=/home/$optUser
jarFile=$optUserDir/fep-app/fep-gateway-fisc-agent/fep-gateway-fisc-agent.jar
programName=com.syscom.fep.gateway.cmd.FISCGatewayAgentCommand

function usage() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  echo "Usage: $0 {start|stop|check|startChannel primary/secondary/all|stopChannel primary/secondary/all}"
  echo "Example: $0 start"
  echo "Example: $0 stop"
  echo "Example: $0 check"
  echo "Example: $0 startChannel primary"
  echo "Example: $0 startChannel secondary"
  echo "Example: $0 startChannel all"
  echo "Example: $0 stopChannel primary"
  echo "Example: $0 stopChannel secondary"
  echo "Example: $0 stopChannel all"
  exit 1
}

function start() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f start
}

function stop() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f stop
}

function check() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f check -i "$1" -p "$2"
}

function channel() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f channel -d "mode=$1&action=$2"
}

case $1 in
start)
  start
  ;;

stop)
  stop
  ;;

check)
  check $2 $3
  ;;

startChannel)
  channel $2 start
  ;;

stopChannel)
  channel $2 stop
  ;;

*)
  usage
  ;;
esac
