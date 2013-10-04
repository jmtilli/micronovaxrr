/** Invalid fencodeable structure exception.
 *
 * Thrown when you are trying to import an object from a data structure which
 * is not a valid representation of an object of that kind.
 */
public class InvalidStructException extends Exception {
    public InvalidStructException() {
        super("Invalid structure");
    }
    public InvalidStructException(String s) {
        super(s);
    }
}
