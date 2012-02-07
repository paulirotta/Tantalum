package com.futurice.lwuitrssreader;

import com.sun.lwuit.Command;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.Form;
import com.sun.lwuit.animations.CommonTransitions;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.ActionEvent;

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
