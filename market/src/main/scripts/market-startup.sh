#! /bin/bash

# MARKET run server
echo " -- launching MARKET SERVER"
cd /tmp/MARKET/lib
runningJar=$(ls market.jar)

echo " -- launching MARKET SERVER : java -jar ${runningJar}"
java -Ddocker.container.id=$HOSTNAME -Dconsul.enabled=true -Dconsul.url=consul -jar ${runningJar}