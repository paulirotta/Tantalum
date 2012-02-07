package com.futurice.formrssreader;

import com.futurice.rssreader.common.StringUtils;
import javax.microedition.lcdui.Image;

/**
 * RSS Item object
 * @author ssaa
 */
public class RSSItem {
    private volatile String title = "";
    private volatile String truncatedTitle = "";
    private volatile String description = "";
    private volatile String link = "";
    private volatile String pubDate = "";
    private volatile String thumbnail = "";
    private volatile Image thumbnailImage = null;
    private volatile boolean loadingImage = false;

    public String toString() {
        return "RSSItem- title:" + title + " truncatedTitle:" + truncatedTitle + " description:" + description + " link:" + link + " pubDate:" + pubDate + " thumbnail:" + thumbnail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;

        //set the truncated title
        this.truncatedTitle = StringUtils.truncate(title, ListView.FONT_TITLE, ListView.getInstance().getWidth() - 2*ListView.MARGIN);
    }

    public String getTruncatedTitle() {
        return truncatedTitle;
    }

    public void setTruncatedTitle(String truncatedTitle) {
        this.truncatedTitle = truncatedTitle;
    }

    public Image getThumbnailImage() {
        return thumbnailImage;
    }

    public void setThumbnailImage(Image thumbnailImage) {
        this.thumbnailImage = thumbnailImage;
    }

    public boolean isLoadingImage() {
        return loadingImage;
    }

    public void setLoadingImage(boolean loadingImage) {
        this.loadingImage = loadingImage;
    }
}
