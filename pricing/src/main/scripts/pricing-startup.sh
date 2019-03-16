#! /bin/bash

# PRICING run server
echo " -- launching PRICING SERVER"
cd /tmp/PRICING/lib
runningJar=$(ls pricing.jar)

echo " -- launching PRICING SERVER : java -jar ${runningJar}"
java -jar ${runningJar}