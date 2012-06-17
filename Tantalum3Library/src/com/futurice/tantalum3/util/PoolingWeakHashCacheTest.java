/*
 * PoolingWeakHashCacheTest.java
 * JMUnit based test
 *
 * Created on 05-Mar-2012, 11:13:28
 */
package com.futurice.tantalum3.util;

import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;

/**
 * @author phou
 */
public class PoolingWeakHashCacheTest extends TestCase {

    public PoolingWeakHashCacheTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(3, "PoolingWeakHashCacheTest");
    }

    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testClear();
                break;
            case 1:
                testRemove();
                break;
            case 2:
                testGetFromPool();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testClear method, of class PoolingWeakHashCache.
     */
    public void testClear() throws AssertionFailedException {
        System.out.println("clear");
        PoolingWeakHashCache instance = new PoolingWeakHashCache();
        instance.put("key", "value");
        instance.remove("key");
        instance.clear();
        assertNull("Get from empty pool", instance.getFromPool());
    }

    /**
     * Test of testRemove method, of class PoolingWeakHashCache.
     */
    public void testRemove() throws AssertionFailedException {
        System.out.println("remove");
        PoolingWeakHashCache instance = new PoolingWeakHashCache();
        instance.put("key", "value");
        instance.remove("key");
        instance.remove("other key");
        instance.remove(null);
    }

    /**
     * Test of testGetFromPool method, of class PoolingWeakHashCache.
     */
    public void testGetFromPool() throws AssertionFailedException {
        System.out.println("getFromPool");
        PoolingWeakHashCache instance = new PoolingWeakHashCache();
        instance.put("key", "value");
        instance.put("key2", "value2");
        instance.remove("key");
        assertNotNull("Get from not-empty pool", instance.getFromPool());
        assertNull("Pool should be empty", instance.getFromPool());
    }
}
