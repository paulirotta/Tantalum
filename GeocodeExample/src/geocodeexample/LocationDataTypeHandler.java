/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geocodeexample;

import com.futurice.tantalum4.log.L;
import com.futurice.tantalum4.storage.DataTypeHandler;
import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

/**
 *
 * @author phou
 */
public class LocationDataTypeHandler implements DataTypeHandler {

    public Object convertToUseForm(byte[] bytes) {
        String out ="";
        String s = new String(bytes);
        try {
            JSONObject src = new JSONObject(s);
            JSONArray inn = src.getJSONArray("Placemark");
            JSONObject arr = inn.getJSONObject(1);
            JSONObject d = arr.getJSONObject("Point");
            JSONArray f = d.getJSONArray("coordinates");
            
            out = "Lat: " + f.getString(0) + " & Lon: " + f.getString(1);
        } catch (JSONException ex) {
            L.e("Can not parse JSON", s, ex);
        }
        
        return out;
    }
}
