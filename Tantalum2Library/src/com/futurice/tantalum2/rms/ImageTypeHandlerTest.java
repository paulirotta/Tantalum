/*
 * ImageTypeHandlerTest.java
 * JMUnit based test
 *
 * Created on 07-Feb-2012, 16:30:05
 */
package com.futurice.tantalum2.rms;

import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;

/**
 * @author phou
 */
public class ImageTypeHandlerTest extends TestCase {
    
    public ImageTypeHandlerTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(1, "ImageTypeHandlerTest");
    }    
    
    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testConvertToUseForm();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testConvertToUseForm method, of class ImageTypeHandler.
     */
    public void testConvertToUseForm() throws AssertionFailedException {
        System.out.println("convertToUseForm");
        ImageTypeHandler instance = new ImageTypeHandler();
        byte[] bytes_1 = null;
        Object expResult_1 = null;
        Object result_1 = instance.convertToUseForm(bytes_1);
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }
}
