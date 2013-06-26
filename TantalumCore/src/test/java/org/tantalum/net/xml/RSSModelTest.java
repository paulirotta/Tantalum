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
package org.tantalum.net.xml;

import org.junit.Before;
import org.junit.Test;
import org.tantalum.MockedStaticInitializers;
import org.tantalum.util.StringUtils;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Ignore;

/**
 * Unit tests for RSSModel
 *
 * @author phou
 */
public class RSSModelTest extends MockedStaticInitializers {

    byte[] xml;
    private RSSModel instance;

    @Before
    public final void rssModelTestFixture() throws IOException {
        xml = StringUtils.readBytesFromJAR("/rss.xml");
        instance = new RSSModel(100);
    }

    /**
     * Test of testParseElement method, of class RSSModel.
     */
    @Test
    public void testParseElement() throws SAXException {
        instance.setXML(xml);
        assertEquals("rss size", 86, instance.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void dontAcceptZeroSizedInput() throws SAXException {
        instance.setXML(new byte[0]);
        fail("Should not handle 0 byte RSS");
    }

    @Test(expected = NullPointerException.class)
    public void dontAllowNullInput() throws SAXException {
        instance.setXML(null);
        fail("Should not attempt to parse null RSS");
    }

    @Test(expected = SAXException.class)
    public void dontAllowOneByteInput() throws SAXException {
        instance.setXML(new byte[1]);
    }
}
