/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.util;

/**
 * A Large Object Reference (LOR) to an object in possibly memory-sensitive
 * situations when it is passed as a parameter.
 *
 * This is used primarily to save memory. You create a LOR to for example a
 * large array or Image and then null all own references to that large object.
 * You can then pass the reference into a method and the method can null out the
 * object as soon as it is finished with it. It frees memory as soon as
 * possible, creating more headroom for other processing tasks.
 *
 * When you pass-by-reference large objects as a LargeObjectReference, the
 * receiving method is explicitly given permission to destroy the passed
 * argument.
 *
 * @author phou
 */
public class LOR {

    private Object largeObject;

    /**
     * Create a Large Object Reference to the specified object
     *
     * It is important that you null or use scoping to ensure all other
     * references to the largeObject are removed. This is usually the next line
     * of code after creating the LOR
     *
     * @param largeObject
     */
    public LOR(final Object largeObject) {
        //#mdebug
        if (largeObject == null) {
            L.i(this, "Warning", "LargeObjectReference(null) created. This is allowed, but may not have been intended");
        }
        //#enddebug

        this.largeObject = largeObject;
    }

    /**
     * A convenience method for avoiding extra null pointer checks.
     *
     * This method returns null if either the LargeObjectReference or the object
     * it points to are null
     *
     * @param largeObjectReference
     * @return
     */
    public static Object get(final LOR largeObjectReference) {
        if (largeObjectReference == null) {
            return null;
        }

        return largeObjectReference.get();
    }

    /**
     * Get the references object, or null if it has been de-referenced
     *
     * @return
     */
    public Object get() {
        return largeObject;
    }

    /**
     * Get the referenced object, which is a byte[]
     * 
     * @return 
     */            
    public byte[] getBytes() {
        return (byte[]) get();
    }

    /**
     * Clear the reference. If no other references to this object exist,
     * additional memory is available.
     */
    public void clear() {
        largeObject = null;
    }

    //#mdebug
    public String toString() {
        return "LargeObjectReference to: " + largeObject;
    }
    //#enddebug
}
