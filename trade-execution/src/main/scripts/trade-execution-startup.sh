#! /bin/bash

# TRADE-EXECUTION run server
echo " -- launching TRADE-EXECUTION"
cd /tmp/TRADE-EXECUTION/lib
runningJar=$(ls trade-execution.jar)

echo " -- launching TRADE-EXECUTION : java -jar ${runningJar}"
java -jar ${runningJar}