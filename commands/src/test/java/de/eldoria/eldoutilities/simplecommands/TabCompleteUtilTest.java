/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.simplecommands;

import de.eldoria.eldoutilities.commands.Completion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class TabCompleteUtilTest {

    @Test
    void completeMaterial() {
        List<String> result = Completion.completeMaterial("gp", true);
        Assertions.assertTrue(result.contains("glass_pane"));
        result = Completion.completeMaterial("pane", true);
        Assertions.assertTrue(result.contains("glass_pane"));
        result = Completion.completeMaterial("pa", true);
        Assertions.assertTrue(result.contains("glass_pane"));
        result = Completion.completeMaterial("glass_p", true);
        Assertions.assertTrue(result.contains("glass_pane"));
    }
}
