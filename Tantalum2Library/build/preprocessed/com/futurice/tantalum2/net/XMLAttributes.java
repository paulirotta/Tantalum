/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2.net;

import java.util.Hashtable;
import org.xml.sax.Attributes;

/**
 * The SAX parser re-uses the same Attributes object internally, so a stack
 * of attributes of lower level elements can not be maintained. We here simplify
 * this a bit while still retaining a stack of underlying attributes you can
 * check if needed in your XMLVO value object parser.
 *
 * @author phou
 */
public final class XMLAttributes {
    private static final Hashtable NO_ATTRIBUTES = new Hashtable();

    private final Hashtable attributes;

    public XMLAttributes(final Attributes a) {
        final int l = a.getLength();

        if (l > 0) {
            attributes = new Hashtable(l);
            for (int i = 0; i < l; i++) {
                attributes.put(a.getQName(i), a.getValue(i));
            }
        } else {
            attributes = NO_ATTRIBUTES;
        }
    }

    public int getLength() {
        return attributes.size();
    }

    public String getValue(final String qName) {
        return (String) attributes.get(qName);
    }
}
