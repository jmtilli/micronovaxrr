import javax.swing.*;
import javax.swing.event.*;
import java.util.*;


/** Layer model.
 *
 * <p>
 *
 * The LayerStack contains all the necessary information of the layer model and
 * other experimental parameters to calculate and fit reflectivity curves.
 * LayerStack objects contain all individual layers, the wavelength used for
 * the experiment, instrumental resolution and a lookup table for elements.
 *
 * <p>
 *
 * LayerStack objects are mutable and not thread safe. If accessed from
 * multiple threads, access must be synchronized properly. Another thread
 * safety approach is to make a deep copy of LayerStack for each thread and
 * then copy the information back from the thread that has modified its deep
 * copy of the LayerStack object. 
 *
 * <p>
 *
 * The LayerStack implements ListModel in order to support showing its layers
 * in a list. ListDataEvents are reported after a change for any property of
 * the layer stack. ListDataListeners can also be used for other purposes. The
 * user interface of this program makes use of ListDataListeners to show
 * wavelength and instrument resolution in text labels, to plot reflectivity
 * curves and to update the positions of sliders.
 *
 * <p>
 *
 * LayerStack supports fencode serialization. Of course, listeners are not
 * stored in fencode structures.
 *
 */

public class LayerStack implements LayerListener, ValueListener {
    private ArrayList<Layer> layers;
    private final Set<ListDataListener> listListeners = new HashSet<ListDataListener>();
    private final Set<LayerModelListener> modelListeners = new HashSet<LayerModelListener>();
    private double lambda;

    private FitValue stddev;

    /* in decibels */
    private FitValue prod;
    private FitValue sum;
    private FitValue beam;

    private LookupTable table;


    public double[] getFitValuesForFitting(FitValue.FitValueType type)
    {
      double[] result = new double[3+3*layers.size()];
      result[0] = prod.getValueForFitting(type);
      result[1] = sum.getValueForFitting(type);
      result[2] = beam.getValueForFitting(type);
      for (int i = 0; i < layers.size(); i++)
      {
        Layer l = layers.get(i);
        result[3+0*layers.size()+i] =
          l.getThickness().getValueForFitting(type);
        result[3+1*layers.size()+i] =
          l.getDensity().getValueForFitting(type);
        result[3+2*layers.size()+i] =
          l.getRoughness().getValueForFitting(type);
      }
      return result;
    }
    public void setFitValues(double[] values)
    {
      if (values.length != 3+3*layers.size())
      {
        throw new IllegalArgumentException();
      }
      this.prod.setExpected(values[0]);
      this.sum.setExpected(values[1]);
      this.beam.setExpected(values[2]);
      /*
         If there are duplicate layers, the last value takes precedence.
       */
      for (int i = 0; i < layers.size(); i++)
      {
        Layer l = layers.get(i);
        l.getThickness().setExpected(values[3+0*layers.size()+i]);
        l.getDensity().setExpected(values[3+1*layers.size()+i]);
        l.getRoughness().setExpected(values[3+2*layers.size()+i]);
      }
    }

    public boolean equals(Object o)
    {
      LayerStack that;
      if (this == o)
      {
        return true;
      }
      if (o == null || !(o instanceof LayerStack))
      {
        return false;
      }
      that = (LayerStack)o;
      // compare wavelengths and table references
      if (   this.lambda != that.lambda
          || this.table != that.table)
      {
        return false;
      }

      // compare fit values
      if (   !this.stddev.equals(that.stddev)
          || !this.prod.equals(that.prod)
          || !this.sum.equals(that.sum)
          || !this.beam.equals(that.beam))
      {
        return false;
      }

      // compare layers: size, numbering, contents
      if (this.layers.size() != that.layers.size())
      {
        return false;
      }
      Map<FitValue,Integer> this_numbering = this.getFitValueNumbering();
      Map<FitValue,Integer> that_numbering = that.getFitValueNumbering();
      for (int i = 0; i < this.layers.size(); i++)
      {
        Layer this_layer = this.layers.get(i);
        Layer that_layer = that.layers.get(i);
        if (!Layer.layerDeepEquals(this_layer, this_numbering,
                                   that_layer, that_numbering))
        {
          return false;
        }
      }
      return true;
    }

