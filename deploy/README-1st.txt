INSTALLATION


This programs needs Java version at least 1.6 to run. This program has been
tested on Windows and Linux, but it should work on any platform for which Java
is available. However, newest version of Java, 1.8, is recommended.



First, you need to install Java Runtime Environment from
http://www.java.com/en/download/. You can test the installation by trying to
start xrr.jar. On Windows it can be started by double-clicking it. It should
start properly. If you have Java already installed and the program doesn't
start after double-clicking xrr.jar, try reinstalling Java.




IMPORTANT NOTE:

If you need more materials than included or support for different wavelengths,
you have to download the atomic scattering factor files from

http://henke.lbl.gov/optical_constants/sf/sf.tar.gz

and extract all the .nff files to the directory "sf". Atomic masses for new
elements can be added to atomic_masses.txt.



DEFAULT LAYER MODEL:

The layer model contains all the other information of the measurement setup
except the actual measured intensities. The wavelength and instrument
convolution width (specified by FWHM) are therefore part of the layer model.
Usually these values are constant for a given diffractometer, so the same
values of wavelength and FWHM are used in most setups.

If you want to have a specific layer model loaded automatically when the
program starts, save the layer model to a file named "default.layers" or
"default.layers.gz". On Unix systems, the name is case sensitive. The file
should be saved to the directory that contains the file atomic_masses.txt. If
you use a source package, the directory is called "deploy". If you install a
binary package, the directory is the main directory which is usually called
"xrr-bin".

You might want to set the wavelength and FWHM to correct values and add a
substrate layer to the default layer model.

An example default layer model is provided with the program. Because of this,
if you modify this default layer model or install your own, you must be aware
that installing a new version of this program will overwrite your default layer
model. If you don't want your default model overwritten when updating the
program, you must keep a backup copy of your own default.layers or
default.layers.gz.
