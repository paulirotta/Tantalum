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

import org.junit.Test;
import org.tantalum.MockedStaticInitializers;
import org.tantalum.util.PoolingWeakHashCache;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for PoolingWeakHashCache
 *
 * @author phou
 */
public class PoolingWeakHashCacheTest extends MockedStaticInitializers {

    /**
     * Test of testClear method, of class PoolingWeakHashCache.
     */
    @Test
    public void testClear() {
        System.out.println("clear");
        PoolingWeakHashCache instance = new PoolingWeakHashCache();
        instance.put("key", "value");
        instance.remove("key");
        instance.clear();
        assertNull("Get from empty pool", instance.getFromPool());
    }

    /**
     * Test of testRemove method, of class PoolingWeakHashCache.
    @Test
    public void testRemove() {
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
    @Test
    public void testGetFromPool() {
        System.out.println("getFromPool");
        PoolingWeakHashCache instance = new PoolingWeakHashCache();
        instance.put("key", "value");
        instance.put("key2", "value2");
        instance.remove("key");
        assertNotNull("Get from not-empty pool", instance.getFromPool());
        assertNull("Pool should be empty", instance.getFromPool());
    }
}
