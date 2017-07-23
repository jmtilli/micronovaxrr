import java.util.*;
import java.io.*;
import fi.iki.jmtilli.javafastcomplex.*;

/** XRR simulation utility class.
 *
 * <p>
 *
 * This class contains the XRR simulation code implementation in Java.
 *
 */

public class XRRSimul {

    private XRRSimul() {}

    /*
     * Calculates output of a fir filter during the time interval [low,high[.
     * Default of Octave's filter/fftfilt: low=0, high=s.length
     * Default of Octave's conv: low=0, high=s.length+b.length-1
     * Symmetric convolution (b.length odd): low=(b.length-1)/2, high = s.length + (b.length-1)/2
     *
     * In Octave code:
     *   filter(b,1,[s,zeros(1,high-low-length(s))])(low:end)
     * where high and low use 1-based indexing instead of 0-based as in Java
     */
    private static double[] fir(double[] b, double[] s, int low, int high) {
        double[] result = new double[high-low];
        for(int i=low; i<high; i++) {
            for(int j=Math.max(0,i-(s.length-1)); j<Math.min(b.length,i+1); j++) {
                result[i-low] += s[i-j]*b[j];
            }
        }
        return result;
    }

    /* Apply an odd filter. */
    /* In Octave:
     *   fir(filter, data, filterside, length(data)+filterside)
     *   where filterside = (filter.length+1)/2 (note the difference!)
     */
    private static double[] applyOddFilter(double[] filter, double[] data) {
        int filterside = (filter.length-1)/2;
        assert((filter.length-1)%2 == 0);
        return fir(filter, data, filterside, data.length+filterside);
    }

    /*
     * Create an odd gaussian filter
     *
     * in Octave:
     *
       function filter = gaussian_filter(dalpha0rad, stddevrad, stddevs)
         filterside = round(stddevs*stddevrad/dalpha0rad);
         filter = exp(-((dalpha0rad*((-filterside):filterside)/stddevrad).^2)/2);
         filter /= sum(filter);
       end
     */
    private static double[] gaussianFilter(double dalpha0rad, double stddevrad, double stddevs) {
        int filterside;
        double[] filter = null;

        filterside = (int)(stddevs*stddevrad/dalpha0rad + 0.5);
        if(filterside <= 0)
            return null;

        filter = new double[2*filterside+1];

        double sum = 0;
        for(int i=0; i<filter.length; i++) {
            double x = dalpha0rad*(i-filterside)/stddevrad;
            filter[i] = Math.exp(-x*x/2);
            sum += filter[i];
        }
        for(int i=0; i<filter.length; i++) {
            filter[i] /= sum;
        }

        return filter;
    }



    /** Tests whether the values in x are uniformly spaced.
     * 
     * Uniform spacing is necessary in DFT and convolution.
     *
     * @param x the array to test
     * @return true if x is uniformly spaced
     *
     */
    public static boolean isUniformlySpaced(double[] x) {
        final double dx_accuracy = 1e-4;
        double dx = (x[x.length-1] - x[0])/(x.length-1);
        for(int i=1; i<x.length; i++) {
            double diff = x[i] - x[i-1];
            if(diff > (1+dx_accuracy)*dx ||
               diff < (1-dx_accuracy)*dx) {
                return false;
            }
        }
        return true;
    }

    /**
     * Generate an array of n depths to simulate the depth profile of a LayerStack
     *
     *
     * @param n the number of depth points
     * @param layers the layer stack to generate the depths for
     *
     * @return an array containing uniformly spaced depth values
     */
    public static double[] depths(int n, LayerStack layers) {
        final double stddevs = 4;
        double min = 0;
        double max = 0;
        double d = 0;
        double[] ds;

        for(int i=0; i<layers.getSize(); i++) {
            Layer l = layers.getElementAt(i);
            min = Math.min(min, d - stddevs*l.getRoughness().getExpected());
            max = Math.max(max, d + stddevs*l.getRoughness().getExpected());
            double diff = max-min;
            min -= 0.05*diff;
            max += 0.05*diff;
            d += l.getThickness().getExpected();
        }
        return depths2(n, min, max);
    }
    /**
     * Generate a uniform distribution of depths between the given values
     *
     * @param n the number of depth points
     * @param min the first depth
     * @param max the last depth
     *
     * @return a uniformly spaced array length n with values between min and max
     */
    public static double[] depths2(int n, double min, double max) {
        double[] ds = new double[n];

        for(int i=0; i<n; i++) {
            ds[i] = min + i*(max-min)/(n-1);
        }
        return ds;
    }

    /** Plottable layer properties.
     *
     * <p>
     *
     * This enum contains information of all the properties that can be plotted
     * in depth profile plot. Code to get these properties from individual
     * layers is also implemented.
     */
    public enum XRRProperty {
        DENSITY("density") {
            public double get(Layer l) {
                return l.getDensity().getExpected();
            }
        },
        DELTA("delta") {
            public double get(Layer l) {
                return l.getDensity().getExpected() * l.getXRRCompound().getDeltaPerRho();
            }
        },
        BETA("beta") {
            public double get(Layer l) {
                return l.getDensity().getExpected() * l.getXRRCompound().getBetaPerRho();
            }
        };
        public final String title;
        /** Get the property from a layer */
        public abstract double get(Layer l);
        XRRProperty(String title) {
            this.title = title;
        }
        public String toString() {
            return title;
        }
    };

