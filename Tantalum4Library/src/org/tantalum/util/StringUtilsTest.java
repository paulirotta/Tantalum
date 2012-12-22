/*
 * StringUtilsTest.java
 * JMUnit based test
 *
 * Created on 13-Nov-2012, 18:01:52
 */

package org.tantalum.util;


import java.util.Vector;
import javax.microedition.lcdui.Font;
import jmunit.framework.cldc11.*;

/**
 * @author phou
 */
public class StringUtilsTest extends TestCase {
    
    public StringUtilsTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(4,"StringUtilsTest");
    }            

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
     */
    public void testReadStringFromJAR() throws AssertionFailedException, Exception {
        System.out.println("readStringFromJAR");
        String name_1 = "";
        String expResult_1 = "";
        String result_1 = StringUtils.readStringFromJAR(name_1);
        assertEquals(expResult_1, result_1);
    }

    /**
     * Test of testReadBytesFromJAR method, of class StringUtils.
     */
    public void testReadBytesFromJAR() throws AssertionFailedException, Exception {
        System.out.println("readBytesFromJAR");
        byte[] result_1 = StringUtils.readBytesFromJAR("/rss.xml");
        assertNotEquals(0, result_1.length);
    }

    /**
     * Test of testTruncate method, of class StringUtils.
     */
    public void testTruncate() throws AssertionFailedException {
        System.out.println("truncate");
        String str_1 = "This is a a really long line of text--------------------------------------------------------------------------------------------------------------------------------";
        Font font_1 = Font.getDefaultFont();
        int maxWidth_1 = 240;
        String expResult_1 = "This is a a really long line of t...";
        String result_1 = StringUtils.truncate(str_1, font_1, maxWidth_1);
        System.out.println(expResult_1);
        System.out.println(result_1);
        assertEquals(result_1, expResult_1);
    }

    /**
     * Test of testSplitToLines method, of class StringUtils.
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
