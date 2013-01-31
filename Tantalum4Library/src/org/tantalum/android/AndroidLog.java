/*
 Copyright © 2012 Paul Houghton and Futurice on behalf of the Tantalum Project.
 All rights reserved.

 Tantalum software shall be used to make the world a better place for everyone.

 This software is licensed for use under the Apache 2 open source software license,
 http://www.apache.org/licenses/LICENSE-2.0.html

 You are kindly requested to return your improvements to this library to the
 open source community at http://projects.developer.nokia.com/Tantalum

 The above copyright and license notice notice shall be included in all copies
 or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
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
            Log.e(LOG_TANTALUM, stringBuffer.toString(), t);
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
