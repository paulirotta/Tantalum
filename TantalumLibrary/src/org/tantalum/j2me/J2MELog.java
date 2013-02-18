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
package org.tantalum.j2me;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;
import javax.microedition.io.CommConnection;
import javax.microedition.io.Connector;
import org.tantalum.util.L;

/**
 * Debug-time logging implementation for J2ME
 *
 * To enable logging from a phone connected to a computer by a USB cable, open a
 * terminal emulator on the window, set the COM port parameters in Windows
 * Devices, and call L.startUSBDebugging() at program start.
 *
 * @author phou
 */
public class J2MELog extends L {

    private static OutputStream os = null;
//#mdebug
    private static final byte[] LFCR = "\n\r".getBytes();
    private static final Vector byteArrayQueue = new Vector();
    private static J2MELog.UsbWriter usbWriter = null;
    private static CommConnection comm = null;
//#enddebug    

    protected void printMessage(final StringBuffer sb, final Throwable t) {
//#mdebug
        if (t != null) {
            sb.append("Exception: ");
            sb.append(t.toString());
        }
        if (os != null) {
            byteArrayQueue.addElement(sb.toString().getBytes());
            synchronized (L.class) {
                L.class.notifyAll();
            }
        } else {
            synchronized (L.class) {
                System.out.println(sb.toString());
            }
        }
//#enddebug        
    }

    /**
     * Close the J2ME Logging and (if needed) USB serial port resources
     *
     */
    protected void close() {
//#mdebug
        if (usbWriter != null) {
            synchronized (L.class) {
                usbWriter.shutdownStarted = true;
                L.class.notifyAll();
            }

            // Give the queue time to flush final messages
            synchronized (usbWriter) {
                try {
                    if (!usbWriter.shutdownComplete) {
                        usbWriter.wait(1000);
                    }
                } catch (InterruptedException ex) {
                }
            }
        }
//#enddebug        
    }

    /**
     * Open a serial port debug connection through the USB cable to an attached
     * PC. This will result in a java security prompt on the phone.
     */
    protected void routeDebugOutputToUsbSerialPort() {
//#mdebug
        try {
            final String commPort = System.getProperty("microedition.commports");
            if (commPort != null) {
                comm = (CommConnection) Connector.open("comm:" + commPort);
                os = comm.openOutputStream();
                usbWriter = new J2MELog.UsbWriter();
                new Thread(usbWriter).start();
            }
        } catch (IOException ex) {
            System.out.println("Usb debug output error: " + ex);
        }
//#enddebug
    }

//#mdebug
    private static final class UsbWriter implements Runnable {

        boolean shutdownStarted = false;
        boolean shutdownComplete = false;

        public void run() {
            try {
                while (!shutdownStarted || !byteArrayQueue.isEmpty()) {
                    synchronized (L.class) {
                        if (byteArrayQueue.isEmpty()) {
                            L.class.wait(1000);
                        }
                    }
                    while (!byteArrayQueue.isEmpty()) {
                        os.write((byte[]) byteArrayQueue.firstElement());
                        byteArrayQueue.removeElementAt(0);
                        os.write(LFCR);
                    }
                    os.flush();
                }
            } catch (Exception e) {
            } finally {
                try {
                    os.close();
                } catch (IOException ex) {
                }
                os = null;
                try {
                    comm.close();
                } catch (IOException ex) {
                }
                synchronized (this) {
                    shutdownComplete = true;
                    this.notifyAll(); // All done- let shutdown() proceed
                }
            }
        }
    }
//#enddebug    
}
