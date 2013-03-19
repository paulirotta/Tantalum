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

import java.util.Vector;
import javax.microedition.lcdui.Font;

import org.tantalum.util.StringUtils;

import jmunit.framework.cldc11.*;
import org.tantalum.PlatformUtils;

/**
 * Unit tests for StringUtils
 *
 * @author phou
 */
public class StringUtilsTest extends TestCase {

    /**
     * Create a new test harness
     */
    public StringUtilsTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(4, "StringUtilsTest");

        PlatformUtils.getInstance().setProgram(this, 4, false); // Init debug
    }

    /**
     * Test invocation by number
     *
     * @param testNumber
     * @throws Throwable
     */
    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testReadStringFromJAR();
                break;
            case 1:
                testReadBytesFromJAR();
                break;
            case 2:
                testTruncate();
                break;
            case 3:
                testSplitToLines();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testReadStringFromJAR method, of class StringUtils.
     *
     * @throws AssertionFailedException
     * @throws Exception
     */
    public void testReadStringFromJAR() throws AssertionFailedException, Exception {
        System.out.println("readStringFromJAR");
        String name_1 = "/test.txt";
        String expResult_1 = "Hello, this is test text";
        String result_1 = StringUtils.readStringFromJAR(name_1);
        assertEquals(expResult_1, result_1);
    }

    /**
     * Test of testReadBytesFromJAR method, of class StringUtils.
     *
     * @throws AssertionFailedException
     * @throws Exception
     */
    public void testReadBytesFromJAR() throws AssertionFailedException, Exception {
        System.out.println("readBytesFromJAR");
        byte[] result_1 = StringUtils.readBytesFromJAR("/rss.xml");
        assertEquals(66547, result_1.length);
    }

    /**
     * Test of testTruncate method, of class StringUtils.
     *
     * @throws AssertionFailedException
     */
    public void testTruncate() throws AssertionFailedException {
        System.out.println("truncate");
        String str_1 = "This is a a really long line of text--------------------------------------------------------------------------------------------------------------------------------";
        Font font_1 = Font.getDefaultFont();
        int maxWidth_1 = 240;
        String expResult_1 = "This is a a really long line of te...";
        String result_1 = StringUtils.truncate(str_1, font_1, maxWidth_1);
        System.out.println(expResult_1);
        System.out.println(result_1);
        assertEquals(expResult_1, result_1);
    }

    /**
     * Test of testSplitToLines method, of class StringUtils.
     *
     * @throws AssertionFailedException
     */
    public void testSplitToLines() throws AssertionFailedException {
        System.out.println("splitToLines");
        Vector vector_1 = new Vector();
        String text_1 = "This is a a really long line of text";
        Font font_1 = Font.getDefaultFont();
        int maxWidth_1 = 100;
        StringUtils.splitToLines(vector_1, text_1, font_1, maxWidth_1);
        this.assertEquals(3, vector_1.size());
    }
}
