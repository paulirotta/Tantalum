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
package org.tantalum.formrssreader;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.*;
import org.tantalum.PlatformUtils;
import org.tantalum.Task;
import org.tantalum.net.StaticWebCache;
import org.tantalum.net.xml.RSSItem;
import org.tantalum.util.L;

/**
 *
 * @author vand
 */
public final class DetailsForm extends Form implements CommandListener {

    private FormRSSReader rssReader;
    private RSSItem selectedItem;
    private static StaticWebCache imageCache = StaticWebCache.getWebCache('1', PlatformUtils.getInstance().getImageTypeHandler());
    private Command openLinkCommand = new Command("Open link", Command.OK, 0);
    private Command backCommand = new Command("Back", Command.BACK, 0);

    public DetailsForm(FormRSSReader rssReader, String title) {
        super(title);
        this.rssReader = rssReader;
        this.addCommand(openLinkCommand);
        this.addCommand(backCommand);
        this.setCommandListener(this);
    }

    public void commandAction(Command command, Displayable d) {
        if (command == openLinkCommand) {
            openLink();
        } else if (command == backCommand) {
            ListForm.getInstance().showList();
        }
    }

    private void openLink() {
        try {
            boolean needsToClose = this.rssReader.platformRequest(ListForm.getInstance().getDetailsView().getSelectedItem().getLink());
            if (needsToClose) {
                PlatformUtils.getInstance().shutdown(false);
            }
        } catch (ConnectionNotFoundException connectionNotFoundException) {
            //#debug
            L.e("Error opening link", ListForm.getInstance().getDetailsView().getSelectedItem().getLink(), connectionNotFoundException);
            rssReader.showError("Could not open link");
        }
    }

    public void setSelectedItem(RSSItem selectedItem) {
        this.selectedItem = selectedItem;
    }

    public RSSItem getSelectedItem() {
        return selectedItem;
    }

    public void paint() {
        this.deleteAll();

        final StringItem titleStringItem = new StringItem(null, selectedItem.getTitle(), StringItem.PLAIN);
        titleStringItem.setFont(ListForm.FONT_TITLE);
        titleStringItem.setLayout(Item.LAYOUT_NEWLINE_AFTER);

        final StringItem dateStringItem = new StringItem(null, selectedItem.getPubDate(), StringItem.PLAIN);
        dateStringItem.setFont(ListForm.FONT_DATE);
        dateStringItem.setLayout(Item.LAYOUT_NEWLINE_AFTER);

        final StringItem descriptionStringItem = new StringItem(null, selectedItem.getDescription(), StringItem.PLAIN);
        descriptionStringItem.setFont(ListForm.FONT_DESCRIPTION);
        descriptionStringItem.setLayout(Item.LAYOUT_NEWLINE_AFTER);

        this.append(titleStringItem);
        this.append(dateStringItem);
        this.append(descriptionStringItem);

        if (selectedItem.getThumbnail() != null) {
            final Image image = (Image) imageCache.synchronousRAMCacheGet(selectedItem.getThumbnail());
            if (image != null) {
                DetailsForm.this.appendImageItem();
            } else if (!selectedItem.isLoadingImage()) {
                //request the thumbnail image, if not already loading
                selectedItem.setLoadingImage(true);
                imageCache.getAsync(selectedItem.getThumbnail(), Task.HIGH_PRIORITY, StaticWebCache.GET_ANYWHERE, new Task(Task.HIGH_PRIORITY) {

                    protected Object exec(final Object in) {
                        L.i("IMAGE DEBUG", selectedItem.getThumbnail());
                        selectedItem.setLoadingImage(false);
                        
                        return in;
                    }
                    public void run(final Object result) {
                        DetailsForm.this.appendImageItem();
                    }
                }.setRunOnUIThreadWhenFinished(true));
            }
        }
    }

    public void appendImageItem() {
        final Image image = (Image) imageCache.synchronousRAMCacheGet(selectedItem.getThumbnail());
        final ImageItem imageItem = new ImageItem(null, image, Item.LAYOUT_CENTER, "");
        this.append(imageItem);
    }
}
