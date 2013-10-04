import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;



/** Fencode: serialization of simple data structures.
 * <p>
 * Fcode implements serialization of arbitrary structures consisting of
 * integers, UTF-8 strings, floating point numbers, lists and dictionaries
 * (keys of which must be strings).
 * <p>
 * This is inspired by bencode, a simple cross-platform serialization which
 * implements one-to-one mapping between values and their bencoded
 * representations. However, bencode does not support floating point numbers.
 * <p>
 * One of the basic ideas is that data structures are implemented as
 * dictionaries, the unsupported keys of which are ignored. It is an easy way
 * to support forward and backward compatibility.
 * <p>
 * Among other supported data types, this class implements floating point
 * serialization. The supported floating point format is IEEE 754
 * double-precision. Unfortunately, due to the characteristics of floating
 * point numbers, bijection between values and their fencoded representations
 * can't be guaranteed unless IEEE 754 double precision arithmetic is natively
 * supported by the underlying platform, and even in that case doesn't make
 * sense and is untested.
 * <p>
 * Floating point formats are stored as 'f' followed by a 16-byte uppercase
 * hexadecimal number, the format of which is described by the documentation of
 * Double.doubleToLongBits. The representation is not terminated with an 'e'
 * since it is fixed-width. The encoding of other data types is compatible
 * with bencode: <a href="http://en.wikipedia.org/wiki/Bencode">http://en.wikipedia.org/wiki/Bencode</a>
 * <p>
 * All strings are converted to UTF-8 when encoding, and converted back to the
 * Java representation when decoding. Strings can therefore NOT be used for
 * storing arbitrary binary data. If you need support for binary data, consider
 * adding a string-like type with no UTF-8 conversion.
 * <p>
 * The implementation is a quick hack, which doesn't guard against integer
 * overflows and maps multiple Java datatypes to the same fencoded data type,
 * breaking bijection. For any serious work this code should be audited
 * thoroughly.
 */

public class Fcode {

    private Fcode() {}

    /* This is called when a number is read from s, starting an encoded
     * representation for a string. The number read is stored in i and this
     * function does the rest.
     */
    private static String readStringRemainder(InputStream s, int i) throws FdecException, IOException {
        int ch;
        assert(i >= 0 && i <= 9);
        if(i == 0) {
            ch = s.read();
            if(ch == -1)
                throw new FdecException("EOF");
            if(ch != ':')
                throw new FdecException("String length starting with 0");
            return "";
        }

forloop:
        for(;;) {
            switch(ch = s.read()) {
                case '0':
                case '1': case '2': case '3':
                case '4': case '5': case '6':
                case '7': case '8': case '9':
                    i *= 10;
                    i += ch - '0';
                    break;
                case ':':
                    break forloop;
                case -1:
                    throw new FdecException("EOF");
                default:
                    throw new FdecException("Non-numeric character in strlen");
            }
        }
        byte[] buf = new byte[i];
        int offset = 0;

        while(offset < i) {
            int bytes_read;
            bytes_read = s.read(buf, offset, i - offset);
            if(bytes_read == -1)
                throw new FdecException("EOF");
            assert(bytes_read>0);
            offset += bytes_read;
        }
        assert(offset == i);
        CharBuffer out;
        try {
            out = Charset.forName("UTF-8").newDecoder().decode(ByteBuffer.wrap(buf));
        }
        catch(CharacterCodingException ex) {
            throw new FdecException("Invalid UTF-8 encoding");
        }
        out.rewind();
        return out.toString();
        /* These are already defaults:
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        */
    }

