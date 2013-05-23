/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.jme;

import java.io.UnsupportedEncodingException;
import java.security.DigestException;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotOpenException;
import org.tantalum.storage.FlashCache;
import org.tantalum.storage.FlashDatabaseException;
import org.tantalum.storage.FlashFullException;
import org.tantalum.util.CryptoUtils;
import org.tantalum.util.L;

/**
 *
 * @author phou
 */
public class RMSFastCache extends FlashCache {

    /**
     * Longer hashes use more RAM as they are stored as keys in a Hashtable. The
     * longer hashes may also take longer to calculate, but reduce the
     * theoretical probability of two Strings producing the exact same digest.
     *
     * The length should probably be evenly divisible by 8
     */
    static final char RECORD_HASH_PREFIX = '_';
    private final RecordStore keyRS;
    private final RecordStore valueRS;
    private final Object MUTEX = new Object();
    /*
     * The Integer index of each key record in the hashtable
     * 
     * Always access within a synchronized(MUTEX) block
     */
    private final Hashtable indexHash;

    public RMSFastCache(final char priority) throws FlashDatabaseException, RecordStoreNotOpenException, RecordStoreException, NoSuchAlgorithmException, InvalidRecordIDException, DigestException, UnsupportedEncodingException {
        super(priority);

        keyRS = RMSUtils.getInstance().getRecordStore(getKeyRSName(), true);
        valueRS = RMSUtils.getInstance().getRecordStore(getValueRSName(), true);
        final int numberOfKeys = keyRS.getNumRecords();
        indexHash = new Hashtable(numberOfKeys);
        initIndex(numberOfKeys);
    }

    /**
     * Read the index into Hashtable for rapid "contains" and read operations.
     *
     * Check the integrity of the RMS and delete inconsistent values that may
     * have occurred due to irregular (too fast to write everything) application
     * close.
     *
     * @param hashTableSize
     * @throws RecordStoreNotOpenException
     * @throws InvalidRecordIDException
     * @throws RecordStoreException
     * @throws DigestException
     * @throws UnsupportedEncodingException
     */
    private void initIndex(final int numberOfKeys) throws RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException, DigestException, UnsupportedEncodingException {
        /*
         * All value records not pointed to by an entry in the keyRS
         */
        final Hashtable keyRMSIndexHash = new Hashtable(numberOfKeys * 5 / 4);
        final Hashtable valueIntegers = new Hashtable(numberOfKeys * 5 / 4);
        initReadValueIntegers(valueIntegers);

        //#debug
        dumpHash(valueIntegers);

        final Vector referencedValueIntegers = new Vector(numberOfKeys);

        initReadIndexRecords(keyRMSIndexHash, referencedValueIntegers);

        //#debug
        dumpHash(keyRMSIndexHash);

        final boolean multiplyReferencedValuesFound = initDeleteValuesReferencedMultipleTimes(referencedValueIntegers, valueIntegers);
        if (multiplyReferencedValuesFound) {
            keyRMSIndexHash.clear();
            referencedValueIntegers.removeAllElements();
            initReadIndexRecords(keyRMSIndexHash, referencedValueIntegers);
        }
        initDeleteUnreferencedValues(referencedValueIntegers, valueIntegers);
        initDeleteIndexEntriesPointingToNonexistantValues(keyRMSIndexHash, valueIntegers);
    }

    /**
     * During startup, create a hash collection of all value record IDs found in
     * RMS. At this point these may or may not be properly referenced from a key
     * in the index- that will be checked next.
     *
     * @param unreferencedValueRecordIdHash
     * @throws RecordStoreNotOpenException
     * @throws InvalidRecordIDException
     */
    private void initReadValueIntegers(final Hashtable valueIntegers) throws RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException {
        forEachRecord(valueRS, new RecordTask() {
            void exec() {
                //#debug
                L.i(this, "initReadValueIntegers", "" + keyIndex);
                final Integer valueIndex = new Integer(keyIndex);
                valueIntegers.put(valueIndex, valueIndex);
            }
        });
    }

