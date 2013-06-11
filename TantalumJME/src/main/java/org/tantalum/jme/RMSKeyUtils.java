package org.tantalum.jme;

import java.io.UnsupportedEncodingException;

/**
 * RMS key conversion methods
 *
 * @author Kai Inkinen <kai.inkinen@futurice.com>; github.com/kaiinkinen
 */
public class RMSKeyUtils {
    public RMSKeyUtils() {
    }

    /**
     * Combine two 4 byte integers into one Long object for indexHash storage
     *
     * @param keyIndex
     * @param valueIndex
     * @return keyIndex and valueIndex coded into a single Long object to keep
     *         in memory as a Hashtable value
     */
    Long toIndexHash(final int keyIndex, final int valueIndex) {
        return new Long(((long) keyIndex << 32) | valueIndex);
    }

    /**
     * Extract the index into the key RMS from Long value stored in the
     * hashtable
     *
     * @param hashValue
     * @return record number in keyRMS
     */
    int toKeyIndex(final Long hashValue) {
        return (int) ((hashValue.longValue() >>> 32) & 0xFFFF);
    }

    /**
     * Extract the index into the value RMS from Long value stored in the
     * hashtable
     *
     * @param hashValue
     * @return record number in valueRMS
     */
    int toValueIndex(final Long hashValue) {
        return (int) (hashValue.longValue() & 0xFFFF);
    }

    /**
     * The string part extracted from the byte[] stored in the keyRMS
     *
     * @param indexBytes
     * @return the key used to add a value to the cache
     */
    String toStringKey(final byte[] indexBytes) throws UnsupportedEncodingException {
        return new String(indexBytes, 4, indexBytes.length - 4, "UTF-8");
    }

    /**
     * The int part extracted from the byte[] stored in the keyRMS
     *
     * @param indexBytes
     * @return the valueRMS index from which the associated value can be
     *         extracted
     */
    int toValueIndex(final byte[] indexBytes) {
        //TODO Write sanity check unit tests, most significant bit
        int i = indexBytes[0] & 0xFF;
        i <<= 8;
        i |= indexBytes[1] & 0xFF;
        i <<= 8;
        i |= indexBytes[2] & 0xFF;
        i <<= 8;
        i |= indexBytes[3] & 0xFF;

        return i;
    }
}
