/** Import exception.
 *
 * <p>
 *
 * This is thrown when the file format of the measurement file to import is
 * invalid.
 */

public class XRRImportException extends Exception {
    /** Default constructor.
     *
     * <p>
     *
     * The default error message is "Invalid file format".
     *
     */
    public XRRImportException() {
        super("Invalid file format");
    }
    /** Constructor.
     *
     * @param s the human-readable error message
     */
    public XRRImportException(String s) {
        super(s);
    }
}
