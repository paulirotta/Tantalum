package com.futurice.tantalum2.rms;

import com.futurice.tantalum2.log.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.futurice.tantalum2.rms.RMSStore.RMSStoreCallbackListener;
import javax.microedition.rms.RecordStore;

/**
 * Data access layer to RMSStore. Class keeps mapping of resource id to RMS's
 * internal id in hashtables. Hashtables itself are also stored to RMS. Each
 * type of resource has an own hashtable (index table).
 *
 * @author mark voit
 */
public final class RMSResourceDB {

    /**
     * Prefix for record store name used for each index table
     */
    private static final String INDEX_TABLE_NAME_PREFIX = "RMSResourceDB.IndexTable.";
    /**
     * Max allowed images per store
     */
    private static final int MAX_BYTE_ARRAY_ITEMS_PER_STORE = 100;
    /**
     * Max allowed payload per image
     */
    private static final int MAX_BYTE_ARRAY_PAYLOAD = 100 * 1024;
    /**
     * Max allowed XML items per store
     */
    private static final int MAX_XML_ITEMS_PER_STORE = 13;
    /**
     * Max allowed payload per XML item
     */
    private static final int MAX_XML_PAYLOAD = 100 * 1024;
    /**
     * Singleton instance of this class
     */
    private static RMSResourceDB INSTANCE;
    /**
     * Underlying RMS store
     */
    private RMSStore store;
    /**
     * Collection of indexes
     */
    private Hashtable indexes;
    private Object mutex = new Object();

