package com.nokia.example.picasa.common;

import com.futurice.tantalum3.Closure;
import com.futurice.tantalum3.log.L;
import com.futurice.tantalum3.net.StaticWebCache;
import com.futurice.tantalum3.rms.DataTypeHandler;
import com.futurice.tantalum3.rms.ImageTypeHandler;
import java.util.Vector;
import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

/**
 * Class for accessing cached data like thumbnails, images and feeds. The class
 * gets the resource from the cache first, if it is not found there it retrieves
 * it from the web.
 *
 * @author oehn
 *
 */
public class Storage {

    /*
     * Picasa reference:
     * https://developers.google.com/picasa-web/docs/2.0/reference
     */
    public static final int NR_OF_FEATURED = 20;
    public static int imageSide = 256; // Must be supported thumb size in picasa
    private static int screenWidth = 200;
    private static String thumbSize;
    private static int imageSize; //Must be valid picasa size
    private static String urlOptions;
    private static String featURL;
    private static String searchURL;
    public static ImageObject selectedImage = null;
    public static StaticWebCache feedCache;
//    public static StaticWebCache thumbCache;
    public static StaticWebCache imageCache;

    /**
     * Initialize the storage. The width is the width of the screen. This is
     * used to determine how large thumbnails should be.
     *
     */
    public static void init(int width) {
        if (feedCache == null) {
            screenWidth = width;
            if (screenWidth < 256) {
                imageSide = 128; //Must be supported picasa thumb size
                imageCache = new StaticWebCache('4', new ImageTypeHandler(screenWidth));
//                thumbCache = new StaticWebCache('3', new ImageTypeHandler(screenWidth / 2));
                imageSize = 288; //Image max size to get suitable sized images from picasa
            } else {
                imageCache = new StaticWebCache('4', new ImageTypeHandler());
//                thumbCache = new StaticWebCache('3', new ImageTypeHandler());
                imageSize = 720; //Picasa size for "fullsize" images
            }

            feedCache = new StaticWebCache('5', new ImageObjectTypeHandler());


            thumbSize = imageSide + "c"; // c is for cropped, ensures image proportions

            urlOptions = "?alt=json&kind=photo&max-results=" + NR_OF_FEATURED + "&thumbsize=" + thumbSize
                    + "&fields=entry(title,author(name),updated,media:group)&imgmax=" + imageSize;
            featURL = "http://picasaweb.google.com/data/feed/base/featured" + urlOptions;
            searchURL = "http://picasaweb.google.com/data/feed/base/all" + urlOptions + "&q=";
        }
    }

    //If we do not know the width of the screen, use a sensible default.
    public static void init() {
        init(256);
    }

    public static int getWidth() {
        return screenWidth;
    }

    /**
     * Tell Tantalum to fetch the ImageObjects
     * 
     * @param closure - RunnableResult to be ran in the UI thread
     * @param fromWeb -  True to force fetch from web
     */
    public static void getImageObjects(final Closure closure, final boolean search, final boolean fromWeb) {
        final String url = search ? searchURL : featURL;

        if (fromWeb) {
            feedCache.update(url, closure);
        } else {
            feedCache.get(url, closure);
        }
    }

    /**
     * Class for converting the JSON response in to a Vector of ImageObject-objects.
     * The vector is saved by Tantalum.
     */
    private static class ImageObjectTypeHandler implements DataTypeHandler {

        public Object convertToUseForm(byte[] bytes) {
            JSONObject o;
            Vector vector = new Vector();
            try {
                o = new JSONObject(new String(bytes));
            } catch (JSONException ex) {
                //#debug
                ex.printStackTrace();
                return null;
            }

            if (o != null) {
                JSONArray entries = new JSONArray();
                try {
                    JSONObject feed = ((JSONObject) o).getJSONObject("feed");
                    entries = feed.getJSONArray("entry");
                } catch (JSONException e) {
                    vector.addElement(new ImageObject("No Results", "", "", ""));
                    
                    //#debug
                    e.printStackTrace();
                }

                for (int i = 0; i < entries.length(); i++) {
                    try {
                        JSONObject m = entries.getJSONObject(i);
                        String title = m.getJSONObject("title").getString("$t");
                        String author = m.getJSONArray("author").getJSONObject(0).getJSONObject("name").getString("$t");
                        String thumbUrl = m.getJSONObject("media$group").getJSONArray("media$thumbnail").getJSONObject(0).getString("url");
                        String imageUrl = m.getJSONObject("media$group").getJSONArray("media$content").getJSONObject(0).getString("url");

                        L.i("JSON parsed title: ", title);
                        L.i("JSON parsed author: ", author);
                        L.i("JSON parsed thumb url: ", thumbUrl);
                        L.i("JSON parsed image url: ", imageUrl);

                        vector.addElement(new ImageObject(title, author, thumbUrl, imageUrl));
                    } catch (JSONException e) {
                        //#debug
                        e.printStackTrace();
                    }
                }
                if (entries.length() == 0) {
                    vector.addElement(new ImageObject("No Results", "", "", ""));
                }
            }
            return vector;
        }
    }
}
