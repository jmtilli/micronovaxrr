#!/bin/sh

export CLASSPATH=".:./deploy/jfreechart-1.0.1.jar:./deploy/javafastcomplex.jar:./deploy/Jama-1.0.3.jar"

rm *.class
javac *.java
jar cfm xrr.jar manifest.txt *.class *.png
cp xrr.jar deploy
