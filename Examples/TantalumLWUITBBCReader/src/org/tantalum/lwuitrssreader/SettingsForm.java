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
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.animations.CommonTransitions;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;

/**
 * @author tsaa
 */
public class SettingsForm extends Form implements ActionListener {

    private static Command saveCommand = new Command("Save");
    private static Command backCommand = new Command("Back");
    private Label urlLabel;
    private TextArea urlTextArea;
    private RSSReader midlet;

    public SettingsForm(String title, RSSReader midlet) {
        super(title);
        this.midlet = midlet;

        urlLabel = new Label("RSS Feed URL");
        urlTextArea = new TextArea(midlet.getUrl());
        urlTextArea.setEditable(true);
        urlTextArea.getStyle().setFont(RSSReader.plainFont);
        this.addComponent(urlLabel);
        this.addComponent(urlTextArea);
        this.addCommand(saveCommand);
        this.addCommand(backCommand);
        this.setBackCommand(backCommand);

        setTransitionOutAnimator(
                CommonTransitions.createSlide(
                CommonTransitions.SLIDE_HORIZONTAL, false, 200));

        addCommandListener(this);
    }

    public void actionPerformed(ActionEvent ae) {
        String cmdStr = ae.getCommand().getCommandName();
        if (cmdStr.equals("Save")) {
            midlet.setUrl(urlTextArea.getText());
            midlet.getListForm().show();

        }
        if (cmdStr.equals("Back")) {
            urlTextArea.setText(midlet.getUrl());
            midlet.getListForm().show();
        }
    }
}
