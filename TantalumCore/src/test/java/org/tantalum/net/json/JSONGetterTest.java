package org.tantalum.net.json;

import java.io.IOException;
import org.json.me.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.tantalum.MockedStaticInitializers;
import org.tantalum.PlatformUtils;
import main.java.org.tantalum.Task;
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
    
    final static String SAMPLE_DATA_2 = "{\"items\":[{\"category\":{\"id\":\"Artist\",\"name\":\"Artist\"},\"genres\":[{\"id\":\"Pop\",\"name\":\"Pop\"}],\"name\":\"Oldřich Říha\",\"sortname\":\"Oldřich Říha\",\"storeuri\":\"http://music.nokia.com/us/r/artist/-/17519228/\",\"id\":\"17519228\",\"type\":\"musicartist\"},{\"category\":{\"id\":\"Track\",\"name\":\"Track\"},\"genres\":[{\"id\":\"World\",\"name\":\"World\"}],\"name\":\"Riha laambar\",\"takenfrom\":{\"name\":\"Meilleurs tubes de dance de la chanson Kabyle (100% Live)\",\"id\":\"42305839\"},\"prices\":{\"permanentdownload\":{\"value\":0.99,\"currency\":\"USD\"}},\"thumbnails\":{\"50x50\":\"http://4.musicimg.ovi.com/u/1.0/image/306534076/?w=50&q=40\",\"100x100\":\"http://4.musicimg.ovi.com/u/1.0/image/306534076/?w=100&q=70\",\"200x200\":\"http://4.musicimg.ovi.com/u/1.0/image/306534076/?w=200&q=90\",\"320x320\":\"http://4.musicimg.ovi.com/u/1.0/image/306534076/?w=320&q=90\"},\"creators\":{\"performers\":[{\"name\":\"Larvi Youva\",\"id\":\"42305113\"}]},\"storeuri\":\"http://music.nokia.com/us/r/product/-/-/42305853/\",\"samples\":{\"wmamms\":\"mms://wmstream.music.ovi.com/a1139/l1/1.0/s/clips/mc/musicus/42305853/2/42305853.wma\",\"mp3rtmp\":\"rtmp://stream.music.ovi.com/a1139/l1/mp3:1.0/s/clips/pc/musicus/42305853/2/42305853.mp3\"},\"id\":\"42305853\",\"type\":\"musictrack\"},{\"category\":{\"id\":\"Track\",\"name\":\"Track\"},\"genres\":[{\"id\":\"Soundtrack\",\"name\":\"Soundtrack\"}],\"name\":\"Hari Riha\",\"takenfrom\":{\"name\":\"Hari Hari Jayadeva Ashtapadhi\",\"id\":\"1692477\"},\"prices\":{\"permanentdownload\":{\"value\":0.69,\"currency\":\"USD\"}},\"thumbnails\":{\"50x50\":\"http://4.musicimg.ovi.com/u/1.0/image/11001660/?w=50&q=40\",\"100x100\":\"http://4.musicimg.ovi.com/u/1.0/image/11001660/?w=100&q=70\",\"200x200\":\"http://4.musicimg.ovi.com/u/1.0/image/11001660/?w=200&q=90\",\"320x320\":\"http://4.musicimg.ovi.com/u/1.0/image/11001660/?w=320&q=90\"},\"creators\":{\"performers\":[{\"name\":\"Sudha Ragunathan\",\"id\":\"120945\"}]},\"storeuri\":\"http://music.nokia.com/us/r/product/-/-/1692480/\",\"samples\":{\"wmamms\":\"mms://wmstream.music.ovi.com/a1139/l1/1.0/s/clips/mc/musicus/1692480/25/1692480.wma\",\"mp3rtmp\":\"rtmp://stream.music.ovi.com/a1139/l1/mp3:1.0/s/clips/pc/musicus/1692480/25/1692480.mp3\"},\"id\":\"1692480\",\"type\":\"musictrack\"},{\"category\":{\"id\":\"Track\",\"name\":\"Track\"},\"genres\":[{\"id\":\"World\",\"name\":\"World\"}],\"name\":\"Hari Riha\",\"takenfrom\":{\"name\":\"Hari Hari Jayadeva Ashtapadhi - Sudha Ragunathan\",\"id\":\"11527310\"},\"prices\":{\"permanentdownload\":{\"value\":0.99,\"currency\":\"USD\"}},\"thumbnails\":{\"50x50\":\"http://4.musicimg.ovi.com/u/1.0/image/160809721/?w=50&q=40\",\"100x100\":\"http://4.musicimg.ovi.com/u/1.0/image/160809721/?w=100&q=70\",\"200x200\":\"http://4.musicimg.ovi.com/u/1.0/image/160809721/?w=200&q=90\",\"320x320\":\"http://4.musicimg.ovi.com/u/1.0/image/160809721/?w=320&q=90\"},\"creators\":{\"performers\":[{\"name\":\"Sudha Ragunathan\",\"id\":\"120945\"}]},\"storeuri\":\"http://music.nokia.com/us/r/product/-/-/11527313/\",\"samples\":{\"wmamms\":\"mms://wmstream.music.ovi.com/a1139/l1/1.0/s/clips/mc/musicus/11527313/13/11527313.wma\",\"mp3rtmp\":\"rtmp://stream.music.ovi.com/a1139/l1/mp3:1.0/s/clips/pc/musicus/11527313/13/11527313.mp3\"},\"id\":\"11527313\",\"type\":\"musictrack\"},{\"category\":{\"id\":\"Artist\",\"name\":\"Artist\"},\"genres\":[{\"id\":\"Pop\",\"name\":\"Pop\"}],\"name\":\"Oldřich Říha, Katapult\",\"sortname\":\"Oldřich Říha, Katapult\",\"storeuri\":\"http://music.nokia.com/us/r/artist/-/19359676/\",\"id\":\"19359676\",\"type\":\"musicartist\"},{\"category\":{\"id\":\"Artist\",\"name\":\"Artist\"},\"genres\":[{\"id\":\"Pop\",\"name\":\"Pop\"}],\"name\":\"Oldřich Říha, Jiří Šindelář\",\"sortname\":\"Oldřich Říha, Jiří Šindelář\",\"storeuri\":\"http://music.nokia.com/us/r/artist/-/20666729/\",\"id\":\"20666729\",\"type\":\"musicartist\"},{\"category\":{\"id\":\"Artist\",\"name\":\"Artist\"},\"genres\":[{\"id\":\"Classical\",\"name\":\"Classical\"}],\"name\":\"Vladimir Ríha, Smetana Quartet\",\"sortname\":\"Vladimir Ríha, Smetana Quartet\",\"storeuri\":\"http://music.nokia.com/us/r/artist/-/11241742/\",\"id\":\"11241742\",\"type\":\"musicartist\"},{\"category\":{\"id\":\"Track\",\"name\":\"Track\"},\"genres\":[{\"id\":\"World\",\"name\":\"World\"}],\"name\":\"Saari Raat Chounda Riha\",\"takenfrom\":{\"name\":\"Master Dholl Blaster\",\"id\":\"5930676\"},\"prices\":{\"permanentdownload\":{\"value\":0.99,\"currency\":\"USD\"}},\"thumbnails\":{\"50x50\":\"http://4.musicimg.ovi.com/u/1.0/image/176939438/?w=50&q=40\",\"100x100\":\"http://4.musicimg.ovi.com/u/1.0/image/176939438/?w=100&q=70\",\"200x200\":\"http://4.musicimg.ovi.com/u/1.0/image/176939438/?w=200&q=90\",\"320x320\":\"http://4.musicimg.ovi.com/u/1.0/image/176939438/?w=320&q=90\"},\"creators\":{\"performers\":[{\"name\":\"Ks Bhamrah Of Apna Sangeet\",\"id\":\"1304799\"}]},\"storeuri\":\"http://music.nokia.com/us/r/product/-/-/5930681/\",\"samples\":{\"wmamms\":\"mms://wmstream.music.ovi.com/a1139/l1/1.0/s/clips/mc/musicus/5930681/32/5930681.wma\",\"mp3rtmp\":\"rtmp://stream.music.ovi.com/a1139/l1/mp3:1.0/s/clips/pc/musicus/5930681/32/5930681.mp3\"},\"id\":\"5930681\",\"type\":\"musictrack\"},{\"category\":{\"id\":\"Track\",\"name\":\"Track\"},\"genres\":[{\"id\":\"World\",\"name\":\"World\"}],\"name\":\"Dhol Vajjda Riha\",\"takenfrom\":{\"name\":\"Dil Te Na Laeen\",\"id\":\"16449340\"},\"prices\":{\"permanentdownload\":{\"value\":0.99,\"currency\":\"USD\"}},\"thumbnails\":{\"50x50\":\"http://4.musicimg.ovi.com/u/1.0/image/180768413/?w=50&q=40\",\"100x100\":\"http://4.musicimg.ovi.com/u/1.0/image/180768413/?w=100&q=70\",\"200x200\":\"http://4.musicimg.ovi.com/u/1.0/image/180768413/?w=200&q=90\",\"320x320\":\"http://4.musicimg.ovi.com/u/1.0/image/180768413/?w=320&q=90\"},\"creators\":{\"performers\":[{\"name\":\"Manmohan Waris\",\"id\":\"89835\"}]},\"storeuri\":\"http://music.nokia.com/us/r/product/-/-/16449343/\",\"samples\":{\"wmamms\":\"mms://wmstream.music.ovi.com/a1139/l1/1.0/s/clips/mc/musicus/16449343/7/16449343.wma\",\"mp3rtmp\":\"rtmp://stream.music.ovi.com/a1139/l1/mp3:1.0/s/clips/pc/musicus/16449343/7/16449343.mp3\"},\"id\":\"16449343\",\"type\":\"musictrack\"},{\"category\":{\"id\":\"Track\",\"name\":\"Track\"},\"genres\":[{\"id\":\"World\",\"name\":\"World\"}],\"name\":\"Gharda Riha Na Munda\",\"takenfrom\":{\"name\":\"Husan Diye Sarkare\",\"id\":\"7148739\"},\"prices\":{\"permanentdownload\":{\"value\":0.99,\"currency\":\"USD\"}},\"thumbnails\":{\"50x50\":\"http://4.musicimg.ovi.com/u/1.0/image/76472533/?w=50&q=40\",\"100x100\":\"http://4.musicimg.ovi.com/u/1.0/image/76472533/?w=100&q=70\",\"200x200\":\"http://4.musicimg.ovi.com/u/1.0/image/76472533/?w=200&q=90\",\"320x320\":\"http://4.musicimg.ovi.com/u/1.0/image/76472533/?w=320&q=90\"},\"creators\":{\"performers\":[{\"name\":\"Kumar Sanjay\",\"id\":\"261806\"}]},\"storeuri\":\"http://music.nokia.com/us/r/product/-/-/7148746/\",\"samples\":{\"wmamms\":\"mms://wmstream.music.ovi.com/a1139/l1/1.0/s/clips/mc/musicus/7148746/26/7148746.wma\",\"mp3rtmp\":\"rtmp://stream.music.ovi.com/a1139/l1/mp3:1.0/s/clips/pc/musicus/7148746/26/7148746.mp3\"},\"id\":\"7148746\",\"type\":\"musictrack\"}],\"facets\":[{\"field\":\"category\",\"values\":[{\"id\":\"Album\",\"name\":\"Album\",\"count\":2},{\"id\":\"Artist\",\"name\":\"Artist\",\"count\":6},{\"id\":\"Track\",\"name\":\"Track\",\"count\":47}]}],\"paging\":{\"total\":55,\"startindex\":0,\"itemsperpage\":10},\"type\":\"itemlist\"}";
    
    boolean cancelCalled;
    MyFailedTestJSONGetter myFailedTestJSONGetter;
    PlatformUtils.HttpConn httpConn;

    @Before
    public final void jsonGetterTestFixture() throws JSONException {
        createMocks();
        cancelCalled = false;
        myFailedTestJSONGetter = new JSONGetterTest.MyFailedTestJSONGetter();
    }

    private void createMocks() {
        PowerMockito.mockStatic(L.class);
        httpConn = Mockito.mock(PlatformUtils.HttpConn.class);
    }

    @Test
    public void networkInterruptionDoesNotBlowTheModel() throws IOException, JSONException {
        myFailedTestJSONGetter.model.setJSON(SAMPLE_DATA);
    }

    @Test
    public void oddParsingOfTheModel() throws IOException, JSONException {
        final JSONModel jsonModel = new JSONModel();
        jsonModel.setJSON(SAMPLE_DATA);
    }

    private class MyFailedTestJSONGetter extends JSONGetter {

        final JSONModel model;

        MyFailedTestJSONGetter() {
            super(Task.HIGH_PRIORITY, new JSONModel());

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
