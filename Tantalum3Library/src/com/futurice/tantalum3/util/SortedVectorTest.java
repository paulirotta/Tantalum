/*
 * SortedVectorTest.java
 * JMUnit based test
 *
 * Created on 22-Mar-2012, 15:24:34
 */
package com.futurice.tantalum3.util;

import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;

/**
 * @author phou
 */
public class SortedVectorTest extends TestCase {
    
    public SortedVectorTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(3, "SortedVectorTest");
    }    
    
    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testInsertElementAt();
                break;
            case 1:
                testSetElementAt();
                break;
            case 2:
                testAddElement();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testInsertElementAt method, of class SortedVector.
     */
    public void testInsertElementAt() throws AssertionFailedException {
        System.out.println("insertElementAt");
        SortedVector instance = new SortedVector(new SortedVector.Comparator() {

            public boolean before(Object o1, Object o2) {
                return ((Integer) o1).intValue() < ((Integer) o2).intValue();
            }
        });
        Object o_1 = null;
        int index_1 = 0;
        try {
            instance.insertElementAt(o_1, index_1);
        } catch (IllegalArgumentException e) {
            return;
        }
        fail("testInsertElementAt() should throw an exception.");
    }

    /**
     * Test of testSetElementAt method, of class SortedVector.
     */
    public void testSetElementAt() throws AssertionFailedException {
        System.out.println("setElementAt");
        SortedVector instance = new SortedVector(new SortedVector.Comparator() {

            public boolean before(Object o1, Object o2) {
                return ((Integer) o1).intValue() < ((Integer) o2).intValue();
            }
        });
        Object o_1 = null;
        int index_1 = 0;
        try {
            instance.setElementAt(o_1, index_1);
        } catch (IllegalArgumentException e) {
            return;
        }
        fail("testSetElementAt() should throw an exception.");
    }

    /**
     * Test of testAddElement method, of class SortedVector.
     */
    public void testAddElement() throws AssertionFailedException {
        System.out.println("addElement");
        SortedVector instance = new SortedVector(new SortedVector.Comparator() {

            public boolean before(Object o1, Object o2) {
                return ((Integer) o1).intValue() < ((Integer) o2).intValue();
            }
        });
        Object o_1 = new Integer(10);
        Object o_2 = new Integer(1);
        Object o_3 = new Integer(3);
        instance.addElement(o_1);
        instance.addElement(o_2);
        instance.addElement(o_3);
        assertEquals("First", o_2, instance.elementAt(0));
        assertEquals("Second", o_3, instance.elementAt(1));
        assertEquals("Third", o_1, instance.elementAt(2));
    }
}
