/**
 *
 */
package com.futurice.rmsdeprecated;

import com.futurice.rmsdeprecated.ByteArrayStorableResource;

/**
 * Class responsible for handling requests to RMSRersourceDB.
 *
 * @author mark voit
 */
public class RMSGetter {

    private static final String LOG_TAG = "RMSGetter";
    private final long sessionId;
    private final RMSResourceType type;
    private final String url;

    /**
     * Constructor
     *
     * @param sessionId id of the session this resource is valid for
     * @param type type of the resource
     * @param url url of the request
     * @param resultHandler the callback to be invoked in case of success or
     * error
     */
    public RMSGetter(final long sessionId, final RMSResourceType type, final String url) {
        this.sessionId = sessionId;
        this.type = type;
        this.url = url;
    }

    public byte[] get() {
        return getFromRMS(type, url);
    }

    /**
     * @see com.nokia.experience.resource.RunnableRequest#getError()
     */
    public Throwable getError() {
        // don't indicate errors, no need to restart thread.
        return null;
    }

    /**
     * Tries to get an image with the given URL from the file system (RMS).
     *
     * @param type type of the resource
     * @param url url of the image
     * @return byte array with raw data
     */
    private byte[] getFromRMS(final RMSResourceType type, final String url) {
        final RMSRecord record = RMSResourceDB.getInstance().getResource(type, url);

        if (record != null) {

            try {
                // check that resource is valid for the given session id
                // if not, it means the record is too old
                // if sessionId is zero, it never expires

                if (record.getSessionId() == 0 || record.getSessionId() == this.sessionId) {
                    return ((ByteArrayStorableResource) record).getData();
                } else {
                    //Log.log(LOG_TAG, "Ignoring entry from previous session. sessionId: "+ record.getSessionId() +", url: "+ url);
                    ((ByteArrayStorableResource) record).clearData();
                }

            } catch (Exception e) {
                //Log.log(LOG_TAG, "RMS error: ", e);
            }
        }

        return null;
    }
}
