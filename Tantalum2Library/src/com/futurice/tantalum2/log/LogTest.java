/*
 * LogTest.java
 * JMUnit based test
 *
 * Created on 07-Feb-2012, 16:23:16
 */
package com.futurice.tantalum2.log;

import javax.microedition.midlet.MIDlet;
import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;

/**
 * @author phou
 */
public class LogTest extends TestCase {
    
    public LogTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(4, "LogTest");
    }    
    
    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testLogNonfatalThrowable();
                break;
            case 1:
                testLog();
                break;
            case 2:
                testLogThrowable();
                break;
            case 3:
                testInit();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testLogNonfatalThrowable method, of class Log.
     */
    public void testLogNonfatalThrowable() throws AssertionFailedException {
        System.out.println("logNonfatalThrowable");
        Throwable t_1 = null;
        String message_1 = "";
        Log.logNonfatalThrowable(t_1, message_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testLog method, of class Log.
     */
    public void testLog() throws AssertionFailedException {
        System.out.println("log");
        String message_1 = "";
        Log.log(message_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testLogThrowable method, of class Log.
     */
    public void testLogThrowable() throws AssertionFailedException {
        System.out.println("logThrowable");
        Throwable t_1 = null;
        String message_1 = "";
        Log.logThrowable(t_1, message_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testInit method, of class Log.
     */
    public void testInit() throws AssertionFailedException {
        System.out.println("init");
        MIDlet midlet_1 = null;
        Log.init(midlet_1);
        fail("The test case is a prototype.");
    }
}
