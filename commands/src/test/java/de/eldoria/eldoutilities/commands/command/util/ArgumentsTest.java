/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.commands.command.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ArgumentsTest {
    @Test
    void rawKeepsEverythingAfterAFlag() {
        var raw = new String[]{"stone", "-incair", "tree", "-place:drop"};
        var args = Arguments.create(null, null, raw);

        assertEquals(1, args.size());
        assertArrayEquals(raw, args.raw());
    }

    @Test
    void rawIsACopy() {
        var args = Arguments.create(null, null, new String[]{"stone"});
        args.raw()[0] = "changed";

        assertEquals("stone", args.raw()[0]);
    }
}
