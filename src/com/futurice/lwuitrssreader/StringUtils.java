package com.futurice.lwuitrssreader;

import java.util.Vector;
import com.sun.lwuit.Font;

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
    public static String truncate(final String str, final Font font, final int maxWidth) {
        if (font.stringWidth(str) <= maxWidth) {
            return str;
        }

        StringBuffer truncated = new StringBuffer(str);
        while (font.stringWidth(truncated.toString()) > maxWidth) {
            truncated.deleteCharAt(truncated.length() - 1);
        }
        truncated.delete(truncated.length() - ELIPSIS.length(), truncated.length() - 1);
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
