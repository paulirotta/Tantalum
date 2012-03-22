package com.futurice.s40rssreader;

import com.futurice.rssreader.common.StringUtils;
import com.futurice.tantalum2.DefaultResult;
import com.futurice.tantalum2.log.Log;
import com.futurice.tantalum2.net.StaticWebCache;
import com.futurice.tantalum2.rms.ImageTypeHandler;
import java.util.Vector;
import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * View for rendering details of an RSS item
 *
 * @author ssaa
 */
public class DetailsView extends View {

    private RSSItem selectedItem;
    private final StaticWebCache imageCache = new StaticWebCache("images", '1', new ImageTypeHandler());
    private int contentHeight;
    private Command openLinkCommand = new Command("Open link", Command.ITEM, 0);
    private Command backCommand = new Command("Back", Command.BACK, 0);

    public DetailsView(RSSReaderCanvas canvas) {
        super(canvas);
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
            Log.l.log("Eror opening link", getCanvas().getDetailsView().getSelectedItem().getLink(), connectionNotFoundException);
            canvas.getRssReader().showError("Could not open link");
        }
    }

    /*
     * Renders the details of the selected item
     */
    public void render(final Graphics g, final int width, final int height) {
        if (contentHeight < canvas.getHeight()) {
            this.renderY = 0;
        } else if (this.renderY < -contentHeight + canvas.getHeight()) {
            this.renderY = -contentHeight + canvas.getHeight();
        } else if (this.renderY > 0) {
            this.renderY = 0;
        }

        int curY = renderY;

        g.setColor(RSSReaderCanvas.COLOR_HIGHLIGHTED_BACKGROUND);
        g.fillRect(0, 0, width, height);

        g.setFont(RSSReaderCanvas.FONT_TITLE);
        g.setColor(RSSReaderCanvas.COLOR_HIGHLIGHTED_FOREGROUND);
        curY = renderLines(g, curY, RSSReaderCanvas.FONT_TITLE.getHeight(), StringUtils.splitToLines(selectedItem.getTitle(), RSSReaderCanvas.FONT_TITLE, canvas.getWidth() - 2 * RSSReaderCanvas.MARGIN));

        g.setFont(RSSReaderCanvas.FONT_DATE);
        g.drawString(selectedItem.getPubDate(), 10, curY, Graphics.LEFT | Graphics.TOP);

        curY += RSSReaderCanvas.FONT_DATE.getHeight() * 2;

        g.setFont(RSSReaderCanvas.FONT_DESCRIPTION);
        curY = renderLines(g, curY, RSSReaderCanvas.FONT_DESCRIPTION.getHeight(), StringUtils.splitToLines(selectedItem.getDescription(), RSSReaderCanvas.FONT_DESCRIPTION, canvas.getWidth() - 2 * RSSReaderCanvas.MARGIN));

        curY += RSSReaderCanvas.FONT_DESCRIPTION.getHeight();

        final String url = selectedItem.getThumbnail();
        if (url != null) {
            final Image image = (Image) imageCache.synchronousRAMCacheGet(url);
            if (image != null) {
                g.drawImage(image, canvas.getWidth() >> 1, curY, Graphics.TOP | Graphics.HCENTER);
                curY += image.getHeight() + RSSReaderCanvas.FONT_TITLE.getHeight();
            } else if (!selectedItem.isLoadingImage()) {
                // Not already loading image, so request it
                final RSSItem item = selectedItem;
                item.setLoadingImage(true);
                imageCache.get(item.getThumbnail(), new DefaultResult() {

                    public void setResult(Object o) {
                        super.setResult(o);
                        item.setLoadingImage(false);
                        DetailsView.this.getCanvas().repaint();
                    }
                });
            }
        }

        contentHeight = curY - renderY;

        renderScrollBar(g, contentHeight);
    }

    private int renderLines(final Graphics g, int curY, final int lineHeight, final Vector lines) {
        final int len = lines.size();

        for (int i = 0; i < len; i++) {
            g.drawString((String) lines.elementAt(i), RSSReaderCanvas.MARGIN, curY, Graphics.LEFT | Graphics.TOP);
            curY += lineHeight;
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
