package com.futurice.s40rssreader;

import com.futurice.rssreader.common.StringUtils;
import com.futurice.tantalum2.rms.DefaultGetResult;
import com.futurice.tantalum2.log.Log;
import com.futurice.tantalum2.net.StaticWebCache;
import com.futurice.tantalum2.rms.ImageTypeHandler;
import java.util.Vector;
import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * View for rendering details of an RSS item
 * @author ssaa
 */
public class DetailsView extends View {

    private RSSItem selectedItem;
    private StaticWebCache staticWebCache;

    private int contentHeight;

    private Command openLinkCommand = new Command("Open link", Command.ITEM, 0);
    private Command backCommand = new Command("Back", Command.BACK, 0);    

    public DetailsView(RSSReaderCanvas canvas) {
        super(canvas);
        this.staticWebCache = new StaticWebCache("images", 1, 10, new ImageTypeHandler());
    }

    public Command[] getCommands() {
        return new Command[]{openLinkCommand, backCommand};
    }

    public void commandAction(Command command, Displayable d) {
        if (command == openLinkCommand) {
            openLink();
        } else if (command == backCommand) {
            canvas.showList();
        }
    }

    /**
     * Opens a link in the browser for the selected RSS item
     */
    private void openLink() {
        try {
            boolean needsToClose = canvas.getRssReader().platformRequest(getCanvas().getDetailsView().getSelectedItem().getLink());
            if (needsToClose) {
                canvas.getRssReader().exitMIDlet();
            }
        } catch (ConnectionNotFoundException connectionNotFoundException) {
            Log.logNonfatalThrowable(connectionNotFoundException, "Eror openin link: " + getCanvas().getDetailsView().getSelectedItem().getLink());
            canvas.getRssReader().showError("Could not open link");
        }
    }

    /*
     * Renders the details of the selected item
     */
    public void render(Graphics g) {
        if (contentHeight < canvas.getHeight()) {
            this.renderY = 0;
        } else if (this.renderY < -contentHeight + canvas.getHeight()) {
            this.renderY = -contentHeight + canvas.getHeight();
        } else if (this.renderY > 0) {
            this.renderY = 0;
        }

        int curY = renderY;

        g.setFont(RSSReaderCanvas.FONT_TITLE);
        curY = renderLines(g, curY, RSSReaderCanvas.FONT_TITLE, StringUtils.splitToLines(selectedItem.getTitle(), RSSReaderCanvas.FONT_TITLE, canvas.getWidth() - 2*RSSReaderCanvas.MARGIN));

        g.setFont(RSSReaderCanvas.FONT_DATE);
        g.drawString(selectedItem.getPubDate(), 10, curY, Graphics.LEFT | Graphics.TOP);

        curY += RSSReaderCanvas.FONT_DATE.getHeight() * 2;

        g.setFont(RSSReaderCanvas.FONT_DESCRIPTION);
        curY = renderLines(g, curY, RSSReaderCanvas.FONT_DESCRIPTION, StringUtils.splitToLines(selectedItem.getDescription(), RSSReaderCanvas.FONT_DESCRIPTION, canvas.getWidth() - 2*RSSReaderCanvas.MARGIN));

        curY += RSSReaderCanvas.FONT_DESCRIPTION.getHeight();

        if (selectedItem.getThumbnail() != null) {
            if (selectedItem.getThumbnailImage() != null) {
                g.drawImage(selectedItem.getThumbnailImage(), canvas.getWidth() / 2, curY, Graphics.TOP | Graphics.HCENTER);
                curY += selectedItem.getThumbnailImage().getHeight() + RSSReaderCanvas.FONT_TITLE.getHeight();
            } else if (!selectedItem.isLoadingImage()) {
                //request the thumbnail image, if not already loading
                selectedItem.setLoadingImage(true);
                staticWebCache.get(selectedItem.getThumbnail(), new DefaultGetResult() {
                    public void run() {
                        selectedItem.setThumbnailImage((Image)getResult());
                        DetailsView.this.getCanvas().repaint();
                        selectedItem.setLoadingImage(false); 
                    }
                }); 
            }  
        }

        contentHeight = curY - renderY;

        renderScrollBar(g, contentHeight);
    }

    private int renderLines(Graphics g, int startY, Font font, Vector lines) {
        int curY = startY;
        final int len = lines.size();
        for (int i = 0; i < len; i++) {
            g.drawString((String)lines.elementAt(i), RSSReaderCanvas.MARGIN, curY, Graphics.LEFT | Graphics.TOP);
            curY += font.getHeight();
        }
        return curY;
    }

    public RSSItem getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(RSSItem selectedItem) {
        this.selectedItem = selectedItem;
    }
}
