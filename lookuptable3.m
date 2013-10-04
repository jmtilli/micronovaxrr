function retstruct = lookuptable3(formula)
  if strcmp(formula, 'Air')
    ret = [0, 0, 0, 0];
  elseif strcmp(formula, 'Al')
    ret = [13, 26.981538, 13.21-13, 0.2416];
  elseif strcmp(formula, 'As')
    ret = [33, 74.92160, 32.05-33, 1.046];
  elseif strcmp(formula, 'Au')
    ret = [79, 196.96655, 73.99-79, 7.718];
  elseif strcmp(formula, 'C')
    ret = [6, 12.0107, 6.19-6, 9.5998e-3];
  elseif strcmp(formula, 'Ga')
    ret = [31, 69.723, 29.71-31, 0.8020];
  elseif strcmp(formula, 'Ge')
    ret = [32, 72.61, 30.9009-32, 0.919435];
  elseif strcmp(formula, 'H')
    ret = [1, 1.00794, 1-1, 1.1110e-6];
  elseif strcmp(formula, 'In')
    ret = [49, 114.818, 49.1374-49, 4.95604];
  elseif strcmp(formula, 'Hf')
    ret = [72, 178.49, 65.86-72, 5.278];
  elseif strcmp(formula, 'La')
    ret = [57, 138.9055, 55.68-57, 9.768];
  elseif strcmp(formula, 'N')
    ret = [7, 14.0067, 7.033-7, 1.8359e-2];
  elseif strcmp(formula, 'Nb')
    ret = [41, 92.90638, 40.9058-41, 2.50498];
  elseif strcmp(formula, 'O')
    ret = [8, 15.9994, 8.052-8, 3.3705e-2];
  elseif strcmp(formula, 'P')
    ret = [15, 30.973761, 15.30-15, 0.4363];
  elseif strcmp(formula, 'Pt')
    ret = [78, 195.078, 73.66-78, 7.237];
  elseif strcmp(formula, 'S')
    ret = [16, 32.065, 16.34-16, 0.5505];
  elseif strcmp(formula, 'Si')
    ret = [14, 28.0855, 14.26-14, 0.3249];
  elseif strcmp(formula, 'Ta')
    ret = [73, 180.9479, 67.51-73, 5.476];
  elseif strcmp(formula, 'Ti')
    ret = [22, 47.867, 22.24-22, 1.868];
  elseif strcmp(formula, 'W')
    ret = [74, 183.84, 68.98-74, 5.769];
  elseif strcmp(formula, 'Zn')
    ret = [30, 65.409, 28.44-30, 0.7035];
  end
  retstruct.Z = ret(1);
  retstruct.M = ret(2);
  retstruct.f1 = ret(1)+ret(3);
  retstruct.f2 = ret(4);
end
