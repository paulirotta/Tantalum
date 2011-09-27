/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum2;

/**
 *
 * @author tsaa
 */
public class Resource {

    private boolean isInRMS;
    private Object o;
    private String url;
    private String key;

    public Resource(String key, String url) {
    }

    public boolean isInRMS() {
        return isInRMS;
    }

    public Object getObject() {
        return o;
    }
}
