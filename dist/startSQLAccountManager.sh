#!/bin/sh
java -Djava.util.logging.config.file=config/console.cfg -cp ./*:l2jtesla.jar:mysql-connector-java-8.0.15.jar dev.l2j.tesla.accountmanager.SQLAccountManager
