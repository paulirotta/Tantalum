package com.futurice.s40rssreader;

import com.nokia.mid.ui.DirectGraphics;
import com.nokia.mid.ui.DirectUtils;
import com.nokia.mid.ui.frameanimator.FrameAnimator;
import com.nokia.mid.ui.frameanimator.FrameAnimatorListener;
import com.nokia.mid.ui.gestures.GestureEvent;
import com.nokia.mid.ui.gestures.GestureInteractiveZone;
import com.nokia.mid.ui.gestures.GestureListener;
import com.nokia.mid.ui.gestures.GestureRegistrationManager;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * The main canvas for displaying RSS Feed list and item details
 * @author ssaa
 */
public class RSSReaderCanvas extends Canvas implements GestureListener, FrameAnimatorListener {

    private static RSSReaderCanvas instance;

    private final RSSReader rssReader;

    private final FrameAnimator animator;

    private final ListView listView;
    private final DetailsView detailsView;

    private View currentView;

    public static final Font FONT_TITLE = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
    public static final Font FONT_DESCRIPTION = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    public static final Font FONT_DATE = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);

    public static int COLOR_BACKGROUND;
    public static int COLOR_HIGHLIGHTED_BACKGROUND;
    public static int COLOR_FOREGROUND;
    public static int COLOR_HIGHLIGHTED_FOREGROUND;
    public static int COLOR_BORDER;
    public static int COLOR_HIGHLIGHTED_BORDER;

    public static final int MARGIN = FONT_TITLE.getHeight() / 2;

    /**
     * Constructor for RSSReaderCanvas
     * @param rssReader
     */
    public RSSReaderCanvas(RSSReader rssReader) {
        super();
        instance = this;
        this.rssReader = rssReader;

        initColors();

        listView = new ListView(this);
        detailsView = new DetailsView(this);

        //register frameanimator with default values
        animator = new FrameAnimator();
        short pps = 0;
        short fps = 0;
        animator.register(0, 0, fps, pps, this);

        //register for gesturevents
        GestureInteractiveZone giz = new GestureInteractiveZone(GestureInteractiveZone.GESTURE_ALL);
        GestureRegistrationManager.register(this, giz);
        GestureRegistrationManager.setListener(this, this);

        setCurrentView(listView);
    }

    private void initColors() {
        COLOR_BACKGROUND = rssReader.getDisplay().getColor(Display.COLOR_BACKGROUND);
        COLOR_HIGHLIGHTED_BACKGROUND = rssReader.getDisplay().getColor(Display.COLOR_HIGHLIGHTED_BACKGROUND);
        COLOR_FOREGROUND = rssReader.getDisplay().getColor(Display.COLOR_FOREGROUND);
        COLOR_HIGHLIGHTED_FOREGROUND = rssReader.getDisplay().getColor(Display.COLOR_HIGHLIGHTED_FOREGROUND);
        COLOR_BORDER = rssReader.getDisplay().getColor(Display.COLOR_BORDER);
        COLOR_HIGHLIGHTED_BORDER = rssReader.getDisplay().getColor(Display.COLOR_HIGHLIGHTED_BORDER);
    }

    /**
     * Returns the instance of RSSReaderCanvas
     * @return RSSReaderCanvas
     */
    public static RSSReaderCanvas getInstance() {
        return instance;
    }

    public RSSReader getRssReader() {
        return rssReader;
    }

    public ListView getListView() {
        return listView;
    }

    public DetailsView getDetailsView() {
        return detailsView;
    }

    /**
     * Paints the content of the current view
     * @param g
     */
    public void paint(final Graphics g) {
        currentView.render(g, DirectUtils.getDirectGraphics(g), getWidth() - View.SCROLL_BAR_WIDTH, getHeight());
    }

    /**
     * Shows the list view
     */
    public void showList() {
        animator.stop();
        listView.setSelectedIndex(-1);
        detailsView.getSelectedItem().setThumbnailImage(null);
        detailsView.setSelectedItem(null);
        setCurrentView(listView);
    }

    /**
     * Show the details view
     * @param selectedItem
     */
    public void showDetails(final RSSItem selectedItem) {
        animator.stop();
        detailsView.setSelectedItem(selectedItem);
        setCurrentView(detailsView);
    }

    /**
     * @see GestureListener.gestureAction(Object o, GestureInteractiveZone giz, GestureEvent ge)
     */
    public void gestureAction(Object o, GestureInteractiveZone giz, GestureEvent ge) {

        switch (ge.getType()) {
            case GestureInteractiveZone.GESTURE_DRAG:
                listView.setSelectedIndex(-1);
                animator.drag(0, currentView.getRenderY() + ge.getDragDistanceY());
                break;
            case GestureInteractiveZone.GESTURE_DROP:
                //no-op
                break;
            case GestureInteractiveZone.GESTURE_FLICK:
                listView.setSelectedIndex(-1);
                animator.kineticScroll(ge.getFlickSpeed(), FrameAnimator.FRAME_ANIMATOR_FREE_ANGLE, FrameAnimator.FRAME_ANIMATOR_FRICTION_MEDIUM, ge.getFlickDirection());
                break;
            case GestureInteractiveZone.GESTURE_LONG_PRESS:
                //no-op
                break;
            case GestureInteractiveZone.GESTURE_LONG_PRESS_REPEATED:
                //no-op
                break;
            case GestureInteractiveZone.GESTURE_TAP:
                if (currentView == listView) {
                    //selects the tapped item
                    listView.selectItem(ge.getStartX(), ge.getStartY(), true);
                }
                break;
        }
    }

    protected void pointerPressed(int x, int y) {
        //just paints the highlight on the selected item
        listView.selectItem(x, y, false);
    }

    public void sizeChanged(final int widht, final int height) {
        listView.canvasSizeChanged();
    }
    
    /*
     * @see FrameAnimatorListener.animate(FrameAnimator fa, int x, int y, short delta, short deltaX, short deltaY, boolean lastFrame)
     */
    public void animate(FrameAnimator fa, int x, int y, short delta, short deltaX, short deltaY, boolean lastFrame) {
        currentView.setRenderY(y);
        repaint();
    }

    public void setCurrentView(final View nextView) {
        if (this.currentView != null) {
            for (int i = 0; i < this.currentView.getCommands().length; i++)
                removeCommand(this.currentView.getCommands()[i]);
        }
        this.currentView = nextView;
        for (int i = 0; i < currentView.getCommands().length; i++)
            addCommand(currentView.getCommands()[i]);
        setCommandListener(currentView);
        repaint();
    }
}
