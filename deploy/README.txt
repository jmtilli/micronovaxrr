USAGE


You can add, edit and remove layers on the layer editor tab. The properties
of a layer are its name, density, thickness, roughness and chemical
composition. The roughness of a layer is the roughness of its top surface.
For example, if you want to specify the roughness of the interface between
substrate and thin film the correct property is the roughness of substrate,
NOT the roughness of thin film.

Density, thickness and roughness are specified by three values, the minimum
value, the actual value and the maximum value. The minimum and maximum values
are used by the fitting algorithm, which will search for the best fit between
these values. If you do not want to fit a parameter, uncheck the "fit" check box
or set the minimum, actual and maximum values of the parameter equal.

The chemical composition is specified by two compounds the layer consists of.
You can adjust the ratio of these compounds. If the composition of the layer
is known exactly, add the chemical formula to both fields. If the exact
composition of the layer is unknown, you have to split the expected chemical
formula to two parts. For example, "AlNO" may be modeled by "AlN" and "AlO",
the ratio which can be adjusted. The ratio is never used as a fitting
parameter since XRR measures electron density, which can be determined from
chemical composition and mass density. Both chemical composition and mass
density can't be determined simultaneously by XRR.

The effects of rough interfaces are calculated with Nevot-Croce model, which
assumes that rough interfaces are normally distributed. The specified
roughness value is RMS roughness. If arbitrary depth profiles are required,
the rough region can be split to multiple equally thick layers, the density of
which can be adjusted and fitted independently. To split rough interfaces,
specify the interfacial roughness and use the split roughness tool on the
layer editor tab. The initial densities and compositions of intermediate
layers are calculated by normal distribution, but they may be adjusted and
fitted independently.

Measurements can be imported from File -> Load measurement. The number of data
points is divided by the modulo parameter. Usually 500 - 1000 is a good number
of data poins. You can also set which data points are imported by the settings
"angle min" and "angle max". Imported measurement will be normalized with the
maximum intensity between minimum and maximum normalization angles set to 1.

Only measurements exported from PANalytical's software are supported. In order
to import other file formats, you need to edit XRRImport.java, which requires
Java programming skills. Specifically, The file format is the .x00 file format.
PANalytical's software may support other file formats as well, but this
software doesn't.

The wavelength of the measurement must be entered manually on the layer editor
tab. The default wavelength is 1.54 nm (Cu K-alpha line). Scattering factors
for other wavelengths are not included in this distribution. Instructions for
installing complete scattering factor databases are in the file README-1st.txt.
The following elements are supported by this software: Al, As, Au, C, Ga, Ge,
Hf, H, In, La, Mo, Nb, N, O, P, Pt, Sb, Si, S, Ta, Ti, W, Zn. If other elements
are needed, you need to install complete scattering factor databases.


The properties of layers can be adjusted by the sliders on the manual fit tab
to match measured data and help the automatic fitting algorithm. The "fit"
check box can be unchecked to disable automatic fitting of a certain parameter.
The "<" and ">" buttons make the search space smaller by setting the minimum or
maximum value to the current value, respectively. The "2" buttons increase the
search space.

Limited resolution can be taken into account by convolving the simulated data
with a Gaussian function, the FWHM of which can be specified by a slider on
the manual fit tab. The measurement points must be uniformly spaced in order
to use convolution. This is the case when importing measurements from
PANalytical's software and most ASCII files exported by this software. If the
points are not uniformly spaced, convolution is ignored with no warning. You
can test the use of convolution by dragging the slider to the maximum value.
If the simulated curve is not affected, convolution is not used.


The simulated curve can be fitted to the measured curve automatically by a
genetic algorithm on the Automatic fit tab. The actual values are used by the
automatic fitting algorithm as one population member. If the actual values
represent a good fit but not the best possible fit, it is possible that the
algorithm misconverges into this false local optimum. There are three
algorithms: JavaDE, JavaCovDE and JavaEitherOrDE. JavaDE may be the most
optimal algorithm for simple fitting problems as it does not need to calculate
the covariance matrix. However, for hard multilayer fitting problems, JavaCovDE
is recommended. JavaEitherOrDE is better than JavaDE but worse than JavaCovDE
for hard multilayer fitting problems. There is usually no good reason to use
JavaEitherOrDE other than scientific research related to fitting algorithms.


