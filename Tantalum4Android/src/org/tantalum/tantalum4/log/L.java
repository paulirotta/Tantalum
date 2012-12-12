/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.tantalum4.log;

import org.tantalum.tantalum4.log.L;
import android.util.Log;

/**
 * Log utilities
 *
 * On J2ME, System.out to the console is written on a dedicated thread to
 * minimize performance impact of logging in critical code sections.
 *
 * If the project profile is "UsbLog", output is to the phone's com port and can
 * be viewed with a terminal emulator such as puttytel. Check the serial port
 * details with the phone connected in Control Panel - System - Device Manager -
 * Serial Ports and copy those settings (128000 baud 8N1 hardware flow control)
 * to your puttytel session.
 *
 * On Android, the standard i is used under tag "Tantalum"
 *
 * @author phou
 */
public class L {

    public static final String LOG_TANTALUM = "Tantalum"; // Android debug i key
    private static long startTime = System.currentTimeMillis();
    public final static L l = new L();

    /**
     * Logs an "information" message.
     *
     * @param tag name of the class logging this message
     * @param message message to i
     */
    public static final void i(final String tag, final String message) {
        Log.i(LOG_TANTALUM, getMessage(tag, message));
    }

    /**
     * Logs an error message and throwable.
     *
     * @param tag name of the class logging this message
     * @param message message to i
     * @param th throwable to i
     */
    public static final void e(final String tag, final String message, final Throwable th) {
        Log.e(LOG_TANTALUM, getMessage(tag, message) + ", EXCEPTION: " + th, th);
    }

    /**
     * Get formatted message string.
     *
     * @return message string
     */
    private static String getMessage(final String tag, final String message) {
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
    public static void shutdown() {
        Log.i(LOG_TANTALUM, "Tantalum log shutdown");
    }
}
