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
import org.tantalum.PlatformUtils;
import org.tantalum.Task;
import org.tantalum.storage.FlashCache;
import org.tantalum.storage.FlashDatabaseException;
import org.tantalum.storage.FlashFullException;
import org.tantalum.util.CryptoUtils;
import org.tantalum.util.L;
import org.tantalum.util.LRUHashtable;

/**
 *
 * @author phou
 */
public final class RMSFastCache extends FlashCache {

    private static final boolean INDIVIDUAL_WRITE_DIRTY_FLAG = true; // Set true is slower to write and shutdown app, but less likely to wipe cache in event of unexpected shutdown

    /**
     * Always accessed from a synchronized block. How big the RMS is after we
     * close it. RMS does not shrink to this size until we close it
     * (experimentally determined implementation detail)
     */
    private long rmsByteSize = 0;
    /**
     * Longer hashes use more RAM as they are stored as keys in a Hashtable. The
     * longer hashes may also take longer to calculate, but reduce the
     * theoretical probability of two Strings producing the exact same digest.
     *
     * The length should probably be evenly divisible by 8
     */
    static final char RECORD_HASH_PREFIX = '_';
    /*
     * The Integer index of each key record in the hashtable
     * 
     * Always access within a synchronized(MUTEX) block
     */
    private final LRUHashtable indexHash = new LRUHashtable();
    private final RecordStore keyRS;
    private final RecordStore valueRS;
    private final Object mutex = new Object();
    private final org.tantalum.jme.RMSKeyUtils RMSKeyUtils = new org.tantalum.jme.RMSKeyUtils();

    public RMSFastCache(final char priority, final FlashCache.StartupTask startupTask) throws FlashDatabaseException, RecordStoreNotOpenException, RecordStoreException, NoSuchAlgorithmException, InvalidRecordIDException, DigestException, UnsupportedEncodingException {
        super(priority);

        clearCacheIfLastCloseWasDirty();
        keyRS = openRMS(getKeyRSName());
        valueRS = openRMS(getValueRSName());
        final int numberOfKeys = keyRS.getNumRecords();
        initIndex(numberOfKeys, startupTask);
    }

    private void updateRMSByteSize() throws RecordStoreNotOpenException {
        final long previous = rmsByteSize;
        rmsByteSize = keyRS.getSize() + valueRS.getSize();
        //#debug
        L.i(this, "updateRMSByteSize", previous + " -> " + rmsByteSize);
    }

    private RecordStore openRMS(final String name) throws FlashDatabaseException, RecordStoreException {
        return RMSUtils.getInstance().getRecordStore(name, true);
    }

    private void toggleRMS(final String name, final RecordStore rs) throws FlashDatabaseException, RecordStoreException {
        if (Task.isShuttingDown()) {
            return;
        }
        //#debug
        L.i(this, "toggleRMS - Closing and re-opening rms", name + " ");
        synchronized (mutex) {
            try {
                rs.closeRecordStore();
            } catch (Exception e) {
                //#debug
                L.e(this, "Problem with toggleRMS closing and re-opening rms", name, e);
            }
            RMSUtils.getInstance().getRecordStore(name, true);
            updateRMSByteSize();
        }
        //#debug
        L.i(this, "End toggleRMS", name);
    }

    public void markLeastRecentlyUsed(final Long digest) {
        if (digest == null) {
            throw new IllegalArgumentException("Can not mark null digest as least recently used");
        }

        indexHash.get(digest);
    }

    private String getFlagRMSName() {
        return "dirty+" + getKeyRSName();
    }

    private String getStoreFlagRMSName() {
        return "IO+" + getKeyRSName();
    }

