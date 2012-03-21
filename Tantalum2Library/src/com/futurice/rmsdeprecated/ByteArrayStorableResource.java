package com.futurice.rmsdeprecated;

import com.futurice.tantalum2.log.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Class responsible for deserializing and serializing byte array data stored in
 * RMS.
 *
 * @author mark voit
 */
public class ByteArrayStorableResource extends AbstractRMSResource {

    protected byte[] byteData;

    /**
     * Constructor of this class. Used for deserialization.
     *
     * @param id id of the resource
     */
    public ByteArrayStorableResource(final String id) {
        this(id, RMSResourceType.BYTE_ARRAY);
    }

    /**
     * Constructor of this class. Used for deserialization.
     *
     * @param id id of the resource
     * @param type type of the resource
     */
    protected ByteArrayStorableResource(final String id, RMSResourceType type) {
        super(id, type);
    }

    /**
     * Constructor of this class.
     *
     * @param sessionId id of the session this resource is valid for, zero means
     * always valid
     * @param id id of the resource
     * @param data the raw data to be stored
     */
    public ByteArrayStorableResource(final long sessionId, final String id, final byte[] data) {
        this(sessionId, id, data, RMSResourceType.BYTE_ARRAY);
    }

    /**
     * Constructor of this class.
     *
     * @param sessionId id of the session this resource is valid for
     * @param id id of the resource
     * @param data the raw data to be stored
     * @param type type of the resource
     */
    protected ByteArrayStorableResource(final long sessionId, final String id, final byte[] data, RMSResourceType type) {
        super(sessionId, id, type);
        this.byteData = data;
    }

    /**
     * @return the raw data
     */
    public byte[] getData() {
        return byteData;
    }

    /**
     * Clear raw data.
     */
    public void clearData() {
        this.byteData = null;
    }

    /**
     * @see com.nokia.experience.resource.rms.RMSResource#deserialize(byte[])
     */
    public void deserialize(byte[] bytes) {

        ByteArrayInputStream out = new ByteArrayInputStream(bytes);
        DataInputStream dataOut = new DataInputStream(out);
        try {
            this.id = dataOut.readUTF();
            this.sessionId = dataOut.readLong();
            int length = dataOut.readInt();
            this.byteData = new byte[length];
            dataOut.read(byteData);
        } catch (IOException e) {
        } finally {
            try {
                if (dataOut != null) {
                    ((InputStream) dataOut).close();
                }
            } catch (IOException e) {
                Log.l.log("Can not deserialze byte array", id, e);
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * @see com.nokia.experience.resource.rms.RMSResource#serialize()
     */
    public byte[] serialize() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(out);
        try {
            dataOut.writeUTF(id);
            dataOut.writeLong(sessionId);
            dataOut.writeInt(byteData.length);
            ((OutputStream) dataOut).write(byteData);
            byte[] bytes = out.toByteArray();
            return bytes;
        } catch (IOException e) {
            Log.l.log("Can not serialze byte array", id, e);
        } finally {
            try {
                if (dataOut != null) {
                    ((OutputStream) dataOut).close();
                }
            } catch (IOException e) {
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
            }
        }

        return null;
    }
}
