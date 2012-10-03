/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum4.storage;

import com.futurice.tantalum4.PlatformUtils;
import com.futurice.tantalum4.log.L;
import javax.microedition.rms.RecordStoreFullException;

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
