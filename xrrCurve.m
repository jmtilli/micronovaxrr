% alpha_0, rho_e and beta_coeff must be row vectors
% r is the bottom interface roughness
function R = xrrCurve(alpha_0, d, rho_e, beta_coeff, r, lambda, stddevrad)
  r_e = 2.817940325e-15; % Classical electron radius
  delta = lambda^2*r_e*rho_e/(2*pi);
  beta = beta_coeff .* delta;
  kz = 2*pi/lambda * sqrt(ones(length(d),1)*alpha_0.^2 - (2*delta + 2*i*beta).' * ones(1,length(alpha_0)));

  R_Fresnel = zeros(length(delta)-1, length(alpha_0));
  R_total = zeros(1, length(alpha_0));

  % Calculate Fresnel reflectivity coefficients
  for k=1:(length(delta)-1)
    denom = kz(k,:) + kz(k+1,:);
    R_Fresnel(k,:) = (kz(k,:) - kz(k+1,:)) ./ (denom + (denom==0));
    R_Fresnel(k,:) = R_Fresnel(k,:).*exp(-2*kz(k,:).*kz(k+1,:)*r(k)^2);
  end

  % Calculate total reflectivity
  for k=(length(delta)-1):(-1):1
    expfactor = exp(-2*i*kz(k+1,:)*d(k+1));
    R_total = (R_Fresnel(k,:) + R_total.*expfactor)./(1+R_total.*R_Fresnel(k,:).*expfactor);
  end
  R = abs(R_total).^2;

  % Instrument convolution
  stddevs = 4;
  dalpha0rad = (alpha_0(end)-alpha_0(1))/(length(alpha_0)-1);
  gfilt = gaussian_filter(dalpha0rad, stddevrad, stddevs);
  if(length(gfilt) > 1)
  %    if(!all(abs(alpha_0-linspace(alpha_0(1),alpha_0(end),length(alpha_0))) <   linthreshold*dalpha0rad))
  %        error("alpha_0 not uniformly spaced");
  %    end
      R = apply_odd_filter(gfilt, R);
  end
end
