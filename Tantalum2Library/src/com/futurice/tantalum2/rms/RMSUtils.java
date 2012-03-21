package com.futurice.tantalum2.rms;

import com.futurice.tantalum2.log.Log;
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
            try {
                RecordStore.deleteRecordStore(recordStoreName);
            } catch (RecordStoreNotFoundException recordStoreNotFoundException) {
                //ignore
            } catch (RecordStoreException recordStoreException) {
                Log.l.log("RMS delete problem", recordStoreName, recordStoreException);
            }
            rs = RecordStore.openRecordStore(recordStoreName, true);
            rs.addRecord(data, 0, data.length);
        } catch (RecordStoreFullException e) {
            Log.l.log("RMS write problem", recordStoreName, e);
            throw e;
        } catch (Exception e) {
            Log.l.log("RMS write problem", recordStoreName, e);
        } finally {
            try {
                if (rs != null) {
                    rs.closeRecordStore();
                }
            } catch (Exception e) {
            }
        }
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

    private static String truncateRecordStoreName(String recordStoreName) {
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
            rs = RecordStore.openRecordStore(recordStoreName, false);
            if (rs.getNumRecords() > 0) {
                data = rs.getRecord(1);
            }
        } catch (Exception e) {
            Log.l.log("Can not read RMS", recordStoreName, e);
        } finally {
            try {
                if (rs != null) {
                    rs.closeRecordStore();
                }
            } catch (RecordStoreException e) {
                Log.l.log("RMS close error", recordStoreName, e);
            }
        }

        return data;
    }
}
