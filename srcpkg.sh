#!/bin/sh
mkdir xrr-src
mkdir xrr-src/lib
mkdir xrr-src/sf
mkdir xrr-src/deploy
mkdir xrr-src/deploy/examples
mkdir xrr-src/librarysrc
cp lib/* xrr-src/lib
cp sf/* xrr-src/sf
cp deploy/examples/* xrr-src/deploy/examples
cp deploy/LICENSE.txt deploy/README* deploy/resolution.txt xrr-src/deploy
cp jfreechart-1.0.1.jar Jama-1.0.3.jar javafastcomplex.jar atomic_masses.txt xrr-src
cp deploy/run.sh deploy/run.bat xrr-src/deploy
cp run.sh run.bat xrr-src
cp default.layers xrr-src
cp *.java *.png *.sh *.bat manifest.txt xrr-src
cp librarysrc/* xrr-src/librarysrc
cp build.xml xrr-src