    private boolean initDeleteValuesReferencedMultipleTimes(final Vector referencedValueIds, final Hashtable valueIntegers) {
        final int n = referencedValueIds.size();
        final Hashtable valuesReferencedOneTime = new Hashtable(n * 5 / 4);
        boolean duplicateValueReferenceFound = false;

        for (int i = 0; i < n; i++) {
            final Integer valueIndex = (Integer) referencedValueIds.elementAt(i);

            if (valueIntegers.contains(valueIndex)) {
                if (valuesReferencedOneTime.containsKey(valueIndex)) {
                    //#debug
                    L.i(this, "Deleting value referenced multiple times in index", valueIndex.toString());
                    valueIntegers.remove(valueIndex);
                    duplicateValueReferenceFound = true;
                    initDeleteRecord(valueRS, valueIndex);
                } else {
                    valuesReferencedOneTime.put(valueIndex, valueIndex);
                }
            }
        }

        return duplicateValueReferenceFound;
    }

    private void initDeleteUnreferencedValues(final Vector referencedValueIds, final Hashtable valueIntegers) {
        final Hashtable valueIntegersCopy = new Hashtable(valueIntegers.size() * 5 / 4);
        final int n = referencedValueIds.size();

        final Enumeration vie = valueIntegers.elements();
        while (vie.hasMoreElements()) {
            final Integer i = (Integer) vie.nextElement();
            valueIntegersCopy.put(i, i);
        }

        for (int i = 0; i < n; i++) {
            final Integer valueIndex = (Integer) referencedValueIds.elementAt(i);

            valueIntegersCopy.remove(valueIndex);
        }

        final Enumeration unreferencedValueIntegers = valueIntegersCopy.elements();
        while (unreferencedValueIntegers.hasMoreElements()) {
            final Integer unreferencedValueInteger = (Integer) unreferencedValueIntegers.nextElement();

            //#debug
            L.i(this, "Deleting unreferenced value", unreferencedValueInteger.toString());
            valueIntegers.remove(unreferencedValueInteger);
            initDeleteRecord(valueRS, unreferencedValueInteger);
        }
    }

    private void initDeleteRecord(final RecordStore rs, final Integer i) {
        try {
            rs.deleteRecord(i.intValue());
        } catch (InvalidRecordIDException ex) {
            //#debug
            L.e(this, "Can not delete record", i.toString(), ex);
        } catch (RecordStoreException ex) {
            //#debug
            L.e(this, "Can not delete record", i.toString(), ex);
        }
    }

