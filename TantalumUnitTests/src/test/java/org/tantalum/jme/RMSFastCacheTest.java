/*
 * RMSFastCacheTest.java
 * JMUnit based test
 *
 * Created on 05-Apr-2013, 12:39:31
 */
package org.tantalum.jme;

import java.io.UnsupportedEncodingException;
import java.security.DigestException;
import javax.microedition.rms.RecordComparator;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordFilter;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.tantalum.MockedStaticInitializers;
import org.tantalum.storage.FlashCache;
import org.tantalum.storage.FlashDatabaseException;

/**
 * @author phou
 */
@PrepareForTest({RMSUtils.class, RMSFastCache.class, FlashCache.class})
public class RMSFastCacheTest extends MockedStaticInitializers {
    // Mock objects

    RMSUtils mockedRmsUtils;
    RecordStore mockedKeyRS;
    RecordStore mockedValueRS;
    // To test
    static RMSFastCache rmsFastCache;

    @Before
    public final void httpGetterTestFixture() throws Exception {
        createMocks();
        if (rmsFastCache == null) {
            rmsFastCache = new RMSFastCache('0');
        }
    }

    private void createMocks() throws FlashDatabaseException, FlashDatabaseException, RecordStoreNotOpenException, RecordStoreException {
        PowerMockito.mockStatic(RMSUtils.class);
        mockedRmsUtils = PowerMockito.mock(RMSUtils.class);

        Mockito.when(RMSUtils.getInstance()).thenReturn(mockedRmsUtils);

        mockedKeyRS = Mockito.mock(RecordStore.class);
        mockedValueRS = Mockito.mock(RecordStore.class);

        Mockito.when(mockedRmsUtils.getRecordStore("_0key", true)).thenReturn(mockedKeyRS);
        Mockito.when(mockedRmsUtils.getRecordStore("_0val", true)).thenReturn(mockedValueRS);

        when(mockedKeyRS.enumerateRecords(any(RecordFilter.class), any(RecordComparator.class), anyBoolean())).thenReturn(mock(RecordEnumeration.class));
        when(mockedValueRS.enumerateRecords(any(RecordFilter.class), any(RecordComparator.class), anyBoolean())).thenReturn(mock(RecordEnumeration.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionThrownWhenNullToDigest() throws DigestException, UnsupportedEncodingException {
        rmsFastCache.toDigest(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionThrownWhenNullToString() throws DigestException, UnsupportedEncodingException, FlashDatabaseException {
        rmsFastCache.toString(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionThrownWhenNullStringRemoveData() throws DigestException, UnsupportedEncodingException, FlashDatabaseException {
        rmsFastCache.removeData((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionThrownWhenNullDigestRemoveData() throws DigestException, UnsupportedEncodingException, FlashDatabaseException {
        rmsFastCache.removeData((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionThrownWhenPutNullKey() throws DigestException, UnsupportedEncodingException, FlashDatabaseException {
        rmsFastCache.put(null, new byte[16]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionThrownWhenPutNullData() throws DigestException, UnsupportedEncodingException, FlashDatabaseException {
        rmsFastCache.put("fah", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionThrownWhenGetNullDigest() throws DigestException, UnsupportedEncodingException, FlashDatabaseException {
        rmsFastCache.get((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionThrownWhenGetNullString() throws DigestException, UnsupportedEncodingException, FlashDatabaseException {
        rmsFastCache.get((String) null);
    }
}
