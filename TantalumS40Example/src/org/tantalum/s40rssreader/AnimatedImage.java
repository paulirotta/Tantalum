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
package org.tantalum.s40rssreader;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import org.tantalum.util.ImageUtils;

/**
 *
 * @author phou
 */
public class AnimatedImage {

    public final Image image;
    private float w;
    private float h;
    private final int endW;
    private final int endH;
    private final int numberOfAnimationFrames;
    private final float deltaW;
    private final float deltaH;
    private boolean animationComplete = false;
    private int frame = 0;
    private static int[] data;

    public AnimatedImage(final Image image, final int startW, final int startH, final int endW, final int endH, final int numberOfAnimationFrames) {
        this.image = image;
        this.w = startW;
        this.h = startH;
        this.endW = endW;
        this.endH = endH;
        this.numberOfAnimationFrames = numberOfAnimationFrames;
        this.deltaW = (endW - startW) / (float) numberOfAnimationFrames;
        this.deltaH = (endH - startH) / (float) numberOfAnimationFrames;
    }

    public boolean animate(final Graphics g, final int x, final int y) {
        if (data == null || data.length < image.getWidth() * image.getHeight()) {
            data = new int[image.getWidth() * image.getHeight()];
        }
        image.getRGB(data, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
        ImageUtils.drawFlipshade(g, x, y, data, image.getWidth(), image.getHeight(), (int) w, (int) h, false);
        animationComplete = ++frame >= numberOfAnimationFrames;
        if (!animationComplete) {
            // Linear
            this.w += deltaW;
            this.h += deltaH;
        } else {
            this.w = endW;
            this.h = endH;
        }

        return animationComplete;
    }
}
