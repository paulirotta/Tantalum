/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.util;

import java.lang.ref.WeakReference;
import java.util.Vector;

/**
 * A callback pattern implementation that uses WeakReference to avoid any
 * possible memory leak from components failing to unregister themselves.
 *
 * A common mistake associated with callbacks is the failure to unregister which
 * prevents an object from being garbage collected. You can and still should
 * unregister, but if you forget to do so the callback will eventually be
 * unregistered by the garbage collector.
 *
 * @author phou
 */
public class WeakReferenceCallbackDelegate {

    private final Class callbackClass;
    private final Object[] NO_LISTENERS_REGISTERED = new Object[0];
    private final Vector listeners = new Vector();
    private volatile boolean listenersAreRegistered = false;

    /**
     * Create a new callback listener interface, specifying the class or
     * interface name of the callback
     *
     * @param callbackClass
     */
    public WeakReferenceCallbackDelegate(final Class callbackClass) {
        this.callbackClass = callbackClass;
    }

    /**
     * Add a new listener
     *
     * @param callback
     */
    public void registerListener(final Object callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Attempt to resister null callback. Expected " + callbackClass);
        }
        if (!(callbackClass.isAssignableFrom(callback.getClass()))) {
            throw new IllegalArgumentException(callback.getClass().toString() + " is not assignable from " + callbackClass + " so can not be registered as a listener");
        }

        synchronized (listeners) {
            unregisterListener(callback);
            listeners.add(new WeakReference(callback));
            listenersAreRegistered = true;
        }
    }

    /**
     * Remove a listener.
     *
     * If for some reason a developer forgets to call this method, the callbacks
     * will continue to notify the object until such time as there are no other
     * references to the callback object and the garbage collector clears this
     * object. This is not ideal, but less damaging than the alternative of
     * introducing a memory leak.
     *
     * @param callback
     */
    public void unregisterListener(final Object callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Attempt to unresister null callback. Expected " + callbackClass);
        }

        synchronized (listeners) {
            for (int i = 0; i < listeners.size(); i++) {
                final WeakReference wr = (WeakReference) listeners.elementAt(i);
                final Object o = wr.get();

                if (o == null || callback.equals(o)) {
                    listeners.removeElementAt(i--);
                }
            }
            listenersAreRegistered = !listeners.isEmpty();
        }
    }

    /**
     * Indicate if there are any listeners currently registered
     * 
     * @return 
     */
    public boolean isEmpty() {
        return !listenersAreRegistered;
    }

    /**
     * Return a list of all registered listeners for which the WeakReference has
     * not expired. You can call the relevant method of these objects with any
     * parameters you like and with some assurance the object is still in use.
     *
     * @return
     */
    public Object[] getAllListeners() {
        if (!listenersAreRegistered) {
            return NO_LISTENERS_REGISTERED;
        }

        synchronized (listeners) {
            final Vector v = new Vector(listeners.size());

            for (int i = 0; i < listeners.size(); i++) {
                final WeakReference wr = (WeakReference) listeners.elementAt(i);
                final Object o = wr.get();

                if (o != null) {
                    v.add(o);
                } else {
                    listeners.removeElementAt(i--);
                }
            }
            listenersAreRegistered = !listeners.isEmpty();

            return v.toArray();
        }
    }
}
