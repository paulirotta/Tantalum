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
package org.tantalum.s40rssreader;

import com.nokia.mid.ui.DirectUtils;
import com.nokia.mid.ui.IconCommand;
import com.nokia.mid.ui.orientation.Orientation;
import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import org.tantalum.util.L;

/**
 *
 * @author phou
 */
public class UpdateIconCommand extends IconCommand {

    static Image image = null;
    private Graphics g;
    private Timer animationTimer;
    private double angle;
    private int startDot;
    private static final int WIDTH = 36;
    private static final int HEIGHT = 36;
    private static final double XC = WIDTH / 2.0;
    private static final double YC = HEIGHT / 2.0;
    private static final double R = 10;
    private static final int[] shades = {0x000000, 0xffffff, 0xdddddd, 0xbbbbbb, 0x999999, 0x777777, 0x333333};
    private static final int dots = shades.length;
    private static final double step = (2 * Math.PI) / dots;
    private static final double circle = (2 * Math.PI);
    private static Image iconImage = DirectUtils.createImage(WIDTH, HEIGHT, 0xffffff);

    static {
        try {
            image = Image.createImage("/connect.png");
        } catch (Exception e) {
            //#debug
            L.e("Can not initialize", "Update icon image", e);
        }
    }

    public UpdateIconCommand() {
        super("Update", "Update article list", iconImage, iconImage, Command.OK, 0);

        angle = 0.0;
        startDot = 0;
        g = iconImage.getGraphics();
        g.drawImage(image, (int) XC, (int) YC, Graphics.HCENTER | Graphics.VCENTER);
    }

    public void startAnimation() {
        if (animationTimer == null) {
            g.setColor(0xff000000);
            g.fillRect(0, 0, WIDTH, HEIGHT);
            animationTimer = new Timer();
            animationTimer.schedule(new TimerTask() {

                public void run() {
                    drawSpinner();
                }
            }, 0, 100);
        }
    }

    public void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.cancel();
            animationTimer = null;
            g.setColor(0x000000);
            g.fillRect(0, 0, WIDTH, HEIGHT);
            g.drawImage(image, (int) XC, (int) YC, Graphics.HCENTER | Graphics.VCENTER);

            // Force the screen to repaint completely, including the no-longer-animated icon
            Orientation.setAppOrientation(Orientation.getAppOrientation());
        }
    }

    public void drawSpinner() {
        for (int i = 0; i < dots; i++) {
            int x = (int) (XC + R * Math.cos(angle));
            int y = (int) (YC + R * Math.sin(angle));
            g.setColor(shades[(i + startDot) % dots]);
            g.fillRoundRect(x, y, 6, 6, 3, 3);
            angle = (angle - step) % circle;
        }
        startDot++;
        startDot = startDot % dots;

        // Force the screen to repaint completely, including the animated icon
        Orientation.setAppOrientation(Orientation.getAppOrientation());
    }
}
