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

import java.lang.ref.WeakReference;
import java.util.Vector;

/**
 * Objects stored in the WeakRefernce hash table may be garbage collected at any
 * time if the phone needs more memory. Explicit memory managment is also
 * supported such that any object manually remove()d from the cache is pooled
 * and can be re-used via getFromPool().
 *
 * This class can be useful for example with procedural graphics paint
 * acceleration where an re-paintable graphics object is pooled as it goes off
 * one edge of the screen and re-used for an equal sized object appearing at the
 * bottom of the screen. If sufficient memory is available, the WeakReference
 * hashtable also allows still-valid objects that were not explicitly removed
 * (such as those still on-screen) to be re-used without repainting, but if
 * other parts of the program need more memory temporarily they can bump these
 * cache objects out of memory.
 *
 * @author phou
 */
public class PoolingWeakHashCache extends WeakHashCache {

    private final Vector pool = new Vector();

    /**
     * Remove an object from the cache. It may then be placed in the pool for
     * possible re-use to minimize heap memory thrash.
     *
     * @param key
     */
    public void remove(final Object key) {
        synchronized (hash) {
            if (key == null) {
                //#debug
                L.i("PoolingWeakHashCache", "remove() with null key");
                return;
            }
            final WeakReference wr = (WeakReference) hash.get(key);

            if (wr != null) {
                hash.remove(key);
                if (wr.get() != null) {
                    //#debug
                    L.i("Adding to pool", key.toString());
                    pool.addElement(wr);
                }
            }
        }
    }

    /**
     * Get an object from the pool for re-use if one is available.
     *
     * @return - null if the pool is empty
     */
    public Object getFromPool() {
        synchronized (hash) {
            Object o = null;
            WeakReference wr;

            while (pool.size() > 0) {
                wr = (WeakReference) pool.firstElement();
                pool.removeElementAt(0);
                o = wr.get();
                if (o != null) {
                    break;
                }
            }

            return o;
        }
    }

    /**
     * Clear both the cache and the pool of re-use objects
     */
    public void clear() {
        synchronized (hash) {
            super.clear();

            pool.removeAllElements();
        }
    }
}
