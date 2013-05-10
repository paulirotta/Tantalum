package org.tantalum;

import javax.microedition.midlet.MIDlet;

/**
 *
 * @author Jari
 */
public class DummyMidlet extends MIDlet {

    @Override
    protected void startApp() {
        PlatformUtils.getInstance().setProgram(this, 4, PlatformUtils.NORMAL_LOG_MODE);
    }

    @Override
    protected void pauseApp() {
    }

    @Override
    protected void destroyApp(final boolean unconditional) {
        PlatformUtils.getInstance().shutdown(unconditional, "destroyApp(" + unconditional + ") received from phone");
    }
}
