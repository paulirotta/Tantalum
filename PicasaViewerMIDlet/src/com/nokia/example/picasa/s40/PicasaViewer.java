package com.nokia.example.picasa.s40;

import com.futurice.tantalum3.PlatformUtils;
import com.futurice.tantalum3.TantalumMIDlet;
import com.futurice.tantalum3.Task;
import com.futurice.tantalum3.Worker;
import com.futurice.tantalum3.log.L;
import com.nokia.example.picasa.common.PicasaStorage;
import javax.microedition.lcdui.*;

public final class PicasaViewer extends TantalumMIDlet implements CommandListener {

    FeaturedCanvas featuredView;
    SearchCanvas searchView;
    private DetailedCanvas detailedView;
    private Displayable lastView;
    private CategoryBarHandler categoryBarHandler = null;
    private Command featuredCommand = new Command("Home", Command.SCREEN, 0);
    private Command searchCommand = new Command("Search", Command.SCREEN, 1);
    private Command backCommand = new Command("Back", Command.BACK, 0);
    private Command backCommand2 = new Command("Back", Command.BACK, 0);
    private Command exitCommand = new Command("Exit", Command.EXIT, 0);
    private Command refreshCommand = new Command("Refresh", Command.OK, 0);
    private final Task otherViewInitTask = new Task() {
        /**
         * Finish other view initialization on other threads
         */
        public Object doInBackground(final Object in) {
            detailedView = new DetailedCanvas(PicasaViewer.this);
            searchView = new SearchCanvas(PicasaViewer.this);

            return in;
        }
    };

    public PicasaViewer() {
        super(4);
    }

    public void startApp() {
        featuredView = new FeaturedCanvas(this);
        Worker.fork(otherViewInitTask);
        PicasaStorage.init(featuredView.getWidth()); // Initialize storage with display width.
        featuredView.loadFeed(false, false);
        lastView = featuredView;
        Display.getDisplay(this).setCurrent(featuredView);
        PlatformUtils.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    final Class cbc = Class.forName("com.nokia.example.picasa.s40.CategoryBarHandler");
                    CategoryBarHandler.setMidlet(PicasaViewer.this);
                    categoryBarHandler = (CategoryBarHandler) cbc.newInstance();
                    refreshCommand = categoryBarHandler.getUpdateIconCommand();
                } catch (Throwable t) {
                    //#debug
                    L.e("Can not set category bar handler", "normal before SDK 2.0", t);
                }
                featuredView.addCommand(refreshCommand);
                searchView.setCommandListener(PicasaViewer.this);
                if (categoryBarHandler == null) {
                    try {
                        /*
                         * Fallback when there is no category bar (Nokia SDK 1.1 and earlier)
                         */
                        featuredView.addCommand(searchCommand);
                        featuredView.addCommand(exitCommand);
                        featuredView.setCommandListener(PicasaViewer.this);
                        otherViewInitTask.join(5000); // Wait up to 5 seconds for views to initialize
                        detailedView.addCommand(backCommand);
                        detailedView.setCommandListener(PicasaViewer.this);
                        searchView.addCommand(featuredCommand);
                        searchView.setCommandListener(PicasaViewer.this);
                        searchView.addCommand(backCommand2);
                    } catch (Exception ex) {
                        //#debug
                        L.e("Can not fallback to command init on other views", "", ex);
                    }
                }
            }
        });
    }

    public void setDetailed() {
        if (phoneSupportsCategoryBar()) {
            categoryBarHandler.setVisibility(false);
        }
        lastView = Display.getDisplay(this).getCurrent();

        Display.getDisplay(this).setCurrent(detailedView);
    }

    public void goBack() {
        if (phoneSupportsCategoryBar()) {
            categoryBarHandler.goBack();
        }
        Display.getDisplay(this).setCurrent(lastView);
    }

    public void stopReloadAnimation() {
        try {
            ((UpdateIconCommand) refreshCommand).stopAnimation();
        } catch (Throwable t) {
        }
    }

    public void startReloadAnimation() {
        try {
            ((UpdateIconCommand) refreshCommand).startAnimation();
        } catch (Throwable t) {
        }
    }

    public void commandAction(Command c, Displayable d) {
        //#debug
        L.i("Command action", "Command " + c);
        if (c.getCommandType() == Command.BACK) {
            goBack();
        } else if (c == refreshCommand) {
            featuredView.refresh();
        } else if (c == featuredCommand) {
            Display.getDisplay(this).setCurrent(featuredView);
        } else if (c == searchCommand) {
            Display.getDisplay(this).setCurrent(searchView);
        } else if (c.getCommandType() == Command.EXIT) {
            Worker.shutdown(false);
        }
    }

    public boolean phoneSupportsCategoryBar() {
        return categoryBarHandler != null;
    }
}
