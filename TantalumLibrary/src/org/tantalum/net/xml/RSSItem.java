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
package org.tantalum.net.xml;

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

//#mdebug
    
    /**
     * Debug use
     * 
     * @return 
     */
    public synchronized String toString() {
        return "RSSItem- title:" + title + " truncatedTitle:" + truncatedTitle + " description:" + description + " link:" + link + " pubDate:" + pubDate + " thumbnail:" + thumbnail;
    }
//#enddebug    
}
