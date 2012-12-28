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
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.animations.CommonTransitions;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;

/**
 * @author tsaa
 */
public class SettingsForm extends Form implements ActionListener {

    private static Command saveCommand = new Command("Save");
    private static Command backCommand = new Command("Back");
    private Label urlLabel;
    private TextArea urlTextArea;
    private RSSReader midlet;

    public SettingsForm(String title, RSSReader midlet) {
        super(title);
        this.midlet = midlet;

        urlLabel = new Label("RSS Feed URL");
        urlTextArea = new TextArea(midlet.getUrl());
        urlTextArea.setEditable(true);
        urlTextArea.getStyle().setFont(RSSReader.plainFont);
        this.addComponent(urlLabel);
        this.addComponent(urlTextArea);
        this.addCommand(saveCommand);
        this.addCommand(backCommand);
        this.setBackCommand(backCommand);

        setTransitionOutAnimator(
                CommonTransitions.createSlide(
                CommonTransitions.SLIDE_HORIZONTAL, false, 200));

        addCommandListener(this);
    }

    public void actionPerformed(ActionEvent ae) {
        String cmdStr = ae.getCommand().getCommandName();
        if (cmdStr.equals("Save")) {
            midlet.setUrl(urlTextArea.getText());
            midlet.getListForm().show();

        }
        if (cmdStr.equals("Back")) {
            urlTextArea.setText(midlet.getUrl());
            midlet.getListForm().show();
        }
    }
}