    private void initDeleteIndexEntriesPointingToNonexistantValues(final Hashtable keyRMSIndexHash, final Hashtable valueIntegers) {
        final Enumeration indexEntries = keyRMSIndexHash.keys();

        while (indexEntries.hasMoreElements()) {
            final Integer keyRecordInteger = (Integer) indexEntries.nextElement();
            final byte[] indexEntryBytes = (byte[]) keyRMSIndexHash.get(keyRecordInteger);
            final int valueRecordId = toValueIndex(indexEntryBytes);
            final Integer valueRecordInteger = new Integer(valueRecordId);
            String key = null;
            long digest = 0;
            boolean error = false;
            try {
                key = toStringKey(indexEntryBytes);
                digest = CryptoUtils.getInstance().toDigest(key);
            } catch (DigestException ex) {
                //#debug
                L.e(this, "Problem decoding apparently valid index entry", keyRecordInteger.toString(), ex);
                error = true;
            } catch (UnsupportedEncodingException ex) {
                //#debug
                L.e(this, "Problem decoding apparently valid index entry", keyRecordInteger.toString(), ex);
                error = true;
            }
            if (!valueIntegers.containsKey(valueRecordInteger)) {
                //#debug
                L.i(this, "Deleting index entry pointing to non-existant value " + valueRecordInteger, "indexEntry=" + keyRecordInteger);
                initDeleteRecord(keyRS, keyRecordInteger);
            } else if (error) {
                //#debug
                L.i(this, "Deleting index and value entries after decoding error", "indexEntry=" + keyRecordInteger + " valueEntry=" + valueRecordId);
                initDeleteRecord(keyRS, keyRecordInteger);
                initDeleteRecord(valueRS, valueRecordInteger);
            } else {
                //#debug
                L.i(this, "Adding valid index entry", "key=" + key + " indexEntry=" + keyRecordInteger + " valueEntry=" + valueRecordId);
                indexHashPut(digest, keyRecordInteger.intValue(), valueRecordId);
            }
        }
    }

//#mdebug    
    private void dumpHash(Hashtable h) {
        Enumeration enuKey = h.keys();
        Enumeration enu = h.elements();
        final StringBuffer sb = new StringBuffer();

        while (enu.hasMoreElements()) {
            sb.append(enuKey.nextElement().toString());
            sb.append(" -> ");
            Object o = enu.nextElement();
            if (o instanceof byte[]) {
                final byte[] bytes = (byte[]) o;
                sb.append("(");
                sb.append(toValueIndex(bytes));
                sb.append(", ");
                try {
                    sb.append(toStringKey(bytes));
                } catch (UnsupportedEncodingException ex) {
                    sb.append(ex);
                }
                sb.append(")");
            } else {
                sb.append(o.toString());
            }
            sb.append("\r\n");
        }

        L.i(this, "HASHTABLE", sb.toString());
    }
//#enddebug

    /**
     * During startup, read into in-memory accelerator Hashtable and check
     * integrity of each key record.
     *
     * @param unreferencedValueIds
     * @param referencedValueRecordIdHash
     * @throws RecordStoreNotOpenException
     * @throws InvalidRecordIDException
     */
    private void initReadIndexRecords(final Hashtable keyRMSIndexHash,
            final Vector referencedValueIntegers) throws RecordStoreNotOpenException {
        forEachRecord(keyRS, new RecordTask() {
            void exec() {
                try {
                    final byte[] keyIndexBytes = keyRS.getRecord(keyIndex);
                    final String key = toStringKey(keyIndexBytes); // Decode to check integrity
                    final Integer keyRecordInteger = new Integer(keyIndex);
                    final int valueRecordId = toValueIndex(keyIndexBytes);
                    final Integer valueRecordInteger = new Integer(valueRecordId);

                    //#debug
                    L.i(this, "initReadIndexRecords", "key(" + keyIndex + ")=" + key + " (" + Long.toString(CryptoUtils.getInstance().toDigest(key), 16) + ") -> value(" + valueRecordId + ")");
                    keyRMSIndexHash.put(keyRecordInteger, keyIndexBytes);
                    referencedValueIntegers.addElement(valueRecordInteger);
                } catch (Exception e) {
                    //#debug
                    L.e("Can not read index entry, deleting", "" + keyIndex, e);
                    try {
                        keyRS.deleteRecord(keyIndex);
                    } catch (RecordStoreNotOpenException ex) {
                        //#debug
                        L.e(this, "Can not delete unreadable index entry", "keyIndex=" + keyIndex, e);
                    } catch (InvalidRecordIDException ex) {
                        //#debug
                        L.e(this, "Can not delete unreadable index entry", "keyIndex=" + keyIndex, e);
                    } catch (RecordStoreException ex) {
                        //#debug
                        L.e(this, "Can not delete unreadable index entry", "keyIndex=" + keyIndex, e);
                    }
                }
            }
        });
    }

    /**
     * Add or replace a value in the heap Hashtable
     *
     * @param digest
     * @param keyRecordId
     * @param valueRecordId
     * @throws DigestException
     * @throws UnsupportedEncodingException
     */
    private void indexHashPut(final long digest, final int keyRecordId, final int valueRecordId) {
        final Long l = toIndexHash(keyRecordId, valueRecordId);

        synchronized (MUTEX) {
            indexHash.put(new Long(digest), l);
        }
    }

