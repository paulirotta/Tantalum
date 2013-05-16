/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.util;

import java.io.IOException;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.tantalum.MockedStaticInitializers;

/**
 *
 * @author phou
 */
public class StringUtilsTest extends MockedStaticInitializers {

    static final String EXAMPLE_FILE_NAME = "/rss.xml";

    int[] intTest1 = {0xFF, 0x00};
    int[] intTest2 = {0xCD, 0x1A};
    byte[] byteTest1 = new byte[intTest1.length];
    byte[] byteTest2 = new byte[intTest2.length];

    @Before
    public final void fixtureStringUtilsTest() {
        copyToBytes(intTest1, byteTest1);
        copyToBytes(intTest2, byteTest2);
    }
    
    private void copyToBytes(int[] ints, byte[] bytes) {
        for (int i = 0; i < ints.length; i++) {
            bytes[i] = (byte) (ints[i] & 0xFF);
            System.out.print((bytes[i] & 0xFF) + " ");
        }
        System.out.println();
    }

    @Test
    public void toHexTestFF00() {
        assertEquals("FF00", StringUtils.toHex(byteTest1));
    }

    @Test
    public void toHexTestCD1A() {
        assertEquals("CD1A", StringUtils.toHex(byteTest2));
    }
    
    @Test
    public void urlEncodeSimple() throws IOException {
        assertEquals("This%2Bis+a+simple+%26+short+test.", StringUtils.urlEncode("This+is a simple & short test."));
    }
    
    @Test
    public void urlEncodeSymbols() throws IOException {
        assertEquals("%24+%26+%3C+%3E+%3F+%3B+%23+%3A+%3D+%2C+%22+%27+%7E+%2B+%25", StringUtils.urlEncode("$ & < > ? ; # : = , \" ' ~ + %"));
    }



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

    
//    @Test
//    public void urlEncodeTraditionalChinese() throws IOException {
//        assertEquals("%E6%BC%A2%E5%AD%97", StringUtils.urlEncode("漢字"));
//    }
//    
//    @Test
//    public void urlEncodeSimplifiedChinese() throws IOException {
//        assertEquals("%E6%B1%89%E5%AD%97", StringUtils.urlEncode("汉字"));
//    }
//    
//    @Test
//    public void urlEncodeVietnamese() throws IOException {
//        assertEquals("ch%E1%BB%AF+H%C3%A1n", StringUtils.urlEncode("chữ Hán"));
//    }
//    
//    @Test
//    public void urlEncodeZhuang() throws IOException {
//        assertEquals("%E5%80%B1", StringUtils.urlEncode("倱"));
//    }
//    
//    @Test
//    public void urlEncodeKoreanHangul() throws IOException {
//        assertEquals("%ED%95%9C%EC%9E%90", StringUtils.urlEncode("한자"));
//    }
//    
//    @Test
//    public void urlEncodeKoreanHanja() throws IOException {
//        assertEquals("%E6%BC%A2%E5%AD%97", StringUtils.urlEncode("漢字"));
//    }
//    
//    @Test
//    public void urlEncodeJapaneseHiragana() throws IOException {
//        assertEquals("%E3%81%8B%E3%82%93%E3%81%98", StringUtils.urlEncode("かんじ"));
//    }
//    
//    @Test
//    public void urlEncodeHindi() throws IOException {
//        assertEquals("%E0%A4%AE%E0%A5%81%E0%A4%9D%E0%A5%87+%E0%A4%A6%E0%A5%8B+%E0%A4%95%E0%A4%AE%E0%A4%B0%E0%A4%BE+%E0%A4%9A%E0%A4%BE%E0%A4%B9%E0%A4%BF%E0%A4%8F", StringUtils.urlEncode("मुझे दो कमरा चाहिए"));
//    }
}
