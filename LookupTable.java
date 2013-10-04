/** Lookup table for x-ray properties of elements
 */
public interface LookupTable {
    /** Get x-ray properties of an element for the specified wavelength.
     *
     * <p>
     *
     * All lookup tables used by this software must be immutable. That is, when a lookup
     * table returns x-ray properties for the specified element and wavelength, it must
     * return the exactly same properties when the lookup method is called again and NOT throw
     * an ElementNotFound exception. 
     *
     * @param name case-sensitive element symbol
     * @param lambda wavelength in meters
     *
     * @throws ElementNotFound if the properties for the specified element and wavelength are not found
     *
     * @return x-ray properties
     */
    public Element lookup(String name, double lambda) throws ElementNotFound;
}
