package com.futurice.skynewsreader;

import com.futurice.tantalum2.DefaultCacheGetResult;
import com.futurice.tantalum2.StaticWebCache;
import com.sun.lwuit.Form;
import com.sun.lwuit.Command;
import com.sun.lwuit.List;
import com.sun.lwuit.animations.CommonTransitions;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.ActionEvent;

/**
 * @author tsaa
 */
public class ListForm extends Form implements ActionListener {

    static Command reloadCommand = new Command("Reload");
    static Command exitCommand = new Command("Exit");
    public List list;
    private ListModel listModel;
    private SkyNewsReader midlet;
    private boolean isReloading = false;
    private RSSVO rssvo;
    private StaticWebCache feedsCache;

    public ListForm(String title, SkyNewsReader midlet) {
        super(title);
        this.midlet = midlet;
        listModel = new ListModel(this);
        list = new List(listModel);
        list.addActionListener(this);
        list.setItemGap(0);
        getStyle().setBgColor(0x444444);
        getTitleComponent().getStyle().setBgColor(0x770000);
        getSoftButtonStyle().setBgColor(0x770000);
        feedsCache = new StaticWebCache("feeds", 1, 1, new RSSTypeHandler(listModel));

        addComponent(list);
        addCommand(reloadCommand);
        addCommand(exitCommand);

        setTransitionOutAnimator(
                CommonTransitions.createSlide(
                CommonTransitions.SLIDE_HORIZONTAL, false, 200));
        list.setListCellRenderer(new RSSListCellRenderer());
        setCommandListener(this);
        reload(false);
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getCommand() != null) {
            String cmdStr = ae.getCommand().getCommandName();

            if (cmdStr.equals("Reload")) {
                reload(true);
            }
            if (cmdStr.equals("Exit")) {
                midlet.notifyDestroyed();
            }
        } else {
            int selectedIndex = ((List) ae.getSource()).getSelectedIndex();
            DetailsForm detailsForm = midlet.getDetailsForm();
            detailsForm.setCurrentRSSItem((RSSItem) listModel.getItemAt(selectedIndex));
            detailsForm.show();
        }
    }

    public void reload(boolean straightFromWeb) {
        if (!isReloading) {
            isReloading = true;

            /*int listSize = list.getModel().getSize();
            for (int i = 0; i < listSize; i++) {
            list.getModel().removeItem(i);
            }*/
            listModel = new ListModel(this);
            list.setModel(listModel);
            ((RSSTypeHandler) feedsCache.getHandler()).setListModel(listModel);
            repaint();
            if (straightFromWeb) {
                feedsCache.update(midlet.getUrl(), new DefaultCacheGetResult() {

                    public void run() {
                        isReloading = false;
                    }
                });
            } else {
                feedsCache.get(midlet.getUrl(), new DefaultCacheGetResult() {

                    public void run() {
                        isReloading = false;
                    }
                });
            }
        }
    }

    public SkyNewsReader getMIDlet() {
        return midlet;
    }
}
