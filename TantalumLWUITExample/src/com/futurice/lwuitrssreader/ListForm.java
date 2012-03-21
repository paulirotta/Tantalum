package com.futurice.lwuitrssreader;

import com.futurice.tantalum2.DefaultResult;
import com.futurice.tantalum2.DefaultRunnableResult;
import com.futurice.tantalum2.net.StaticWebCache;
import com.sun.lwuit.*;
import com.sun.lwuit.animations.CommonTransitions;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.list.ListCellRenderer;

/**
 * @author tsaa
 */
public final class ListForm extends Form implements ActionListener, ListCellRenderer {

    static Command settingsCommand = new Command("Settings");
    static Command reloadCommand = new Command("Reload");
    static Command exitCommand = new Command("Exit");
    public final List list;
    private final ListModel listModel;
    private RSSReader midlet;
    private boolean isReloading = false;
    private StaticWebCache feedCache;

    public ListForm(String title, RSSReader midlet) {
        super(title);
        this.midlet = midlet;
        listModel = new ListModel(this);
        list = new List(listModel);
        list.addActionListener(this);
        feedCache = new StaticWebCache("feeds", '5', new RSSTypeHandler(listModel));

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

    public void reload(final boolean fromNet) {
        if (!isReloading) {
            isReloading = true;

            int listSize = list.getModel().getSize();
            for (int i = 0; i < listSize; i++) {
                list.getModel().removeItem(i);
            }

            final DefaultResult result = new DefaultResult() {

                public void noResult() {
                    isReloading = false;
                }

                public void setResult(Object o) {
                    super.setResult(o);
                    
                    isReloading = false;
                }
            };

            if (fromNet) {
                feedCache.update(midlet.getUrl(), result);
            } else {
                feedCache.get(midlet.getUrl(), result);
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
