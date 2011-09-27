/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.skynewsreader;

import com.sun.lwuit.Command;
import com.sun.lwuit.Component;
import com.sun.lwuit.Form;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Image;
import com.sun.lwuit.animations.CommonTransitions;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.geom.Dimension;
import java.util.Vector;

/**
 *
 * @author tsaa
 */
public class DetailsForm extends Form implements ActionListener {

    static Command linkCommand = new Command("Open link");
    static Command backCommand = new Command("Back");
    private SkyNewsReader midlet;
    private RSSItem current;

    public DetailsForm(String title, SkyNewsReader midlet) {
        super(title);
        this.midlet = midlet;
        getStyle().setBgColor(0x444444);
        getTitleComponent().getStyle().setBgColor(0x770000);
        getSoftButtonStyle().setBgColor(0x770000);
        setScrollableY(true);

        addCommand(linkCommand);
        addCommand(backCommand);

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
                SkyNewsReader.showDialog("Couldn't open link");
            }
        }
        if (cmdStr.equals("Back")) {
            midlet.getListForm().show();
        }
    }

    public void setCurrentRSSItem(final RSSItem item) {
        current = item;
        removeAll();

        addComponent(new DetailsComponent(current));
        setScrollY(0);
    }

    public void repaintImg() {
        if (current.getImg() != null) {
            repaint();
        }
    }

    class DetailsComponent extends Component {

        RSSItem item;

        public DetailsComponent(RSSItem item) {
            this.item = item;
            setPreferredSize(new Dimension(SkyNewsReader.SCREEN_WIDTH, 260));
        }

        public void paint(Graphics g) {
            Vector titleLines = StringUtils.splitToLines(item.getTitle(), SkyNewsReader.mediumFont, SkyNewsReader.SCREEN_WIDTH);
            Vector descLines = StringUtils.splitToLines(item.getDescription(), SkyNewsReader.plainFont, SkyNewsReader.SCREEN_WIDTH);

            g.setColor(0xEEEEEE);
            g.fillRect(2, 2, this.getWidth(), this.getHeight());
            g.setColor(0xCCCCCC);
            g.drawRect(2, 2, this.getWidth() - 1, this.getHeight() - 1);
            g.setFont(SkyNewsReader.mediumFont);
            g.setColor(0xCCCCCC);
            g.fillRect(3, 3, this.getWidth() - 2, titleLines.size() * 17 + 3);
            g.setColor(0x000000);
            for (int i = 0; i < titleLines.size(); i++) {
                g.drawString((String) titleLines.elementAt(i), 4, i * 17);
            }

            Image img = null;
            if (item != null) {
                img = item.getImg();
            }
            if (img != null) {
                g.drawRect(7, titleLines.size() * 17 + 10, 96, 54);
                g.drawImage(img, 8, titleLines.size() * 17 + 11);
            }

            g.setFont(SkyNewsReader.plainFont);
            for (int i = 0; i < descLines.size(); i++) {
                g.drawString((String) descLines.elementAt(i), 4, titleLines.size() * 17 + 11 + 54 + 4 + i * 15);
            }
        }
    }
}
