/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3;

import android.app.Activity;
import android.os.Bundle;
import com.futurice.tantalum3.rms.AndroidDatabase;

/**
 *
 * @author phou
 */
public abstract class TantalumActivity extends Activity {

    public TantalumActivity() {
        super();
        
        PlatformUtils.setProgram(this);
        Worker.init(this, 4);
    }

    /**
     * Call this to close your MIDlet in an orderly manner, exactly the same way
     * it is closed if the system sends you a destoryApp().
     *
     * Ongoing Work tasks will complete, or if you set unconditional then they
     * will complete within 3 seconds.
     *
     * @param unconditional
     */
    public void exitMIDlet(final boolean unconditional) {
        Worker.shutdown(unconditional);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AndroidDatabase.setContext(((Activity) this).getApplicationContext());
    }

    /**
     * Do not call this directly. Instead, call exitMidlet(false) to avoid
     * common errors.
     *
     * If you do for some reason call this directly, realize the MIDlet will
     * exit immediately after the call is complete, rather than wait for you to
     * call notifyDestroyed() once an ongoing file or RMS write actions are
     * completed. Usually, this is not desirable, call exitMidlet() instead and
     * ongoing tasks will complete.
     *
     * If you want something done during shutdown, use
     * Worker.queueShutdownTask(Workable) and it will be handled for you.
     *
     * @param unconditional
     * @throws MIDletStateChangeException
     */
    protected final void destroyApp(final boolean unconditional) {
        exitMIDlet(unconditional);
    }

    /**
     * Do nothing, not generally used
     *
     */
    protected void pauseApp() {
    }
}
