/*
 * LTest.java
 * JMUnit based test
 *
 * Created on 29-Feb-2012, 15:06:24
 */
package org.tantalum.util;

import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;
import org.tantalum.PlatformUtils;


/**
 * @author phou
 */
public class LTest extends TestCase {
    
    public LTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(1, "LogTest");
    }    
    
    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testLog();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testLog method, of class L.
     */
    public void testLog() throws AssertionFailedException {
        System.out.println("log");
        L instance = PlatformUtils.getLog();
        String tag_1 = "";
        String message_1 = "";
        instance.i(tag_1, message_1);
        String tag_2 = "";
        String message_2 = "";
        Throwable th_2 = new Exception("Test Exception");
        instance.e(tag_2, message_2, th_2);
    }
}
