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
package org.tantalum.android;

import android.util.Log;
import org.tantalum.util.L;

/**
 * Android implementation of cross-platform logging.
 * 
 * Log lines are added to the Android log facility with tag "Tantalum". You can
 * use this to create a filtered view of the application notifications when
 * debugging with the TantalumDebug.jar version of the library. When you release
 * your application, use the Tantalum.jar version of the library to speed up
 * your application by suppressing the generation of informative debug lines in
 * the library.
 * 
 * @author phou
 */
public final class AndroidLog extends L {

    private static final String LOG_TANTALUM = "Tantalum"; // Android debug infoMessage key

    /**
     * Prints given string to Android logging.
     *
     * @param stringBuffer information to print for debugging purposes
     * @param t is this an error or just information
     */
    protected void printMessage(final StringBuffer stringBuffer, final Throwable t) {
        if (t == null) {
            Log.i(LOG_TANTALUM, stringBuffer.toString());
        } else {
            Log.e(LOG_TANTALUM, stringBuffer.toString());//, t);
        }
    }

    /**
     * Finalize logging. This does nothing on the Android platform.
     * 
     */
    protected void close() {
    }

    /**
     * This does nothing on the Android platform. Debug over USB cable is
     * automatic when running in an Android IDE.
     */
    protected void routeDebugOutputToUsbSerialPort() {
    }
}
