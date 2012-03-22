package com.futurice.tantalum2.net.xml;

import com.futurice.tantalum2.util.StringUtils;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;

/**
 * RSS Item object
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
    private boolean loadingImage = false;
    private Font truncatedFont;
    private int truncatedTitleWidth = 0;

    public synchronized String getDescription() {
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

    public synchronized Image getThumbnailImage() {
        return thumbnailImage;
    }

    public synchronized void setThumbnailImage(Image thumbnailImage) {
        this.thumbnailImage = thumbnailImage;
    }

    public synchronized boolean isLoadingImage() {
        return loadingImage;
    }

    public synchronized void setLoadingImage(boolean loadingImage) {
        this.loadingImage = loadingImage;
    }

    public String toString() {
        return "RSSItem- title:" + title + " truncatedTitle:" + truncatedTitle + " description:" + description + " link:" + link + " pubDate:" + pubDate + " thumbnail:" + thumbnail;
    }
}