    /** Calculate a layer property at arbitrary points of depth profile.
     *
     * @param ds an array of depths
     * @param layers the layer stack
     * @param stair if set to true, interfacial roughness is ignored in the calculation
     * @param prop the property to calculate the depth profile of
     *
     * @return an array containing the specified property at the specified depths
     *
     */
    public static double[] depthProfile(double[] ds, LayerStack layers, boolean stair, XRRProperty prop) {
        int factor = stair?0:1;
        double[] mu;
        double[] stddev;
        double[] betaF;
        double[] x;
        double[] result;
        int n;
        double d = 0;

        n = layers.getSize();
        mu = new double[n];
        stddev = new double[n];
        betaF = new double[n];
        x = new double[n+1];
        x[0] = 0;
        result = new double[ds.length];

        for(int i=0; i<n; i++) {
            Layer l = layers.getElementAt(i);
            mu[i] = d;
            stddev[i] = factor*l.getRoughness().getExpected();
            betaF[i] = stair?10:l.getBetaF().getExpected();
            x[i+1] = prop.get(l);
            d += l.getThickness().getExpected();
        }

        for(int i=0; i<ds.length; i++) {
            result[i] = 0;
            for(int j=0; j<n; j++)
            {
                double B = stddev[j] * betaF[j];
                if (ds[i] <= mu[j] - B)
                {
                  result[i] += 0;
                }
                else if (ds[i] >= mu[j] + B)
                {
                  result[i] += x[j+1] - x[j];
                }
                else
                {
                  result[i] += (x[j+1]-x[j]) * (SMath.normCdf(ds[i], mu[j], stddev[j]) - SMath.normCdf(mu[j] - B, mu[j], stddev[j])) / (SMath.normCdf(mu[j] + B, mu[j], stddev[j]) - SMath.normCdf(mu[j] - B, mu[j], stddev[j]));
                }
                //result[i] += (x[j+1]-x[j])*SMath.normCdf(ds[i], mu[j], stddev[j]);
            }
        }

        return result;
    }
    private static final Complex MINUS_TWO_I = new Complex(0, -2);
    /** The real simulation code that uses real complex numbers.
     *
     * <p>
     *
     * This function implements the real simulation.  Layers are passed as four
     * arrays of doubles that contain the numerical properties of the layers.
     * The lengths of all these arrays must equal the number of layers.
     *
     * <p>
     *
     * Convolution is silently ignored if alpha0rad is not uniformly spaced. If
     * it's necessary to know whether convolution is used, the caller must
     * check whether alpha0rad is uniformly spaced by the function
     * isUniformlySpaced
     *
     * <p>
     *
     * Unlike in simulate, the uppermost layer must be the ambient layer (air),
     * that is, delta[0] = beta[0] = 0. d[0] and r[0] have no meaning and are
     * therefore ignored. The thickness of the substrate, d[n-1], where
     * n is the number of layers, must be finite and not NaN but is
     * otherwise ignored.
     * 
     * @param alpha0rad angles of incidence in radians
     * @param delta an array containing delta for all the layers
     * @param beta an array containing beta for all the layers
     * @param d an array containing the thicknesses of all the layers
     * @param r an array containing the roughnesses of the upper interfaces of all the layers
     * @param lambda wavelength in meters
     * @param stddevrad standard deviation of angle (instrument resolution) in
     * radians.
     *
     * @return an array containing the absolute values of reflectivity for intensity
     *
     */

