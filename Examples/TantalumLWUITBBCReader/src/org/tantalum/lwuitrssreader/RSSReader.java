package org.tantalum.lwuitrssreader;

import com.sun.lwuit.Dialog;
import com.sun.lwuit.Display;
import com.sun.lwuit.Font;
import com.sun.lwuit.plaf.DefaultLookAndFeel;
import com.sun.lwuit.plaf.UIManager;
import org.tantalum.j2me.TantalumMIDlet;

/**
 * @author tsaa
 */
public class RSSReader extends TantalumMIDlet {

    public static int SCREEN_WIDTH;
    public static int SCREEN_HEIGHT;
    private ListForm listForm;
    private SettingsForm settingsForm;
    private DetailsForm detailsForm;
    private String url;
    public static Font mediumFont;
    public static Font plainFont;
    public static Font italicFont;
    public static Font underlinedFont;

    public RSSReader() {
        super(DEFAULT_NUMBER_OF_WORKER_THREADS);
    }
    
    public void startApp() {
        Display.init(this);
        UIManager.getInstance().setLookAndFeel(new DefaultLookAndFeel());

        mediumFont = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
        plainFont = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        italicFont = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_ITALIC, Font.SIZE_SMALL);
        underlinedFont = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_UNDERLINED, Font.SIZE_SMALL);
        url = getAppProperty("RSS-Feed-Url");
        listForm = new ListForm("LWUIT RSS Reader", this);

        SCREEN_WIDTH = listForm.getWidth();
        SCREEN_HEIGHT = listForm.getHeight();
        listForm.show();
        settingsForm = new SettingsForm("LWUIT RSS Reader", this);
        detailsForm = new DetailsForm("LWUIT RSS Reader", this);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ListForm getListForm() {
        return listForm;
    }

    public SettingsForm getSettingsForm() {
        return settingsForm;
    }

    public DetailsForm getDetailsForm() {
        return detailsForm;
    }

    public String getUrl() {
        return url;
    }

    public static void showDialog(String msg) {
        Dialog dialog = new Dialog(msg);
        dialog.setTimeout(2000);
        dialog.setDialogType(Dialog.TYPE_ERROR);
        dialog.show();
    }
}
