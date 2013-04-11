/*
 Copyright (c) 2013, Paul Houghton and Futurice Oy
 All rights reserved.

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
     * Logs an "information" message.
     *
     * @param tag name of the class logging this message
     * @param message message to i
     */
    public static final void i(final String tag, final String message) {
//#mdebug
        final StringBuffer sb = getMessage(tag, message);
        
        synchronized (L.class) {
            PlatformUtils.getInstance().getLog().printMessage(sb, null);
        }
//#enddebug
    }

    /**
     * Logs an error message and throwable
     *
     * @param tag message category
     * @param message explanation
     * @param t exception
     */
    public static final void e(final String tag, final String message, final Throwable t) {
//#mdebug
        final StringBuffer sb = getMessage(tag, message);
        sb.append(", EXCEPTION: ");
        sb.append(t);

        synchronized (L.class) {
            PlatformUtils.getInstance().getLog().printMessage(sb, t);
            if (t != null) {
                t.printStackTrace();
            }
        }
//#enddebug
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
        String threadName = PlatformUtils.getInstance().isUIThread() ? "UI" : Thread.currentThread().getName();
        sb.append(threadName);
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
