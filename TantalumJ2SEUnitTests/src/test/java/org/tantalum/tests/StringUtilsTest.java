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
import org.tantalum.util.StringUtils;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for StringUtils
 *
 * @author phou
 */
public class StringUtilsTest extends MockedStaticInitializers {

    static final String EXAMPLE_FILE_NAME = "/rss.xml";


    /**
     * Test of testReadStringFromJAR method, of class StringUtils.
     *
     * @throws Exception
     */
    @Test
    public void testReadStringFromJAR() throws Exception {
        String name_1 = "/test.txt";
        String expResult_1 = "Hello, this is test text";
        String result_1 = StringUtils.readStringFromJAR(name_1);
        assertEquals(expResult_1, result_1);
    }

    /**
     * Test of testReadBytesFromJAR method, of class StringUtils.
     *
     * @throws Exception
     */
    @Test
    public void testReadBytesFromJAR() throws Exception {
        //ClassLoader classLoader = this.getClass()
        // JSE classloader to verify the length of the file, rather than using a magic number
        InputStream stream = getClass().getResourceAsStream(EXAMPLE_FILE_NAME);

        assertNotNull("Could not read example file from classpath", stream);

        byte[] result_1 = StringUtils.readBytesFromJAR(EXAMPLE_FILE_NAME);
        assertNotNull(result_1);
        assertEquals(stream.available(), result_1.length);
    }

}
