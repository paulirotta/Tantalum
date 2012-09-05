/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.s40rssreader;

import com.futurice.tantalum3.Task;
import com.futurice.tantalum3.Worker;
import com.futurice.tantalum3.log.L;
import com.futurice.tantalum3.net.xml.RSSItem;
import com.futurice.tantalum3.util.ImageUtils;
import java.util.Hashtable;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * A scrollable grid if pictures from the news feed. Click to open an article.
 * Press and hold to preview.
 *
 * @author phou
 */
public final class IconListView extends RSSListView {

    private static final int ROW_HEIGHT = 40 + 2 * RSSReaderCanvas.MARGIN;
    private final Command exitCommand = new Command("Exit", Command.EXIT, 0);
    private Command updateCommand = new Command("Update", Command.OK, 0);
    private final Command clearCacheCommand = new Command("Clear Cache", Command.SCREEN, 5);
    private final Command prefetchImagesCommand = new Command("Prefetch Images", Command.SCREEN, 2);
    private int selectedIndex = -1;
    public int numberOfColumns = 3;
    private final Hashtable icons = new Hashtable();
    private int columnWidth = 100;
    private static int[] data = null;
    private RSSItem[] modelCopy = null;
    private boolean animationRunning = false;
    private static boolean iconSupport = false;

    public IconListView(final RSSReaderCanvas canvas) {
        super(canvas);

        try {
            updateCommand = (Command) Class.forName("com.futurice.s40rssreader.UpdateIconCommand").newInstance();
            iconSupport = true;
        } catch (Throwable t) {
            L.e("IconCommand not supported", "Update", t);
        }
    }

    public Command[] getCommands() {
//#ifdef Release
//#         return new Command[]{updateCommand, exitCommand};
//#else
        return new Command[]{updateCommand, exitCommand, clearCacheCommand, prefetchImagesCommand};
//#endif        
    }

    public void commandAction(final Command command, final Displayable d) {
        if (command == exitCommand) {
            canvas.getRssReader().exitMIDlet(false);
        } else if (command == updateCommand) {
            reload(true);
        } else if (command == clearCacheCommand) {
            clearCache();
        } else if (command == prefetchImagesCommand) {
            prefetchImages = true;
            clearCache();
        }
    }

