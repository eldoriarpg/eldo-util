/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.utils;

import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static de.eldoria.eldoutilities.utils.Parser.parseTimeToTicks;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParserTest {
    @Test
    void testParseTimeToTicks() {
        OptionalInt ticks = parseTimeToTicks("6:00");
        assertTrue(ticks.isPresent());
        assertEquals(0, ticks.getAsInt());
        ticks = parseTimeToTicks("24:00");
        assertTrue(ticks.isPresent());
        assertEquals(18000, ticks.getAsInt());
    }
}
