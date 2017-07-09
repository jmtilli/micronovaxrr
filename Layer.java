/* Layer */

import java.util.*;
import fi.iki.jmtilli.javaxmlfrag.*;



/** The layer class.
 *
 * <p>
 *
 * The layer class is used to store information about one layer.  A layer has a
 * name and properties. Thickness, density and roughness are stored as a
 * FitValue with minimum, maximum and fitted values.  Chemical composition,
 * which is modeled by a mixture of two compounds, is not fitted.
 *
 * <p>
 *
 * Layer objects are mutable, so access must be synchronized carefully if
 * accessed by multiple threads even if only one thread modifies the object.
 * Objects can be copied by the deepCopy method. Another possible way to
 * implement thread safety is to make a copy of the object for each thread.
 *
 * <p>
 *
 * Layer objects store also wavelength and the lookup table used to find
 * elements. These are needed to allow changing composition without specifying
 * the wavelength and the lookup table each time. Changes to composition will
 * throw an exception if an element is not found in the lookup table for the
 * wavelength.
 *
 * <p>
 *
 * Fencode serialization is implemented by structImport and structExport which
 * convert between a layer object and its fencodeable structure representation.
 *
 * <p>
 *
 * All units are SI units.
 *
 */

public class Layer implements ValueListener {
    private String name;
    private FitValue d, rho, r; /* thickness, density, roughness (in SI units) */

    private double f; /* 0 = only compound1, 1 = only compound2 */
    private ChemicalFormula compound1, compound2;

    private double lambda;
    private LookupTable table;
    private Compound compound;

    private final Set<LayerListener> listeners = new HashSet<LayerListener>();

    private static boolean utilEquals(Object o1, Object o2)
    {
      if (o1 == null)
      {
        return o2 == null;
      }
      if (o2 == null)
      {
        return false;
      }
      return o1.equals(o2);
    }
    /**
       Deep equality comparison for layers.

       This method here does deep equality comparison, ie. it compares the
       contents of the objects.
     */
    public static boolean layerDeepEquals(Layer l1,
                                          Map<FitValue,Integer> l1_numbering,
                                          Layer l2,
                                          Map<FitValue,Integer> l2_numbering)
    {
      if (   l1_numbering.get(l1.d) != l2_numbering.get(l2.d)
          || l1_numbering.get(l1.rho) != l2_numbering.get(l2.rho)
          || l1_numbering.get(l1.r) != l2_numbering.get(l2.r))
      {
          return false;
      }
      if (   !utilEquals(l1.name, l2.name)
          || !utilEquals(l1.d, l2.d)
          || !utilEquals(l1.rho, l2.rho)
          || !utilEquals(l1.r, l2.r)
          || (l1.f != l2.f)
          || (l1.lambda != l2.lambda)
          || (l1.table != l2.table)
          || !utilEquals(l1.compound1, l2.compound1)
          || !utilEquals(l1.compound2, l2.compound2))
      {
        return false;
      }
      return true;

    }

    /** Returns the layer name */
    public String getName() { return this.name; };
    /** Returns thickness */
    public FitValue getThickness() { return this.d; };
    /** Returns the first compound */
    public ChemicalFormula getCompound1() { return this.compound1; };
    /** Returns the second compound */
    public ChemicalFormula getCompound2() { return this.compound2; };
    /** Returns the proportion of second compound */
    public double getF() { return this.f; };
    /** Returns mass density */
    public FitValue getDensity() { return this.rho; };
    /** Returns roughness */
    public FitValue getRoughness() { return this.r; };

    public void addLayerListener(LayerListener listener) {
        listeners.add(listener);
    }
    public void removeLayerListener(LayerListener listener) {
        listeners.remove(listener);
    }

    /** Returns composition as element-&gt;amount -mapping */
    public Map<String,Double> getComposition() {
        Map<String,Double> result = new HashMap<String,Double>();
        compound1.parse(result, 1-f);
        compound2.parse(result, f);
        return result;
    }


