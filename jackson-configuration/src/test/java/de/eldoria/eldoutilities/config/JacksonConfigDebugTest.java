/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.config;

import de.eldoria.eldoutilities.debug.ConfigFiles;
import de.eldoria.eldoutilities.debug.data.EntryData;
import dev.chojo.ocular.key.Key;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Which configuration files a {@link JacksonConfig} puts into debug reports.
 */
class JacksonConfigDebugTest {
    @TempDir
    Path folder;

    private final Plugin plugin = mock(Plugin.class);
    private final Key<Settings> mainKey =
            Key.builder(Path.of("config.yml"), Settings::new).build();
    private final Key<Settings> secretKey =
            Key.builder(Path.of("secret.yml"), Settings::new).build();

    @BeforeEach
    void setUp() {
        when(plugin.getDataFolder()).thenReturn(folder.toFile());
        when(plugin.getLogger()).thenReturn(Logger.getLogger("test"));
    }

    @Test
    void everyLoadedFileIsIncludedByDefault() {
        var config = new JacksonConfig<>(plugin, mainKey);
        config.main();
        config.secondary(secretKey);

        assertEquals(List.of("config.yml", "secret.yml"), dumpedFiles(config));
        assertFalse(ConfigFiles.isExcluded(plugin, folder.resolve("config.yml")));
    }

    @Test
    void anExcludedFileIsLeftOut() {
        var config = new JacksonConfig<>(plugin, mainKey);
        config.main();
        config.secondary(secretKey);

        config.excludeFromDebug(secretKey);

        assertEquals(List.of("config.yml"), dumpedFiles(config));
        assertTrue(ConfigFiles.isExcluded(plugin, folder.resolve("secret.yml")));
    }

    private static List<String> dumpedFiles(JacksonConfig<?> config) {
        return Arrays.stream(config.getDebugInformations())
                .map(EntryData::getName)
                .map(name -> name.split(" ")[0])
                .sorted()
                .toList();
    }

    /**
     * A small configuration file.
     */
    public static class Settings {
        private String value = "value";

        public String value() {
            return value;
        }
    }
}
