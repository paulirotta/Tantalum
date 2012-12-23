/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.util;

import org.tantalum.PlatformUtils;

/**
 * Utility class for logging.
 *
 * @author mark voit, paul houghton
 */
public abstract class L {

//#debug
    private static final long startTime = System.currentTimeMillis();
    private static L log = PlatformUtils.getLog();

    /**
     * Start sending debug output to a serial terminal on a PC attached by
     * Usb cable.
     * 
     * This gives you a running list of how long each operation takes which is
     * helpful for on-device-debug, application profiling and seeing how
     * concurrency is behaving on device which may be different from the slower
     * emulator.
     *
     */
    public static void startUsbDebug() {
//#debug
        L.log.routeDebugOutputToUsbSerialPort();
    }

//#debug   
    protected abstract void routeDebugOutputToUsbSerialPort();

    /**
     * Logs an "information" message.
     *
     * @param tag name of the class logging this message
     * @param message message to i
     */
    public final static void i(final String tag, final String message) {
//#debug
        log.printMessage(getMessage(tag, message).toString(), false);
    }

    /**
     * Logs an error message and throwable
     *
     * @param tag name of the class logging this message
     * @param message message to i
     * @param th throwable to i
     */
    public final static void e(final String tag, final String message, final Throwable th) {
//#mdebug
        final StringBuffer sb = getMessage(tag, message);
        sb.append(", EXCEPTION: ");
        sb.append(th);

        synchronized (L.class) {
            log.printMessage(sb.toString(), true);
            if (th != null) {
                th.printStackTrace();
            }
        }
//#enddebug
    }

//#mdebug
    /**
     * Prints given string to system out.
     *
     * @param string string to print
     */
    protected abstract void printMessage(final String string, final boolean errorMessage);

    /**
     * Get formatted message string.
     *
     * @return message string
     */
    private static StringBuffer getMessage(String tag, String message) {
        if (tag == null) {
            tag = "<null>";
        }
        if (message == null) {
            message = "<null>";
        }
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

        return sb;
    }

    protected abstract void close();
//#enddebug

    /**
     * Close any open resources. This is the last action before the program
     * calls notifyDestoryed(). This is used primarily by UsbLog to flush and
     * finalize the outbound message queue.
     *
     */
    public static void shutdown() {
//#mdebug
        L.log.printMessage("Tantalum log shutdown", false);
        L.log.close();
//#enddebug
    }
}
