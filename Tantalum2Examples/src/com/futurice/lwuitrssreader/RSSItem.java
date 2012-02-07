package com.futurice.lwuitrssreader;


import com.sun.lwuit.Image;

/**
 * RSS Item object
 * @author ssaa
 * 
 * Modified by tsaa
 */
public class RSSItem {
    
    private String title = "";
    private String truncatedTitle = "";
    private String description = "";
    private String link = "";
    private String pubDate = "";
    private String imgSrc = "";
    private Image img = null;
    public boolean isLoadingImg = false;
    
    public RSSItem() {
        
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.truncatedTitle = StringUtils.truncate(title, RSSReader.mediumFont, RSSReader.SCREEN_WIDTH);
    }
    
    public String getTruncatedTitle() {
        return truncatedTitle;
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
    
    public String getImgSrc() {
        return imgSrc;
    }
    
    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }
    
    public Image getImg() {
        return img;
    }
    
    public void setImg(Image img) {
        this.img = img;
    }
    
    public String toString() {
        return truncatedTitle;
    }
}
