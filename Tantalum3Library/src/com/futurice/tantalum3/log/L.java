/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3.log;

/**
 * Utility class for logging.
 *
 * @author mark voit, paul houghton
 */
public class L {

//#if UsbDebug
//#     public final static L l = new UsbLog();
//#else
    public final static L l = new L();
//#endif
    private static long startTime = System.currentTimeMillis();

    /**
     * Logs an "information" message.
     *
     * @param tag name of the class logging this message
     * @param message message to i
     */
    public final void i(final String tag, final String message) {
        //#debug
        printMessage(getMessage(tag, message));
    }

    /**
     * Logs an error message and throwable
     *
     * @param tag name of the class logging this message
     * @param message message to i
     * @param th throwable to i
     */
    public final void e(final String tag, final String message, final Throwable th) {
        //#mdebug
        printMessage(getMessage(tag, message) + ", EXCEPTION: " + th);
        if (th != null) {
            th.printStackTrace();
        }
        //#enddebug
    }

    /**
     * Prints given string to system out.
     *
     * @param string string to print
     */
    protected synchronized void printMessage(final String string) {
        System.out.println(string);
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
    }
}
