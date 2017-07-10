import java.io.*;
import fi.iki.jmtilli.javaxmlfrag.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import java.util.*;
import java.util.zip.*;


/** Measurement importing code.
 *
 * <p>
 *
 * Imports measurements exported from PANalytical's software
 */

public class XRRImport {
    private XRRImport() {}
    /** The imported data */
    public static class XRRData {
        public final double[][] arrays;
        public final boolean[] valid;
        public boolean isTwoTheta;
        public XRRData(double[][] arrays, boolean isTwoTheta) {
            this.isTwoTheta = isTwoTheta;
            this.arrays = arrays;
            this.valid = new boolean[arrays.length];
            for (int i = 0; i < arrays.length; i++)
            {
                this.valid[i] = (arrays[i] != null);
            }
        }
        public XRRData(double[][] arrays) {
            this(arrays, false);
        }
    };
    /** Imports measurement file from an InputStream.
     *
     * @param s the stream to import the measurement from
     * @return the imported data points
     * @throws IOException if an I/O error occurs
     * @throws XRRImportException if the file format is invalid
     * */
    public static XRRData X00Import(InputStream s) throws XRRImportException, IOException {
        double[] alpha_0;
        double[] meas;
        Boolean is_2theta_omega = null;
        try {
            Double firstAngle = null, stepWidth = null;
            Integer nrOfData = null;
            InputStreamReader rr = new InputStreamReader(s);
            BufferedReader r = new BufferedReader(rr);

            String line = r.readLine();
            if(line == null || !line.equals("HR-XRDScan"))
                throw new XRRImportException();
            for(;;) {
                line = r.readLine();
                if(line == null)
                    throw new XRRImportException();
                if(line.length() > 10 && line.substring(0,10).equals("ScanAxis, ")) {
                    String axis;
                    if(is_2theta_omega != null)
                        throw new XRRImportException();
                    axis = line.substring(10);
                    if (axis.equals("2Theta/Omega"))
                    {
                      is_2theta_omega = Boolean.TRUE;
                    }
                    else if (axis.equals("Omega/2Theta"))
                    {
                      is_2theta_omega = Boolean.FALSE;
                    }
                    else
                    {
                      throw new XRRImportException();
                    }
                }
                if(line.length() > 12 && line.substring(0,12).equals("FirstAngle, ")) {
                    if(firstAngle != null)
                        throw new XRRImportException();
                    firstAngle = Double.valueOf(line.substring(12));
                }
                if(line.length() > 11 && line.substring(0,11).equals("StepWidth, ")) {
                    if(stepWidth != null)
                        throw new XRRImportException();
                    stepWidth = Double.valueOf(line.substring(11));
                }
                if(line.length() > 10 && line.substring(0,10).equals("NrOfData, ")) {
                    if(nrOfData != null)
                        throw new XRRImportException();
                    nrOfData = Integer.valueOf(line.substring(10));
                }
                if(line.equals("ScanData"))
                    break;
            }
            if(stepWidth == null || firstAngle == null || nrOfData == null)
                throw new XRRImportException();

            int size = nrOfData.intValue();
            double alpha = firstAngle.doubleValue();
            double width = stepWidth.doubleValue();
            if (is_2theta_omega == null)
              throw new XRRImportException();
            if (is_2theta_omega)
            {
              alpha /= 2;
              width /= 2;
            }

            alpha_0 = new double[size];
            meas = new double[size];

            for(int i=0; i<size; i++) {
                line = r.readLine();
                if(line == null)
                    throw new XRRImportException();
                alpha_0[i] = alpha + i*width;
                meas[i] = Double.parseDouble(line);
            }
            if(r.readLine() != null)
                throw new XRRImportException();
        }
        catch(NumberFormatException ex) {
            throw new XRRImportException();
        }
        return new XRRData(new double[][]{alpha_0, meas});
    }
    public static XRRData XRDMLImport(InputStream s) throws XRRImportException, IOException
    {
        DocumentFragment doc_frag;
        try
        {
            doc_frag = DocumentFragmentHandler.parseWhole(s);
        }
        catch(ParserConfigurationException ex)
        {
            return null;
        }
        catch(SAXException ex)
        {
            return null;
        }
        try {
            DocumentFragment measurement;
            DocumentFragment scan;
            DocumentFragment dataPoints;
            DocumentFragment commonCountingTime;
            DocumentFragment intensities;
            String axis, unit;
            String[] counts;
            double time = 1.0;
            double[] times = null;
            double start = 0, end = 90, step;
            boolean valid = false;
            double[] alpha_0, meas;
            doc_frag.assertTag("xrdMeasurements");
            measurement = doc_frag.getNotNull("xrdMeasurement");
            if (!measurement.getAttrStringNotNull("measurementType").equals("Scan"))
            {
                throw new XRRImportException();
            }
            if (!measurement.getAttrStringNotNull("status").equals("Completed"))
            {
                throw new XRRImportException();
            }
            scan = measurement.getNotNull("scan");
            axis = scan.getAttrStringNotNull("scanAxis");
            if (!axis.equals("Omega-2Theta") &&
                !axis.equals("2Theta-Omega"))
            {
                throw new XRRImportException();
            }
            dataPoints = scan.getNotNull("dataPoints");
            commonCountingTime = dataPoints.get("commonCountingTime");
            if (commonCountingTime == null)
            {
                String[] strtimes;
                DocumentFragment countingTimes;
                countingTimes = dataPoints.getNotNull("countingTimes");
                unit = countingTimes.getAttrStringNotNull("unit");
                if (!unit.equals("seconds"))
                {
                    throw new XRRImportException();
                }
                strtimes = dataPoints.getStringNotNull("countingTimes").trim().split("[ \t\r\n]+", 0);
                times = new double[strtimes.length];
                for (int i = 0; i < times.length; i++)
                {
                    times[i] = Double.parseDouble(strtimes[i]);
                }
            }
            else
            {
                unit = commonCountingTime.getAttrStringNotNull("unit");
                if (!unit.equals("seconds"))
                {
                    throw new XRRImportException();
                }
                time = dataPoints.getDoubleNotNull("commonCountingTime");
            }
            intensities = dataPoints.getNotNull("intensities");
            unit = intensities.getAttrStringNotNull("unit");
            if (!unit.equals("counts"))
            {
                throw new XRRImportException();
            }
            counts = dataPoints.getStringNotNull("intensities").trim().split("[ \t\r\n]+", 0);
            for (DocumentFragment positions: dataPoints.getMulti("positions"))
            {
                if (!positions.getAttrStringNotNull("axis").equals("2Theta"))
                {
                    continue;
                }
                if (!positions.getAttrStringNotNull("unit").equals("deg"))
                {
                    throw new XRRImportException();
                }
                start = positions.getDoubleNotNull("startPosition")/2.0;
                end = positions.getDoubleNotNull("endPosition")/2.0;
                valid = true;
            }
            if (!valid || counts.length <= 1)
            {
                throw new XRRImportException();
            }
            step = (end-start)/(counts.length-1);
            meas = new double[counts.length];
            alpha_0 = new double[counts.length];
            if (times != null && times.length != counts.length)
            {
                throw new XRRImportException();
            }
            for (int i = 0; i < meas.length; i++)
            {
                if (times == null)
                {
                    meas[i] = Double.parseDouble(counts[i])/time;
                }
                else
                {
                    meas[i] = Double.parseDouble(counts[i])/times[i];
                }
                alpha_0[i] = start + i*step;
            }
            return new XRRData(new double[][]{alpha_0, meas});
        }
        catch(NumberFormatException ex)
        {
            throw new XRRImportException();
        }
        catch(XMLException ex)
        {
            throw new XRRImportException();
        }
    }
    public static XRRData UXDImport(BufferedReader in) throws XRRImportException, IOException {
        String line;
        for (;;)
        {
            in.mark(256*1024);
            line = in.readLine();
            if (line == null)
            {
                throw new XRRImportException();
            }
            if (!line.trim().startsWith(";") && !line.trim().startsWith("_"))
            {
                in.reset();
                break;
            }
        }
        XRRData dat = asciiImportReader(in);
        dat.isTwoTheta = true;
        /*
        for (int i = 0; i < dat.arrays[0].length; i++)
        {
            dat.arrays[0][i] /= 2.0;
        }
        */
        return dat;
    }
    public static XRRData rigakuImport(InputStream is) throws XRRImportException, IOException {
        double[] alpha_0 = null, meas = null;
        double start = Double.NaN;
        double step = Double.NaN;
        int count = -1;
        boolean begun = false;
        boolean ended = false;
        int i = 0;
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        try
        {
            String line;
            while((line = r.readLine()) != null)
            {
                line = line.replaceAll("#.*$", "");
                if (line.trim().equals(""))
                {
                    continue;
                }
                if (line.startsWith("*TYPE"))
                {
                    String[] vals = line.split("=", 2);
                    if (vals.length != 2)
                    {
                        throw new XRRImportException();
                    }
                }
                else if (line.startsWith("*START"))
                {
                    String[] vals = line.split("=", 2);
                    if (vals.length != 2)
                    {
                        throw new XRRImportException();
                    }
                    start = Double.parseDouble(vals[1].trim());
                }
                else if (line.startsWith("*STEP"))
                {
                    String[] vals = line.split("=", 2);
                    if (vals.length != 2)
                    {
                        throw new XRRImportException();
                    }
                    step = Double.parseDouble(vals[1].trim());
                }
                else if (line.startsWith("*GROUP_COUNT"))
                {
                    String[] vals = line.split("=", 2);
                    if (vals.length != 2)
                    {
                        throw new XRRImportException();
                    }
                    if (Integer.parseInt(vals[1].trim()) != 1)
                    {
                        throw new XRRImportException();
                    }
                }
                else if (line.startsWith("*GROUP"))
                {
                    String[] vals = line.split("=", 2);
                    if (vals.length != 2)
                    {
                        throw new XRRImportException();
                    }
                    if (Integer.parseInt(vals[1].trim()) != 0)
                    {
                        throw new XRRImportException();
                    }
                }
                else if (line.startsWith("*COUNT"))
                {
                    String[] vals = line.split("=", 2);
                    if (vals.length != 2)
                    {
                        throw new XRRImportException();
                    }
                    count = Integer.parseInt(vals[1].trim());
                    if (count < 0)
                    {
                        throw new XRRImportException();
                    }
                    alpha_0 = new double[count];
                    meas = new double[count];
                }
                else if (line.startsWith("*BEGIN"))
                {
                    begun = true;
                }
                else if (line.startsWith("*END"))
                {
                    ended = true;
                }
                else if (line.startsWith("*EOF"))
                {
                    ended = true;
                }
                else if (line.startsWith("*"))
                {
                }
                else
                {
                    String[] vals = line.split(",");
                    if (!begun || ended)
                    {
                        throw new XRRImportException();
                    }
                    if (Double.isNaN(start) || Double.isNaN(step) ||
                       meas == null || alpha_0 == null)
                    {
                        throw new XRRImportException();
                    }
                    for (String val: vals)
                    {
                        double d = Double.parseDouble(val.trim());
                        meas[i] = d;
                        alpha_0[i] = (start + i*step);
                        i++;
                    }
                }
            }
        }
        catch(NumberFormatException ex) {
            throw new XRRImportException();
        }
        catch(IndexOutOfBoundsException ex) {
            throw new XRRImportException();
        }
        return new XRRData(new double[][]{alpha_0, meas}, true);
    }
    public static XRRData asciiImportReader(BufferedReader r) throws XRRImportException, IOException {
        ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
        double[][] arrays;
        int cols = -1;
        int validCols = 0;
        try {
            String line;
            while((line = r.readLine()) != null) {
                line = line.trim().replaceAll("[#%].*$", "").trim();
                if (line.equals(""))
                {
                    continue;
                }
                StringTokenizer t = new StringTokenizer(line,  " \t\n\r\f;|:");
                int curcols = 0;
                ArrayList<Double> list = new ArrayList<Double>();
                while (t.hasMoreTokens())
                {
                    String s = t.nextToken().replace(",",".");
                    double d;
                    try
                    {
                        d = Double.parseDouble(s);
                    }
                    catch (NumberFormatException ex)
                    {
                        if (curcols == 0)
                        {
                            throw ex;
                        }
                        else
                        {
                            curcols++;
                            list.add(null);
                            continue;
                        }
                    }
                    if (curcols == 0 && (d < 0 || d > 90))
                    {
                        throw new XRRImportException();
                    }
                    curcols++;
                    list.add(d);
                }
                if ((cols >= 0 && cols != curcols) || curcols < 2)
                {
                    throw new XRRImportException();
                }
                if (cols < 0)
                {
                    cols = curcols;
                }
                data.add(list);
            }
        }
        catch(NumberFormatException ex) {
            throw new XRRImportException();
        }
        catch(NoSuchElementException ex) {
            throw new XRRImportException();
        }
        arrays = new double[cols][];
        for (int i = 0; i < cols; i++)
        {
            boolean ok = true;
            for (int j = 0; j < data.size(); j++)
            {
                if (data.get(j).get(i) == null)
                {
                    ok = false;
                    break;
                }
            }
            if (ok)
            {
                validCols++;
                arrays[i] = new double[data.size()];
                for (int j = 0; j < data.size(); j++)
                {
                    arrays[i][j] = data.get(j).get(i);
                }
            }
            else
            {
                arrays[i] = null;
            }
        }
        if (validCols < 2 || arrays[0] == null)
        {
            throw new XRRImportException();
        }
        return new XRRData(arrays);
    }
    public static XRRData asciiImport(InputStream is) throws XRRImportException, IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        return asciiImportReader(r);
    }
    private static void readWhiteSpace(BufferedInputStream bs) throws IOException
    {
        byte[] array = new byte[1024];
        int j = 0;
outer:
        while (true)
        {
            bs.mark(2048);
            bs.read(array, 0, 1024);
            for (int i = 0; i < 1024; i++)
            {
                if (array[i] != '\t' && array[i] != '\n' &&
                    array[i] != '\r' && array[i] != ' ')
                {
                    bs.reset();
                    j = i;
                    break outer;
                }
            }
        }
        bs.read(array, 0, j);
    }
    public static short readLe16(InputStream s) throws IOException
    {
        byte[] header = new byte[2];
        if (s.read(header, 0, 2) != 2)
        {
            throw new IOException("EOF");
        }
        return (short)(((short)header[0]&0xFF) | (((short)header[1]&0xFF)<<8));
    }
    public static int readLe32(InputStream s) throws IOException
    {
        byte[] header = new byte[4];
        if (s.read(header, 0, 4) != 4)
        {
            throw new IOException("EOF");
        }
        return (header[0]&0xFF) | (((int)header[1]&0xFF)<<8) |
               (((int)header[2]&0xFF)<<16) | (((int)header[3]&0xFF)<<24);
    }
    public static long readLe64(InputStream s) throws IOException
    {
        byte[] header = new byte[8];
        if (s.read(header, 0, 8) != 8)
        {
            throw new IOException("EOF");
        }
        return (header[0]&0xFF) | (((long)header[1]&0xFF)<<8) |
               (((long)header[2]&0xFF)<<16) | (((long)header[3]&0xFF)<<24) |
               (((long)header[4]&0xFF)<<32) | (((long)header[5]&0xFF)<<40) |
               (((long)header[6]&0xFF)<<48) | (((long)header[7]&0xFF)<<56);
    }
    public static float readLeFloat(InputStream s) throws IOException
    {
        int bits = readLe32(s);
        return Float.intBitsToFloat(bits);
    }
    public static double readLeDouble(InputStream s) throws IOException
    {
        long bits = readLe64(s);
        return Double.longBitsToDouble(bits);
    }
    public static XRRData brukerImport1(InputStream s)
        throws XRRImportException, IOException
    {
        byte[] buf = new byte[1024];
        double[] alpha_0, meas;
        if (s.read(buf, 0, 4) != 4)
        {
            throw new XRRImportException();
        }
        if (buf[0] != 'R' || buf[1] != 'A' || buf[2] != 'W' || buf[3] != ' ')
        {
            throw new XRRImportException();
        }
        int steps = readLe32(s);
        alpha_0 = new double[steps];
        meas = new double[steps];
        float timePerStep = readLeFloat(s);
        float xStep = readLeFloat(s);
        s.skip(8);
        float xStart = readLeFloat(s);
        float thetaStart = readLeFloat(s);
        if (thetaStart == -1e6)
        {
            throw new XRRImportException();
        }
        s.skip(4);
        s.skip(4);
        s.skip(32);
        s.skip(4);
        s.skip(4);
        s.skip(72);
        if (readLe32(s) != 0)
        {
            throw new XRRImportException();
        }
        for (int i = 0; i < steps; i++)
        {
            alpha_0[i] = (xStart + xStep*i);
            meas[i] = readLeFloat(s);
        }
        return new XRRData(new double[][]{alpha_0, meas}, true);
    }
        
    public static XRRData brukerImport2(InputStream s)
        throws XRRImportException, IOException
    {
        byte[] buf = new byte[1024];
        double[] alpha_0, meas;
        if (s.read(buf, 0, 4) != 4)
        {
            throw new XRRImportException();
        }
        if (buf[0] != 'R' || buf[1] != 'A' || buf[2] != 'W' || buf[3] != '2')
        {
            throw new XRRImportException();
        }
        if (readLe16(s) != 1) // range count
        {
            throw new XRRImportException();
        }
        s.skip(162);
        s.skip(22);
        s.skip(5*4);
        s.skip(4);
        s.skip(42);
        short curHeaderLen = readLe16(s);
        if (curHeaderLen < 48)
        {
            throw new XRRImportException();
        }
        short steps = readLe16(s);
        alpha_0 = new double[steps];
        meas = new double[steps];
        s.skip(4);
        float timePerStep = readLeFloat(s);
        float xStep = readLeFloat(s);
        float xStart = readLeFloat(s);
        s.skip(28);
        s.skip(curHeaderLen - 48);
        
        for (int i = 0; i < steps; i++)
        {
            alpha_0[i] = (xStart + xStep*i);
            meas[i] = readLeFloat(s);
        }
        return new XRRData(new double[][]{alpha_0, meas}, true);
    }

    public static XRRData rdImport(InputStream s)
        throws XRRImportException, IOException
    {
        byte[] header = new byte[4];
        double[] alpha_0, meas;
        int version;
        s.read(header);
        if (header[0] != 'V')
        {
            throw new XRRImportException();
        }
        version = header[1] - '0';
        if (version != 3 && version != 5)
        {
            throw new XRRImportException();
        }
        s.skip(80);
        s.skip(3);
        s.skip(138 - 84 - 3);
        s.skip(8);
        s.skip(20);
        s.skip(214 - 138 - 8 - 20);
        double xStep = readLeDouble(s);
        double xStart = readLeDouble(s);
        double xEnd = readLeDouble(s);
        // XXX good to add 0.5?
        int count = (int)((xEnd - xStart) / xStep + 1.0 + 0.5);
        alpha_0 = new double[count];
        meas = new double[count];
        if (version == 3)
        {
            s.skip(250 - 214 - 8*3);
        }
        else
        {
            s.skip(810 - 214 - 8*3);
        }
        for (int i = 0; i < count; i++)
        {
            int packedY = readLe16(s);
            double y = Math.floor(0.01*packedY*packedY);
            alpha_0[i] = xStart + i*xStep;
            meas[i] = y;
        }
        return new XRRData(new double[][]{alpha_0, meas}, true);
    }
        
    public static XRRData brukerImport101(InputStream s)
        throws XRRImportException, IOException
    {
        byte[] buf = new byte[1024];
        double[] alpha_0, meas;
        if (s.read(buf, 0, 8) != 8)
        {
            throw new XRRImportException();
        }
        if (buf[0] != 'R' || buf[1] != 'A' || buf[2] != 'W' || buf[3] != '1' ||
            buf[4] != '.' || buf[5] != '0' || buf[6] != '1' || buf[7] != 0)
        {
            throw new XRRImportException();
        }
        if (readLe32(s) != 1) // 1 means successful
        {
            throw new XRRImportException();
        }
        if (readLe32(s) != 1) // 1 means range count is 1
        {
            throw new XRRImportException();
        }
        s.skip(10 + 10 + 72 + 218 + 60 + 160);
        s.skip(2 + 4*15);
        s.skip(4); // anode material
        s.skip(5*8); // alpha/beta doubles for radiation
        s.skip(4*3);
        s.skip(43 + 1);
        s.skip(4);
        // range header begins here
        if (readLe32(s) != 304) // header len
        {
            throw new XRRImportException();
        }
        int steps = readLe32(s);
        alpha_0 = new double[steps];
        meas = new double[steps];
        double startTheta = readLeDouble(s);
        double startTwoTheta = readLeDouble(s);
        s.skip(6*8);
        s.skip(6+2);
        s.skip(8);
        s.skip(6+2);
        s.skip(4);
        s.skip(4*5);
        s.skip(8);
        s.skip(4*2);
        s.skip(5+3);
        s.skip(8*3);
        s.skip(4*2);
        double stepSize = readLeDouble(s);
        s.skip(8);
        float timePerStep = readLeFloat(s);
        s.skip(11*4);
        s.skip(8);
        s.skip(4*2);
        int supplementaryHeaderSize = readLe32(s);
        s.skip(4*3);
        s.skip(8+24);
        s.skip(supplementaryHeaderSize);
        for (int i = 0; i < steps; i++)
        {
            alpha_0[i] = (startTwoTheta + stepSize*i);
            meas[i] = readLeFloat(s);
        }
        return new XRRData(new double[][]{alpha_0, meas}, true);
    }
    /** Imports measurement file from an InputStream.
     *
     * @param s the stream to import the measurement from
     * @return the imported data points
     * @throws IOException if an I/O error occurs
     * @throws XRRImportException if the file format is invalid
     * */
    public static XRRData XRRImport(InputStream s) throws XRRImportException, IOException {
        BufferedInputStream bs = new BufferedInputStream(s);
        XRRData data;
        byte[] header = new byte[10];
        int ch;
        bs.mark(16);
        bs.read(header, 0, 10);
        bs.reset();
        if (header[0] == (byte) (GZIPInputStream.GZIP_MAGIC&0xFF) &&
            header[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8))
        {
            GZIPInputStream gz = new GZIPInputStream(bs);
            bs = new BufferedInputStream(gz);
            bs.mark(16);
            bs.read(header, 0, 10);
            bs.reset();
        }
        else if (header[0] == 'P' && header[1] == 'K' &&
                 header[2] == 3 && header[3] == 4)
        {
            ZipOneInputStream gz = new ZipOneInputStream(bs);
            bs = new BufferedInputStream(gz);
            bs.mark(16);
            bs.read(header, 0, 10);
            bs.reset();
        }
        if (header[0] == 'R' && header[1] == 'A' &&
            header[2] == 'W' && header[3] == '1')
        {
            return brukerImport101(bs);
        }
        if (header[0] == 'R' && header[1] == 'A' &&
            header[2] == 'W' && header[3] == '2')
        {
            return brukerImport2(bs);
        }
        if (header[0] == 'R' && header[1] == 'A' &&
            header[2] == 'W' && header[3] == ' ')
        {
            return brukerImport1(bs);
        }
        if (header[0] == 'V' && header[1] == '3' &&
            header[2] == 'R' && header[3] == 'D')
        {
            return rdImport(bs);
        }
        if (header[0] == 'V' && header[1] == '5' &&
            header[2] == 'R' && header[3] == 'D')
        {
            return rdImport(bs);
        }
        if (header[0] == '*' && header[1] == 'T' &&
            header[2] == 'Y' && header[3] == 'P' && header[4] == 'E')
        {
            return rigakuImport(bs);
        }
        if (new String(header).startsWith("HR-XRDScan"))
        {
            return X00Import(bs);
        }
        readWhiteSpace(bs);
        bs.mark(16);
        ch = bs.read();
        bs.reset();
        if (ch == '_')
        {
            String line;
            BufferedReader r = new BufferedReader(new InputStreamReader(bs));
            line = r.readLine();
            if (line.trim().startsWith("_FILEVERSION"))
            {
                return UXDImport(r);
            }
            throw new XRRImportException();
        }
        if (ch == ';')
        {
            BufferedReader r = new BufferedReader(new InputStreamReader(bs));
            String line;
            r.readLine();
            for (;;)
            {
                line = r.readLine();
                if (line == null)
                {
                    throw new XRRImportException();
                }
                if (!line.trim().startsWith(";"))
                {
                    break;
                }
            }
            if (line.trim().startsWith("_FILEVERSION"))
            {
                return UXDImport(r);
            }
            return asciiImportReader(r);
        }
        if (ch == '<')
        {
            data = XRDMLImport(bs);
            if (data == null)
            {
                throw new XRRImportException();
            }
            return data;
        }
        return asciiImport(bs);
    }
    public static void main(String[] args) throws Throwable
    {
        XRRData dat = XRRImport(new FileInputStream(args[0]));
        for (int i = 0; i < dat.arrays[0].length; i++)
        {
            for (int j = 0; j < dat.arrays.length; j++)
            {
                System.out.print(dat.arrays[j][i] + " ");
            }
            System.out.println();
        }
    }
};
