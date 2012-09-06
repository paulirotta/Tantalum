package com.nokia.example.picasa.s40;

import com.futurice.tantalum3.TantalumMIDlet;
import com.futurice.tantalum3.Task;
import com.futurice.tantalum3.Worker;
import com.futurice.tantalum3.log.L;
import com.nokia.example.picasa.common.Storage;
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
    private final Task otherViewInitTask = new Task() {
        /**
         * Finish other view initialization on other threads
         */
        public void exec() {
            detailedView = new DetailedCanvas(PicasaViewer.this);
            searchView = new SearchCanvas(PicasaViewer.this);
        }
    };

    public PicasaViewer() {
        super(4);

        Worker.fork(otherViewInitTask);
        try {
            categoryBarHandler = new CategoryBarHandler(this);
        } catch (Throwable e) {
            try {
                /*
                 * Fallback when there is no category bar (Nokia SDK 1.1 and earlier)
                 */
                otherViewInitTask.join(5000); // Wait up to 5 seconds for views to initialize
                detailedView.addCommand(searchCommand);
                detailedView.addCommand(backCommand);
                detailedView.setCommandListener(this);
                searchView.addCommand(featuredCommand);
                searchView.setCommandListener(this);
                searchView.addCommand(backCommand2);
            } catch (Exception ex) {
                //#debug
                L.e("Can not fallback to command init on other views", "", e);
            }
        }
    }

    public void startApp() {
        featuredView = new FeaturedCanvas(this);
        Storage.init(featuredView.getWidth()); // Initialize storage with display width.
        featuredView.loadFeed(false, false);
        lastView = featuredView;
        Display.getDisplay(this).setCurrent(featuredView);
    }

    public void setDetailed() {
        if (categoryBarHandler != null) {
            categoryBarHandler.setVisibility(false);
        }
        lastView = Display.getDisplay(this).getCurrent();

        Display.getDisplay(this).setCurrent(detailedView);
    }

    public void goBack() {
        if (categoryBarHandler != null) {
            categoryBarHandler.goBack();
        }
        Display.getDisplay(this).setCurrent(lastView);
    }

    public void commandAction(Command c, Displayable d) {
        if (c.getCommandType() == Command.BACK) {
            goBack();
        } else if (c == featuredCommand) {
        } else if (c == searchCommand) {
        }
    }
}
