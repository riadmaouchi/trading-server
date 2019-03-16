#! /bin/bash

# MARKET run server
echo " -- launching MARKET SERVER"
cd /tmp/MARKET/lib
runningJar=$(ls market.jar)

echo " -- launching MARKET SERVER : java -jar ${runningJar}"
java -jar ${runningJar}