@echo off
title L2jTesla gameserver registration console
@java -Djava.util.logging.config.file=config/console.cfg -cp ./libs/*; dev.l2j.tesla.gsregistering.GameServerRegister
@pause