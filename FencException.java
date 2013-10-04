/** fencoding exception.
 *
 * This is thrown when the data structure to encode contains unsupported data
 * types (for example, dictionaries with keys other than strings or other data
 * types than integers, strings, floating point numbers, lists and
 * dictionaries) or strings unconvertible to UTF-8.
 */
public class FencException extends FException {
    public FencException(String s) {
        super(s);
    }
};
