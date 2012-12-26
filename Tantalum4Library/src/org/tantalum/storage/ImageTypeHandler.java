/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.storage;

/**
 *
 * @author phou
 */
public abstract class ImageTypeHandler implements DataTypeHandler {

    protected boolean processAlpha = false;
    protected boolean bestQuality = false;
    protected boolean preserveAspectRatio = true;
    protected int maxWidth = -1;
    protected int maxHeight = -1;

    public synchronized void setProcessAlpha(final boolean processAlpha) {
        this.processAlpha = processAlpha;
    }

    public synchronized boolean getProcessAlpha() {
        return this.processAlpha;
    }

    public synchronized void setBestQuality(final boolean bestQuality) {
        this.bestQuality = bestQuality;
    }

    public synchronized boolean getBestQuality() {
        return this.bestQuality;
    }

    public synchronized void setPreserveAspectRatio(final boolean preserveAspectRatio) {
        this.preserveAspectRatio = preserveAspectRatio;
    }

    public synchronized boolean getPreserveAspectRatio() {
        return this.preserveAspectRatio;
    }

    public synchronized void setMaxSize(final int maxWidth, final int maxHeight) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    public synchronized int getMaxWidth() {
        return this.maxWidth;
    }

    public synchronized int getMaxHeight() {
        return this.maxHeight;
    }
}
