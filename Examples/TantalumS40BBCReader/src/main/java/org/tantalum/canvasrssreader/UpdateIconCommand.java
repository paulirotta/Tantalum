/*
 Copyright (c) 2013 Nokia Corporation. All rights reserved.
 Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation.
 Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 affiliates. Other product and company names mentioned herein may be trademarks
 or trade names of their respective owners.

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
package org.tantalum.canvasrssreader;

import com.nokia.mid.ui.DirectUtils;
import com.nokia.mid.ui.IconCommand;
import com.nokia.mid.ui.orientation.Orientation;
import java.util.TimerTask;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import org.tantalum.Task;
import org.tantalum.util.L;

/**
 *
 * @author phou
 */
public class UpdateIconCommand extends IconCommand {

    static Image image = null;
    private Graphics g;
    private TimerTask animationTimerTask;
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
        if (animationTimerTask == null) {
            g.setColor(0xff000000);
            g.fillRect(0, 0, WIDTH, HEIGHT);
            animationTimerTask = new TimerTask() {
                public void run() {
                    drawSpinner();
                }
            };
            Task.getTimer().schedule(animationTimerTask, 0, 100);
        }
    }

    public void stopAnimation() {
        if (animationTimerTask != null) {
            animationTimerTask.cancel();
            animationTimerTask = null;
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
