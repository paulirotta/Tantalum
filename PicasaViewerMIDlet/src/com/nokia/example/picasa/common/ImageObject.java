package com.nokia.example.picasa.common;

/**
 * Class for storing information about a Picasa image.
 *
 * @author oehn
 *
 */
public final class ImageObject {

    private String title;
    private String author;
    private String thumbUrl;
    private String imageUrl;

    public ImageObject(String title, String photographer, String thumbUrl, String imgUrl) {
        this.title = title;
        this.author = photographer;
        this.thumbUrl = thumbUrl;
        this.imageUrl = imgUrl;
    }

    public String getTitle() {
        return this.title;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getThumbUrl() {
        return this.thumbUrl;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }
}
