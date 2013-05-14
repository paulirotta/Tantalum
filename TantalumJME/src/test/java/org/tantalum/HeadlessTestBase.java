
package org.tantalum;

import java.util.HashMap;
import javax.microedition.lcdui.Display;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import static org.powermock.api.mockito.PowerMockito.*;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.tantalum.util.L;

/**
 * Base class for all JUnit tests requiring emulator functionality. Takes care
 * of initializing the device and emulator context.
 *
 * FIXME Not used for testing- refinement needed, or pure JSE approach
 * 
 * @author Jari
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({L.class})
@SuppressStaticInitializationFor({"org.tantalum.util.L"})
public abstract class HeadlessTestBase {

    private static DummyMidlet m;
    private static Display d;

    @BeforeClass
    public static void setUpClass() throws Exception {
        
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("MIDlet-Version", "1.0.0");
        System.setProperty("microedition.locale", "gb-GB");
        //System.setProperty("com.nokia.keyboard.type", "PhoneKeypad");

        m = new DummyMidlet();
        m.startApp();
        
        d = Display.getDisplay(m);
    }
    
    @Before
    public void mockL() {
        mockStatic(L.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }    
}
