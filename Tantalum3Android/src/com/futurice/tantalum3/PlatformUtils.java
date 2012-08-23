/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3;

import android.app.Activity;
import com.futurice.tantalum3.rms.AndroidDatabase;

/**
 * Android-specific support routines for Tantalum
 * 
 * @author phou
 */
public class PlatformUtils {

    private static Activity program;

    public static void setProgram(final Object program) {
        PlatformUtils.program = (Activity) program;
//        AndroidDatabase.setContext(((Activity) program).getApplicationContext());
    }

    /**
     * Add an object to be executed in the foreground on the event dispatch
     * thread. All popular JavaME applications require UI and input events to be
     * called serially only from this one thread.
     *
     * Note that if you queue too many object on the EDT you risk out of memory
     * and (more commonly) a temporarily unresponsive user interface.
     *
     * @param runnable
     */
    public static void runOnUiThread(final Runnable runnable) {
        program.runOnUiThread(runnable);
    }

    /**
     * The application calls this when all Workers have completed shutdown to
     * inform the phone that the program has closed itself.
     *
     * Do not call this directly, call Worker.shutdown() to initiate a close
     *
     */
    public static void notifyDestroyed() {
        program.finish(); //TODO Is this the right call?
    }    
}
