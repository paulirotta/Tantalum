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
    private static UsbWriter usbWriter = null;
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
                usbWriter = new UsbWriter();
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
    protected void printMessage(final String string) {
        if (this.os != null) {
            final byte[] bytes = string.getBytes();
            synchronized (byteArrayQueue) {
                byteArrayQueue.addElement(bytes);
                byteArrayQueue.addElement(LFCR);
                byteArrayQueue.notifyAll();
            }
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
            synchronized (byteArrayQueue) {
                usbWriter.shutdown = true;
                byteArrayQueue.notifyAll();
            }

            // Give the queue time to flush final messages
            synchronized (this) {
                try {
                    this.wait(1000);
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    private class UsbWriter implements Runnable {

        public boolean shutdown = false;

        public void run() {
            try {
                byte[] bytes = null;

                while (!shutdown || !byteArrayQueue.isEmpty()) {
                    synchronized (byteArrayQueue) {
                        if (byteArrayQueue.size() > 0) {
                            bytes = (byte[]) byteArrayQueue.firstElement();
                            byteArrayQueue.removeElementAt(0);
                        } else {
                            bytes = null;
                            byteArrayQueue.wait();
                        }
                    }
                    if (bytes != null) {
                        os.write(bytes);
                    }
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
                    this.notifyAll(); // All done- let shutdown() proceed
                }
            }
        }
    }
}