    /**
     * Renders the list of rss feed items as columns of icons
     *
     * @param g
     */
    public void render(final Graphics g, final int width, final int height) {
        try {
            numberOfColumns = canvas.isPortrait() ? 3 : 4;
            modelCopy = rssModel.copy(modelCopy);
            if (modelCopy.length == 0) {
                if (iconSupport && !animationRunning){
                    ((UpdateIconCommand) updateCommand).startAnimation();
                }
                g.setColor(RSSReader.COLOR_BACKGROUND);
                g.fillRect(0, 0, width, height);
                g.setColor(RSSReader.COLOR_FOREGROUND);
                g.drawString("Loading...", canvas.getWidth() >> 1, canvas.getHeight() >> 1, Graphics.BASELINE | Graphics.HCENTER);
                return;
            } else if (iconSupport){
             ((UpdateIconCommand) updateCommand).stopAnimation();
             animationRunning = false;
            }

            final int totalHeight = modelCopy.length * ROW_HEIGHT / numberOfColumns;
            columnWidth = width / numberOfColumns;

            //Limit the renderY not to keep the content on the screen
            if (totalHeight < height) {
                this.renderY = 0;
            } else if (this.renderY < -totalHeight + height) {
                this.renderY = -totalHeight + height;
            } else if (this.renderY > 0) {
                this.renderY = 0;
            }

            int curY = this.renderY;

            //start rendeing from the first visible item
            for (int i = 0; i < modelCopy.length; i++) {
                final int column = i % numberOfColumns;
                final RSSItem item = modelCopy[i];

                if (curY > -(ROW_HEIGHT * 3) && curY <= height + ROW_HEIGHT * 2) {
                    final boolean visible = curY > -ROW_HEIGHT && curY <= height;
                    final Object icon = icons.get(item);

                    if (visible) {
                        g.setColor(i == this.selectedIndex ? RSSReader.COLOR_HIGHLIGHTED_BACKGROUND : RSSReader.COLOR_BACKGROUND);
                        g.fillRect(columnWidth * column, curY, columnWidth, ROW_HEIGHT);
                    }
                    if (icon != null) {
                        if (visible) {
                            final int x = columnWidth * column + columnWidth / 2;
                            final int y = curY + ROW_HEIGHT / 2;
                            if (icon instanceof Image) {
                                g.drawImage((Image) icon, x, y, Graphics.HCENTER | Graphics.VCENTER);
                            } else {
                                if (((AnimatedImage) icon).animate(g, x, y)) {
                                    // End animation
                                    icons.put(item, ((AnimatedImage) icon).image);
                                }
                                canvas.queueRepaint();
                            }
                        }
                    } else if (!item.isLoadingImage()) {
                        // Shrunken image not available in RAM cache, get and create it
                        item.setLoadingImage(true);
                        if (item.getThumbnail() == null || item.getThumbnail().length() == 0) {
                            //#debug
                            L.i("Trivial thumbnail link in RSS feed", item.getTitle());
                        } else {
                            DetailsView.imageCache.get(item.getThumbnail(), new Task() {

                                public void set(final Object o) {
                                    super.set(o);
                                    try {
                                        //#debug
                                        L.i("getIcon result", "" + o);
                                        item.setLoadingImage(false);
                                        Image icon = (Image) o;
                                        final int w = icon.getWidth();
                                        final int h = icon.getHeight();
                                        synchronized (Worker.LARGE_MEMORY_MUTEX) {
                                            if (data == null || data.length < w * h) {
                                                data = new int[w * h];
                                            }
                                            icon.getRGB(data, 0, w, 0, 0, w, h);
                                            icon = null;
                                            icon = ImageUtils.downscaleImage(data, w, h, 72, h, true, false, false);
                                        }
                                        if (item.isNewItem()) {
                                            item.setNewItem(false);
                                            icons.put(item, new AnimatedImage(icon, 10, icon.getHeight(), icon.getWidth(), icon.getHeight(), 10));
                                        } else {
                                            icons.put(item, icon);
                                        }
                                        canvas.queueRepaint();
                                    } catch (Exception e) {
                                        //#debug
                                        L.e("Problem with getIcon setResult", item.getThumbnail(), e);
                                        cancel(false);
                                    }
                                }

                                public boolean cancel(boolean mayInterruptIfNeeded) {
                                    item.setLoadingImage(false);
                                    
                                    return super.cancel(mayInterruptIfNeeded);
                                }
                            });
                        }
                    }
                } else {
                    // Remove icons currently off screen
                    icons.remove(item);
                }
                if (column == numberOfColumns - 1) {
                    curY += ROW_HEIGHT;
                }
            }
            renderScrollBar(g, totalHeight);
        } catch (Exception e) {
            //#debug
            L.e("IconList Render error", modelCopy.toString(), e);
        }
    }

    protected void doClearCache() {
        super.doClearCache();

        icons.clear();
    }

    public void deselectItem() {
        if (setSelectedIndex(-1)) {
            canvas.queueRepaint();
        }
    }

    public boolean setSelectedIndex(final int newIndex) {
        if (selectedIndex == newIndex) {
            return false;
        }
        selectedIndex = newIndex;

        return true;
    }

    /**
     * Selects item at the specified x- and y-position (if any). If tapped makes
     * the selection, otherwise just repaints the highlighted item.
     *
     * @param x
     * @param y
     * @param tapped
     */
    public void selectItem(final int x, final int y, boolean tapped) {
        final int row = (y - renderY) / ROW_HEIGHT;
        final int pointedIndex = x / columnWidth + row * numberOfColumns;

        if (pointedIndex >= 0 && pointedIndex < rssModel.size()) {
            setSelectedIndex(pointedIndex);
            if (tapped) {
                canvas.showDetails((RSSItem) rssModel.elementAt(this.selectedIndex), 0);
            } else {
                canvas.queueRepaint();
            }
        }
    }
}
