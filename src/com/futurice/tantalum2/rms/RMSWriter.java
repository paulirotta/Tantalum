package com.futurice.tantalum2.rms;

import com.futurice.tantalum2.Workable;
import com.futurice.tantalum2.Worker;
import javax.microedition.lcdui.Display;

/**
 *
 * @author ssaa
 */
public class RMSWriter implements Workable {

    final Display display;
    final Runnable eventDispatchThreadRunnable;
    final String recordStoreName;
    private String value;
    private byte[] data;

    public RMSWriter(final Display display, final Runnable eventDisplatchThreadRunnable, final String recordStoreName, final String value, final byte[] data) {
        this.display = display;
        this.eventDispatchThreadRunnable = eventDisplatchThreadRunnable;
        this.recordStoreName = recordStoreName;
        this.value = value;
        this.data = data;
    }

    public boolean work() {
        if (data != null) {
            RMSUtils.write(this.recordStoreName, this.data);
        } else {
            RMSUtils.write(this.recordStoreName, this.value);
        }
        Worker.queueEDT(eventDispatchThreadRunnable);
        
        return true;
    }
}
