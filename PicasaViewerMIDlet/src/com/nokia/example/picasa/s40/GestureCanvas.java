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

    protected static final int SPIN_SPEED = 100; // ms per animation frame
    protected static Image backIcon;
    public static final Timer spinTimer = new Timer();
    private static TimerTask spinTimerTask = null; // Access within synchronized blocks only
    protected int friction = GestureHandler.FRAME_ANIMATOR_FRICTION_LOW;
    protected final PicasaViewer midlet;
    protected GestureHandler gestureHandler = null;
    protected int scrollY = 0;
    protected int top = -getHeight();
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
        boolean gestureHandled = false;
        
        if (animating) {
            animating = false;
            gestureHandler.stopAnimation();
            gestureHandled = true;
        }
        
        gestureHandled |= stopSpin();
        
        return gestureHandled;
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
        animating = false;
        gestureHandler.stopAnimation();
        gestureHandler.unregister();
        stopSpin();

        super.hideNotify();
    }

    public void sizeChanged(final int w, final int h) {
        gestureHandler.updateCanvasSize();

        super.sizeChanged(w, h);
    }

    protected final synchronized void startSpin(final long delay) {
        if (spinTimerTask == null) {
            spinTimerTask = new TimerTask() {
                public void run() {
                    repaint();
                }
            };
            spinTimer.scheduleAtFixedRate(spinTimerTask, 0, delay);
        }
    }
    

    protected final synchronized boolean stopSpin() {
        boolean stopped = spinTimerTask != null;
        
        if (stopped) {
            spinTimerTask.cancel();
            spinTimerTask = null;
            repaint();
        }
        
        return stopped;
    }

    protected final synchronized boolean isSpinning() {
        return spinTimerTask != null;
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
}