    private void clearCacheIfLastCloseWasDirty() {
        RecordStore flagRMS = null;
        RecordStore storeFlagRMS = null;

        try {
            flagRMS = RMSUtils.getInstance().getRecordStore(getFlagRMSName(), false); // Just see if the RMS exists, do not create
            if (INDIVIDUAL_WRITE_DIRTY_FLAG) {
                storeFlagRMS = RMSUtils.getInstance().getRecordStore(getStoreFlagRMSName(), false); // Just see if the RMS exists, do not create
            }
        } catch (RecordStoreException e) {
            //#debug
            L.e("*** Cache \'" + priority + "\'", "Had trouble checking dirty flag", e);
            deleteDataFiles(priority);
        } catch (FlashDatabaseException e) {
            //#debug
            L.e("*** Cache \'" + priority + "\'", "Had trouble checking dirty flag", e);
            deleteDataFiles(priority);
        } finally {
            try {
                if (flagRMS != null) {
                    flagRMS.closeRecordStore();
                    if (!INDIVIDUAL_WRITE_DIRTY_FLAG || storeFlagRMS != null) {
                        //#debug
                        L.i("*** Cache \'" + priority + "\' is possibly dirty after incomplete shutdown last run, deleting entire cache to avoid possible deadlock", null);
                        if (INDIVIDUAL_WRITE_DIRTY_FLAG) {
                            storeFlagRMS.closeRecordStore();
                        }
                        deleteDataFiles(priority);
                    }
                }
            } catch (RecordStoreException ex) {
                //#debug
                L.e("*** Cache \'" + priority + "\'", "Had trouble deleting after dirty previous shutdown detected", ex);
            } finally {
                try {
                    setDirtyFlagAfterNormalStartup();
                } catch (RecordStoreException e) {
                    //#debug
                    L.e("*** Cache \'" + priority + "\' had trouble setting dirty flag", "(normal RMS-not-yet-closed flag created on startup)", e);
                    RMSUtils.getInstance().wipeRMS();
                }
            }
        }
    }

    private void clearDirtyFlagAfterNormalShutdown() throws RecordStoreException {
        //#debug
        L.i("Start: clear dirty flag on normal shutdown", "cache \'" + priority + "\'");
        RecordStore.deleteRecordStore(getFlagRMSName());
        //#debug
        L.i("Success: clear dirty flag on normal shutdown", "cache \'" + priority + "\'");
    }

    private void setDirtyFlagAfterNormalStartup() throws RecordStoreException {
        RecordStore flagRMS = null;

        try {
            flagRMS = RMSUtils.getInstance().getRecordStore(getFlagRMSName(), true); // Just create the RMS, does not matter what is inside
        } catch (RecordStoreException e) {
            //#debug
            L.e("*** Cache \'" + priority + "\'", "Had trouble setting dirty flag", e);
            RMSUtils.getInstance().wipeRMS();
        } catch (FlashDatabaseException e) {
            //#debug
            L.e("*** Cache \'" + priority + "\'", "Had trouble setting dirty flag", e);
            RMSUtils.getInstance().wipeRMS();
        } finally {
            if (flagRMS != null) {
                try {
                    flagRMS.closeRecordStore();
                } catch (RecordStoreException ex) {
                    //#debug
                    L.e("*** Cache \'" + priority + "\'", "Had trouble closing flag after create on startup", ex);
                    RMSUtils.getInstance().wipeRMS();
                }
            }
        }
    }

    private void clearStoreFlag() throws RecordStoreException {
        if (INDIVIDUAL_WRITE_DIRTY_FLAG) {
            //#debug
            L.i("Start: clear corrupted store flag after adding/deleting/closing the record", "cache \'" + priority + "\'");
            RecordStore.deleteRecordStore(getStoreFlagRMSName());
            //#debug
            L.i("Success: clear corrupted store flag after adding/deleting/closing the record", "cache \'" + priority + "\'");
        }
    }

    private void setStoreFlag() throws RecordStoreException {
        if (INDIVIDUAL_WRITE_DIRTY_FLAG) {
            RecordStore flagRMS = null;

            try {
                flagRMS = RMSUtils.getInstance().getRecordStore(getStoreFlagRMSName(), true); // Just create the RMS, does not matter what is inside
            } catch (RecordStoreException e) {
                //#debug
                L.e("*** Cache \'" + priority + "\'", "Had trouble setting corrupted store flag", e);
                RMSUtils.getInstance().wipeRMS();
            } catch (FlashDatabaseException e) {
                //#debug
                L.e("*** Cache \'" + priority + "\'", "Had trouble setting corrupted store flag", e);
                RMSUtils.getInstance().wipeRMS();
            } finally {
                if (flagRMS != null) {
                    try {
                        flagRMS.closeRecordStore();
                    } catch (RecordStoreException ex) {
                        //#debug
                        L.e("*** Cache \'" + priority + "\'", "Had trouble closing corrupted sotre flag after creation", ex);
                        RMSUtils.getInstance().wipeRMS();
                    }
                }
            }
        }
    }

