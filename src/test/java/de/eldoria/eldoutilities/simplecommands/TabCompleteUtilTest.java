/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.simplecommands;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TabCompleteUtilTest {

    @Test
    void completeMaterial() {
        List<String> result = TabCompleteUtil.completeMaterial("gp", true);
        Assertions.assertTrue(result.contains("glass_pane"));
        result = TabCompleteUtil.completeMaterial("pane", true);
        Assertions.assertTrue(result.contains("glass_pane"));
        result = TabCompleteUtil.completeMaterial("pa", true);
        Assertions.assertTrue(result.contains("glass_pane"));
        result = TabCompleteUtil.completeMaterial("glass_p", true);
        Assertions.assertTrue(result.contains("glass_pane"));
    }
}
