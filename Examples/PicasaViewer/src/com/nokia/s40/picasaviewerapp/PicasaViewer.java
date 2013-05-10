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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import org.tantalum.PlatformUtils;
import org.tantalum.net.StaticWebCache;
import org.tantalum.util.L;

import com.nokia.common.picasaviewerapp.PicasaStorage;
import javax.microedition.midlet.MIDlet;
import org.tantalum.jme.TantalumJME;


public final class PicasaViewer extends MIDlet {

    FeaturedCanvas featuredView;
    SearchCanvas searchView;
    private DetailCanvas detailedView;
    private Displayable lastView;
    private CategoryBarHandler categoryBarHandler = null;

    public void startApp() {
        TantalumJME.start(this);

        try {
            categoryBarHandler = (CategoryBarHandler) Class.forName("com.nokia.example.picasa.s40.CategoryBarHandler").newInstance();
            categoryBarHandler.setMidlet(this);
        } catch (Throwable t) {
            //#debug
            L.i("Can not set category bar handler", "normal for some phones");
        }
        
        try {
            featuredView = new FeaturedCanvas(this);
        } catch (Exception ex) {
            //#debug
            L.e("Can not create FeaturedCanvas", null, ex);
            PlatformUtils.getInstance().shutdown(false, "Can not create FeaturedCanvas: " + ex);
        }
        
        PicasaStorage.init(featuredView.getWidth()); // Initialize storage with display width.
        try {
            featuredView.loadFeed(null, StaticWebCache.GET_ANYWHERE).fork(); //.join(200);
        } catch (Exception ex) {
            //#debug
            L.e("Slow initial feed load", null, ex);
        }
        lastView = featuredView;
        Display.getDisplay(this).setCurrent(featuredView);

        // Initial display is on-screen, continue init tasks before releasing start thread
        detailedView = new DetailCanvas(PicasaViewer.this);
        searchView = new SearchCanvas(PicasaViewer.this);
    }

    public Command getRefreshIconCommand() throws IOException {
        return categoryBarHandler.getRefreshIconCommand();
    }

    public void goDetailCanvas() {
        lastView = Display.getDisplay(PicasaViewer.this).getCurrent();
        PlatformUtils.getInstance().runOnUiThread(new Runnable() {
            public void run() {
                if (phoneSupportsCategoryBar()) {
                    categoryBarHandler.setVisibility(false);
                }
                Display.getDisplay(PicasaViewer.this).setCurrent(detailedView);
            }
        });
    }

    public void goBack() {
        PlatformUtils.getInstance().runOnUiThread(new Runnable() {
            public void run() {
                if (phoneSupportsCategoryBar()) {
                    categoryBarHandler.setVisibility(true);
                }
                Display.getDisplay(PicasaViewer.this).setCurrent(lastView);
            }
        });
    }

    public void goSearchCanvas() {
        lastView = searchView;
        PlatformUtils.getInstance().runOnUiThread(new Runnable() {
            public void run() {
                Display.getDisplay(PicasaViewer.this).setCurrent(searchView);
            }
        });
    }

    public void goFeaturedCanvas() {
        lastView = featuredView;
        PlatformUtils.getInstance().runOnUiThread(new Runnable() {
            public void run() {
                Display.getDisplay(PicasaViewer.this).setCurrent(featuredView);
            }
        });
    }

    public boolean phoneSupportsCategoryBar() {
        return categoryBarHandler != null;
    }

    public void setCategoryBarVisibility(final boolean visibility) {
        if (categoryBarHandler != null) {
            categoryBarHandler.setVisibility(visibility);
        }
    }

    protected void pauseApp() {
    }

    protected void destroyApp(final boolean unconditional) {
        PlatformUtils.getInstance().shutdown(unconditional, "destroyApp(" + unconditional + ") received from phone");
    }
}