    private static FitValue getFitValue(Object o,
                                        Map<Integer, FitValue> fitValueById)
      throws InvalidStructException
    {
        Map<?,?> m;
        if(!(o instanceof Map))
            throw new InvalidStructException();
        m = (Map<?,?>)o;
        if (m.containsKey("fitvalrefid"))
        {
            Object o2;
            o2 = m.get("fitvalrefid");
            if(o2 == null || !(o2 instanceof Integer))
                throw new InvalidStructException();
            int fitvalrefid = (Integer)o2;
            FitValue result = fitValueById.get(fitvalrefid);
            if (result == null)
            {
                throw new NullPointerException();
            }
            return result;
        }
        FitValue result = FitValue.structImport(o);
        if (m.containsKey("fitvalid"))
        {
            Object o2;
            o2 = m.get("fitvalid");
            if(o2 == null || !(o2 instanceof Integer))
                throw new InvalidStructException();
            int fitvalid = (Integer)o2;
            fitValueById.put(fitvalid, result);
        }
        return result;
    }

    /** Imports a layer from its fencodeable structure representation.
     * <p>
     * Lookup table can't be stored to the structure so we need to supply it as a parameter.
     * <p>
     * Wavelength could have been saved to the structure representations
     * of individual Layers, but it is stored only to the structure
     * representation of LayerStack to minimize redundancy.
     *
     * @return a new layer imported from the structure representation
     *
     * @param o the structure representation to import a Layer from
     * @param fitValueById a mapping for FitValue by its id
     * @param table the lookup table
     * @param lambda the wavelength of the layer which is only stored to the structure representation of LayerStack
     * @throws InvalidStructException the structure does not represent a Layer
     * @throws ElementNotFound an element was not found in the lookup table for the given wavelength
     */
    public static Layer structImport(Object o, Map<Integer, FitValue> fitValueById, LookupTable table, double lambda) throws InvalidStructException, ElementNotFound {
        Map<?,?> m;
        Object o2;
        Layer l = new Layer();
        l.table = table;
        l.lambda = lambda;

        if(!(o instanceof Map))
            throw new InvalidStructException();
        m = (Map<?,?>)o;

        o2 = m.get("name");
        if(o2 == null || !(o2 instanceof String))
            throw new InvalidStructException("invalid name");
        l.setName((String)o2);

        o2 = m.get("f");
        if(o2 == null || !(o2 instanceof Double))
            throw new InvalidStructException("invalid f");
        l.f = (Double)o2;

        o2 = m.get("d");
        if(o2 == null)
            throw new InvalidStructException("invalid thickness");
        l.setThicknessObject(getFitValue(o2, fitValueById));

        o2 = m.get("r");
        if(o2 == null)
            throw new InvalidStructException("invalid roughness");
        l.setRoughnessObject(getFitValue(o2, fitValueById));

        o2 = m.get("rho");
        if(o2 == null)
            throw new InvalidStructException("invalid density");
        l.setDensityObject(getFitValue(o2, fitValueById));

        try {
            o2 = m.get("compound1");
            if(o2 == null || !(o2 instanceof String))
                throw new InvalidStructException("invalid compound1");

            String c1 = (String)o2;

            o2 = m.get("compound2");
            if(o2 == null || !(o2 instanceof String))
                throw new InvalidStructException("invalid compound2");
            String c2 = (String)o2;

            l.setCompounds(new ChemicalFormula(c1), new ChemicalFormula(c2));
        }
        catch(ChemicalFormulaException ex) {
            throw new InvalidStructException("invalid chemical formula");
        }
        return l;
    }

    public Map<String, Object> setFitValue(
        Map<FitValue, Integer> fitValueNumbering,
        Set<FitValue> alreadyAdded,
        FitValue val)
    {
        if (fitValueNumbering.containsKey(val))
        {
            Map<String,Object> structure = val.structExport();
            if (alreadyAdded.contains(val))
            {
                structure.put("fitvalrefid", fitValueNumbering.get(val));
            }
            else
            {
                structure.put("fitvalid", fitValueNumbering.get(val));
                alreadyAdded.add(val);
            }
            return structure;
        }
        else
        {
            return val.structExport();
        }
    }