    /**
     * Returns singleton instance of RMSResourceDB.
     *
     * @return instance of RMSResourceDB
     */
    public static RMSResourceDB getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RMSResourceDB();
        }
        return INSTANCE;
    }

    /**
     * Private constructor. Singleton.
     */
    private RMSResourceDB() {
        this.store = new RMSStore();
        this.indexes = initIndexTablesFromRMS();
    }

    /**
     * Returns the max allowed payload for the specified resource type.
     *
     * @param type type of the resource
     * @return max allowed payload per record
     */
    private int getMaxPayload(RMSResourceType type) {
        if (type == RMSResourceType.XML) {
            return MAX_XML_PAYLOAD;
        }
        return MAX_BYTE_ARRAY_PAYLOAD;
    }

    /**
     * Returns the max allowed payload for the specified resource type.
     *
     * @param type type of the resource
     * @return max allowed payload per record
     */
    private int getMaxAmountItems(RMSResourceType type) {
        if (type == RMSResourceType.XML) {
            return MAX_XML_ITEMS_PER_STORE;
        }
        return MAX_BYTE_ARRAY_ITEMS_PER_STORE;
    }

    /**
     * Writes resource to the store.
     *
     * @param resource to be stored
     * @param listener listener to be invoked when write is complete
     * @throws IllegalArgumentException if resource is null or if payload is
     * exceeded
     */
    int storeResource(final RMSRecord resource) {

        if (resource == null) {
            throw new IllegalArgumentException("resource cannot be null");
        }

        // check max limit items
        if (getNumRecords(resource.getType()) > getMaxAmountItems(resource.getType())) {
            //Log.log(LOG_TAG, "reached max amount entries "+ getMaxAmountItems(resource.getType()) +", freeing space in RMS: "+ resource.getType());
            freeRecordStore(resource.getType(), getMaxAmountItems(resource.getType()) / 2);
        }

        byte[] record = resource.serialize();

        // check max limit payload
        if (record.length > getMaxPayload(resource.getType())) {
            throw new IllegalArgumentException("payload cannot exceed byte limit of " + getMaxPayload(resource.getType()) + ", actual size: " + record.length);
        }

        int intId = getInternalId(resource);

        RMSStoreCallbackListener lsnr = new RMSStoreCallbackListener() {

            public void notifyRMSWriteCompleted(int intId) {
                indexInternalId(intId, resource);
            }

            public void notifyRecordStoreFull(String name, Throwable e) {
                if (e != null) {
                    clearResources(RMSResourceType.valueOf(name));
                }
            }
        };

        this.store.storeRecord(resource.getType().toString(), intId, record, lsnr);

        return record.length;
    }

    /**
     * Deletes resource with given id from store. Store is selected by given
     * type.
     *
     * @param type type of resource
     * @param id id of resource to be deleted
     * @throws IllegalArgumentException if type is null
     */
    public void deleteResource(final RMSResourceType type, final String id) {

        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }

        int intId = getInternalId(type, id);

        if (intId != -1) {
            removeEntry(type, id);
        }
    }

    /**
     * Reads resource with given id from store. Store is selected by given type.
     *
     * @param type type of resource
     * @param id id of resource to be retrieved
     * @return found resource or null
     * @throws IllegalArgumentException if type is null
     */
    public RMSRecord getResource(final RMSResourceType type, final String id) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }

        final int intId = getInternalId(type, id);

        if (intId != -1) {
            byte[] record = this.store.getRecord(type.toString(), intId);

            if (record != null) {
                final RMSRecord resource = type.createNewResource(id);
                resource.deserialize(record);
                return resource;
            } else {
                removeEntry(type, id);
            }
        }

        return null;
    }

    /**
     * Reads all resources from store. Store is selected by given type.
     *
     * @param type type of resource
     * @return vector of found resource or null
     * @throws IllegalArgumentException if type is null
     */
    public Vector getAllResources(final RMSResourceType type) {

        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }

        Vector records = this.store.getRecords(type.toString());

        if (records != null && records.size() > 0) {
            Vector resources = new Vector();

            for (int i = 0; i < records.size(); i++) {
                byte[] record = (byte[]) records.elementAt(i);
                RMSRecord resource = type.createNewResource(null);
                resource.deserialize(record);
                resources.addElement(resource);
            }

            return resources;
        }

        return null;
    }

    /**
     * Deletes underlying record store of given type.
     *
     * @param type type of resource
     */
    public void clearResources(final RMSResourceType type) {
        if (type != null) {
            getIndexTable(type).clear();
            this.store.deleteRecordStore(INDEX_TABLE_NAME_PREFIX + type.getType());
            this.store.deleteRecordStore(type.getType());
        }
    }

    /**
     * Deletes all underlying record stores.
     */
    public void clear() {
        for (int i = 0; i < RMSResourceType.types.length; i++) {
            clearResources(RMSResourceType.types[i]);
        }
    }

    /**
     * Returns number of elements in index table.
     *
     * @param type type of resource
     * @return number of elements in index table.
     */
    public int getNumRecords(RMSResourceType type) {
        return getIndexTable(type).size();
    }

    /**
     * Closes underlying store.
     */
    public void shutdown() {
        if (INSTANCE != null) {
            if (this.indexes != null) {
                this.indexes.clear();
                this.indexes = null;
            }
            if (this.store != null) {
                this.store.shutdown();
            }
            INSTANCE = null;
        }
    }

    /**
     * Retrieves internal id of record from index.
     *
     * @param resource resource for which internal id should be retrieved from
     * index
     * @return internal id or -1
     */
    private int getInternalId(final RMSRecord resource) {
        return getInternalId(resource.getType(), resource.getId());
    }

    /**
     * Retrieves internal id of record from index.
     *
     * @param type type of resource
     * @param id of resource for which internal id should be retrieved
     *
     * @return internal id or -1
     */
    private int getInternalId(final RMSResourceType type, final String id) {
        try {
            Hashtable indexTable = getIndexTable(type);
            if (indexTable != null) {
                Integer intId = (Integer) indexTable.get(id);
                return (intId != null ? intId.intValue() : -1);
            }
        } catch (Exception e) {
            Log.l.log("Can not getInternalId from RMS", id + "", e);
        }
        return -1;
    }

    /**
     * Stores internal id of record to index and RMS.
     *
     * @param intId internal id to be indexed
     * @param resource resource for which the internal id should be indexed
     */
    private void indexInternalId(final int intId, final RMSRecord resource) {
        final Hashtable indexTable = getIndexTable(resource.getType());

        if (indexTable != null && indexTable.get(resource.getId()) == null) {
            indexTable.put(resource.getId(), new Integer(intId));
            updateRMSIndexTable(resource.getType(), resource.getId(), intId);
        }
    }

    /**
     * Removes given amount of elements from index table and record store.
     *
     * @param type type of resource
     * @param amount amount of elements to delete
     */
    private void freeRecordStore(RMSResourceType type, int amount) {
        Hashtable indexTable = getIndexTable(type);
        Vector toBeRemoved = new Vector(amount);

        Enumeration en = indexTable.keys();
        for (int i = 0; i < amount && en.hasMoreElements(); i++) {
            toBeRemoved.addElement(en.nextElement());
        }

        for (en = toBeRemoved.elements(); en.hasMoreElements();) {
            removeEntry(type, (String) en.nextElement());
        }
    }

    /**
     * Removes internal id of record from index table and actual record from
     * RMS.
     *
     * @param type type of resource
     * @param id of resource which should be removed
     */
    private void removeEntry(final RMSResourceType type, final String id) {
        Hashtable indexTable = getIndexTable(type);
        if (indexTable != null) {
            Integer intId = (Integer) indexTable.get(id);
            if (intId != null) {
                indexTable.remove(id);
                this.store.deleteRecord(INDEX_TABLE_NAME_PREFIX + type.toString(), intId.intValue());
                this.store.deleteRecord(type.toString(), intId.intValue());
            }
        }
    }

    /**
     * Returns existing index table based on given resource type.
     *
     * @param type type of resource
     * @return existing or new index table
     */
    private Hashtable getIndexTable(final RMSResourceType type) {
        String name = INDEX_TABLE_NAME_PREFIX + type.toString();

        if (indexes != null) {
            Hashtable index = (Hashtable) indexes.get(name);
            if (index == null) {
                index = new Hashtable();
                indexes.put(name, index);
            }
            return index;
        }
        return null;
    }

    /**
     * Reads previously stored index tables from RMS.
     *
     * @return previously stored index tables
     */
    private Hashtable initIndexTablesFromRMS() {
        final Hashtable indexHash = new Hashtable();
        final String[] names = RecordStore.listRecordStores();

        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                if (names[i].startsWith(INDEX_TABLE_NAME_PREFIX)) {
                    indexHash.put(names[i], readRMSIndexTable(names[i]));
                }
            }
        }

        return indexHash;
    }

    /**
     * Reads previously stored index table from RMS.
     *
     * @return previously stored index tables
     */
    private Hashtable readRMSIndexTable(String name) {

        Hashtable indexTable = new Hashtable();
        Vector records = this.store.getRecords(name);

        if (records != null) {

            DataInputStream dataIn = null;

            for (Enumeration en = records.elements(); en.hasMoreElements();) {

                byte[] record = (byte[]) en.nextElement();
                dataIn = new DataInputStream(new ByteArrayInputStream(record));

                try {
                    indexTable.put(dataIn.readUTF(), new Integer(dataIn.readInt()));
                } catch (IOException e) {
                    //Log.log(LOG_TAG, "error reading index table", e);
                }
            }

            try {
                if (dataIn != null) {
                    ((InputStream) dataIn).close();
                }
            } catch (IOException e) {
            }
        }

        return indexTable;
    }

    /**
     * Updates index table inside RMS.
     *
     * @param type type of resource
     * @param id id of resource
     * @param intId internal id to be indexed
     */
    private void updateRMSIndexTable(final RMSResourceType type, final String id, final int intId) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(out);

        try {
            dataOut.writeUTF(id);
            dataOut.writeInt(intId);
            byte[] record = out.toByteArray();
            this.store.storeRecord(INDEX_TABLE_NAME_PREFIX + type.toString(), -1, record, null);

        } catch (IOException e) {
            Log.l.log("error updating index table", id, e);
        } finally {
            try {
                dataOut.close();
            } catch (IOException e) {
            }
            try {
                out.close();
            } catch (IOException e) {
            }
        }
    }
}
