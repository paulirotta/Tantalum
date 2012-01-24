/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2.log;

import javax.microedition.midlet.MIDlet;

/**
 *
 * @author pahought
 */
public class Log {

    private static long startTime;
    private static MIDlet midlet;

    public static void init(MIDlet midlet) {
        startTime = System.currentTimeMillis();
        Log.midlet = midlet;
    }

    private static String currentTime() {
        final long t = System.currentTimeMillis() - startTime;

        return "(" + Thread.currentThread().getName() + ") " + t / 1000 + "." + t % 1000 + " : ";
    }

    public static void log(String message) {
        System.out.println(/*currentTime() + */message);
    }

    /**
     * The application will stop on first exception. To change this with the
     * Netbeans preprocessor, set
     *    Project Properties | Build | Compiling | Debug block level
     * to some value greater than "debug". Or edit below.
     *
     * @param t
     * @param message
     */
    public static void logThrowable(Throwable t, String message) {
        System.out.println(currentTime() + "EXCEPTION: " + message + " - " + t);
        t.printStackTrace();
        //#debug
        midlet.notifyDestroyed();
    }

    public static void logNonfatalThrowable(Throwable t, String message) {
        System.out.println(currentTime() + "EXCEPTION: " + message + " - " + t);
        t.printStackTrace();
    }
}
