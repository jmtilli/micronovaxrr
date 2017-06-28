#!/bin/sh

export CLASSPATH=".:./deploy/jfreechart-1.0.1.jar:./deploy/javafastcomplex.jar:./deploy/Jama-1.0.3.jar"

rm -rf javadocs
mkdir javadocs
javadoc -quiet -d javadocs *.java