    /**
       Return link numbers for FitValues that are linked.

       @return Link numbers for FitValues that are linked.
     */
    private Map<FitValue,Integer> getFitValueNumbering() {
        Map<FitValue,Integer> counts = new HashMap<FitValue,Integer>();
        Map<FitValue,Integer> links = new HashMap<FitValue,Integer>();
        int id = 0;
        for (Layer l: layers)
        {
            FitValue[] vals = {l.getThickness(), l.getDensity(),
                               l.getRoughness()};
            for (FitValue val: vals)
            {
                if (counts.containsKey(val))
                {
                    counts.put(val, counts.get(val)+1);
                }
                else
                {
                    counts.put(val, 1);
                }
            }
        }
        for (Layer l: layers)
        {
            FitValue[] vals = {l.getThickness(), l.getDensity(),
                               l.getRoughness()};
            for (FitValue val: vals)
            {
                if (counts.get(val) > 1 && !links.containsKey(val))
                {
                    links.put(val, ++id);
                }
            }
        }
        return links;
    }

    public final ListModel<String> listModel = new ListModel<String>() {
        public void addListDataListener(ListDataListener l) {
            listListeners.add(l);
        }
        public void removeListDataListener(ListDataListener l) {
            listListeners.remove(l);
        }
        public int getSize() {
            return layers.size();
        }


        /* XXX: this is slow! Replace with a better (caching) algorithm if necessary */
        /* map from layers to integers in order to make references to
         * same layer distinguishable from identical different layers.
         *
         * The caching could be implemented in getFitValueNumbering.
         * */
        public String getElementAt(int i) {
            Map<FitValue,Integer> fitValueNumbering = getFitValueNumbering();
            Layer l = layers.get(i);
            return l.toString(fitValueNumbering);
        }
    };

    /** Constructor.
     *
     * <p>
     *
     * Creates a layer stack.
     *
     * @param lambda wavelength
     * @param table lookup table
     *
     */
    public LayerStack(double lambda, LookupTable table) {
        this.layers = new ArrayList<Layer>();
        this.lambda = lambda;
        this.table = table;
        this.stddev = new FitValue(0,0,0.01*Math.PI/180,false,false);
        this.prod = new FitValue(-100,0,100,false);
        this.beam = new FitValue(0,250,1000,false);
        this.sum = new FitValue(-200,-200,100,false);
        this.prod.addValueListener(this);
        this.beam.addValueListener(this);
        this.sum.addValueListener(this);
        this.stddev.addValueListener(this);
    }

    public void valueChanged(ValueEvent ev) {
        signalPropertyChange();
    }

