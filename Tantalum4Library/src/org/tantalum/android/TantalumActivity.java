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

    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AndroidCache.setContext(getApplicationContext());
    }

    protected void onDestroy() {
        Worker.shutdown(true); // Closed database and wait for similar orderly exit tasks
        
        super.onDestroy();
    }
}
