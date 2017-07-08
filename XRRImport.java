import java.io.*;
import fi.iki.jmtilli.javaxmlfrag.*;
import javax.xml.parsers.*;
import org.xml.sax.*;


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
        public final double[] alpha_0, meas;
        public XRRData(double[] alpha_0, double[] meas) {
            this.alpha_0 = alpha_0;
            this.meas = meas;
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
        return new XRRData(alpha_0, meas);
    }
    public static XRRData XRDMLImport(InputStream s) throws XRRImportException, IOException
    {
        try {
            DocumentFragment doc_frag =
                DocumentFragmentHandler.parseWhole(s);
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
            return new XRRData(alpha_0, meas);
        }
        catch(ParserConfigurationException ex)
        {
            throw new XRRImportException();
        }
        catch(NumberFormatException ex)
        {
            throw new XRRImportException();
        }
        catch(SAXException ex)
        {
            throw new XRRImportException();
        }
        catch(XMLException ex)
        {
            throw new XRRImportException();
        }
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
        byte[] header = new byte[10];
        bs.mark(16);
        bs.read(header, 0, 10);
        bs.reset();
        if (new String(header).equals("HR-XRDScan"))
        {
            return X00Import(bs);
        }
        return XRDMLImport(bs);
    }
};