    public static double[] rawSimulateComplex(double[] alpha0rad, double[] delta, double[] beta, double[] betaFeranchuk, double[] d, double[] r, double lambda, double stddevrad, double beam, double offset) {
        Complex[] R;
        double[] R2;
        Complex[][] kz;
        double k0 = 2*Math.PI/lambda;
        final double stddevs = 4;
        double[] filter = null;
        double dalpha0rad = alpha0rad.length > 1 ? (alpha0rad[alpha0rad.length-1] - alpha0rad[0])/(alpha0rad.length-1) : 1;


        filter = gaussianFilter(dalpha0rad, stddevrad, stddevs);

        if(filter != null) {
            if(!isUniformlySpaced(alpha0rad)) {
                //throw new RuntimeException("Measurement points not uniformly spaced");
                filter = null;
            }
        }

        R = new Complex[alpha0rad.length];
        for (int i = 0; i < R.length; i++)
        {
          R[i] = new Complex(0);
        }
        R2 = new double[alpha0rad.length];
        kz = new Complex[2][];
        for (int i = 0; i<kz.length; i++)
        {
            kz[i] = new Complex[alpha0rad.length];
        }

        Complex kz0, kz1;
        Complex num, den;
        Complex ri;
        Complex roughri;
        Complex b;
        Complex d_times_minus_two_i;

        /* we only calculate wavevector for i==d.length,
         * other calculations are done starting from i==d.length-1 */
        for(int i=d.length; i>=1; i--) {

            /* a bit tricky optimization */
            for(int j=0; j<alpha0rad.length; j++) {
                double alpha0 = alpha0rad[j]-offset;

                // Calculate z component of wavevector
                kz[(i-1)%2][j] = new Complex(alpha0*alpha0-2*delta[i-1], -2*beta[i-1])
                                    .sqrt().multiply(k0);
            }
            if(i == d.length)
                continue;

            double roughness = r[i];
            double beta_feranchuk = betaFeranchuk[i];
            double beta_feranchuk_squared = beta_feranchuk * beta_feranchuk;
            double roughness_squared = roughness*roughness;
            double roughness_factor = -2*roughness_squared;
            d_times_minus_two_i = MINUS_TWO_I.multiply(d[i]);

            for(int j=0; j<alpha0rad.length; j++) {
                double q = 2*k0*(alpha0rad[j]-offset);
                double q_squared = q*q;
                kz0 = kz[(i-1)%2][j];
                kz1 = kz[i%2][j];

                // Fresnel reflection coefficient
                num = kz0.subtract(kz1);
                den = kz0.add(kz1);
                ri = num.divide(den);
                // this can actually occur at small angles when there's no reflection.
                if (ri.isNaN())
                {
                  ri = Complex.ZERO;
                }
                double secondTermDivisor = q_squared*roughness_squared+beta_feranchuk_squared;
                double secondTermFactor = Math.exp(beta_feranchuk_squared/(-2))/secondTermDivisor;
                double secondTerm = roughness_squared*q_squared/Math.sqrt(Math.PI)*secondTermFactor;
                roughri = kz0.multiply(kz1).multiply(roughness_factor).exp()
                          .add(secondTerm)
                          .multiply(ri);

                // phase factor times reflectivity coefficient
                b = kz1.multiply(d_times_minus_two_i).exp().multiply(R[j]);

                // recursive formula
                num = b.add(roughri);
                den = b.multiply(roughri).add(1);
                R[j] = num.divide(den);
            }
        }
        for(int i=0; i<R2.length; i++) {
            Complex Ri = R[i];
            double F = beam*Math.sin(alpha0rad[i]-offset);
            if (F > 1.0)
                F = 1.0;
            R2[i] = Ri.getReal()*Ri.getReal() + Ri.getImag()*Ri.getImag();
            R2[i] *= F;
        }

        if(filter != null)
            return applyOddFilter(filter, R2);
        else
            return R2;
    }
    /** The real simulation code that uses real complex numbers.
     *
     * <p>
     *
     * This function implements the real simulation.  Layers are passed as four
     * arrays of doubles that contain the numerical properties of the layers.
     * The lengths of all these arrays must equal the number of layers.
     *
     * <p>
     *
     * Convolution is silently ignored if alpha0rad is not uniformly spaced. If
     * it's necessary to know whether convolution is used, the caller must
     * check whether alpha0rad is uniformly spaced by the function
     * isUniformlySpaced
     *
     * <p>
     *
     * Unlike in simulate, the uppermost layer must be the ambient layer (air),
     * that is, delta[0] = beta[0] = 0. d[0] and r[0] have no meaning and are
     * therefore ignored. The thickness of the substrate, d[n-1], where
     * n is the number of layers, must be finite and not NaN but is
     * otherwise ignored.
     * 
     * @param alpha0rad angles of incidence in radians
     * @param delta an array containing delta for all the layers
     * @param beta an array containing beta for all the layers
     * @param d an array containing the thicknesses of all the layers
     * @param r an array containing the roughnesses of the upper interfaces of all the layers
     * @param lambda wavelength in meters
     * @param stddevrad standard deviation of angle (instrument resolution) in
     * radians.
     *
     * @return an array containing the absolute values of reflectivity for intensity
     *
     */

