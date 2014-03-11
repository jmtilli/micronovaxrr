/** Enumeration of the supported algorithms.
 *
 * All the supported algorithms and their Octave and human-readable names.
 */
public enum Algorithm {
    JavaDE("JavaDE","JavaDE",true),
    JavaCovDE("JavaCovDE","JavaCovDE",true),
    DE("DE","DE",false),
    CovDE("CovDE","CovDE",false),
    LinDE("LinDE","LinDE",false);

    /** Octave name */
    public final String octName;
    public final boolean isJava;
    private final String name;
    Algorithm(String octName, String name, boolean isJava) {
        this.octName = octName;
        this.name = name;
        this.isJava = isJava;
    }
    public String toString() {
        return name;
    }
};
