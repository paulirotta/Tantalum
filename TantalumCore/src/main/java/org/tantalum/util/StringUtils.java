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
     * Return a String object stored as a file in the JAR package using the
     * phone's default String encoding.
     *
     * @param name
     * @return
     * @throws IOException
     */
    public static String readStringFromJAR(final String name) throws IOException {
        return readStringFromJAR(name, "UTF-8");
    }

    /**
     * Return a String object stored as a file in the JAR package
     *
     * @param name
     * @param encoding such as "UTF-8"
     * @return
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public static String readStringFromJAR(final String name, final String encoding) throws IOException, UnsupportedEncodingException {
        return new String(readBytesFromJAR(name), encoding);
    }

    /**
     * Encodes a string so that certain characters are replace by their ASCII
     * code counterpart. E.g. for space "%20".
     *
     * UTF-8 up to 3 bytes per character is supported. 4 byte Unicode characters
     * are not supported.
     *
     * @param s Text that is to be encoded.
     * @return The encoded string.
     * @throws IOException
     */
    public static String urlEncode(final String s) throws IOException {
        if (s == null) {
            throw new NullPointerException("Can not urlEncode null string");
        }
        if (s.length() == 0) {
            return s;
        }

        final ByteArrayInputStream bIn;
        final StringBuffer sb = new StringBuffer((s.length() * 5) / 4); //return value
        {
            final ByteArrayOutputStream bOut = new ByteArrayOutputStream((s.length() * 3) / 2);
            final DataOutputStream dOut = new DataOutputStream(bOut);
            dOut.writeUTF(s);
            bIn = new ByteArrayInputStream(bOut.toByteArray());
            dOut.close();
        }
        bIn.read(); // Initial bytes unicode read hack
        bIn.read();
        int c;
        while ((c = bIn.read()) >= 0) {
            if (c == ' ') {
                sb.append('+');
            } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '.' || c == '-'
                    || c == '*' || c == '_') {
                sb.append((char) c);
            } else {
                appendTaggedHex(c, sb);
                if (c >= 128) {
                    appendTaggedHex(bIn.read(), sb);
                    if (c >= 224) {
                        appendTaggedHex(bIn.read(), sb);
                        // Java UTF-8 encoding modifies the standard- it is not so easy to encode 4-byte UTF-8 characters
//                        if (c >= 240) {
//                            appendTaggedHex(bIn.read(), sb);
//                        }
                    }
                }
            }
        }

        return sb.toString();
    }

    /**
     * Decodes a UTF-8 string where certain characters are replace by their
     * ASCII code counterpart. E.g. for space "%20".
     *
     * The full UTF-8 including 4 bytes per character is supported. Note that
     * the fonts on your phone may not support these extended characters.
     *
     * @param s
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public static String urlDecode(final String s) throws UnsupportedEncodingException, IOException {
        if (s == null) {
            throw new NullPointerException("Can not urlDecode null string");
        }
        final int n = s.length();
        final StringBuffer sb = new StringBuffer(n * 2);

        for (int i = 0; i < n; i++) {
            final char c = s.charAt(i);

            if (c == '+') {
                sb.append(' ');
            } else if (c == '%') {
                final String s1 = s.substring(++i, i++ + 2);
                final int first = Integer.parseInt(s1, 16);

                if (first < 128) {
                    sb.append((char) first);
                } else {
                    if (s.charAt(++i) != '%') {
                        throw new IllegalArgumentException("urlDecode expected second '%' at position " + i + " but was '" + s.charAt(i) + "' : " + s);
                    }
                    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    final String s2 = s.substring(++i, i++ + 2);
                    final int second = Integer.parseInt(s2, 16);

                    bos.write(first);
                    bos.write(second);
                    if (first < 224) {
                        sb.append(new String(bos.toByteArray(), "UTF-8"));
                    } else {
                        if (s.charAt(++i) != '%') {
                            throw new IllegalArgumentException("urlDecode expected third '%' at position " + i + " but was '" + s.charAt(i) + "'" + s);
                        }
                        final String s3 = s.substring(++i, i++ + 2);
                        final int third = Integer.parseInt(s3, 16);

                        bos.write(third);
                        if (first < 240) {
                            sb.append(new String(bos.toByteArray(), "UTF-8"));
                        } else {
                            if (s.charAt(++i) != '%') {
                                throw new IllegalArgumentException("urlDecode expected fourth '%' at position " + i + " but was '" + s.charAt(i) + "'" + s);
                            }
                            final String s4 = s.substring(++i, i++ + 2);
                            final int fourth = Integer.parseInt(s4, 16);

                            bos.write(fourth);
                            sb.append(new String(bos.toByteArray(), "UTF-8"));
                        }
                    }
                }
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
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
     * Decode any string that is encoded using hesStringToString()
     *
     * @param s
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String hexStringToString(final String s) throws UnsupportedEncodingException {
        final byte[] bytes = hexStringToByteArray(s);

        return new String(bytes, "UTF-8");
    }

    /**
     * Encode any string as hex digits in a String that can be included for
     * example in JSON, regardless of other encodings of the string.
     *
     * @param s
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String stringToHexString(final String s) throws UnsupportedEncodingException {
        final byte[] bytes = s.getBytes("UTF-8");

        return byteArrayToHexString(bytes);
    }

    /**
     * Return a string of the form "0FCC" with two characters per byte
     *
     * @param bytes
     * @return
     */
    public static String byteArrayToHexString(final byte[] bytes) {
        final StringBuffer sb = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; i++) {
            appendHex(bytes[i], sb);
        }

        return sb.toString();
    }

    /**
     * Return a byte array from a string of the form "0FCC" with strictly two
     * bytes per character in the string
     *
     * @param s
     * @return
     */
    public static byte[] hexStringToByteArray(final String s) {
        if (s == null) {
            throw new NullPointerException("hexStringToByteArray was pass null string");
        }
        
        final int n = s.length();
        if (n % 2 != 0) {
            throw new IllegalArgumentException("Input string must be an even length and encoded by byteArrayToHexString(s), but input length is " + n);
        }
        final byte[] bytes = new byte[n / 2];

        int k = 0;
        for (int i = 0; i < n; i += 2) {
            final String substring = s.substring(i, i + 2);
            final int j = Integer.parseInt(substring, 16);
            bytes[k++] = (byte) j;
        }

        return bytes;
    }
}
