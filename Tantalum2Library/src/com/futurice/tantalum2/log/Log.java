/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2.log;

/**
 * Utility class for logging.
 * 
 * @author mark voit, paul houghton
 */
public class Log {
    public static final Log l = new Log(); // Replace with UsbLog to debug by console from your PC
    private static long startTime = System.currentTimeMillis();

    /**
     * Logs given message.
     * 
     * @param tag name of the class logging this message
     * @param message message to log
     */
    public final void log(final String tag, final String message) {
        printMessage(getMessage(tag, message));
    }

    /**
     * Logs given throwable and message.
     * 
     * @param tag name of the class logging this message
     * @param message message to log
     * @param th throwable to log
     */
    public final void log(final String tag, final String message, final Throwable th) {
        printMessage(getMessage(tag, message) + ", EXCEPTION: " + th);
        if (th != null) {
            th.printStackTrace();
        }
    }

    /**
     * Prints given string to system out.
     * 
     * @param string string to print
     */
    protected void printMessage(final String string) {
        System.out.println(string);
    }

    /**
     * Get formatted message string.
     * 
     * @return message string
     */
    private String getMessage(final String tag, final String message) {
        return currentTime() + " (" + Thread.currentThread().getName() + "): " + tag + ": " + message;
    }

    /**
     * Return current time and thread name as string.
     * 
     * @return current time as string
     */
    private String currentTime() {
        final long t = System.currentTimeMillis() - startTime;
        return (t / 1000) + "." + (t % 1000);
    }
}
