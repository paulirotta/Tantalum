/*
 * JSONGetterTest.java
 * JMUnit based test
 *
 * Created on May 25, 2012, 9:50:42 PM
 */
package com.futurice.tantalum3.net.json;

import jmunit.framework.cldc11.*;

/**
 * @author phou
 */
public class JSONGetterTest extends TestCase {
    
    public JSONGetterTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(1, "JSONGetterTest");
    }    
    
    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testSetResult();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testSetResult method, of class JSONGetter.
     */
    public void testSetResult() throws AssertionFailedException {
        System.out.println("setResult");
        JSONGetter instance = null;
        Object o_1 = null;
        instance.setResult(o_1);
        fail("The test case is a prototype.");
    }
}
