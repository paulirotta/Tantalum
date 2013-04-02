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

import junit.framework.TestCase;
import org.tantalum.MockedStaticInitializers;
import org.tantalum.util.LengthLimitedVector;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * LengthLimitedVector unit tests
 *
 * @author phou
 */
public class LengthLimitedVectorTest extends MockedStaticInitializers {

    private boolean tooLong = false;

    /**
     * Test of testAddElement method, of class LengthLimitedVector.
     *
     */
    @Test
    public void testAddElement() {
        System.out.println("addElement");
        LengthLimitedVector instance = new LengthLimitedVector(3) {
            protected void lengthExceeded(Object extra) {
                tooLong = true;
            }
        };
        instance.addElement("a");
        instance.addElement("b");
        instance.addElement("c");
        instance.addElement("d");
        assertEquals("too long test", true, tooLong);
    }

    /**
     * Test of testLengthExceeded method, of class LengthLimitedVector.
     *
     */
    @Test
    public void testLengthExceeded() {
        System.out.println("lengthExceeded");
        LengthLimitedVector instance = new LengthLimitedVector(3) {
            protected void lengthExceeded(Object o) {
            }
        };
        instance.addElement("a");
        instance.addElement("b");
        instance.addElement("c");
        assertEquals("Full length", 3, instance.size());
        instance.addElement("d");
        assertEquals("Max length", 3, instance.size());
        assertEquals("LRU after length exceeded", "b", instance.firstElement());
    }
}
