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
package org.tantalum.storage;

import java.util.Vector;

/**
 * A Hashtable-style interface for persistent data.
 *
 * Each implementation is platform-specific (J2ME, Android, ...)
 *
 * @author phou
 */
public abstract class FlashCache {

    /**
     * A unique local identifier for the cache.
     */
    public char priority = Character.MIN_VALUE;

    /**
     * The priority must be unique in the application.
     *
     * Caches with lower priority are garbage collected to save space before
     * caches with higher priority.
     *
     * @param priority
     */
    public void init(final char priority) {
        if (this.priority != Character.MIN_VALUE) {
            throw new IllegalStateException("FlashCache.init() can only be called once");
        }
        this.priority = priority;
    }
    
    /**
     * Get the data object associated with the key from persistent memory
     *
     * @param key
     * @return
     * @throws FlashDatabaseException
     */
    public abstract byte[] getData(String key) throws FlashDatabaseException;

    /**
     * Store the data object to persistent memory
     *
     * @param key
     * @param bytes
     * @throws FlashFullException
     * @throws FlashDatabaseException
     */
    public abstract void putData(String key, byte[] bytes) throws FlashFullException, FlashDatabaseException;

    /**
     * Remove the data object from persistent memory
     *
     * @param key
     * @throws FlashDatabaseException
     */
    public abstract void removeData(String key) throws FlashDatabaseException;

    /**
     * Provide a list of all keys for objects stored in persistent memory
     *
     * @return
     * @throws FlashDatabaseException
     */
    public abstract Vector getKeys() throws FlashDatabaseException;
    
    /**
     * Remove all items from this flash cache
     * 
     */
    public abstract void clear();
}