    /**
     * Hard kill the RMS files. This is only done if there are unfixable errors
     * detected.
     *
     * @param priority
     */
    public static void deleteDataFiles(final char priority) {
        //#debug
        L.i("*** Attempting to delete keyRS", "priority=" + priority);
        try {
            RMSUtils.getInstance().delete(getKeyRSName(priority));
            //#debug
            L.i("Successful delete keyRS", "priority=" + priority);
        } catch (FlashDatabaseException ex) {
            //#debug
            L.e("Failed to delete keyRS", "priority=" + priority, ex);
        }

        //#debug
        L.i("*** Attempting to delete valueRS", "priority=" + priority);
        try {
            RMSUtils.getInstance().delete(getValueRSName(priority));
            //#debug
            L.i("Successful delete valueRS", "priority=" + priority);
        } catch (FlashDatabaseException ex) {
            //#debug
            L.e("Failed to delete valueRS", "priority=" + priority, ex);
        }
    }

    /**
     * Read the index into Hashtable for rapid "contains" and read operations.
     *
     * Check the integrity of the RMS and delete inconsistent values that may
     * have occurred due to irregular (too fast to write everything) application
     * close.
     *
     * @param numberOfKeys
     * @param startupTask
     * @throws RecordStoreNotOpenException
     * @throws InvalidRecordIDException
     * @throws RecordStoreException
     * @throws DigestException
     * @throws UnsupportedEncodingException
     */
    private void initIndex(final int numberOfKeys, final FlashCache.StartupTask startupTask) throws RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException, DigestException, UnsupportedEncodingException {
        /*
         * All value records not pointed to by an entry in the keyRS
         * 
         * currentRecordKeyAsInteger -> keyIndexBytes(4bytes:string where 4bytes is int index of valueRS and string is url)
         */
        final Hashtable keyRMSIndexHash = new Hashtable(numberOfKeys * 5 / 4);
        /*
         * Hash of all valueRM index numbers for fast "contains"
         * 
         * value -> value
         */
        final Hashtable valueIntegers = new Hashtable(numberOfKeys * 5 / 4);
        initReadValueIntegers(valueIntegers);

        //#debug
        dumpHash(valueIntegers);

        final Vector referencedValueIntegers = new Vector(numberOfKeys);

        initReadIndexRecords(keyRMSIndexHash, referencedValueIntegers, startupTask);

        //#debug
        dumpHash(keyRMSIndexHash);

        final boolean multiplyReferencedValuesFound = initDeleteValuesReferencedMultipleTimes(referencedValueIntegers, valueIntegers);
        if (multiplyReferencedValuesFound) {
            keyRMSIndexHash.clear();
            referencedValueIntegers.removeAllElements();
            initReadIndexRecords(keyRMSIndexHash, referencedValueIntegers, null);
        }
        initDeleteUnreferencedValues(referencedValueIntegers, valueIntegers);
        initDeleteIndexEntriesPointingToNonexistantValues(keyRMSIndexHash, valueIntegers);
        //#mdebug
        // Validate that no two keys have the same value
        final Hashtable copy = new Hashtable();

        final Enumeration enumeration = indexHash.elements();
        while (enumeration.hasMoreElements()) {
            final Object dummyObject = new Object();
            Object theValue = enumeration.nextElement();
            if (copy.containsKey(theValue)) {
                // Here we have duplicate value in the indexHash
                L.i(this, "Duplicate value in indexHash. Two distinct keys point to equal value ", RMSKeyUtils.toKeyIndex((Long) theValue) + "-" + RMSKeyUtils.toValueIndex((Long) theValue));
                dumpHash(copy);
                PlatformUtils.getInstance().shutdown("RMS is inconsistent");
            }

            copy.put(theValue, dummyObject);
        }

        if (copy.size() != indexHash.size()) {
            // We have a problem
            L.i(this, "Size mismatch in indexHash", copy.size() + " != " + indexHash.size());
            dumpHash(copy);
            PlatformUtils.getInstance().shutdown("RMS size mismatch, is inconsistent");
        }
        //#enddebug
    }

