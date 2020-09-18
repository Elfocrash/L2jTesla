#!/bin/bash

# exit codes of GameServer:
#  0 normal shutdown
#  2 reboot attempt

if ! [ -d ./log/ ]; then
	mkdir log
fi

while :; do
	[ -f log/java0.log.0 ] && mv log/java0.log.0 "log/`date +%Y-%m-%d_%H-%M-%S`_java.log"
	[ -f log/stdout.log ] && mv log/stdout.log "log/`date +%Y-%m-%d_%H-%M-%S`_stdout.log"
	java -Xmx2G -cp ./*:l2jtesla.jar dev.l2j.tesla.gameserver.GameServer > log/stdout.log 2>&1
	[ $? -ne 2 ] && break
#	/etc/init.d/mysql restart
	sleep 10
done
