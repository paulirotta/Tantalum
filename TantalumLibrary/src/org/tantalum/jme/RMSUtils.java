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

import java.util.Vector;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;
import org.tantalum.storage.FlashDatabaseException;
import org.tantalum.util.CryptoUtils;
import org.tantalum.util.L;
import org.tantalum.util.StringUtils;

/**
 * RMS Utility methods
 *
 * @author ssaa
 */
public final class RMSUtils {

    private static final int MAX_RECORD_NAME_LENGTH = 32;
    private static final char RECORD_HASH_PREFIX = '_';
    private static final int RADIX = 32;

    private static class RMSUtilsHolder {

        private static RMSUtils instance = new RMSUtils();
    }

    /**
     * Access the singleton
     *
     * @return the singleton
     */
    public static RMSUtils getInstance() {
        return RMSUtilsHolder.instance;
    }

    /**
     * Singleton constructor
     *
     */
    private RMSUtils() {
    }

    /**
     * Return of a list of record stores whose name indicates that they are
     * caches
     *
     * @return list of cache-related RMS names as strings
     */
    public Vector getCacheRecordStoreNames() {
        final String[] rs = RecordStore.listRecordStores();

        if (rs == null) {
            return new Vector();
        }
        final Vector v = new Vector(rs.length);

        for (int i = 0; i < rs.length; i++) {
            String name = rs[i];
            if (name.charAt(0) == RECORD_HASH_PREFIX) {
                name = name.substring(1); // Cut off initial '@'
                v.addElement(name);
            }
        }

        return v;
    }

    /**
     * Return of a list of record stores which are not part of a cache
     *
     * @return all RMS names, whether part of the cache or not
     */
    public Vector getNoncacheRecordStoreNames() {
        final String[] rs = RecordStore.listRecordStores();

        if (rs == null) {
            return new Vector();
        }
        final Vector v = new Vector(rs.length);

        for (int i = 0; i < rs.length; i++) {
            final String name = rs[i];
            if (name.charAt(0) != RECORD_HASH_PREFIX) {
                v.addElement(name);
            }
        }

        return v;
    }

    /**
     * Remove all record stores
     *
     * This is rather violent. Use only as a last resort, for example when
     * corruption is detected.
     */
    public void wipeRMS() {
        final String[] rs = RecordStore.listRecordStores();

        for (int i = 0; i < rs.length; i++) {
            try {
                RecordStore.deleteRecordStore(rs[i]);
            } catch (Exception ex) {
                //#debug
                L.e("wipeRMS(), problem deleting record store", rs[i], ex);
            }
        }
    }

    private String getRecordStoreCacheName(final char priority, final byte[] digest) {
        final StringBuffer sb = new StringBuffer(MAX_RECORD_NAME_LENGTH);

        sb.append(RECORD_HASH_PREFIX);
        sb.append(priority);
        appendDigestAsShortString(digest, sb);

        final String s = sb.toString();
        //#debug
        L.i("digest to rms cache key", StringUtils.toHex(digest) + " -> " + s);

        return s;
    }

    /**
     * The digest length must be evenly divisible by 8
     *
     * @param digest
     * @param sb
     */
    private void appendDigestAsShortString(final byte[] digest, final StringBuffer sb) {
        final long l = CryptoUtils.getInstance().bytesToLong(digest, 0);
        sb.append(Long.toString(l, RADIX));
    }

//    private String getRecordStoreCacheName(final String key) {
//        final StringBuffer sb = new StringBuffer(MAX_RECORD_NAME_LENGTH);
//
//        sb.append(RECORD_HASH_PREFIX);
//        if (key.length() > MAX_RECORD_NAME_LENGTH - 1) {
//            final String hashString = Integer.toString(key.hashCode(), Character.MAX_RADIX);
//            final int fillLength = MAX_RECORD_NAME_LENGTH - 1 - hashString.length();
//            sb.append(key.substring(0, fillLength));
//            sb.append(hashString);
//        } else {
//            // Short key, just prepend 
//            sb.append(key);
//        }
//
//        final String s = sb.toString();
//        //#debug
//        L.i("key to rms cache key", key + " -> " + s);
//
//        return s;
//    }
    /**
     * Write to the record store a cached value based on the hashcode of the key
     * to the data
     *
     * @param key
     * @param data
     * @throws RecordStoreFullException
     * @throws FlashDatabaseException
     */
    public void cacheWrite(final char priority, final byte[] digest, final byte[] data) throws RecordStoreFullException, FlashDatabaseException {
        write(getRecordStoreCacheName(priority, digest), data);
    }