    /* Encode an integer (64-bit long) */
    private static void fencLong(long l, OutputStream s) throws IOException {
        String str = "i"+l+"e";
        s.write(str.getBytes());
    }
    /* Encode a floating point number (64-bit IEEE 754 double precision) */
    private static void fencDouble(double d, OutputStream s) throws IOException {
        String zeros = "0000000000000000";
        String str = Long.toHexString(Double.doubleToLongBits(d)).toUpperCase();
        String str2 = zeros.substring(str.length())+str;
        assert(str2.length() == 16);
        s.write(("f"+str2).getBytes());
    }
    /* Encode a string. The string is converted to UTF-8 */
    private static void fencString(String str, OutputStream s) throws FencException, IOException {
        try {
            byte[] b1 = toUTF8(str);
            byte[] b2 = (""+b1.length).getBytes();
            byte[] b3 = new byte[b1.length+b2.length+1];
            System.arraycopy(b2, 0, b3, 0, b2.length);
            b3[b2.length] = ':';
            System.arraycopy(b1, 0, b3, b2.length+1, b1.length);
            s.write(b3);
        }
        catch(CharacterCodingException ex) {
            throw new FencException("Can't conver string to UTF-8");
        }
    }
    /* encode a dictionary, the keys of which must be strings */
    private static void fencMap(Map<?,?> m, OutputStream s) throws FencException, IOException {
        s.write('d');
        for(Object o: new TreeSet<Object>(m.keySet())) {
            if(!(o instanceof String))
                throw new FencException("dict keys must be strings");
            internal_fencode(o, s);
            internal_fencode(m.get(o), s);
        }
        s.write('e');
    }
    /** Encode a data structure.
     *
     * @param o The object to encode, the data type of which must be one of the supported fencode data types.
     * @param s The output stream to encode the object to.
     * @throws IOException If an I/O exception occurs
     * @throws FencException If the data type of o is not supported or contains strings that can't be converted to UTF-8
     */
    public static void fencode(Object o, OutputStream s) throws FencException, IOException {
        BufferedOutputStream bs = new BufferedOutputStream(s);
        internal_fencode(o, bs);
        bs.flush();
    };
    private static void internal_fencode(Object o, OutputStream s) throws FencException, IOException {
        if(o instanceof String)
            fencString((String)o,s);
        else if(o instanceof Integer)
            fencLong((Integer)o,s);
        else if(o instanceof Long)
            fencLong((Long)o,s);
        else if(o instanceof Short)
            fencLong((Short)o,s);
        else if(o instanceof Byte)
            fencLong((Byte)o,s);
        else if(o instanceof Double)
            fencDouble((Double)o,s);
        else if(o instanceof Float)
            fencDouble((Float)o,s);
        else if(o instanceof ArrayList) {
            ArrayList<?> l = (ArrayList<?>)o;
            s.write('l');
            for(Object o2: l) {
                internal_fencode(o2, s);
            }
            s.write('e');
        }
        else if(o instanceof Map) {
            fencMap((Map<?,?>)o, s);
        }
        else
            throw new FencException("Unknown data type");
    }
    /* this is called when an 'i' is read from s */
    private static int readIntRemainder(InputStream s) throws FdecException, IOException {
        int i = 0;
        int ch;
        boolean minus = false;
        for(;;) switch(ch = s.read()) {
            case -1:
                throw new FdecException("EOF");
            case '-':
                if(i != 0 || minus)
                    throw new FdecException("Minus sign in the middle of an integer");
                minus = true;
                break;
            case '0':
                if(i == 0) {
                    ch = s.read();
                    if(ch == -1)
                        throw new FdecException("EOF");
                    if(ch != 'e')
                        throw new FdecException("Int starting with 0");
                    if(minus)
                        throw new FdecException("Invalid encoding for 0");
                    return 0;
                }
            case '1': case '2': case '3':
            case '4': case '5': case '6':
            case '7': case '8': case '9':
                i *= 10;
                i += ch - '0';
                break;
            case 'e':
                if(i == 0)
                    throw new FdecException("Invalid encoding");
                return minus?-i:i;
            default:
                throw new FdecException("Non-numeric character in integer");
        }
    }
    /* Convert s to UTF-8 */
    private static byte[] toUTF8(String s) throws CharacterCodingException {
        ByteBuffer out;
        byte[] b;
        out = Charset.forName("UTF-8").newEncoder().encode(CharBuffer.wrap(s.toCharArray()));
        out.rewind();
        b = new byte[out.remaining()];
        out.get(b);
        assert(!out.hasRemaining());
        return b;
    }
    /* This is called when an 'f' is read from s */
    private static double readFloat(InputStream s) throws FdecException, IOException {
        byte[] buf = new byte[16];
        int offset = 0;
        long result = 0;

        while(offset < 16) {
            int bytes_read;
            bytes_read = s.read(buf, offset, 16 - offset);
            if(bytes_read == -1)
                throw new FdecException("EOF");
            assert(bytes_read>0);
            offset += bytes_read;
        }
        assert(offset == 16);
        for(int i=0; i<16; i++) {
            long v;
            int ch;
            switch(ch = buf[i]) {
                case '0':
                case '1': case '2': case '3':
                case '4': case '5': case '6':
                case '7': case '8': case '9':
                    v = ch - '0';
                    break;
                case 'A': case 'B': case 'C':
                case 'D': case 'E': case 'F':
                    v = ch - 'A' + 10;
                    break;
                default:
                    throw new FdecException("Invalid float encoding");
            }
            result |= (v<<(4*(15-i)));
        }
        if(result >= 0x7ff0000000000001L && result <= 0x7fffffffffffffffL
                && result != 0x7ff8000000000000L)
            throw new FdecException("Invalid NaN");
        if(result >= 0xfff0000000000001L && result <= 0xffffffffffffffffL)
            throw new FdecException("Invalid NaN");
        return Double.longBitsToDouble(result);
    }

