#!/bin/sh
export CLASSPATH=".:./deploy/jfreechart-1.0.1.jar:./deploy/javafastcomplex.jar:./deploy/Jama-1.0.3.jar:./deploy/javaxmlfrag.jar"
echo Running regression tests...
java -ea ChemicalFormula
java -ea Fcode
echo Regression tests completed
