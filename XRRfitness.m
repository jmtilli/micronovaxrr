% fitness function for x-ray reflectivity curves

% q is a struct:
% q.alpha_0 is an 1xk vector of angle points in radians
% q.meas is a list of linear intensities for each angle in q.alpha_0
% q.beta_coeff is an 1xn vector of the coefficients beta/delta for each n layers
% q.lambda is the wavelength in meters
% q.stddevrad is the standard deviation of the convolution gaussian in radians
% q.pnorm is the p-norm to use in fitness calculation. Usually this is 1 or 2.

function E = XRRfitness(pop,q)
  npop = size(pop,1);
  nparam = size(pop,2);

  % both are in decibels to make the fitting more efficient
  prodfactor = 10 .^ (pop(:,1) / 10);
  sumterm = 10 .^ (pop(:,2) / 10);

  pop_drhor = pop(:,3:end);

  d = pop_drhor(:,1:end/3);
  rho_e = pop_drhor(:,end/3+1:2*end/3);
  r = pop_drhor(:,2*end/3+1:end);
  E = zeros(npop,1);

  for k = 1:npop
	  R = xrrCurve(q.alpha_0, d(k,:), rho_e(k,:), q.beta_coeff, r(k,:), q.lambda, q.stddevrad);
    R = R*prodfactor(k) + sumterm(k);
	  E(k) = feval(q.fitnessfunction,R,q.meas,q.g);
  end
  %[minval, minind] = min(E);
  %R = xrrCurve(q.alpha_0, d(minind,:), rho_e(minind,:), q.beta_coeff, r(minind,:), q.lambda, q.stddevrad);
  %plot(q.alpha_0, log(R), q.alpha_0, log(q.meas));
end
