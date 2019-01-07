#! /bin/bash

# TRADE-EXECUTION run server
echo " -- launching TRADE-EXECUTION SERVER"
cd /tmp/TRADE-EXECUTION/lib
runningJar=$(ls trade-execution.jar)

echo " -- launching TRADE-EXECUTION SERVER : java -jar ${runningJar}"
java -Ddocker.container.id=$HOSTNAME -Dconsul.enabled=true -Dconsul.url=consul -jar ${runningJar}