#!/bin/bash

function find_pid() {
  ps -ef  | grep $1 | grep java | grep -v grep | cut -c 1-100 | awk '{printf $2}'
}

if [ -z "$JAVA_HOME" ]; then
  echo JAVA_HOME is not defined
  exit 125
fi

CURDIR=`pwd`
cd "$JAVA_HOME"
JAVA_HOME=`pwd`
cd "$CURDIR"

param1=$1
MODE=${param1:=none}

MAIN_JAR_NAME=@build.finalName@
MAIN_CLASS_NAME=com.github.upperbound.secret_santa.SecretSantaApplication
WAIT_TIME_SECONDS=30
JAVA_OPTS="-Dfile.encoding=UTF-8"
if [ -z "$HEAP_INIT" ]; then
  JAVA_OPTS="$JAVA_OPTS -Xms1G"
else
  JAVA_OPTS="$JAVA_OPTS -Xms$HEAP_INIT"
fi
if [ -z "$HEAP_MAX" ]; then
  JAVA_OPTS="$JAVA_OPTS -Xms1G"
else
  JAVA_OPTS="$JAVA_OPTS -Xms$HEAP_MAX"
fi
if [ -z "$JMX_REMOTE_PORT" ]; then
  JAVA_OPTS=$JAVA_OPTS
else
  JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote=true"
  JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.port=$JMX_REMOTE_PORT"
  JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.authenticate=false"
  JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.ssl=false"
fi

if [ "$MODE" = "example" ]; then
  JAVA_OPTS="$JAVA_OPTS -Dspring.sql.init.data-locations=classpath:data-example.sql"
  JAVA_OPTS="$JAVA_OPTS -Dspring.datasource.db-name=example"
  JAVA_OPTS="$JAVA_OPTS -Dspring.datasource.username=example"
  JAVA_OPTS="$JAVA_OPTS -Dspring.datasource.password=example"
  JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=ignore-notifications"
fi

FULL_CLASSPATH="config/:$MAIN_JAR_NAME.jar"

while read -r line;
do
  if [ -f "$line" ]; then
    FULL_CLASSPATH="$FULL_CLASSPATH:$line"
  fi
done < <(find ./dependencies -name "*")

export CLASSPATH="$FULL_CLASSPATH"
nohup $JAVA_HOME/bin/java $JAVA_OPTS $MAIN_CLASS_NAME &

echo waiting for $MAIN_JAR_NAME being started...
pid=0;
sleep_sec=0;
while [ "$pid" != "" ]; do
	if [[ $sleep_sec -ge $WAIT_TIME_SECONDS ]]; then
		break;
	fi
	sleep_sec=$(($sleep_sec+5));
	sleep 5;
	pid=$(find_pid $MAIN_CLASS_NAME);
done

if [ "$pid" = "" ]; then
	echo $MAIN_JAR_NAME was terminated, see log for details
	exit 125;
fi

echo $MAIN_JAR_NAME started
