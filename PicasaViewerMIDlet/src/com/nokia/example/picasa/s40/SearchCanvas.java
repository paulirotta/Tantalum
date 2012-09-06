package com.nokia.example.picasa.s40;

import com.futurice.tantalum3.log.L;
import com.nokia.example.picasa.common.ImageObject;
import com.nokia.example.picasa.common.Storage;
import com.nokia.mid.ui.TextEditor;
import java.io.IOException;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.TextField;

/**
 * Class for displaying the search bar and search results.
 *
 */
public final class SearchCanvas extends ImageGridCanvas {

    private static final int SEARCH_HEIGHT = 25;
    private static final int SEARCH_WIDTH = 180;
    private static final int SEARCH_PADDING = 10;
    private static final int HEADER_BAR_HEIGHT = SEARCH_HEIGHT + 2 * SEARCH_PADDING;
    private static final int SEARCH_BUTTON_WIDTH = 26;
    private static final int ROUNDED = 10;
    private Image searchImg;
    private TextEditor searchField;
    private String searchText = "Search";

    public SearchCanvas(PicasaViewer midlet) {
        super(midlet);
        
        headerHeight = HEADER_BAR_HEIGHT;

        try {
            searchImg = Image.createImage("/search.png");
        } catch (IOException e) {
            //#debug
            L.e("Can not create search image", "", e);
        }
    }

    public void showNotify() {
        // Status bar doesn't work well with on-screen keyboard, so leave it out.
        this.setFullScreenMode(true);

        super.showNotify();
    }
    
    public void hideNotify() {
        disableKeyboard();
        
        super.hideNotify();
    }

    public void paint(final Graphics g) {
        if (searchField != null && !searchField.isVisible()) {
            disableKeyboard();
        }
        drawGrid(g, HEADER_BAR_HEIGHT);

        /*
         * Draw search field
         */
        g.setColor(0x000000);
        g.fillRect(0, 0, getWidth(), HEADER_BAR_HEIGHT);
        g.setColor(0xffffff);
        g.fillRoundRect(SEARCH_PADDING, SEARCH_PADDING, SEARCH_WIDTH, SEARCH_HEIGHT, ROUNDED, ROUNDED);
        if (searchField == null) {
            g.setColor(0x000000);
            g.drawString(searchText, SEARCH_PADDING + 1, SEARCH_PADDING + 1, Graphics.LEFT | Graphics.TOP);
        }
        g.drawImage(searchImg, getWidth() - SEARCH_PADDING, HEADER_BAR_HEIGHT / 2, Graphics.VCENTER | Graphics.RIGHT);
    }

    public void gestureTap(int startX, int startY) {
        final int index = getItemIndex(startX, startY);

        // Not search bar
        if (index >= 0) {
            // Keyboard is not active
            if (searchField == null) {
                if (imageObjects.size() > index) {
                    Storage.selectedImage = (ImageObject) imageObjects.elementAt(index);
                    midlet.setDetailed();
                }
            } else {
                disableKeyboard();
            }
            //Search bar
        } else {
            //Search button
            if (startX > getWidth() - SEARCH_BUTTON_WIDTH
                    && startY < HEADER_BAR_HEIGHT) {

                imageObjects.removeAllElements();
                disableKeyboard();
                scrollY = 0;
                loadFeed(true, true);
                repaint();
                //Text Input
            } else {
                enableKeyboard();
            }
        }
    }

    private void enableKeyboard() {
        if (searchText.equals("Search")) {
            searchText = "";
        }
        searchField = TextEditor.createTextEditor(searchText, 20, TextField.ANY, SEARCH_WIDTH, SEARCH_HEIGHT);
        searchField.setForegroundColor(0xFF000000);
        searchField.setBackgroundColor(0x00000000);
        searchField.setParent(this); // Canvas to draw on
        searchField.setPosition(SEARCH_PADDING, SEARCH_PADDING);
        searchField.setIndicatorVisibility(true);
        searchField.setVisible(true);
        searchField.setFocus(true);
        searchField.setCaret(searchText.length());
    }

    private void disableKeyboard() {
        if (searchField != null) {
            searchText = searchField.getContent();
            searchField.setParent(null);
            searchField = null;
            if (searchText.equalsIgnoreCase("")) {
                searchText = "Search";
            }
        }
    }
}
