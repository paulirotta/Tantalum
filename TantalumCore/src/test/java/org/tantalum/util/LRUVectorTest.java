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
import org.tantalum.util.LRUVector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * LRUVector unit tests.
 *
 * @author phou
 */
public class LRUVectorTest extends MockedStaticInitializers {

    /**
     * Test of testSetElementAt method, of class LRUVector.
     */
    @Test
    public void testSetElementAt() {
        System.out.println("setElementAt");
        LRUVector instance = new LRUVector();
        Object o_1 = null;
        int index_1 = 0;
        try {
            instance.setElementAt(o_1, index_1);
        } catch (IllegalArgumentException e) {
            return;
        }
        fail("testSetElementAt() should throw an exception.");
    }

    /**
     * Test of testSetElementAt method, of class LRUVector.
     */
    @Test
    public void testInsertElementAt() {
        System.out.println("setElementAt");
        LRUVector instance = new LRUVector();
        Object o_1 = null;
        int index_1 = 0;
        try {
            instance.insertElementAt(o_1, index_1);
        } catch (IllegalArgumentException e) {
            return;
        }
        fail("testInsertElementAt() should throw an exception.");
    }

    /**
     * Test of testAddElement method, of class LRUVector.
     */
    @Test
    public void testAddElement() {
        System.out.println("addElement");
        LRUVector instance = new LRUVector();
        Object o_1 = "a";
        Object o_2 = "b";
        instance.addElement(o_1);
        assertEquals("Size 1", 1, instance.size());
        instance.addElement(o_2);
        assertEquals("Size 2", 2, instance.size());
        instance.addElement(o_1);
        assertEquals("Size 2 again", 2, instance.size());
    }

    /**
     * Test of testRemoveLeastRecentlyUsed method, of class LRUVector.
     */
    @Test
    public void testRemoveLeastRecentlyUsed() {
        System.out.println("removeLeastRecentlyUsed");
        LRUVector instance = new LRUVector();
        Object expResult_1 = true;
        instance.addElement("a");
        instance.addElement("b");
        instance.addElement("c");
        instance.addElement("d");
        instance.addElement("e");
        instance.addElement("f");
        Object result_1 = instance.contains("a");
        assertEquals("Least recently used", expResult_1, result_1);
        instance.addElement("a");
        instance.contains("b");
        Object result_2 = instance.removeLeastRecentlyUsed();
        assertEquals("Least recently used", "c", result_2);
    }

    /**
     * Test of testContains method, of class LRUVector.
     */
    public void testContains() {
        System.out.println("contains");
        LRUVector instance = new LRUVector();
        instance.addElement("a");
        instance.addElement("b");
        instance.addElement("c");
        boolean result_1 = instance.contains("b");
        assertEquals("Contains", true, result_1);
        boolean result_2 = instance.contains("d");
        assertEquals("Does not contain", false, result_2);
    }
}
