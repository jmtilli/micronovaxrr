import java.io.*;
import fi.iki.jmtilli.javaxmlfrag.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import java.util.*;


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
        public XRRData(double[][] arrays) {
            this.arrays = arrays;
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
            double time;
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
            commonCountingTime = dataPoints.getNotNull("commonCountingTime");
            unit = commonCountingTime.getAttrStringNotNull("unit");
            if (!unit.equals("seconds"))
            {
                throw new XRRImportException();
            }
            time = dataPoints.getDoubleNotNull("commonCountingTime");
            intensities = dataPoints.getNotNull("intensities");
            unit = intensities.getAttrStringNotNull("unit");
            if (!unit.equals("counts"))
            {
                throw new XRRImportException();
            }
            counts = dataPoints.getStringNotNull("intensities").split(" ", 0);
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
            for (int i = 0; i < meas.length; i++)
            {
                meas[i] = Double.parseDouble(counts[i])/time;
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
    public static XRRData asciiImport(InputStream is) throws XRRImportException, IOException {
        ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
        double[][] arrays;
        int cols = -1;
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            while((line = r.readLine()) != null) {
                StringTokenizer t = new StringTokenizer(line);
                int curcols = 0;
                ArrayList<Double> list = new ArrayList<Double>();
                while (t.hasMoreTokens())
                {
                    String s = t.nextToken();
                    double d = Double.parseDouble(s);
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
            arrays[i] = new double[data.size()];
            for (int j = 0; j < data.size(); j++)
            {
                arrays[i][j] = data.get(j).get(i);
            }
        }
        return new XRRData(arrays);
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
        bs.mark(16);
        bs.read(header, 0, 10);
        bs.reset();
        if (new String(header).equals("HR-XRDScan"))
        {
            return X00Import(bs);
        }
        bs.mark(16*1024*1024);
        data = XRDMLImport(bs);
        if (data == null)
        {
            bs.reset();
            return asciiImport(bs);
        }
        return data;
    }
};
