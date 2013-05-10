/*
 Copyright (c) 2013, Paul Houghton and Futurice Oy
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
package org.tantalum.lwuitrssreader;

import com.sun.lwuit.Dialog;
import com.sun.lwuit.Display;
import com.sun.lwuit.Font;
import com.sun.lwuit.plaf.DefaultLookAndFeel;
import com.sun.lwuit.plaf.UIManager;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import org.tantalum.PlatformUtils;
import org.tantalum.jme.TantalumJME;

/**
 * @author tsaa
 */
public class RSSReader extends MIDlet {

    public static int SCREEN_WIDTH;
    public static int SCREEN_HEIGHT;
    private ListForm listForm;
    private DetailsForm detailsForm;
    private final String url = "http://feeds.bbci.co.uk/news/rss.xml";
    public static Font mediumFont;
    public static Font plainFont;
    public static Font italicFont;
    public static Font underlinedFont;

    public void startApp() {
        TantalumJME.start(this);
        Display.init(this);
        UIManager.getInstance().setLookAndFeel(new DefaultLookAndFeel());

        mediumFont = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
        plainFont = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        italicFont = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_ITALIC, Font.SIZE_SMALL);
        underlinedFont = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_UNDERLINED, Font.SIZE_SMALL);
        listForm = new ListForm("LWUIT RSS Reader", this);

        SCREEN_WIDTH = listForm.getWidth();
        SCREEN_HEIGHT = listForm.getHeight();
        listForm.show();
        detailsForm = new DetailsForm("LWUIT RSS Reader", this);
    }

    public ListForm getListForm() {
        return listForm;
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

    protected void pauseApp() {
    }

    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        PlatformUtils.getInstance().shutdown(unconditional, "destroyApp(" + unconditional + ") received from phone");
    }
}
