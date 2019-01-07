#! /bin/bash

# MATCHING-ENGINE run server
echo " -- launching MATCHING-ENGINE"
cd /tmp/MATCHING-ENGINE/lib
runningJar=$(ls matching-engine.jar)

echo " -- launching MATCHING-ENGINE : java -jar ${runningJar}"
java -Ddocker.container.id=$HOSTNAME -Dconsul.enabled=true -Dconsul.url=consul -jar ${runningJar}