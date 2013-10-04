@echo off

set CLASSPATH=.;.\deploy\jfreechart-1.0.1.jar;.\deploy\uncommons-maths-1.2.3.jar;.\deploy\javafastcomplex.jar

del *.class
del deploy\*.m
javac *.java
jar cfm xrr.jar manifest.txt *.class *.png
copy xrr.jar deploy
copy *.m deploy
