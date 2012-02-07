package com.futurice.tantalum2.rms;

import com.futurice.tantalum2.net.XMLStorableResource;


/**
 * Class defining available RMS resource types.
 * 
 * @author taho
 */
public abstract class RMSResourceType {
	
    private String type;
    
    public abstract RMSRecord createNewResource( final String id );
    
    public static final RMSResourceType BYTE_ARRAY = new RMSResourceType("B") {
         public RMSRecord createNewResource( final String id ) {
             return new ByteArrayStorableResource( id );
         }
    };
    
    public static final RMSResourceType XML = new RMSResourceType("X") {
        public RMSRecord createNewResource( final String id ) {
            return new XMLStorableResource( id );
        }
    };
    
    public static final RMSResourceType[] types = new RMSResourceType[] {BYTE_ARRAY, XML};

    private RMSResourceType( final String inType ) {
        this.type = inType;
    }

    public String getType() {
        return this.type;
    }

    public String toString() {
        return this.type;
    }

    public static RMSResourceType valueOf( final String type ) {
        for( int i=0; i < types.length; i++ ) {
            if( types[i].getType().equals( type ) ) {
                return types[i];
            }
        }
        
        return null;
    }

}
