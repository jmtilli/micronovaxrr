@echo off
mkdir xrr-src
mkdir xrr-src\librarysrc
mkdir xrr-src\deploy
mkdir xrr-src\deploy\lib
mkdir xrr-src\deploy\sf
mkdir xrr-src\deploy\examples
copy librarysrc\* xrr-src\librarysrc
copy deploy\lib\* xrr-src\deploy\lib
copy deploy\sf\* xrr-src\deploy\sf
copy deploy\examples\* xrr-src\deploy\examples
copy deploy\README* xrr-src\deploy
copy deploy\LICENSE.txt xrr-src\deploy
copy deploy\jfreechart-1.0.1.jar xrr-src\deploy
copy deploy\uncommons-maths-1.2.3.jar xrr-src\deploy
copy deploy\javafastcomplex.jar xrr-src\deploy
copy deploy\atomic_masses.txt xrr-src\deploy
copy deploy\resolution.txt xrr-src\deploy
copy deploy\run.sh xrr-src\deploy
copy deploy\run.bat xrr-src\deploy
copy deploy\default.layers xrr-src\deploy
copy *.java xrr-src
copy *.m xrr-src
copy *.png xrr-src
copy *.sh xrr-src
copy *.bat xrr-src
copy manifest.txt xrr-src
copy UPDATING_OCTAVE_CODE.txt xrr-src
