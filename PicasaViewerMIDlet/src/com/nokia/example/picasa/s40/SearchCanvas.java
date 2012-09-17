package com.nokia.example.picasa.s40;

import com.futurice.tantalum3.PlatformUtils;
import com.futurice.tantalum3.log.L;
import com.nokia.example.picasa.common.PicasaImageObject;
import com.nokia.example.picasa.common.PicasaStorage;
import com.nokia.mid.ui.TextEditor;
import com.nokia.mid.ui.TextEditorListener;
import com.nokia.mid.ui.gestures.GestureInteractiveZone;
import java.io.IOException;
import javax.microedition.lcdui.Command;
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
    private Command deleteCommand;

    public SearchCanvas(PicasaViewer midlet) {
        super(midlet);

        headerHeight = HEADER_BAR_HEIGHT;
        statusBarVisible = Boolean.FALSE;
        setTitle("Search");
        if (midlet.phoneSupportsCategoryBar()) {
            this.setFullScreenMode(true);
        }
        try {
            searchImg = Image.createImage("/search.png");
        } catch (IOException e) {
            //#debug
            L.e("Can not create search image", "", e);
        }
    }

    public void showNotify() {
        // Status bar doesn't work well with on-screen keyboard, so leave it out.
        if (!midlet.phoneSupportsCategoryBar()) {
            /*
             * Throw an event forward on the UI thread
             * 
             * SDK 1.0 and 1.1 phones don't use an onscreen keyboard, so enter
             * edit mode right away so the user can just start typing
             */
            PlatformUtils.runOnUiThread(new Runnable() {
                public void run() {
                    enableKeyboard();
                }
            });
        }

        super.showNotify();
    }

    public void hideNotify() {
        disableKeyboard();

        super.hideNotify();
    }

    public void paint(final Graphics g) {
        checkScroll();
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

    public boolean gestureTap(int startX, int startY) {
        if (!super.gestureTap(startX, startY)) {
            final int index = getItemIndex(startX, startY);

            // Not search bar
            if (index >= 0) {
                // Keyboard is not active
                if (searchField == null) {
                    if (imageObjectModel.size() > index) {
                        PicasaStorage.selectedImage = (PicasaImageObject) imageObjectModel.elementAt(index);
                        midlet.setDetailed();
                    }
                } else {
                    PlatformUtils.runOnUiThread(new Runnable() {
                        public void run() {
                            disableKeyboard();
                        }
                    });
                }
                //Search bar
            } else {
                //Search button
                if (startX > getWidth() - SEARCH_BUTTON_WIDTH
                        && startY < HEADER_BAR_HEIGHT) {
                    startSearch();
                } else {
                    PlatformUtils.runOnUiThread(new Runnable() {
                        public void run() {
                            enableKeyboard();
                            repaint();
                        }
                    });
                }
            }
            
            return true;
        }
        
        return false;
    }

    private void startSearch() {
        imageObjectModel.removeAllElements();
        disableKeyboard();
        scrollY = 0;
        loadFeed(true, true);
        repaint();
    }

    private void enableKeyboard() {
        if (searchField != null) {
            return;
        }
        if (searchText.equals("Search")) {
            searchText = "";
        }
        searchField = TextEditor.createTextEditor(searchText, 20, TextField.ANY, SEARCH_WIDTH, SEARCH_HEIGHT);
        searchField.setForegroundColor(0xFF000000);
        searchField.setBackgroundColor(0x00000000);
        searchField.setParent(this); // Canvas to draw on
        searchField.setPosition(SEARCH_PADDING, SEARCH_PADDING);
        searchField.setCaret(searchText.length());
//        if (GestureInteractiveZone.GESTURE_ALL > 63) {
//            try {
//                ((TextEditorHelper) Class.forName("com.nokia.example.picasa.s40.TextEditorHelper").getClass().newInstance()).setIndicatorVisibility(searchField, true);
//            } catch (Throwable ex) {
//                //#debug
//                L.e("Indicator visibility", "Can not invoke", ex);
//            }
//        }
        searchField.setTextEditorListener(new TextEditorListener() {
            public void inputAction(TextEditor textEditor, int actions) {
                repaint();
            }
        });
        deleteCommand = new Command("Delete", Command.CANCEL, 0);
        addCommand(deleteCommand);
        searchField.setVisible(true);
        searchField.setFocus(true);
    }

    private void disableKeyboard() {
        if (searchField != null) {
            searchText = searchField.getContent();
            searchField.setParent(null);
            searchField = null;
            if (searchText.equalsIgnoreCase("")) {
                searchText = "Search";
            }
            removeCommand(deleteCommand);
            deleteCommand = null;
            repaint();
        }
    }
}