    /**
     * Get the RMS index value for the key and value RMSs from the heap
     * hashtable
     *
     * @param digest
     * @return
     */
    private Long indexHashGet(final long digest) {
        synchronized (MUTEX) {
            return (Long) indexHash.get(new Long(digest));
        }
    }

    /**
     * Read the associated RMS entry from the index to find the original string
     * from which this digest was constructed.
     *
     * @param digest 16 byte cryptographic hash
     * @return the original string used to generate the digest
     * @throws FlashDatabaseException
     * @throws UnsupportedEncodingException
     */
    public String getKey(final long digest) throws FlashDatabaseException {
        synchronized (MUTEX) {
            final Long keyAndValueIndexes = indexHashGet(digest);

            if (keyAndValueIndexes == null) {
                return null;
            }

            final int keyIndex = toKeyIndex(keyAndValueIndexes);
            try {
                final byte[] indexBytes = keyRS.getRecord(keyIndex);
                return toStringKey(indexBytes);
            } catch (RecordStoreException e) {
                throw new FlashDatabaseException("Error converting toString(digest): " + e);
            } catch (UnsupportedEncodingException e) {
                throw new FlashDatabaseException("Error converting toString(digest): " + e);
            }
        }
    }

    /**
     * Combine two 4 byte integers into one Long object for indexHash storage
     *
     * @param keyIndex
     * @param valueIndex
     * @return keyIndex and valueIndex coded into a single Long object to keep
     * in memory as a Hashtable value
     */
    private Long toIndexHash(final int keyIndex, final int valueIndex) {
        return new Long(((long) keyIndex << 32) | valueIndex);
    }

    /**
     * Extract the index into the key RMS from Long value stored in the
     * hashtable
     *
     * @param hashValue
     * @return record number in keyRMS
     */
    private int toKeyIndex(final Long hashValue) {
        return (int) ((hashValue.longValue() >>> 32) & 0xFFFF);
    }

    /**
     * Extract the index into the value RMS from Long value stored in the
     * hashtable
     *
     * @param hashValue
     * @return record number in valueRMS
     */
    private int toValueIndex(final Long hashValue) {
        return (int) (hashValue.longValue() & 0xFFFF);
    }

    /**
     * The file name
     *
     * @return the keyRMS name based on the cache priority
     */
    private String getKeyRSName() {
        return "" + RECORD_HASH_PREFIX + priority + "key";
    }

    /**
     * The file name
     *
     * @return the valueRMS name based on the cache priority
     */
    private String getValueRSName() {
        return "" + RECORD_HASH_PREFIX + priority + "val";
    }

    /**
     * Encode the key and an index into the valueRMS into a byte[] to store in
     * the keyRMS
     *
     * @param key
     * @param valueIndex
     * @return the bytes to put in the keyRMS
     * @throws UnsupportedEncodingException
     */
    private byte[] toIndexBytes(final String key, final int valueIndex) throws UnsupportedEncodingException {
        final byte[] bytes = key.getBytes();
        final byte[] bytesWithValue = new byte[bytes.length + 4];

        bytesWithValue[0] = (byte) ((valueIndex & 0xFF000000) >>> 24);
        bytesWithValue[1] = (byte) ((valueIndex & 0xFF0000) >>> 16);
        bytesWithValue[2] = (byte) ((valueIndex & 0x0FF00) >>> 8);
        bytesWithValue[3] = (byte) (valueIndex & 0xFF);

        System.arraycopy(bytes, 0, bytesWithValue, 4, bytes.length);

        return bytesWithValue;
    }

    /**
     * The string part extracted from the byte[] stored in the keyRMS
     *
     * @param indexBytes
     * @return the key used to add a value to the cache
     */
    private String toStringKey(final byte[] indexBytes) throws UnsupportedEncodingException {
        return new String(indexBytes, 4, indexBytes.length - 4, "UTF-8");
    }

