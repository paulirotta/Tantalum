/*
 Copyright © 2012 Paul Houghton and Futurice on behalf of the Tantalum Project.
 All rights reserved.

 Tantalum software shall be used to make the world a better place for everyone.

 This software is licensed for use under the Apache 2 open source software license,
 http://www.apache.org/licenses/LICENSE-2.0.html

 You are kindly requested to return your improvements to this library to the
 open source community at http://projects.developer.nokia.com/Tantalum or 
 http://github.com/TantalumMobile

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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.tantalum.MockedStaticInitializers;
import org.tantalum.PlatformUtils;
import org.tantalum.util.L;

import java.io.IOException;
import java.util.Vector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;
import static org.mockito.Mockito.*;
import org.tantalum.Task;

/**
 * Unit tests for the default implementation of
 * <code>HttpGetter</code>
 *
 * @author Kai Inkinen <kai.inkinen@futurice.com>; github.com/kaiinkinen
 */
public class HttpGetterTest extends MockedStaticInitializers {

    boolean cancelCalled;
    HttpGetter getter;
    PlatformUtils.HttpConn httpConn;

    @Before
    public final void httpGetterTestFixture() {
        createMocks();
        cancelCalled = false;
        getter = new MyTestHttpGetter();
    }

    /**
     * It's valid to call exec with a non-string key, but we'd better blow up
     * already before casting a random object to String, or converting it using
     * toString()
     */
    @Test
    public void canceledWhenNonStringKey() throws InterruptedException {
        getter.exec(new Object());
        assertEquals("Getter was not correctly cancelled for non-String url", getter.getStatus(), Task.CANCELED);
    }

    @Ignore
    @Test
    public void responseCodesIn400RangeAreConsideredBad() throws IOException, InterruptedException {
        final String url = "http://github.com/TantalumMobile";

        /*
         * Setup test 
         */
        // Return a unauthorized response code, no response body
        when(platformUtils.getHttpGetConn(eq(url), any(Vector.class), any(Vector.class))).thenReturn(httpConn);
        when(httpConn.getResponseCode()).thenReturn(HttpGetter.HTTP_401_UNAUTHORIZED);
        when(httpConn.getLength()).thenReturn(0L);

        /*
         * Execute
         */
        final Object returnValue = getter.exec(url);

        /*
         * Assert
         */
        verify(httpConn).close();
        assertEquals("Return null when get is unsuccessful", null, returnValue);
        assertEquals(HttpGetter.HTTP_401_UNAUTHORIZED, getter.getResponseCode());
        assertTrue("Task was not correctly cancelled after error", cancelCalled);
    }

    @Ignore
    @Test
    public void responseCodesIn300RangeAreConsideredBad() throws IOException, InterruptedException {
        final String url = "http://github.com/TantalumMobile";

        /*
         * Setup test 
         */
        // Return a unauthorized response code, no response body
        when(platformUtils.getHttpGetConn(eq(url), any(Vector.class), any(Vector.class))).thenReturn(httpConn);
        when(httpConn.getResponseCode()).thenReturn(HttpGetter.HTTP_307_TEMPORARY_REDIRECT);
        when(httpConn.getLength()).thenReturn(0L);

        /*
         * Execute
         */
        final Object returnValue = getter.exec(url);

        /*
         * Assert
         */
        verify(httpConn).close();
        assertEquals("Return null when get is unsuccessful", null, returnValue);
        assertEquals(HttpGetter.HTTP_307_TEMPORARY_REDIRECT, getter.getResponseCode());
        assertTrue("Task was not correctly cancelled after error", cancelCalled);
    }

    private void createMocks() {
        PowerMockito.mockStatic(L.class);
        httpConn = Mockito.mock(PlatformUtils.HttpConn.class);
    }

    private class MyTestHttpGetter extends HttpGetter {
        MyTestHttpGetter() {
            super(Task.HIGH_PRIORITY);
        }

        @Override
        public boolean cancel(final boolean mayInterruptIfRunning, final String reason, final Throwable t) {
            cancelCalled = true;
            return super.cancel(mayInterruptIfRunning, reason, t);
        }
    }
}
