/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.android;

import android.util.Log;
import org.tantalum.util.L;

/**
 *
 * @author phou
 */
public final class AndroidLog extends L {

    private static final String LOG_TANTALUM = "Tantalum"; // Android debug infoMessage key

    /**
     * Prints given string to Android logging.
     *
     * @param string string to print
     */
    protected void printMessage(final String message, final boolean errorMessage) {
        if (errorMessage) {
            Log.e(LOG_TANTALUM, message);

        } else {
            Log.i(LOG_TANTALUM, message);
        }
    }

    protected void close() {
    }

    protected void routeDebugOutputToUsbSerialPort() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
