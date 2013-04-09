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
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import org.tantalum.PlatformAdapter;
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
    private final JMELog.LogWriter usbWriter;
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
    public JMELog(final int logMode) {
        OutputStream s = null;
//#mdebug
        String uri = null;
        JMELog.LogWriter writer = null;
        switch (logMode) {
            case PlatformAdapter.NORMAL_LOG_MODE:
                break;

            case PlatformAdapter.USB_SERIAL_PORT_LOG_MODE:
                System.out.println("Routing debug output to USB serial port");
                final String commPort = System.getProperty("microedition.commports");
                uri = "comm:" + commPort;
                break;


            case PlatformAdapter.MEMORY_CARD_LOG_MODE:
                final String memoryCardPath = System.getProperty("fileconn.dir.memorycard");
                uri = memoryCardPath + "tantalum.log";
                System.out.println("Routing debug output to memory card log file: " + uri);
                break;

            default:
                throw new IllegalArgumentException("Log mode must be one of the pre-defined constants such as JMELog.NORMAL_LOG_MODE");
        }
        try {
            writer = initWriter(uri, logMode == PlatformAdapter.USB_SERIAL_PORT_LOG_MODE);
            if (writer != null) {
                s = writer.getOutputStream();
            }
        } catch (IOException ex) {
            System.out.println("Debug output setup error: " + ex);
        }
        usbWriter = writer;
//#enddebug
        os = s;
    }

//#mdebug
    private JMELog.LogWriter initWriter(final String uri, final boolean serialPortMode) throws IOException {
        JMELog.LogWriter writer = null;

        if (uri != null) {
            writer = new JMELog.LogWriter(uri, serialPortMode);
            new Thread(writer).start();
        }

        return writer;
    }
//#enddebug

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
        final JMELog.LogWriter writer = usbWriter;



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
                    //#debug
                    System.out.println("JMELog.LogWriter was interrupted while waiting one second during shutdown");
                }
            }
        }
//#enddebug        
    }

//#mdebug
    private final class LogWriter implements Runnable {

        boolean shutdownStarted = false;
        boolean shutdownComplete = false;
        private final Connection conn;
        private final OutputStream os;

        public LogWriter(final String uri, final boolean serialPortMode) throws IOException {
            if (serialPortMode) {
                final CommConnection connection = (CommConnection) Connector.open(uri);
                os = connection.openOutputStream();
                conn = connection;
            } else {
                final FileConnection connection = (FileConnection) Connector.open("file:///CFCard/newfile.txt", Connector.READ_WRITE);
                if (connection.exists()) {
                    System.out.println("Log file exists on the memory card, clearing old data... " + uri);
                    connection.truncate(0);
                } else {
                    connection.create();  // 
                }
                os = connection.openOutputStream();
                conn = connection;
            }
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
                    conn.close();
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
