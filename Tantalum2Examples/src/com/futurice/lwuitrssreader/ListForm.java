package com.futurice.lwuitrssreader;

import com.futurice.tantalum2.rms.DefaultResult;
import com.futurice.tantalum2.net.StaticWebCache;
import com.sun.lwuit.Form;
import com.sun.lwuit.Command;
import com.sun.lwuit.List;
import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Label;
import com.sun.lwuit.animations.CommonTransitions;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.list.ListCellRenderer;

/**
 * @author tsaa
 */
public class ListForm extends Form implements ActionListener, ListCellRenderer {

    static Command settingsCommand = new Command("Settings");
    static Command reloadCommand = new Command("Reload");
    static Command exitCommand = new Command("Exit");
    public final List list;
    private final ListModel listModel;
    private RSSReader midlet;
    private boolean isReloading = false;
    private StaticWebCache cache;

    public ListForm(String title, RSSReader midlet) {
        super(title);
        this.midlet = midlet;
        listModel = new ListModel(this);
        list = new List(listModel);
        list.addActionListener(this);
        cache = new StaticWebCache("feeds", 1, 1, new RSSTypeHandler(listModel));

        addComponent(list);
        addCommand(settingsCommand);
        addCommand(reloadCommand);
        addCommand(exitCommand);

        setTransitionOutAnimator(
                CommonTransitions.createSlide(
                CommonTransitions.SLIDE_HORIZONTAL, false, 200));
        list.setRenderer(this);
        this.addCommandListener(this);
        reload(false);
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getCommand() != null) {
            String cmdStr = ae.getCommand().getCommandName();

            if (cmdStr.equals("Settings")) {
                midlet.getSettingsForm().show();
            }
            if (cmdStr.equals("Reload")) {
                reload(true);
            }
            if (cmdStr.equals("Exit")) {
                midlet.destroyApp(true);
            }
        } else {
            int selectedIndex = ((List) ae.getSource()).getSelectedIndex();
            DetailsForm detailsForm = midlet.getDetailsForm();
            detailsForm.setCurrentRSSItem((RSSItem) listModel.getItemAt(selectedIndex));
            detailsForm.show();
        }
    }

    public void reload(boolean fromNet) {
        if (!isReloading) {
            isReloading = true;

            int listSize = list.getModel().getSize();
            for (int i = 0; i < listSize; i++) {
                list.getModel().removeItem(i);
            }

            if (fromNet) {
                cache.update(midlet.getUrl(), new DefaultResult() {

                    public void run() {
                        isReloading = false;
                    }
                });
            } else {
                cache.get(midlet.getUrl(), new DefaultResult() {

                    public void run() {
                        isReloading = false;
                    }
                });
            }
        }
    }

    public RSSReader getMIDlet() {
        return midlet;
    }

    public Component getListCellRendererComponent(List list, Object value, int index, boolean isSelected) {
        Container c = new Container();
        Label titleLabel = new Label(((RSSItem) value).getTitle());
        titleLabel.getStyle().setFont(RSSReader.plainFont);
        c.addComponent(titleLabel);
        return c;
    }

    public Component getListFocusComponent(List list) {
        return null;
    }
}