    /**
     * The int part extracted from the byte[] stored in the keyRMS
     *
     * @param indexBytes
     * @return the valueRMS index from which the associated value can be
     * extracted
     */
    private int toValueIndex(final byte[] indexBytes) {
        int i = indexBytes[0] & 0xFF;
        i <<= 8;
        i |= indexBytes[1] & 0xFF;
        i <<= 8;
        i |= indexBytes[2] & 0xFF;
        i <<= 8;
        i |= indexBytes[3] & 0xFF;

        return i;
    }

    /**
     * Get the value associated with this key digest from phone flash memory
     *
     * @param digest
     * @return the bytes of the value
     * @throws FlashDatabaseException
     */
    public byte[] get(final long digest) throws FlashDatabaseException {
        synchronized (MUTEX) {
            final Long hashValue = ((Long) indexHash.get(new Long(digest)));

            if (hashValue != null) {
                try {
                    final int valueIndex = toValueIndex(hashValue);
                    return valueRS.getRecord(valueIndex);
                } catch (Exception ex) {
                    throw new FlashDatabaseException("Can not getData from RMS: " + Long.toString(digest, 16) + " - " + ex);
                }
            }

            return null;
        }
    }

    /**
     * Put new or replacement data associated with a key into the cache
     *
     * @param key
     * @param value
     * @throws DigestException
     * @throws UnsupportedEncodingException
     * @throws FlashFullException
     * @throws FlashDatabaseException
     */
    public void put(final String key, final byte[] value) throws DigestException, FlashFullException, FlashDatabaseException {
        if (key == null) {
            throw new IllegalArgumentException("You attempted to put a null key to the cache");
        }
        if (value == null) {
            throw new IllegalArgumentException("You attempted to put null data to the cache");
        }

        synchronized (MUTEX) {
            try {
                final long digest = CryptoUtils.getInstance().toDigest(key);
                final Long indexEntry = indexHashGet(digest);
                final int valueRecordId;
                final int keyRecordId;

                if (indexEntry == null) {
                    valueRecordId = valueRS.addRecord(value, 0, value.length);
                    final byte[] byteKey = toIndexBytes(key, valueRecordId);
                    keyRecordId = keyRS.addRecord(byteKey, 0, byteKey.length);
                    indexHashPut(digest, keyRecordId, valueRecordId);
                    //#debug
                    L.i(this, "put(" + key + ") digest=" + Long.toString(digest, 16), "Value added to RMS=" + valueRS.getName() + " index=" + valueRecordId + " bytes=" + value.length + " keyIndex=" + keyRecordId);
                } else {
                    valueRecordId = toValueIndex(indexEntry);
                    valueRS.setRecord(valueRecordId, value, 0, value.length);
                    //#debug
                    L.i(this, "put(" + key + ") digest=" + Long.toString(digest, 16), "Value overwrite to RMS=" + valueRS.getName() + " index=" + valueRecordId + " bytes=" + value.length);
                }
            } catch (RecordStoreFullException e) {
                //#debug
                L.e(this, "Can not write", "key=" + key, e);
                //FIXME Clear space instead of blowing up
                throw new FlashFullException("Flash full when adding key: " + key);
            } catch (RecordStoreException e) {
                //#debug
                L.e(this, "Can not write", "key=" + key, e);
                throw new FlashDatabaseException("Can not putData to RMS: " + key + " - " + e);
            } catch (UnsupportedEncodingException ex) {
                //#debug
                L.e(this, "Can not write", "key=" + key, ex);
                throw new FlashDatabaseException("Can not putData to RMS: " + key + " - " + ex);
            }
        }
    }

    /**
     * Remove the key and value associated with a key digest from the cache
     *
     * @param digest
     * @throws FlashDatabaseException
     */
    public void removeData(final long digest) throws FlashDatabaseException {
        synchronized (MUTEX) {
            try {
                final Long indexEntry = indexHashGet(digest);

                if (indexEntry != null) {
                    final int valueRecordId = toValueIndex(indexEntry);
                    final int keyRecordId = toKeyIndex(indexEntry);
                    valueRS.deleteRecord(valueRecordId);
                    keyRS.deleteRecord(keyRecordId);
                }
            } catch (RecordStoreException e) {
                throw new FlashDatabaseException("Can not removeData from RMS: " + Long.toString(digest, 16) + " - " + e);
            }
        }
    }

