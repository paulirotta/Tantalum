package org.tantalum.net;

import java.io.InputStream;

/**
 * The user of this interface can directly read from the InputStream
 * of the HttpGetter
 */
public interface StreamReader {
	
	 /**
     *  Callback when InputStream is ready for reading
     * 
     * @param InputStream
     */
	public void readReady(InputStream is);
}
