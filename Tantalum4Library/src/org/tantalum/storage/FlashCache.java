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
public interface FlashCache {
    /**
     * Get the data object associated with the key from persistent memory
     * 
     * @param key
     * @return
     * @throws FlashDatabaseException 
     */       
    public byte[] getData(String key) throws FlashDatabaseException;

    /**
     * Store the data object to persistent memory
     * 
     * @param key
     * @param bytes
     * @throws FlashFullException
     * @throws FlashDatabaseException 
     */
    public void putData(String key, byte[] bytes) throws FlashFullException, FlashDatabaseException;

    /**
     * Remove the data object from persistent memory
     * 
     * @param key
     * @throws FlashDatabaseException 
     */
    public void removeData(String key) throws FlashDatabaseException;

    /**
     * Provide a list of all keys for objects stored in persistent memory
     * 
     * @return
     * @throws FlashDatabaseException 
     */
    public Vector getKeys() throws FlashDatabaseException;
}
