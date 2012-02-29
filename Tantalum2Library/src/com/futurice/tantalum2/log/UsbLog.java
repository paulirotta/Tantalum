package com.futurice.tantalum2.log;

import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.io.CommConnection;
import javax.microedition.io.Connector;

/**
 * Logger class that can be used for debugging when phone is connected via USB
 * cable.
 *
 * @author mark voit
 */
public final class UsbLog extends Log {

    private CommConnection comm = null;
    private OutputStream os = null;

    /**
     * Constructor. Initializes comm port.
     */
    public UsbLog() {
        try {
            String ports = System.getProperty("microedition.commports");

            if (-1 == ports.indexOf("USB", 0)) {
                throw new RuntimeException("No COM port found from system properties");
            }

            comm = (CommConnection) Connector.open("comm:" + ports);
            os = comm.openOutputStream();

        } catch (Exception e) {
        }
    }

    /**
     * Prints given string to comm port.
     *
     * @param string string to print
     */
    protected void printMessage(String string) {
        if (string != null && this.os != null) {
            try {
                this.os.write(string.getBytes());
                this.os.write("\n".getBytes());
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Closes output stream.
     */
    public void shutdown() {
        try {
            this.os.close();
        } catch (IOException ex) {
        }
        try {
            this.comm.close();
        } catch (IOException ex) {
        }
    }
}
