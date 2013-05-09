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
package org.tantalum.lwuitrssreader;

import com.sun.lwuit.Command;
import com.sun.lwuit.Form;
import com.sun.lwuit.Image;
import com.sun.lwuit.Label;
import com.sun.lwuit.animations.CommonTransitions;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import java.util.Vector;
import org.tantalum.PlatformUtils;
import org.tantalum.Task;
import org.tantalum.net.StaticWebCache;
import org.tantalum.net.xml.RSSItem;
import org.tantalum.util.L;

/**
 *
 * @author tsaa
 */
public class DetailsForm extends Form implements ActionListener {

    static Command linkCommand = new Command("Open link");
    static Command backCommand = new Command("Back");
    private Label pubDateLabel;
    private Label imgLabel;
    private Vector titleLabels;
    private Vector descriptionLabels;
    private Vector linkLabels;
    private RSSReader midlet;
    private RSSItem current;
    private static final StaticWebCache imageCache = StaticWebCache.getWebCache('1', PlatformUtils.PHONE_DATABASE_CACHE, new LWUITImageTypeHandler());

    public DetailsForm(String title, RSSReader midlet) {
        super(title);
        this.midlet = midlet;
        setScrollableY(true);
        pubDateLabel = new Label("");

        pubDateLabel.getStyle().setFont(RSSReader.italicFont);

        imgLabel = new Label("");


        addCommand(backCommand);
        addCommand(linkCommand);
        this.setBackCommand(backCommand);

        setTransitionOutAnimator(
                CommonTransitions.createSlide(
                CommonTransitions.SLIDE_HORIZONTAL, true, 200));

        addCommandListener(this);
    }

    public void actionPerformed(ActionEvent ae) {
        String cmdStr = ae.getCommand().getCommandName();

        if (cmdStr.equals("Open link")) {
            try {
                midlet.platformRequest(current.getLink());
            } catch (Exception e) {
                RSSReader.showDialog("Couldn't open link");
            }
        }
        if (cmdStr.equals("Back")) {
            midlet.getListForm().show();
        }
    }

    public void setCurrentRSSItem(final RSSItem item) {
        current = item;
        removeAll();

        titleLabels = getLabels(item.getTitle(), RSSReader.mediumFont, RSSReader.SCREEN_WIDTH);
        pubDateLabel.setText(item.getPubDate());
        descriptionLabels = getLabels(item.getDescription(), RSSReader.plainFont, RSSReader.SCREEN_WIDTH);
        linkLabels = getLabels(item.getLink(), RSSReader.underlinedFont, RSSReader.SCREEN_WIDTH);
        imgLabel = new Label("");
        addLabels(titleLabels);
        addComponent(pubDateLabel);
        addLabels(descriptionLabels);
        addComponent(imgLabel);

        imageCache.getAsync(item.getThumbnail(), Task.HIGH_PRIORITY,
                StaticWebCache.GET_ANYWHERE, new Task(Task.UI_PRIORITY) {
            protected Object exec(final Object in) {
                try {
                    imgLabel.setIcon((Image) in);
                    repaint();
                } catch (Exception ex) {
                    //#debug
                    L.e("Can not get image for RSSItem", item.getThumbnail(), ex);
                }

                return in;
            }
        }.setClassName("RSSItemIconSetter"));

        addLabels(linkLabels);
        setScrollY(0);
    }

    public Vector getLabels(String str, com.sun.lwuit.Font font, int width) {
        final Vector labels = new Vector();
        final Vector lines = StringUtils.splitToLines(str, font, width);
        for (int i = 0; i < lines.size(); i++) {
            labels.addElement(new Label((String) lines.elementAt(i)));
            ((Label) labels.lastElement()).getStyle().setFont(font);
            ((Label) labels.lastElement()).setGap(0);
        }
        return labels;
    }

    public void addLabels(Vector labels) {
        for (int i = 0; i < labels.size(); i++) {
            addComponent((Label) labels.elementAt(i));
        }
    }
}
