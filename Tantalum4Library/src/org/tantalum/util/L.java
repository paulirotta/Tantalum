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
     * To enable logging from a phone connected to a computer by a USB cable,
     * open a terminal emulator on the window, set the COM port parameters in
     * Windows Devices, and call L.startUSBDebugging() at program start.
     *
     * This gives you a running list of how long each operation takes which is
     * helpful for on-device-debug, application profiling and seeing how
     * concurrency is behaving on device which may be different from the slower
     * emulator.
     */
    public static void startUsbDebug() {
//#debug
        L.log.routeDebugOutputToUsbSerialPort();
    }

//#mdebug
    /**
     * Start USB debugging mode. Note that Android supports this with the
     * default debugger through adb so calling this method is not needed and has
     * no effect on Android.
     */
    protected abstract void routeDebugOutputToUsbSerialPort();
//#enddebug

    /**
     * Logs an "information" message.
     *
     * @param tag name of the class logging this message
     * @param message message to i
     */
    public final static void i(final String tag, final String message) {
//#mdebug
        final StringBuffer sb = getMessage(tag, message);
        log.printMessage(sb, null);
//#enddebug
    }

    /**
     * Logs an error message and throwable
     *
     * @param tag message category
     * @param message explanation
     * @param t exception
     */
    public final static void e(final String tag, final String message, final Throwable t) {
//#mdebug
        final StringBuffer sb = getMessage(tag, message);
        sb.append(", EXCEPTION: ");
        sb.append(t);

        synchronized (L.class) {
            log.printMessage(sb, t);
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
        sb.append(Thread.currentThread().getName());
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
//#enddebug

    /**
     * Close any open resources. This is the last action before the program
     * calls notifyDestoryed(). This is used primarily by UsbLog to flush and
     * finalize the outbound message queue.
     *
     */
    public static void shutdown() {
//#mdebug
        L.log.printMessage(new StringBuffer("Tantalum log shutdown"), null);
        L.log.close();
//#enddebug
    }
}
