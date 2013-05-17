package org.tantalum.jme;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.tantalum.MockedStaticInitializers;
import javax.microedition.lcdui.*;
import java.awt.*;
import java.util.Vector;
import javax.microedition.lcdui.Font;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;


/**
 * User: kink
 * Date: 2013.03.26
 * Time: 15:34
 */
@PrepareForTest({JMEFontUtils.class, Font.class})
public class FontUtilsTest extends MockedStaticInitializers {

    static final int DEFAULT_CHARACTER_WIDTH_FOR_FONT = 1;

    private Font font;
    private JMEFontUtils fontUtils;

    @Before
    public final void fontUtilsTestFixture() {
        font = PowerMockito.mock(Font.class);
        fontUtils = JMEFontUtils.getFontUtils(font, "...");
        setupMocks();
    }


    /**
     * Test of testTruncate method, of class StringUtils.
     */
    @Test
    public void testTruncate() {
        final String part1 = "This is a a really long line of text";
        final String part2 = "--------------------------------------------------------------------------------------------------------------------------------";
        final String str_1 = part1 + part2;
        final String ellipsis = "...";
        final String expResult_1 = part1 + ellipsis;
        int maxWidth_1 = expResult_1.length();
        String result_1 = fontUtils.truncate(str_1, maxWidth_1, false);
        assertEquals(expResult_1, result_1);
    }

    /**
     * Mock the font, since it is either not available in the test context, or if it is we cannot still guarantee that
     * the width is known. Hence we use the length of the string as the width, i.e. every character is width 1.
     */
    private void setupMocks() {
        when(font.stringWidth(anyString())).thenAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                return ((String) arguments[0]).length();

            }
        });


        when(font.charWidth(anyChar())).thenReturn(DEFAULT_CHARACTER_WIDTH_FOR_FONT);

    }

    /**
     * Test of testSplitToLines method, of class StringUtils.
     */
    @Test
    public void testSplitToLines() {
        Vector vector_1 = new Vector();
        String text_1 = "This is a a really long line of text";
        int maxWidth_1 = text_1.length() / 3 + 1;

        fontUtils.splitToLines(vector_1, text_1, maxWidth_1, false);
        assertEquals(3, vector_1.size());
    }
}
