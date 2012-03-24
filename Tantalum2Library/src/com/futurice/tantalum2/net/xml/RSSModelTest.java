/*
 * RSSModelTest.java
 * JMUnit based test
 *
 * Created on 24-Mar-2012, 14:17:12
 */

package com.futurice.tantalum2.net.xml;


import com.futurice.tantalum2.log.Log;
import com.futurice.tantalum2.util.StringUtils;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import jmunit.framework.cldc11.AssertionFailedException;
import jmunit.framework.cldc11.TestCase;
import org.xml.sax.Attributes;
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
        RSSModel instance = new RSSModel();
        try {
            instance.setXML(xml);
        } catch (Exception ex) {
            fail("Can not parse RSS: " + ex);
        }
        assertEquals("rss size", 78, instance.size());
    }
}
