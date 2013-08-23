/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tantalum.util;

/**
 * A simple tracking filter. A stream of values are fed to the filter, and the
 * output is a smoothed average of recent values.
 *
 * @author phou
 */
public final class RollingAverage {

    private float average;
    private final int windowLength;
    private int window;
    private float upperBound = Float.MAX_VALUE;
    private float lowerBound = -Float.MAX_VALUE;

    /**
     * Create a new rolling average filter. This will slowly track
     * <code>update(value)</code> values fed to it. The longer the window, the
     * more slow the slew rate (rate of change).
     *
     * @param windowLength
     * @param initialValue
     */
    public RollingAverage(final int windowLength, final float initialValue) {
        if (windowLength < 2) {
            throw new IllegalArgumentException("RollingAverage window length must be at least 2: " + windowLength);
        }
        if (Float.isNaN(initialValue)) {
            throw new IllegalArgumentException("RollingAverage initial value must be a rational float: " + initialValue);
        }

        this.windowLength = windowLength;
        reset(initialValue);
    }

    /**
     * The average is hard limited to never be more than an optional upper value
     * 
     * @param upperBound 
     */
    public void setUpperBound(final float upperBound) {
        if (upperBound < lowerBound) {
            throw new IllegalArgumentException("Upper bound " + upperBound + " must be greater than lower bound " + lowerBound);
        }
        
        this.upperBound = upperBound;
    }

    /**
     * The average is hard limited to never be less than an optional lower value
     * 
     * @param lowerBound 
     */
    public void setLowerBound(final float lowerBound) {
        if (upperBound < lowerBound) {
            throw new IllegalArgumentException("Lower bound " + lowerBound + " must be less than upper bound " + upperBound);
        }

        this.lowerBound = lowerBound;
    }

    /**
     * Get the current average of recent update() values
     *
     * @return
     */
    public float value() {
        return average;
    }

    /**
     * Shift the average by adding one more data point.
     *
     * The update can be calculated as:
     * <pre>
     *
     * <code>average = (average*window + value) / ++window</code></pre>
     *
     * where
     * <code>window</code> starts at 1 and increases until the
     * <code>windowLength</code> is reached. This is roughly equivalent to the
     * average of the last
     * <code>windowLength</code> values but faster to calculate.
     *
     * @param value
     * @return
     */
    public float update(final float value) {
        average = (average * window + value) / ++window;
        average = Math.max(average, lowerBound);
        average = Math.min(average, upperBound);

        if (window == windowLength) {
            window--;
        }

        return average;
    }

    /**
     * Hard jump the value and clear the window. This is equivalent to creating
     * a new RollingAverage
     *
     * @param value
     * @return
     */
    public float reset(final float value) {
        window = 1;
        average = value;

        return value;
    }
}
