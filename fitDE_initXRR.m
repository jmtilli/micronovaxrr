% initialize DE fitting with XRR layer model and fitness function.
% alpha_0 is in radians, meas is the linear measured intensity.
% beta_coeff, lambda and stddevrad have their usual meanings (SI-units).
% d, rho_e and r are in SI-units. Their minimum and maximum values are
% specified along with their expected values (which are added to the
% initialized population). Look for algoname and npop in fitDE_init.m
%
% The individuals are [d,rho_e,r]. You need to manually extract d, rho_e
% and r from the fitting results by fitDE_best and fitDE_median
function ctx = fitDE_initXRR(alpha_0, meas, beta_coeff, lambda, stddevrad, d_min, d, d_max, rho_e_min, rho_e, rho_e_max, r_min, r, r_max, prodfactor_min, prodfactor, prodfactor_max, sumterm_min, sumterm, sumterm_max, algoname, npop, func, g)
  q.alpha_0 = alpha_0;
  q.meas = meas;
  q.beta_coeff = beta_coeff;
  q.lambda = lambda;
  q.stddevrad = stddevrad;
  q.pnorm = 2;
  if exist('func','var')
    q.fitnessfunction = func;
    q.g = g;
  else
    q.fitnessfunction = @logfitnessfunction;
    q.g.pnorm = 2;
    q.g.threshold = 400;
  end

  ctx = fitDE_init(@XRRfitness, [prodfactor_min, sumterm_min, d_min, rho_e_min, r_min], [prodfactor_max, sumterm_max, d_max, rho_e_max, r_max], [prodfactor, sumterm, d, rho_e, r], q, algoname, npop);
end
