package org.tantalum.jme;

/**
 * User: kink
 * Date: 2013.05.31
 * Time: 01:08
 */

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;


/**
 * Unit tests for the key methods in the RMSFastCache.
 */
public class RMSKeyUtilsTest extends RMSTestUtils {

    RMSKeyUtils keyUtils;

    @Before
    public final void rmsFastCachedTestFixture() throws Exception {
        keyUtils = new RMSKeyUtils();
    }

    @Test
    public void indexKeyForTrivialValueIsReversible() {
        long indexHash = keyUtils.toIndexHash(13, 0);
        assertEquals(13, keyUtils.toKeyIndex(indexHash));
    }

    @Test
    public void indexValueForTrivialValIsReversible() {
        long indexHash = keyUtils.toIndexHash(0, 97);
        assertEquals(97, keyUtils.toValueIndex(indexHash));
    }

    @Test
    public void reversible() {
        final int value = 1 << 16;

        long indexHash = keyUtils.toIndexHash(value, value);

        assertEquals(value, keyUtils.toKeyIndex(indexHash));
        assertEquals(value, keyUtils.toValueIndex(indexHash));
    }


    @Test
    public void indexKeyCanReverseAllIntegerBits() {
        int one = 1;
        for (int bits = 0; bits < 32; bits++) {
            final int shifted = one << bits;
            long indexHash = keyUtils.toIndexHash(shifted, 0);
            assertEquals("For shift value " + bits + " values are not equal", shifted, keyUtils.toKeyIndex(indexHash));
        }
    }

    @Test
    public void indexValueCanReverseAllIntegerBits() {
        int one = 1;
        for (int bits = 0; bits < 32; bits++) {
            final int shifted = one << bits;
            long indexHash = keyUtils.toIndexHash(0, shifted);
            assertEquals("For shift value " + bits + " values are not equal", shifted, keyUtils.toValueIndex(indexHash));
        }
    }

    @Test
    public void valueIndexValuesCanStoreEntireIntegerRange() {
        long one = 1;
        long previous = 0;

        for (int bits = 0; bits < 32; bits++) {
            long shifted = one << bits;
            assertTrue("Overflow when shifting long. Previous value: " + previous + " now: " + shifted, shifted > previous);
            previous = shifted;

            int vi = keyUtils.toValueIndex(shifted);

            assertEquals((int) shifted, vi);
        }
    }

    @Test
    public void keyIndexValuesCanStoreEntireIntegerRange() {
        final int bitsInInteger = 32;
        final long one = 1;

        for (int bits = 0; bits < bitsInInteger; bits++) {
            final int keyIntegerValue = (int)(one << bits);
            final long shiftedLong = one << (bits + bitsInInteger);

            int ki = keyUtils.toKeyIndex(shiftedLong);

            assertEquals(keyIntegerValue, ki);
        }
    }

    @Test
    public void collidingHashValues() {
        Long l1 = -6745443400823342501l;
        Long l2 = -1687905435239235501l;

        final int l1KeyIndex = keyUtils.toKeyIndex(l1);
        final int l2KeyIndex = keyUtils.toKeyIndex(l2);
        assertNotEquals(l1KeyIndex, l2KeyIndex);

        final int l1ValueIndex = keyUtils.toValueIndex(l1);
        final int l2ValueIndex = keyUtils.toValueIndex(l2);
        assertNotEquals(l1ValueIndex, l2ValueIndex);
    }

}


