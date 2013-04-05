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
    public void illegalArgumentExceptionThrownWhenNullStringToDigest() throws DigestException, UnsupportedEncodingException {
        rmsFastCache.toDigest(null);
    }
}