    /** Exports a layer to its fencodeable structure representation.
     * <p>
     * Lookup table can't be stored to the structure.
     * <p>
     * Wavelength could be saved to the structure representations
     * of individual Layers, but it is stored only to the structure
     * representation of LayerStack to minimize redundancy.
     *
     * @return the structure representation of this layer without lookup table or wavelength information
     */
    public Object structExport(final Map<FitValue, Integer> fitValueNumbering,
                               final Set<FitValue> alreadyAdded) {
        Map<String,Object> structure = new HashMap<String,Object>();
        structure.put("name",name);
        structure.put("d",setFitValue(fitValueNumbering, alreadyAdded, d));
        structure.put("rho",setFitValue(fitValueNumbering, alreadyAdded, rho));
        structure.put("r",setFitValue(fitValueNumbering, alreadyAdded, r));
        structure.put("f",f);
        structure.put("compound1",compound1.toString());
        structure.put("compound2",compound2.toString());
        return structure;
    }

    public XMLRowable xmlRowable(final Map<FitValue, Integer> fitValueNumbering,
                                 final Set<FitValue> alreadyAdded)
    {
        return new XMLRowable() {
            public void setFitValue(DocumentFragment f, String name,
                                    FitValue val)
            {
                if (fitValueNumbering.containsKey(val))
                {
                    if (alreadyAdded.contains(val))
                    {
                        f.set(name).setRow("fitvalue", val);
                        f.get(name).get("fitvalue").setAttrInt(
                            "fitvalrefid", fitValueNumbering.get(val));
                    }
                    else
                    {
                        f.set(name).setRow("fitvalue", val);
                        f.get(name).get("fitvalue").setAttrInt(
                            "fitvalid", fitValueNumbering.get(val));
                        alreadyAdded.add(val);
                    }
                }
                else
                {
                    f.set(name).setRow("fitvalue", val);
                }
            }
            public void toXMLRow(DocumentFragment frag) {
                frag.setAttrString("name", name);
                frag.setString("compound1", compound1.toString());
                frag.setString("compound2", compound2.toString());
                frag.setDouble("f", f);
                setFitValue(frag, "d", d);
                setFitValue(frag, "rho", rho);
                setFitValue(frag, "r", r);
            }
        };
    }


    /** This can be sent by a FitValue */
    public void valueChanged(ValueEvent ev) {
        signalEvent(null);
    };

    private void signalEvent(LayerEvent ev) {
        if(ev == null)
            ev = new LayerEvent(this);
        for(LayerListener listener: listeners)
            listener.layerPropertyChanged(ev);
    }

    private static FitValue newFitValue(
        Map<FitValue, Integer> fitValueNumbering,
        Map<Integer, FitValue> newFitValues,
        FitValue old)
    {
        if (fitValueNumbering.containsKey(old))
        {
            int i = fitValueNumbering.get(old);
            if (newFitValues.containsKey(i))
            {
                return newFitValues.get(i);
            }
            FitValue val = old.deepCopy();
            newFitValues.put(i, val);
            return val;
        }
        else
        {
          return old.deepCopy();
        }
    }

    /** Makes a deep copy of this layer.
     *
     * @return A deep copy of this layer
     */
    public Layer deepCopy(Map<FitValue, Integer> fitValueNumbering,
                          Map<Integer, FitValue> newFitValues) {
        FitValue d2 = newFitValue(fitValueNumbering, newFitValues, d);
        FitValue rho2 = newFitValue(fitValueNumbering, newFitValues, rho);
        FitValue r2 = newFitValue(fitValueNumbering, newFitValues, r);

        Layer result = new Layer();
        result.name = this.name;
        result.setThicknessObject(d2);
        result.setDensityObject(rho2);
        result.setRoughnessObject(r2);
        result.f = this.f;
        result.compound1 = this.compound1;
        result.compound2 = this.compound2;
        result.lambda = this.lambda;
        result.table = this.table;
        result.compound = this.compound;

        return result;
    }

    private Layer() {}

