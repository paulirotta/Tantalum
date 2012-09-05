/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3.net.xml;

import com.futurice.tantalum3.log.L;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XML Value Object is a data structure extracted from XML text. The simplified
 * parsing and integration to utility methods allows these to work online and
 * offline (using RMS) transparent to the application developer using the
 * caching classes.
 *
 * @author pahought
 */
public abstract class XMLModel extends DefaultHandler {

    protected String[] qnameStack = new String[100];
    protected String[] charStack = new String[100];
    protected XMLAttributes[] attributeStack = new XMLAttributes[100];
    protected int currentDepth;

    public synchronized void setXML(final byte[] xml) throws SAXException, IllegalArgumentException {
        if (xml == null || xml.length == 0) {
            throw new IllegalArgumentException("Attempt to XML parse a null or zero byte value");
        }
        final InputStream in = new ByteArrayInputStream(xml);

        qnameStack = new String[100];
        charStack = new String[100];
        attributeStack = new XMLAttributes[100];
        currentDepth = 0;

        try {
            //#debug
            L.i("Start parse", "length=" + xml.length);
            SAXParserFactory.newInstance().newSAXParser().parse(in, this);
            //#debug
            L.i("End parse", "length=" + xml.length);
        } catch (SAXException t) {
            //#debug
            L.e("SAX Parse error", new String(xml), t);
            throw t;
        } catch (Throwable t) {
            //#debug
            L.e("Parse error", "", t);
        } finally {
            try {
                in.close();
            } catch (Exception e) {
            }
            qnameStack = null;
            charStack = null;
            attributeStack = null;
        }
    }

    /**
     * Implement this method to store fields of interest in your value object
     * from the qnameStack, charStack, and
     *
     */
    abstract protected void parseElement(String qname, String chars, XMLAttributes attributes);

    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        qnameStack[currentDepth] = qName;
        if (attributeStack[currentDepth] == null) {
            attributeStack[currentDepth] = new XMLAttributes(attributes);
        } else {
            attributeStack[currentDepth].setAttributes(attributes);
        }
        charStack[currentDepth] = "";
        ++currentDepth;
    }

    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        charStack[currentDepth - 1] = new String(ch, start, length);
    }

    public void endElement(final String uri, final String localName, final String qname) throws SAXException {
        --currentDepth;
        parseElement(qname, charStack[currentDepth], attributeStack[currentDepth]);
    }
}
