/*
 * JSONModelTest.java
 * JMUnit based test
 *
 * Created on 07-Feb-2012, 15:09:46
 */
package com.futurice.tantalum3.net.json;

import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;

/**
 * @author phou
 */
public class JSONModelTest extends TestCase {
    
    public JSONModelTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(6, "JSONModelTest");
    }    
    
    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testGetString();
                break;
            case 1:
                testGetBoolean();
                break;
            case 2:
                testGetDouble();
                break;
            case 3:
                testSetJSON();
                break;
            case 4:
                testGetLong();
                break;
            case 5:
                testGetInt();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testGetString method, of class JSONModel.
     */
    public void testGetString() throws AssertionFailedException, Exception {
        System.out.println("getString");
        JSONModel instance = new JSONModel();
        String key_1 = "";
        String expResult_1 = "";
        String result_1 = instance.getString(key_1);
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testGetBoolean method, of class JSONModel.
     */
    public void testGetBoolean() throws AssertionFailedException, Exception {
        System.out.println("getBoolean");
        JSONModel instance = new JSONModel();
        String key_1 = "";
        boolean expResult_1 = false;
        boolean result_1 = instance.getBoolean(key_1);
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testGetDouble method, of class JSONModel.
     */
    public void testGetDouble() throws AssertionFailedException, Exception {
        System.out.println("getDouble");
        JSONModel instance = new JSONModel();
        String key_1 = "";
        double expResult_1 = 0.0;
        double result_1 = instance.getDouble(key_1);
        assertTrue(expResult_1 == result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testSetJSON method, of class JSONModel.
     */
    public void testSetJSON() throws AssertionFailedException, Exception {
        System.out.println("setJSON");
        JSONModel instance = new JSONModel();
        String json_1 = "";
        instance.setJSON(json_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testGetLong method, of class JSONModel.
     */
    public void testGetLong() throws AssertionFailedException, Exception {
        System.out.println("getLong");
        JSONModel instance = new JSONModel();
        String key_1 = "";
        long expResult_1 = 0L;
        long result_1 = instance.getLong(key_1);
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testGetInt method, of class JSONModel.
     */
    public void testGetInt() throws AssertionFailedException, Exception {
        System.out.println("getInt");
        JSONModel instance = new JSONModel();
        String key_1 = "";
        int expResult_1 = 0;
        int result_1 = instance.getInt(key_1);
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }
}