    /** Constructor.
     * <p>
     *
     * Creates a layer.
     *
     * @param name the layer name
     * @param d thickness
     * @param rho mass density
     * @param r roughness
     * @param compound1 first compound of the mixture of two compounds
     * @param compound2 second compound of the mixture of two compounds
     * @param f the proportion of second compound
     * @param table lookup table
     * @param lambda wavelength
     *
     * @throws ElementNotFound an element was not found in the lookup table for the given wavelength
     */
    public Layer(String name, FitValue d, FitValue rho, FitValue r,
            ChemicalFormula compound1, ChemicalFormula compound2, double f,
            LookupTable table, double lambda) throws ElementNotFound
    {
        this.table = table;
        this.lambda = lambda;
        setName(name);
        setThickness(d);
        setDensity(rho);
        setRoughness(r);
        setCompounds(compound1, compound2);
        setF(f);
    }

    private FitValue getFitValue(DocumentFragment f,
                                 Map<String, FitValue> fitValueById)
    {
        if (f.getNotNull("fitvalue").getAttrStringObject("fitvalrefid") != null)
        {
            FitValue result = fitValueById.get(
                f.getNotNull("fitvalue").getAttrStringNotNull("fitvalrefid"));
            if (result == null)
            {
                throw new NullPointerException();
            }
            return result;
        }
        FitValue result = new FitValue(f.getNotNull("fitvalue"));
        String id = f.getNotNull("fitvalue").getAttrStringObject("fitvalid");
        if (id != null)
        {
            fitValueById.put(id, result);
        }
        return result;
    }

    public Layer(DocumentFragment frag, LookupTable table, double lambda,
                 Map<String, FitValue> fitValueById)
    throws ChemicalFormulaException, ElementNotFound
    {
        this.table = table;
        this.lambda = lambda;
        setName(frag.getAttrStringNotNull("name"));
        setThicknessObject(getFitValue(frag.getNotNull("d"), fitValueById));
        setDensityObject(getFitValue(frag.getNotNull("rho"), fitValueById));
        setRoughnessObject(getFitValue(frag.getNotNull("r"), fitValueById));
        setCompounds(new ChemicalFormula(frag.getStringNotNull("compound1")),
                     new ChemicalFormula(frag.getStringNotNull("compound2")));
        setF(frag.getDoubleNotNull("f"));
    }


    /** Changes the layer name */
    public void setName(String name) {
        if(name == null)
            throw new NullPointerException();
        this.name = name;
    };

    /** Changes the two compounds.
     *
     * <p>
     *
     * This method changes the two compounds this layer consists of. If an
     * element from a compound is not found in the lookup table for the used
     * wavelength, an exception is thrown and the layer composition is rolled
     * back.
     */
    public void setCompounds(ChemicalFormula compound1, ChemicalFormula compound2) throws ElementNotFound {
        ChemicalFormula c1bak = this.compound1, c2bak = this.compound2;
        try {
            if(compound1 != null)
                this.compound1 = compound1;
            if(compound2 != null)
                this.compound2 = compound2;
            calcXRRCompound();
        }
        catch(ElementNotFound e) {
            this.compound1 = c1bak;
            this.compound2 = c2bak;
            throw e;
        }
    };

    /** Changes the wavelength and the lookup table.
     *
     * <p>
     *
     * This method tries to change the lookup table and used wavelength for the
     * layer. If all the elements from the composition of this layer are not
     * found in the new lookup table for the given wavelength, an exception is
     * thrown and the layer state is rolled back.
     *
     * <p>
     *
     * The method should only be called by LayerStack.
     */
    public void updateWlData(LookupTable table, double lambda) throws ElementNotFound {
        double lambdabak = this.lambda;
        LookupTable tablebak = this.table;
        try {
            this.table = table;
            this.lambda = lambda;
            calcXRRCompound();
        }
        catch(ElementNotFound e) {
            this.table = tablebak;
            this.lambda = lambdabak;
            throw e;
        }
    }

    private void calcXRRCompound() throws ElementNotFound {
        Map<String,Double> m = getComposition();
        compound = new Compound(m, table, lambda);
        signalEvent(null);
    }

    /** Returns the X-ray properties of this layer.
     *
     * <p>
     *
     * This method returns the X-ray optical properties of this layer for the
     * used wavelength.
     */
    public Compound getXRRCompound() {
        return compound;
    }

