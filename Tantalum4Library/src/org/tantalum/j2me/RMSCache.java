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
public final class RMSCache implements FlashCache {

    public byte[] getData(final String key) throws FlashDatabaseException {
        return RMSUtils.cacheRead(key);
    }

    public void putData(final String key, final byte[] bytes) throws FlashFullException, FlashDatabaseException {
        try {
            RMSUtils.cacheWrite(key, bytes);
        } catch (RecordStoreFullException ex) {
            L.e("RMS Full", key, ex);
            throw new FlashFullException("key = " + key + " : " + ex);
        }
    }

    public void removeData(final String key) {
        RMSUtils.cacheDelete(key);
    }

    public Vector getKeys() throws FlashDatabaseException {
        return RMSUtils.getCachedRecordStoreNames();
    }
}
