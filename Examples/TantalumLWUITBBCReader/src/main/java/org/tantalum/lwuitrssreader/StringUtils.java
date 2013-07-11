/*
 Copyright (c) 2013 Nokia Corporation. All rights reserved.
 Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation.
 Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 affiliates. Other product and company names mentioned herein may be trademarks
 or trade names of their respective owners.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
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
