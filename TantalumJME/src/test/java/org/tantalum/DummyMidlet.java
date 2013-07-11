package org.tantalum;

import javax.microedition.midlet.MIDlet;
import org.tantalum.jme.TantalumJME;

/**
 *
 * @author Jari
 */
public class DummyMidlet extends MIDlet {

    @Override
    protected void startApp() {
        TantalumJME.start(this, 4, PlatformUtils.NORMAL_LOG_MODE);
    }

    @Override
    protected void pauseApp() {
    }

    @Override
    protected void destroyApp(final boolean unconditional) {
        TantalumJME.stop("DummyMidlet end");
    }
}
