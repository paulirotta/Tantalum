package com.futurice.tantalum2.rms;

/**
 * Callback interface for RMSResourceDB.
 * 
 * @author mark voit
 */
interface RMSCallbackListener {
	
    /**
     * Invoked when resource has been stored to RMS.
     * 
     * @param resource resource that has been stored
     */
	void notifyRMSWriteCompleted(RMSRecord resource);
}
