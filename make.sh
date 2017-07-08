#!/bin/sh

export CLASSPATH=".:./deploy/jfreechart-1.0.1.jar:./deploy/javafastcomplex.jar:./deploy/Jama-1.0.3.jar:./deploy/javaxmlfrag.jar"

rm *.class
javac -Xlint:unchecked -source 1.5 -target 1.5 *.java
jar cfm xrr.jar manifest.txt *.class *.png
cp xrr.jar deploy
cp javafastcomplex.jar deploy
cp javaxmlfrag.jar deploy
cp jfreechart-1.0.1.jar deploy
cp Jama-1.0.3.jar deploy
mkdir -p deploy/lib
cp lib/jcommon-1.0.0.jar deploy/lib
cp atomic_masses.txt deploy
cp default.layers deploy
mkdir -p deploy/sf
cp sf/*.nff deploy/sf
