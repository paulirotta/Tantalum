/*
 * RMSFastCacheTest.java
 * JMUnit based test
 *
 * Created on 05-Apr-2013, 12:39:31
 */
package org.tantalum.jme;

import java.io.UnsupportedEncodingException;
import java.security.DigestException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.tantalum.MockedStaticInitializers;
import org.tantalum.storage.FlashDatabaseException;
import org.tantalum.util.L;

/**
 * @author phou
 */
public class RMSFastCacheTest extends MockedStaticInitializers {
    RMSFastCache rmsFastCache;

    @Before
    public final void httpGetterTestFixture() {
        createMocks();
    }

    private void createMocks() {
        PowerMockito.mockStatic(L.class);
        rmsFastCache = Mockito.mock(RMSFastCache.class);
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
