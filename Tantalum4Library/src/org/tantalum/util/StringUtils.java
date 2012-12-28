/*
 Copyright © 2012 Paul Houghton and Futurice on behalf of the Tantalum Project.
 All rights reserved.

 Tantalum software shall be used to make the world a better place for everyone.

 This software is licensed for use under the Apache 2 open source software license,
 http://www.apache.org/licenses/LICENSE-2.0.html

 You are kindly requested to return your improvements to this library to the
 open source community at http://projects.developer.nokia.com/Tantalum

 The above copyright and license notice notice shall be included in all copies
 or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package org.tantalum.util;

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
    }

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
    public static String truncate(String str, final Font font, final int maxWidth) {
        if (font.stringWidth(str) > maxWidth) {
            final StringBuffer truncated = new StringBuffer(str);
            while (font.stringWidth(truncated.toString()) > maxWidth) {
                truncated.deleteCharAt(truncated.length() - 1);
            }
            truncated.delete(truncated.length() - ELIPSIS.length(), truncated.length());
            truncated.append(ELIPSIS);
            str = truncated.toString();
        }

        return str;
    }

    /**
     * Split a string in to several lines of text which will display within a
     * maximum width.
     * 
     * @param vector
     * @param text
     * @param font
     * @param maxWidth 
     */
    public static void splitToLines(final Vector vector, final String text, final Font font, final int maxWidth) {
        int lastSpace = 0;

        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == ' ') {
                lastSpace = i;
            }
            final int len = font.stringWidth(text.substring(0, i));
            if (len > maxWidth) {
                vector.addElement(text.substring(0, lastSpace + 1).trim());
                splitToLines(vector, text.substring(lastSpace + 1), font, maxWidth);
                return;
            }
        }
        vector.addElement(text.trim());
    }

    /**
     * This method can not be static in order to access the current instance's
     * path
     *
     * @param name
     * @return
     * @throws IOException
     */
    private byte[] doReadBytesFromJAR(final String name) throws IOException {
        final InputStream in = getClass().getResourceAsStream(name);
        final byte[] bytes;

        try {
            bytes = new byte[in.available()];
            in.read(bytes);
        } finally {
            in.close();
        }

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
    
    //TODO Add clean implementations of uuencode and uudecode
}
