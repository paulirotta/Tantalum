/*
 * ByteArrayStorableResourceTest.java
 * JMUnit based test
 *
 * Created on 07-Feb-2012, 16:29:35
 */
package com.futurice.tantalum2.rms;

import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;

/**
 * @author phou
 */
public class ByteArrayStorableResourceTest extends TestCase {
    
    public ByteArrayStorableResourceTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(4, "ByteArrayStorableResourceTest");
    }    
    
    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testClearData();
                break;
            case 1:
                testSerialize();
                break;
            case 2:
                testDeserialize();
                break;
            case 3:
                testGetData();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testClearData method, of class ByteArrayStorableResource.
     */
    public void testClearData() throws AssertionFailedException {
        System.out.println("clearData");
        ByteArrayStorableResource instance = null;
        instance.clearData();
        fail("The test case is a prototype.");
    }

    /**
     * Test of testSerialize method, of class ByteArrayStorableResource.
     */
    public void testSerialize() throws AssertionFailedException {
        System.out.println("serialize");
        ByteArrayStorableResource instance = null;
        byte[] expResult_1 = null;
        byte[] result_1 = instance.serialize();
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testDeserialize method, of class ByteArrayStorableResource.
     */
    public void testDeserialize() throws AssertionFailedException {
        System.out.println("deserialize");
        ByteArrayStorableResource instance = null;
        byte[] bytes_1 = null;
        instance.deserialize(bytes_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testGetData method, of class ByteArrayStorableResource.
     */
    public void testGetData() throws AssertionFailedException {
        System.out.println("getData");
        ByteArrayStorableResource instance = null;
        byte[] expResult_1 = null;
        byte[] result_1 = instance.getData();
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }
}