    /**
     * Writes the byte array to the record store. Deletes the previous data.
     *
     * @param key
     * @param data
     * @throws RecordStoreFullException
     * @throws FlashDatabaseException
     */
    public void write(final String key, final byte[] data) throws RecordStoreFullException, FlashDatabaseException {
        RecordStore rs = null;
        final String recordStoreName = truncateRecordStoreNameToLast32(key);

        if (key == null || key.length() == 0) {
            throw new IllegalArgumentException("Can not RMSUtils.write(), null or trivial key: " + key);
        }
        if (data == null) {
            throw new IllegalArgumentException("Can not RMSUtils.write(), data is null: " + key);
        }
        try {
            //#debug
            L.i("Add to RMS", "key=" + key + " recordStoreName=" + recordStoreName + " (" + data.length + " bytes)");
            rs = getRecordStore(recordStoreName, true);
            //#debug
            L.i("Add to RMS", "recordStoreName=" + recordStoreName + " opened");
            if (rs.getNumRecords() == 0) {
                rs.addRecord(data, 0, data.length);
            } else {
                rs.setRecord(1, data, 0, data.length);
            }
            //#debug
            L.i("Added to RMS", recordStoreName + " (" + data.length + " bytes)");
        } catch (RecordStoreFullException e) {
            //#debug
            L.i("RMS FULL when writing", key + " " + recordStoreName);
            throw e;
        } catch (Exception e) {
            try {
                //#debug
                L.e("RMS write problem, will attempt to delete record", key + " " + recordStoreName, e);
                delete(key);
            } finally {
                throw new FlashDatabaseException("RMS write problem, delete was attempted: " + key + " : " + e);
            }
        } finally {
            close(key, rs);
        }
    }

    private void close(final String key, final RecordStore rs) throws FlashDatabaseException {
        if (rs != null) {
            try {
                rs.closeRecordStore();
            } catch (Exception e) {
                throw new FlashDatabaseException("RMS close problem: " + key + " : " + e);
            }
        }
    }

    /**
     * Read from the record store a cached value based on the hashcode of the
     * key to the data
     *
     * @param key
     * @return bytes stored in phone flash memory
     * @throws FlashDatabaseException
     */
    public byte[] cacheRead(final char priority, final byte[] digest) throws FlashDatabaseException {
        return read(getRecordStoreCacheName(priority, digest));
    }

    /**
     * Reads the data from the given record store.
     *
     * @param key
     * @return bytes stored in phone flash memory
     * @throws FlashDatabaseException
     */
    public byte[] read(final String key) throws FlashDatabaseException {
        RecordStore rs = null;
        final String recordStoreName = truncateRecordStoreNameToLast32(key);
        byte[] data = null;

        try {
            //#debug
            L.i("Read from RMS", recordStoreName);
            rs = getRecordStore(recordStoreName, false);
            if (rs != null && rs.getNumRecords() > 0) {
                data = rs.getRecord(1);
                //#debug
                L.i("End read from RMS", recordStoreName + " (" + data.length + " bytes)");
            } else {
                //#debug
                L.i("End read from RMS", recordStoreName + " (NOTHING TO READ)");
            }
        } catch (Exception e) {
            //#debug
            L.e("Can not read RMS", recordStoreName, e);
            throw new FlashDatabaseException("Can not read record from RMS: " + key + " - " + recordStoreName + " : " + e);
        } finally {
            close(key, rs);
        }

        return data;
    }

    /**
     * Delete one item from a cache
     *
     * @param key
     * @throws FlashDatabaseException
     */
    public void cacheDelete(final char priority, final byte[] digest) throws FlashDatabaseException {
        delete(getRecordStoreCacheName(priority, digest));
    }

    /**
     * Get a RecordStore. This method supports a global pool of open record
     * stores and thereby avoids repeated opening and closing of record stores
     * which are used several times.
     *
     * @param recordStoreName
     * @param createIfNecessary
     * @return null if the record store does not exist
     * @throws RecordStoreException
     */
    RecordStore getRecordStore(final String recordStoreName, final boolean createIfNecessary) throws FlashDatabaseException, RecordStoreNotOpenException, RecordStoreException {
        RecordStore rs = null;
        boolean success = false;

        //#debug
        L.i("getRecordStore", recordStoreName + " createIfNecessary:" + createIfNecessary);
        try {
            rs = RecordStore.openRecordStore(recordStoreName, createIfNecessary);
            //            openRecordStores.addElement(rs);

            success = true;
        } catch (RecordStoreNotFoundException e) {
            success = !createIfNecessary;
            rs = null;
        } catch (RecordStoreException e) {
            throw new FlashDatabaseException("Can not get RMS: " + recordStoreName + " : " + e);
        } finally {
            //#mdebug
            if (!success) {
                L.i("Can not open record store", recordStoreName);
            }
            //#enddebug
        }

        return rs;
    }

    /**
     * Delete one item
     *
     * @param key
     * @throws FlashDatabaseException
     */
    public void delete(final String key) throws FlashDatabaseException {
        final String truncatedRecordStoreName = truncateRecordStoreNameToLast32(key);

        try {
            RecordStore.deleteRecordStore(truncatedRecordStoreName);
        } catch (RecordStoreNotFoundException ex) {
            //#debug
            L.i("RMS not found (normal result)", key);
        } catch (RecordStoreException ex) {
            //#debug
            L.e("RMS delete problem", key, ex);
            throw new FlashDatabaseException("Can not delete RMS: " + key + " : " + ex);
        }
    }

    /**
     * Shorten the name to fit within the 32 character limit imposed by RMS.
     *
     * @param recordStoreName
     * @return (possibly) shortened string suitable for use as an RMS (file
     * system) name
     */
    private String truncateRecordStoreNameToLast32(String recordStoreName) {
        if (recordStoreName == null || recordStoreName.length() == 0) {
            throw new IllegalArgumentException("null or trivial record store name");
        }
        if (recordStoreName.length() > MAX_RECORD_NAME_LENGTH) {
            recordStoreName = recordStoreName.substring(recordStoreName.length() - MAX_RECORD_NAME_LENGTH);
        }

        return recordStoreName;
    }
}
