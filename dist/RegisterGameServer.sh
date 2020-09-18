#!/bin/sh
java -Djava.util.logging.config.file=config/console.cfg -cp ./*:l2jtesla.jar dev.l2j.tesla.gsregistering.GameServerRegister
