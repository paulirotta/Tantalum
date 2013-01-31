/*
 * Copyright © 2012 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */
package com.nokia.s40.picasaviewerapp;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import org.tantalum.util.L;

/**
 * A helper for creating gesture-based UIs that also work on older S40 phones
 *
 * @author phou
 */
public abstract class GestureCanvas extends Canvas implements CommandListener {

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
    private int startDot;
    private double angle;
    private static final double R = 12;
    private double YC;
    private double XC;
    private static final int[] shades = {0x000000, 0xffffff, 0xdddddd, 0xbbbbbb, 0x999999, 0x777777, 0x333333};
    private static final int dots = shades.length;
    private static final double step = (2 * Math.PI) / dots;
    private static final double circle = (2 * Math.PI);

    static {
        try {
            backIcon = Image.createImage("/s40/back.png");
        } catch (IOException e) {
            //#debug
            L.e("Can not create back icon", null, e);
        }
    }

    public GestureCanvas(final PicasaViewer midlet) {
        this.midlet = midlet;
        this.setCommandListener(this);
        try {
            gestureHandler = (GestureHandler) Class.forName("com.nokia.s40.picasaviewerapp.GestureHandler").newInstance();
            gestureHandler.setCanvas(this);
            gestureHandler.register(0, scrollY);
        } catch (Throwable ex) {
            //#debug
            L.e("GestureHandler", "can not instantiate", ex);
        }
        this.setTitle("GestureCanvas");
        XC = getWidth() / 2;
        YC = getHeight() / 2;
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
//            gestureHandler.stopAnimation();
            gestureHandled = true;
        }
        
        //gestureHandled |= stopSpinner();
        
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

//    public void showNotify() {
//        gestureHandler.register(0, scrollY);
//    }

    public void hideNotify() {
        animating = false;
        stopSpinner();
//        gestureHandler.unregister();
    }

    public void sizeChanged(final int w, final int h) {
        gestureHandler.updateCanvasSize();
        XC = w / 2;
        YC = h / 2;
    }

    protected final synchronized void startSpinner() {
        if (spinTimerTask == null) {
            spinTimerTask = new TimerTask() {
                public void run() {
                    repaint();
                }
            };
            spinTimer.scheduleAtFixedRate(spinTimerTask, SPIN_SPEED, SPIN_SPEED);
        }
    }

    protected final synchronized boolean stopSpinner() {
        boolean stopped = spinTimerTask != null;
        
        if (stopped) {
            spinTimerTask.cancel();
            spinTimerTask = null;
        }
        repaint();
        
        return stopped;
    }

    protected final synchronized boolean isSpinning() {
        return spinTimerTask != null;
    }

    protected void drawSpinner(final Graphics g) {
        if (isSpinning()) {
            // Draw loading animation
            for (int i = 0; i < dots; i++) {
                int x = (int) (XC + R * Math.cos(angle));
                int y = (int) (YC + R * Math.sin(angle));
                g.setColor(shades[(i + startDot) % dots]);
                g.fillRoundRect(x, y, 6, 6, 3, 3);
                angle = (angle - step) % circle;
            }
            startDot = ++startDot % dots;
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
}
