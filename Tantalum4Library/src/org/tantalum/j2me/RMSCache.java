/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.j2me;

import org.tantalum.log.L;
import javax.microedition.rms.RecordStoreFullException;
import org.tantalum.storage.FlashCache;
import org.tantalum.storage.FlashFullException;

/**
 *
 * @author phou
 */
public final class RMSCache implements FlashCache {

    public byte[] getData(final String key) {
        return RMSUtils.cacheRead(key);
    }

    public void putData(final String key, final byte[] bytes) throws FlashFullException {
        try {
            RMSUtils.cacheWrite(key, bytes);
        } catch (RecordStoreFullException ex) {
            L.e("RMS Full", key, ex);
            throw new FlashFullException();
        }
    }

    public void removeData(final String key) {
        RMSUtils.cacheDelete(key);
    }
}
