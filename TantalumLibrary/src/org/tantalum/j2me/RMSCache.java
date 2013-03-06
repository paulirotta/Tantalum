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
package org.tantalum.j2me;

import java.util.Vector;
import javax.microedition.rms.RecordStoreFullException;
import org.tantalum.storage.FlashCache;
import org.tantalum.storage.FlashDatabaseException;
import org.tantalum.storage.FlashFullException;
import org.tantalum.util.L;

/**
 * Persistent storage implementation for J2ME
 *
 * @author phou
 */
public final class RMSCache extends FlashCache {
    public RMSCache(final char priority) {
        super(priority);
    }
    
    /**
     * Get the key with the priority prepended to create a key that is uniquely
     * stamped as belonging to this cache.
     *
     * The cache implementation is a flat hashtable structure. The key must thus
     * be marked to indicate the "path" of the specific cache to which it
     * belongs.
     *
     * @param key
     * @return
     */
    private String getPriorityKey(final String key) {
        return priority + key;
    }

    /**
     * Remove the priority from the beginning of the key
     *
     * @param priorityKey
     * @return
     */
    private String getKeyPriorityStripped(final String priorityKey) {
        return priorityKey.substring(1);
    }

    /**
     * Get one item from flash memory
     *
     * @param key
     * @return
     * @throws FlashDatabaseException
     */
    public byte[] getData(String key) throws FlashDatabaseException {
        key = getPriorityKey(key);

        return RMSUtils.getInstance().cacheRead(key);
    }

    /**
     * Add one item to flash memory
     *
     * @param key
     * @param bytes
     * @throws FlashFullException
     * @throws FlashDatabaseException
     */
    public void putData(String key, final byte[] bytes) throws FlashFullException, FlashDatabaseException {
        key = getPriorityKey(key);

        try {
            RMSUtils.getInstance().cacheWrite(key, bytes);
        } catch (RecordStoreFullException ex) {
            L.e("RMS Full", key, ex);
            throw new FlashFullException("key = " + key + " : " + ex);
        }
    }

    /**
     * Remove one item from flash memory
     *
     * @param key
     */
    public void removeData(String key) {
        key = getPriorityKey(key);

        RMSUtils.getInstance().cacheDelete(key);
    }

    /**
     * Get a list of flash memory cache contents
     *
     * @return
     * @throws FlashDatabaseException
     */
    public Vector getKeys() throws FlashDatabaseException {
        final Vector keys = RMSUtils.getInstance().getCachedRecordStoreNames();
        final String prefix = String.valueOf(priority);

        for (int i = keys.size() - 1; i >= 0; i--) {
            final String key = (String) keys.elementAt(i);

            if (key.startsWith(prefix)) {
                keys.setElementAt(getKeyPriorityStripped(key), i);
            } else {
                /**
                 * From some other RMS cache, but not the same priority tag
                 */
                keys.removeElementAt(i);
            }
        }

        return keys;
    }

    /**
     * Clear everything from flash memory for this specific cache
     */
    public void clear() {
        try {
            //#debug
            L.i("Clear RMS cache", "" + priority);

            final Vector keys = getKeys();

            for (int i = keys.size() - 1; i >= 0; i--) {
                final String key = (String) keys.elementAt(i);

                try {
                    //#debug
                    L.i("Clear RMS, remove one item, cache priority=" + priority, "key=" + key);
                    removeData(key);
                } catch (Exception e) {
                    //#debug
                    L.e("Problem during clear() of RMSCache, will continue to try next item", key, e);
                }
            }
        } catch (FlashDatabaseException e) {
            //#debug
            L.e("Problem during clear() of RMSCache- aborting clear()", "cache priority=" + priority, e);
            RMSUtils.getInstance().wipeRMS();
        }
    }
}
