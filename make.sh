#!/bin/sh

export CLASSPATH=".:./deploy/jfreechart-1.0.1.jar:./deploy/uncommons-maths-1.2.3.jar:./deploy/javafastcomplex.jar"

rm *.class
rm deploy/*.m
javac *.java
jar cfm xrr.jar manifest.txt *.class *.png
cp xrr.jar deploy
cp *.m deploy
