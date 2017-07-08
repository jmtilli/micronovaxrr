@echo off

set CLASSPATH=.;.\deploy\jfreechart-1.0.1.jar;.\deploy\javafastcomplex.jar;.\deploy\Jama-1.0.3.jar;.\deploy\javaxmlfrag.jar

del *.class
javac -Xlint:unchecked -source 1.5 -target 1.5 *.java
jar cfm xrr.jar manifest.txt *.class *.png
copy xrr.jar deploy
copy javafastcomplex.jar deploy
copy jfreechart-1.0.1.jar deploy
copy Jama-1.0.3.jar deploy
if not exist deploy\lib md deploy\lib
copy lib\jcommon-1.0.0.jar deploy\lib
copy atomic_masses.txt deploy
copy default.layers deploy
if not exist deploy\sf md deploy\sf
copy sf\*.nff deploy\sf