    public static double[] rawSimulateComplexBuffer(double[] alpha0rad, double[] delta, double[] beta, double[] betaFeranchuk, double[] d, double[] r, double lambda, double stddevrad, double beam, double offset) {
        ComplexBuffer[] R;
        double[] R2;
        ComplexBuffer[][] kz;
        double k0 = 2*Math.PI/lambda;
        final double stddevs = 4;
        double[] filter = null;
        double dalpha0rad = alpha0rad.length > 1 ? (alpha0rad[alpha0rad.length-1] - alpha0rad[0])/(alpha0rad.length-1) : 1;


        filter = gaussianFilter(dalpha0rad, stddevrad, stddevs);

        if(filter != null) {
            if(!isUniformlySpaced(alpha0rad)) {
                //throw new RuntimeException("Measurement points not uniformly spaced");
                filter = null;
            }
        }

        R = new ComplexBuffer[alpha0rad.length];
        for (int i = 0; i < R.length; i++)
        {
          R[i] = new ComplexBuffer();
        }
        R2 = new double[alpha0rad.length];
        kz = new ComplexBuffer[2][];
        for (int i = 0; i<kz.length; i++)
        {
            kz[i] = new ComplexBuffer[alpha0rad.length];
            for (int j = 0; j < alpha0rad.length; j++)
            {
              kz[i][j] = new ComplexBuffer();
            }
        }

        ComplexBuffer kz0, kz1;
        ComplexBuffer num = new ComplexBuffer(), den = new ComplexBuffer();
        ComplexBuffer ri = new ComplexBuffer();
        ComplexBuffer roughri = new ComplexBuffer();
        ComplexBuffer b = new ComplexBuffer(); // ri*ph
        ComplexBuffer d_times_minus_two_i = new ComplexBuffer();

        /* we only calculate wavevector for i==d.length,
         * other calculations are done starting from i==d.length-1 */
        for(int i=d.length; i>=1; i--) {

            ComplexBuffer[] kz0_ar = kz[(i-1)%2];
            ComplexBuffer[] kz1_ar = kz[i%2];

            double two_times_delta = 2*delta[i-1];
            double minus_two_times_beta = -2*beta[i-1];

            /* a bit tricky optimization */
            for(int j=0; j<alpha0rad.length; j++) {
                double alpha0 = alpha0rad[j]-offset;

                // Calculate z component of wavevector
                kz0_ar[j].set(alpha0*alpha0-two_times_delta, minus_two_times_beta)
                         .sqrtInPlace().multiplyInPlace(k0);
            }
            if(i == d.length)
                continue;

            double roughness = r[i];
            double beta_feranchuk = betaFeranchuk[i];
            double beta_feranchuk_squared = beta_feranchuk * beta_feranchuk;
            double roughness_squared = roughness*roughness;
            double roughness_factor = -2*roughness_squared;
            d_times_minus_two_i.set(MINUS_TWO_I).multiplyInPlace(d[i]);

            for(int j=0; j<alpha0rad.length; j++) {
                double q = 2*k0*(alpha0rad[j]-offset);
                double q_squared = q*q;
                kz0 = kz0_ar[j];
                kz1 = kz1_ar[j];

                // Fresnel reflection coefficient
                num.set(kz0).subtractInPlace(kz1);
                den.set(kz0).addInPlace(kz1);
                ri.set(num).divideInPlace(den);
                // this can actually occur at small angles when there's no reflection.
                if (ri.isNaN())
                {
                  ri.set(0, 0);
                }
                /*
                roughri.set(kz0).multiplyInPlace(kz1).multiplyInPlace(roughness_factor)
                       .expInPlace().multiplyInPlace(ri); */
                double secondTermDivisor = q_squared*roughness_squared+beta_feranchuk_squared;
                double secondTermFactor = Math.exp(beta_feranchuk_squared/(-2))/secondTermDivisor;
                double secondTerm = roughness_squared*q_squared/Math.sqrt(Math.PI)*secondTermFactor;
                roughri.set(kz0).multiplyInPlace(kz1).multiplyInPlace(roughness_factor)
                       .expInPlace().addInPlace(secondTerm).multiplyInPlace(ri);

                // phase factor times reflectivity coefficient
                b.set(kz1).multiplyInPlace(d_times_minus_two_i).expInPlace()
                 .multiplyInPlace(R[j]);

                // recursive formula
                num.set(b).addInPlace(roughri);
                den.set(b).multiplyInPlace(roughri).addInPlace(1);
                R[j].set(num).divideInPlace(den);
            }
        }
        for(int i=0; i<R2.length; i++) {
            ComplexBuffer Ri = R[i];
            double re = Ri.getReal(), im = Ri.getImag();
            double F = beam*Math.sin(alpha0rad[i]-offset);
            if (F > 1.0)
                F = 1.0;
            R2[i] = re*re + im*im;
            R2[i] *= F;
        }

        if(filter != null)
            return applyOddFilter(filter, R2);
        else
            return R2;
    }

    /** The real simulation code that uses real complex numbers.
     *
     * <p>
     *
     * This function implements the real simulation.  Layers are passed as four
     * arrays of doubles that contain the numerical properties of the layers.
     * The lengths of all these arrays must equal the number of layers.
     *
     * <p>
     *
     * Convolution is silently ignored if alpha0rad is not uniformly spaced. If
     * it's necessary to know whether convolution is used, the caller must
     * check whether alpha0rad is uniformly spaced by the function
     * isUniformlySpaced
     *
     * <p>
     *
     * Unlike in simulate, the uppermost layer must be the ambient layer (air),
     * that is, delta[0] = beta[0] = 0. d[0] and r[0] have no meaning and are
     * therefore ignored. The thickness of the substrate, d[n-1], where
     * n is the number of layers, must be finite and not NaN but is
     * otherwise ignored.
     * 
     * @param alpha0rad angles of incidence in radians
     * @param delta an array containing delta for all the layers
     * @param beta an array containing beta for all the layers
     * @param d an array containing the thicknesses of all the layers
     * @param r an array containing the roughnesses of the upper interfaces of all the layers
     * @param lambda wavelength in meters
     * @param stddevrad standard deviation of angle (instrument resolution) in
     * radians.
     *
     * @return an array containing the absolute values of reflectivity for intensity
     *
     */

