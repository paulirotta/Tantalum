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
package org.tantalum.storage;


/**
 * This DataTypeHandler for use with a StaticCache converts from compressed
 * byte[] format (JPG, PNG, etc) to the platform-specific Image class.
 *
 * Call PlatformUtils.getImageTypeHandler() to receive the implementation
 * appropriate for you current platform.
 *
 * You can optionally set parameters on how images are handled to automatically
 * shrink images before they are kept in heap memory in the StaticCache. Some
 * platform implementations, such as Android, may choose to ignore these resize
 * commands in which case the platform-specific UI in your view is expected to
 * resize images as appropriate.
 *
 * Note that image resizes by powers of 2 default to faster but still minimally
 * lossy scaling algorithms automatically.
 *
 * @author phou
 */
public abstract class ImageTypeHandler implements DataTypeHandler {

    /**
     * This value, used by default, means no bounding box is specified and the
     * image will be returned at full original size on this dimension.
     */
    public static final int SCALING_DISABLED = -1;
    /**
     * Select the image resize algorithm from among the cross-platform algorithms
     * available in ImageUtils. Some produce faster results, some sharper, some
     * smoother, some support alpha blending. The default value for JME is
     * ImageUtils.FIVE_POINT_BLEND which is fast and smooth and supports alpha
     * translucency. The Android implementation does not implement scaling. It
     * recommends the default Android use of native scaling algorithms
     * in the UI layer itself so this value is not used.
     */
    protected int algorithm = 0;
    /**
     * If the source image is larger than maxWidith and/or maxHeight, should the
     * image keep the same width/height ratio after resizing. The default value
     * is true, which prevents the "squeezed" look of non-proportional image
     * resize. This might be useful effect in some cases.
     */
    protected boolean preserveAspectRatio = true;
    /**
     * The maximum width of the image expected from this routine by the UI view.
     * Images larger than this will be scaled down to fit. The default value is
     * -1, which means no resizing.
     */
    protected int maxWidth = SCALING_DISABLED;
    /**
     * The maximum height of the image expected from this routine by the UI
     * view. Images larger than this will be scaled down to fit. The default
     * value is -1, which means no resizing.
     */
    protected int maxHeight = SCALING_DISABLED;

    /**
     * Set the scaling algorithm to be used. Choose from among the public
     * constants in ImageUtils. If you want to use native or other scaling
     * not included in Tantalum, you should extend
     * DataTypeHandler directly rather than modify the cross-platform
     * ImageTypeHandler.
     *
     * @param algorithm
     */
    public synchronized void setAlgorithm(final int algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Turn this to false to allow the image scaling algorithm to maximally fill
     * the maxWidth and maxHeight for distortion effects. Images will not be
     * expanded. The default if true.
     *
     * @param preserveAspectRatio
     */
    public synchronized void setPreserveAspectRatio(final boolean preserveAspectRatio) {
        this.preserveAspectRatio = preserveAspectRatio;
    }

    /**
     * Get the current aspect ratio image scaling mode setting.
     *
     * @return
     */
    public synchronized boolean getPreserveAspectRatio() {
        return this.preserveAspectRatio;
    }

    /**
     * Set the bounding box which images must fit within when loaded with this
     * handler. Because thse are locked to the StaticWebCache, you should create
     * a different StaticWebCache for each automatically scaled image size you
     * support. By default, image scaling is off unless you set a bounding box.
     *
     * @param maxWidth
     * @param maxHeight
     */
    public synchronized void setMaxSize(final int maxWidth, final int maxHeight) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    /**
     * Get the current width bound used for image scaling.
     *
     * The default is -1 for image scaling off.
     *
     * @return
     */
    public synchronized int getMaxWidth() {
        return this.maxWidth;
    }

    /**
     * Get the current height bound user for image scaling.
     *
     * The default is -1 for image scaling off.
     *
     * @return
     */
    public synchronized int getMaxHeight() {
        return this.maxHeight;
    }
}
