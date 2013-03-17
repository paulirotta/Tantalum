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
package org.tantalum.j2me;

import javax.microedition.lcdui.Image;
import org.tantalum.Task;
import org.tantalum.storage.ImageTypeHandler;
import org.tantalum.util.ImageUtils;
import org.tantalum.util.L;

/**
 * This is a helper class for creating an image class. It automatically converts
 * the byte[] to an Image as the data is loaded from the network or cache.
 *
 * @author tsaa
 */
public final class J2MEImageTypeHandler extends ImageTypeHandler {

    public Object convertToUseForm(final Object key, final byte[] bytes) {
        final Image img;
        final int alg;
        final boolean aspect;
        final int w, h;

        synchronized (this) {
            alg = this.algorithm;
            aspect = this.preserveAspectRatio;
            w = this.maxWidth;
            h = this.maxHeight;
        }

        try {
            if (w == -1) {
                //#debug
                L.i("convert image", "length=" + bytes.length);
                img = Image.createImage(bytes, 0, bytes.length);
            } else {
                synchronized (Task.LARGE_MEMORY_MUTEX) {
                    final int tempW;
                    final int tempH;
                    final int[] argb;
                    {
                        final Image tempImage = Image.createImage(bytes, 0, bytes.length);
                        tempW = tempImage.getWidth();
                        tempH = tempImage.getHeight();
                        argb = new int[tempW * tempH];
                        tempImage.getRGB(argb, 0, tempW, 0, 0, tempW, tempH);
                    }
                    img = ImageUtils.scaleImage(argb, argb, tempW, tempH, w, h, aspect, alg);
                }
            }
        } catch (IllegalArgumentException e) {
            //#debug
            L.e("Exception converting bytes to image", "image byte length=" + bytes.length, e);
            throw e;
        }

        return img;
    }
}
