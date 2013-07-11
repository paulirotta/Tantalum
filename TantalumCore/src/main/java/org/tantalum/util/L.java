/*
 Copyright (c) 2013 Nokia Corporation. All rights reserved.
 Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation.
 Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 affiliates. Other product and company names mentioned herein may be trademarks
 or trade names of their respective owners.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
package org.tantalum.util;

import org.tantalum.PlatformUtils;
import org.tantalum.Task;

/**
 * Utility class for logging.
 *
 * @author mark voit, paul houghton
 */
public abstract class L {

//#mdebug
    public static final String CRLF = "\r\n";
    private static final long startTime = System.currentTimeMillis();
//#enddebug    

    /**
     * Add information to the log, including the class name of the callingObject
     * 
     * @param callingObject
     * @param tag
     * @param message 
     */
    public static final void i(final Object callingObject, final String tag, final String message) {
//#mdebug
        final StringBuffer sb = getMessage(callingObject, false, tag, message);

        synchronized (L.class) {
            PlatformUtils.getInstance().getLog().printMessage(sb, null);
        }
//#enddebug        
    }

    /**
     * Logs an "information" message.
     *
     * @param tag name of the class logging this message
     * @param message message to i
     */
    public static final void i(final String tag, final String message) {
        i(null, tag, message);
    }

    /**
     * Add an error to the log, including the class name of the callingObject
     * 
     * @param callingObject
     * @param tag
     * @param message
     * @param t 
     */
    public static final void e(final Object callingObject, final String tag, final String message, final Throwable t) {
//#mdebug
        final StringBuffer sb = getMessage(callingObject, true, tag, message);
        if (t != null) {
            sb.append(", EXCEPTION: ");
            sb.append(t);
        }
        sb.append(CRLF);

        synchronized (L.class) {
            PlatformUtils.getInstance().getLog().printMessage(sb, t);
            if (t != null) {
                t.printStackTrace();
                System.err.flush();
            }
        }
//#enddebug
    }

    /**
     * Logs an error message and
     * <code>Throwable</code>
     *
     * @param tag message category
     * @param message explanation
     * @param t exception
     */
    public static final void e(final String tag, final String message, final Throwable t) {
        //#debug
        e(null, tag, message, t);
    }

//#mdebug
    /**
     * Prints given string to system out.
     *
     * @param sb - String to print
     * @param t - Exception or Error to print
     */
    protected abstract void printMessage(final StringBuffer sb, final Throwable t);

    /**
     * Get formatted message string.
     *
     * @param tag
     * @return message string
     * @return
     */
    private static StringBuffer getMessage(final Object callingObject, final boolean prependLineOfStars, String tag, String message) {
        if (tag == null) {
            tag = "<null>";
        }
        if (message == null) {
            message = "<null>";
        }
        final long t = System.currentTimeMillis() - startTime;
        final StringBuffer sb = new StringBuffer(20 + tag.length() + message.length());
        final String millis = Long.toString(t % 1000);

        if (prependLineOfStars) {
            sb.append(CRLF);
            sb.append("******");
        }
        sb.append(CRLF);
        sb.append(t / 1000);
        sb.append('.');
        for (int i = millis.length(); i < 3; i++) {
            sb.append('0');
        }
        sb.append(millis);
        sb.append(" (");
        String threadName = PlatformUtils.getInstance().isUIThread() ? "UI" : Thread.currentThread().getName();
        sb.append(threadName);
        if (callingObject != null) {
            sb.append(' ');
            sb.append(Task.getClassName(callingObject));
        }
        sb.append("): ");
        sb.append(tag);
        sb.append(": ");
        sb.append(message);

        return sb;
    }

    /**
     * End the debugging session, closing any necessary resources such as USB
     * debug output connections.
     */
    protected abstract void close();

    /**
     * Close any open resources. This is the last action before the program
     * calls notifyDestoryed(). This is used primarily by UsbLog to flush and
     * finalize the outbound message queue.
     *
     */
    public static void shutdown() {
        PlatformUtils.getInstance().getLog().printMessage(new StringBuffer("Tantalum log shutdown"), null);
        PlatformUtils.getInstance().getLog().close();
    }
//#enddebug
}
