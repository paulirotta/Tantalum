package org.tantalum.util;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * User: kink
 * Date: 2013.02.19
 * Time: 00:09
 */
public class SortedVectorTest extends MockedStaticInitializers {

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

    @Test
    public void integerSequenceOrderIsCorrect() {
        Object o_1 = new Integer(10);
        Object o_2 = new Integer(20);
        Object o_3 = new Integer(50);
        Object o_4 = new Integer(40);
        Object o_5 = new Integer(30);
        collection.addElement(o_1);
        collection.addElement(o_2);
        collection.addElement(o_3);
        collection.addElement(o_4);
        collection.addElement(o_5);

        Integer[] expected = {new Integer(10), new Integer(20), new Integer(30), new Integer(40), new Integer(50)};
        for (int i = 0; i < collection.size(); i++) {
            assertEquals("sequence test " + (i + 1), expected[i], (Integer) collection.elementAt(i));
        }
    }

}
