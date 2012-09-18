/*
 * LengthLimitedVectorTest.java
 * JMUnit based test
 *
 * Created on 22-Mar-2012, 15:11:25
 */
package com.futurice.tantalum3.util;

import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;

/**
 * @author phou
 */
public class LengthLimitedVectorTest extends TestCase {
    public boolean tooLong = false;
    public LengthLimitedVectorTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(2, "LengthLimitedLRUVectorTest");
    }
    
    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testAddElement();
                break;
            case 1:
                testLengthExceeded();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testAddElement method, of class LengthLimitedVector.
     */
    public void testAddElement() throws AssertionFailedException {
        System.out.println("addElement");
        LengthLimitedVector instance = new LengthLimitedVector(3) {

            protected void lengthExceeded(Object extra) {
               tooLong = true;
            }
        };
        instance.addElement("a");
        instance.addElement("b");
        instance.addElement("c");
        instance.addElement("d");
        assertEquals("too long test", true, tooLong);
    }

    /**
     * Test of testLengthExceeded method, of class LengthLimitedVector.
     */
    public void testLengthExceeded() throws AssertionFailedException {
        System.out.println("lengthExceeded");
        LengthLimitedVector instance = new LengthLimitedVector(3) {

            protected void lengthExceeded(Object o) {
            }
        };
        instance.addElement("a");
        instance.addElement("b");
        instance.addElement("c");
        assertEquals("Full length", 3, instance.size());
        instance.addElement("d");
        assertEquals("Max length", 3, instance.size());
        assertEquals("LRU after length exceeded", "b", instance.firstElement());
    }
}
