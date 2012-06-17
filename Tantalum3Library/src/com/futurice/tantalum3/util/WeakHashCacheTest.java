/*
 * WeakHashCacheTest.java
 * JMUnit based test
 *
 * Created on 07-Feb-2012, 16:02:23
 */
package com.futurice.tantalum3.util;

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
        String key1 = "";
        String key2 = "a";
        instance.remove(null);
        instance.remove(key1);
        instance.put(key1, "fruit");
        instance.put(key2, "cake");
        instance.remove(key1);
        instance.remove(key2);
        assertEquals(instance.size(), 0);
    }

    /**
     * Test of testPut method, of class WeakHashCache.
     */
    public void testPut() throws AssertionFailedException {
        System.out.println("put");
        WeakHashCache instance = new WeakHashCache();
        String key1 = "";
        String key2 = "a";
        String expectedVal1 = "fruit";
        instance.put(key1, expectedVal1);
        instance.put(key2, "cake");
        instance.put(null, "dog");
        assertEquals(instance.size(), 2);
        instance.put(key2, null);
        assertEquals(instance.size(), 1);
        for (int i = 0; i < 10000; i++) {
            instance.put("keykeykey" + i, "valuevaluevalue" + i);
        }
        Object val1 = instance.get(key1);
        if (val1 != null) {
            assertEquals(val1, expectedVal1);
        }
    }

    /**
     * Test of testGet method, of class WeakHashCache.
     */
    public void testGet() throws AssertionFailedException {
        System.out.println("get");
        WeakHashCache instance = new WeakHashCache();
        String key1 = "11";
        String key2 = "22";
        Object value1 = "chocolate";
        Object value2 = "cake";
        Object result_1 = instance.get(key1);
        assertNull("get null", result_1);
        instance.put(key1, value1);
        instance.put(key2, value2);
        assertEquals("get val 1", value1, instance.get(key1));
        assertEquals("get val 2", value2, instance.get(key2));
    }

    /**
     * Test of testContainsKey method, of class WeakHashCache.
     */
    public void testContainsKey() throws AssertionFailedException {
        System.out.println("containsKey");
        WeakHashCache instance = new WeakHashCache();
        String key1 = "11";
        String key2 = "22";
        Object value1 = "chocolate";
        Object value2 = "cake";
        instance.put(key1, value1);
        instance.put(key2, value2);
        assertEquals(false, instance.containsKey("red"));
        assertEquals(true, instance.containsKey(key1));
        assertEquals(false, instance.containsKey(null));
    }

    /**
     * Test of testSize method, of class WeakHashCache.
     */
    public void testSize() throws AssertionFailedException {
        System.out.println("size");
        WeakHashCache instance = new WeakHashCache();
        final int length = 10000;
        for (int i = 0; i < length; i++) {
            instance.put("keykeykey" + i, "valuevaluevalue" + i);
        }
        assertEquals(length, instance.size());
    }
}
