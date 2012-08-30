package com.futurice.lwuitrssreader;

import com.futurice.tantalum3.Task;
import com.futurice.tantalum3.Worker;
import com.futurice.tantalum3.net.StaticWebCache;
import com.futurice.tantalum3.net.xml.RSSItem;
import com.sun.lwuit.*;
import com.sun.lwuit.animations.CommonTransitions;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.list.ListCellRenderer;

/**
 * @author tsaa
 */
public final class ListForm extends Form implements ActionListener, ListCellRenderer {

    static final Command settingsCommand = new Command("Settings");
    static final Command reloadCommand = new Command("Reload");
    static final Command exitCommand = new Command("Exit");
    private final ListModel listModel = new ListModel(this);
    public final List list = new List(listModel);
    private final StaticWebCache feedCache = new StaticWebCache('5', listModel);
    private RSSReader midlet;
    private boolean isReloading = false;

    public ListForm(String title, RSSReader midlet) {
        super(title);
        this.midlet = midlet;
        list.addActionListener(this);

        addComponent(list);
        addCommand(settingsCommand);
        addCommand(exitCommand);
        addCommand(reloadCommand);
        this.setBackCommand(exitCommand);

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
                Worker.shutdown(true);
            }
        } else {
            int selectedIndex = ((List) ae.getSource()).getSelectedIndex();
            DetailsForm detailsForm = midlet.getDetailsForm();
            detailsForm.setCurrentRSSItem((RSSItem) listModel.getItemAt(selectedIndex));
            detailsForm.show();
        }
    }

    public void reload(final boolean fromNet) {
        if (!isReloading) {
            isReloading = true;

            int listSize = list.getModel().getSize();
            for (int i = 0; i < listSize; i++) {
                list.getModel().removeItem(i);
            }

            final Task result = new Task() {

                public void onCancel() {
                    isReloading = false;
                }

                public void set(Object o) {
                    isReloading = false;
                    super.set(o);
                }
            };

            if (fromNet) {
                feedCache.update(midlet.getUrl(), result);
            } else {
                feedCache.get(midlet.getUrl(), result, Worker.HIGH_PRIORITY);
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
