@echo off
title L2jTesla geodata converter

java -Xmx512m -cp ./libs/*; dev.l2j.tesla.geodataconverter.GeoDataConverter

pause
