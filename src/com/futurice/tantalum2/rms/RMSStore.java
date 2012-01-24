package com.futurice.tantalum2.rms;

import com.futurice.tantalum2.log.Log;
import com.futurice.tantalum2.Workable;
import com.futurice.tantalum2.Worker;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;

/**
 * Data access layer to RMS.
 * Provides store, get, and delete methods to write and read from RMS.
 * 
 * @author mark voit
 */
final class RMSStore {

    /** Log tag */
    private static final String LOG_TAG = "RMSStore";
    /** RMS doesn't allow names longer than 32 characters */
    private static final int MAX_STORAGE_KEY_NAME_LENGTH = 32;
    /** hashtable keeping all open stores */
    private Hashtable openStores;
    /** synchronized handle */
    private Object mutex = new Object();

    /**
     * Constructor. 
     */
    public RMSStore() {
        this.openStores = new Hashtable();
    }

    /**
     * Writes resource to the store.
     * 
     * @param name name of record store
     * @param intId internal id of record; should be -1 if record doesn't exist yet.
     * @param record data to be stored
     * @param listener listener to be invoked when write is complete
     */
    public void storeRecord(final String name, final int intId, final byte[] record, final RMSStoreCallbackListener listener) {
        Worker.queue(new RMSWriter(name, intId, record, listener));
    }

    /**
     * Reads record with given id from store.
     * 
     * @param name name of record store
     * @param intId internal id of record to be retrieved
     * @return found record or null
     * @throws IllegalArgumentException if name is null
     */
    public byte[] getRecord(final String name, final int intId) {

        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }

        if (intId == -1) {
            return null;
        }

        try {
            RecordStore store = openRecordStore(name);

            if (store.getNumRecords() > 0) {
                return store.getRecord(intId);
            }
        } catch (Exception e) {
            //Log.log(LOG_TAG, "ERROR Cannot read record '" + name + "':'" + intId + "'", e);
        }

