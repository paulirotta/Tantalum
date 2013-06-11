package org.tantalum.jme;

/**
 * User: kink
 * Date: 2013.05.31
 * Time: 01:08
 */

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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
    public void indexKeyValueIsReversible() {
        long indexHash = keyUtils.toIndexHash(13, 0);
        assertEquals(13, keyUtils.toKeyIndex(indexHash));
    }

    @Test
    public void indexValueForValIsReversible() {
        long indexHash = keyUtils.toIndexHash(0, 97);
        assertEquals(97, keyUtils.toValueIndex(indexHash));
    }

    @Test
    public void hashValueCollision() {
        final int keyIndex = 2;
        final int valueIndex = 2;
        final Long hashValue = 8589934594l;

        assertEquals(hashValue, keyUtils.toIndexHash(keyIndex, valueIndex));


        final int numberOfEntries = 500;
        Set<Long> values = new HashSet<Long>(numberOfEntries * numberOfEntries);
        int counter = 0;

        for (int key = 1; key < numberOfEntries; key++) {
            for (int value = 1; value < numberOfEntries; value++) {
                if (counter++ % 10000 == 0) {
                    System.out.println("Checked " + counter + " values");
                }

                Long hash = keyUtils.toIndexHash(key, value);
                if (values.contains(hash)) {
                    System.out.println("Hash value " + hash + " already exists");
                }

                values.add(hash);
            }
        }

        assertEquals((numberOfEntries - 1) * (numberOfEntries - 1), values.size());
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


