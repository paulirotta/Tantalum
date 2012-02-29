/*
 * XMLAttributesTest.java
 * JMUnit based test
 *
 * Created on 07-Feb-2012, 16:26:53
 */
package com.futurice.tantalum2.net.xml;

import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;

/**
 * @author phou
 */
public class XMLAttributesTest extends TestCase {
    
    public XMLAttributesTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(2, "XMLAttributesTest");
    }    
    
    public void test(int testNumber) throws Throwable {
        switch (testNumber) {
            case 0:
                testGetValue();
                break;
            case 1:
                testGetLength();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testGetValue method, of class XMLAttributes.
     */
    public void testGetValue() throws AssertionFailedException {
        System.out.println("getValue");
        XMLAttributes instance = null;
        String qName_1 = "";
        String expResult_1 = "";
        String result_1 = instance.getValue(qName_1);
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }

    /**
     * Test of testGetLength method, of class XMLAttributes.
     */
    public void testGetLength() throws AssertionFailedException {
        System.out.println("getLength");
        XMLAttributes instance = null;
        int expResult_1 = 0;
        int result_1 = instance.getLength();
        assertEquals(expResult_1, result_1);
        fail("The test case is a prototype.");
    }
}
