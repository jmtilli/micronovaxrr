function ret = calculate_beta_coeff(layers)
  ret = zeros(1,size(layers, 1));
  for k=1:size(layers, 1)
    layer = layers{k};
    num = 0;
    den = 0;
    for n=1:(size(layer,2)/2)
      formula = layer{2*n-1};
      N = layer{2*n};
      element = lookuptable3(formula);
      den = den + N*element.f1;
      num = num + N*element.f2;
    end
    ret(k) = num/(den + (den==0));
  end
end
