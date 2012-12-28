/*
 Copyright © 2012 Paul Houghton and Futurice on behalf of the Tantalum Project.
 All rights reserved.

 Tantalum software shall be used to make the world a better place for everyone.

 This software is licensed for use under the Apache 2 open source software license,
 http://www.apache.org/licenses/LICENSE-2.0.html

 You are kindly requested to return your improvements to this library to the
 open source community at http://projects.developer.nokia.com/Tantalum

 The above copyright and license notice notice shall be included in all copies
 or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
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