    /** Decode a data structure.
     *
     * For maximum efficiency s should be a buffered input stream if lastVal == false
     *
     * @param s The output stream to decode the object from
     * @param lastVal Whether the object to read is supposed be the last object
     * in the input stream after which an EOF is supposed to be reached.
     * @return The decoded object
     * @throws IOException If an I/O exception occurs
     * @throws FdecException If the data format is invalid or contains
     * unsupported UTF-8 characters, or lastVal == true and the object is not
     * the last in the input stream.
     */

    public static Object fdecode(InputStream s, boolean lastVal) throws FdecException, IOException {
        if(lastVal && !(s instanceof BufferedInputStream))
            s = new BufferedInputStream(s);
        return fdecode(s, false, lastVal);
    }

    /* Do the actual decoding work */
    private static Object fdecode(InputStream s, boolean acceptEnd, boolean lastVal) throws FdecException, IOException {
        int ch = s.read();
        Object o;
        Object result = null;
        switch(ch) {
            case 'd':
                HashMap<String,Object> d = new HashMap<String,Object>();
                String lastKey = null;
                while((o = fdecode(s, true, false)) != null) {
                    try {
                        String key;
                        key = (String)o;
                        if(lastKey != null && key.compareTo(lastKey) <= 0)
                            throw new FdecException("Dict keys must be sorted");
                        if((o = fdecode(s, true, false)) == null)
                            throw new FdecException("Dict keys with no value");
                        d.put(key, o);
                        lastKey = key;
                    }
                    catch(ClassCastException ex) {
                        throw new FdecException("Dict keys must be strings");
                    }
                }
                result = d;
                break;
            case 'l':
                ArrayList<Object> l = new ArrayList<Object>();
                while((o = fdecode(s, true, false)) != null)
                    l.add(o);
                result = l;
                break;
            case '0':
            case '1': case '2': case '3':
            case '4': case '5': case '6':
            case '7': case '8': case '9':
                result = readStringRemainder(s, ch - '0');
                break;
            case 'i':
                result = readIntRemainder(s);
                break;
            case 'f':
                result = readFloat(s);
                break;
            case 'e':
                if(acceptEnd) {
                    result = null;
                    break;
                }
                throw new FdecException("end");
            case -1:
                throw new FdecException("EOF");
            default:
                throw new FdecException("Unknown data format");
        }
        if(lastVal && s.read() != -1)
            throw new FdecException("Junk");
        return result;
    }


