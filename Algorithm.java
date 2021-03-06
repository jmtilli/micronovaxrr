/** Enumeration of the supported algorithms.
 *
 * All the supported algorithms and their human-readable names.
 */
public enum Algorithm {
    JavaCovDE("JavaCovDE"),
    JavaDE("JavaDE"),
    JavaEitherOrDE("JavaEitherOrDE");

    private final String name;
    Algorithm(String name) {
        this.name = name;
    }
    public String toString() {
        return name;
    }
};
