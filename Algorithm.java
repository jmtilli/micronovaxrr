/** Enumeration of the supported algorithms.
 *
 * All the supported algorithms and their Octave and human-readable names.
 */
public enum Algorithm {
    DE("DE","DE"),
    CovDE("CovDE","CovDE"),
    LinDE("LinDE","LinDE");

    /** Octave name */
    public final String octName;
    private final String name;
    Algorithm(String octName, String name) {
        this.octName = octName;
        this.name = name;
    }
    public String toString() {
        return name;
    }
};

