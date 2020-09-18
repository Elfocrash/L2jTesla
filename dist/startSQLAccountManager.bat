@echo off
title L2jTesla account manager console
@java -Djava.util.logging.config.file=config/console.cfg -cp ./libs/*; dev.l2j.tesla.accountmanager.SQLAccountManager
@pause
