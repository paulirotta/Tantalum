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

import org.junit.Before;
import org.junit.Test;
import org.tantalum.PlatformUtils;
import static org.junit.Assert.*;

import org.tantalum.net.xml.RSSModel;
import org.tantalum.util.StringUtils;
import org.xml.sax.SAXException;

/**
 * Unit tests for RSSModel
 *
 * @author phou
 */
public class RSSModelTest {

    byte[] xml;

    /**
     * Test of testParseElement method, of class RSSModel.
     */
    @Test
    public void testParseElement() {
        System.out.println("parseElement");
        RSSModel instance = new RSSModel(100);
        try {
            instance.setXML(xml);
        } catch (Exception ex) {
            fail("Can not parse RSS: " + ex);
        }
        assertEquals("rss size", 86, instance.size());
        try {
            instance.setXML(null);
            fail("Should not attempt to parse null RSS");
        } catch (IllegalArgumentException e) {
            // Correct answer
        } catch (Exception ex) {
            fail("Can not parse null RSS: " + ex);
        }
        try {
            instance.setXML(new byte[0]);
            fail("Should not handle 0 byte RSS");
        } catch (IllegalArgumentException e) {
            // Correct answer
        } catch (Exception ex) {
            fail("Can not handle 0 byte RSS: " + ex);
        }
        try {
            instance.setXML(new byte[1]);
            fail("Should not handle 1 byte RSS");
        } catch (SAXException ex) {
            // Correct
        } catch (Exception e) {
            fail("Wrong exception on parse bad 1 byte RSS: " + e);
        }
    }
}
