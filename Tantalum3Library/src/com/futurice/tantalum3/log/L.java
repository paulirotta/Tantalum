/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3.log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;
import javax.microedition.io.CommConnection;
import javax.microedition.io.Connector;

/**
 * Utility class for logging.
 *
 * @author mark voit, paul houghton
 */
public class L {

//#if UsbDebug
//#     private static final byte[] LFCR = "\n\r".getBytes();
//#     private static final Vector byteArrayQueue = new Vector();
//#     private static final L.UsbWriter usbWriter = new L.UsbWriter();
//#     private static OutputStream os = null;
//#     private static CommConnection comm = null;
//#endif
    private static final long startTime = System.currentTimeMillis();

//#if UsbDebug
//#     /*
//#      * Initialize comm port.
//#      */
//#     static {
//#         try {
//#             final String commPort = System.getProperty("microedition.commports");
//#             if (commPort != null) {
//#                 comm = (CommConnection) Connector.open("comm:" + commPort);
//#                 os = comm.openOutputStream();
//#                 new Thread(usbWriter).start();
//#             }
//#         } catch (IOException ex) {
//#         }
//#     }
//#endif    
    
    /**
     * Logs an "information" message.
     *
     * @param tag name of the class logging this message
     * @param message message to i
     */
    public final static void i(final String tag, final String message) {
//#debug
        printMessage(getMessage(tag, message));
    }

    /**
     * Logs an error message and throwable
     *
     * @param tag name of the class logging this message
     * @param message message to i
     * @param th throwable to i
     */
    public final static void e(final String tag, final String message, final Throwable th) {
//#mdebug
        printMessage(getMessage(tag, message) + ", EXCEPTION: " + th);
        if (th != null) {
            th.printStackTrace();
        }
//#enddebug
    }

    /**
     * Prints given string to system out.
     *
     * @param string string to print
     */
    protected synchronized static void printMessage(final String string) {
//#if UsbDebug
//#         if (os != null) {
//#             byteArrayQueue.addElement(string.getBytes());
//#             L.class.notifyAll();
//#         }
//#else
        System.out.println(string);
//#endif
    }

    /**
     * Get formatted message string.
     *
     * @return message string
     */
    private static String getMessage(final String tag, final String message) {
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

        return sb.toString();
    }

    /**
     * Close any open resources. This is the last action before the MIDlet calls
     * MIDlet.notifyDestoryed(). This is used by UsbLog.
     *
     */
    public static void shutdown() {
//#if UsbDebug
//#         if (usbWriter != null) {
//#             synchronized (L.class) {
//#                 usbWriter.shutdownStarted = true;
//#                 L.class.notifyAll();
//#             }
//# 
//#             // Give the queue time to flush final messages
//#             synchronized (usbWriter) {
//#                 try {
//#                     if (!usbWriter.shutdownComplete) {
//#                         usbWriter.wait(1000);
//#                     }
//#                 } catch (InterruptedException ex) {
//#                 }
//#             }
//#         }
//#else
        printMessage("Tantalum log shutdown");
//#endif        
    }

//#if UsbDebug
//#     private static final class UsbWriter implements Runnable {
//# 
//#         boolean shutdownStarted = false;
//#         boolean shutdownComplete = false;
//# 
//#         public void run() {
//#             try {
//#                 while (!shutdownStarted || !byteArrayQueue.isEmpty()) {
//#                     synchronized (L.class) {
//#                         if (byteArrayQueue.isEmpty()) {
//#                             L.class.wait(1000);
//#                         }
//#                     }
//#                     while (!byteArrayQueue.isEmpty()) {
//#                         os.write((byte[]) byteArrayQueue.firstElement());
//#                         byteArrayQueue.removeElementAt(0);
//#                         os.write(LFCR);
//#                     }
//#                     os.flush();
//#                 }
//#             } catch (Exception e) {
//#             } finally {
//#                 try {
//#                     os.close();
//#                 } catch (IOException ex) {
//#                 }
//#                 os = null;
//#                 try {
//#                     comm.close();
//#                 } catch (IOException ex) {
//#                 }
//#                 synchronized (this) {
//#                     shutdownComplete = true;
//#                     this.notifyAll(); // All done- let shutdown() proceed
//#                 }
//#             }
//#         }
//#     }
//#endif
}
