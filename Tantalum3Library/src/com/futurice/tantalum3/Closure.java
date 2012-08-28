package com.futurice.tantalum3;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * A convenience class for creating anonymous inner classes which first
 * perform an action on a Worker thread, then notify the user on the EDT
 * 
 * @author phou
 */
public abstract class Closure implements Task, Runnable{
}
