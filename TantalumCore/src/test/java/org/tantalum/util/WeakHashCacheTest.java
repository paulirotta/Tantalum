/*
 Copyright (c) 2013 Nokia Corporation. All rights reserved.
 Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation.
 Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 affiliates. Other product and company names mentioned herein may be trademarks
 or trade names of their respective owners.

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
package org.tantalum.util;

import org.junit.Test;
import org.tantalum.MockedStaticInitializers;

import static org.junit.Assert.*;

/**
 * Unit tests for WeakHashCache
 *
 * @author phou
 */
public class WeakHashCacheTest extends MockedStaticInitializers {

    /**
     * Test of testRemove method, of class WeakHashCache.
     */
    @Test
    public void testRemove() {
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
    @Test
    public void testPut() {
        System.out.println("put");
        WeakHashCache instance = new WeakHashCache();
        String key1 = "";
        String key2 = "a";
        String expectedVal1 = "fruit";
        instance.put(key1, expectedVal1);
        instance.put(key2, "cake");
        try {
            instance.put(null, "dog");
            fail("Did not catch null key put to WeakHashCache");
        } catch (Exception e) {
        }
        assertEquals(instance.size(), 2);
        for (int i = 0; i < 10000; i++) {
            instance.put("keykeykey" + i, "valuevaluevalue" + i);
        }
        Object val1 = instance.get(key1);
        if (val1 != null) {
            assertEquals(val1, expectedVal1);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutNullValue() {
        WeakHashCache instance = new WeakHashCache();
        instance.put("aKey", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutNullKey() {
        WeakHashCache instance = new WeakHashCache();
        instance.put(null, "aValue");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetNullKey() {
        WeakHashCache instance = new WeakHashCache();
        instance.put("aKey", "aValue");
        instance.get(null);
    }

    /**
     * Test of testGet method, of class WeakHashCache.
     */
    @Test
    public void testGet() {
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
    @Test
    public void testContainsKey() {
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
        try {
            instance.containsKey(null);
            fail("Did not throw exception on containsKey(null)");
        } catch (Exception e) {
        }
    }

    /**
     * Test of testSize method, of class WeakHashCache.
     */
    @Test
    public void testSize() {
        System.out.println("size");
        WeakHashCache instance = new WeakHashCache();
        final int length = 10000;
        for (int i = 0; i < length; i++) {
            instance.put("keykeykey" + i, "valuevaluevalue" + i);
        }
        assertEquals(length, instance.size());
    }

    final String KEY_CONSTANT = "keykeykey";
    final String VALUE_CONSTANT = "valvalval";
    @Test
    public void testClear() {
        System.out.println("size");
        WeakHashCache instance = new WeakHashCache();
        final int length = 10000;
        for (int i = 0; i < length; i++) {
            instance.put(VALUE_CONSTANT + i, "valuevaluevalue" + i);
        }
        assertEquals(length, instance.size());
        instance.clearValues();
        assertEquals(length, instance.size());
        for (int i = 0; i < length; i++) {
            String key = (String) KEY_CONSTANT + i;
            assertEquals(null, instance.get(key));
        }
    }    
}
