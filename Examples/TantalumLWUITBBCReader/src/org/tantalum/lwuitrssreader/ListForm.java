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

import com.sun.lwuit.*;
import com.sun.lwuit.animations.CommonTransitions;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.list.ListCellRenderer;
import org.tantalum.PlatformUtils;
import org.tantalum.Task;
import org.tantalum.net.StaticWebCache;
import org.tantalum.net.xml.RSSItem;

/**
 * @author tsaa
 */
public final class ListForm extends Form implements ActionListener, ListCellRenderer {

    static final Command settingsCommand = new Command("Settings");
    static final Command reloadCommand = new Command("Reload");
    static final Command exitCommand = new Command("Exit");
    private final ListModel listModel = new ListModel(this);
    public final List list = new List(listModel);
    private final StaticWebCache feedCache = StaticWebCache.getWebCache('5', listModel);
    private RSSReader midlet;
    private boolean isReloading = false;

    public ListForm(String title, RSSReader midlet) {
        super(title);
        this.midlet = midlet;
        list.addActionListener(this);

        addComponent(list);
        addCommand(settingsCommand);
        addCommand(exitCommand);
        addCommand(reloadCommand);
        setBackCommand(exitCommand);

        setTransitionOutAnimator(
                CommonTransitions.createSlide(
                CommonTransitions.SLIDE_HORIZONTAL, false, 200));
        list.setRenderer(this);
        this.addCommandListener(this);
        reload(false);
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getCommand() != null) {
            String cmdStr = ae.getCommand().getCommandName();

            if (cmdStr.equals("Reload")) {
                reload(true);
            }
            if (cmdStr.equals("Exit")) {
                PlatformUtils.getInstance().shutdown(true);
            }
        } else {
            int selectedIndex = ((List) ae.getSource()).getSelectedIndex();
            DetailsForm detailsForm = midlet.getDetailsForm();
            detailsForm.setCurrentRSSItem((RSSItem) listModel.getItemAt(selectedIndex));
            detailsForm.show();
        }
    }

    public void reload(final boolean fromNet) {
        if (!isReloading) {
            isReloading = true;

            int listSize = list.getModel().getSize();
            for (int i = 0; i < listSize; i++) {
                list.getModel().removeItem(i);
            }

            final Task task = new Task() {
                public Object exec(final Object in) {
                    isReloading = false;

                    return in;
                }

                protected void onCanceled() {
                    isReloading = false;
                }
            };

            if (fromNet) {
                feedCache.getAsync(midlet.getUrl(), Task.HIGH_PRIORITY, StaticWebCache.GET_WEB, task);
            } else {
                feedCache.getAsync(midlet.getUrl(), Task.HIGH_PRIORITY, StaticWebCache.GET_ANYWHERE, task);
            }
        }
    }

    public RSSReader getMIDlet() {
        return midlet;
    }

    public Component getListCellRendererComponent(List list, Object value, int index, boolean isSelected) {
        Container c = new Container();
        Label titleLabel = new Label(((RSSItem) value).getTitle());
        titleLabel.getStyle().setFont(RSSReader.plainFont);
        c.addComponent(titleLabel);
        return c;
    }

    public Component getListFocusComponent(List list) {
        return null;
    }
}
