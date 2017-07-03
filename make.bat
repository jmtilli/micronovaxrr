@echo off

set CLASSPATH=.;.\deploy\jfreechart-1.0.1.jar;.\deploy\javafastcomplex.jar;.\deploy\Jama-1.0.3.jar

del *.class
javac -Xlint:unchecked -source 1.5 -target 1.5 *.java
jar cfm xrr.jar manifest.txt *.class *.png
copy xrr.jar deploy
