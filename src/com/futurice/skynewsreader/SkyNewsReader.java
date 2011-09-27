package com.futurice.skynewsreader;

import com.futurice.tantalum2.Worker;
import javax.microedition.midlet.MIDlet;
import com.sun.lwuit.Display;
import com.sun.lwuit.plaf.UIManager;
import com.sun.lwuit.plaf.DefaultLookAndFeel;
import com.sun.lwuit.Font;
import com.sun.lwuit.Dialog;

/**
 * @author tsaa
 */
public class SkyNewsReader extends MIDlet {

    public static final int SCREEN_WIDTH = 240;
    public static final int SCREEN_HEIGHT = 320;
    private ListForm listForm;
    private SettingsForm settingsForm;
    private DetailsForm detailsForm;
    private String url;
    public static Font mediumFont;
    public static Font plainFont;
    public static Font italicFont;
    public static Font underlinedFont;

    public void startApp() {
        Display.init(this);
        UIManager.getInstance().setLookAndFeel(new DefaultLookAndFeel());
        Worker.init(this, 2);

        mediumFont = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
        plainFont = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        italicFont = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_ITALIC, Font.SIZE_SMALL);
        underlinedFont = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_UNDERLINED, Font.SIZE_SMALL);
        url = "http://news.sky.com/sky-news/rss/home/rss.xml";
        listForm = new ListForm("Sky News Reader", this);
        settingsForm = new SettingsForm("Sky News Reader", this);
        detailsForm = new DetailsForm("Sky News Reader", this);
        
        //SCREEN_WIDTH = 240;//listForm.getWidth();
        //SCREEN_HEIGHT = 320;//listForm.getHeight();
        
        listForm.show();
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
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
