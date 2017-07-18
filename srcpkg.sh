#!/bin/sh
mkdir xrr-src
mkdir xrr-src/sf
mkdir xrr-src/deploy
mkdir xrr-src/deploy/examples
cp sf/* xrr-src/sf
cp deploy/examples/* xrr-src/deploy/examples
cp deploy/APACHE-LICENSE-2.0.txt xrr-src/deploy
cp deploy/LICENSE.txt deploy/README* deploy/resolution.txt xrr-src/deploy
cp xchart-3.4.1-CUSTOM.jar Jama-1.0.3.jar javafastcomplex.jar javaxmlfrag.jar atomic_masses.txt xrr-src
cp deploy/run.sh deploy/run.bat xrr-src/deploy
cp run.sh run.bat xrr-src
cp default.layers xrr-src
cp default.properties xrr-src
cp *.java *.png *.sh *.bat manifest.txt xrr-src
cp build.xml xrr-src
