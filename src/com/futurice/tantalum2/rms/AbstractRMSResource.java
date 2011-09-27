package com.futurice.tantalum2.rms;

import com.futurice.tantalum2.rms.RMSResourceType;
import com.futurice.tantalum2.rms.RMSRecord;

/**
 * Abstract base class for RMS resources.
 * 
 * @author mark voit
 */
abstract class AbstractRMSResource implements RMSRecord {
	
    protected String id;
    protected long sessionId = 0;
    protected RMSResourceType type;

    /**
     * Constructor of this class.
	 */
    public AbstractRMSResource() {
    }

    /**
     * Constructor of this class.
     * Used for deserialization.
     * 
     * @param id id of the resource
     * @param type type of the resource
     */
    public AbstractRMSResource(final String id, final RMSResourceType type) {
        this.id = id;
        this.type = type;
    }
    
    /**
     * Constructor of this class.
     * 
     * @param sessionID if of the session this resource is valid for
     * @param id id of the resource
     * @param type type of the resource
     */
    public AbstractRMSResource(final long sessionID, final String id, final RMSResourceType type) {
    	this.sessionId = sessionID;
        this.id = id;
        this.type = type;
    }

    /**
     * @see com.nokia.experience.resource.rms.RMSRecord#getId()
     */
    public String getId() {
        return id;
    }

    /**
     * @see com.nokia.experience.resource.rms.RMSRecord#getSessionId()
     */
    public long getSessionId() {
        return sessionId;
    }
    
    /**
     * @see com.nokia.experience.resource.rms.RMSRecord#getType()
     */
    public RMSResourceType getType() {
        return type;
    }

}
