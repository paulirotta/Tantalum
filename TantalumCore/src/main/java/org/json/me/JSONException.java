package org.json.me;

/**
 * The JSONException is thrown by the JSON.org classes then things are amiss.
 * @author JSON.org
 * @version 2
 */
public final class JSONException extends Exception {
    private Throwable cause;

    /**
     * Constructs a JSONException with an explanatory message.
     * @param message Detail about the reason for the exception.
     */
    public JSONException(final String message) {
        super(message);
    }

    /**
     * 
     * @param t 
     */
    public JSONException(final Throwable t) {
        super(t.getMessage());
        this.cause = t;
    }

    /**
     * 
     * @return 
     */
    public Throwable getCause() {
        return this.cause;
    }
}
