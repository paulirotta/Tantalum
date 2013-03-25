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
package org.tantalum.jme;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;
import javax.microedition.io.CommConnection;
import javax.microedition.io.Connector;
import org.tantalum.util.L;

/**
 * Debug-time logging implementation for JME
 *
 * To enable logging from a phone connected to a computer by a USB cable, open a
 * terminal emulator on the window, set the COM port parameters in Windows
 * Devices, and call L.startUSBDebugging() at program start.
 *
 * This gives you a running list of how long each operation takes which is
 * helpful for on-device-debug, application profiling and seeing how concurrency
 * is behaving on device which may be different from the slower emulator.
 *
 * @author phou
 */
public class JMELog extends L {

    private final OutputStream os;
//#mdebug
    private final byte[] CRLF = "\r\n".getBytes();
    private final Vector byteArrayQueue = new Vector();
    private final JMELog.UsbWriter usbWriter;
//#enddebug    

    /**
     * Create a new platform-specific logger implementation
     *
     * Do not access this directly. You can use it from the convenience
     * accessors
     * <code>L.i("blah", "blah")</code> and
     * <code>L.e("blah", "blah", myException)</code>
     *
     * @param routeDebugOutputToSerialPort
     */
    public JMELog(final boolean routeDebugOutputToSerialPort) {
        OutputStream s = null;
//#mdebug
        if (routeDebugOutputToSerialPort) {
            JMELog.UsbWriter writer = null;
            try {
                System.out.println("Routing debug output to USB serial port");
                writer = new JMELog.UsbWriter();
                s = writer.getOutputStream();
                new Thread(writer).start();
            } catch (IOException ex) {
                System.out.println("Usb debug output error: " + ex);
            }
            usbWriter = writer;
        } else {
            usbWriter = null;
        }
//#enddebug
        os = s;
    }

    /**
     * Add to the log a messsage stored in a StringBuffer with optional error or
     * exception (use null if information only)
     *
     * @param sb
     * @param t
     */
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
     * Close the JME Logging and (if needed) USB serial port resources
     *
     */
    protected void close() {
//#mdebug
        final JMELog.UsbWriter writer = usbWriter;

        if (writer != null) {
            synchronized (L.class) {
                writer.shutdownStarted = true;
                L.class.notifyAll();
            }

            // Give the queue time to flush final messages
            synchronized (writer) {
                try {
                    if (!writer.shutdownComplete) {
                        writer.wait(1000);
                    }
                } catch (InterruptedException ex) {
                }
            }
        }
//#enddebug        
    }

//#mdebug
    private final class UsbWriter implements Runnable {

        boolean shutdownStarted = false;
        boolean shutdownComplete = false;
        private final CommConnection comm;
        private final OutputStream os;

        public UsbWriter() throws IOException {
            final String commPort = System.getProperty("microedition.commports");

            comm = (CommConnection) Connector.open("comm:" + commPort);
            os = comm.openOutputStream();
        }

        public OutputStream getOutputStream() {
            return os;
        }

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
                        os.write(CRLF);
                    }
                    os.flush();
                }
            } catch (Exception e) {
            } finally {
                try {
                    os.close();
                } catch (IOException ex) {
                }
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
