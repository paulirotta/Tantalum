/*
 Copyright © 2012 Paul Houghton and Futurice on behalf of the Tantalum Project.
 All rights reserved.

 Tantalum software shall be used to make the world a better place for everyone.

 This software is licensed for use under the Apache 2 open source software license,
 http://www.apache.org/licenses/LICENSE-2.0.html

 You are kindly requested to return your improvements to this library to the
 open source community at http://projects.developer.nokia.com/Tantalum

 The above copyright and license notice notice shall be included in all copies
 or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package org.tantalum.lwuitrssreader;

import com.sun.lwuit.Command;
import com.sun.lwuit.Form;
import com.sun.lwuit.Image;
import com.sun.lwuit.Label;
import com.sun.lwuit.animations.CommonTransitions;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import java.util.Vector;
import org.tantalum.UITask;
import org.tantalum.Worker;
import org.tantalum.net.StaticWebCache;
import org.tantalum.net.xml.RSSItem;
import org.tantalum.util.L;

/**
 *
 * @author tsaa
 */
public class DetailsForm extends Form implements ActionListener {

    static Command linkCommand = new Command("Open link");
    static Command backCommand = new Command("Back");
    private Label pubDateLabel;
    private Label imgLabel;
    private Vector titleLabels;
    private Vector descriptionLabels;
    private Vector linkLabels;
    private RSSReader midlet;
    private RSSItem current;
    private static final StaticWebCache imageCache = StaticWebCache.getWebCache('1', new LWUITImageTypeHandler());

    public DetailsForm(String title, RSSReader midlet) {
        super(title);
        this.midlet = midlet;
        setScrollableY(true);
        pubDateLabel = new Label("");

        pubDateLabel.getStyle().setFont(RSSReader.italicFont);

        imgLabel = new Label("");


        addCommand(backCommand);
        addCommand(linkCommand);
        this.setBackCommand(backCommand);

        setTransitionOutAnimator(
                CommonTransitions.createSlide(
                CommonTransitions.SLIDE_HORIZONTAL, true, 200));

        addCommandListener(this);
    }

    public void actionPerformed(ActionEvent ae) {
        String cmdStr = ae.getCommand().getCommandName();

        if (cmdStr.equals("Open link")) {
            try {
                midlet.platformRequest(current.getLink());
            } catch (Exception e) {
                RSSReader.showDialog("Couldn't open link");
            }
        }
        if (cmdStr.equals("Back")) {
            midlet.getListForm().show();
        }
    }

    public void setCurrentRSSItem(final RSSItem item) {
        current = item;
        removeAll();

        titleLabels = getLabels(item.getTitle(), RSSReader.mediumFont, RSSReader.SCREEN_WIDTH);
        pubDateLabel.setText(item.getPubDate());
        descriptionLabels = getLabels(item.getDescription(), RSSReader.plainFont, RSSReader.SCREEN_WIDTH);
        linkLabels = getLabels(item.getLink(), RSSReader.underlinedFont, RSSReader.SCREEN_WIDTH);
        imgLabel = new Label("");
        addLabels(titleLabels);
        addComponent(pubDateLabel);
        addLabels(descriptionLabels);
        addComponent(imgLabel);

        imageCache.getAsync(item.getThumbnail(), Worker.HIGH_PRIORITY, StaticWebCache.GET_ANYWHERE, new UITask() {
                                                               protected void onPostExecute(final Object result) {
                                                                   try {
                                                                       imgLabel.setIcon((Image) result);
                                                                       DetailsForm.this.repaint();
                                                                   } catch (Exception ex) {
                                                                       //#debug
                                                                       L.e("Can not get image for RSSItem", item.getThumbnail(), ex);
                                                                   }
                                                               }
                                                           });

        addLabels(linkLabels);
        setScrollY(0);
    }

    public Vector getLabels(String str, com.sun.lwuit.Font font, int width) {
        final Vector labels = new Vector();
        final Vector lines = StringUtils.splitToLines(str, font, width);
        for (int i = 0; i < lines.size(); i++) {
            labels.addElement(new Label((String) lines.elementAt(i)));
            ((Label) labels.lastElement()).getStyle().setFont(font);
            ((Label) labels.lastElement()).setGap(0);
        }
        return labels;
    }

    public void addLabels(Vector labels) {
        for (int i = 0; i < labels.size(); i++) {
            addComponent((Label) labels.elementAt(i));
        }
    }
}
