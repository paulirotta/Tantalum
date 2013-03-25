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
package org.tantalum.jme;

import javax.microedition.midlet.MIDlet;
import org.tantalum.PlatformUtils;

/**
 * This is a convenience class to embody the best practice patterns for starting
 * and stopping an app which uses Tantalum. Using this class is optional, but
 * makes your life simpler. You can either extend it, or implement your own
 * variant.
 *
 * @author phou
 */
public abstract class TantalumMIDlet extends MIDlet {
    /**
     * A suggested number of background Worker threads for this platform. Use
     * this if you do not have a specific reason to increase or decrease it.
     * More threads means more concurrent operations such as HTTP GET, but can
     * increase context switching overhead and contention for scarce bandwidth
     * under poor network conditions. More threads is more useful on multi-core
     * hardware.
     *
     * Series40 phones have one core, but fairly low context switching overhead
     * so concurrency works well. If you find foreground tasks bogging down or
     * too much network contention when there are parallel HTTP GET operations,
     * try reducing the number of threads to 2.
     */
    protected static final int DEFAULT_NUMBER_OF_WORKER_THREADS = 4;

    /**
     * Create a MIDlet that hooks into the MIDlet life-cycle events to
     * start and stop the background Worker threads with proper notification.
     * 
     * @param numberOfWorkerThreads
     */
    protected TantalumMIDlet(final int numberOfWorkerThreads) {
        this(numberOfWorkerThreads, JMELog.NORMAL_MODE);
    }
    /**
     * If you create a MIDlet constructor, you must call super() as the first
     * line of your MIDlet's constructor. Alternatively, you can call
     * Worker.init() yourself with custom parameters for special needs.
     *
     * @param numberOfWorkerThreads -  - Feel free to use
     * TantalumMIDlet.DEFAULT_NUMBER_OF_WORKER_THREADS
     * @param routeDebugOutputToUsbSerialPort 
     */
    protected TantalumMIDlet(final int numberOfWorkerThreads, final int logMode) {
        super();

        PlatformUtils.getInstance().setProgram(this, numberOfWorkerThreads, logMode);
    }

    /**
     * Do not call this directly. Call exitMidlet(false) instead.
     *
     * This is part of MIDlet lifecycle model and may be called by the platform,
     * for example when a users presses and holds the RED button on their phone
     * to force an application to close.
     *
     * @param unconditional
     */
    protected void destroyApp(final boolean unconditional) {
        PlatformUtils.getInstance().shutdown(unconditional);
    }
}
