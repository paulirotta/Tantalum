package com.nokia.example.picasa.s40;

import com.futurice.tantalum3.Task;
import com.futurice.tantalum3.log.L;
import com.futurice.tantalum3.util.StringUtils;
import com.nokia.example.picasa.common.PicasaStorage;
import com.nokia.mid.ui.LCDUIUtil;
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
    private int bottom = 0;

    public DetailedCanvas(final PicasaViewer midlet) {
        super(midlet);

        width = getWidth();
        this.setFullScreenMode(true);
    }

    public void paint(final Graphics g) {
        checkScroll();
        g.setColor(0x000000);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(0xffffff);

        if (PicasaStorage.selectedImage != null) {

            // If we do not have the image and we are not loading it, start loading it.
            if (image == null && !loading) {
                loading = true;
                PicasaStorage.imageCache.get(PicasaStorage.selectedImage.imageUrl, new Task() {
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
                StringUtils.splitToLines(titleLines, PicasaStorage.selectedImage.title, Font.getDefaultFont(), width - 2 * PADDING);
            } else {
                for (int i = 0; i < titleLines.size(); i++) {
                    g.drawString((String) titleLines.elementAt(i), PADDING, textY, Graphics.LEFT | Graphics.TOP);
                    textY = textY + Font.getDefaultFont().getHeight();
                }
                if (-textY < bottom) {
                    bottom = -textY;
                }
            }

            g.drawString(PicasaStorage.selectedImage.author, PADDING, textY, Graphics.LEFT | Graphics.TOP);
        }
        drawBackIcon(g);
    }

    public void showNotify() {
        XC = getWidth() / 2;
 
        // Show statusbar
        try {
            LCDUIUtil.setObjectTrait(this, "nokia.ui.canvas.status_zone", Boolean.TRUE);
        } catch (Exception e) {
            L.i("showNotify LCDUIUtil", "trait not supported, normal before SDK 2.0");
        }

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
}
