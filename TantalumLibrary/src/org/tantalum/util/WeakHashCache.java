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
import java.util.Hashtable;

/**
 * This is a hashtable which acts as a heap memory cache using WeakReference.
 *
 * Objects in the hashtable are not held in memory, they may be garbage
 * collected at any time, in which case the calling routine must do something
 * else to recreate the value. You can freely put as many objects in this cache
 * as you like, but do not store things of no interest as the objects will
 * displace more useful data.
 *
 * If an object is garbage collected, it will not be removed from the hash cache
 * automatically, so your app can count on this is a stable list of objects. You
 * may choose to manually remove references which will no longer be of interest
 * according to application logic.
 *
 * Since you do not have any control over which weak references are destroyed,
 * your application should be prepared to re-create data stored in this cache
 * without too much cost to user responsiveness.
 *
 * Placing a null object into the cache is the same as removing it- null values
 * are not stored as useful.
 *
 * @author phou
 */
public class WeakHashCache {

    /**
     * A Hashtable of WeakReference objects.
     */
    protected final Hashtable hash = new Hashtable();

    /**
     * Get the object associated with this key
     *
     * @param key
     * @return - null if the object is not stored, or if the WeakReference has
     * been garbage collected by the virtual machine.
     */
    public Object get(final Object key) {
        Object o = null;
        final WeakReference reference = (WeakReference) hash.get(key);

        if (reference != null) {
            o = reference.get();
        }

        return o;
    }

    /**
     * Put an object into the heap memory cache.
     *
     * Note that you can feel free to put a very large number of objects into
     * the cache, thereby simplifying your algorithms and their scalability
     * between small memory and large memory architectures. If memory is low,
     * the virtual machine will remove something from this or another
     * WeakReference cache to make room. The only overhead of having a very
     * large amount of data (some of it garbage collected) in the cache is the
     * relatively small WeakReference objects and Hashtable key hashcodes
     * themselves.
     *
     * @param key
     * @param value
     */
    public void put(final Object key, final Object value) {
        if (key == null) {
            throw new IllegalArgumentException("null key put to WeakHashCache");
        }
        synchronized (hash) {
            if (value == null) {
                //#debug
                L.i("WeakHash put", "value is null, key removed");
                hash.remove(key);
                return;
            }
            hash.put(key, new WeakReference(value));
        }
    }

    /**
     * Remove the object from the cache
     *
     * @param key
     */
    public void remove(final Object key) {
        if (key != null) {
            hash.remove(key);
        } else {
            //#debug
            L.i("WeakHashCache", "remove() with null key");
        }
    }

    /**
     * Indicate if the cache contains the given key.
     *
     * Note that the object itself may have been garbage collected and no longer
     * be in the cache. Testing this "deep contains" is done by get(). But it is
     * useful that keys never are garbage collected, and so the set of key
     * hashcodes is a useful test of membership in a collection.
     *
     * @param key
     * @return
     */
    public boolean containsKey(final Object key) {
        if (key == null) {
            throw new IllegalArgumentException("containsKey() with null key");
        }

        return hash.containsKey(key);
    }

    /**
     * The number of keys in the collection.
     *
     * The number of still-available objects in the collection can not be known
     * with certainty at any given instant. You could walk the entire cache, but
     * by the time you reach the end the count may no longer be valid due to VM
     * garbage collection.
     *
     * @return
     */
    public int size() {
        return hash.size();
    }

    /**
     * Empty the collection.
     *
     * This does not free a great deal of memory, but it does free the overhead
     * structure associated with each collection element.
     */
    public void clear() {
        hash.clear();
    }
}