Before fitting you have to import layer model by pressing the button "Import".
The range of angles used for fitting can be adjusted by the first angle and
last angle settings. First angle should be set to a value where the beam
doesn't go directly to the detector, usually 0.07 degrees is a good value. Good
values for last angle are limited by the amount of noise especially if the
fitting error function p-norm in logarithmic space is used. However, the
default mixed relative/chi-squared function can reduce the harmful effects of
noise, so the last angle with this fitting error function can be set to the
maximum angle of the measurement. Iterations and population size are options of
the genetic algorithm. Higher values are slower but may help find better fits.
It is usually recommended that population size is about ten times the number of
fitting parameters. The default 60 is good for 6 fitting parameters. Iteration
count should be preferably higher rather than lower, as the fit can be
interrupted early but the fit cannot be continued once the iteration count is
reached. For this reason, the default iteration count is as high as 500. 

The parameters p-norm and threshold rel.f (dB) are parameters of the fitting
error functions. p-norm is used by p-norm in logarithmic space and p-norm in
sqrt space. Threshold rel.f (dB) on the other hand is used by mixed relative /
chi-squared and tells where the regime of relative fitting error function
begins and the regime of chi-squared fitting error function ends.

There are several adjustable fitting parameters that can be changed by the Opts
button:
- Mutation strength (k_m): 0.7
- Recombination probability to take from second gene (c_r): 0.5
- Mutation individual parameter lambda: 1.0
  - 1.0 means use best individual (DE/best/1/bin)
  - 0.5 means move random individual halfway to best individual
  - 0.0 means use random individual (DE/rand/1/bin)
- JavaEitherOrDE mutation probability (p_m): 0.5
  - not used by JavaDE or JavaCovDE
- JavaEitherOrDE recombination strength (k_r): 0.5*(k_m+1)
  - not used by JavaDE or JavaCovDE

Probably the only parameter worth adjusting is mutation strength. Lower
mutation strength means convergence is fast, but there is a risk of
misconvergence. Higher mutation strength slows down convergence but reduces the
risk of misconvergence. Recommended values are k_m = 0.5 .. 0.75. The default
is 0.7, but if you want faster convergence, try 0.6.


The fit is started by pressing the Start fit button. After the fitting is ready
or interrupted by the Stop fit button, the fitted layer model can be exported
to Layer editor and Manual fit tabs with the Export button. The fitted model
replaces the old model, so if the original model is better than the fit, do not
export the fit. 

You can load and save layer models with File -> Load layers and File -> Save
layers. The layer model on Automatic fit tab is never used by these operations,
so you need to export the model by the "Export" button before saving it.
Otherwise you will end up saving old unfitted data.  Measured data isn't saved
with the layer model so you can save a model which can be used as a basis for a
model for new measurements. This means that you shouldn't delete the measured
data because you will need to import it again.

The program doesn't add an extension to the file name automatically so you
need to enter the complete name with the extension. You should choose an
extension like ".layers" which isn't used by other programs. For example,
enter "06050304measure2.layers" instead of "06050304measure2".



Measured and simulated data can also be saved to a text file by Data -> Linear
plot -> File -> Export. The file can be imported to other applications with
the instructions in README-export.txt. The program doesn't add an extension
automatically, so you should use "06050304measure2.txt" instead of
"06050304measure2". ASCII exports contain only measured and simulated data, so
if you need to be able to load the layer model again, use the Save layers
command.

Measured or simulated data from ASCII exports can be imported later from File
-> Load ASCII export. The data to import must be in linear format. The
imported data is used as measurement data even if you import simulated data.
The option to import simulated data as measurement data can be used to test
the fitting algorithm. If the simulation you want to use as measurement is the
active layer model, you can use the simulated data directly as measurement
data by choosing File -> Use simulation as measurement.


The data menu contains various plotting tools to investigate measurement,
simulation and layer model. The layer model these tools operate on is always
the model on the manual fit tab. The plots can also be saved to text files.

The roughness-splitting plots split the layer model to multiple equally thick
layers, the density of which is calculated from the depth profile. These tools
calculate the effects of rough interfaces more accurately than the Nevot-Croce
model at the cost of slower calculations. The number of data points should be
in the range 1000 ..  10 000. Calculation with fewer data points is not
accurate enough and too many data points result in an extremely slow
calculation.

The Fourier transform tool calculates the discrete Fourier transform of
Reflectivity * alpha^4. It can be used to quickly estimate layer thicknesses.
The peaks of the Fourier transform correspond to various layer thicknesses and
different combinations of them. For example, peaks at 20 nm, 30 nm and 50 nm
indicate that the sample consists of two layers, 20 nm and 30 nm, and the 50
nm peak is the sum of their thicknesses.

The resolution of the Fourier transform tool depends on the interval the
Fourier transform is evaluated in. Angle min should be at least three times
the largest critical angle in the layer model. Angle max should be as large as
possible, but noise can limit the effective resolution at large angles.

The various depth profile plots will plot the selected variable as a function
of depth with and without interfacial roughnesses. It is helpful to identify
very thin rough layers which have minimal effect on the resulting depth
profile.
