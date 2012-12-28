/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package android.app;

import android.content.Context;
import android.os.Bundle;

/**
 * Stub class to aid in cross-platform build and obfuscation
 * 
 * @author phou
 */
public class Activity {

    /**
     * 
     * @param savedInstanceState 
     */
    protected void onCreate(Bundle savedInstanceState) {
    }

    /**
     * 
     */
    protected void onDestroy() {
    }

    /**
     * 
     * @param action 
     */
    public void runOnUiThread(Runnable action) {
    }

    /**
     * 
     */
    public void finish() {
    }

    /**
     * 
     * @return 
     */
    public Context getApplicationContext() {
        return null;
    }
}
