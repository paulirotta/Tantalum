/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.j2me;

import java.util.Vector;
import javax.microedition.rms.RecordStoreFullException;
import org.tantalum.storage.FlashCache;
import org.tantalum.storage.FlashDatabaseException;
import org.tantalum.storage.FlashFullException;
import org.tantalum.util.L;

/**
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
