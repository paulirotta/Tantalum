/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package geocodeexample;

import com.futurice.tantalum4.PlatformUtils;
import com.futurice.tantalum4.Task;
import com.futurice.tantalum4.net.HttpPoster;
import com.futurice.tantalum4.net.json.JSONModel;

/**
 *
 * @author phou
 */
public class LocationPusher {

    public static void push(String url, JSONModel jsonModel) {
        Task task = new HttpPoster(url, 1, jsonModel.toString().getBytes()) {
            public Object doInBackground(Object in) {
                in = super.doInBackground(in);

                PlatformUtils.runOnUiThread(new Runnable() {
                    public void run() {
                        //TODO Tell the user post succeeded
                    }
                });
                return in;
            }

            protected void onCanceled() {
                //TODO Tell the usre post failed
            }
        };
        task.fork();
    }
}
