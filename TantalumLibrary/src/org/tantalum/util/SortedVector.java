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
 * A collection which continuously maintains sort order
 *
 * @author phou
 */
public class SortedVector extends Vector {

    private final Comparator comparator;

    /**
     * Create a new sorted collection
     *
     * @param comparator
     */
    public SortedVector(final Comparator comparator) {
        this.comparator = comparator;
    }

    /**
     * Add one element to the continuously-sorted collection
     *
     * @param o
     */
    public synchronized void addElement(final Object o) {
        int insertIndex = size(); /* Future index of o in the SortedVector? */
        /* Finding the index */
        for (int i = size() - 1; i >= 0; i--) {
            if (comparator.before(o, elementAt(i))) {
                insertIndex--;
            } else {
                break;
            }
        }

        /* Inserting the object */
        if (insertIndex == size()) {
            super.addElement(o);
        } else {
            super.insertElementAt(o, insertIndex);
        }
    }

    /**
     * Add an element at a specified location is not allowed.
     *
     * This method will always return IllegalArgumentException
     *
     * @param o
     * @param index
     */
    public final void insertElementAt(Object o, int index) {
        throw new IllegalArgumentException("insertElementAt() not supported");
    }

    /**
     * Replace an element at a specified location is not allowed.
     *
     * This method will always return IllegalArgumentException
     *
     * @param o
     * @param index
     */
    public final void setElementAt(Object o, int index) {
        throw new IllegalArgumentException("setElementAt() not supported");
    }

    /**
     * Test for equivalence including having an equivalent Comparator
     *
     * @param o
     * @return
     */
    public boolean equals(Object o) {
        return super.equals(o) && o instanceof SortedVector && this.comparator.equals(((SortedVector) o).comparator);
    }

    /**
     * Generate a hash based on the underlying Vector hash
     *
     * @return
     */
    public int hashCode() {
        return super.hashCode() ^ comparator.hashCode();
    }

    /**
     * A helper class to indicate the sort order for the Vector with the given
     * current data type
     *
     */
    public abstract static class Comparator {

        /**
         * Return true of o1 should be before o1 in the ordering imposed by this
         * Comparator
         *
         * @param o1
         * @param o2
         * @return
         */
        public abstract boolean before(Object o1, Object o2);
    }
}
