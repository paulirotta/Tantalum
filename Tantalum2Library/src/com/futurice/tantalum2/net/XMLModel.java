/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2.net;

import com.futurice.tantalum2.log.Log;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import javax.xml.parsers.*;
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
    final private Vector qnameStack = new Vector();
    final private Vector charStack = new Vector();
    final private Vector attributeStack = new Vector();

    /**
     * Null constructor is an empty placeholder
     */
    public XMLModel() {
    }

    public synchronized void setXML(final String xml) throws ParserConfigurationException, SAXException, IOException {
        final InputStream in = new ByteArrayInputStream(xml.getBytes());

        try {
            SAXParserFactory.newInstance().newSAXParser().parse(in, this);
        } catch (Throwable t) {
            Log.logThrowable(t, "parser error: " + xml);
        } finally {
            in.close();
        }
    }

    /**
     * Implement this method to store fields of interest in your value object
     *
     * @param chars
     * @param qnameStack
     * @param attributeStack
     */
    abstract protected void element(Vector charStack, Vector qnameStack, Vector attributeStack);

    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        qnameStack.addElement(qName);
        attributeStack.addElement(new XMLAttributes(attributes));
        charStack.addElement("");
    }

    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        final String chars = new String(ch, start, length);
        charStack.setElementAt(chars, charStack.size() - 1);
    }

    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        element(charStack, qnameStack, attributeStack); 
        qnameStack.removeElementAt(qnameStack.size() - 1);
        attributeStack.removeElementAt(attributeStack.size() - 1);
        charStack.removeElementAt(charStack.size() - 1);
    }
}
