This directory contains the following XRR measurements:

- gaaspn_xrr.x00: nominally 20 nm thick GaAsPN layer on nominally 70 nm thick
  GaP buffer layer on GaP substrate

- xrr003.x00: nominally 30 nm thick GaAsPN layer directly on top of GaP
  substrate

Both were made for the M.Sc. thesis of the software author ("Composition
determination of quaternary GaAsPN", Aalto University). Additionally, the
xrr003.x00 sample was used in the following journal article (in this article,
it was denoted with the sample number #3):

J-.M Tilli, H. Jussila, K. M. Yu, T. Huhtio, and M. Sopanen. Composition
determination of quaternary GaAsPN layers from single X-ray diffraction
measurement of quasi-forbidden (002) reflection. J. Appl. Phys., 115:203102,
2014.

What can be deduced from the measurements, then?

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
