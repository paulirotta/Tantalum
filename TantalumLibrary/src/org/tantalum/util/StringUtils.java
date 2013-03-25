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

/**
 * Utility methods for String operations such as extracting a String from the
 * JAR
 *
 * @author ssaa, paul houghton
 */
public class StringUtils {

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
     */
    public static String encodeURL(final String s) throws IOException {
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
                ret.append("%20");
            } else {
                if (c < 128) {
                    appendHex(c, ret);
                } else if (c < 224) {
                    appendHex(c, ret);
                    appendHex(bIn.read(), ret);
                } else if (c < 240) {
                    appendHex(c, ret);
                    appendHex(bIn.read(), ret);
                    appendHex(bIn.read(), ret);
                }
            }
            c = bIn.read();
        }
        bIn.close();

        return ret.toString();
    }

    /**
     * Appends integer as a hex formatted string to buffer.
     *
     * @param arg0 Value that should be appended
     * @param buff Buffer we are appending to
     * @return The encoded string.
     */
    private static void appendHex(final int arg0, final StringBuffer buff) {
        buff.append('%');
        if (arg0 < 16) {
            buff.append('0');
        }
        buff.append(Integer.toHexString(arg0).toUpperCase());
    }
}
