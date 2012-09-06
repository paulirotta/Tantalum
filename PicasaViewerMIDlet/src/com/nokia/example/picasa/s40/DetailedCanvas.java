package com.nokia.example.picasa.s40;

import com.futurice.tantalum3.Task;
import com.futurice.tantalum3.log.L;
import com.futurice.tantalum3.util.StringUtils;
import com.nokia.example.picasa.common.Storage;
import com.nokia.mid.ui.LCDUIUtil;
import java.io.IOException;
import java.util.Vector;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * The class displaying the larger image, title and photographer.
 *
 */
public final class DetailedCanvas extends GestureCanvas {

    private static final int PADDING = 10;
    private static final int SPIN_SPEED = 100; // ms per animation frame
    private long spinAnimationStartTime = System.currentTimeMillis();
    private volatile Image image;
    private Image backIcon;
    private static final int iconSide = 43;
    private int startDot;
    private double angle;
    private static final double R = 12;
    private final double YC = 50.0;
    private double XC;
    private static final int[] shades = {0x000000, 0xffffff, 0xdddddd, 0xbbbbbb, 0x999999, 0x777777, 0x333333};
    private static final int dots = shades.length;
    private static final double step = (2 * Math.PI) / dots;
    private static final double circle = (2 * Math.PI);
    private boolean loading = false;
    private final int width;
    private Vector titleLines;
    private int textY = 0;
//    private final int height;
    private int bottom = 0;

    public DetailedCanvas(final PicasaViewer midlet) {
        super(midlet);

        width = getWidth();
//        height = getHeight();

        try {
            backIcon = Image.createImage("/back.png");
        } catch (IOException e) {
            //#debug
            L.e("Can not create back icon", null, e);
        }
    }

    public void paint(final Graphics g) {
        g.setColor(0x000000);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(0xffffff);

        if (Storage.selectedImage != null) {

            // If we do not have the image and we are not loading it, start loading it.
            if (image == null && !loading) {
                loading = true;
                Storage.imageCache.get(Storage.selectedImage.getImageUrl(), new Task() {
                    public void set(Object o) {
                        if (o != null) {
                            image = (Image) o;
                            stopSpin();
                            repaint();
                        }
                        super.set(o);
                    }
                });
                startSpin(SPIN_SPEED);
            }
            if (image == null) {
                drawSpinner(g); // Loading...
            } else {
                // Done, draw image.
                loading = false;
                g.drawImage(image, getWidth() / 2, scrollY, Graphics.TOP | Graphics.HCENTER);
            }
            textY = image == null ? (int) YC + ((int) R) << 1 : image.getHeight() + scrollY;
            g.setColor(0xFFFFFF);

            if (titleLines == null) {
                titleLines = new Vector();
                StringUtils.splitToLines(titleLines, Storage.selectedImage.getTitle(), Font.getDefaultFont(), width - 2 * PADDING);
            } else {
                for (int i = 0; i < titleLines.size(); i++) {
                    g.drawString((String) titleLines.elementAt(i), PADDING, textY, Graphics.LEFT | Graphics.TOP);
                    textY = textY + Font.getDefaultFont().getHeight();
                }
                if (-textY < bottom) {
                    bottom = -textY;
                }
            }

            g.drawString(Storage.selectedImage.getAuthor(), PADDING, textY, Graphics.LEFT | Graphics.TOP);
        }
        if (backIcon != null) {
            g.drawImage(backIcon, getWidth(), getHeight(), Graphics.BOTTOM | Graphics.RIGHT);
        }
    }

    public void showNotify() {
        XC = getWidth() / 2;
        this.setFullScreenMode(true);

        // Show statusbar
        LCDUIUtil.setObjectTrait(this, "nokia.ui.canvas.status_zone", Boolean.TRUE);

        super.showNotify();
    }

    public void hideNotify() {
        image = null; //Set to null to free up memory
        titleLines = null;

        super.hideNotify();
    }

    public void pointerPressed(int x, int y) {
        if (x >= getWidth() - iconSide && y >= getHeight() - iconSide) {
            midlet.goBack();
        }
    }

    /**
     * Draw loading animation.
     */
    public void drawSpinner(final Graphics g) {
        startDot = ((int) (System.currentTimeMillis() - spinAnimationStartTime) / SPIN_SPEED) % dots;
        for (int i = 0; i < dots; i++) {
            int x = (int) (XC + R * Math.cos(angle));
            int y = (int) (YC + R * Math.sin(angle));
            g.setColor(shades[(i + startDot) % dots]);
            g.fillRoundRect(x, y, 6, 6, 3, 3);
            angle = (angle - step) % circle;
        }

        startDot++;
        startDot = startDot % dots;
    }

//    public void gestureFlick(int startX, int startY, float flickDirection, int flickSpeed, int flickSpeedX, int flickSpeedY) {
//        gestureHandler.kineticScroll(flickSpeed, GestureHandler.FRAME_ANIMATOR_VERTICAL, friction, flickDirection);
//    }
//
//    public void animate(int x, int y, short delta, short deltaX, short deltaY, boolean lastFrame) {
//        if (-bottom > height) {
//            if (scrollY + deltaY > 0) {
//                scrollY = 0;
//            } else if (scrollY + deltaY < bottom >> 1) {
//                scrollY = bottom >> 1;
//            } else {
//                scrollY += deltaY;
//            }
//        }
//        repaint();
//    }
}
