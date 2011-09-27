/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2;


/**
 * Interface for logger classes.
 * 
 * @author mark voit
 */
public interface Logger {

    /**
     * Logs given message.
     * 
     * @param tag name of the class logging this message
     * @param message message to log
     */
    void log(final String tag, final String message);

    /**
     * Logs given throwable and message.
     * 
     * @param tag name of the class logging this message
     * @param message message to log
     * @param th throwable to log
     */
    void log(final String tag, final String message, final Throwable th);
}
