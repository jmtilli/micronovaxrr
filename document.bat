@echo off

set CLASSPATH=.;.\deploy\jfreechart-1.0.1.jar;.\deploy\uncommons-maths-1.2.3.jar;.\deploy\javafastcomplex.jar

rmdir /S /Q javadocs
mkdir javadocs
javadoc -quiet -d javadocs *.java