    /**
     * During startup, create a hash collection of all value record IDs found in
     * RMS. At this point these may or may not be properly referenced from a key
     * in the index- that will be checked next.
     *
     * @param valueIntegers
     * @throws RecordStoreNotOpenException
     * @throws InvalidRecordIDException
     */
    private void initReadValueIntegers(final Hashtable valueIntegers) throws RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException {
        forEachRecord(valueRS, new RecordTask() {
            void exec() {
                //#debug
                L.i(this, "initReadValueIntegers", Integer.toString(currentRecordTaskKeyIndex));
                final Integer valueIndex = new Integer(currentRecordTaskKeyIndex);
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
            final int valueRecordId = RMSKeyUtils.toValueIndex(indexEntryBytes);
            final Integer valueRecordInteger = new Integer(valueRecordId);
            String key = null;
            long digest = 0;
            boolean error = false;
            try {
                key = RMSKeyUtils.toStringKey(indexEntryBytes);
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
                sb.append(RMSKeyUtils.toValueIndex(bytes));
                sb.append(", ");
                try {
                    sb.append(RMSKeyUtils.toStringKey(bytes));
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
     * //
     *
     * @param initTimeToKeyRMSIndexHash
     * @param initTimeReferencedValueIntegers
     * @throws RecordStoreNotOpenException
     */
    private void initReadIndexRecords(final Hashtable initTimeToKeyRMSIndexHash,
            final Vector initTimeReferencedValueIntegers, final FlashCache.StartupTask startupTask) throws RecordStoreNotOpenException {
        //#mdebug
        final Hashtable duplicateIndexStringHash = new Hashtable();
        final Hashtable duplicateIndexValueHash = new Hashtable();
        final Object AN_OBJECT_THAT_SAVES_MEMORY = new Object();
        //#enddebug

        forEachRecord(keyRS, new RecordTask() {
            private void deleteCurrentKey() {
                try {
                    keyRS.deleteRecord(currentRecordTaskKeyIndex);
                } catch (RecordStoreNotOpenException ex) {
                    //#debug
                    L.e(this, "Can not delete unreadable index entry", "keyIndex=" + currentRecordTaskKeyIndex, ex);
                } catch (InvalidRecordIDException ex) {
                    //#debug
                    L.e(this, "Can not delete unreadable index entry", "keyIndex=" + currentRecordTaskKeyIndex, ex);
                } catch (RecordStoreException ex) {
                    //#debug
                    L.e(this, "Can not delete unreadable index entry", "keyIndex=" + currentRecordTaskKeyIndex, ex);
                }
            }

            void exec() {
                try {
                    final byte[] keyIndexBytes = keyRS.getRecord(currentRecordTaskKeyIndex);
                    final String key = RMSKeyUtils.toStringKey(keyIndexBytes); // Decode to check integrity
                    final Integer currentRecordKeyAsInteger = new Integer(currentRecordTaskKeyIndex);
                    final int valueRecordId = RMSKeyUtils.toValueIndex(keyIndexBytes);
                    final Integer valueRecordIdAsInteger = new Integer(valueRecordId);

                    //#mdebug
                    L.i(this, "initReadIndexRecords", "key(" + currentRecordTaskKeyIndex + ")=" + key + " (" + Long.toString(CryptoUtils.getInstance().toDigest(key), 16) + ") -> value(" + valueRecordId + ")");
                    final String s = RMSKeyUtils.toStringKey(keyIndexBytes);
                    final Integer v = new Integer(RMSKeyUtils.toValueIndex(keyIndexBytes));
                    if (duplicateIndexStringHash.containsKey(s)) {
                        L.i(this, "WARNING: duplicate keyRS key entries found, should be fixed in next phase of init", s + " - " + v + " at keyRMS index " + currentRecordTaskKeyIndex);
                    }
                    if (duplicateIndexValueHash.containsKey(v)) {
                        L.i(this, "WARNING: duplicate keyRS key entries pointing to the same valueRS position found, should be fixed in next phase of init", s + " - " + v + " at keyRMS index " + currentRecordTaskKeyIndex);
                    }
                    duplicateIndexStringHash.put(s, AN_OBJECT_THAT_SAVES_MEMORY);
                    duplicateIndexValueHash.put(v, AN_OBJECT_THAT_SAVES_MEMORY);
                    //#enddebug
                    initTimeToKeyRMSIndexHash.put(currentRecordKeyAsInteger, keyIndexBytes);
                    initTimeReferencedValueIntegers.addElement(valueRecordIdAsInteger);

                    // Run startup task
                    if (startupTask != null) {
                        startupTask.execForEachKey(RMSFastCache.this, key);
                    }
                } catch (final DigestException e) {
                    //#debug
                    L.e("Can not read index entry, deleting", "" + currentRecordTaskKeyIndex, e);
                    deleteCurrentKey();
                } catch (final FlashDatabaseException e) {
                    //#debug
                    L.e("Can not read index entry, deleting", "" + currentRecordTaskKeyIndex, e);
                    deleteCurrentKey();
                } catch (final UnsupportedEncodingException e) {
                    //#debug
                    L.e("Can not read index entry, deleting", "" + currentRecordTaskKeyIndex, e);
                    deleteCurrentKey();
                } catch (final RecordStoreException e) {
                    //#debug
                    L.e("Can not read index entry, deleting", "" + currentRecordTaskKeyIndex, e);
                    deleteCurrentKey();
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
     */
    private void indexHashPut(final long digest, final int keyRecordId, final int valueRecordId) {
        final Long l = RMSKeyUtils.toIndexHash(keyRecordId, valueRecordId);

        synchronized (mutex) {
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
        synchronized (mutex) {
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
        synchronized (mutex) {
            final Long keyAndValueIndexes = indexHashGet(digest);

            if (keyAndValueIndexes == null) {
                return null;
            }

            final int keyIndex = RMSKeyUtils.toKeyIndex(keyAndValueIndexes);
            try {
                final byte[] indexBytes = keyRS.getRecord(keyIndex);
                return RMSKeyUtils.toStringKey(indexBytes);
            } catch (RecordStoreException e) {
                throw new FlashDatabaseException("Error converting toString(digest): " + e);
            } catch (UnsupportedEncodingException e) {
                throw new FlashDatabaseException("Error converting toString(digest): " + e);
            }
        }
    }

    /**
     * The file name
     *
     * @return the keyRMS name based on the cache priority
     */
    private String getKeyRSName() {
        return RMSFastCache.getKeyRSName(priority);
    }

    private static String getKeyRSName(final char priority) {
        return "" + RECORD_HASH_PREFIX + priority + "key";
    }

    /**
     * The file name
     *
     * @return the valueRMS name based on the cache priority
     */
    private String getValueRSName() {
        return getValueRSName(priority);
    }

    private static String getValueRSName(final char priority) {
        return "" + RECORD_HASH_PREFIX + priority + "val";
    }

    /**
     * Get the value associated with this key digest from phone flash memory
     *
     * @param digest
     * @return the bytes of the value
     * @throws FlashDatabaseException
     */
    public byte[] get(final long digest, final boolean markAsLeastRecentlyUsed) throws FlashDatabaseException {
        synchronized (mutex) {
            final Long dig = new Long(digest);
            final Long hashValue = ((Long) indexHash.get(dig));

            if (hashValue != null) {
                try {
                    final int valueIndex = RMSKeyUtils.toValueIndex(hashValue);
                    final byte[] bytes = valueRS.getRecord(valueIndex);

                    if (markAsLeastRecentlyUsed) {
                        indexHash.get(dig);
                    }

                    return bytes;
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
            throw new NullPointerException("You attempted to put a null key to the cache");
        }
        if (value == null) {
            throw new NullPointerException("You attempted to put null data to the cache");
        }

        synchronized (mutex) {
            try {
                final long digest = CryptoUtils.getInstance().toDigest(key);
                final Long indexEntry = indexHashGet(digest);
                final int valueRecordId;
                final int keyRecordId;

                setStoreFlag();
                byte[] byteKey = null;
                if (indexEntry == null) {
                    valueRecordId = valueRS.addRecord(value, 0, value.length);
                    byteKey = RMSKeyUtils.toIndexBytes(key, valueRecordId);
                    keyRecordId = keyRS.addRecord(byteKey, 0, byteKey.length);
                    indexHashPut(digest, keyRecordId, valueRecordId);

                    //#debug
                    L.i(this, "put(" + key + ") digest=" + Long.toString(digest, 16), "Value added to RMS=" + valueRS.getName() + " index=" + valueRecordId + " bytes=" + value.length + " keyIndex=" + keyRecordId);
                } else {
                    valueRecordId = RMSKeyUtils.toValueIndex(indexEntry);
                    valueRS.setRecord(valueRecordId, value, 0, value.length);
                    //#debug
                    L.i(this, "put(" + key + ") digest=" + Long.toString(digest, 16), "Value overwrite to RMS=" + valueRS.getName() + " index=" + valueRecordId + " bytes=" + value.length);
                }
                clearStoreFlag();
                rmsByteSize += value.length + (byteKey == null ? 0 : byteKey.length);
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
        synchronized (mutex) {
            try {
                final Long indexEntry = indexHashGet(digest);

                if (indexEntry != null) {

                    final Long dig = new Long(digest);
                    indexHash.remove(dig);
                    final int valueRecordId = RMSKeyUtils.toValueIndex(indexEntry);
                    final int keyRecordId = RMSKeyUtils.toKeyIndex(indexEntry);
                    int size = 0;
                    try {
                        final byte[] keyBytes = keyRS.getRecord(keyRecordId);
                        final byte[] valueBytes = valueRS.getRecord(valueRecordId);
                        size = valueBytes.length + keyBytes.length;
                    } catch (Exception e) {
                        //#debug
                        L.e(this, "removeData", "can't read", e);
                    }
                    setStoreFlag();
                    valueRS.deleteRecord(valueRecordId);
                    keyRS.deleteRecord(keyRecordId);
                    clearStoreFlag();
                    rmsByteSize -= size;
                } else {
                    //#debug
                    L.i("*** Can not remove from RMS, digest not found", "" + digest + " - " + toString());
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
    public Enumeration getDigests() {
        synchronized (mutex) {
            return indexHash.keys();
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
            synchronized (mutex) {
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
                    recordStore.deleteRecord(currentRecordTaskKeyIndex);
                } catch (Exception e) {
                    //#debug
                    L.e("Can not clear rms", "" + currentRecordTaskKeyIndex, e);
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
        synchronized (mutex) {
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

    public long getSize() {
        synchronized (mutex) {
            return rmsByteSize;
        }
    }

    public void close() throws FlashDatabaseException {
        synchronized (mutex) {
            try {
                super.close();
                try {
                    //#debug
                    L.i(this, "Close", "valueRS");
                    valueRS.closeRecordStore();
                } finally {
                    //#debug
                    L.i(this, "Close", "keyRS");
                    keyRS.closeRecordStore();
                }
                //#debug
                L.i(this, "Close", "valurRS and keyRS closed");
                clearDirtyFlagAfterNormalShutdown();
            } catch (RecordStoreException ex) {
                //#debug
                L.e(this, "RMS close exception", this.toString(), ex);
                throw new FlashDatabaseException("RMS close exception: " + ex);
            }
        }
    }

    //#mdebug
    public String toString() {
        final StringBuffer sb = new StringBuffer();

        sb.append(super.toString());
        sb.append("\n");
        try {
            sb.append("keyRMS.numRecords=");
            sb.append(this.keyRS.getNumRecords());
            sb.append(" keyRMS.getSize=");
            sb.append(this.keyRS.getSize());
            sb.append(" keyRMS.getSizeAvailable=");
            sb.append(this.keyRS.getSizeAvailable());
        } catch (Throwable t) {
            sb.append("(can not get keyRMS data");
            sb.append(t);
        }

        try {
            sb.append(" valueRMS.numRecords=");
            sb.append(this.valueRS.getNumRecords());
            sb.append(" valueRMS.getSize=");
            sb.append(this.valueRS.getSize());
            sb.append(" valueRMS.getSizeAvailable=");
            sb.append(this.valueRS.getSizeAvailable());
        } catch (Throwable t) {
            sb.append("(can not get valueRMS data");
            sb.append(t);
        }

        return sb.toString();
    }
    //#enddebug

    public void maintainDatabase() throws FlashDatabaseException {
        try {
            toggleRMS(this.getKeyRSName(), keyRS);
        } catch (RecordStoreException ex) {
            //#debug
            L.e(this, "maintainDatabase", getKeyRSName(), ex);
            throw new FlashDatabaseException("Can not toggle " + getKeyRSName());
        }
        try {
            toggleRMS(this.getValueRSName(), valueRS);
        } catch (RecordStoreException ex) {
            //#debug
            L.e(this, "maintainDatabase", getValueRSName(), ex);
            throw new FlashDatabaseException("Can not toggle " + getValueRSName());
        }
    }

    /**
     * Extend this to perform an operation on all records in a record store
     * using the forEachRecord() method
     */
    private static abstract class RecordTask {

        public int currentRecordTaskKeyIndex;

        final void exec(final int recordIndex) {
            this.currentRecordTaskKeyIndex = recordIndex;
            exec();
        }

        abstract void exec();
    }
}
