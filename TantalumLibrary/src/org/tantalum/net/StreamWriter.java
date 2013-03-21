package org.tantalum.net;
import java.io.OutputStream;

/**
 * The user of this interface can directly write to the OutputStream
 * of a HTTP POST operation
 */
public interface StreamWriter {
	/**
     *  Callback when OutputStream is ready for writing
     * 
     * @param InputStream
     */
	public void writeReady(OutputStream os);
}
