@echo off
call make.bat
mkdir xrr-bin
mkdir xrr-bin\sf
mkdir xrr-bin\examples
copy deploy\sf\* xrr-bin\sf
copy deploy\examples\* xrr-bin\examples
copy deploy\README* xrr-bin
copy deploy\LICENSE.txt xrr-bin
copy deploy\resolution.txt xrr-bin
copy deploy\*.jar xrr-bin
copy deploy\atomic_masses.txt xrr-bin
copy deploy\run.sh xrr-bin
copy deploy\run.bat xrr-bin
copy deploy\default.layers xrr-bin
copy deploy\default.properties xrr-bin
