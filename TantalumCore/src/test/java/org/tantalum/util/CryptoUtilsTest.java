/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.util;

import java.io.UnsupportedEncodingException;
import java.security.DigestException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.tantalum.MockedStaticInitializers;

/**
 *
 * @author phou
 */
public class CryptoUtilsTest extends MockedStaticInitializers {

    String imageUrl1 = "http://1.maps.nlp.nokia.com/maptile/2.1/maptile/newest/normal.day/11/1099/675/128/png8?app_id=Db84BEVKmbJGsBiRR_RJ&token=FzPD8qiiOKt9ZDmy7jCtEQ&";
    String imageUrl3 = "http://2.maps.nlp.nokia.com/maptile/2.1/maptile/newest/normal.day/11/1105/674/128/png8?app_id=Db84BEVKmbJGsBiRR_RJ&token=FzPD8qiiOKt9ZDmy7jCtEQ&";
    String imageUrl2 = "http://1.maps.nlp.nokia.com/maptile/2.1/maptile/newest/normal.day/11/1098/675/128/png8?app_id=Db84BEVKmbJGsBiRR_RJ&token=FzPD8qiiOKt9ZDmy7jCtEQ&";
    String boodle = "boodle";
    static CryptoUtils cryptoUtils;

    @Before
    public final void getSingleton() {
        cryptoUtils = CryptoUtils.getInstance();
    }

    @Test
    public void boodleLongsAreNotTheSameTest() throws DigestException, UnsupportedEncodingException {
        assertNotEquals("boodle is not a url", cryptoUtils.toDigest(imageUrl1), cryptoUtils.toDigest(boodle));
    }

    @Test
    public void almostSameLongsAreNotTheSameTest() throws DigestException, UnsupportedEncodingException {
        assertNotEquals(cryptoUtils.toDigest(imageUrl1), cryptoUtils.toDigest(imageUrl2));
    }

    @Test
    public void statelessSequenceTest() throws DigestException, UnsupportedEncodingException {
        long l = cryptoUtils.toDigest(imageUrl2);
        cryptoUtils.toDigest("wahhh");
        assertEquals(l, cryptoUtils.toDigest(imageUrl2));
    }

    // These values might produce same long according to system tests (verifying...)
    @Test
    public void magicValueTest() throws DigestException, UnsupportedEncodingException {
        assertEquals(1683574533465955905l, cryptoUtils.toDigest(imageUrl3));
    }

    // These values might produce same long according to system tests (verifying...)
    @Test
    public void magicValuesAreNotTheSameTest() throws DigestException, UnsupportedEncodingException {
        assertNotEquals(cryptoUtils.toDigest(imageUrl1), cryptoUtils.toDigest(imageUrl3));
    }

    @Test
    public void spaceIsNotEqualEmptyStringTest() throws DigestException, UnsupportedEncodingException {
        assertNotEquals(cryptoUtils.toDigest(" "), cryptoUtils.toDigest(""));
    }

    @Test(expected = NullPointerException.class)
    public void nullStringTest() throws DigestException, UnsupportedEncodingException {
        cryptoUtils.toDigest((String) null);
    }

    @Test(expected = NullPointerException.class)
    public void nullByteArrayTest() throws DigestException, UnsupportedEncodingException {
        cryptoUtils.toDigest((byte[]) null);
    }
}
