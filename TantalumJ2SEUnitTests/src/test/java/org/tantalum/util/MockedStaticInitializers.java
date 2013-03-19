/*
 Copyright (c) 2013, Paul Houghton and Futurice Oy
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
package org.tantalum.util;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.tantalum.PlatformUtils;


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
 * @author kink
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({L.class, PlatformUtils.class})
@SuppressStaticInitializationFor(
        {"org.tantalum.util.L",
                "org.tantalum.PlatformUtils",
                "org.tantalum.storage.StaticCache",
                "org.tantalum.net.StaticWebCache"})
public abstract class MockedStaticInitializers {

    @Before
    public final void tantalumMockTestFixture() {
        PowerMockito.mockStatic(PlatformUtils.class);
        PowerMockito.mockStatic(L.class);
    }

}
