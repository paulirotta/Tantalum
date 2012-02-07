/*
 * JSONGetterTest.java
 * JMUnit based test
 *
 * Created on 07-Feb-2012, 15:09:28
 */
package com.futurice.tantalum2.net.json;

import com.futurice.tantalum2.net.HttpGetter;
import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;

/**
 * @author phou
 */
public class JSONGetterTest extends TestCase {
    
    public JSONGetterTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(5, "JSONGetterTest");
    }    
    
    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testSetHttpGetter();
                break;
            case 1:
                testSetResult();
                break;
            case 2:
                testWork();
                break;
            case 3:
                testException();
                break;
            case 4:
                testGetResult();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testSetHttpGetter method, of class JSONGetter.
     */
    public void testSetHttpGetter() throws AssertionFailedException {
        System.out.println("setHttpGetter");
        JSONGetter instance = null;
        HttpGetter httpGetter_1 = null;
        instance.setHttpGetter(httpGetter_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testSetResult method, of class JSONGetter.
     */
    public void testSetResult() throws AssertionFailedException {
        System.out.println("setResult");
        JSONGetter instance = null;
        Object result_2_1 = null;
        instance.setResult(result_2_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testWork method, of class JSONGetter.
     */
    public void testWork() throws AssertionFailedException {
        System.out.println("work");
        JSONGetter instance = null;
        boolean expResult_1 = false;
        boolean result_1 = instance.work();
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testException method, of class JSONGetter.
     */
    public void testException() throws AssertionFailedException {
        System.out.println("exception");
        JSONGetter instance = null;
        Exception e_1 = null;
        instance.exception(e_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testGetResult method, of class JSONGetter.
     */
    public void testGetResult() throws AssertionFailedException {
        System.out.println("getResult");
        JSONGetter instance = null;
        Object expResult_1 = null;
        Object result_1 = instance.getResult();
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }
}
