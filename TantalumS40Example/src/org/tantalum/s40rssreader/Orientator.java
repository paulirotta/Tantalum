/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.s40rssreader;

import com.nokia.mid.ui.orientation.Orientation;
import com.nokia.mid.ui.orientation.OrientationListener;

/**
 *
 * @author phou
 */
public class Orientator implements OrientationListener {
    public Orientator() {
        Orientation.addOrientationListener(this);
    }

    public void displayOrientationChanged(final int newDisplayOrientation) {
        switch (newDisplayOrientation) {

            case Orientation.ORIENTATION_LANDSCAPE:
                Orientation.setAppOrientation(Orientation.ORIENTATION_LANDSCAPE);
                break;

            case Orientation.ORIENTATION_PORTRAIT:
            default:
                Orientation.setAppOrientation(Orientation.ORIENTATION_PORTRAIT);
        }
    }
}
