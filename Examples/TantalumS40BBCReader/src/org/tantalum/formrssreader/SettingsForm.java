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
package org.tantalum.formrssreader;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.rms.RecordStoreFullException;
import org.tantalum.j2me.RMSUtils;
import org.tantalum.util.L;

/**
 * Simple settings form for setting the RSS Feed URL
 *
 * @author ssaa
 */
public final class SettingsForm extends TextBox implements CommandListener {
    private final FormRSSReader midlet;
    private final Command saveCommand = new Command("Save", Command.OK, 0);
    private final Command backCommand = new Command("Back", Command.BACK, 0);

    public SettingsForm(final FormRSSReader midlet) {
        super("RSS Feed URL", "", 256, TextField.URL & TextField.NON_PREDICTIVE);
        setInitialInputMode("UCB_BASIC_LATIN");
        this.midlet = midlet;

        addCommand(saveCommand);
        addCommand(backCommand);
        setCommandListener(this);
    }

    public void setUrlValue(String url) {
        setString(url);
    }

    public String getUrlValue() {
        return getString();
    }

    public void commandAction(Command command, Displayable displayable) {

        if (command == saveCommand) {
            try {
                RMSUtils.write("settings", getString().getBytes());
            } catch (RecordStoreFullException ex) {
                L.e("Can not write settings", "", ex);
            }
            midlet.switchDisplayable(null, midlet.getList());
            midlet.getList().reload(true);
        } else if (command == backCommand) {
            midlet.switchDisplayable(null, midlet.getList());
        }
    }
}