    public static double[] rawSimulateComplexBufferArray(double[] alpha0rad, double[] delta, double[] beta, double[] betaFeranchuk, double[] d, double[] r, double lambda, double stddevrad, double beam, double offset) {
        ComplexBufferArray R;
        double[] R2;
        ComplexBufferArray[] kz;
        double k0 = 2*Math.PI/lambda;
        final double stddevs = 4;
        double[] filter = null;
        double dalpha0rad = alpha0rad.length > 1 ? (alpha0rad[alpha0rad.length-1] - alpha0rad[0])/(alpha0rad.length-1) : 1;


        filter = gaussianFilter(dalpha0rad, stddevrad, stddevs);

        if(filter != null) {
            if(!isUniformlySpaced(alpha0rad)) {
                //throw new RuntimeException("Measurement points not uniformly spaced");
                filter = null;
            }
        }

        R = new ComplexBufferArray(alpha0rad.length);
        R2 = new double[alpha0rad.length];
        kz = new ComplexBufferArray[2];
        for (int i = 0; i<kz.length; i++)
        {
            kz[i] = new ComplexBufferArray(alpha0rad.length);
        }

        //ComplexBuffer kz0, kz1;
        ComplexBuffer num = new ComplexBuffer(), den = new ComplexBuffer();
        ComplexBuffer ri = new ComplexBuffer();
        ComplexBuffer roughri = new ComplexBuffer();
        ComplexBuffer b = new ComplexBuffer(); // ri*ph
        ComplexBuffer d_times_minus_two_i = new ComplexBuffer();

        /* we only calculate wavevector for i==d.length,
         * other calculations are done starting from i==d.length-1 */
        for(int i=d.length; i>=1; i--) {

            ComplexBufferArray kz0_ar = kz[(i-1)%2];
            ComplexBufferArray kz1_ar = kz[i%2];

            double two_times_delta = 2*delta[i-1];
            double minus_two_times_beta = -2*beta[i-1];

            /* a bit tricky optimization */
            for(int j=0; j<alpha0rad.length; j++) {
                double alpha0 = alpha0rad[j]-offset;

                // Calculate z component of wavevector
                kz0_ar.set(j, alpha0*alpha0-two_times_delta, minus_two_times_beta)
                         .sqrtInPlace(j).multiplyInPlace(j, k0);
            }
            if(i == d.length)
                continue;

            double roughness = r[i];
            double beta_feranchuk = betaFeranchuk[i];
            double beta_feranchuk_squared = beta_feranchuk * beta_feranchuk;
            double roughness_squared = roughness*roughness;
            double roughness_factor = -2*roughness_squared;
            d_times_minus_two_i.set(MINUS_TWO_I).multiplyInPlace(d[i]);

            for(int j=0; j<alpha0rad.length; j++) {
                double q = 2*k0*(alpha0rad[j]-offset);
                double q_squared = q*q;
                //kz0 = kz0_ar[j];
                //kz1 = kz1_ar[j];

                // Fresnel reflection coefficient
                num.set(kz0_ar, j).subtractInPlace(kz1_ar, j);
                den.set(kz0_ar, j).addInPlace(kz1_ar, j);
                ri.set(num).divideInPlace(den);
                // this can actually occur at small angles when there's no reflection.
                if (ri.isNaN())
                {
                  ri.set(0, 0);
                }
                // roughri = exp(-kz0*kz1*2*r^2)*ri
                /*
                roughri.set(kz0_ar, j).multiplyInPlace(kz1_ar, j).multiplyInPlace(roughness_factor)
                       .expInPlace().multiplyInPlace(ri); */
                double secondTermDivisor = q_squared*roughness_squared+beta_feranchuk_squared;
                double secondTermFactor = Math.exp(beta_feranchuk_squared/(-2))/secondTermDivisor;
                double secondTerm = roughness_squared*q_squared/Math.sqrt(Math.PI)*secondTermFactor;
                roughri.set(kz0_ar, j).multiplyInPlace(kz1_ar, j).multiplyInPlace(roughness_factor)
                       .expInPlace().addInPlace(secondTerm).multiplyInPlace(ri);

                // phase factor times reflectivity coefficient
                b.set(kz1_ar, j).multiplyInPlace(d_times_minus_two_i).expInPlace()
                 .multiplyInPlace(R, j);

                // recursive formula
                num.set(b).addInPlace(roughri);
                den.set(b).multiplyInPlace(roughri).addInPlace(1);
                R.set(j, num).divideInPlace(j, den);
            }
        }
        for(int i=0; i<R2.length; i++) {
            double re = R.getReal(i), im = R.getImag(i);
            double F = beam*Math.sin(alpha0rad[i]-offset);
            if (F > 1.0)
                F = 1.0;
            R2[i] = re*re + im*im;
            R2[i] *= F;
        }

        if(filter != null)
            return applyOddFilter(filter, R2);
        else
            return R2;
    }


