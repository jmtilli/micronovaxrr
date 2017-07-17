@echo off
mkdir xrr-src
mkdir xrr-src\sf
mkdir xrr-src\deploy
mkdir xrr-src\deploy\examples

copy sf\* xrr-src\sf

copy deploy\examples\* xrr-src\deploy\examples

copy deploy\LICENSE.txt xrr-src\deploy
copy deploy\README* xrr-src\deploy
copy deploy\resolution.txt xrr-src\deploy

copy xchart-3.4.0.jar xrr-src
copy Jama-1.0.3.jar xrr-src
copy javafastcomplex.jar xrr-src
copy javaxmlfrag.jar xrr-src
copy atomic_masses.txt xrr-src

copy deploy\run.sh xrr-src\deploy
copy deploy\run.bat xrr-src\deploy

copy run.sh xrr-src
copy run.bat xrr-src

copy default.layers xrr-src
copy default.properties xrr-src

copy *.java xrr-src
copy *.png xrr-src
copy *.sh xrr-src
copy *.bat xrr-src
copy manifest.txt xrr-src

copy build.xml xrr-src