    /**
     * Get a list of all digests in this cache
     *
     * @return an array of byte[] digest-of-key objects in the cache
     * @throws FlashDatabaseException
     */
    public long[] getDigests() throws FlashDatabaseException {
        synchronized (MUTEX) {
            final long[] digests = new long[indexHash.size()];
            final Enumeration enu = indexHash.keys();

            for (int i = 0; i < digests.length; i++) {
                digests[i] = ((Long) enu.nextElement()).longValue();
            }

            return digests;
        }
    }

    /**
     * Iterate and perform a task on every RMS entry in the specified record
     * store. The record store is locked to prevent any changes during this
     * loop.
     *
     * @param recordStore
     * @param task
     * @throws RecordStoreNotOpenException
     */
    private void forEachRecord(final RecordStore recordStore, final RecordTask task) throws RecordStoreNotOpenException {
        RecordEnumeration recordEnum = null;

        try {
            synchronized (MUTEX) {
                recordEnum = recordStore.enumerateRecords(null, null, false);
                //#debug
                int i = 0;
                while (recordEnum.hasNextElement()) {
                    int recordId = 0;

                    try {
                        recordId = recordEnum.nextRecordId();
                        //#debug
                        L.i(this, "forEachRecord(" + i++ + ", " + recordStore.getName() + ")", "recordId=" + recordId);
                        task.exec(recordId);
                    } catch (InvalidRecordIDException ex) {
                        //#debug
                        L.e("forEach record problem", "recordId=" + recordId + " task=" + task, ex);
                    }
                }
            }
        } finally {
            if (recordEnum != null) {
                recordEnum.destroy();
            }
        }
    }

    /**
     * Delete all records in the record store
     *
     * @param recordStore
     * @throws RecordStoreNotOpenException
     */
    private void clear(final RecordStore recordStore) throws RecordStoreException {
        forEachRecord(recordStore, new RecordTask() {
            void exec() {
                try {
                    recordStore.deleteRecord(keyIndex);
                } catch (Exception e) {
                    //#debug
                    L.e("Can not clear rms", "" + keyIndex, e);
                }
            }
        });
    }

    /**
     * Delete all key and value objects from heap memory and from the phone
     * flash memory
     *
     */
    public void clear() {
        synchronized (MUTEX) {
            //#debug
            L.i("Clearing RMSFastCache", "" + priority);
            indexHash.clear();
            try {
                clear(valueRS);
            } catch (RecordStoreException ex) {
                //#debug
                L.e("Can not clear RMS values", "aborting", ex);
            }
            try {
                clear(keyRS);
            } catch (RecordStoreException ex) {
                //#debug
                L.e("Can not clear RMS keys", "aborting", ex);
            }
        }
    }

    public long getFreespace() throws FlashDatabaseException {
        try {
            return valueRS.getSizeAvailable();
        } catch (RecordStoreNotOpenException ex) {
            //#debug
            L.e(this, "Can not get freespace", this.toString(), ex);
            throw new FlashDatabaseException("Can not get freespace: " + ex);
        }
    }

    public void close() throws FlashDatabaseException {
        synchronized (MUTEX) {
            try {
                super.close();
                
                try {
                    valueRS.closeRecordStore();
                } finally {
                    keyRS.closeRecordStore();
                }
            } catch (RecordStoreException ex) {
                L.e(this, "RMS close exception", this.toString(), ex);
                throw new FlashDatabaseException("RMS close exception: " + ex);
            }
        }
    }

    /**
     * Extend this to perform an operation on all records in a record store
     * using the forEachRecord() method
     */
    private static abstract class RecordTask {

        public int keyIndex;

        final void exec(final int recordIndex) {
            this.keyIndex = recordIndex;
            exec();
        }

        abstract void exec();
    }
}
