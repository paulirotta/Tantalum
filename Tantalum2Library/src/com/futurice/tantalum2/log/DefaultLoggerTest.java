/*
 * DefaultLoggerTest.java
 * JMUnit based test
 *
 * Created on 07-Feb-2012, 16:23:10
 */

package com.futurice.tantalum2.log;


import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;

/**
 * @author phou
 */
public class DefaultLoggerTest extends TestCase {
    
    public DefaultLoggerTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(2,"DefaultLoggerTest");
    }            

    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testPrintMessage();
                break;
            case 1:
                testLog();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testPrintMessage method, of class DefaultLogger.
     */
    public void testPrintMessage() throws AssertionFailedException {
        System.out.println("printMessage");
        DefaultLogger instance = new DefaultLogger();
        String string_1 = "";
        instance.printMessage(string_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testLog method, of class DefaultLogger.
     */
    public void testLog() throws AssertionFailedException {
        System.out.println("log");
        DefaultLogger instance = new DefaultLogger();
        String tag_1 = "";
        String message_1 = "";
        instance.log(tag_1, message_1);
        fail("The test case is a prototype.");
        String tag_2 = "";
        String message_2 = "";
        Throwable th_2 = null;
        instance.log(tag_2, message_2, th_2);
        fail("The test case is a prototype.");
    }
}
