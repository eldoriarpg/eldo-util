/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.utils;

import de.eldoria.eldoutilities.container.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static de.eldoria.eldoutilities.utils.EMath.clamp;
import static de.eldoria.eldoutilities.utils.EMath.parabolaValue;
import static de.eldoria.eldoutilities.utils.EMath.smoothCurveValue;

public class EMathTest {
    @Test
    public void clampTest() {
        Assertions.assertEquals(100, clamp(0, 100, 101));
        Assertions.assertEquals(0, clamp(0, 100, -1));
        Assertions.assertEquals(50, clamp(0, 100, 50));
    }

    @Test
    public void parabolaValueTest() {
        // up
        double rightUp = parabolaValue(0, 0, 1, 1, 1);
        double leftUp = parabolaValue(0, 0, 1, 1, -1);
        Assertions.assertEquals(rightUp, leftUp);
        Assertions.assertEquals(1, leftUp);
        Assertions.assertNotEquals(2, parabolaValue(0, 0, 1, 1, 2));
        //down
        double rightDown = parabolaValue(0, 0, -1, -1, 1);
        double leftDown = parabolaValue(0, 0, -1, -1, -1);
        Assertions.assertEquals(rightDown, leftDown);
        Assertions.assertEquals(-1, leftDown);
        Assertions.assertNotEquals(-2, parabolaValue(0, 0, 1, 1, 2));
    }

    @Test
    public void smoothCurveValueTest() {
        // Simple tests
        Assertions.assertEquals(0, smoothCurveValue(0));
        Assertions.assertEquals(1, smoothCurveValue(1));
        Assertions.assertEquals(0.5, smoothCurveValue(0.5));
        Assertions.assertNotEquals(0.25, smoothCurveValue(0.25));
        Assertions.assertNotEquals(0.75, smoothCurveValue(0.75));

        // Complex tests
        Pair<Double, Double> neg = Pair.of(-1d, -1d);
        Pair<Double, Double> pos = Pair.of(1d, 1d);
        // Descending curve
        Assertions.assertEquals(0, smoothCurveValue(neg, pos, 0));
        Assertions.assertTrue(smoothCurveValue(neg, pos, -0.5) < -0.5);
        Assertions.assertTrue(smoothCurveValue(neg, pos, 0.5) > 0.5);

        // Ascending curve
        neg = Pair.of(1d, -1d);
        pos = Pair.of(-1d, 1d);
        Assertions.assertEquals(0, smoothCurveValue(pos, neg, 0));
        Assertions.assertTrue(smoothCurveValue(pos, neg, -0.5) > 0.6);
        Assertions.assertTrue(smoothCurveValue(pos, neg, 0.5) < -0.6);
    }

    @Test
    public void diffTest() {
        Assertions.assertEquals(2, EMath.diff(-1,1));
        Assertions.assertEquals(1, EMath.diff(0,1));
        Assertions.assertEquals(0, EMath.diff(0,0));
        Assertions.assertEquals(3, EMath.diff(-4,-1));
        Assertions.assertEquals(3, EMath.diff(4,1));
        Assertions.assertEquals(150, EMath.diff(-50,100));
    }
}
