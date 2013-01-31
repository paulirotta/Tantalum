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
package org.tantalum.canvasrssreader;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Graphics;

/**
 * Abstract View class
 * @author ssaa
 */
public abstract class View implements CommandListener {
    public static final int SCROLL_BAR_WIDTH = 4;
    public static final int SCROLL_BAR_COLOR = 0x44000000;
    protected final RSSReaderCanvas canvas;
    protected int renderY;

    public View(final RSSReaderCanvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Renders the view using the Graphics object
     * @param g
     */
    public abstract void render(Graphics g, int width, int height);

    /**
     * Returns array of Commands related to this view
     * @return Command[]
     */
    public abstract Command[] getCommands();

    public int getRenderY() {
        return renderY;
    }

    public void setRenderY(final int renderY) {
        this.renderY = renderY;
    }

    public void renderScrollBar(final Graphics g, final int contentHeight) {
        final int visibleAreaHeight = canvas.getHeight();

        if (contentHeight < visibleAreaHeight) {
            //no need for a scrollbar
            return;
        }

        //fill background with transparent color
        g.setColor(SCROLL_BAR_COLOR);
        g.fillRect(canvas.getWidth() - SCROLL_BAR_WIDTH, 0, SCROLL_BAR_WIDTH, canvas.getHeight());

        final int barY = -renderY * canvas.getHeight() / contentHeight;
        final int barHeight = Math.max(2, canvas.getHeight() * canvas.getHeight() / contentHeight);

        //fill bar
        g.setColor(RSSReader.COLOR_HIGHLIGHTED_BACKGROUND);
        g.fillRect(canvas.getWidth() - SCROLL_BAR_WIDTH, barY, SCROLL_BAR_WIDTH, barHeight);
    }
}
