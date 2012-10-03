package com.futurice.s40rssreader;

import com.futurice.tantalum4.log.L;
import com.futurice.tantalum4.net.xml.RSSItem;
import com.nokia.mid.ui.frameanimator.FrameAnimator;
import com.nokia.mid.ui.frameanimator.FrameAnimatorListener;
import com.nokia.mid.ui.gestures.GestureEvent;
import com.nokia.mid.ui.gestures.GestureInteractiveZone;
import com.nokia.mid.ui.gestures.GestureListener;
import com.nokia.mid.ui.gestures.GestureRegistrationManager;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * The main canvas for displaying RSS Feed list and item details
 *
 * @author ssaa
 */
//#ifdef Profile
//# public final class RSSReaderCanvas extends Canvas {
//#else    
public final class RSSReaderCanvas extends Canvas implements GestureListener, FrameAnimatorListener {
//#endif

    private final RSSReader rssReader;
//#ifndef Profile
    private final FrameAnimator animator;
//#endif
    private final RSSListView listView;
    private final DetailsView detailsView;
    private View currentView;
    public static final Font FONT_TITLE = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
    public static final Font FONT_DESCRIPTION = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    public static final Font FONT_DATE = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    public static final int MARGIN = FONT_TITLE.getHeight() / 2;

    /**
     * Constructor for RSSReaderCanvas
     *
     * @param rssReader
     */
    public RSSReaderCanvas(RSSReader rssReader) {
        super();
        this.rssReader = rssReader;

        //listView = new VerticalListView(this);
        listView = new IconListView(this);
        detailsView = new DetailsView(this);

//#ifndef Profile        
        //register frameanimator with default values
        animator = new FrameAnimator();
        short pps = 0;
        short fps = 0;
        animator.register(0, 0, fps, pps, this);

        //register for gesturevents
        final GestureInteractiveZone giz;
        GestureInteractiveZone tmp = null;
        try {
          tmp = new GestureInteractiveZone(
                GestureInteractiveZone.GESTURE_ALL);
        } catch (Exception e){
            L.e("Some gesture events not supported.", "", e);
             tmp = new GestureInteractiveZone(
                GestureInteractiveZone.GESTURE_DRAG | 
                GestureInteractiveZone.GESTURE_FLICK | 
                GestureInteractiveZone.GESTURE_TAP);
        } finally {
            giz = tmp;
        }
        GestureRegistrationManager.register(this, giz);
        GestureRegistrationManager.setListener(this, this);
//#endif

        try {
            Class.forName("com.futurice.s40rssreader.Orientator").newInstance();
        } catch (Throwable t) {
            //#debug
            L.e("Orientation changes not supported", "", t);
        }
        setCurrentView(listView);
    }

    public RSSReader getRssReader() {
        return rssReader;
    }

    public RSSListView getListView() {
        return listView;
    }

    public DetailsView getDetailsView() {
        return detailsView;
    }

    /**
     * Paints the content of the current view
     *
     * @param g
     */
    public void paint(final Graphics g) {
        currentView.render(g, getWidth() - View.SCROLL_BAR_WIDTH, getHeight());
    }

    /**
     * Shows the list view
     */
    public void showList() {
//#ifndef Profile
        animator.stop();
//#endif        
//        detailsView.getCurrentItem().setThumbnailImage(null);
        detailsView.hide();
//        iconListView.rssModel.itemNextTo(selectedItem, true), iconListView.rssModel.itemNextTo(selectedItem, false), 0);
        setCurrentView(listView);
    }

    /**
     * Show the details view
     *
     * @param selectedItem
     */
    public void showDetails(final RSSItem selectedItem, final int x) {
        if (selectedItem == null) {
            //#debug
            L.i("Show details on null item", "Selected item has been cleared on another thread");
            return;
        }
//#ifndef Profile
        animator.stop();
//#endif        
        detailsView.setCurrentItem(selectedItem, listView.rssModel.itemNextTo(selectedItem, true), listView.rssModel.itemNextTo(selectedItem, false), x);
        setCurrentView(detailsView);
    }

//#ifndef Profile    
    /**
     * @see GestureListener.gestureAction(Object o, GestureInteractiveZone giz,
     * GestureEvent ge)
     */
    public void gestureAction(Object o, GestureInteractiveZone giz, GestureEvent ge) {
        switch (ge.getType()) {
            case GestureInteractiveZone.GESTURE_DRAG:
                if (currentView == listView) {
                    listView.setSelectedIndex(-1);
                }
                currentView.setRenderY(currentView.getRenderY() + ge.getDragDistanceY());
                animator.drag(0, currentView.getRenderY() + ge.getDragDistanceY());
                break;
            case GestureInteractiveZone.GESTURE_DROP:
                //no-op
                break;
            case GestureInteractiveZone.GESTURE_FLICK:
                boolean scroll = true;
                if (currentView == listView) {
                    listView.setSelectedIndex(-1);
                } else if (currentView == detailsView) {
                    scroll = !detailsView.horizontalFlick(ge.getFlickDirection());
                }
                if (scroll) {
                    animator.kineticScroll(ge.getFlickSpeed(), FrameAnimator.FRAME_ANIMATOR_FREE_ANGLE, FrameAnimator.FRAME_ANIMATOR_FRICTION_LOW, ge.getFlickDirection());
                }
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
//#endif

    protected void pointerPressed(int x, int y) {
        //just paints the highlight on the selected item
        listView.selectItem(x, y, false);
    }

    protected void pointerReleased(int x, int y) {
        //just paints the highlight on the selected item
        listView.deselectItem();
        //#ifdef Profile
//#         if (currentView == listView) {
//#             //selects the tapped item
//#             listView.selectItem(x, y, true);
//#         }
        //#endif
    }

//    public void sizeChanged(final int widht, final int height) {
//        verticalListView.canvasSizeChanged();
//    }
//#ifndef Profile    
    /*
     * @see FrameAnimatorListener.animate(FrameAnimator fa, int x, int y, short
     * delta, short deltaX, short deltaY, boolean lastFrame)
     */
    public void animate(FrameAnimator fa, int x, int y, short delta, short deltaX, short deltaY, boolean lastFrame) {
        currentView.setRenderY(y);
        repaint();
    }
//#endif

    public void setCurrentView(final View nextView) {
        if (this.currentView != null) {
            for (int i = 0; i < this.currentView.getCommands().length; i++) {
                removeCommand(this.currentView.getCommands()[i]);
            }
        }
        this.currentView = nextView;
        for (int i = 0; i < currentView.getCommands().length; i++) {
            addCommand(currentView.getCommands()[i]);
        }
        setCommandListener(currentView);
        repaint();
    }

    public boolean isPortrait() {
        return getWidth() <= 240;
    }
}
