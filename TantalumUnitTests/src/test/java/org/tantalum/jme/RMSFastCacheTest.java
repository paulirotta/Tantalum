/*
 * RMSFastCacheTest.java
 * JMUnit based test
 *
 * Created on 05-Apr-2013, 12:39:31
 */
package org.tantalum.jme;

import org.tantalum.MockedStaticInitializers;

/**
 * @author phou
 */
//FIXME
//@PrepareForTest({RMSUtils.class, RMSFastCache.class, FlashCache.class})
public class RMSFastCacheTest extends MockedStaticInitializers {
//    // Mock objects
//
//    RMSUtils mockedRmsUtils;
//    RecordStore mockedKeyRS;
//    RecordStore mockedValueRS;
//    // To test
//    static RMSFastCache rmsFastCache;
//
//    @Before
//    public final void httpGetterTestFixture() throws Exception {
//        createMocks();
//        if (rmsFastCache == null) {
//            rmsFastCache = new RMSFastCache('0');
//        }
//    }
//
//    private void createMocks() throws FlashDatabaseException, FlashDatabaseException, RecordStoreNotOpenException, RecordStoreException {
//        PowerMockito.mockStatic(RMSUtils.class);
//        mockedRmsUtils = PowerMockito.mock(RMSUtils.class);
//
//        Mockito.when(RMSUtils.getInstance()).thenReturn(mockedRmsUtils);
//
//        mockedKeyRS = Mockito.mock(RecordStore.class);
//        mockedValueRS = Mockito.mock(RecordStore.class);
//
//        Mockito.when(mockedRmsUtils.getRecordStore("_0key", true)).thenReturn(mockedKeyRS);
//        Mockito.when(mockedRmsUtils.getRecordStore("_0val", true)).thenReturn(mockedValueRS);
//
//        when(mockedKeyRS.enumerateRecords(any(RecordFilter.class), any(RecordComparator.class), anyBoolean())).thenReturn(mock(RecordEnumeration.class));
//        when(mockedValueRS.enumerateRecords(any(RecordFilter.class), any(RecordComparator.class), anyBoolean())).thenReturn(mock(RecordEnumeration.class));
//    }
//
//    @Test(expected = IllegalArgumentException.class)
//    public void illegalArgumentExceptionThrownWhenNullStringRemoveData() throws DigestException, FlashDatabaseException {
//        rmsFastCache.removeData((String) null);
//    }
//
//    @Test(expected = IllegalArgumentException.class)
//    public void illegalArgumentExceptionThrownWhenPutNullKey() throws DigestException, FlashDatabaseException {
//        rmsFastCache.put(null, new byte[16]);
//    }
//
//    @Test(expected = IllegalArgumentException.class)
//    public void illegalArgumentExceptionThrownWhenPutNullData() throws DigestException, FlashDatabaseException {
//        rmsFastCache.put("fah", null);
//    }
//
//    @Test(expected = IllegalArgumentException.class)
//    public void illegalArgumentExceptionThrownWhenGetNullString() throws DigestException, FlashDatabaseException {
//        rmsFastCache.get((String) null);
//    }
}
