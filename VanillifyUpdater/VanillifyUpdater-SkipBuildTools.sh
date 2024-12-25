#!/bin/bash
cp dist/VanillifyUpdater.jar ./VanillifyUpdater.jar
java -jar VanillifyUpdater.jar --skip-BuildTools
rm -r VanillifyUpdater.jar

