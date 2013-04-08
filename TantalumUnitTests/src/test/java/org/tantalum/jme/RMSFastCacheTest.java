/*
 * RMSFastCacheTest.java
 * JMUnit based test
 *
 * Created on 05-Apr-2013, 12:39:31
 */
package org.tantalum.jme;

import java.io.UnsupportedEncodingException;
import java.security.DigestException;
import java.security.NoSuchAlgorithmException;
import javax.microedition.lcdui.Font;
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
import org.tantalum.PlatformUtils;
import org.tantalum.storage.FlashDatabaseException;
import org.tantalum.util.L;

/**
 * @author phou
 */
@PrepareForTest({RMSUtils.class})
public class RMSFastCacheTest extends MockedStaticInitializers {
    // Mock objects
    RMSUtils mockedRmsUtils;
    RecordStore mockedKeyRS;
    RecordStore mockedValueRS;
    
    // To test
    RMSFastCache rmsFastCache;

    @Before
    public final void httpGetterTestFixture() throws Exception {
        createMocks();
        rmsFastCache = new RMSFastCache('0');
    }

    private void createMocks() throws FlashDatabaseException, FlashDatabaseException, RecordStoreNotOpenException, RecordStoreException {
        mockedRmsUtils = PowerMockito.mock(RMSUtils.class);
        PowerMockito.mockStatic(RMSUtils.class);
        when(RMSUtils.getInstance()).thenReturn(mockedRmsUtils);
        
        mockedKeyRS = Mockito.mock(RecordStore.class);
        mockedValueRS = Mockito.mock(RecordStore.class);
        
        when(mockedRmsUtils.getRecordStore("_0key", true)).thenReturn(mockedKeyRS);
        when(mockedRmsUtils.getRecordStore("_0value", true)).thenReturn(mockedValueRS);
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
