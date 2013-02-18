package org.tantalum.util;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * User: kink
 * Date: 2013.02.19
 * Time: 00:09
 */
public class SortedVectorTest {

    private SortedVector collection;

    @Before
    public final void fixtureSortedVectorTest() {
        collection = new SortedVector(new SortedVector.Comparator() {
            public boolean before(Object o1, Object o2) {
                Integer int1 = (Integer) o1;
                Integer int2 = (Integer) o2;

                return int1 < int2;
            }
        });
    }

    @Test
    public void collectionIsInitiallyEmpty() {
        assertTrue(collection.isEmpty());
    }

    @Test
    public void collectionMaintainsSortOrder() {
        collection.addElement(2);
        collection.addElement(1);

        assertEquals(1, collection.firstElement());
        assertEquals(2, collection.lastElement());
    }

    @Test
    public void duplicatesAreAllowed() {
        Integer one = new Integer(1);
        Integer two = new Integer(1);

        assertNotSame(one, two);
        assertEquals(one, two);

        collection.addElement(one);
        collection.addElement(two);

        assertEquals(2, collection.size());
    }
}
