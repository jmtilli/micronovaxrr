/** X-ray properties of an element for one wavelength.
 * <p>
 * This class stores the x-ray properties of an element for one wavelength.
 * The wavelength is not stored in this class.
 * <p>
 * The real (<i>f</i><sub>1</sub>) and imaginary (<i>f</i><sub>2</sub>) scattering factors of an element are
 * defined by the following equation:
 * <p>
 * <i>n</i> = 1 - <i>N</i>*<i>r<sub>e</sub></i>*<i>&lambda;</i>^2*(<i>f</i><sub>1</sub> + <b>i</b>*<i>f</i><sub>2</sub>) / (2<i>&pi;</i>)
 * <p>
 * where <i>N</i> is the number of atoms in unit volume, <i>r<sub>e</sub></i> the classical electron
 * radius, <i>&lambda;</i> the wavelength and <i>n</i> the complex refractive index.
 *
 */

public class Element {
    /** Real scattering factor */
    public final double f1;
    /** Imaginary scattering factor */
    public final double f2;
    /** The atomic mass in grams/mol */
    public final double A;
    /** Constructor */
    public Element(double f1, double f2, double A) {
        this.f1 = f1;
        this.f2 = f2;
        this.A = A;
    }
}
