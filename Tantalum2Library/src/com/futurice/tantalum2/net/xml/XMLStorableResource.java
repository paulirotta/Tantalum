package com.futurice.tantalum2.net.xml;

import com.futurice.rmsdeprecated.ByteArrayStorableResource;
import com.futurice.rmsdeprecated.RMSResourceType;

/**
 * Class responsible for deserializing and serializing large byte arrays.
 * This class is essentially the same as ByteArrayStorableResource,
 * but since the type is RMSResourceType.XML, it is handled differently in RMSResourceDB.  
 * 
 * @author mark voit
 */
public final class XMLStorableResource extends ByteArrayStorableResource {
    /**
     * Constructor of this class.
     * Used for deserialization.
     * 
     * @param id id of the resource
     */
    public XMLStorableResource(final String id) {
        super(id, RMSResourceType.XML);
    }

    /**
     * Constructor of this class.
     * 
     * @param sessionId id of the session this resource is valid for
     * @param id id of the resource
     * @param inData the raw data to be stored
     */
    public XMLStorableResource(final long sessionId, final String id, final byte[] inData) {
        super(sessionId, id, inData, RMSResourceType.XML);
    }
}
