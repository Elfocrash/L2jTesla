#! /bin/sh

if ! [ -d ./log/ ]; then
	mkdir log
fi

java -Xmx512m -cp ./libs/*; dev.l2j.tesla.geodataconverter.GeoDataConverter > log/stdout.log 2>&1