package com.futurice.tantalum2.rms;

import com.futurice.tantalum2.Workable;
import com.futurice.tantalum2.Worker;
import com.futurice.tantalum2.log.Log;
import com.futurice.tantalum2.util.LengthLimitedLRUVector;
import java.util.Vector;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;

/**
 * RMS Utility methods
 *
 * @author ssaa
 */
public class RMSUtils {

    private static final int MAX_RECORD_NAME_LENGTH = 32;
    private static final int MAX_OPEN_RECORD_STORES = 10;
    private static final String RECORD_HASH_PREFIX = "@";
    private static final LengthLimitedLRUVector openRecordStores = new OpenRecordStores(MAX_OPEN_RECORD_STORES);

    private static final class OpenRecordStores extends LengthLimitedLRUVector {

        OpenRecordStores(final int maxLength) {
            super(maxLength);
        }

        protected void lengthExceeded() {
            closeLeastRecentlyUsed();
        }
    };

    private static boolean closeLeastRecentlyUsed() {
        final RecordStore oldest = (RecordStore) openRecordStores.removeLeastRecentlyUsed();

        try {
            if (oldest != null) {
                oldest.closeRecordStore();
            }
        } catch (RecordStoreException ex) {
            Log.l.log("Can not close LRU record store", "", ex);
        }

        return oldest != null;
    }

    static {
        /**
         * Close all open record stores during shutdown
         *
         */
        Worker.queueShutdownTask(new Workable() {

            public boolean work() {
                while (closeLeastRecentlyUsed());

                return false;
            }
        });
    }

    public static Vector getCachedRecordStoreNames() {
        final String[] rs = RecordStore.listRecordStores();
        final Vector v = new Vector(rs.length);

        for (int i = 0; i < rs.length; i++) {
            if (rs[i].startsWith(RECORD_HASH_PREFIX)) {
                v.addElement(rs[i]);
            }
        }

        return v;
    }

    private static String getRecordStoreCacheName(final String key) {
        final StringBuffer sb = new StringBuffer(MAX_RECORD_NAME_LENGTH);

        sb.append(RECORD_HASH_PREFIX);
        sb.append(Integer.toString(key.hashCode(), Character.MAX_RADIX));
        final int fullLength = sb.length() + key.length();
        if (fullLength > MAX_RECORD_NAME_LENGTH) {
            sb.append(key.substring(fullLength - MAX_RECORD_NAME_LENGTH));
        } else {
            sb.append(key);
        }

        return sb.toString();
    }

    /**
     * Write to the record store a cached value based on the hashcode of the key
     * to the data
     *
     * @param keyHash
     * @param data
     * @throws RecordStoreFullException
     */
    public static void cacheWrite(final String key, final byte[] data) throws RecordStoreFullException {
        write(getRecordStoreCacheName(key), data);
    }

    /**
     * Read from the record store a cached value based on the hashcode of the
     * key to the data
     *
     * @param keyHash
     * @param data
     * @return
     * @throws RecordStoreFullException
     */
    public static byte[] cacheRead(final String key) {
        return read(getRecordStoreCacheName(key));
    }

    public static void cacheDelete(final String key) {
        delete(getRecordStoreCacheName(key));
    }

    /**
     * Writes the byte array to the record store. Deletes the previous data.
     *
     * @param recordStoreName
     * @param data
     */
    public static void write(String recordStoreName, final byte[] data) throws RecordStoreFullException {
        RecordStore rs = null;

        try {
            //delete old value
            Log.l.log("Add to RMS", recordStoreName + " (" + data.length + " bytes)");
            recordStoreName = truncateRecordStoreName(recordStoreName);
//            try {
//                RecordStore.deleteRecordStore(recordStoreName);
//            } catch (RecordStoreNotFoundException recordStoreNotFoundException) {
//                //ignore
//            } catch (RecordStoreException recordStoreException) {
//                Log.l.log("RMS delete problem", recordStoreName, recordStoreException);
//            }
            rs = getRecordStore(recordStoreName, true);

            if (rs.getNumRecords() == 0) {
                rs.addRecord(data, 0, data.length);
            } else {
                rs.setRecord(1, data, 0, data.length);
            }
            Log.l.log("Added to RMS", recordStoreName + " (" + data.length + " bytes)");
        } catch (RecordStoreFullException e) {
            Log.l.log("RMS write problem", recordStoreName, e);
            throw e;
        } catch (Exception e) {
            Log.l.log("RMS write problem", recordStoreName, e);
//        } finally {
//            try {
//                if (rs != null) {
//                    rs.closeRecordStore();
//                }
//            } catch (Exception e) {
//            }
        }
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
    public static RecordStore getRecordStore(final String recordStoreName, final boolean createIfNecessary) throws RecordStoreException {
        RecordStore rs = null;

        try {
            rs = RecordStore.openRecordStore(recordStoreName, createIfNecessary);
            openRecordStores.addElement(rs);
        } catch (RecordStoreNotFoundException e) {
        }

        return rs;
    }

    public static void delete(String recordStoreName) {
        try {
            recordStoreName = truncateRecordStoreName(recordStoreName);
            RecordStore.deleteRecordStore(recordStoreName);
        } catch (RecordStoreNotFoundException ex) {
        } catch (RecordStoreException ex) {
            Log.l.log("RMS delete problem", recordStoreName, ex);
        }
    }

    public static String truncateRecordStoreName(String recordStoreName) {
        if (recordStoreName.length() > MAX_RECORD_NAME_LENGTH) {
            recordStoreName = recordStoreName.substring(0, MAX_RECORD_NAME_LENGTH);
        }

        return recordStoreName;
    }

    /**
     * Reads the data from the given recordstore.
     *
     * @param recordStoreName
     * @return byte[]
     */
    public static byte[] read(String recordStoreName) {
        RecordStore rs = null;
        byte[] data = null;

        try {
            recordStoreName = truncateRecordStoreName(recordStoreName);
            Log.l.log("Read from RMS", recordStoreName);
            rs = getRecordStore(recordStoreName, false);
            if (rs != null && rs.getNumRecords() > 0) {
                data = rs.getRecord(1);
                Log.l.log("End read from RMS", recordStoreName + " (" + data.length + " bytes)");
            } else {
                Log.l.log("End read from RMS", recordStoreName + " (NOTHING TO READ)");
            }
        } catch (Exception e) {
            Log.l.log("Can not read RMS", recordStoreName, e);
//        } finally {
//            try {
//                if (rs != null) {
//                    rs.closeRecordStore();
//                }
//            } catch (RecordStoreException e) {
//                Log.l.log("RMS close error", recordStoreName, e);
//            }
        }

        return data;
    }
}
