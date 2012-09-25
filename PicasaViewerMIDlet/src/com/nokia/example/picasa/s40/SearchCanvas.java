package com.nokia.example.picasa.s40;

import com.futurice.tantalum3.AsyncCallbackTask;
import com.futurice.tantalum3.PlatformUtils;
import com.futurice.tantalum3.Worker;
import com.futurice.tantalum3.log.L;
import com.futurice.tantalum3.net.StaticWebCache;
import com.nokia.example.picasa.common.PicasaImageObject;
import com.nokia.example.picasa.common.PicasaStorage;
import com.nokia.mid.ui.TextEditor;
import com.nokia.mid.ui.TextEditorListener;
import java.io.IOException;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
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
    private static int SEARCH_BUTTON_WIDTH;
    private static final int ROUNDED = 10;
    private Image searchImg;
    private TextEditor searchField;
    private String searchText = "";
    private Command deleteCommand;
    private final Command featuredCommand = new Command("Featured", Command.SCREEN, 0);

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
            SEARCH_BUTTON_WIDTH = searchImg.getWidth();
        } catch (IOException e) {
            //#debug
            L.e("Can not create search image", "", e);
        }
        if (!midlet.phoneSupportsCategoryBar()) {
                addCommand(featuredCommand);
        }
    }

    public void commandAction(Command c, Displayable d) {
        //#debug
        L.i("SearchCanvas Command action", "Command " + c);

        if (c == featuredCommand) {
            midlet.goFeaturedCanvas();
        } else if (c.getCommandType() == Command.CANCEL) {
            deleteChar();
        }
    }

    public void showNotify() {
        // Status bar doesn't work well with on-screen keyboard, so leave it out.
        if (!midlet.phoneSupportsCategoryBar() || imageObjectModel.size() == 0) {
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
        disableKeyboard(true);

        super.hideNotify();
    }

    public void paint(final Graphics g) {
        checkScroll();
        if (searchField != null && !searchField.isVisible()) {
            disableKeyboard(false);
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

    public void deleteChar() {
        if (searchField != null && searchField.getContent().length() != 0) {
            searchField.delete(searchField.getContent().length() - 1, 1);
            searchText = searchField.getContent().trim().toLowerCase();
            refresh(searchText, StaticWebCache.GET_LOCAL);
        }
    }

    public boolean gestureTap(int startX, int startY) {
        //#debug
        L.i("tap", searchText + " (" + startX + ", " + startY + ")");
        if (!super.gestureTap(startX, startY)) {
            final int index = getItemIndex(startX, startY);

            // Not search bar
            if (index >= 0) {
                // Keyboard is not active
                if (searchField == null) {
                    if (imageObjectModel.size() > index) {
                        PicasaStorage.setSelectedImage((PicasaImageObject) imageObjectModel.elementAt(index));
                        midlet.goDetailCanvas();
                    }
                } else {
                    PlatformUtils.runOnUiThread(new Runnable() {
                        public void run() {
                            disableKeyboard(true);
                        }
                    });
                }
            } else {
                //Search bar or button
                if (startX > getWidth() - SEARCH_BUTTON_WIDTH * 2
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

    private AsyncCallbackTask startSearch() {
        if (searchField != null) {
            searchText = searchField.getContent().trim().toLowerCase();
        }
        disableKeyboard(false);
        scrollY = 0;

        return loadFeed(searchText, StaticWebCache.GET_WEB);
    }

    private void enableKeyboard() {
        if (searchField != null) {
            searchField.setCaret(searchField.getContent().length());
            return;
        }
        searchField = TextEditor.createTextEditor("", 20, TextField.NON_PREDICTIVE, SEARCH_WIDTH, SEARCH_HEIGHT);
        searchField.insert(searchText, 0);
        searchField.setForegroundColor(0xFF000000);
        searchField.setBackgroundColor(0x00000000);
        searchField.setParent(this); // Canvas to draw on
        searchField.setPosition(SEARCH_PADDING, SEARCH_PADDING);
        if (this.hasPointerEvents()) {
            searchField.setTouchEnabled(true);
        }
        searchField.setTextEditorListener(new TextEditorListener() {
            public void inputAction(final TextEditor textEditor, final int action) {
                //#debug
                L.i("TextEditor inputAction", "" + action);
                if ((action & TextEditorListener.ACTION_CONTENT_CHANGE) != 0) {
                    searchText = textEditor.getContent().trim().toLowerCase();
                    //#debug
                    L.i("TextEditorListener, content changed refresh", searchText);
                    refresh(searchText, StaticWebCache.GET_LOCAL);
                } else {
                    repaint();
                }
            }
        });
        final String keyboard = System.getProperty("com.nokia.keyboard.type");
        if ("PhoneKeypad".equals(keyboard)) {
            deleteCommand = new Command("Delete", Command.CANCEL, 0);
            addCommand(deleteCommand);
        }
        searchField.setVisible(true);
        searchField.setFocus(true);
    }

    private boolean disableKeyboard(final boolean force) {
        if (!force && !midlet.phoneSupportsCategoryBar()) {
            return false;
        }
        if (searchField != null) {
            searchText = searchField.getContent().trim().toLowerCase();
            //#debug
            L.i("TextEditor disabled", searchText);
            searchField.setParent(null);
            searchField = null;
            removeCommand(deleteCommand);
            deleteCommand = null;
            return true;
        }

        return false;
    }

    public void gesturePinch(
            int pinchDistanceStarting,
            int pinchDistanceCurrent,
            int pinchDistanceChange,
            int centerX,
            int centerY,
            int centerChangeX,
            int centerChangeY) {

        // Pinch to reload
        startSearch();
    }
}
