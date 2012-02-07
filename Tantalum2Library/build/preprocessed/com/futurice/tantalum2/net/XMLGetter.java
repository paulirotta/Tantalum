/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2.net;

/**
 *
 * @author pahought
 */
public class XMLGetter /*extends HttpGetter*/ {

    protected final XMLModel xmlvo;

    public XMLGetter(final String url, final int retriesRemaining, final XMLModel xmlvo) {
        //super(url, retriesRemaining);

        this.xmlvo = xmlvo;
    }

    public boolean work() {
        /*if (super.work()) {
            try {
                xmlvo.setXML(new String((byte[]) getResult()));

                return true;
            } catch (Exception e) {
                Log.logThrowable(e, "XMLGetter HTTP response problem at " + getUrl());
                this.exception(e);
            }
        }*/

        return false;
    }
}
