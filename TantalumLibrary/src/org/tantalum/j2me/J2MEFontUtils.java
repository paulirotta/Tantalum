/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.j2me;

import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Font;

/**
 * Some convenience utilities for working with J2ME Fonts and strings to lay out
 * a user interface on a Canvas or CustomItem. Some of these functions are backed
 * by character-width Hashtable structures to give a much faster alternative
 * calculations to the very slow Font.stringWidth() routine that can slow your
 * user interface.
 * 
 * @author phou
 */
public final class J2MEFontUtils {
    private static final Hashtable instances = new Hashtable();
    private final Hashtable charWidth = new Hashtable();
    /**
     * The Font which this instance operates on
     * 
     */
    public final Font font;
    /**
     * The string added to the end of truncated text to indicate that the entire
     * value is not shown. "..." is a typical value.
     */
    public final String elipsis;

    /**
     * Get an instance for the specified Font and, for those routines that need
     * it, elipsis (line ending addition such as '...' to indicate that the
     * line was truncated)
     * 
     * @param font
     * @param elipsis
     * @return 
     */
    public static synchronized J2MEFontUtils getFontUtils(final Font font, final String elipsis) {
        if (font == null) {
            throw new IllegalArgumentException("J2MEFontUtils was passed a null font");
        }
        if (elipsis == null) {
            throw new IllegalArgumentException("J2MEFontUtils was passed a null elipsis");
        }
        final int key = font.hashCode() ^ elipsis.hashCode();
        J2MEFontUtils instance = (J2MEFontUtils) instances.get(new Integer(key));
        
        if (instance == null) {
            instance = new J2MEFontUtils(font, elipsis);
        }

        return instance;
    }

    private J2MEFontUtils(final Font font, final String elipsis) {
        this.font = font;
        this.elipsis = elipsis;
    }
    
    /**
     * Return the approximate (within a few pixels) width of a <code>String</code> when rendered
     * in the specified <code>Font</code>.
     * 
     * Note that since nearby characters are not known, this width does not take
     * into account possible kerning of this character when placed next to others
     * in a line of text. For most purposes this is acceptable, and the advantage
     * of using this method vs the more precise <code>Font.stringWidth()</code>
     * method is that this returns values which are good enough for most purposes
     * much more quickly.
     * 
     * @param str
     * @return 
     */
    public int stringWidth(final String str) {
        int w = 0;
        final char[] chars = str.toCharArray();
        
        for (int i = 0; i < chars.length; i++) {
            i += charWidth(chars[i]);
        }
        
        return w;
    }

    /**
     * Find the width of the character in the specified Font.
     * 
     * Note that since nearby characters are not known, this width does not take
     * into account possible kerning of this character when placed next to others
     * in a line of text.
     * 
     * @param c
     * @return 
     */
    public int charWidth(final char c) {
        final Character ca = new Character(c);
        Integer width = (Integer) this.charWidth.get(ca);
        
        if (width == null) {
            width = new Integer(font.stringWidth(ca.toString()));
            this.charWidth.put(ca, width);
        }
        
        return width.intValue();
    }

    /**
     * Truncates the string to fit the maxWidth. If truncated, an elipsis "..."
     * is displayed to indicate this.
     *
     * @param str
     * @param maxWidth
     * @return String - truncated string with ellipsis added to end of the
     * string
     */
    public String truncate(final String str, final int maxWidth) {
        String truncatedStr = str;
        
        if (font.stringWidth(str) > maxWidth) {
            final StringBuffer truncated = new StringBuffer(str);
            while (font.stringWidth(truncated.toString()) > maxWidth) {
                truncated.deleteCharAt(truncated.length() - 1);
            }
            truncated.delete(truncated.length() - elipsis.length(), truncated.length());
            truncated.append(elipsis);
            truncatedStr = truncated.toString();
        }

        return truncatedStr;
    }

    /**
     * Split a string in to several lines of text which will display within a
     * maximum width. To return good but not exact line truncation points quickly,
     * exact Font kerning is not considered. As a result the actual length
     * of each line of text may be up to a few pixels more than a kerned render,
     * so in rare cases lines may be broken one word earlier than they would
     * otherwise.
     *
     * @param text
     * @param maxWidth
     * @return 
     */
    public Vector splitToLines(final String text, final int maxWidth) {
        return splitToLines(new Vector(), text, maxWidth, false);
    }
    
    /**
     * Split a string in to several lines of text which will display within a
     * maximum width. Single lines of text are expected and only the common
     * whitespace character <code>' '</code> is considered as a line break position.
     *
     * @param vector - The existing vector to which the lines should be appended
     * @param text
     * @param maxWidth
     * @param useKerning
     * @return 
     */
    public Vector splitToLines(final Vector vector, final String text, final int maxWidth, final boolean useKerning) {
        int lastSpace = 0;

        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == ' ') {
                lastSpace = i;
            }
            final int width;
            if (useKerning) {
                width = font.stringWidth(text.substring(0, i));
            } else {
                width = stringWidth(text.substring(0, i));
            }
            if (width > maxWidth) {
                vector.addElement(text.substring(0, lastSpace + 1).trim());
                splitToLines(vector, text.substring(lastSpace + 1), maxWidth, useKerning);
                return vector;
            }
        }
        vector.addElement(text.trim());
        
        return vector;
    }
}
