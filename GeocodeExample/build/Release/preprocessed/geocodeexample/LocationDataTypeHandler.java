/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geocodeexample;

import com.futurice.tantalum4.storage.DataTypeHandler;

/**
 *
 * @author phou
 */
public class LocationDataTypeHandler implements DataTypeHandler {

    public Object convertToUseForm(byte[] bytes) {
        String s = new String(bytes);
        
        return s;
    }
}
