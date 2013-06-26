/*
 Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
 Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation.
 Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 affiliates. Other product and company names mentioned herein may be trademarks
 or trade names of their respective owners.

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
package org.tantalum;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.tantalum.util.L;
import org.tantalum.util.SystemOutLogger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Base class for tests requiring Tantalum methods to be mocked out.
 * <p/>
 * The RunWith-annotation uses the PowerMockRunner to execute tests. This allows
 * us to mock static methods etc.
 * <p/>
 * The PrepareForTest-annotation is mark any classes that need static mocking
 * <p/>
 * The SuppressStaticInitalizationFor-annotation turns off any static
 * initalizing code in the provided classes. This way we can avoid running
 * potentially difficult to test platform-dependant code.
 * <p/>
 * To use this class in tests, just extend it. If some other Tantalum-classes
 * need to be mocked out, add them here.
 *
 * @author Kai Inkinen <kai.inkinen@futurice.com>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({L.class, PlatformUtils.class, Worker.class})
@SuppressStaticInitializationFor(
        {"org.tantalum.util.L",
                "org.tantalum.PlatformUtils",
                "org.tantalum.storage.StaticCache",
                "org.tantalum.net.StaticWebCache"})
public abstract class MockedStaticInitializers {

    // Declare a publicly available mock for all the subclasses to use
    public PlatformUtils platformUtils;
    public L log;

    @Before
    public final void tantalumMockTestFixture() {
        PowerMockito.mockStatic(PlatformUtils.class);
        PowerMockito.mockStatic(L.class);

        platformUtils = mock(PlatformUtils.class);
        when(PlatformUtils.getInstance()).thenReturn(platformUtils);

        log = new SystemOutLogger();
        when(platformUtils.getLog()).thenReturn(log);
    }
}
