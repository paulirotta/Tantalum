package com.futurice.tantalum3.rms;

import com.futurice.tantalum3.Workable;
import com.futurice.tantalum3.Worker;
import com.futurice.tantalum3.log.L;
import com.futurice.tantalum3.util.LengthLimitedLRUVector;
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
    private static final LengthLimitedLRUVector openRecordStores = new LengthLimitedLRUVector(MAX_OPEN_RECORD_STORES) {

        protected void lengthExceeded() {
            while (openRecordStores.isLengthExceeded()) {
                RecordStore rs = null;
                String rsName = "";
                
                try {
                    synchronized (openRecordStores) {
                        if (openRecordStores.isLengthExceeded()) {
                            rs = (RecordStore) openRecordStores.removeLeastRecentlyUsed();
                        }
                    }
                    if (rs != null) {
                        rsName = rs.getName();
                        //#debug
                        L.i("Closing LRU record store", rsName + " open=" + openRecordStores.size());
                        rs.closeRecordStore();
                        //#debug
                        L.i("LRU record store closed", rsName + " open=" + openRecordStores.size());
                    }
                } catch (Exception ex) {
                    //#debug
                    L.e("Can not close LRU record store", rsName, ex);
                }
            }
        }
    };

    static {
        /**
         * Close all open record stores during shutdown
         *
         */
        Worker.queueShutdownTask(new Workable() {

            public Object exec(final Object in) {
                //#debug
                L.i("Closing record stores during shutdown", "open=" + openRecordStores.size());
                openRecordStores.setMaxLength(0);
                //#debug
                L.i("Closed record stores during shutdown", "open=" + openRecordStores.size());
                
                return in;
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
            //#debug
            L.i("Add to RMS", recordStoreName + " (" + data.length + " bytes)");
            recordStoreName = truncateRecordStoreName(recordStoreName);
            rs = getRecordStore(recordStoreName, true);

            if (rs.getNumRecords() == 0) {
                rs.addRecord(data, 0, data.length);
            } else {
                rs.setRecord(1, data, 0, data.length);
            }
            //#debug
            L.i("Added to RMS", recordStoreName + " (" + data.length + " bytes)");
        } catch (Exception e) {
            //#debug
            L.e("RMS write problem", recordStoreName, e);
            if (e instanceof RecordStoreFullException) {
                throw (RecordStoreFullException) e;
            }
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
        boolean success = false;

        try {
            rs = RecordStore.openRecordStore(recordStoreName, createIfNecessary);
            openRecordStores.addElement(rs);
            success = true;
        } catch (RecordStoreNotFoundException e) {
            success = !createIfNecessary;
            rs = null;
        } finally {
            if (!success) {
                //#debug
                L.i("Can not open record store", "Deleting " + recordStoreName);
                delete(recordStoreName);
            }
        }

        return rs;
    }

    public static void delete(String recordStoreName) {
        try {
            recordStoreName = truncateRecordStoreName(recordStoreName);
            RecordStore rs = null;
            synchronized (openRecordStores) {
                for (int i = 0; i < openRecordStores.size(); i++) {
                    rs = (RecordStore) openRecordStores.elementAt(i);
                    if (rs.getName().equals(recordStoreName)) {
                        openRecordStores.removeElementAt(i);
                        break;
                    }
                    rs = null;
                }
            }
            if (rs != null) {
                rs.closeRecordStore();
            }
            RecordStore.deleteRecordStore(recordStoreName);
        } catch (RecordStoreNotFoundException ex) {
        } catch (RecordStoreException ex) {
            //#debug
            L.e("RMS delete problem", recordStoreName, ex);
        }
    }

    public static String truncateRecordStoreName(String recordStoreName) {
        if (recordStoreName.length() > MAX_RECORD_NAME_LENGTH) {
            recordStoreName = recordStoreName.substring(0, MAX_RECORD_NAME_LENGTH);
        }

        return recordStoreName;
    }

    /**
     * Reads the data from the given record store.
     *
     * @param recordStoreName
     * @return byte[]
     */
    public static byte[] read(String recordStoreName) {
        RecordStore rs = null;
        byte[] data = null;

        try {
            recordStoreName = truncateRecordStoreName(recordStoreName);
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
        }

        return data;
    }
}
