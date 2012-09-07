package com.nokia.example.picasa.common;

/**
 * Class for storing information about a Picasa image.
 *
 * @author oehn
 *
 */
public final class PicasaImageObject {
    public final String title;
    public final String author;
    public final String thumbUrl;
    public final String imageUrl;

    public PicasaImageObject(final String title, final String photographer, final String thumbUrl, final String imgUrl) {
        this.title = title;
        this.author = photographer;
        this.thumbUrl = thumbUrl;
        this.imageUrl = imgUrl;
    }
}
