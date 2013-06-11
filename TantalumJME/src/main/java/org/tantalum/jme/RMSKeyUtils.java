package org.tantalum.jme;

import java.io.UnsupportedEncodingException;

/**
 * RMS key conversion methods
 *
 * @author Kai Inkinen <kai.inkinen@futurice.com>; github.com/kaiinkinen
 */
public class RMSKeyUtils {

    /**
     * Combine two 4 byte integers into one Long object for indexHash storage
     *
     * @param keyIndex
     * @param valueIndex
     * @return keyIndex and valueIndex coded into a single Long object to keep
     *         in memory as a Hashtable value
     */
    Long toIndexHash(final int keyIndex, final int valueIndex) {
        final long lki = (long) keyIndex;
        return new Long((lki << 32) | valueIndex);
    }

    /**
     * Extract the index into the key RMS from Long value stored in the
     * hashtable
     *
     * @param hashValue
     * @return record number in keyRMS
     */
    int toKeyIndex(final Long hashValue) {
        return (int) ((hashValue.longValue() >>> 32) & 0xFFFFFFFF);
    }

    /**
     * Extract the index into the value RMS from Long value stored in the
     * hashtable
     *
     * @param hashValue
     * @return record number in valueRMS
     */
    int toValueIndex(final Long hashValue) {
        return (int) (hashValue.longValue() & 0xFFFFFFFF);
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
