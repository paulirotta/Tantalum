/*
 * LogTest.java
 * JMUnit based test
 *
 * Created on 29-Feb-2012, 15:06:24
 */
package com.futurice.tantalum2.log;

import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;


/**
 * @author phou
 */
public class LogTest extends TestCase {
    
    public LogTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(2, "LogTest");
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
     * Test of testPrintMessage method, of class Log.
     */
    public void testPrintMessage() throws AssertionFailedException {
        System.out.println("printMessage");
        Log instance = new Log();
        instance.printMessage("Test message");
    }

    /**
     * Test of testLog method, of class Log.
     */
    public void testLog() throws AssertionFailedException {
        System.out.println("log");
        Log instance = new Log();
        String tag_1 = "";
        String message_1 = "";
        instance.log(tag_1, message_1);
        String tag_2 = "";
        String message_2 = "";
        Throwable th_2 = new Exception("Test Exception");
        instance.log(tag_2, message_2, th_2);
    }
}
