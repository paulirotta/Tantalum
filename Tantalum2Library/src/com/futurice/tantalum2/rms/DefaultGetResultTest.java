/*
 * DefaultGetResultTest.java
 * JMUnit based test
 *
 * Created on 07-Feb-2012, 16:29:52
 */

package com.futurice.tantalum2.rms;


import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;

/**
 * @author phou
 */
public class DefaultGetResultTest extends TestCase {
    
    public DefaultGetResultTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(3,"DefaultGetResultTest");
    }            

    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testSetResult();
                break;
            case 1:
                testRun();
                break;
            case 2:
                testGetResult();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testSetResult method, of class DefaultGetResult.
     */
    public void testSetResult() throws AssertionFailedException {
        System.out.println("setResult");
        DefaultGetResult instance = new DefaultGetResult();
        Object result_2_1 = null;
        instance.setResult(result_2_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testRun method, of class DefaultGetResult.
     */
    public void testRun() throws AssertionFailedException {
        System.out.println("run");
        DefaultGetResult instance = new DefaultGetResult();
        instance.run();
        fail("The test case is a prototype.");
    }

    /**
     * Test of testGetResult method, of class DefaultGetResult.
     */
    public void testGetResult() throws AssertionFailedException {
        System.out.println("getResult");
        DefaultGetResult instance = new DefaultGetResult();
        Object expResult_1 = null;
        Object result_1 = instance.getResult();
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }
}
