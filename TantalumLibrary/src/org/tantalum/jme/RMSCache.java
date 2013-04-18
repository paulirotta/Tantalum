/*
 Copyright (c) 2013, Paul Houghton and Futurice Oy
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
package org.tantalum.jme;

import java.io.UnsupportedEncodingException;
import java.security.DigestException;
import java.util.Vector;
import javax.microedition.rms.RecordStoreFullException;
import org.tantalum.storage.FlashCache;
import org.tantalum.storage.FlashDatabaseException;
import org.tantalum.storage.FlashFullException;
import org.tantalum.util.CryptoUtils;
import org.tantalum.util.L;

/**
 * Persistent storage implementation for JME
 *
 * You should not access this class directly. Use
 * <code>PlatformUtils.getInstance()...</code> instead.
 *
 * @author phou
 */
public final class RMSCache {
//extends FlashCache {
//
//    /**
//     * Create a new RMSCache. You should not call this method directly, but
//     * rather request the cache from
//     * <code>PlatformUtils.getInstance().getFlashCache(priority)</code>
//     *
//     * @param priority
//     */
//    public RMSCache(final char priority) {
//        super(priority);
//    }
//
//    /**
//     * Remove the priority from the beginning of the key
//     *
//     * @param priorityKey
//     * @return
//     */
//    private String getKeyPriorityStripped(final String priorityKey) {
//        return priorityKey.substring(1);
//    }
//
//    /**
//     *
//     * @param key
//     * @return
//     * @throws FlashDatabaseException
//     */
//    public byte[] get(final byte[] digest) throws FlashDatabaseException {
//        return RMSUtils.getInstance().cacheRead(priority, digest);
//    }
//
//    /**
//     * Add one item to flash memory
//     *
//     * @param key
//     * @param bytes
//     * @throws FlashFullException
//     * @throws FlashDatabaseException
//     */
//    public void put(final String key, final byte[] bytes) throws FlashFullException, FlashDatabaseException, DigestException, UnsupportedEncodingException {
//        try {
//            final byte[] digest = toDigest(key);
//            RMSUtils.getInstance().cacheWrite(priority, digest, bytes);
//        } catch (RecordStoreFullException ex) {
//            throw new FlashFullException("key = " + key + " : " + ex);
//        }
//    }
//
//    /**
//     * Remove one item from flash memory
//     *
//     * @param key
//     */
//    public void removeData(final byte[] digest) throws FlashDatabaseException {
//        RMSUtils.getInstance().cacheDelete(priority, digest);
//    }
//
//    /**
//     * Get a list of flash memory cache contents
//     *
//     * To save memory we a list of cryptographic digests of the index string of
//     * each element
//     *
//     * @return
//     * @throws DigestException
//     * @throws FlashDatabaseException 
//     */
//    public long[] getDigests() throws DigestException, FlashDatabaseException {
//        final Vector keys = RMSUtils.getInstance().getCacheRecordStoreNames();
//        final String prefix = String.valueOf(priority);
//
//        for (int i = keys.size() - 1; i >= 0; i--) {
//            final String key = (String) keys.elementAt(i);
//
//            if (key.startsWith(prefix)) {
//                keys.setElementAt(getKeyPriorityStripped(key), i);
//            } else {
//                /**
//                 * From some other RMS cache, but not the same priority tag
//                 */
//                keys.removeElementAt(i);
//            }
//        }
//        final long[] dig = new long[keys.size()];
//        for (int i = 0; i < dig.length; i++) {
//            try {
//                dig[i] = CryptoUtils.getInstance().toDigest((String) keys.elementAt(i));
//            } catch (UnsupportedEncodingException ex) {
//               adf
//            }
//        }
//
//        return dig;
//    }
//
//    /**
//     * Clear everything from flash memory for this specific cache
//     */
//    public void clear() {
//        try {
//            //#debug
//            L.i("Clear RMS cache", "" + priority);
//
//            final byte[][] digests = getDigests();
//
//            for (int i = digests.length - 1; i >= 0; i--) {
//                final byte[] digest = digests[i];
//                final String key = toString(digest);
//
//                try {
//                    //#debug
//                    L.i("Clear RMS, remove one item, cache priority=" + priority, "key=" + key);
//                    removeData(key);
//                } catch (Exception e) {
//                    //#debug
//                    L.e("Problem during clear() of RMSCache, will continue to try next item", key, e);
//                }
//            }
//        } catch (Exception e) {
//            //#debug
//            L.e("Problem during clear() of RMSCache- aborting clear()", "cache priority=" + priority, e);
//            RMSUtils.getInstance().wipeRMS();
//        }
//    }
//
//    public String toString(final byte[] digest) throws FlashDatabaseException, UnsupportedEncodingException {
//        return new String(digest, "UTF-8");
//    }
}
