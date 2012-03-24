/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2.net.xml;

import com.futurice.tantalum2.log.Log;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
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

    /**
     * Null constructor is an empty placeholder
     */
    public XMLModel() {
    }

    public synchronized void setXML(final byte[] xml) throws ParserConfigurationException, SAXException, IOException {
        final InputStream in = new ByteArrayInputStream(xml);

        qnameStack = new String[100];
        charStack = new String[100];
        attributeStack = new XMLAttributes[100];
        currentDepth = 0;

        try {
            Log.l.log("Start parse", "");
            SAXParserFactory.newInstance().newSAXParser().parse(in, this);
            Log.l.log("End parse", "");
        } catch (Throwable t) {
            Log.l.log("Parse error", "", t);
        } finally {
            in.close();
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
