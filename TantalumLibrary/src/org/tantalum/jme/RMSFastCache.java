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
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotOpenException;
import org.tantalum.CancellationException;
import org.tantalum.Task;
import org.tantalum.TimeoutException;
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
     */
    private final Hashtable indexHash;

    public RMSFastCache(final char priority) throws FlashDatabaseException, RecordStoreNotOpenException, RecordStoreException, NoSuchAlgorithmException, InvalidRecordIDException, DigestException, UnsupportedEncodingException {
        super(priority);

        keyRS = RMSUtils.getInstance().getRecordStore(getKeyRSName(), true);
        valueRS = RMSUtils.getInstance().getRecordStore(getValueRSName(), true);
        final int hashTableSize = (keyRS.getNumRecords() * 5) / 4;
        indexHash = new Hashtable(hashTableSize);
        (new Task() {
            protected Object exec(final Object in) {
                final Task[] t;
                synchronized (shutdownTasks) {
                    t = new Task[shutdownTasks.size()];
                    for (int i = 0; i < t.length; i++) {
                        t[i] = (Task) shutdownTasks.elementAt(i);
                        t[i].fork();
                    }
                }
                try {
                    Task.joinAll(t);
                } catch (CancellationException ex) {
                    //#debug
                    L.e(this, "Canceled", "Cache shutdown tasks not completed", ex);
                } catch (TimeoutException ex) {
                    //#debug
                    L.e(this, "Timeout", "Cache shutdown tasks not completed", ex);
                }
                (new Task(Task.SHUTDOWN) {
                    protected Object exec(Object in) {
                        synchronized (MUTEX) {
                            //#debug
                            L.i("Closing Cache", "" + priority);
                            try {
                                valueRS.closeRecordStore();
                            } catch (Exception ex) {
                                //#debug
                                L.e("Problem closing valueRS", getValueRSName(), ex);
                            }
                            try {
                                keyRS.closeRecordStore();
                            } catch (Exception ex) {
                                //#debug
                                L.e("Problem closing keyRS", getKeyRSName(), ex);
                            }

                            return in;
                        }
                    }
                }).setClassName("CloseAfterShutdownTasksComplete").fork();

                return in;
            }
        }.setClassName("LaunchShutdownTasksOnShutdown")).fork(Task.SHUTDOWN);
        initIndex(hashTableSize);
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
    private void initIndex(final int hashTableSize) throws RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException, DigestException, UnsupportedEncodingException {
        /*
         * All value records not pointed to by an entry in the keyRS
         */
        final Hashtable unreferencedValueIds = new Hashtable(hashTableSize);
        /*
         * All value records pointed to one time in the keyRS
         */
        final Hashtable referencedValueIds = new Hashtable(hashTableSize);

        readValueRecords(unreferencedValueIds);
        readIndexRecords(unreferencedValueIds, referencedValueIds);
        deleteUnreferencedValueRecords(unreferencedValueIds);
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
    private void readValueRecords(final Hashtable unreferencedValueIds) throws RecordStoreNotOpenException, InvalidRecordIDException {
        forEachRecord(valueRS, new RecordTask() {
            void exec() {
                final Integer valueIndex = new Integer(keyIndex);
                unreferencedValueIds.put(valueIndex, valueIndex);
                //#debug
                L.i(this, "valueRecordId", "" + keyIndex);
            }
        });
    }

    /**
     * During startup, readAllIndexRecords() uses this to check for duplicate
     * pointers to a value in RMS
     *
     * Duplicate pointers to a value should never occur because of execution
     * order. We always write to or delete from the value RMS first, then write
     * to or delete from the key RMS.
     *
     * @param referencedValueIds
     * @param valueRecordId
     * @param keyRecordId
     * @return
     */
    private boolean isValueRecordIdAlreadyInUse(final Hashtable referencedValueIds, final int valueRecordId, final int keyRecordId) {
        final Integer v = new Integer(valueRecordId);
        final Integer k = new Integer(keyRecordId);
        final boolean in = referencedValueIds.containsKey(v);

        if (!in) {
            referencedValueIds.put(v, k);
        }

        return in;
    }

    /**
     * During startup, read into in-memory accelerator Hashtable and check
     * integrity of each key record.
     *
     * @param unreferencedValueIds
     * @param referencedValueRecordIdHash
     * @throws RecordStoreNotOpenException
     * @throws InvalidRecordIDException
     */
    private void readIndexRecords(final Hashtable unreferencedValueIds, final Hashtable referencedValueIds) throws RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException, DigestException, UnsupportedEncodingException {
        final Hashtable validEntries = new Hashtable(); // In use value -> key

        forEachRecord(keyRS, new RecordTask() {
            void exec() {
                try {
                    final byte[] keyIndexBytes = keyRS.getRecord(keyIndex);
                    final int valueRecordId = toValueIndex(keyIndexBytes);
                    final Integer vri = new Integer(valueRecordId);
                    final String key = toStringKey(keyIndexBytes); // Check integrity by decoding the key String from bytes

                    //#debug
                    L.i(this, "keyRecord", "key(" + keyIndex + ")=" + key + " (" + CryptoUtils.getInstance().toDigest(key) + ") -> value(" + valueRecordId + ")");

                    if (!unreferencedValueIds.contains(vri)) {
                        //#debug
                        L.i("Found key pointing to non-existant value record", "deleting key " + key + " index=" + keyIndex + " key=" + key);
                        keyRS.deleteRecord(keyIndex);
                    } else if (isValueRecordIdAlreadyInUse(referencedValueIds, valueRecordId, keyIndex)) {
                        final Integer duplicateKeyIndex = ((Integer) validEntries.get(vri));

                        validEntries.remove(vri);
                        referencedValueIds.remove(vri);
                        unreferencedValueIds.remove(vri);
                        //#debug
                        L.i("Found multiple keys pointing to the same value record", "deleting key " + key + " index=" + keyIndex + " and duplicate keyindex=" + duplicateKeyIndex + " value index=" + valueRecordId);
                        valueRS.deleteRecord(valueRecordId);
                        keyRS.deleteRecord(keyIndex);
                        if (duplicateKeyIndex != null) {
                            keyRS.deleteRecord(duplicateKeyIndex.intValue());
                        }
                    } else {
                        final Integer referencedValueId = new Integer(valueRecordId);
                        if (unreferencedValueIds.contains(referencedValueId)) {
                            unreferencedValueIds.remove(referencedValueId);
                            validEntries.put(referencedValueId, new Integer(keyIndex));
                            indexHashPut(key, keyIndex, valueRecordId);
                        } else {
                            //#debug
                            L.i("Key points to non-existant value, deleting", "key=" + key + " keyRecordId=" + keyIndex);
                            keyRS.deleteRecord(keyIndex);
                        }
                    }
                } catch (Exception e) {
                    //#debug
                    L.e("Can not readAllIndexRecords", "" + keyIndex, e);
                }
            }
        });
    }

    /**
     * During startup, we may have some records in the value RMS which are not
     * referenced from the index RMS. If so, delete them and put a warning in
     * the debug. This can happen during sudden shutdown if the value but not
     * the key was written before the application closed.
     *
     * @param unreferencedValueIds
     * @throws RecordStoreNotOpenException
     * @throws InvalidRecordIDException
     * @throws RecordStoreException
     */
    private void deleteUnreferencedValueRecords(final Hashtable unreferencedValueIds) throws RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException {
        synchronized (MUTEX) {
            final Enumeration ids = unreferencedValueIds.elements();

            while (ids.hasMoreElements()) {
                final int unreferencedValueIndex = ((Integer) ids.nextElement()).intValue();

                //#debug
                L.i(this, "Unreferenced value entry- deleting", "" + unreferencedValueIndex);
                valueRS.deleteRecord(unreferencedValueIndex);
            }
        }
    }

    /**
     * Add or replace a value to the heap Hashtable which accelerates index
     * activities.
     *
     * @param key
     * @param keyRecordId
     * @param valueRecordId
     * @throws DigestException
     * @throws UnsupportedEncodingException
     */
    private void indexHashPut(final String key, final int keyRecordId, final int valueRecordId) throws DigestException, UnsupportedEncodingException {
        final long digest = CryptoUtils.getInstance().toDigest(key);
        indexHashPut(digest, keyRecordId, valueRecordId);
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
    private void indexHashPut(final long digest, final int keyRecordId, final int valueRecordId) throws DigestException, UnsupportedEncodingException {
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
                throw new FlashDatabaseException("RMS error converting toString(digest): " + e);
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
    private String toStringKey(final byte[] indexBytes) {
        return new String(indexBytes, 4, indexBytes.length - 4);
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

        //#debug
        L.i(this, "forEachRecord", "recordStore=" + recordStore.getName() + " recordStoreSize=" + recordStore.getSize());

        try {
            synchronized (MUTEX) {
                recordEnum = recordStore.enumerateRecords(null, null, true);
                //#debug
                int i = 0;
                while (recordEnum.hasNextElement()) {
                    int recordId = 0;

                    try {
                        recordId = recordEnum.nextRecordId();
                        //#debug
                        L.i(this, "forEachRecord(" + i++ + ")", "nextRecordId=" + recordId);
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
    private void clear(final RecordStore recordStore) throws RecordStoreNotOpenException {
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
            } catch (RecordStoreNotOpenException ex) {
                //#debug
                L.e("Can not clear RMS values", "aborting", ex);
            }
            try {
                clear(keyRS);
            } catch (RecordStoreNotOpenException ex) {
                //#debug
                L.e("Can not clear RMS keys", "aborting", ex);
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
