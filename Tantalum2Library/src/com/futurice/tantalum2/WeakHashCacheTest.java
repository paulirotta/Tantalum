/*
 * WeakHashCacheTest.java
 * JMUnit based test
 *
 * Created on 07-Feb-2012, 16:02:23
 */
package com.futurice.tantalum2;

import jmunit.framework.cldc11.*;

/**
 * @author phou
 */
public class WeakHashCacheTest extends TestCase {
    
    public WeakHashCacheTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(5, "WeakHashCacheTest");
    }    
    
    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testRemove();
                break;
            case 1:
                testPut();
                break;
            case 2:
                testGet();
                break;
            case 3:
                testContainsKey();
                break;
            case 4:
                testSize();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testRemove method, of class WeakHashCache.
     */
    public void testRemove() throws AssertionFailedException {
        System.out.println("remove");
        WeakHashCache instance = new WeakHashCache();
        String key_1 = "";
        instance.remove(key_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testPut method, of class WeakHashCache.
     */
    public void testPut() throws AssertionFailedException {
        System.out.println("put");
        WeakHashCache instance = new WeakHashCache();
        String key_1 = "";
        Object value_1 = null;
        instance.put(key_1, value_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testGet method, of class WeakHashCache.
     */
    public void testGet() throws AssertionFailedException {
        System.out.println("get");
        WeakHashCache instance = new WeakHashCache();
        String key_1 = "";
        Object expResult_1 = null;
        Object result_1 = instance.get(key_1);
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testContainsKey method, of class WeakHashCache.
     */
    public void testContainsKey() throws AssertionFailedException {
        System.out.println("containsKey");
        WeakHashCache instance = new WeakHashCache();
        String key_1 = "";
        boolean expResult_1 = false;
        boolean result_1 = instance.containsKey(key_1);
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testSize method, of class WeakHashCache.
     */
    public void testSize() throws AssertionFailedException {
        System.out.println("size");
        WeakHashCache instance = new WeakHashCache();
        int expResult_1 = 0;
        int result_1 = instance.size();
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }
}
