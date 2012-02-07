/*
 * HttpGetterTest.java
 * JMUnit based test
 *
 * Created on 07-Feb-2012, 16:25:08
 */
package com.futurice.tantalum2.net;

import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;

/**
 * @author phou
 */
public class HttpGetterTest extends TestCase {
    
    public HttpGetterTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(2, "HttpGetterTest");
    }    
    
    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testGetUrl();
                break;
            case 1:
                testWork();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testGetUrl method, of class HttpGetter.
     */
    public void testGetUrl() throws AssertionFailedException {
        System.out.println("getUrl");
        HttpGetter instance = null;
        String expResult_1 = "";
        String result_1 = instance.getUrl();
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testWork method, of class HttpGetter.
     */
    public void testWork() throws AssertionFailedException {
        System.out.println("work");
        HttpGetter instance = null;
        boolean expResult_1 = false;
        boolean result_1 = instance.work();
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }
}
