package com.futurice.s40rssreader;

import com.futurice.rssreader.common.StringUtils;
import javax.microedition.lcdui.Image;

/**
 * RSS Item object
 *
 * @author ssaa
 */
public class RSSItem {

    private String title = "";
    private String truncatedTitle = "";
    private String description = "";
    private String link = "";
    private String pubDate = "";
    private String thumbnail = "";
    private Image thumbnailImage = null;
    private boolean loadingImage = false;

    public String toString() {
        return "RSSItem- title:" + title + " truncatedTitle:" + truncatedTitle + " description:" + description + " link:" + link + " pubDate:" + pubDate + " thumbnail:" + thumbnail;
    }

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

        //set the truncated title
        this.truncatedTitle = StringUtils.truncate(title, RSSReaderCanvas.FONT_TITLE, RSSReaderCanvas.getInstance().getWidth() - 2 * RSSReaderCanvas.MARGIN);
    }

    public synchronized String getTruncatedTitle() {
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
}