        return null;
    }

    /**
     * Reads all records from store.
     * 
     * @param name name of record store
     * @return vector of byte[] or null
     * @throws IllegalArgumentException if name is null
     */
    public Vector getRecords(final String name) {

        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }

        try {
            RecordStore store = openRecordStore(name);

            if (store.getNumRecords() > 0) {

                Vector records = new Vector(store.getNumRecords());
                RecordEnumeration re = store.enumerateRecords(null, null, false);

                for (int i = 0; re.hasNextElement(); i++) {
                    records.addElement(re.nextRecord());
                }

                re.destroy(); // ????

                return records;
            }
        } catch (Exception e) {
            //Log.log(LOG_TAG, "ERROR Cannot read records for '" + name + "'", e);
        }

        return null;
    }

    /**
     * Deletes record with given id from store.
     * 
     * @param name name of record store
     * @param intId internal id of record to be deleted
     * @throws IllegalArgumentException if name is null
     */
    public void deleteRecord(final String name, final int intId) {

        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }

        try {
            RecordStore store = openRecordStore(name);

            if (store.getNumRecords() > 0) {
                store.deleteRecord(intId);
            }

        } catch (Exception e) {
            //Log.log(LOG_TAG, "ERROR Cannot delete record '" + name + "':'" + intId + "'", e);
        }
    }

    /**
     * Writes record to the store defined by its type. 
     * If intId is greater -1, existing record will be overwritten.
     * 
     * Deletes all existing records if record store becomes full.
     * 
     * @param name name of record store
     * @param intId internal id of record; should be -1 if record doesn't exist yet.
     * @param record data to be written
     * @return internal record id
     * @throws IllegalArgumentException if record is null
     * @throws IllegalArgumentException serialized data exceeds configured size limit
     * @throws RecordStoreException if record could not be stored
     */
    private int writeRecord(final String name, final int intId, final byte[] record) throws RecordStoreException {

        String debug = "-";
        RecordStore store = null;
        int id = intId;

        if (record == null) {
            throw new IllegalArgumentException("record cannot be null");
        }

        try {
            debug = "open";
            store = openRecordStore(name);

            if (id == -1) {
                // create new record
                debug = "check size";
                if (record.length > store.getSizeAvailable()) {
                    //Log.log(LOG_TAG, "record store " + name + " almost full. availble space: "+ store.getSizeAvailable());
                    throw new RecordStoreFullException("not enough available space for given record. store size: " + store.getSize()
                            + ", num records: " + store.getNumRecords() + ", record size: " + record.length);
                }

                debug = "add";
                id = store.addRecord(record, 0, record.length);
            } else {
                // update
                debug = "update";
                store.setRecord(id, record, 0, record.length);
            }

        } catch (RecordStoreFullException ex) {
            //Log.log(LOG_TAG, "Record store full!", ex);
            deleteRecordStore(name);
            throw ex;
        } catch (InvalidRecordIDException ex) {
            //Log.log(LOG_TAG, "Invalid id! " + id, ex);
            throw ex;
        } catch (Exception e) {
            //Log.log(LOG_TAG, "ERROR Cannot write record to store '" + name + "' : " + debug, e);
            throw new RecordStoreException(e.getMessage());
        }

        return id;
    }

    /**
     * Opens record store with given name. Creates a new one if needed.
     * 
     * @param name name of record store
     * @return record store
     * @throws RecordStoreException
     * @throws IllegalArgumentException if name is null or longer than 32 characters
     */
    private RecordStore openRecordStore(final String name) throws RecordStoreException {

        String debug = "-";

        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        if (name.length() > MAX_STORAGE_KEY_NAME_LENGTH) {
            throw new IllegalArgumentException("name cannot be longer than 32 characters");
        }

        synchronized (this.mutex) {

            try {
                debug = "openStores";
                RecordStore store = (RecordStore) this.openStores.get(name);
                if (store != null) {
                    return store;
                }

                debug = "RecordStore.openRecordStore";
                store = RecordStore.openRecordStore(name, true);

                debug = "put";
                this.openStores.put(name, store);

                return store;

            } catch (Throwable e) {
                //Log.log(LOG_TAG, "ERROR Cannot open RecordStore '" + name + "' : "+ debug, e);
                throw new RecordStoreException(e.getMessage());
            }
        }
    }

    /**
     * Closes given record store.
     * 
     * @param store to be closed
     */
    private void closeRecordStore(final RecordStore store) {
        try {
            if (store != null) {
                synchronized (this.mutex) {
                    this.openStores.remove(store.getName());
                }
                store.closeRecordStore();
            }
        } catch (Exception e) {
            //Log.log(LOG_TAG, "ERROR Cannot close RecordStore", e);
        }
    }

    /**
     * Closes all open record stores.
     */
    private void closeAllRecordStores() {
        Enumeration en = this.openStores.elements();
        while (en.hasMoreElements()) {
            closeRecordStore((RecordStore) en.nextElement());
        }
    }

    /**
     * Deletes given record store after closing it.
     * 
     * @param name name of store to be deleted
     */
    public void deleteRecordStore(final String name) {

        try {
            if (name != null) {
                try {
                    // get store in order to close it
                    RecordStore store = openRecordStore(name);
                    if (store != null) {
                        closeRecordStore(store);
                    }
                } catch (RecordStoreException e) {
                }

                if (name != null) {
                    //Log.log(LOG_TAG, "Deleting record store: " + name);
                    RecordStore.deleteRecordStore(name);
                }
            }
        } catch (Exception e) {
            //Log.log(LOG_TAG, "ERROR Cannot delete record store '" + name + "'", e);
        }
    }

    /**
     * Returns array of all record store names.
     * 
     * @return all record store names
     */
    public static String[] listRecordStores() {
        return RecordStore.listRecordStores();
    }

    /**
     * Closes all stores.
     */
    public void shutdown() {
        closeAllRecordStores();
        this.openStores = null;
    }

    /**
     * Runnable class for storing records to RMS.
     */
    private final class RMSWriter implements Workable {

        private String name;
        private int intId;
        private byte[] record;
        private RMSStoreCallbackListener listener;

        /**
         * @param name name of record store
         * @param intId internal id of record
         * @param record record to store
         * @param listener callback for async storing
         */
        public RMSWriter(final String name, final int intId, final byte[] record, final RMSStoreCallbackListener listener) {
            this.name = name;
            this.intId = intId;
            this.record = record;
            this.listener = listener;
        }

        public boolean work() {
            try {
                int id = writeRecord(name, intId, record);
                if (null != listener) {
                    listener.notifyRMSWriteCompleted(id);
                }
                
                return true;
            } catch (RecordStoreFullException ex) {
                Log.log("RMSWriter exception while storing resource to store '" + name + "':" + intId + " " +  ex);
                if (null != listener) {
                    listener.notifyRecordStoreFull(name, ex);
                    
                    //FIXME- we should still write the record after space is cleared
                }
            } catch (Throwable e) {
                Log.logThrowable(e, "RMSWriter exception while storing resource to store '" + name + "':" + intId);
            }
                
                return false;
        }

        public String toString() {
            return "RMSWriter[" + this.name + ":" + intId + "]";
        }
    }

    /**
     * Callback interface for asynchronous storing of records.
     */
    public interface RMSStoreCallbackListener {

        /**
         * @param intId internal id of stored record
         */
        public void notifyRMSWriteCompleted(int intId);

        /**
         * @param name name of the record store
         * @param e the error
         */
        public void notifyRecordStoreFull(String name, Throwable e);
    }
}
