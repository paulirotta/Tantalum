/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.tantalum3;

/**
 * A queued Task blew an error during execution, thus it has no return value.
 * 
 * @author phou
 */
public class ExecutionException extends Exception {
    public ExecutionException(String s) {
        super(s);
    }
    
    public ExecutionException() {
        super();
    }
}
