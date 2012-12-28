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
package org.tantalum.util;

import java.util.Vector;

/**
 * A collection which calls lengthExceeded() when the designated maximum size is
 * reached. Your implementation can use this to limit growth by trimming the
 * collection.
 *
 * @author phou
 */
public abstract class LengthLimitedVector extends Vector {

    private int maxLength;

    /**
     * Create a new limited-size collection, specifying the maximum allowed
     * size.
     *
     * @param maxLength
     */
    public LengthLimitedVector(final int maxLength) {
        super(maxLength + 1);

        this.maxLength = maxLength;
    }

    /**
     * You decide what to do with the extra object each time the maximum
     * allowable length is exceeded
     *
     * @param extra
     */
    protected abstract void lengthExceeded(Object extra);

    /**
     * Add one element to the collection
     *
     * @param o
     */
    public synchronized void addElement(final Object o) {
        if (maxLength == 0) {
            throw new IllegalArgumentException("Max length of LRU vector is currently 0, can not add: " + o);
        }

        super.addElement(o);

        if (size() > maxLength) {
            final Object extra = firstElement();
            removeElementAt(0);
            lengthExceeded(extra);
        }
    }

    /**
     * Indicates if the collection is larger than expected
     *
     * @return
     */
    public synchronized final boolean isLengthExceeded() {
        return size() > maxLength;
    }

    /**
     * Remove the object and call lengthExceeded() on it, treating it as if the
     * Vector has outgrown its bounds.
     *
     * @param o
     * @return
     */
    public boolean markAsExtra(final Object o) {
        Object extra = null;

        synchronized (this) {
            final int index = this.indexOf(o);

            if (index >= 0) {
                extra = elementAt(index);
                removeElementAt(index);
            }
        }
        if (extra != null) {
            lengthExceeded(extra);
        }

        return extra != null;
    }

    /**
     * Change the maximum allowed size for the collection
     *
     * @param maxLength
     */
    public synchronized void setMaxLength(final int maxLength) {
        this.maxLength = maxLength;
        while (size() > maxLength) {
            final Object extra = firstElement();
            removeElementAt(0);
            lengthExceeded(extra);
        }
    }
}
