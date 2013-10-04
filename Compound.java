import java.util.*;

/** Optical information of a compound for an x-ray wavelength.
 *
 * This class stores the optical properties of a compound for one wavelength.
 * As the optical properties -- delta, beta -- of a layer for x-ray wavelengths
 * are proportional to the density, only the proportionality constants of delta
 * and beta are stored. The wavelength for which these constants are valid is
 * also stored.
 */

public class Compound {
    private final double deltaPerRho, betaPerRho, lambda;
    private final double rhoEPerRho;

    private static final double r_e = 2.817940325e-15; /* Classical electron radius */
    private static final double Na = 6.0221415e23; /* Avogadro's number */

    /** Constructor.
     *
     * This constructor calculates the optical properties for a compound. The
     * optical properties of elements for the specified wavelength are taken
     * from a LookupTable.
     *
     * @param composition the composition, specified as an element-&gt;amount
     * map.
     * @param table lookup table 
     * @param lambda wavelength in meters
     *
     * @throws ElementNotFound optical information of an element for the
     * specified wavelength was not found in the lookup table
     *
     */

    public Compound(Map<String,Double> composition, LookupTable table, double lambda) throws ElementNotFound {
        double rhoEPerRho = 0;
        double rhoEPerRho_i = 0;
        double molar_mass = 0;
        double rho = 1;

        for(String s: composition.keySet()) {
            /* IMPORTANT NOTE: this method must lookup() every element even if n == 0 for an element */
            double n = composition.get(s);
            Element e = table.lookup(s, lambda);
            rhoEPerRho += n*e.f1;
            rhoEPerRho_i += n*e.f2;
            molar_mass += n*e.A/1e3; /* Chemists like to use grams, but we use kilograms */
        }
        if(molar_mass != 0)
            rhoEPerRho *= Na/molar_mass;
        if(molar_mass != 0)
            rhoEPerRho_i *= Na/molar_mass;
        deltaPerRho = lambda*lambda*r_e*rhoEPerRho/2/Math.PI;
        betaPerRho = lambda*lambda*r_e*rhoEPerRho_i/2/Math.PI;
        this.lambda = lambda;
        this.rhoEPerRho = rhoEPerRho;
    }

    /** returns delta/rho in SI units */
    public double getDeltaPerRho() {
        return deltaPerRho;
    }
    /** returns rho_e/rho in SI units */
    public double getRhoEPerRho() {
        return rhoEPerRho;
    }
    /** returns beta/rho in SI units */
    public double getBetaPerRho() {
        return betaPerRho;
    }
    /** returns beta/delta */
    public double getBetaPerDelta() {
        if(deltaPerRho == 0)
            return 0;
        else
            return betaPerRho / deltaPerRho;
    }
    /** returns wavelength in SI units */
    public double getLambda() {
        return lambda;
    }
}
