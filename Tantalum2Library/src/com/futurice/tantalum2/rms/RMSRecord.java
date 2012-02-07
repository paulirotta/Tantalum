package com.futurice.tantalum2.rms;


/** 
 * Interface representing a resource stored in RMS.
 *
 * @author mark voit
 */
public interface RMSRecord  {
	
	/**
	 * @return type if the resource
	 */
    public RMSResourceType getType();
    
    /**
     * @return id of the resource
     */
    public String getId();
    
    /**
     * @return id of session this resource is valid for
     */
    public long getSessionId();
    
    /**
     * @return byte array representation of the resource
     */
    public byte[] serialize();
    
    /**
     * @param bytes the bytes to be deserialized
     */
    public void deserialize(final byte[] bytes);
}
