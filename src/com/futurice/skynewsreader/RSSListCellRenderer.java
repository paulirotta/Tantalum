package com.futurice.skynewsreader;

import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Image;
import com.sun.lwuit.Label;
import com.sun.lwuit.list.ListCellRenderer;
import com.sun.lwuit.List;
import com.sun.lwuit.geom.Dimension;
import java.util.Vector;

/**
 *
 * @author tsaa
 */
public class RSSListCellRenderer implements ListCellRenderer {

    private Label imgLabel = new Label("");
    //private Label titleLabel = new Label("");
    private Vector titleLabels = new Vector();

    public RSSListCellRenderer() {
        /*setLayout(new BorderLayout());
        addComponent(BorderLayout.WEST, imgLabel);
        Container cnt = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        for (int i = 0; i < 3; i++) {
        Label l = new Label("");
        l.setGap(0);
        titleLabels.addElement(l);
        l.getStyle().setFont(SkyNewsReader.plainFont);
        cnt.addComponent((Label) titleLabels.elementAt(i));
        }
        addComponent(BorderLayout.CENTER, cnt);*/
    }

    public Component getListCellRendererComponent(List list, Object value, int index, boolean isSelected) {
        Container c = new Container();
        final RSSItem item = ((RSSItem) value);
        ItemComponent itemComponent = new ItemComponent(item);
        c.addComponent(itemComponent);
        return c;
        /*Image img = item.getImg();
        
        if (img != null) {
        imgLabel.setIcon(img);
        imgLabel.setVisible(true);
        } else {
        imgLabel.setVisible(false);
        }
        Vector lines = StringUtils.splitToLines(item.getTitle(), SkyNewsReader.plainFont, SkyNewsReader.SCREEN_WIDTH / 2 - 10);
        for (int i = 0; i < titleLabels.size(); i++) {
        if (i < lines.size()) {
        ((Label) titleLabels.elementAt(i)).setText((String) lines.elementAt(i));
        } else {
        ((Label) titleLabels.elementAt(i)).setText("");
        }
        }
        return this;*/
    }

    public Component getListFocusComponent(List list) {
        return null;
    }

    class ItemComponent extends Component {

        RSSItem item;

        public ItemComponent(RSSItem item) {
            this.item = item;
            this.setPreferredSize(new Dimension(SkyNewsReader.SCREEN_WIDTH, 64)); // rss image height = 53
        }

        public void paint(Graphics g) {
            g.setColor(0xEEEEEE);
            g.fillRect(2, 2, this.getWidth(), this.getHeight());
            g.setColor(0xCCCCCC);
            g.drawRect(2, 2, this.getWidth()-1, this.getHeight()-1);

            g.setColor(0x000000);
            Image img = null;
            if (item != null) {
                img = item.getImg();
            }
            if (img != null) {
                g.drawRect(6, 6, 96, 54);
                g.drawImage(img, 7, 7);
            }
            g.setFont(SkyNewsReader.plainFont);
            Vector lines = StringUtils.splitToLines(item.getTitle(), SkyNewsReader.plainFont, SkyNewsReader.SCREEN_WIDTH / 2);
            for (int i = 0; i < 4 && i < lines.size(); i++) {
                g.drawString((String) lines.elementAt(i), 105, 2 + 14 * i);
            }
        }
    }
}