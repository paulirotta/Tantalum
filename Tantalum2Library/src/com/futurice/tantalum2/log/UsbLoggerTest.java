/*
 * UsbLoggerTest.java
 * JMUnit based test
 *
 * Created on 07-Feb-2012, 16:23:33
 */
package com.futurice.tantalum2.log;

import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;

/**
 * @author phou
 */
public class UsbLoggerTest extends TestCase {
    
    public UsbLoggerTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(2, "UsbLoggerTest");
    }    
    
    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testPrintMessage();
                break;
            case 1:
                testShutdown();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testPrintMessage method, of class UsbLogger.
     */
    public void testPrintMessage() throws AssertionFailedException {
        System.out.println("printMessage");
        UsbLogger instance = new UsbLogger();
        String string_1 = "";
        instance.printMessage(string_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testShutdown method, of class UsbLogger.
     */
    public void testShutdown() throws AssertionFailedException {
        System.out.println("shutdown");
        UsbLogger instance = new UsbLogger();
        instance.shutdown();
        fail("The test case is a prototype.");
    }
}