    private void signalPropertyChange() {
        EventObject ev = new EventObject(this);
        for(LayerModelListener listener: modelListeners) {
            listener.modelPropertyChanged(ev);
            listener.simulationChanged(ev);
        }
    }
    private void signalStackChange(ListDataEvent ev) {
        for(LayerModelListener listener: modelListeners) {
            switch(ev.getType()) {
                case ListDataEvent.CONTENTS_CHANGED:
                    listener.layersChanged(ev);
                    break;
                case ListDataEvent.INTERVAL_ADDED:
                    listener.layersAdded(ev);
                    break;
                case ListDataEvent.INTERVAL_REMOVED:
                    listener.layersRemoved(ev);
                    break;
            }
            listener.simulationChanged(ev);
        }
        for(ListDataListener listener: listListeners) {
            switch(ev.getType()) {
                case ListDataEvent.CONTENTS_CHANGED:
                    listener.contentsChanged(ev);
                    break;
                case ListDataEvent.INTERVAL_ADDED:
                    listener.intervalAdded(ev);
                    break;
                case ListDataEvent.INTERVAL_REMOVED:
                    listener.intervalRemoved(ev);
                    break;
            }
        }
    }
    /* there may be multiple instances of the same layer */
    /* XXX: should we send one CONTENTS_CHANGED message with the whole range? */
    public void layerPropertyChanged(LayerEvent ev) {
        for(int i=0; i<layers.size(); i++) {
            if(layers.get(i) == ev.layer) {
                ListDataEvent lev = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, i, i);
                for(ListDataListener listener: listListeners)
                    listener.contentsChanged(lev);
            }
        }
        for(LayerModelListener listener: modelListeners)
            listener.simulationChanged(new EventObject(this));
    }

    /** Adds a listener. */
    public void addLayerModelListener(LayerModelListener l) {
        this.modelListeners.add(l);
    }
    /** Removes a listener. */
    public void removeLayerModelListener(LayerModelListener l) {
        this.modelListeners.remove(l);
    }


    /** Changes the wavelength.
     *
     * <p>
     *
     * The method tries to change the wavelength for this layer stack. If any
     * layer has an element which is not found in the lookup table for the new
     * wavelength, an exception is thrown and the original state is rolled back.
     *
     * @param lambda2 the new wavelength
     *
     * @throws ElementNotFound if the lookup table does not contain information
     * for an element for the given wavelength
     *
     */
    public void changeLambda(double lambda2) throws ElementNotFound {
        LayerStack stack2 = this.deepCopy();
        stack2.lambda = lambda2;
        for(int i=0; i<stack2.getSize(); i++) {
            stack2.getElementAt(i).updateWlData(table, lambda2);
        }
        this.deepCopyFrom(stack2);
    }
    /** Instrument resolution.
     *
     * <p>
     *
     * The method returns the instrument resolution. Limited resolution
     * is modeled by a Gaussian function, the standard distribution of
     * which is returned by this method.
     *
     * @return standard deviation of a Gaussian distribution
     */
    public FitValue getStdDev() { return stddev; }

    public void setStdDev(FitValue stddev) {
        this.stddev.deepCopyFrom(stddev);
    }

    public FitValue getProd() {
        return prod;
    }
    public FitValue getBeam() {
        return beam;
    }
    public FitValue getSum() {
        return sum;
    }
    public void setProd(FitValue prod) {
        this.prod.deepCopyFrom(prod);
    }
    public void setBeam(FitValue beam) {
        this.beam.deepCopyFrom(beam);
    }
    public void setSum(FitValue sum) {
        this.sum.deepCopyFrom(sum);
    }

    /** Deep copy.
     *
     * <p>
     *
     * The method makes a deep copy of this object. Listeners are not copied.
     *
     * @return a deep copy of this layer stack
     */
    public LayerStack deepCopy() {
        LayerStack result = new LayerStack(this.lambda, this.table);
        Map<FitValue, Integer> fitValueNumbering = getFitValueNumbering();
        Map<Integer, FitValue> newFitValues = new HashMap<Integer, FitValue>();
        int size = layers.size();
        for(int i=0; i<size; i++)
            result.layers.add(layers.get(i).deepCopy(fitValueNumbering,
                                                     newFitValues));
        result.stddev.deepCopyFrom(this.stddev.deepCopy());
        result.prod.deepCopyFrom(this.prod.deepCopy());
        result.beam.deepCopyFrom(this.beam.deepCopy());
        result.sum.deepCopyFrom(this.sum.deepCopy());
        return result;
    }

    /** Exports a layer stack to its fencodeable structure representation.
     *
     * <p>
     *
     * This method creates a structure representation of this layer stack
     * which can be serialized later to a byte stream by fencode.
     * Listeners and the lookup table are not present in the structure representation.
     *
     * @param additional_data an additional structure to add to the structure representation of this layer stack.
     * The layer stack is represented by a Map, which will contain a key named "additional_data". Can be null.
     *
     * @return the structure representation of this layer without lookup table or wavelength information
     */
    public Object structExport(Object additional_data) {
        Map<FitValue, Integer> fitValueNumbering = getFitValueNumbering();
        Set<FitValue> alreadyAdded = new HashSet<FitValue>();
        ArrayList<Object> layerStructs = new ArrayList<Object>();
        for(Layer l: layers)
            layerStructs.add(l.structExport(fitValueNumbering, alreadyAdded));
        Map<String,Object> m = new HashMap<String,Object>();
        m.put("layers",layerStructs);
        m.put("lambda",lambda);
        m.put("stddev",stddev.getExpected());
        m.put("measNormal",1.0);
        m.put("stddevObj", stddev.structExport());
        m.put("prod", prod.structExport());
        m.put("beam", beam.structExport());
        m.put("sum", sum.structExport());
        m.put("measSum",0.0);
        if(additional_data != null)
            m.put("additional_data",additional_data);
        return m;
    }

    /** Imports a layer stack from its fencodeable structure representation.
     *
     * <p>
     *
     * Listeners and the lookup table are not imported from the structure. The new layer stack
     * contains no listeners. The lookup table of the new layer stack is specified as a parameter.
     *
     * @return a new layer stack imported from the structure representation
     *
     * @param o the structure representation to import a Layer from
     * @param table the lookup table
     * @throws InvalidStructException the structure does not represent a Layer
     * @throws ElementNotFound an element was not found in the lookup table for the given wavelength
     */

    public static LayerStack structImport(Object o, LookupTable table) throws InvalidStructException, ElementNotFound {
        Map<?,?> m;
        Object layersO;
        Object obj;
        ArrayList<?> layersL;
        final double Cu_K_alpha = 1.5405600e-10;
        double lambda = Cu_K_alpha;
        Map<Integer, FitValue> fitValueById = new HashMap<Integer, FitValue>();

        if(!(o instanceof Map))
            throw new InvalidStructException();
        m = (Map<?,?>)o;
        obj = m.get("lambda");
        if(obj != null) {
            if(!(obj instanceof Double))
                throw new InvalidStructException();
            lambda = (Double)obj;
        }


        LayerStack temp = new LayerStack(lambda, table);

        obj = m.get("stddevObj");
        if (obj != null) {
            temp.stddev.deepCopyFrom(FitValue.structImport(obj));
        }
        else
        {
            obj = m.get("stddev");
            if(obj != null) {
                if(!(obj instanceof Double))
                    throw new InvalidStructException();
                temp.stddev.setExpected(((Double)obj)*Math.PI/180);
            }
        }

        obj = m.get("prod");
        if (obj != null) {
            temp.prod.deepCopyFrom(FitValue.structImport(obj));
        }
        obj = m.get("beam");
        if (obj != null) {
            temp.beam.deepCopyFrom(FitValue.structImport(obj));
        }
        obj = m.get("sum");
        if (obj != null) {
            temp.sum.deepCopyFrom(FitValue.structImport(obj));
        }

        /*
        obj = m.get("stddev");
        if(obj != null) {
            if(!(obj instanceof Double))
                throw new InvalidStructException();
            temp.setStdDev((Double)obj);
        }

        obj = m.get("measNormal");
        if(obj != null) {
            if(!(obj instanceof Double))
                throw new InvalidStructException();
            temp.setMeasNormal((Double)obj);
        }

        obj = m.get("measSum");
        if(obj != null) {
            if(!(obj instanceof Double))
                throw new InvalidStructException();
            temp.setMeasSum((Double)obj);
        }
        */


        layersO = m.get("layers");
        if(layersO == null || !(layersO instanceof ArrayList))
            throw new InvalidStructException();
        layersL = (ArrayList<?>)layersO;
        for(Object o2: layersL) {
            temp.layers.add(Layer.structImport(o2, fitValueById, table, lambda));
        }
        return temp;
    }
    /** Deep copy from an object.
     *
     * <p>
     *
     * The method makes a deep copy of the given object to this object. The listeners of this
     * object are not changed in any way.
     *
     * @param s2 the layer stack to make the deep copy from
     */
    public void deepCopyFrom(LayerStack s2) {
        LayerStack temp = s2.deepCopy();
        if(getSize() > 0)
            signalStackChange(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, getSize()-1));
        this.layers = temp.layers;
        this.lambda = temp.lambda;
        this.stddev.deepCopyFrom(temp.stddev);
        this.prod.deepCopyFrom(temp.prod);
        this.beam.deepCopyFrom(temp.beam);
        this.sum.deepCopyFrom(temp.sum);
        this.table = temp.table;
        if(getSize() > 0)
            signalStackChange(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, 0, getSize()-1));
    }

    /** Adds a layer.
     *
     * <p>
     *
     * The wavelength and lookup table of the layer are set to the properties
     * of this LayerStack. If the new lookup table does not contain all the
     * necessary elements for the new wavelength, an exception is thrown and
     * nothing is changed.
     *
     * <p>
     *
     * After a layer is added to a layer stack, the layer stack should be
     * considered the owner of the layer. See getElementAt for the rules
     * for using layers owned by a layer stack.
     *
     * @param l the layer to add
     *
     * @throws ElementNotFound an element was not found in the lookup table
     *
     * @see #getElementAt
     */

    public void add(Layer l) throws ElementNotFound {
        add(l, 0);
    }
    /** Returns the wavelength */
    public double getLambda() {
        return lambda;
    }
    /** Returns the element lookup table */
    public LookupTable getTable() {
        return table;
    }
    /** Inserts a layer to the given position.
     *
     * <p>
     *
     * The wavelength and lookup table of the layer are set
     * to the properties of this LayerStack. If the new lookup
     * table does not contain all the necessary elements for the
     * new wavelength, an exception is thrown and nothing is changed.
     *
     * <p>
     *
     * After a layer is added to a layer stack, the layer stack should be
     * considered the owner of the layer. See getElementAt for the rules
     * for using layers owned by a layer stack.
     *
     * @param l the layer to add
     * @param i the position to insert the layer to
     *
     * @throws ElementNotFound an element was not found in the lookup table
     *
     * @see #getElementAt
     */
    public void add(Layer l, int i) throws ElementNotFound {
        if(l == null)
            throw new NullPointerException();
        l.updateWlData(this.table, this.lambda);
        this.layers.add(i, l);
        signalStackChange(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, i, i));
    }
    /** Removes a layer from this stack.
     *
     * @param i the index of the layer to remove
     */
    public void remove(int i) {
        this.layers.remove(i);
        signalStackChange(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, i, i));
    }
    /** Moves a layer up.
     *
     * @param i the index of the layer to move
     *
     * @throws IndexOutOfBoundsException the layer at the given index was not found or is the uppermost layer
     */
    public void moveUp(int i) {
        if(i <= 0)
            throw new IndexOutOfBoundsException();
        layers.add(i-1,layers.remove(i));
        signalStackChange(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, i-1, i));
    }
    /** Moves a layer down.
     *
     * @param i the index of the layer to move
     *
     * @throws IndexOutOfBoundsException the layer at the given index was not found or is the lowermost layer
     */
    public void moveDown(int i) {
        if(i >= getSize()-1)
            throw new IndexOutOfBoundsException();
        layers.add(i+1,layers.remove(i));
        signalStackChange(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, i, i+1));
    }
    /** Signals a change of the layer stack to all listeners.
     *
     * <p>
     *
     * This method must be called after a layer is modified.
     *
     * @param o the sending object of the event. It can be null to set it to this LayerStack.
     *
     */
    public void invalidate(Object o) {
        /* XXX: the whole method is an ugly hack. */
        if(o == null)
            o = this;
        if(getSize() > 0)
            signalStackChange(new ListDataEvent(o, ListDataEvent.CONTENTS_CHANGED, 0, getSize()-1));
    }
    /** Returns the number of layers in this stack. */
    public int getSize() {
        return this.layers.size();
    }

    /** Gets a layer at the specified position.
     *
     * <p>
     *
     * The layer returned by this method is owned by this LayerStack object. It may
     * not be added to another LayerStack. The updateWlData method must not be called.
     * After the layer is modified, the invalidate method of this LayerStack must be called.
     * 
     * @return the layer at the specified position
     *
     * @param i position of the layer
     *
     * @throws IndexOutOfBoundsException if the layer was not found
     *
     * @see #invalidate
     */
    public Layer getElementAt(int i) {
        return this.layers.get(i);
    }
    /** Returns a string representation of this layer stack.
     *
     * @return string representations of individual layers separated by newlines.
     */
    public String toString() {
        String result = "";
        int size = getSize();
        for(int i=0; i<size; i++) {
            result += getElementAt(i).toString() + "\n";
        }
        return result;
    }
}
