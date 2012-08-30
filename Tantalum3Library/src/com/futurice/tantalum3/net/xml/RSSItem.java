package com.futurice.tantalum3.net.xml;

import com.futurice.tantalum3.util.StringUtils;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;

/**
 * RSS Item object, associated with RSSModel which is a list of such objects.
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
    private Image thumbnailImage = null;
    private volatile boolean loadingImage = false;
    private volatile boolean newItem = true;
    private Font truncatedFont;
    private int truncatedTitleWidth = 0;
    
    public String getDescription() {
        return description;
    }

    public synchronized void setDescription(String description) {
        this.description = description;
    }

    public synchronized String getLink() {
        return link;
    }

    public synchronized void setLink(String link) {
        this.link = link;
    }

    public synchronized String getPubDate() {
        return pubDate;
    }

    public synchronized void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public synchronized String getThumbnail() {
        return thumbnail;
    }

    public synchronized void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public synchronized String getTitle() {
        return title;
    }

    public synchronized void setTitle(String title) {
        this.title = title;
    }

    public synchronized String getTruncatedTitle(final Font font, final int width) {
        if (truncatedTitle == null || truncatedFont != font || truncatedTitleWidth != width) {
            this.truncatedTitle = StringUtils.truncate(title, font, width);            
        }
        
        return truncatedTitle;
    }

    public synchronized void setTruncatedTitle(String truncatedTitle) {
        this.truncatedTitle = truncatedTitle;
    }

    public boolean isLoadingImage() {
        return loadingImage;
    }

    public void setLoadingImage(final boolean loadingImage) {
        this.loadingImage = loadingImage;
    }

    public boolean isNewItem() {
        return newItem;
    }

    public void setNewItem(final boolean newItem) {
        this.newItem = newItem;
    }

    public synchronized String toString() {
        return "RSSItem- title:" + title + " truncatedTitle:" + truncatedTitle + " description:" + description + " link:" + link + " pubDate:" + pubDate + " thumbnail:" + thumbnail;
    }
}
