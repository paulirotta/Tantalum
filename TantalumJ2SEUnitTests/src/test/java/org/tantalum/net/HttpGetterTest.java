/*
 Copyright © 2012 Paul Houghton and Futurice on behalf of the Tantalum Project.
 All rights reserved.

 Tantalum software shall be used to make the world a better place for everyone.

 This software is licensed for use under the Apache 2 open source software license,
 http://www.apache.org/licenses/LICENSE-2.0.html

 You are kindly requested to return your improvements to this library to the
 open source community at http://projects.developer.nokia.com/Tantalum

 The above copyright and license notice notice shall be included in all copies
 or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package org.tantalum.net;

import java.io.IOException;
import java.util.Vector;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.tantalum.PlatformUtils;
import org.tantalum.util.L;

/**
 *
 * @author kink
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({L.class, PlatformUtils.class})
@SuppressStaticInitializationFor({"org.tantalum.util.L",
    "org.tantalum.PlatformUtils"})
public class HttpGetterTest {

    public static final int RESPONSE_UNAUTHORIZED = 401;
    
    HttpGetter getter;
    PlatformUtils platformUtils;
    PlatformUtils.HttpConn httpConn;

    @Before
    public final void httpGetterTestFixture() {
        createMocks();

        getter = new HttpGetter();
    }

    /**
     * It's valid to call exec with a non-string key, but we'd better blow up
     * already before casting a random object to String, or converting it using
     * toString()
     */
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionThrownWhenNonStringKey() {
        getter.exec(new Object());
    }
    
    @Test
    public void responseCodesIn400RangeAreConsideredBad() throws IOException {
        final String url = "http://github.com/TantalumMobile";

        // Return a unauthorized response code, no response body
        when(platformUtils.getHttpGetConn(eq(url), any(Vector.class), any(Vector.class))).thenReturn(httpConn);
        when(httpConn.getResponseCode()).thenReturn(RESPONSE_UNAUTHORIZED);
        when(httpConn.getLength()).thenReturn(0L);

        final Object returnValue = getter.exec(url);

        // Assert
        verify(httpConn).close();
        assertEquals(url, returnValue);
        assertEquals(RESPONSE_UNAUTHORIZED, getter.getResponseCode());
        
    }

    private void createMocks() {
        PowerMockito.mockStatic(L.class);
        PowerMockito.mockStatic(PlatformUtils.class);
        platformUtils = Mockito.mock(PlatformUtils.class);
        Mockito.when(PlatformUtils.getInstance()).thenReturn(platformUtils);

        httpConn = Mockito.mock(PlatformUtils.HttpConn.class);
    }
}
