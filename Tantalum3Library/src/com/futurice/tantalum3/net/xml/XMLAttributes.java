/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3.net.xml;

import java.util.Hashtable;
import org.xml.sax.Attributes;

/**
 * The SAX parser re-uses the same Attributes object internally, so a stack of
 * attributes of lower level elements can not be maintained. We here simplify
 * this a bit while still retaining a stack of underlying attributes you can
 * check if needed in your XMLModel value object parser.
 *
 * @author phou
 */
public final class XMLAttributes {

    private final Hashtable attributes = new Hashtable();

    public XMLAttributes(final Attributes a) {
        final int l = a.getLength();

        for (int i = 0; i < l; i++) {
            attributes.put(a.getQName(i), a.getValue(i));
        }        
    }
    
    public void setAttributes(final Attributes a) {
        final int l = a.getLength();
        
        attributes.clear();
        for (int i = 0; i < l; i++) {
            attributes.put(a.getQName(i), a.getValue(i));
        }
    }

    public int getLength() {
        return attributes.size();
    }

    public String getValue(final String qName) {
        return (String) attributes.get(qName);
    }
}
