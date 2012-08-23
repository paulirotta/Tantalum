/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

/**
 * Utilities for support of each specific platform (J2ME, Android, ..)
 * 
 * The platform-specific local copy of this class replaces this base version
 * for other platforms.
 * 
 * @author phou
 */
public class PlatformUtils {

    private static MIDlet program;
    private static Display display;

    public static void setProgram(final Object program) {
        PlatformUtils.program = (MIDlet) program;
        PlatformUtils.display = Display.getDisplay((MIDlet) program);
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
        display.callSerially(runnable);
    }

    /**
     * The application calls this when all Workers have completed shutdown to
     * inform the phone that the program has closed itself.
     *
     * Do not call this directly, call Worker.shutdown() to initiate a close
     *
     */
    public static void notifyDestroyed() {
        program.notifyDestroyed();
    }
}