    /** The real simulation code.
     *
     * <p>
     *
     * This function implements the real simulation.  Layers are passed as four
     * arrays of doubles that contain the numerical properties of the layers.
     * The lengths of all these arrays must equal the number of layers.
     *
     * <p>
     *
     * Convolution is silently ignored if alpha0rad is not uniformly spaced. If
     * it's necessary to know whether convolution is used, the caller must
     * check whether alpha0rad is uniformly spaced by the function
     * isUniformlySpaced
     *
     * <p>
     *
     * Unlike in simulate, the uppermost layer must be the ambient layer (air),
     * that is, delta[0] = beta[0] = 0. d[0] and r[0] have no meaning and are
     * therefore ignored. The thickness of the substrate, d[n-1], where
     * n is the number of layers, must be finite and not NaN but is
     * otherwise ignored.
     * 
     * @param alpha0rad angles of incidence in radians
     * @param delta an array containing delta for all the layers
     * @param beta an array containing beta for all the layers
     * @param d an array containing the thicknesses of all the layers
     * @param r an array containing the roughnesses of the upper interfaces of all the layers
     * @param lambda wavelength in meters
     * @param stddevrad standard deviation of angle (instrument resolution) in
     * radians.
     *
     * @return an array containing the absolute values of reflectivity for intensity
     *
     */

    public static double[] rawSimulate(double[] alpha0rad, double[] delta, double[] beta, double[] betaFeranchuk, double[] d, double[] r, double lambda, double stddevrad, double beam, double offset) {
        double[] R_real;
        double[] R_imag;
        double[] R2;
        double[][] kz_reals;
        double[][] kz_imags;
        double k0 = 2*Math.PI/lambda;
        final double stddevs = 4;
        double[] filter = null;
        double dalpha0rad = alpha0rad.length > 1 ? (alpha0rad[alpha0rad.length-1] - alpha0rad[0])/(alpha0rad.length-1) : 1;


        filter = gaussianFilter(dalpha0rad, stddevrad, stddevs);

        if(filter != null) {
            if(!isUniformlySpaced(alpha0rad)) {
                //throw new RuntimeException("Measurement points not uniformly spaced");
                filter = null;
            }
        }

        R_real = new double[alpha0rad.length];
        R_imag = new double[alpha0rad.length];
        R2 = new double[alpha0rad.length];
        kz_reals = new double[2][];
        kz_imags = new double[2][];
        for(int i=0; i<kz_reals.length; i++) {
            kz_reals[i] = new double[alpha0rad.length];
            kz_imags[i] = new double[alpha0rad.length];
        }
        for(int i=0; i<alpha0rad.length; i++) // this is important
            R_real[i] = R_imag[i] = 0;

        /* we only calculate wavevector for i==d.length,
         * other calculations are done starting from i==d.length-1 */
        for(int i=d.length; i>=1; i--) {

            /* a bit tricky optimization */
            for(int j=0; j<alpha0rad.length; j++) {
                double alpha0 = alpha0rad[j]-offset;

                // Calculate z component of wavevector
                double sq_real = alpha0*alpha0 - 2*delta[i-1];
                double sq_imag = - 2*beta[i-1];
                double absval = Math.sqrt(sq_real*sq_real + sq_imag*sq_imag);
                kz_reals[(i-1)%2][j] = k0*Math.sqrt((absval+sq_real)/2);
                kz_imags[(i-1)%2][j] = -k0*Math.sqrt((absval-sq_real)/2);
            }
            if(i == d.length)
                continue;

            double roughness = r[i];
            double beta_feranchuk = betaFeranchuk[i];
            double beta_feranchuk_squared = beta_feranchuk * beta_feranchuk;
            double roughness_squared = roughness*roughness;
            double roughness_factor = -2*roughness_squared;

            for(int j=0; j<alpha0rad.length; j++) {
                double q = 2*k0*(alpha0rad[j]-offset);
                double q_squared = q*q;
                double kz_real = kz_reals[(i-1)%2][j];
                double kz_imag = kz_imags[(i-1)%2][j];
                double kz1_real = kz_reals[i%2][j];
                double kz1_imag = kz_imags[i%2][j];

                // phase factor
                double ph_real = Math.exp(2*kz1_imag*d[i])*Math.cos(-2*kz1_real*d[i]);
                double ph_imag = Math.exp(2*kz1_imag*d[i])*Math.sin(-2*kz1_real*d[i]);


                // numerator and denominator
                double num_real, num_imag;
                double den_real, den_imag;
                double divisor;

                // Fresnel reflection coefficient
                double ri_real, ri_imag; // Fresnel refl. coeff
                double roughri_real, roughri_imag; // Fresnel refl. coeff

                num_real = kz_real - kz1_real;
                num_imag = kz_imag - kz1_imag;
                den_real = kz_real + kz1_real;
                den_imag = kz_imag + kz1_imag;

                divisor = den_real*den_real + den_imag*den_imag;

                ri_real = (num_real*den_real + num_imag*den_imag)/divisor; /* divisor */
                ri_imag = (num_imag*den_real - num_real*den_imag)/divisor;

                /* this can actually occur at small angles when there's no reflection. */
                if(Double.isNaN(ri_real) || Double.isNaN(ri_imag)) {
                    ri_real = 0;
                    ri_imag = 0;
                }

                double kzkz1_real = kz_real*kz1_real-kz_imag*kz1_imag;
                double kzkz1_imag = kz_real*kz1_imag+kz_imag*kz1_real;
                double roughexp_real = -2*r[i]*r[i]*kzkz1_real;
                double roughexp_imag = -2*r[i]*r[i]*kzkz1_imag;
                double rough_real = Math.exp(roughexp_real)*Math.cos(roughexp_imag);
                double rough_imag = Math.exp(roughexp_real)*Math.sin(roughexp_imag);
                double secondTermDivisor = q_squared*roughness_squared+beta_feranchuk_squared;
                double secondTermFactor = Math.exp(beta_feranchuk_squared/(-2))/secondTermDivisor;
                double secondTerm = roughness_squared*q_squared/Math.sqrt(Math.PI)*secondTermFactor;
                rough_real += secondTerm;

                roughri_real = ri_real*rough_real - ri_imag*rough_imag;
                roughri_imag = ri_real*rough_imag + ri_imag*rough_real;




                // recursive formula

                double b_real, b_imag; // ri*ph

                // a = ri + R[j]*ph
                num_real = roughri_real + R_real[j]*ph_real - R_imag[j]*ph_imag;
                num_imag = roughri_imag + R_real[j]*ph_imag + R_imag[j]*ph_real;

                // b = ri*ph
                b_real = roughri_real*ph_real - roughri_imag*ph_imag;
                b_imag = roughri_real*ph_imag + roughri_imag*ph_real;

                // c = 1 + R[j]*b = 1 + R[j]*ri*ph
                den_real = 1 + R_real[j]*b_real - R_imag[j]*b_imag;
                den_imag = R_real[j]*b_imag + R_imag[j]*b_real;

                divisor = den_real*den_real + den_imag*den_imag;

                R_real[j] = (num_real*den_real + num_imag*den_imag)/divisor; /* divisor */
                R_imag[j] = (num_imag*den_real - num_real*den_imag)/divisor;
            }
        }
        for(int i=0; i<R2.length; i++) {
            double F = beam*Math.sin(alpha0rad[i]-offset);
            if (F > 1.0)
                F = 1.0;
            R2[i] = R_real[i]*R_real[i] + R_imag[i]*R_imag[i];
            R2[i] *= F;
        }


        if(filter != null)
            return applyOddFilter(filter, R2);
        else
            return R2;
    }
    /** Call simulation with layers from a LayerStack.
     *
     * <p>
     *
     * The ambient layer (air) is included automatically in the simulation.
     *
     * @param alpha0rad angles of incidence in radians
     * @param layers the layer stack to simulate
     *
     */

