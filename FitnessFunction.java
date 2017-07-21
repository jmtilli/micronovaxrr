/** Enumeration of the supported algorithms.
 *
 * All the supported algorithms and their human-readable names.
 */
public enum FitnessFunction {
    relchi2transform("p-norm in MR/chi^2 space"),
    relchi2("Mixed relative / chi-squared"),
    logfitness("p-norm in logarithmic space"),
    sqrtfitness("p-norm in sqrt-space"),
    chi2("chi-squared");

    private final String name;
    FitnessFunction(String name) {
        this.name = name;
    }
    public String toString() {
        return name;
    }
};
