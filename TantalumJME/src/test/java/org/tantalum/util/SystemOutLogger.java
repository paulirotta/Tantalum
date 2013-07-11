package org.tantalum.util;

/**
 * User: kink
 * Date: 2013.04.02
 * Time: 11:57
 */
public class SystemOutLogger extends L {
    protected void printMessage(StringBuffer sb, Throwable t) {
        System.out.println("Logger called: " + sb.toString());
        if (t != null) {
            t.printStackTrace();
        }
    }

    protected void close() {
        // Nothing to do
    }
}
