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
package org.tantalum.net.xml;

import org.tantalum.util.StringUtils;

/**
 * RSS Item object, associated with RSSModel which is a list of such objects.
 * 
 * This class is thread safe. You can and should externally synchronized on this
 * object to access multiple fields without risk that they will be changed by
 * background updates.
 *
 * @author ssaa
 */
public class RSSItem {

    private String title = "";
    private String truncatedTitle = null;
    private String description = "";
    private String link = "";
    private String pubDate = "";
    private String thumbnail = "";
    private volatile boolean loadingImage = false;
    private volatile boolean newItem = true;
    private Object truncatedFont;
    private int truncatedTitleWidth = 0;
    
    /**
     * Get the item description field
     * 
     * @return 
     */
    public synchronized String getDescription() {
        return description;
    }

    /**
     * Set the item description field
     * 
     * @param description 
     */
    public synchronized void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the url associated with this item
     * 
     * @return 
     */
    public synchronized String getLink() {
        return link;
    }

    /**
     * Set the url associated with this item
     * 
     * @param link 
     */
    public synchronized void setLink(String link) {
        this.link = link;
    }

    /**
     * Get the publication date
     * 
     * @return 
     */
    public synchronized String getPubDate() {
        return pubDate;
    }

    /**
     * Set the publication date
     * 
     * @param pubDate 
     */
    public synchronized void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    /**
     * Get the thumbnail image url
     * 
     * @return 
     */
    public synchronized String getThumbnail() {
        return thumbnail;
    }

    /**
     * Set the thumbnail image url
     * 
     * @param thumbnail 
     */
    public synchronized void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    /**
     * Get the item title
     * 
     * @return 
     */
    public synchronized String getTitle() {
        return title;
    }

    /**
     * Set the item title
     * 
     * @param title 
     */
    public synchronized void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the item title, shortened to fit within the specified pixel width
     * with the specified font.
     * 
     * This is for J2ME only and will throw a runtime exception on other platforms.
     * 
     * @param font
     * @param width
     * @return 
     */
    public synchronized String getTruncatedTitle(final Object font, final int width) {
        if (truncatedTitle == null || truncatedFont != font || truncatedTitleWidth != width) {
            this.truncatedTitle = StringUtils.truncate(title, (javax.microedition.lcdui.Font) font, width);
            truncatedFont = font;
        }
        
        return truncatedTitle;
    }

    /**
     * Set the item title, shortened to fit on screen
     * 
     * @param truncatedTitle 
     */
    public synchronized void setTruncatedTitle(String truncatedTitle) {
        this.truncatedTitle = truncatedTitle;
        truncatedFont = null;
    }

    /**
     * A flag you can set from your application indicating that the associated
     * image has already been fork()ed for asynchronous load so it should not be
     * requested again.
     * 
     * @return 
     */
    public boolean isLoadingImage() {
        return loadingImage;
    }

    /**
     * Indicate that image loading is starting or stopping to block re-requests
     * 
     * @param loadingImage 
     */
    public void setLoadingImage(final boolean loadingImage) {
        this.loadingImage = loadingImage;
    }

    /**
     * Indicate that this is a new item. It may be displayed differently in your
     * application as a result.
     * 
     * @return 
     */
    public boolean isNewItem() {
        return newItem;
    }

    /**
     * Items start in the new state, and you set that to false if you find it
     * useful to your display model.
     * 
     * @param newItem 
     */
    public void setNewItem(final boolean newItem) {
        this.newItem = newItem;
    }

    /**
     * Debug use
     * 
     * @return 
     */
    public synchronized String toString() {
        return "RSSItem- title:" + title + " truncatedTitle:" + truncatedTitle + " description:" + description + " link:" + link + " pubDate:" + pubDate + " thumbnail:" + thumbnail;
    }
}