    /** Unit test */
    public static void main(String[] args) {
        Random r = new Random();
        try {
            try {
                for(int i = -5000; i<5000; i++) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    fencLong(i,stream);
                    InputStream is = new ByteArrayInputStream(stream.toByteArray());
                    int i2 = (Integer)fdecode(is,true);

                    assert(i == i2);
                }
            }
            catch(FdecException ex) {
                ex.printStackTrace();
                assert(false);
            }
            for(int i=0; i<50000; i++) {
                int len;
                char[] chs;
                len = r.nextInt(100);
                chs = new char[len];
                for(int j=0; j<len; j++) {
                    chs[j] = (char)r.nextInt(10000);
                }
                try {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    String s = new String(chs);
                    fencString(s,stream);
                    InputStream is = new ByteArrayInputStream(stream.toByteArray());
                    assert(((String)fdecode(is,true)).equals(s));
                }
                catch (FencException ex) {
                    System.out.println("char coding err"); /* won't be thrown for other reasons */
                }
                catch (FdecException ex) {
                    ex.printStackTrace();
                    assert(false);
                }
            }
            //boolean ok = true;
            String[] invalidEncodings = {"i-0e", "i01e", "i005e", "i00e",
                "i23-e", "i0-e", "i2-3e", "i0-3e", "i+5e", "01:a", "00:",
                "2:aaa", "2:a", "l", "a", "la", "di1e0:e", "d0:e",
                "d1:a1:b1:ce", "d1:bi0e1:ai0ee", "d1:ai0e1:ai0ee",
                "f7ff8000000000000", "f7FF8000000000001",
                "fFFF0000000000001", "fFFFFFFFFFFFFFFFF"};

            for(String invalidEncoding : invalidEncodings) {
                boolean ok = true;
                try {
                    fdecode(new ByteArrayInputStream((invalidEncoding).getBytes()),true);
                }
                catch(FdecException ex) {
                    ok = false;
                }
                assert(!ok);
            }

            String[] validEncodings = {"li1ei2e5:abcdee", "i0e", "i-1e", "i1e", "i15e",
                "0:", "1:a", "2:Ab", "f1234567890123456",
                "f0234567890123456", "f0034567890123456", "f0004567890123456",
                "f0000567890123456", "f0000067890123456", "f0000007890123456",
                "f0000000000000000", "d2:aali3ei4ei5ee1:bi1e1:cd1:ai1e1:bi2eee"};


            for(String validEncoding : validEncodings) {
                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] ib = validEncoding.getBytes();
                    ByteArrayInputStream in = new ByteArrayInputStream(ib);
                    Object o = fdecode(in,true);
                    fencode(o, out);
                    byte[] ob = out.toByteArray();
                    assert(ib.length == ob.length);
                    for(int i=0; i<ib.length; i++) {
                        assert(ib[i] == ob[i]);
                    }
                }
                catch(FException ex) {
                    assert(false);
                }
            }

            


            try {
                /* seems to work */
                ArrayList<?> l;
                l = ((ArrayList<?>)fdecode(new ByteArrayInputStream(("li1ei2e5:abcdee").getBytes()),true));
                assert(l.get(0).equals(1));
                assert(l.get(1).equals(2));
                assert(l.get(2).equals("abcde"));

                double d;

                d = (Double)fdecode(new ByteArrayInputStream(("f7FF8000000000000").getBytes()),true);
                assert(Double.isNaN(d));

                /* seems to work */
                Map<?,?> m;
                Set<Object> s1 = new HashSet<Object>();
                Set<Object> s2 = new HashSet<Object>();
                s1.add("aa");
                s1.add("b");
                s1.add("c");
                s2.add("a");
                s2.add("b");
                m = ((Map<?,?>)fdecode(new ByteArrayInputStream(("d2:aali3ei4ei5ee1:bi1e1:cd1:ai1e1:bi2eee").getBytes()),true));
                for(Object o: new TreeSet<Object>(m.keySet()))
                    assert(s1.contains(o));
                assert(m.get("b").equals(1));
                ArrayList<?> l2 = (ArrayList<?>)m.get("aa");
                assert(l2.size() == 3);
                assert(l2.get(0).equals(3));
                assert(l2.get(1).equals(4));
                assert(l2.get(2).equals(5));
                m = (Map<?,?>)m.get("c");
                for(Object o: new TreeSet<Object>(m.keySet()))
                    assert(s2.contains(o));
                assert(m.get("a").equals(1));
                assert(m.get("b").equals(2));
            }
            catch(FdecException ex) {
                ex.printStackTrace();
                assert(false);
            }
        }
        catch(IOException ex) {
            ex.printStackTrace();
            assert(false);
        }
    }
}
