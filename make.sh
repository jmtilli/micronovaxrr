#!/bin/sh

export CLASSPATH=".:./deploy/xchart-3.4.1-CUSTOM.jar:./deploy/javafastcomplex.jar:./deploy/Jama-1.0.3.jar:./deploy/javaxmlfrag.jar"

rm *.class
javac -Xlint:unchecked -source 1.5 -target 1.5 *.java
jar cfm xrr.jar manifest.txt *.class *.png
cp xrr.jar deploy
cp javafastcomplex.jar deploy
cp javaxmlfrag.jar deploy
cp xchart-3.4.1-CUSTOM.jar deploy
cp Jama-1.0.3.jar deploy
cp atomic_masses.txt deploy
cp default.layers deploy
cp default.properties deploy
mkdir -p deploy/sf
cp sf/*.nff deploy/sf
