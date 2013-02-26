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
