/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.android;

import android.app.Activity;
import android.os.Bundle;
import org.tantalum.PlatformUtils;
import org.tantalum.Worker;

/**
 *
 * @author phou
 */
public abstract class TantalumActivity extends Activity {

    public TantalumActivity() {
        super();
        PlatformUtils.setProgram(this);
    }

    /**
     * Call this to close your Activity's database and final queued Tasks in an
     * orderly manner.
     *
     * Ongoing Work tasks will complete, or if you set unconditional then they
     * will complete within 3 seconds.
     *
     * @param unconditional
     */
    public void shutdown(final boolean unconditional) {
        Worker.shutdown(unconditional);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AndroidCache.setContext(((Activity) this).getApplicationContext());
    }

    protected void onDestroy() {
        shutdown(true);
        
        super.onDestroy();
    }
}
