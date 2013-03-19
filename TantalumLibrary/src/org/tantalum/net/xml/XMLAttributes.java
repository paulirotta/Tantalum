/*
 Copyright (c) 2013, Paul Houghton and Futurice Oy
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
package org.tantalum.net.xml;

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

    /**
     * Create a Hashtable of SAX XML parser attributes
     * 
     * @param a 
     */
    public XMLAttributes(final Attributes a) {
        final int l = a.getLength();

        for (int i = 0; i < l; i++) {
            attributes.put(a.getQName(i), a.getValue(i));
        }        
    }

    /**
     * Replace current attributes with a new value. This improves speed by
     * reducing object thrash during parsing.
     * 
     * @param a 
     */
    public void setAttributes(final Attributes a) {
        final int l = a.getLength();
        
        attributes.clear();
        for (int i = 0; i < l; i++) {
            attributes.put(a.getQName(i), a.getValue(i));
        }
    }

    /**
     * Get the number of attributes for this node
     * 
     * @return 
     */
    public int getLength() {
        return attributes.size();
    }

    /**
     * Get the value of the specified attribute, or null if it does not exist.
     * 
     * @param qName
     * @return 
     */
    public String getValue(final String qName) {
        return (String) attributes.get(qName);
    }
}
