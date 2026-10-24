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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LynaLicenseTest {
    private static UserData buildData(String content) {
        var plugin = mock(Plugin.class);
        when(plugin.getResource("build.data"))
                .thenReturn(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        return UserData.get(plugin);
    }

    @Test
    void anInjectedKeyIsRead() {
        var license = LynaLicense.of(buildData("type=PUBLIC\nlynaLicense=ABCD-1234\n"));

        assertEquals(LynaLicense.State.INJECTED, license.state());
        assertEquals(Optional.of("ABCD-1234"), license.key());
    }

    @Test
    void theRawPlaceholderIsNoKey() {
        var license = LynaLicense.of(buildData("lynaLicense=%%__LYNA_LICENSE__%%\n"));

        assertEquals(LynaLicense.State.NOT_INJECTED, license.state());
        assertEquals(Optional.empty(), license.key());
    }

    @Test
    void aMissingEntryIsNoKey() {
        assertEquals(
                LynaLicense.State.NOT_INJECTED,
                LynaLicense.of(buildData("type=LOCAL\n")).state());
        assertEquals(
                LynaLicense.State.NOT_INJECTED, LynaLicense.of((String) null).state());
        assertEquals(LynaLicense.State.NOT_INJECTED, LynaLicense.of(" ").state());
    }

    @Test
    void builtByBitsApologyIsUnavailable() {
        var license =
                LynaLicense.of("Unable to acquire a license key automatically, please contact the creator directly.");

        assertEquals(LynaLicense.State.UNAVAILABLE, license.state());
        assertEquals(Optional.empty(), license.key());
    }

    @Test
    void thePlaceholderIsTheOnePublishdataWrites() {
        assertEquals("%%__LYNA_LICENSE__%%", LynaLicense.PLACEHOLDER);
    }

    @Test
    void theDebugOutputShowsTheStateButNeverTheKey() {
        String output = buildData("lynaLicense=ABCD-1234\n").asString();

        assertTrue(output.contains("Lyna licence: INJECTED"));
        assertFalse(output.contains("ABCD-1234"));
    }
}
