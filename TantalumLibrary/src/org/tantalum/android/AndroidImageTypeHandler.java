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
package org.tantalum.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.tantalum.storage.ImageTypeHandler;
import org.tantalum.util.L;

/**
 * This is a helper class for creating an image class. It automatically converts
 * the byte[] to an Image as the data is loaded from the network or cache.
 *
 * @author tsaa
 */
public final class AndroidImageTypeHandler extends ImageTypeHandler {

    public Object convertToUseForm(final Object key, final byte[] bytes) {
        try {
            Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (maxWidth == SCALING_DISABLED || maxHeight == SCALING_DISABLED) {
                return b;
            }
            return Bitmap.createScaledBitmap(b, maxWidth, maxHeight, true);
        } catch (IllegalArgumentException e) {
            L.e("Exception converting bytes to image", bytes == null ? "" : "" + bytes.length, e);
            throw e;
        }
    }
}