    /** Changes the proportion of second compound.
     *
     * <p>
     *
     * Changing the proportion changes the composition of the layer requiring a
     * new calculation of X-ray optical properties. If the lookup table used is
     * static, no exception is thrown when changing the composition, since
     * the exception would have been thrown earlier.
     */
    public void setF(double f) {
        if(f < 0.0)
            f = 0.0;
        if(f > 1.0)
            f = 1.0;
        this.f = f;
        try {
            calcXRRCompound();
        }
        catch(ElementNotFound ex) {
            throw new RuntimeException("Doesn't get thrown"); /* XXX: ugly hack */
        }
    };

    /** Changes the FitValue object of layer thickness.
     *
     * <p>
     *
     * The layer thickness can also be changed by modifying the existing FitValue
     * returned by getThickness().
     *
     */
    public void setThickness(FitValue d) {
        if(d == null)
            throw new NullPointerException();
        if (this.d == null)
        {
          this.d = d.deepCopy();
          this.d.addValueListener(this);
        }
        else
        {
          this.d.deepCopyFrom(d);
        }
    };
    /*
       You may only call this for layers not owned by a layer stack
     */
    public void setThicknessObject(FitValue d)
    {
        if(d == null)
            throw new NullPointerException();
        if (this.d != null)
            this.d.removeValueListener(this);
        this.d = d;
        this.d.addValueListener(this);
    }
    /** Changes the FitValue object of mass density.
     *
     * <p>
     *
     * The mass density can also be changed by modifying the existing FitValue
     * returned by getThickness().
     */
    public void setDensity(FitValue rho) {
        if(rho == null)
            throw new NullPointerException();
        if (this.rho == null)
        {
          this.rho = rho.deepCopy();
          this.rho.addValueListener(this);
        }
        else
        {
          this.rho.deepCopyFrom(rho);
        }
    };
    /*
       You may only call this for layers not owned by a layer stack
     */
    public void setDensityObject(FitValue rho)
    {
        if(rho == null)
            throw new NullPointerException();
        if (this.rho != null)
            this.rho.removeValueListener(this);
        this.rho = rho;
        this.rho.addValueListener(this);
    }
    /** Changes the FitValue object of roughness.
     *
     * <p>
     *
     * The roughness can also be changed by modifying the existing FitValue
     * returned by getThickness().
     */
    public void setRoughness(FitValue r) {
        if(r== null)
            throw new NullPointerException();
        if (this.r == null)
        {
          this.r = r.deepCopy();
          this.r.addValueListener(this);
        }
        else
        {
          this.r.deepCopyFrom(r);
        }
    };
    /*
       You may only call this for layers not owned by a layer stack
     */
    public void setRoughnessObject(FitValue r)
    {
        if(r == null)
            throw new NullPointerException();
        if (this.r != null)
            this.r.removeValueListener(this);
        this.r = r;
        this.r.addValueListener(this);
    }


    /** String representation.
      
       <p>
      
       The methods returns a string representation for the layer which is shown
       in layer stack lists.
      
       @return String representation
      
     */
    public String toString() {
        return toString(new HashMap<FitValue, Integer>());
    }
    /** String representation.
      
       <p>
      
       The methods returns a string representation for the layer which is shown
       in layer stack lists.
      
       @param fitValueNumbering Link numbers for linked FitValues
       @return String representation
     */
    public String toString(Map<FitValue, Integer> fitValueNumbering) {
        final Map<FitValue, Integer> n = fitValueNumbering;
        return this.name +
        ", d = " + String.format(Locale.US,"%.6g",this.d.getExpected()*1e9) + " nm " +
          (n.containsKey(this.d) ? "[L" + n.get(this.d) + "] " : "") +
          (this.d.getEnabled() ? "(fit)" : "(no fit)") +
        ", rho = " + String.format(Locale.US,"%.6g",this.rho.getExpected()/1e3) + " g/cm^3 " +
          (n.containsKey(this.rho) ? "[L" + n.get(this.rho) + "] " : "") +
          (this.rho.getEnabled() ? "(fit)" : "(no fit)") +
        ", r = " + String.format(Locale.US,"%.6g",this.r.getExpected()*1e9) + " nm " +
          (n.containsKey(this.r) ? "[L" + n.get(this.r) + "] " : "") +
          (this.r.getEnabled() ? "(fit)" : "(no fit)");
    }

};

