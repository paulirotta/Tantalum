/*
 * Tantalum Mobile Toolset
 * https://projects.forum.nokia.com/Tantalum
 *
 * Special thanks to http://www.futurice.com for support of this project
 * Project lead: paul.houghton@futurice.com
 *
 * Copyright 2010 Paul Eugene Houghton
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.futurice.tantalum;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import javax.xml.parsers.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Paul Eugene Houghton
 */
public abstract class XMLVO extends DefaultHandler {
    final private Vector qnameStack = new Vector();
    final private Vector charStack = new Vector();
    final private Vector attributeStack = new Vector();

    /**
     * Null constructor is an empty placeholder
     */
    public XMLVO() {
    }

    public synchronized void setXML(String xml) throws ParserConfigurationException, SAXException, IOException {
        final SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        final InputStream in = new ByteArrayInputStream(xml.getBytes());

        try {
            parser.parse(in, this);
        } catch (Throwable t) {
            Log.logThrowable(t, "parser error");
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

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        qnameStack.addElement(qName);
        attributeStack.addElement(attributes);
        charStack.addElement("");
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        final String chars = new String(ch, start, length);
        charStack.setElementAt(chars, charStack.size() - 1);
        Log.log("chars: " + chars);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        element(charStack, qnameStack, attributeStack); 
        qnameStack.removeElementAt(qnameStack.size() - 1);
        attributeStack.removeElementAt(attributeStack.size() - 1);
        charStack.removeElementAt(charStack.size() - 1);
    }
}
