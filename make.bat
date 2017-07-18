@echo off

set CLASSPATH=.;.\deploy\xchart-3.4.1-CUSTOM.jar;.\deploy\javafastcomplex.jar;.\deploy\Jama-1.0.3.jar;.\deploy\javaxmlfrag.jar

del *.class
javac -Xlint:unchecked -source 1.5 -target 1.5 *.java
jar cfm xrr.jar manifest.txt *.class *.png
copy xrr.jar deploy
copy javafastcomplex.jar deploy
copy xchart-3.4.1-CUSTOM.jar deploy
copy Jama-1.0.3.jar deploy
copy atomic_masses.txt deploy
copy default.layers deploy
copy default.properties deploy
if not exist deploy\sf md deploy\sf
copy sf\*.nff deploy\sf
