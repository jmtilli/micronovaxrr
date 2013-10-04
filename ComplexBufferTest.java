import java.util.*;
public class ComplexBufferTest {
    /* not used outside of this file */
    static class TestLookup implements LookupTable {
        private static final Map<String,Element> table;
        static {
            final Map<String,Element> table2 = new HashMap<String,Element>();
            table2.put("Al",new Element(13+0.21, 0.2416, 26.982));
            table2.put("Si",new Element(14+0.26, 0.3249, 28.086));
            table2.put("O",new Element(8+0.052, 3.3705e-2, 15.999));
            table = Collections.unmodifiableMap(table2);
        }
        public Element lookup(String name, double lambda) throws ElementNotFound {
            final double Cu_K_alpha = 1.5405600e-10; /* Cu K_alpha */
            Element e;
            if(Math.abs(lambda - Cu_K_alpha) > 0.001e-10)
                throw new ElementNotFound("Only Cu K_alpha wavelength is supported");
            e = table.get(name);
            if(e == null)
                throw new ElementNotFound("Element "+name+" not found");
            return e;
        }
    }
    /** regression testing */
    public static void main(String[] args) {
        double[] alpha0 = new double[2000];
        double[] alpha0rad = new double[2000];
        LookupTable table = new TestLookup();
        final double lambda = 1.5405600e-10; // Cu K_alpha
        LayerStack[] layerStacks = new LayerStack[2];

        for(int i=0; i<alpha0.length; i++)
        {
            alpha0[i] = 5.0*i/alpha0.length;
            alpha0rad[i] = alpha0[i]*Math.PI/180;
        }

        GraphData data = new GraphData(alpha0, null, null);

        try {
            LayerStack layers = new LayerStack(lambda,table);
            layers.add(new Layer("Substrate", new FitValue(0,0,0),
                       new FitValue(2.26e3,2.33e3,2.4e3), new FitValue(0,3e-9,3e-9),
                       new ChemicalFormula("Si"),new ChemicalFormula("Si"),0,table,lambda));
            layers.add(new Layer("Native oxide", new FitValue(0e-9,7e-9,8e-9),
                       new FitValue(1e3,2.1e3,3e3), new FitValue(0,2e-9,2e-9),
                       new ChemicalFormula("Si"),new ChemicalFormula("O"),2.0/3,table,lambda));
            layers.add(new Layer("Thin film", new FitValue(40e-9,55e-9,70e-9),
                       new FitValue(1e3,3.4e3,4e3), new FitValue(0,1e-9,1e-9),
                       new ChemicalFormula("Al"),new ChemicalFormula("O"),3/5.0,table,lambda));
            for (int i = 0; i < 10000; i++)
            {
                XRRSimul.simulateComplexBuffer(alpha0rad, layers);
            }
        }
        catch(ChemicalFormulaException ex) {
            throw new RuntimeException("Doesn't get thrown");
        }
        catch(ElementNotFound ex) {
            throw new RuntimeException("Doesn't get thrown");
        }
    }
}
