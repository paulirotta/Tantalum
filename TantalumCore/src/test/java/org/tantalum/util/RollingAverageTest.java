/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.util;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.tantalum.MockedStaticInitializers;

/**
 *
 * @author phou
 */
public class RollingAverageTest extends MockedStaticInitializers {

    @Test(expected = IllegalArgumentException.class)
    public void badInitWindow() {
        RollingAverage rollingAverage = new RollingAverage(0, 10.0f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badInitValueNaN() {
        RollingAverage rollingAverage = new RollingAverage(0, Float.NaN);
    }

    @Test
    public void firstGet() {
        RollingAverage rollingAverage = new RollingAverage(10, 10.0f);
        assertEquals(10.f, rollingAverage.value(), 0.000001f);
    }

    @Test
    public void secondGet() {
        RollingAverage rollingAverage = new RollingAverage(2, 10.0f);
        rollingAverage.update(20.f);
        assertEquals(15.f, rollingAverage.value(), 0.000001f);
    }

    @Test
    public void repeatGet() {
        RollingAverage rollingAverage = new RollingAverage(2, 10.0f);
        for (int i = 0; i < 1000; i++) {
            rollingAverage.update(-100.f);
        }
        assertEquals(-100.f, rollingAverage.value(), 0.01f);
    }
}