    public static double[] simulateComplex(double[] alpha0rad, LayerStack layers) {
        double[] delta;
        double[] beta, betaFeranchuk;
        double[] d; // layer thickness
        double[] r; // top interface roughness
        double lambda = layers.getLambda();
        double stddevrad = layers.getStdDev().getExpected();
        double beam = layers.getBeam().getExpected();
        double offset = layers.getOffset().getExpected()*Math.PI/180;

        /* convert the layer stack to delta, beta, thickness and roughness arrays */

        delta = new double[layers.getSize()+1];
        beta = new double[layers.getSize()+1];
        betaFeranchuk = new double[layers.getSize()+1];
        d = new double[layers.getSize()+1];
        r = new double[layers.getSize()+1];

        delta[0] = beta[0] = betaFeranchuk[0] = d[0] = r[0] = 0; /* ambient (air) */

        for(int i=0; i<d.length-1; i++) {
            Layer layer = layers.getElementAt(i);
            Compound compound = layer.getXRRCompound();

            d[i+1] = layers.getElementAt(i).getThickness().getExpected();
            r[i+1] = layers.getElementAt(i).getRoughness().getExpected();
            delta[i+1] = layer.getDensity().getExpected() * compound.getDeltaPerRho();
            betaFeranchuk[i+1] = layer.getBetaF().getExpected();
            beta[i+1] = delta[i+1] * compound.getBetaPerDelta();
        }

        return rawSimulateComplex(alpha0rad, delta, beta, betaFeranchuk, d, r, lambda, stddevrad, beam, offset);
    }
    /** Call simulation with layers from a LayerStack.
     *
     * <p>
     *
     * The ambient layer (air) is included automatically in the simulation.
     *
     * @param alpha0rad angles of incidence in radians
     * @param layers the layer stack to simulate
     *
     */

