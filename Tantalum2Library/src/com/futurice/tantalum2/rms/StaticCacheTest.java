/*
 * StaticCacheTest.java
 * JMUnit based test
 *
 * Created on 07-Feb-2012, 16:33:50
 */
package com.futurice.tantalum2.rms;

import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;

/**
 * @author phou
 */
public class StaticCacheTest extends TestCase {
    
    public StaticCacheTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(13, "StaticCacheTest");
    }    
    
    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testGet();
                break;
            case 1:
                testGetHandler();
                break;
            case 2:
                testStoreToRMS();
                break;
            case 3:
                testContainsKey();
                break;
            case 4:
                testClear();
                break;
            case 5:
                testMakeSpace();
                break;
            case 6:
                testGetSizeOfReservedSpace();
                break;
            case 7:
                testGetFromRMS();
                break;
            case 8:
                testRemove();
                break;
            case 9:
                testToString();
                break;
            case 10:
                testGetSize();
                break;
            case 11:
                testPut();
                break;
            case 12:
                testGetPriority();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testGet method, of class StaticCache.
     */
    public void testGet() throws AssertionFailedException {
        System.out.println("get");
        StaticCache instance = null;
        String key_1 = "";
        Object expResult_1 = null;
        Object result_1 = instance.get(key_1);
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testGetHandler method, of class StaticCache.
     */
    public void testGetHandler() throws AssertionFailedException {
        System.out.println("getHandler");
        StaticCache instance = null;
        DataTypeHandler expResult_1 = null;
        DataTypeHandler result_1 = instance.getHandler();
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testStoreToRMS method, of class StaticCache.
     */
    public void testStoreToRMS() throws AssertionFailedException {
        System.out.println("storeToRMS");
        StaticCache instance = null;
        String key_1 = "";
        byte[] bytes_1 = null;
        instance.storeToRMS(key_1, bytes_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testContainsKey method, of class StaticCache.
     */
    public void testContainsKey() throws AssertionFailedException {
        System.out.println("containsKey");
        StaticCache instance = null;
        String key_1 = "";
        boolean expResult_1 = false;
        boolean result_1 = instance.containsKey(key_1);
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testClear method, of class StaticCache.
     */
    public void testClear() throws AssertionFailedException {
        System.out.println("clear");
        StaticCache instance = null;
        instance.clear();
        fail("The test case is a prototype.");
    }

    /**
     * Test of testMakeSpace method, of class StaticCache.
     */
    public void testMakeSpace() throws AssertionFailedException {
        System.out.println("makeSpace");
        int spaceNeeded_1 = 0;
        boolean expResult_1 = false;
        boolean result_1 = StaticCache.makeSpace(spaceNeeded_1);
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testGetSizeOfReservedSpace method, of class StaticCache.
     */
    public void testGetSizeOfReservedSpace() throws AssertionFailedException {
        System.out.println("getSizeOfReservedSpace");
        StaticCache instance = null;
        int expResult_1 = 0;
        int result_1 = instance.getSizeOfReservedSpace();
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testGetFromRMS method, of class StaticCache.
     */
    public void testGetFromRMS() throws AssertionFailedException {
        System.out.println("getFromRMS");
        StaticCache instance = null;
        String key_1 = "";
        byte[] expResult_1 = null;
        byte[] result_1 = instance.getFromRMS(key_1);
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testRemove method, of class StaticCache.
     */
    public void testRemove() throws AssertionFailedException {
        System.out.println("remove");
        StaticCache instance = null;
        String key_1 = "";
        instance.remove(key_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testToString method, of class StaticCache.
     */
    public void testToString() throws AssertionFailedException {
        System.out.println("toString");
        StaticCache instance = null;
        String expResult_1 = "";
        String result_1 = instance.toString();
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testGetSize method, of class StaticCache.
     */
    public void testGetSize() throws AssertionFailedException {
        System.out.println("getSize");
        StaticCache instance = null;
        int expResult_1 = 0;
        int result_1 = instance.getSize();
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testPut method, of class StaticCache.
     */
    public void testPut() throws AssertionFailedException {
        System.out.println("put");
        StaticCache instance = null;
        String key_1 = "";
        Object o_1 = null;
        instance.put(key_1, o_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testGetPriority method, of class StaticCache.
     */
    public void testGetPriority() throws AssertionFailedException {
        System.out.println("getPriority");
        StaticCache instance = null;
        int expResult_1 = 0;
        int result_1 = instance.getPriority();
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }
}
