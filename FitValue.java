/* This class stores a value to fit and its range */


import java.util.*;


/* Not immutable! */


/** Fitting value and its range.
 *
 * This class stores a value to fit, and its allowed range. It is not immutable
 * and not thread safe.
 *
 */
public class FitValue {
    private double min, expected, max;
    private boolean enable; /* whether to fit the value or not */
    private boolean supported;
    private final List<ValueListener> listeners = new ArrayList<ValueListener>();

    public static enum FitValueType {
      MIN, EXPECTED, MAX
    };

    public double getValue(FitValueType type)
    {
      if (type == null)
      {
        throw new NullPointerException();
      }
      switch (type)
      {
        case MIN:
          return min;
        case EXPECTED:
          return expected;
        case MAX:
          return max;
        default:
          throw new Error("not reached");
      }
    }
    public double getValueForFitting(FitValueType type)
    {
      return getValue(enable ? type : FitValueType.EXPECTED);
    }

    public boolean equals(Object o)
    {
      FitValue that;
      if (this == o)
      {
        return true;
      }
      if (o == null || !(o instanceof FitValue))
      {
        return false;
      }
      that = (FitValue)o;
      if (   this.min       != that.min
          || this.expected  != that.expected
          || this.max       != that.max
          || this.enable    != that.enable
          || this.supported != that.supported)
      {
        return false;
      }
      return true;
    }

    private FitValue() {
        enable = true;
    }
    public FitValue(double min, double expected, double max)
    {
        this(min, expected, max, true, true);
    }
    public FitValue(double min, double expected, double max, boolean enable)
    {
        this(min, expected, max, enable, true);
    }
    public FitValue(double min, double expected, double max, boolean enable,
                    boolean supported)
    {
        this.supported = supported;
        setValues(min, expected, max, enable);
    }

    public void addValueListener(ValueListener l) {
        listeners.add(l);
    }
    public void removeValueListener(ValueListener l) {
        listeners.remove(l);
    }
    private void signalChange() {
        ValueEvent ev = new ValueEvent(this);
        for(ValueListener listener: listeners)
            listener.valueChanged(ev);
    }
    public boolean isSupported()
    {
       return supported;
    }

    public void setValues(double min, double expected, double max)
    {
        this.setValues(min, expected, max, this.enable);
    }

    /** Set values
     *
     * @param min new minimum value
     * @param expected new expected value
     * @param max new maximum value
     * @param enabled whether fitting is enabled
     * @throws IllegalArgumentException if min &gt; max or expected not in [min,max]
     */
    public void setValues(double min, double expected, double max,
                          boolean enabled)
    {
        if(expected < min || expected > max || min > max)
            throw new IllegalArgumentException("Invalid boundary values");
        this.expected = expected;
        this.min = min;
        this.max = max;
        this.enable = supported ? enabled : false;
        signalChange();
    }
    /** Enables or disables fitting of this value
     */
    public void setEnabled(boolean enable)
    {
        this.enable = supported ? enable : false;
        signalChange();
    }
    /** Make a copy of this object
     */
    public FitValue deepCopy() {
        return new FitValue(min, expected, max, enable);
    }
    public void deepCopyFrom(FitValue v2) {
        setValues(v2.min, v2.expected, v2.max, v2.enable);
    }

    /** Make a fencodeable data structure of this object
     */
    public Map<String,Object> structExport() {
        Map<String,Object> m = new HashMap<String,Object>();
        m.put("min",min);
        m.put("expected",expected);
        m.put("max",max);
        m.put("enable",enable?1:0); /* we can't store booleans */
        return m;
    }
    /** Make a new object based on the fencodeable data structure
     *
     * @param o the data structure
     * @return the new object
     * @throws InvalidStructException if the data structure does not represent a FitValue
     */
    public static FitValue structImport(Object o) throws InvalidStructException {
        Map<?,?> m;
        Object o2;
        FitValue val = new FitValue();


        if(!(o instanceof Map))
            throw new InvalidStructException();
        m = (Map<?,?>)o;

        o2 = m.get("min");
        if(o2 == null || !(o2 instanceof Double))
            throw new InvalidStructException();
        val.min = (Double)o2;

        o2 = m.get("max");
        if(o2 == null || !(o2 instanceof Double))
            throw new InvalidStructException();
        val.max = (Double)o2;

        o2 = m.get("expected");
        if(o2 == null || !(o2 instanceof Double))
            throw new InvalidStructException();
        val.expected = (Double)o2;

        o2 = m.get("enable");
        if(o2 != null) {
          if(!(o2 instanceof Integer))
              throw new InvalidStructException();
          val.enable = (((Integer)o2)!=0);
        }

        return val;
    }

    public double getMin() { return this.min; };
    public double getMax() { return this.max; };
    public double getExpected() { return this.expected; };
    public boolean getEnabled() { return this.enable; }

    /** Set the expected value.
     *
     * The new expected value is set to newExpected unless newExpected &lt; min
     * or newExpected &gt; max in which case the expected value is set to min
     * or max, respectively.
     *
     * @param newExpected the new expected value
     */
    public void setExpected(double newExpected) {
        if(newExpected < this.min)
            newExpected = this.min;
        if(newExpected > this.max)
            newExpected = this.max;
        this.expected = newExpected;
        signalChange();
    }
};
