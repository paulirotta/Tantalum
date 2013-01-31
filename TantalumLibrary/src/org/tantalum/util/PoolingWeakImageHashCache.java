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
package org.tantalum.util;

import javax.microedition.lcdui.Image;

/**
 * Keep recent instances of images all of which are the same size in memory.
 * 
 * The cache will automatically clear() if the image size changes. This means you
 * can be guaranteed that if an images is returned, it is the correct size.
 * 
 * @author phou
 */
public final class PoolingWeakImageHashCache extends PoolingWeakHashCache {
    
    /**
     * Request an image from the cache. It must be of the specified dimensions.
     * 
     * @param width
     * @param height
     * @return 
     */
    public Image getImageFromPool(final int width, final int height) {
        Image image = (Image) getFromPool();
        
        if (image != null && (image.getWidth() != width || image.getHeight() != height)) {
            // The cache is being re-used for a different size image. Remove the old
            image = null;
            clear();
        } 
        
        return image;
    }
}
