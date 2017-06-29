This directory contains the following true XRR measurements:

- gaaspn_xrr.x00: nominally 20 nm thick GaAsPN layer on nominally 70 nm thick
  GaP buffer layer on GaP substrate

- xrr003.x00: nominally 30 nm thick GaAsPN layer directly on top of GaP
  substrate

And the following simulated fitting target (can be loaded with "File" -> "Load
ASCII export"):

- covga.txt

Both true XRR measurements were made for the M.Sc. thesis of the software
author ("Composition determination of quaternary GaAsPN", Aalto University).
Additionally, the xrr003.x00 sample was used in the following journal article
(in this article, it was denoted with the sample number #3):

J-.M Tilli, H. Jussila, K. M. Yu, T. Huhtio, and M. Sopanen. Composition
determination of quaternary GaAsPN layers from single X-ray diffraction
measurement of quasi-forbidden (002) reflection. J. Appl. Phys., 115:203102,
2014.

What can be deduced from the true XRR measurements, then?

Both measurements needed a two-layer model for the fit. For gaaspn_xrr.x00 the
reason is that the GaP buffer layer has pinholes, and therefore, its average
density is lower than that of the GaP substrate. For xrr003.x00, the top
interface roughness is non-Gaussian probably due to pinholes.

By looking at the delta depth profile of gaaspn_xrr.x00.layers, it can be
observed that the GaP buffer layer is approximately 80 nm thick and that the
thickness that the software reports (50 nm) is incorrect. The buffer layer has
approximately linearly declining density. The reason for the incorrect
thickness reported by the software is that the linearly declining density is
achieved by a Gaussian interface between the layer in the model and the
substrate, so part of the Gaussian change needs to be included in the buffer
layer thickness, as well.

For xrr003.x00.layers, the depth profile reveals the non-Gaussianness of the
top interface. The pinholes are probably not particularly deep in this sample.
Thus, it can be concluded that 70 nm thick layer has a huge pinhole problem,
but a 30 nm thick layer has only small pinholes near the surface.

The file covga.txt contains simulated fitting target from the following paper:

J. Tiilikainen, J.–M. Tilli, V. Bosund, M. Mattila, T. Hakkarainen, V.–M.
Airaksinen and H. Lipsanen, Nonlinear fitness–space–structure adaptation and
principal component analysis in genetic algorithms: an application to x–ray
reflectivity analysis, Journal of Physics D: Applied Physics 40 (2007) 215–218

The file covga.txt has artificial photon counting noise added from Poisson
distribution with a photon level of -70 dB.

To test the fitting algorithms of the software, load covga.txt using the "Load
ASCII export" tool. Then load covga_tofit.layers. It has an incorrect initial
guess, yet the fitting ranges are sensible. Then go to automatic fit tab and
select the algorithm "JavaCovDE". Set the last angle to 3 degrees. Use 280 as
the population size and 500 iterations. You should obtain a relatively good
fit. For fun, you can also test the "JavaDE" algorithm. It does not have the
improvements suggested in the article, and therefore, fitting is much slower
and poorer if the initial guess is not correct.
