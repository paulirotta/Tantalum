/*
 Copyright (c) 2013, Paul Houghton and Futurice Oy
 All rights reserved.

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
package org.tantalum.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Utility methods for String operations such as extracting a String from the
 * JAR
 *
 * @author ssaa, paul houghton
 */
public class StringUtils {

    private final static char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    private static final class StringUtilsHolder {

        public static final StringUtils instance = new StringUtils();
    }

    private StringUtils() {
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

        //#debug
        L.i("Read bytes from jar, file", name);
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
        return StringUtilsHolder.instance.doReadBytesFromJAR(name);
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

    /**
     * Encodes a string so that certain characters are replace by their ASCII
     * code counterpart. E.g. for space "%20".
     *
     * @param s Text that is to be encoded.
     * @return The encoded string.
     * @throws IOException
     */
    public static String urlEncode(final String s) throws IOException {
        if (s == null) {
            throw new IllegalArgumentException("Can not urlEncode null string");
        }

        final ByteArrayInputStream bIn;
        final StringBuffer ret = new StringBuffer((s.length() * 5) / 4); //return value
        {
            final ByteArrayOutputStream bOut = new ByteArrayOutputStream((s.length() * 3) / 2);
            final DataOutputStream dOut = new DataOutputStream(bOut);
            dOut.writeUTF(s);
            bIn = new ByteArrayInputStream(bOut.toByteArray());
            dOut.close();
        }
        //TODO Why this initial read hack?
        bIn.read();
        bIn.read();
        int c = bIn.read();
        while (c >= 0) {
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '.' || c == '-'
                    || c == '*' || c == '_') {
                ret.append((char) c);
            } else if (c == ' ') {
                ret.append('+');
            } else {
                if (c < 128) {
                    appendTaggedHex(c, ret);
                } else if (c < 224) {
                    appendTaggedHex(c, ret);
                    appendTaggedHex(bIn.read(), ret);
                } else if (c < 240) {
                    appendTaggedHex(c, ret);
                    appendTaggedHex(bIn.read(), ret);
                    appendTaggedHex(bIn.read(), ret);
                }
            }
            c = bIn.read();
        }
        bIn.close();

        return ret.toString();
    }

    /**
     * Decodes a UTF-8 string where certain characters are replace by their
     * ASCII code counterpart. E.g. for space "%20".
     *
     * @param s
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public static String urlDecode(final String s) throws UnsupportedEncodingException, IOException {
        if (s == null) {
            throw new IllegalArgumentException("Can not urlDecode null string");
        }
        final int n = s.length();
        final ByteArrayOutputStream out = new ByteArrayOutputStream(n * 2);

        for (int i = 0; i < n; i++) {
            final int c = (int) s.charAt(i);

            if (c == '+') {
                out.write(' ');
            } else if (c == '%') {
                final int c1 = Character.digit(s.charAt(++i), 16);
                if (c1 < 128) {
                    out.write((char) c1);
                } else {
                    final int c2 = Character.digit(s.charAt(++i), 16);
                    if (c1 < 224) {
                        out.write(new String("" + (char) ((c1 << 4) | c2)).getBytes("UTF-8"));
                    } else {
                        final int c3 = Character.digit(s.charAt(++i), 16);
                        out.write(new String("" + (char) ((c3 << 8) | (c2 << 4) | c3)).getBytes("UTF-8"));
                    }
                }
            } else {
                out.write(c);
            }
        }

        return new String(out.toByteArray(), "UTF-8");
    }

    /**
     * Appends integer as a hex formatted string to buffer.
     *
     * @param i Value that should be appended
     * @param sb Buffer we are appending to
     * @return The encoded string.
     */
    private static void appendTaggedHex(final int i, final StringBuffer sb) {
        sb.append('%');
        appendHex((byte) (i & 0xFF), sb);
    }

    private static void appendHex(final int i, final StringBuffer sb) {
        sb.append(HEX_CHARS[(i & 0xF0) >>> 4]);
        sb.append(HEX_CHARS[i & 0x0F]);
    }

    /**
     * Return a string of the form "0FCC" with two characters per byte
     *
     * @param bytes
     * @return
     */
    public static String toHex(final byte[] bytes) {
        final StringBuffer sb = new StringBuffer(bytes.length);

        for (int i = 0; i < bytes.length; i++) {
            appendHex(bytes[i], sb);
        }

        return sb.toString();
    }
}