    public static double[] simulateComplexBuffer(double[] alpha0rad, LayerStack layers) {
        double[] delta;
        double[] beta, betaFeranchuk;
        double[] d; // layer thickness
        double[] r; // top interface roughness
        double lambda = layers.getLambda();
        double stddevrad = layers.getStdDev().getExpected();
        double beam = layers.getBeam().getExpected();
        double offset = layers.getOffset().getExpected()*Math.PI/180;

        /* convert the layer stack to delta, beta, thickness and roughness arrays */

        delta = new double[layers.getSize()+1];
        beta = new double[layers.getSize()+1];
        betaFeranchuk = new double[layers.getSize()+1];
        d = new double[layers.getSize()+1];
        r = new double[layers.getSize()+1];

        delta[0] = beta[0] = betaFeranchuk[0] = d[0] = r[0] = 0; /* ambient (air) */

        for(int i=0; i<d.length-1; i++) {
            Layer layer = layers.getElementAt(i);
            Compound compound = layer.getXRRCompound();

            d[i+1] = layers.getElementAt(i).getThickness().getExpected();
            r[i+1] = layers.getElementAt(i).getRoughness().getExpected();
            delta[i+1] = layer.getDensity().getExpected() * compound.getDeltaPerRho();
            beta[i+1] = delta[i+1] * compound.getBetaPerDelta();
            betaFeranchuk[i+1] = layers.getElementAt(i).getBetaF().getExpected();
        }

        return rawSimulateComplexBuffer(alpha0rad, delta, beta, betaFeranchuk, d, r, lambda, stddevrad, beam, offset);
    }

    /** Call simulation with layers from a LayerStack.
     *
     * <p>
     *
     * The ambient layer (air) is included automatically in the simulation.
     *
     * @param alpha0rad angles of incidence in radians
     * @param layers the layer stack to simulate
     *
     */

    public static double[] simulateComplexBufferArray(double[] alpha0rad, LayerStack layers) {
        double[] delta;
        double[] beta;
        double[] betaFeranchuk;
        double[] d; // layer thickness
        double[] r; // top interface roughness
        double lambda = layers.getLambda();
        double stddevrad = layers.getStdDev().getExpected();
        double beam = layers.getBeam().getExpected();
        double offset = layers.getOffset().getExpected()*Math.PI/180;

        /* convert the layer stack to delta, beta, thickness and roughness arrays */

        delta = new double[layers.getSize()+1];
        beta = new double[layers.getSize()+1];
        betaFeranchuk = new double[layers.getSize()+1];
        d = new double[layers.getSize()+1];
        r = new double[layers.getSize()+1];

        delta[0] = beta[0] = betaFeranchuk[0] = d[0] = r[0] = 0; /* ambient (air) */

        for(int i=0; i<d.length-1; i++) {
            Layer layer = layers.getElementAt(i);
            Compound compound = layer.getXRRCompound();

            d[i+1] = layers.getElementAt(i).getThickness().getExpected();
            r[i+1] = layers.getElementAt(i).getRoughness().getExpected();
            betaFeranchuk[i+1] = layers.getElementAt(i).getBetaF().getExpected();
            delta[i+1] = layer.getDensity().getExpected() * compound.getDeltaPerRho();
            beta[i+1] = delta[i+1] * compound.getBetaPerDelta();
        }

        return rawSimulateComplexBufferArray(alpha0rad, delta, beta, betaFeranchuk, d, r, lambda, stddevrad, beam, offset);
    }

    /** Call simulation with layers from a LayerStack.
     *
     * <p>
     *
     * The ambient layer (air) is included automatically in the simulation.
     *
     * @param alpha0rad angles of incidence in radians
     * @param layers the layer stack to simulate
     *
     */

    public static double[] simulate(double[] alpha0rad, LayerStack layers) {
        double[] delta;
        double[] beta, betaFeranchuk;
        double[] d; // layer thickness
        double[] r; // top interface roughness
        double lambda = layers.getLambda();
        double stddevrad = layers.getStdDev().getExpected();
        double beam = layers.getBeam().getExpected();
        double offset = layers.getOffset().getExpected()*Math.PI/180;

        /* convert the layer stack to delta, beta, thickness and roughness arrays */

        delta = new double[layers.getSize()+1];
        beta = new double[layers.getSize()+1];
        betaFeranchuk = new double[layers.getSize()+1];
        d = new double[layers.getSize()+1];
        r = new double[layers.getSize()+1];

        delta[0] = beta[0] = betaFeranchuk[0] = d[0] = r[0] = 0; /* ambient (air) */

        for(int i=0; i<d.length-1; i++) {
            Layer layer = layers.getElementAt(i);
            Compound compound = layer.getXRRCompound();

            d[i+1] = layers.getElementAt(i).getThickness().getExpected();
            r[i+1] = layers.getElementAt(i).getRoughness().getExpected();
            delta[i+1] = layer.getDensity().getExpected() * compound.getDeltaPerRho();
            beta[i+1] = delta[i+1] * compound.getBetaPerDelta();
            betaFeranchuk[i+1] = layers.getElementAt(i).getBetaF().getExpected();
        }

        return rawSimulate(alpha0rad, delta, beta, betaFeranchuk, d, r, lambda, stddevrad, beam, offset);
    }
}
