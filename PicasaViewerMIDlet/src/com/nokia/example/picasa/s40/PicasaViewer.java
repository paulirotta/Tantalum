package com.nokia.example.picasa.s40;

import com.futurice.tantalum3.TantalumMIDlet;
import com.futurice.tantalum3.Worker;
import com.futurice.tantalum3.log.L;
import com.futurice.tantalum3.net.StaticWebCache;
import com.nokia.example.picasa.common.PicasaStorage;
import java.io.IOException;
import javax.microedition.lcdui.*;

public final class PicasaViewer extends TantalumMIDlet {

    FeaturedCanvas featuredView;
    SearchCanvas searchView;
    private DetailCanvas detailedView;
    private Displayable lastView;
    private CategoryBarHandler categoryBarHandler = null;

    public PicasaViewer() {
        super(4);
    }

    public void startApp() {
        try {
            categoryBarHandler = (CategoryBarHandler) Class.forName("com.nokia.example.picasa.s40.CategoryBarHandler").newInstance();
            categoryBarHandler.setMidlet(this);
        } catch (Throwable t) {
            //#debug
            L.i("Can not set category bar handler", "normal before SDK 2.0");
        }
        try {
            featuredView = new FeaturedCanvas(this);
        } catch (Exception ex) {
            //#debug
            L.e("Can not create FeaturedCanvas", null, ex);
            Worker.shutdown(true);
        }
        PicasaStorage.init(featuredView.getWidth()); // Initialize storage with display width.
        try {
            featuredView.loadFeed(null, StaticWebCache.GET_ANYWHERE).join(2000);
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
        if (phoneSupportsCategoryBar()) {
            categoryBarHandler.setVisibility(false);
        }
        lastView = Display.getDisplay(PicasaViewer.this).getCurrent();
        Display.getDisplay(PicasaViewer.this).setCurrent(detailedView);
    }

    public void goBack() {
        if (phoneSupportsCategoryBar()) {
            categoryBarHandler.setVisibility(true);
        }
        Display.getDisplay(this).setCurrent(lastView);
    }

    public void goSearchCanvas() {
        lastView = searchView;
        Display.getDisplay(this).setCurrent(searchView);
    }

    public void goFeaturedCanvas() {
        lastView = featuredView;
        Display.getDisplay(this).setCurrent(featuredView);
    }

    public boolean phoneSupportsCategoryBar() {
        return categoryBarHandler != null;
    }

    public void setCategoryBarVisibility(final boolean visibility) {
        if (categoryBarHandler != null) {
            categoryBarHandler.setVisibility(visibility);
        }
    }
}
