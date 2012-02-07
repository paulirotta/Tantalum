/*
 * RMSGetterTest.java
 * JMUnit based test
 *
 * Created on 07-Feb-2012, 16:31:13
 */
package com.futurice.tantalum2.rms;

import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;

/**
 * @author phou
 */
public class RMSGetterTest extends TestCase {
    
    public RMSGetterTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(2, "RMSGetterTest");
    }    
    
    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testGetError();
                break;
            case 1:
                testGet();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testGetError method, of class RMSGetter.
     */
    public void testGetError() throws AssertionFailedException {
        System.out.println("getError");
        RMSGetter instance = null;
        Throwable expResult_1 = null;
        Throwable result_1 = instance.getError();
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testGet method, of class RMSGetter.
     */
    public void testGet() throws AssertionFailedException {
        System.out.println("get");
        RMSGetter instance = null;
        byte[] expResult_1 = null;
        byte[] result_1 = instance.get();
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }
}
