/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.storage;

/**
 * RMS Full or Database Full on the phone, need to clear space
 *
 * This class is used for platform-independence vs difference exceptions on
 * various platforms
 * 
 * @author phou
 */
public final class FlashFullException extends FlashDatabaseException {
    public FlashFullException(final String message) {
        super(message);
    }
}
