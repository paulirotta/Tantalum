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
package org.tantalum.lwuitrssreader;

import com.sun.lwuit.Font;
import java.util.Vector;

/**
 * Utility methods for String handling
 * @author ssaa
 */
public class StringUtils {

    private final static String ELIPSIS = "...";

    /**
     * Truncates the string to fit the maxWidth. If truncated, an elipsis "..." is displayed to indicate this.
     * 
     * @param str
     * @param font
     * @param maxWidth
     * @return String - truncated string with ellipsis added to end of the string
     */
    public static String truncate(final String str, final Font font, int maxWidth) {
        if (str.length() < 10 || font.stringWidth(str) <= maxWidth) {
            return str;
        }

        maxWidth -= font.stringWidth(ELIPSIS);
        StringBuffer truncated = new StringBuffer(str);
        while (truncated.length() > 2 && font.stringWidth(truncated.toString()) > maxWidth) {
            truncated.deleteCharAt(truncated.length() - 1);
        }
        truncated.append(ELIPSIS);
        
        return truncated.toString();
    }

    /**
     * Split a string in to several lines of text which will display within a maximum width.
     *
     * @param str
     * @param font
     * @param maxWidth
     * @return
     */
    public static Vector splitToLines(final String str, final Font font, final int maxWidth) {
        final Vector lines = new Vector();

        if (font.stringWidth(str) <= maxWidth) {
            lines.addElement(str);
            return lines;
        }

        StringBuffer currentLine = new StringBuffer();
        String word = null;
        int currentIndex = 0;
        int wordBoundaryIndex = str.indexOf(' ', currentIndex);
        if (wordBoundaryIndex == -1) {
            for (int i = 0; i < str.length(); i++) {
                if (font.stringWidth(str.substring(0, i)) > maxWidth) {
                    wordBoundaryIndex = i;
                    break;
                }
            }
        }

        while (currentIndex != -1 && currentIndex < str.length()) {

            word = str.substring(currentIndex, wordBoundaryIndex+1);

            if (currentIndex == 0) {
                currentLine.append(word);
            } else {
                if (font.stringWidth((currentLine.toString() + " " + word)) < maxWidth) {
                    currentLine.append(" " + word);
                } else {
                    lines.addElement(currentLine.toString());
                    currentLine.setLength(0);
                    currentLine.append(word);
                }
            }

            currentIndex = wordBoundaryIndex + 1;
            wordBoundaryIndex = str.indexOf(' ', currentIndex);
            if (wordBoundaryIndex == -1) {
                wordBoundaryIndex = str.length()-1;
                for (int i = currentIndex; i < str.length(); i++) {
                    if (font.stringWidth(str.substring(currentIndex, i)) > maxWidth) {
                        wordBoundaryIndex = i;
                        break;
                    }
                }
            }
        }
        lines.addElement(currentLine.toString());

        return lines;
    }
}
