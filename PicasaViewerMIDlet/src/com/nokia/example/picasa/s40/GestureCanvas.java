/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.example.picasa.s40;

import com.futurice.tantalum3.log.L;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * A helper for creating gesture-based UIs that also work on older S40 phones
 *
 * @author phou
 */
public abstract class GestureCanvas extends Canvas {

    protected static Image backIcon;
    private static final Timer spinTimer = new Timer();
    private static TimerTask spinTimerTask = null; // Access within synchronized blocks only
    private long spinTimerDelay;
    protected int friction = GestureHandler.FRAME_ANIMATOR_FRICTION_LOW;
    protected final PicasaViewer midlet;
    protected GestureHandler gestureHandler = null;
    protected int scrollY = 0;
    protected int top = getHeight();
    protected boolean animating = false;

    static {
        try {
            backIcon = Image.createImage("/back.png");
        } catch (IOException e) {
            //#debug
            L.e("Can not create back icon", null, e);
        }
    }

    public GestureCanvas(final PicasaViewer midlet) {
        this.midlet = midlet;
        try {
            gestureHandler = (GestureHandler) Class.forName("com.nokia.example.picasa.s40.GestureHandler").newInstance();
            gestureHandler.setCanvas(this);
        } catch (Throwable ex) {
            //#debug
            L.e("GestureHandler", "can not instantiate", ex);
        }
        this.setTitle("GestureCanvas");
    }

    public int getScrollY() {
        return scrollY;
    }

    protected void checkScroll() {
        if (scrollY < top) {
            //#debug
            L.i("checkScroll", "bang top scrollY=" + scrollY + " top=" + top);
            scrollY = top;
        } else if (scrollY > 0) {
            //#debug
            L.i("checkScroll", "bang bottom scrollY=" + scrollY);
            scrollY = 0;
        }        
    }
    
    /**
     * Two fingers, moving in or out
     *
     * @param pinchDistanceStartingp
     * @param pinchDistanceCurrent
     * @param pinchDistanceChange
     * @param centerX
     * @param centerY
     * @param centerChangeX
     * @param centerChangeY
     */
    public void gesturePinch(
            int pinchDistanceStarting,
            int pinchDistanceCurrent,
            int pinchDistanceChange,
            int centerX,
            int centerY,
            int centerChangeX,
            int centerChangeY) {
    }

    /**
     * Short press
     * 
     * Always call the super.gestureTap() in the hierarchy and accept if the
     * parent class has caught and consumed the tap event
     * 
     * @param startX
     * @param startY
     * @return true of the tap was caught, handled and consumed
     */
    public boolean gestureTap(int startX, int startY) {
        if (animating) {
            animating = false;
            gestureHandler.stopAnimation();
            return true;
        }
        
        return false;
    }

    /**
     * Hold
     *
     * @param startX
     * @param startY
     */
    public void gestureLongPress(int startX, int startY) {
    }

    /**
     * Hold, then hold again
     *
     * @param startX
     * @param startY
     */
    public void gestureLongPressRepeated(int startX, int startY) {
    }

    /**
     * The user is moving their finger and has not yet lifted it
     *
     * @param startX
     * @param startY
     * @param dragDistanceX
     * @param dragDistanceY
     */
    public void gestureDrag(int startX, int startY, int dragDistanceX, int dragDistanceY) {
        gestureHandler.animateDrag(startX + dragDistanceX, scrollY + dragDistanceY);
    }

    /**
     * The user finished a animateDrag motion by lifting their finger when not
     * moving
     *
     * @param startX
     * @param startY
     * @param dragDistanceX
     * @param dragDistanceY
     */
    public void gestureDrop(int startX, int startY, int dragDistanceX, int dragDistanceY) {
    }

    /**
     * The user's finger was still moving when the lifted it from the screen
     *
     * The default implementation does kinetic scrolling on both X and Y. You
     * can reduce the computational load and thus slightly increase the frame
     * rate by overriding this if you are only interested in animation on one
     * axis.
     *
     * @param startX
     * @param startY
     * @param flickDirection
     * @param flickSpeed
     * @param flickSpeedX
     * @param flickSpeedY
     */
    public void gestureFlick(int startX, int startY, float flickDirection, int flickSpeed, int flickSpeedX, int flickSpeedY) {
        animating = true;
        gestureHandler.kineticScroll(flickSpeed, GestureHandler.FRAME_ANIMATOR_FREE_ANGLE, friction, flickDirection);
    }

    public void showNotify() {
        gestureHandler.register(0, scrollY);

        super.showNotify();
    }

    public void hideNotify() {
        gestureHandler.stopAnimation();
        gestureHandler.unregister();

        super.hideNotify();
    }

    public void sizeChanged(final int w, final int h) {
        gestureHandler.updateCanvasSize();

        super.sizeChanged(w, h);
    }

    public synchronized void startSpin(final long delay) {
        if (spinTimerTask != null || delay != spinTimerDelay) {
            // Spin speed has changed
            stopSpin();
        }
        spinTimerDelay = delay;
        spinTimerTask = new TimerTask() {
            public void run() {
                repaint();
            }
        };
        spinTimer.scheduleAtFixedRate(spinTimerTask, delay, delay);
    }

    public synchronized void stopSpin() {
        if (spinTimerTask != null) {
            spinTimerTask.cancel();
            spinTimerTask = null;
        }
    }

    /**
     * Update by painting at the new scroll position
     *
     * @param x
     * @param y
     * @param delta
     * @param deltaX
     * @param deltaY
     * @param lastFrame
     */
    public void animate(int x, int y, short delta, short deltaX, short deltaY, boolean lastFrame) {
        //#debug
        L.i("animate", "y=" + y + " deltaY=" + deltaY + " top=" + top + " scrollY=" + scrollY + " screenHeight=" + getHeight());
        scrollY = y;
        animating = !lastFrame;
        repaint();
    }

    protected void drawBackIcon(final Graphics g) {
        g.drawImage(backIcon, getWidth(), getHeight(), Graphics.BOTTOM | Graphics.RIGHT);
    }

    /**
     * Adding this method, even if it does nothing, tells some Series40 phones to
     * not display a button in the top right corner. This button is not needed in
     * this application, but it would allow the user to
     * manually display the virtual keyboard. It is always better to automatically
     * display the virtual keyboard when the user presses an area of the screen,
     * or even better- when the user would need to enter input as the next logical
     * action, even if they have not pressed the screen.
     * 
     * @param x
     * @param y 
     */
//    protected void pointerPressed(int x, int y) {
//    }
//
//    /**
//     * See the comments for pointerPressed()
//     * 
//     * @param x
//     * @param y 
//     */
//    protected void pointerReleased(int x, int y) {
//    }
//
//    /**
//     * See the comments for pointerPressed()
//     * 
//     * @param x
//     * @param y 
//     */
//    protected void pointerDragged(int x, int y) {
//    }
}
