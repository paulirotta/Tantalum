/*
 * LRUVectorTest.java
 * JMUnit based test
 *
 * Created on 22-Mar-2012, 14:44:06
 */
package com.futurice.tantalum3.util;

import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;

/**
 * @author phou
 */
public class LRUVectorTest extends TestCase {

    public LRUVectorTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(5, "LRUVectorTest");
    }

    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testSetElementAt();
                break;
            case 1:
                testAddElement();
                break;
            case 2:
                testRemoveLeastRecentlyUsed();
                break;
            case 3:
                testContains();
                break;
            case 4:
                testInsertElementAt();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testSetElementAt method, of class LRUVector.
     */
    public void testSetElementAt() throws AssertionFailedException {
        System.out.println("setElementAt");
        LRUVector instance = new LRUVector();
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
     * Test of testSetElementAt method, of class LRUVector.
     */
    public void testInsertElementAt() throws AssertionFailedException {
        System.out.println("setElementAt");
        LRUVector instance = new LRUVector();
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
     * Test of testAddElement method, of class LRUVector.
     */
    public void testAddElement() throws AssertionFailedException {
        System.out.println("addElement");
        LRUVector instance = new LRUVector();
        Object o_1 = "a";
        Object o_2 = "b";
        instance.addElement(o_1);
        assertEquals("Size 1", 1, instance.size());
        instance.addElement(o_2);
        assertEquals("Size 2", 2, instance.size());
        instance.addElement(o_1);
        assertEquals("Size 2 again", 2, instance.size());
    }

    /**
     * Test of testRemoveLeastRecentlyUsed method, of class LRUVector.
     */
    public void testRemoveLeastRecentlyUsed() throws AssertionFailedException {
        System.out.println("removeLeastRecentlyUsed");
        LRUVector instance = new LRUVector();
        Object expResult_1 = "a";
        instance.addElement(expResult_1);
        instance.addElement("b");
        instance.addElement("c");
        instance.addElement("d");
        instance.addElement("e");
        instance.addElement("f");
        Object result_1 = instance.removeLeastRecentlyUsed();
        assertEquals("Least recently used", expResult_1, result_1);
        instance.addElement("b");
        instance.contains("c");
        Object result_2 = instance.removeLeastRecentlyUsed();
        assertEquals("Least recently used", "d", result_2);
    }

    /**
     * Test of testContains method, of class LRUVector.
     */
    public void testContains() throws AssertionFailedException {
        System.out.println("contains");
        LRUVector instance = new LRUVector();
        instance.addElement("a");
        instance.addElement("b");
        instance.addElement("c");
        boolean result_1 = instance.contains("b");
        assertEquals("Contains", true, result_1);
        boolean result_2 = instance.contains("d");
        assertEquals("Does not contain", false, result_2);
    }
}
