/*
 Copyright (c) 2013 Nokia Corporation. All rights reserved.
 Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation.
 Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 affiliates. Other product and company names mentioned herein may be trademarks
 or trade names of their respective owners.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
package org.tantalum.canvasrssreader;

import org.tantalum.Task;
import org.tantalum.util.L;
import org.tantalum.net.StaticWebCache;
import org.tantalum.net.xml.RSSItem;
import org.tantalum.jme.JMEImageUtils;
import java.util.Hashtable;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import org.tantalum.PlatformUtils;
import org.tantalum.storage.FlashDatabaseException;

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

    public IconListView(final RSSReaderCanvas canvas) throws FlashDatabaseException {
        super(canvas);

//        try {
//            updateCommand = (Command) Class.forName("org.tantalum.canvasrssreader.UpdateIconCommand").newInstance();
//            iconSupport = true;
//        } catch (Throwable t) {
//            L.e("IconCommand not supported", "Update", t);
//        }
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
            PlatformUtils.getInstance().shutdown(false, "Exit command received");
        } else if (command == updateCommand) {
            reloadAsync(true);
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
            //#debug
            L.i(this, "render", "modelLength=" + modelCopy.length);
            if (modelCopy.length == 0) {
//                if (iconSupport && !animationRunning) {
//                    ((UpdateIconCommand) updateCommand).startAnimation();
//                }
                g.setColor(RSSReader.COLOR_BACKGROUND);
                g.fillRect(0, 0, width, height);
                g.setColor(RSSReader.COLOR_FOREGROUND);
                g.drawString("Loading...", canvas.getWidth() >> 1, canvas.getHeight() >> 1, Graphics.BASELINE | Graphics.HCENTER);
                return;
//            } else if (iconSupport) {
//                ((UpdateIconCommand) updateCommand).stopAnimation();
//                animationRunning = false;
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
            synchronized (MUTEX) {
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
                                    canvas.refresh();
                                }
                            }
                        } else if (!item.isLoadingImage()) {
                            // Shrunken image not available in RAM cache, getAsync and create it
                            item.setLoadingImage(true);
                            if (item.getThumbnail() == null || item.getThumbnail().length() == 0) {
                                //#debug
                                L.i("Trivial thumbnail link in RSS feed", item.getTitle());
                            } else {
                                canvas.imageCache.getAsync(item.getThumbnail(),
                                        Task.HIGH_PRIORITY,
                                        StaticWebCache.GET_ANYWHERE,
                                        new Task(Task.FASTLANE_PRIORITY) {
                                    public Object exec(final Object o) {
                                        try {
                                            //#debug
                                            L.i(this, "getIcon result received", "" + this);
                                            item.setLoadingImage(false);
                                            Image icon = (Image) o;
                                            final int w = icon.getWidth();
                                            final int h = icon.getHeight();
                                            synchronized (Task.LARGE_MEMORY_MUTEX) {
                                                if (data == null || data.length < w * h) {
                                                    data = new int[w * h];
                                                }
                                                icon.getRGB(data, 0, w, 0, 0, w, h);
                                                icon = JMEImageUtils.scaleImage(data, data, w, h, 72, h, true, JMEImageUtils.FIVE_POINT_BLEND);
                                            }
                                            if (item.isNewItem()) {
                                                item.setNewItem(false);
                                                icons.put(item, new AnimatedImage(icon, 10, icon.getHeight(), icon.getWidth(), icon.getHeight(), 10));
                                            } else {
                                                icons.put(item, icon);
                                            }
                                            canvas.refresh();
                                        } catch (Exception e) {
                                            //#debug
                                            L.e(this, "Problem with getIcon setValue", item.getThumbnail(), e);
                                            cancel(false, "Problem with getIcon: " + item, e);
                                        }

                                        return null;
                                    }

                                    protected void onCanceled(String reason) {
                                        item.setLoadingImage(false);
                                    }
                                }.setClassName("GetIcon"));
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
            }
            renderScrollBar(g, totalHeight);
        } catch (Exception e) {
            //#debug
            L.e("IconList Render error", modelCopy.toString(), e);
        }
    }

    protected void clearCache() {
        super.clearCache();
        synchronized (MUTEX) {
            icons.clear();
        }
    }

    public void deselectItem() {
        if (setSelectedIndex(-1)) {
            canvas.refresh();
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
                canvas.refresh();
            }
        }
    }

    public String toString() {
        synchronized (MUTEX) {
            return "IconList" + super.toString() + " iconsSize=" + icons.size();
        }
    }
}
