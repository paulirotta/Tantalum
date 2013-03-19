/*
 Copyright (c) 2013, Paul Houghton and Futurice Oy
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
package org.tantalum.tests;

import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;
import org.tantalum.util.PoolingWeakHashCache;

/**
 * Unit tests for PoolingWeakHashCache
 * 
 * @author phou
 */
public class PoolingWeakHashCacheTest extends TestCase {

    /**
     * Create a unit test harness
     */
    public PoolingWeakHashCacheTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(3, "PoolingWeakHashCacheTest");
    }

    /**
     * Invoke tests by number
     * 
     * @param testNumber
     * @throws Throwable 
     */
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
     * 
     * @throws AssertionFailedException 
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
     * 
     * @throws AssertionFailedException 
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
     * 
     * @throws AssertionFailedException 
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
