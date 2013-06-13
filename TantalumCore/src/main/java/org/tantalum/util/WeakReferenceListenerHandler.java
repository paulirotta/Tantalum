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
public class WeakReferenceListenerHandler {

    private final Class listenerClass;
    private final Object[] NO_LISTENERS_REGISTERED = new Object[0];
    private final Vector listeners = new Vector();
    private volatile boolean listenersAreRegistered = false;

    /**
     * Create a new callback listener interface, specifying the class or
     * interface name of the callback
     *
     * @param listenerClass
     */
    public WeakReferenceListenerHandler(final Class listenerClass) {
        this.listenerClass = listenerClass;
    }

    /**
     * Add a new listener
     *
     * @param listener
     */
    public void registerListener(final Object listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Attempt to resister null callback. Expected " + listenerClass);
        }
        if (!(listenerClass.isAssignableFrom(listener.getClass()))) {
            throw new IllegalArgumentException(listener.getClass().toString() + " is not assignable from " + listenerClass + " so can not be registered as a listener");
        }

        synchronized (listeners) {
            unregisterListener(listener);
            listeners.addElement(new WeakReference(listener));
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
     * @param listener
     */
    public void unregisterListener(final Object listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Attempt to unresister null callback. Expected " + listenerClass);
        }

        synchronized (listeners) {
            for (int i = 0; i < listeners.size(); i++) {
                final WeakReference wr = (WeakReference) listeners.elementAt(i);
                final Object o = wr.get();

                if (o == null || listener.equals(o)) {
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
     * Number of listeners registered. Note that some of these may have expired
     * WeakReferences and this will not be detected until other calls to this
     * object.
     *
     * @return
     */
    public int size() {
        return listeners.size();
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

                if (o == null) {
                    listeners.removeElementAt(i--);
                } else {
                    v.addElement(o);
                }
            }
            listenersAreRegistered = !listeners.isEmpty();
            if (!listenersAreRegistered) {
                return NO_LISTENERS_REGISTERED;
            }

            final Object[] listenersCopy = new Object[v.size()];
            v.copyInto(listenersCopy);

            return listenersCopy;
        }
    }
}
