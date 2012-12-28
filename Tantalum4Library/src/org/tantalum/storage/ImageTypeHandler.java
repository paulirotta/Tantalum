/*
 Copyright © 2012 Paul Houghton and Futurice on behalf of the Tantalum Project.
 All rights reserved.

 Tantalum software shall be used to make the world a better place for everyone.

 This software is licensed for use under the Apache 2 open source software license,
 http://www.apache.org/licenses/LICENSE-2.0.html

 You are kindly requested to return your improvements to this library to the
 open source community at http://projects.developer.nokia.com/Tantalum

 The above copyright and license notice notice shall be included in all copies
 or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
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
     * Should any image resizing event also handle the alpha (transparency)
     * channel. This makes resizing slightly slower with opaque JPG images which
     * do not support transparency, but may be valuable for translucent PNGs
     * depending on how you use them in your views. The default value is false.
     */
    protected boolean processAlpha = false;
    /**
     * Should any image resizing use the best possible resize algorithm, which
     * produces less visible artifacts but runs about 4x longer than the fast,
     * lossy algorithm. For most cases, the fast algorithm is the better choice.
     * Neither algorithm is particularly good at shrinking text in an image and
     * keeping that readable. The default value is false.
     */
    protected boolean bestQuality = false;
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
     * Set if the translucency channel should be processed. Otherwise the image
     * is treated as opaque by default.
     *
     * @param processAlpha
     */
    public synchronized void setProcessAlpha(final boolean processAlpha) {
        this.processAlpha = processAlpha;
    }

    /**
     * Indicates if translucency is currently being processed when images are
     * resized.
     *
     * @return
     */
    public synchronized boolean getProcessAlpha() {
        return this.processAlpha;
    }

    /**
     * Indicate if the fast image resize algorithm that uses a weighted average
     * of 5 pixels in the source image, or the slower but less lossy algorithm
     * should be used for shrinking images when necessary. The default is false,
     * which triggers the faster image resize methods.
     *
     * @param bestQuality
     */
    public synchronized void setBestQuality(final boolean bestQuality) {
        this.bestQuality = bestQuality;
    }

    /**
     * Return the current image scaling quality mode.
     *
     * @return
     */
    public synchronized boolean getBestQuality() {
        return this.bestQuality;
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
