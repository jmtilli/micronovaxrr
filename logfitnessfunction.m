% Do the fitness function calculation for one
% measured and one simulated dataset using p-norm
% in logarithmic space. The scaling factor is
% automatically adjusted so that both datasets
% have the same arithmetic means.
function E = logfitnessfunction(dataset1,dataset2,g)
	indices = find(dataset1>0 & dataset2>0);
	dataset1 = dataset1(indices);
	dataset2 = dataset2(indices);
	a = 10*log(dataset1)/log(10);
	b = 10*log(dataset2)/log(10);

	%diff = mean(a) - mean(b);
	%a = a - diff;

	%E = sqrt(sum((a-b).^2)/length(a));
	E = norm(a-b, g.pnorm)/length(a)^(1/g.pnorm);
end
