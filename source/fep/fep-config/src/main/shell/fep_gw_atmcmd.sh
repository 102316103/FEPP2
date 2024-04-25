#!/bin/bash

optUser=syscom
optUserDir=/home/$optUser
jarFile=$optUserDir/fep-app/fep-gateway-atm/fep-gateway-atm.jar
programName=com.syscom.fep.gateway.cmd.ATMGatewayCommand

function usage() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  echo "Usage: $0 {sslswitch|sslnow|ssllist|sslchange number|sslremove number|ssladd file sscode|sslalias action (atmIp) (alias)|monitor|clientlist}"
  echo "Example: $0 sslswitch"
  echo "Example: $0 sslchange 1"
  echo "Example: $0 ssladd certificate.p12 12345678"
  echo "Example: $0 sslalias set 127.0.0.1 1.0"
  echo "Example: $0 sslalias monitor"
  echo "Example: $0 clientlist"
  exit 1
}

function sslswitch() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f sslswitch
}

function sslchange() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f sslchange -d "index=$1"
}

function sslnow() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f sslnow
}

function ssllist() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f ssllist
}

function sslremove() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  #java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f sslremove -d "index=$1"
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f sslremove
}

function ssladd() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  # java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f ssladd -d "file=$1&sscode=$2"
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f ssladd
}

function sslalias() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f sslalias -d "action=$1&atmIp=$2&alias=$3"
}

function monitor() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f monitor -d "action=get&listClient=true"
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f monitor -d "action=get&listClient=false" -i 10.3.101.3 -p 8300
}

function clientlist() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f clientlist -d "atmStatus=$1"
}

case $1 in
sslswitch)
  sslswitch
  ;;

sslnow)
  sslnow
  ;;

ssllist)
  ssllist
  ;;

sslchange)
  sslchange $2
  ;;

sslremove)
  sslremove $2
  ;;

ssladd)
  ssladd $2 $3
  ;;

sslalias)
  sslalias $2 $3 $4
  ;;

monitor)
  monitor
  ;;

clientlist)
  clientlist $2
  ;;

*)
  usage
  ;;
esac
