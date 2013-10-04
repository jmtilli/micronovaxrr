% rho is in grams per cubic meter
function rho = calculate_mass_density(layers, rho_e)
  N_A = 6.0221415e23;
  rho = zeros(1,size(layers, 1));
  for k=1:size(layers, 1)
    layer = layers{k};
    num = 0;
    den = 0;
    for n=1:(size(layer,2)/2)
      formula = layer{2*n-1};
      N = layer{2*n};
      element = lookuptable3(formula);
      num = num + N*element.f1;
      den = den + N*element.M/N_A;
    end
    rho(k) = rho_e(k) / (num/(den + (den==0)) + (num==0));
  end
end
