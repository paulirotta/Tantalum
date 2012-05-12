package com.futurice.tantalum2.log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;
import javax.microedition.io.CommConnection;
import javax.microedition.io.Connector;

/**
 * Logger class that can be used for debugging when phone is connected via USB
 * cable.
 *
 * @author mark voit
 */
public final class UsbLog extends Log {

    private static final byte[] LFCR = "\n\r".getBytes();
    private static final Vector byteArrayQueue = new Vector();
    private final UsbWriter usbWriter = new UsbWriter();
    private OutputStream os = null;
    private static CommConnection comm = null;

    /**
     * Constructor. Initialize comm port.
     */
    public UsbLog() {
        try {
            final String commPort = System.getProperty("microedition.commports");
            if (commPort != null) {
                comm = (CommConnection) Connector.open("comm:" + commPort);
                os = comm.openOutputStream();
                new Thread(usbWriter).start();
            }
        } catch (IOException ex) {
        }
    }

    /**
     * Prints given string to comm port.
     *
     * @param string string to print
     */
    protected synchronized void printMessage(final String string) {
        if (os != null) {
            byteArrayQueue.addElement(string.getBytes());
            this.notifyAll();
//            try {
//                os.write((byte[]) string.getBytes());
//                os.write(LFCR);
//            } catch (Exception e) {
//                try {
//                    os.close();
//                } catch (Exception e2) {
//                }
//                os = null;
//            }
        }
    }

    /**
     * Closes output stream.
     *
     * Call this in MIDlet.destroyApp() if you are using USB logging, otherwise
     * you may not release the serial port for your next debug session.
     */
    public void shutdown() {
        if (usbWriter != null) {
            synchronized (this) {
                usbWriter.shutdownStarted = true;
                this.notifyAll();
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
    }

    private final class UsbWriter implements Runnable {

        boolean shutdownStarted = false;
        boolean shutdownComplete = false;

        public void run() {
            try {
                while (!shutdownStarted || !byteArrayQueue.isEmpty()) {
                    synchronized (UsbLog.this) {
                        if (byteArrayQueue.isEmpty()) {
                            UsbLog.this.wait(1000);
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
}
