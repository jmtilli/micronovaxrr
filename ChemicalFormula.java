import java.util.*;


/** A chemical formula.
 *
 * This class contains the necessary code to parse a chemical formula.
 *
 * The chemical formula consists of the named elements and their amounts, which
 * must be expressed in integers. Parentheses are not supported. For example,
 * you must use AlC3H9 instead of Al(CH3)3.
 */
public class ChemicalFormula {
    private final String s;

    /** Constructor.
     *
     * This class creates a chemical formula and ensures it is syntactically valid.
     *
     * @throws ChemicalFormulaException if s is syntactically illegal
     */
    public ChemicalFormula(String s) throws ChemicalFormulaException {
        this.s = s;
        doParse(null, 1.0);
    }

    public String toString() {
        return s;
    }

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

    public boolean equals(Object o)
    {
      ChemicalFormula that;
      if (o == this)
      {
        return true;
      }
      if (o == null)
      {
        return false;
      }
      if (!(o instanceof ChemicalFormula))
      {
        return false;
      }
      that = (ChemicalFormula)o;
      if (!utilEquals(this.s, that.s))
      {
        return false;
      }
      return true;
    }


    /** Chemical formula parsing.
     *
     * This function parses the chemical formula and converts it to an
     * element-&gt;amount dictionary. Multiple chemical formulae can be added
     * to the same dictionary with different weights.
     *
     * @param elements The dictionary to add the elements to.
     * 
     * @param f The weight of the compound described by this formula. The
     * amount of each element is multiplied by this weight before adding it to
     * the dictionary.
     */
    public void parse(Map<String,Double> elements, double f) {
        /* XXX: this is an ugly hack. Should only parse at creation */
        try {
            doParse(elements, f);
        }
        catch(ChemicalFormulaException e) {
            throw new RuntimeException("This doesn't get thrown");
        }
    }

    /* The real code to implement parsing */
    private void doParse(Map<String,Double> elements, double f) throws ChemicalFormulaException {
        /* IMPORTANT NOTE: this method must add every element to the map even if f == 0 */
        String element = null;
        double count = -1;
        for(int i=0; i<s.length(); i++) {
            char ch = s.charAt(i);
            if(Character.isUpperCase(ch)) {
                if(element != null) {
                    if(count < 0)
                        count = 1;
                    if(count == 0)
                        throw new ChemicalFormulaException("count == 0");
                    if(elements != null) {
                        if(!elements.containsKey(element))
                            elements.put(element,0.0);
                        elements.put(element,elements.get(element)+f*count);
                    }
                    element = null;
                    count = -1;
                }
                element = ""+ch;
            } else if(Character.isLowerCase(ch)) {
                if(element == null || count >= 0)
                    throw new ChemicalFormulaException("Element starting with a lowercase letter");
                element += ch;
            } else if(Character.isDigit(ch)) {
                if(element == null)
                    throw new ChemicalFormulaException("Formula starting with a digit");
                if(count < 0)
                    count = 0;
                count *= 10;
                try {
                    count += Integer.parseInt(""+ch);
                }
                catch(NumberFormatException e) {
                    throw new ChemicalFormulaException("Unsupported character");
                }
            } else {
                throw new ChemicalFormulaException("Unsupported character");
            }
        }
        if(element == null)
            throw new ChemicalFormulaException("Empty formula");
        if(count < 0)
            count = 1;
        if(count == 0)
            throw new ChemicalFormulaException("count == 0");
        if(elements != null) {
            if(!elements.containsKey(element))
                elements.put(element,0.0);
            elements.put(element,elements.get(element)+f*count);
        }
    }

    /** Unit test. */
    public static void main(String[] args) {
        boolean ex = false;
        try {
            new ChemicalFormula("AlCl3");
            new ChemicalFormula("Al1Cl3");
            new ChemicalFormula("AlCH3CH3CH3");
            new ChemicalFormula("H2O");
            new ChemicalFormula("H2");
            new ChemicalFormula("O2");
            new ChemicalFormula("O");
            new ChemicalFormula("HCOOH");
            new ChemicalFormula("CH3COOH");
            new ChemicalFormula("CH3CH2OH");
        } catch (Exception e) {
            System.out.println("err");
        }

        ex = false;
        try { new ChemicalFormula("3OH"); } catch (Exception e) { ex = true; }
        if(!ex) System.out.println("error1");
        ex = false;
        try { new ChemicalFormula("H2O0"); } catch (Exception e) { ex = true; }
        if(!ex) System.out.println("error1");
        ex = false;
        try { new ChemicalFormula("HO0"); } catch (Exception e) { ex = true; }
        if(!ex) System.out.println("error1");
        ex = false;
        try { new ChemicalFormula("H1O0"); } catch (Exception e) { ex = true; }
        if(!ex) System.out.println("error1");
        ex = false;
        try { new ChemicalFormula("O0"); } catch (Exception e) { ex = true; }
        if(!ex) System.out.println("error1");
        ex = false;
        try { new ChemicalFormula("H0O"); } catch (Exception e) { ex = true; }
        if(!ex) System.out.println("error1");
        ex = false;
        try { new ChemicalFormula("alcl3"); } catch (Exception e) { ex = true; }
        if(!ex) System.out.println("error2");
        ex = false;
        try { new ChemicalFormula(""); } catch (Exception e) { ex = true; }
        if(!ex) System.out.println("error3");
        ex = false;
        try { new ChemicalFormula("H2o"); } catch (Exception e) { ex = true; }
        if(!ex) System.out.println("error4");
        ex = false;
        try { new ChemicalFormula("h2O"); } catch (Exception e) { ex = true; }
        if(!ex) System.out.println("error5");
        ex = false;
        try { new ChemicalFormula("OH-"); } catch (Exception e) { ex = true; }
        if(!ex) System.out.println("error6");
    }
};
