#!/bin/sh
sh make.sh
mkdir xrr-bin
mkdir xrr-bin/sf
mkdir xrr-bin/examples
cp deploy/sf/* xrr-bin/sf
cp deploy/examples/* xrr-bin/examples
cp deploy/LICENSE.txt deploy/README* deploy/resolution.txt deploy/*.jar deploy/atomic_masses.txt xrr-bin
cp deploy/run.sh deploy/run.bat xrr-bin
cp deploy/default.layers xrr-bin
cp deploy/default.properties xrr-bin
