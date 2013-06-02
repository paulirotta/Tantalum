/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.jme;

/**
 * Useful networking utilities, for Nokia phones only.
 *
 * @author phou
 */
public final class JMENetUtils {

    /**
     * Either the network is not available, or the phone can not determine the
     * network availability, or (may vary by device) the phone can not determine
     * the network availability yet because you have have yet used the network.
     */
    public static final int PACKET_DATA_UNKNOWN = 0;
    /**
     * Packet data in a GSM network (2G)
     */
    public static final int PACKET_DATA_GSM = 1;
    /**
     * Packet data in an EDGE network (2.5G, same frequencies as GSM but with
     * better software in the cell network allowing somewhat faster data rates)
     */
    public static final int PACKET_DATA_EDGE = 2;
    /**
     * Packet data in a 3G network. Generally a pretty fast connection except in
     * crowds and bad weather.
     */
    public static final int PACKET_DATA_3G = 3;
    /**
     * Packet data in a 3G HSDPA network or faster (Usually 3.5G, same
     * frequencies as 3G but with better software in the cell network allowing
     * for faster downstream data)
     */
    public static final int PACKET_DATA_HSDPA = 4;
    /**
     * Packet data in a circuit-switched data connection (like an old-school
     * model telephone call over the mobile network). With this type of
     * connection you are generally charged by the time, not data usage. Usually
     * the connection is on an slower network such as GSM, but it may be a high
     * speed circuit switched (HSCSD) connection.
     */
    public static final int PACKET_DATA_CSD = 5;
    /**
     * Packet data over a Bluetooth Personal Area Network (PAN). There is no
     * reasonable guess about the network speed of the network shared- it could
     * be fast or slow.
     */
    public static final int PACKET_DATA_PAN = 6;
    /**
     * Packet data over a WIFI or WIMAX network. Usually this is a reasonably
     * fast network delivered by 802.11b or 802.11G, but it may be a slow or
     * medium speed cell data network as above shared from a mobile phone.
     */
    public static final int PACKET_DATA_WLAN = 7;
    private static final String[] NET_STRINGS = {"pd", "pd.EDGE", "pd.3G", "pd.HSDPA", "csd", "bt_pan", "wlan"};
    private static final int[] NET_STATES = {PACKET_DATA_GSM,
        PACKET_DATA_EDGE,
        PACKET_DATA_3G,
        PACKET_DATA_HSDPA,
        PACKET_DATA_CSD,
        PACKET_DATA_PAN,
        PACKET_DATA_WLAN};

    /**
     * Return an integer constant indicating the type of data network the phone
     * is attached to.
     *
     * @return
     */
    public static int getCurrentDataNetwork() {
        final String s = System.getProperty("com.nokia.network.access");
        int state = PACKET_DATA_UNKNOWN;

        for (int i = 0; i < NET_STRINGS.length; i++) {
            if (equals(s, NET_STRINGS[i])) {
                state = NET_STATES[i];
                break;
            }
        }

        return state;
    }

    private static boolean equals(final String s1, final String s2) {
        if (s1 == null && s2 == null) {
            return true;
        }
        if (s1 == null) {
            return false;
        }
        return s1.equals(s2);
    }
}
