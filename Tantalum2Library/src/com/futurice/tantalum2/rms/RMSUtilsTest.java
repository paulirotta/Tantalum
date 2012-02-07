/*
 * RMSUtilsTest.java
 * JMUnit based test
 *
 * Created on 07-Feb-2012, 16:32:51
 */
package com.futurice.tantalum2.rms;

import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;

/**
 * @author phou
 */
public class RMSUtilsTest extends TestCase {
    
    public RMSUtilsTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(3, "RMSUtilsTest");
    }    
    
    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testReadString();
                break;
            case 1:
                testReadData();
                break;
            case 2:
                testWrite();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testReadString method, of class RMSUtils.
     */
    public void testReadString() throws AssertionFailedException {
        System.out.println("readString");
        String recordStoreName_1 = "";
        String expResult_1 = "";
        String result_1 = RMSUtils.readString(recordStoreName_1);
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testReadData method, of class RMSUtils.
     */
    public void testReadData() throws AssertionFailedException {
        System.out.println("readData");
        String recordStoreName_1 = "";
        byte[] expResult_1 = null;
        byte[] result_1 = RMSUtils.readData(recordStoreName_1);
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testWrite method, of class RMSUtils.
     */
    public void testWrite() throws AssertionFailedException {
        System.out.println("write");
        String recordStoreName_1 = "";
        String value_1 = "";
        RMSUtils.write(recordStoreName_1, value_1);
        fail("The test case is a prototype.");
        String recordStoreName_2 = "";
        byte[] data_2 = null;
        RMSUtils.write(recordStoreName_2, data_2);
        fail("The test case is a prototype.");
    }
}
