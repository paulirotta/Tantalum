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

import com.sun.lwuit.events.DataChangedListener;
import com.sun.lwuit.list.DefaultListModel;
import org.tantalum.net.xml.RSSModel;
import org.tantalum.storage.CacheView;
import org.tantalum.util.L;
import org.xml.sax.SAXException;

/**
 *
 * @author tsaa
 */
public class ListModel extends DefaultListModel implements DataChangedListener, CacheView {

    private ListForm listForm;
    private final LiveUpdateRSSModel rssModel = new LiveUpdateRSSModel(60);

    public ListModel(ListForm listForm) {
        this.listForm = listForm;
        addDataChangedListener(this);
    }

    public void dataChanged(int type, int index) {
        listForm.repaint();
    }

    public void repaint() {
        listForm.repaint();
    }

    public Object convertToUseForm(final Object key, byte[] bytes) {
        try {
            if (bytes.length > 0) {
                rssModel.setXML(bytes);
            }

            return this;
        } catch (NullPointerException e) {
            L.e("Null bytes when to RSSModel", "", e);
        } catch (Exception e) {
            L.e("Error converting bytes to RSSModel", "", e);
        }
        return null;
    }

    private class LiveUpdateRSSModel extends RSSModel {

        public LiveUpdateRSSModel(final int maxSize) {
            super(maxSize);
        }

        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            if (currentItem != null && qName.equals("item")) {
                ListModel.this.addItem(currentItem);
            }

            super.endElement(uri, localName, qName);
        }
    }
}
