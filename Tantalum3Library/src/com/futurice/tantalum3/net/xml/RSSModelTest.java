/*
 * RSSModelTest.java
 * JMUnit based test
 *
 * Created on 24-Mar-2012, 14:17:12
 */

package com.futurice.tantalum3.net.xml;


import com.futurice.tantalum3.util.StringUtils;
import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;
import org.xml.sax.SAXException;


/**
 * @author phou
 */
public class RSSModelTest extends TestCase {
    byte[] xml;

    
    public RSSModelTest() {
        //The first parameter of inherited constructor is the number of test cases
        super(1,"RSSModelTest");
    }            

    public void test(int testNumber) throws Throwable {
        xml = StringUtils.readBytesFromJAR("/rss.xml");
        switch (testNumber) {
            case 0:
                testParseElement();
                break;
            default:
                break;
        }
    }

    /**
     * Test of testParseElement method, of class RSSModel.
     */
    public void testParseElement() throws AssertionFailedException {
        System.out.println("parseElement");
        RSSModel instance = new RSSModel(100);
        try {
            instance.setXML(xml);
        } catch (Exception ex) {
            fail("Can not parse RSS: " + ex);
        }
        assertEquals("rss size", 86, instance.size());
        try {
            instance.setXML(null);
            fail("Should not attempt to parse null RSS");
        } catch (IllegalArgumentException e) {
            // Correct answer
        } catch (Exception ex) {
            fail("Can not parse null RSS: " + ex);
        }
        try {
            instance.setXML(new byte[0]);
            fail("Should not handle 0 byte RSS");
        } catch (IllegalArgumentException e) {
            // Correct answer
        } catch (Exception ex) {
            fail("Can not handle 0 byte RSS: " + ex);
        }
        try {
            instance.setXML(new byte[1]);
            fail("Should not handle 1 byte RSS");
        } catch (SAXException ex) {
            // Correct
        } catch (Exception e) {
            fail("Wrong exception on parse bad 1 byte RSS: " + e);
        }
    }
}
