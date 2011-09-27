/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.skynewsreader;

import com.futurice.tantalum2.Log;
import com.sun.lwuit.list.DefaultListModel;
import com.sun.lwuit.events.DataChangedListener;

/**
 *
 * @author tsaa
 */
public class ListModel extends DefaultListModel implements DataChangedListener {

    private ListForm listForm;

    public ListModel(ListForm listForm) {
        this.listForm = listForm;
        addDataChangedListener(this);
    }

    public void dataChanged(int type, int index) {
        if (type != ListModel.REMOVED) {
            listForm.repaint();
        }
    }
}
