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
package org.tantalum.jme;

import javax.microedition.lcdui.Image;
import org.tantalum.Task;
import org.tantalum.storage.ImageCacheView;
import org.tantalum.util.L;
import org.tantalum.util.LOR;

/**
 * This is a helper class for creating an image class. It automatically converts
 * the byte[] to an Image as the data is loaded from the network or cache.
 *
 * @author tsaa
 */
public class JMEImageCacheView extends ImageCacheView {

    public Object convertToUseForm(final Object key, final LOR bytesReference) {
        final Image img;
        final int alg;
        final boolean aspect;
        final int w, h;

        if (LOR.get(bytesReference) == null) {
            throw new NullPointerException("Can not convert null bytes to an image");
        }

        synchronized (this) {
            alg = this.algorithm;
            aspect = this.preserveAspectRatio;
            w = this.maxWidth;
            h = this.maxHeight;
        }

        //#debug
        int bytesLength = 0;
        try {
            byte[] bytes = bytesReference.getBytes();
            bytesReference.clear();
            synchronized (Task.LARGE_MEMORY_MUTEX) {
                if (w == -1) {
                    //#mdebug
                    bytesLength = bytes.length;
                    L.i("convert bytes to image", "length=" + bytesLength);
                    //#enddebug
                    img = Image.createImage(bytes, 0, bytes.length);
                } else {
                    final int tempW;
                    final int tempH;
                    final int[] argb;
                    {
                        final Image tempImage = Image.createImage(bytes, 0, bytes.length);
                        bytes = null;
                        tempW = tempImage.getWidth();
                        tempH = tempImage.getHeight();
                        argb = new int[tempW * tempH];
                        tempImage.getRGB(argb, 0, tempW, 0, 0, tempW, tempH);
                    }
                    img = JMEImageUtils.scaleImage(argb, argb, tempW, tempH, w, h, aspect, alg);
                }
            }
        } catch (IllegalArgumentException e) {
            //#debug
            L.e("Exception converting bytes to image", "image byte length=" + bytesLength, e);
            throw e;
        }

        return img;
    }
}
