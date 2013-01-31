/*
 * Copyright © 2012 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */
package com.nokia.common.picasaviewerapp;

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

    /**
     * For debug
     *
     * @return
     */
    public String toString() {
        return "PicasaImageObject title:" + title + " author:" + author + " thumbUrl:" + thumbUrl + " imageUrl:" + imageUrl;
    }
}
