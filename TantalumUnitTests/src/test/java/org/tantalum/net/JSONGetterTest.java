/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.net;

import java.io.IOException;
import org.json.me.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.tantalum.MockedStaticInitializers;
import org.tantalum.PlatformUtils;
import org.tantalum.Task;
import org.tantalum.net.json.JSONGetter;
import org.tantalum.net.json.JSONModel;
import org.tantalum.util.L;

/**
 *
 * @author phou
 */
public class JSONGetterTest extends MockedStaticInitializers {

    static String SAMPLE_DATA = "{\n"
            + "    \"glossary\": {\n"
            + "        \"title\": \"example glossary\",\n"
            + "		\"GlossDiv\": {\n"
            + "            \"title\": \"S\",\n"
            + "			\"GlossList\": {\n"
            + "                \"GlossEntry\": {\n"
            + "                    \"ID\": \"SGML\",\n"
            + "					\"SortAs\": \"SGML\",\n"
            + "					\"GlossTerm\": \"Standard Generalized Markup Language\",\n"
            + "					\"Acronym\": \"SGML\",\n"
            + "					\"Abbrev\": \"ISO 8879:1986\",\n"
            + "					\"GlossDef\": {\n"
            + "                        \"para\": \"A meta-markup language, used to create markup languages such as DocBook.\",\n"
            + "						\"GlossSeeAlso\": [\"GML\", \"XML\"]\n"
            + "                    },\n"
            + "					\"GlossSee\": \"markup\"\n"
            + "                }\n"
            + "            }\n"
            + "        }\n"
            + "    }\n"
            + "}";
    boolean cancelCalled;
    MyFailedTestJSONGetter jsonGetter;
    PlatformUtils.HttpConn httpConn;

    @Before
    public final void jsonGetterTestFixture() throws JSONException {
        createMocks();
        cancelCalled = false;
        jsonGetter = new JSONGetterTest.MyFailedTestJSONGetter();
    }

    private void createMocks() {
        PowerMockito.mockStatic(L.class);
        httpConn = Mockito.mock(PlatformUtils.HttpConn.class);
    }

    @Test
    public void networkInterruptionDoesNotBlowTheModel() throws IOException, JSONException {
        jsonGetter.model.setJSON(SAMPLE_DATA);
    }

    private class MyFailedTestJSONGetter extends JSONGetter {

        final JSONModel model;

        MyFailedTestJSONGetter() {
            super(new JSONModel(), Task.HIGH_PRIORITY);

            model = super.jsonModel;
        }

        @Override
        public Object exec(Object in) {
            super.exec(in);
            cancel(false, "Test net cancel");
            
            return null;
        }
    }
}
