package com.futurice.tantalum2.rms;

import com.futurice.tantalum2.DefaultResultHandler;

/**
 * Read data asynchronously from the Record Management System
 *
 * @author ssaa
 */
public class RMSReader extends DefaultResultHandler {

    final String recordStoreName;
    private byte[] data;

    public RMSReader(final String recordStoreName) {
        this.recordStoreName = recordStoreName;
    }

    public String getValue() {
        if (data == null) {
            return "";
        }
        return new String(data);
    }

    public byte[] getData() {
        return data;
    }

    public boolean work() {
        this.data = RMSUtils.readData(this.recordStoreName);

        return true;
    }
}
