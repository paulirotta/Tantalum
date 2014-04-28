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

import java.util.Enumeration;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author phou
 */
public class LRUHashtableTest {

    @Test
    public void testAddElement() {
        System.out.println("testAddElement");
        LRUHashtable instance = new LRUHashtable();
        instance.put("a", "A");
        assertEquals("Size 1", 1, instance.size());
        instance.put("b", "B");
        assertEquals("Size 2", 2, instance.size());
    }

    @Test
    public void testGetAndLRUKey() {
        System.out.println("testGetAndLRUKey");
        LRUHashtable instance = new LRUHashtable();
        instance.put("a", "A");
        instance.put("b", "B");
        instance.put("c", "C");
        instance.get("a");
        assertEquals("LRU is b", "b", instance.getLeastRecentlyUsedKey());
    }

    @Test
    public void testKeyEnumeration() {
        System.out.println("testKeyEnumeration");
        LRUHashtable instance = new LRUHashtable();
        String[] keys = {"a", "b", "c", "d" };
        instance.put("a", "A");
        instance.put("b", "B");
        instance.put("c", "C");
        instance.put("d", "D");
        Enumeration enu = instance.keys();
        instance.get("c");
        instance.remove("b");
        int i = 0;
        while (enu.hasMoreElements()) {
            String s = (String) enu.nextElement();
            assertEquals("LRU element " + i + " sequence", keys[i++], s);            
        }
    }
}
