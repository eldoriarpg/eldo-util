/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.utils;

import de.eldoria.eldoutilities.container.Pair;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;

/**
 * Utility class with basic math functions which are not covered by java.math
 */
public final class EMath {
    private EMath() {
    }

    /**
     * Clamp a value.
     *
     * @param min   min value
     * @param max   max value
     * @param value value to clamp
     * @return value with a size between min and max
     */
    public static int clamp(int min, int max, int value) {
        return min(max, max(value, min));
    }

    /**
     * Clamp a value.
     *
     * @param min   min value
     * @param max   max value
     * @param value value to clamp
     * @return value with a size between min and max
     */
    public static float clamp(float min, float max, float value) {
        return min(max, max(value, min));
    }

    /**
     * Clamp a value.
     *
     * @param min   min value
     * @param max   max value
     * @param value value to clamp
     * @return value with a size between min and max
     */
    public static long clamp(long min, long max, long value) {
        return min(max, max(value, min));
    }

    /**
     * Clamp a value.
     *
     * @param min   min value
     * @param max   max value
     * @param value value to clamp
     * @return value with a size between min and max
     */
    public static double clamp(double min, double max, double value) {
        return min(max, max(value, min));
    }

    /**
     * Returns the total difference between two numbers
     *
     * @param a first value
     * @param b second value
     * @return the total difference between two numbers
     */
    public static float diff(float a, float b) {
        if (a <= 0 && b <= 0 || a > 0 && b > 0) {
            var values = compareValues(abs(a), abs(b));

            return values.second - values.first;
        }

        return abs(a) + abs(b);
    }

    /**
     * Returns the total difference between two numbers
     *
     * @param a first value
     * @param b second value
     * @return the total difference between two numbers
     */
    public static double diff(double a, double b) {
        if (a <= 0 && b <= 0 || a > 0 && b > 0) {
            var values = compareValues(abs(a), abs(b));

            return values.second - values.first;
        }

        return abs(a) + abs(b);
    }


    /**
     * Returns the total difference between two numbers
     *
     * @param a first value
     * @param b second value
     * @return the total difference between two numbers
     */
    public static int diff(int a, int b) {
        if ((a <= 0) == (b <= 0)) {
            var values = compareValues(abs(a), abs(b));

            return values.second - values.first;
        }

        return abs(a) + abs(b);
    }

    /**
     * Compare two values
     *
     * @param a first value
     * @param b second value
     * @return Pair of values. smaller value is first.
     */
    public static Pair<Integer, Integer> compareValues(int a, int b) {
        int small;
        int large;

        if (abs(a) > abs(b)) {
            large = a;
            small = b;
        } else {
            large = b;
            small = a;
        }

        return Pair.of(small, large);
    }


    /**
     * Compare two values
     *
     * @param a first value
     * @param b second value
     * @return Pair of values. smaller value is first.
     */
    public static Pair<Float, Float> compareValues(float a, float b) {
        float small;
        float large;

        if (abs(a) > abs(b)) {
            large = a;
            small = b;
        } else {
            large = b;
            small = a;
        }

        return Pair.of(small, large);
    }

    /**
     * Compare two values
     *
     * @param a first value
     * @param b second value
     * @return Pair of values. smaller value is first.
     */
    public static Pair<Double, Double> compareValues(double a, double b) {
        double small;
        double large;

        if (abs(a) > abs(b)) {
            large = a;
            small = b;
        } else {
            large = b;
            small = a;
        }

        return Pair.of(small, large);
    }

    /**
     * Method to get a point from a parabola.
     * <p>
     * Creates a parabola from a point and the vertex of a parabola.
     * <p>
     *
     * @param xVector X Value of the Vector
     * @param yVector Y Value of the Vector
     * @param xPoint  x Value of the Point
     * @param yPoint  Y Value of the Point
     * @param x       The Value of x in f(x);
     * @return Returns the value of f(x) at the specified point.
     */
    public static double parabolaValue(double xVector, double yVector, double xPoint, double yPoint, double x) {
        return (yPoint - yVector) / pow(xPoint - xVector, 2) * pow(x - xVector, 2) + yVector;
    }


    /**
     * Returns a smooth curve created with two opposite parabolas.
     * <p>
     * The slope at the start and end is 0.
     * <p>
     * The curve is generated by a start and endpoint.
     * <p>
     * The x value of the start point must be smaller than the end point
     * <p>
     * Returns always a value between half of the height and 0;
     *
     * @param x The Point x between the start and end point.
     * @return Returns a value between 0 and 1.
     */
    public static double smoothCurveValue(double x) {
        return smoothCurveValue(Pair.of(0d, 0d), Pair.of(1d, 1d), clamp(0, 1, x));
    }

    /**
     * Returns a smooth curve created with two opposite parabolas.
     * <p>
     * The slope at the start and end is 0.
     * <p>
     * The curve is generated by a start and endpoint.
     * <p>
     * The x value of the start point must be smaller than the end point
     *
     * @param start The start point as a tuple of two floats
     * @param end   The end point as a tuple of two floats
     * @param x     The Point x between the start end end point.
     * @return Returns a float which contains the value of the curve at the point x
     */
    public static double smoothCurveValue(Pair<Double, Double> start, Pair<Double, Double> end, double x) {
        var length = diff(start.first, end.first);
        var height = diff(start.second, end.second);
        var lengthHalf = start.first + length / 2;
        double heightHalf;

        var type = start.second < end.second ? CurveType.ASCENDING : CurveType.DESCENDING;

        switch (type) {
            case ASCENDING:
                heightHalf = start.second + height / 2;

                if (x < lengthHalf) {
                    return parabolaValue(start.first, start.second, lengthHalf, heightHalf, x);
                }

                return parabolaValue(end.first, end.second, lengthHalf, heightHalf, x);
            case DESCENDING:
                heightHalf = end.second + height / 2;

                if (x < lengthHalf) {
                    return parabolaValue(start.first, start.second, lengthHalf, heightHalf, x);
                }

                return parabolaValue(end.first, end.second, lengthHalf, heightHalf, x);
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    private enum CurveType {
        ASCENDING, DESCENDING
    }
}
