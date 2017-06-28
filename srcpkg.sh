#!/bin/sh
mkdir xrr-src
mkdir xrr-src/deploy
mkdir xrr-src/deploy/lib
mkdir xrr-src/deploy/sf
mkdir xrr-src/deploy/examples
mkdir xrr-src/librarysrc
cp deploy/lib/* xrr-src/deploy/lib
cp deploy/sf/* xrr-src/deploy/sf
cp deploy/examples/* xrr-src/deploy/examples
cp deploy/LICENSE.txt deploy/README* deploy/resolution.txt deploy/jfreechart-1.0.1.jar deploy/Jama-1.0.3.jar deploy/javafastcomplex.jar deploy/atomic_masses.txt xrr-src/deploy
cp deploy/run.sh deploy/run.bat xrr-src/deploy
cp deploy/default.layers xrr-src/deploy
cp *.java *.png *.sh *.bat manifest.txt xrr-src
cp librarysrc/* xrr-src/librarysrc
