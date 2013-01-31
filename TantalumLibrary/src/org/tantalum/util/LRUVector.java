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
 * Least recently used list of object
 *
 * Each object can be in the Vector a maximum of one time. Elements are always
 * added to the end of the Vector so the oldest object is at position 0.
 *
 * @author phou
 */
public class LRUVector extends Vector {

    /**
     * Create a new least-recently-used collection
     */
    public LRUVector() {
        super();
    }

    /**
     * Create a new least-recently-used collection, specifying the maximum size
     *
     * @param length
     */
    public LRUVector(final int length) {
        super(length);
    }

    /**
     * Add one element. If the element is already in the collection, it will be
     * moved to the beginning (position 0).
     *
     * @param o
     */
    public synchronized void addElement(Object o) {
        removeElement(o);
        super.addElement(o);
    }

    /**
     * Explicitly setting an element at a position is not allowed in an
     * automatically-sorted collection like this. This will always through
     * IllegalArgumentException.
     *
     * @param o
     * @param index
     */
    public void setElementAt(Object o, int index) {
        throw new IllegalArgumentException("setElementAt() not allowed on LRUVector");
    }

    /**
     * Explicitly adding an element at a position is not allowed in an
     * automatically-sorted collection like this. This will always through
     * IllegalArgumentException.
     *
     * @param o
     * @param index
     */
    public void insertElementAt(Object o, int index) {
        throw new IllegalArgumentException("insertElementAt() not allowed on LRUVector");
    }

    /**
     * Remove the element least recently accessed in the collection, thereby
     * shrinking the size.
     *
     * @return
     */
    public synchronized Object removeLeastRecentlyUsed() {
        Object o = null;

        if (size() > 0) {
            o = firstElement();
            removeElementAt(0);
        }

        return o;
    }

    /**
     * Check if the object is in the collection.
     *
     * Note that this operation will also shift the object to position 0 as it
     * counts as "use" and thus possible relevancy for access in
     * least-recently-used algorithms.
     *
     * @param o
     * @return
     */
    public synchronized boolean contains(Object o) {
        final boolean contained = super.contains(o);

        if (contained) {
            // Shift to be LRU
            addElement(o);
        }

        return contained;
    }
}
