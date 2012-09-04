/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3.log;

import android.util.Log;

/**
 *
 * @author phou
 */
public class Logg {
    public static final String LOG_TANTALUM = "Tantalum"; // Android debug log key
    private static long startTime = System.currentTimeMillis();
    public final static Logg l = new Logg();

    /**
     * Logs given message.
     *
     * @param tag name of the class logging this message
     * @param message message to log
     */
    public final void log(final String tag, final String message) {
        Log.i(LOG_TANTALUM, getMessage(tag, message));
    }

    /**
     * Logs given throwable and message.
     *
     * @param tag name of the class logging this message
     * @param message message to log
     * @param th throwable to log
     */
    public final void log(final String tag, final String message, final Throwable th) {
        Log.e(LOG_TANTALUM, getMessage(tag, message) + ", EXCEPTION: " + th, th);
        if (th != null) {
            th.printStackTrace();
        }
    }

    /**
     * Get formatted message string.
     *
     * @return message string
     */
    private String getMessage(final String tag, final String message) {
        final long t = System.currentTimeMillis() - startTime;
        final StringBuffer sb = new StringBuffer(20 + tag.length() + message.length());
        final String millis = Long.toString(t % 1000);
        
        sb.append(t / 1000);
        sb.append('.');
        for (int i = millis.length(); i < 3; i++) {
            sb.append('0');
        }
        sb.append(millis);
        sb.append(" (");
        sb.append(Thread.currentThread().getName());
        sb.append("): ");
        sb.append(tag);
        sb.append(": ");
        sb.append(message);

        return sb.toString();
    }

    /**
     * Close any open resources. This is the last action before the MIDlet calls
     * MIDlet.notifyDestoryed(). This is used by UsbLog.
     *
     */
    public void shutdown() {
        Log.i(LOG_TANTALUM, "Tantalum log shutdown");
    }
}
