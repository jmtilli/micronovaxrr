@echo off
call make.bat
mkdir xrr-bin
mkdir xrr-bin\lib
mkdir xrr-bin\librarysrc
mkdir xrr-bin\sf
mkdir xrr-bin\examples
mkdir xrr-bin\nativelib
copy librarysrc\* xrr-bin\librarysrc
copy deploy\lib\* xrr-bin\lib   
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
copy deploy\JMatLink.dll xrr-bin
