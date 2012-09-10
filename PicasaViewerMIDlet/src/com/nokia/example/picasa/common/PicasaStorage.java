package com.nokia.example.picasa.common;

import com.futurice.tantalum3.UITask;
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
public class PicasaStorage {

    /*
     * Picasa reference:
     * https://developers.google.com/picasa-web/docs/2.0/reference
     */
    public static final int NR_OF_FEATURED = 20;
    public static int imageSide;
    private static int screenWidth;
    private static String thumbSize;
    private static int imageSize; //Must be valid picasa size
    private static String urlOptions;
    private static String featURL;
    private static String searchURL;
    public static PicasaImageObject selectedImage = null;
    public static StaticWebCache feedCache;
    public static StaticWebCache imageCache;

    /**
     * Initialize the storage. The width is the width of the screen. This is
     * used to determine how large images should be.
     *
     */
    public static synchronized void init(final int width) {
        if (feedCache == null) {
            screenWidth = width;
            if (screenWidth < 256) {
                imageSide = 128; //Must be supported picasa thumb size
                imageCache = new StaticWebCache('4', new ImageTypeHandler(false, false, width));
                imageSize = 288; //Image max size to get suitable sized images from picasa
            } else {
                imageSide = 256; // Must be supported thumb size in picasa
                imageCache = new StaticWebCache('4', new ImageTypeHandler());
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

    /**
     * Tell Tantalum to fetch the ImageObjects
     *
     * @param closure - RunnableResult to be ran in the UI thread
     * @param fromWeb - True to force fetch from web
     */
    public static void getImageObjects(final UITask closure, final boolean search, final boolean fromWeb) {
        final String url = search ? searchURL : featURL;

        if (fromWeb) {
            feedCache.update(url, closure);
        } else {
            feedCache.get(url, closure);
        }
    }

    /**
     * Class for converting the JSON response in to a Vector of
     * PicasaImageObject-objects. The vector is saved by Tantalum.
     */
    private static class ImageObjectTypeHandler implements DataTypeHandler {

        public Object convertToUseForm(byte[] bytes) {
            JSONObject o;
            final Vector vector = new Vector();

            try {
                o = new JSONObject(new String(bytes));
            } catch (JSONException ex) {
                //#debug
                L.e("bytes are not a JSON object", featURL, ex);
                return null;
            }

            if (o != null) {
                JSONArray entries = new JSONArray();
                try {
                    final JSONObject feed = ((JSONObject) o).getJSONObject("feed");
                    entries = feed.getJSONArray("entry");
                } catch (JSONException e) {
                    vector.addElement(new PicasaImageObject("No Results", "", "", ""));

                    //#debug
                    L.e("JSON no result", featURL, e);
                }

                for (int i = 0; i < entries.length(); i++) {
                    try {
                        final JSONObject m = entries.getJSONObject(i);
                        final String title = m.getJSONObject("title").getString("$t");
                        final String author = m.getJSONArray("author").getJSONObject(0).getJSONObject("name").getString("$t");
                        final String thumbUrl = m.getJSONObject("media$group").getJSONArray("media$thumbnail").getJSONObject(0).getString("url");
                        final String imageUrl = m.getJSONObject("media$group").getJSONArray("media$content").getJSONObject(0).getString("url");

                        //#mdebug
                        L.i("JSON parsed title: ", title);
                        L.i("JSON parsed author: ", author);
                        L.i("JSON parsed thumb url: ", thumbUrl);
                        L.i("JSON parsed image url: ", imageUrl);
                        //#enddebug

                        vector.addElement(new PicasaImageObject(title, author, thumbUrl, imageUrl));
                    } catch (JSONException e) {
                        //#debug
                        L.e("JSON item parse error", featURL, e);
                    }
                }
                if (entries.length() == 0) {
                    vector.addElement(new PicasaImageObject("No Results", "", "", ""));
                }
            }
            return vector;
        }
    }
}
