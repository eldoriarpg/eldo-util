/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug;

import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserDataTest {
    @Test
    void readsTheBuildDataOfAPluginWithoutAJar() {
        var plugin = mock(Plugin.class);
        when(plugin.getResource("build.data"))
                .thenReturn(new ByteArrayInputStream(
                        "type=PUBLIC\nlynaProduct=5\nlynaProduct=6\n".getBytes(StandardCharsets.UTF_8)));

        var data = UserData.get(plugin);

        assertTrue(data.isPremium());
        assertEquals(Optional.of("6"), data.property("lynaProduct"));
        assertEquals(Optional.empty(), data.property("md5"));
    }
}
