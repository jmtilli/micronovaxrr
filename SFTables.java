import java.util.*;
import java.io.*;


/** Scattering factor database.
 *
 * <p>
 *
 * This class imports atomic masses and scattering factors from ASCII files.
 * Atomic masses are imported from a single text file. Scattering factors are
 * loaded from a directory for every element for which an atomic mass is
 * specified in the atomic mass file. Information for an element is only
 * available if both the atomic mass and the scattering factors for the element
 * are available.
 *
 * <p>
 *
 * This class is thread safe since it is immutable. Lookups may be performed
 * simultaneously by multiple threads.
 *
 */

public class SFTables implements LookupTable {

    private class SFTable {
        public final double[] E, f1, f2; /* sorted by E (in eV) */
        public SFTable(double[] E, double[] f1, double[] f2) {
            assert(E.length == f1.length);
            assert(E.length == f2.length);
            this.E = E;
            this.f1 = f1;
            this.f2 = f2;
        }
    }
    private class AtomicMasses {
        public final Map<String,Double> massMap;
        public AtomicMasses(InputStream is) throws IOException, FileFormatException {
            Map<String,Double> tempMap = new HashMap<String,Double>();
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            try {
                String line;
                while((line = r.readLine()) != null) {
                    StringTokenizer t = new StringTokenizer(line);
                    String element = t.nextToken();
                    String As = t.nextToken();
                    double A = Double.parseDouble(As);
                    if(A <= 0)
                        throw new FileFormatException();
                    if(t.hasMoreElements())
                        throw new FileFormatException();
                    tempMap.put(element,A);
                }
                massMap = Collections.unmodifiableMap(tempMap);
            }
            catch(NumberFormatException ex) {
                throw new FileFormatException(ex);
            }
            catch(NoSuchElementException ex) {
                throw new FileFormatException(ex);
            }
        }
    }



    private final Map<String,SFTable> tables;
    private final AtomicMasses masses;

    /** Constructor.
     * <p>
     * Creates the database and loads atomic masses and scattering factors.
     *
     * @param m a text file of atomic masses
     * @param d a directory which contains scattering factor files
     *
     * @throws FileFormatException if the file format of the atomic mass file or a scattering factor is invalid
     * @throws IOException if an I/O error occurs
     *
     */
    public SFTables(File m, File d) throws FileFormatException, IOException {
        tables = Collections.synchronizedMap(new HashMap<String,SFTable>());
        FileInputStream fstr = new FileInputStream(m);
        try {
            masses = new AtomicMasses(fstr);
        }
        finally {
            fstr.close();
        }
        readTables(d);
    }
    private void readTables(File d) throws FileFormatException, IOException {
        for(String s: masses.massMap.keySet()) {
            File f = new File(d,s.toLowerCase()+".nff");
            FileInputStream is = null;
            try {
                is = new FileInputStream(f);
            }
            catch(IOException e) {}
            if(is != null)
            {
                try {
                    read(s, is);
                }
                finally {
                    is.close();
                }
            }
        }
    }
    private class SFData implements Comparable<SFData> {
        public final double E, f1, f2;
        public SFData(double E, double f1, double f2) {
            this.E = E;
            this.f1 = f1;
            this.f2 = f2;
        }
        public int compareTo(SFData d2) {
            if(this.E > d2.E)
                return 1;
            else if(this.E < d2.E)
                return -1;
            else
                return 0;
        }
        public boolean equals(Object d2) {
            try {
                return this.E == ((SFData)d2).E;
            }
            catch(ClassCastException ex) {
                return false;
            }
        }
    };
    private void read(String element, InputStream is) throws IOException, FileFormatException {
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        if(r.readLine() == null)
            throw new FileFormatException();
        try {
            ArrayList<SFData> data;
            String line;
            data = new ArrayList<SFData>();
            while((line = r.readLine()) != null) {
                StringTokenizer t = new StringTokenizer(line);
                String Es = t.nextToken();
                String f1s = t.nextToken();
                String f2s = t.nextToken();
                double E = Double.parseDouble(Es);
                double f1 = Double.parseDouble(f1s);
                double f2 = Double.parseDouble(f2s);
                if(f1 != -9999)
                    data.add(new SFData(E,f1,f2));
                if(t.hasMoreElements())
                    throw new FileFormatException();
            }
            Collections.sort(data);

            double[] Ea, f1a, f2a;
            Ea = new double[data.size()];
            f1a = new double[data.size()];
            f2a = new double[data.size()];
            for(int i=0; i<data.size(); i++) {
                Ea[i] = data.get(i).E;
                f1a[i] = data.get(i).f1;
                f2a[i] = data.get(i).f2;
            }
            tables.put(element, new SFTable(Ea, f1a, f2a));

        }
        catch(NumberFormatException ex) {
            throw new FileFormatException(ex);
        }
        catch(NoSuchElementException ex) {
            throw new FileFormatException(ex);
        }
    }
    /** Search for an element in the table
     *
     * @param name the case-sensitive element symbol
     * @param lambda the wavelength to get the information for
     *
     * @return Atomic mass and scattering factors of the element for the specified wavelength
     *
     * @throws ElementNotFound if the scattering data of the element is not found for the specified wavelength
     */
    public Element lookup(String name, double lambda) throws ElementNotFound {
        final double h = 4.13566743e-15; /* in eV*s */
        final double c = 299792458; /* exact */
        double E = h*c/lambda;
        double A;
        double f1, f2;
        int i;
        SFTable table = tables.get(name);
        Double A2 = masses.massMap.get(name);
        if(table == null || A2 == null)
            throw new ElementNotFound("Element "+name+" not found");
        A = A2;
        for(i=0; i<table.E.length && table.E[i] < E; i++);
        if(i == 0 || i == table.E.length) {
            throw new ElementNotFound("No scattering data for element "+name+" and wavelength "+lambda*1e9 + " nm");
        }
        assert(table.E[i] >= E);
        assert(table.E[i-1] < E);
        double f = (E - table.E[i-1])/(table.E[i] - table.E[i-1]);
        f1 = (1-f)*table.f1[i-1] + f*table.f1[i];
        f2 = (1-f)*table.f2[i-1] + f*table.f2[i];
        return new Element(f1, f2, A); /* A */
    }
}
