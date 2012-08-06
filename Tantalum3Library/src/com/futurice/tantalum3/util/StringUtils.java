package com.futurice.tantalum3.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import javax.microedition.lcdui.Font;

/**
 * Utility methods for String handling
 *
 * @author ssaa, paul houghton
 */
public class StringUtils {

    private static StringUtils singleton;
    private final static String ELIPSIS = "...";

    private static synchronized StringUtils getStringUtils() {
        if (singleton == null) {
            singleton = new StringUtils();
        }
        
        return singleton;
    }
    
    private StringUtils() {
    };
    
    /**
     * Truncates the string to fit the maxWidth. If truncated, an elipsis "..."
     * is displayed to indicate this.
     *
     * @param str
     * @param font
     * @param maxWidth
     * @return String - truncated string with ellipsis added to end of the
     * string
     */
    public static String truncate(final String str, final Font font, final int maxWidth) {
        if (font.stringWidth(str) <= maxWidth) {
            return str;
        }

        StringBuffer truncated = new StringBuffer(str);
        while (font.stringWidth(truncated.toString()) > maxWidth) {
            truncated.deleteCharAt(truncated.length() - 1);
        }
        truncated.delete(truncated.length() - ELIPSIS.length(), truncated.length());
        truncated.append(ELIPSIS);
        return truncated.toString();
    }

    /**
     * Split a string in to several lines of text which will display within a maximum width.
     *
     * @param vector
     * @param str
     * @param font
     * @param maxWidth
     * @return
     */
    public static Vector splitToLines(Vector vector, String text, Font font, int maxWidth) {
        int len, lastSpace = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == ' ') {
                lastSpace = i;
            }
            len = font.stringWidth(text.substring(0, i));
            if (len > maxWidth) {
                vector.addElement(text.substring(0, lastSpace + 1).trim());
                return splitToLines(vector, text.substring(lastSpace), font, maxWidth);
            }
        }
        vector.addElement(text.trim());
        return vector;
    }

    /**
     * This method can not be static in order to access the current instance's path
     * 
     * @param name
     * @return
     * @throws IOException 
     */
    private byte[] doReadBytesFromJAR(final String name) throws IOException {
        final InputStream in = getClass().getResourceAsStream(name);
        final byte[] bytes = new byte[in.available()];
        in.read(bytes);

        return bytes;
    }

    /**
     * Return a byte[] stored as a file in the JAR package
     * 
     * @param name
     * @return
     * @throws IOException 
     */
    public static byte[] readBytesFromJAR(final String name) throws IOException {
        return getStringUtils().doReadBytesFromJAR(name);
    }

    /**
     * Return a String object stored as a file in the JAR package
     * 
     * @param name
     * @return
     * @throws IOException 
     */
    public static String readStringFromJAR(final String name) throws IOException {
        return new String(readBytesFromJAR(name));
    }
}
