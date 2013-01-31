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

import com.sun.lwuit.*;
import com.sun.lwuit.animations.CommonTransitions;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.list.ListCellRenderer;
import org.tantalum.Task;
import org.tantalum.Worker;
import org.tantalum.net.StaticWebCache;
import org.tantalum.net.xml.RSSItem;

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
        setBackCommand(exitCommand);

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

            final Task task = new Task() {
                public Object doInBackground(final Object in) {
                    isReloading = false;

                    return in;
                }

                protected void onCanceled() {
                    isReloading = false;
                }
            };

            if (fromNet) {
                feedCache.get(midlet.getUrl(), Worker.HIGH_PRIORITY, StaticWebCache.GET_WEB, task);
            } else {
                feedCache.get(midlet.getUrl(), Worker.HIGH_PRIORITY, StaticWebCache.GET_ANYWHERE, task);
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
