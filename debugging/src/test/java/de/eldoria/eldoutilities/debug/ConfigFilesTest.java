/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug;

import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Which configuration files a report carries, and how much of them.
 */
class ConfigFilesTest {
    @TempDir
    Path folder;
    private final Plugin plugin = mock(Plugin.class);

    @BeforeEach
    void setUp() {
        when(plugin.getDataFolder()).thenReturn(folder.toFile());
        when(plugin.getLogger()).thenReturn(Logger.getLogger("test"));
    }

    private Path write(String name, int bytes) throws IOException {
        Path path = folder.resolve(name);
        Files.createDirectories(path.getParent());
        Files.writeString(path, "x".repeat(bytes));
        return path;
    }

    @Test
    void everyConfigLikeFileIsChosenInOrder() throws IOException {
        write("config.yml", 1);
        write("mobs/zombie.yaml", 1);
        write("messages.properties", 1);
        write("data.db", 1);
        write("store.json", 1);

        List<String> names = ConfigFiles.read(plugin, ConfigFiles.inDataFolder(plugin)).stream()
                .map(ConfigFiles.Read::name).toList();

        assertEquals(List.of("config.yml", "messages.properties", "mobs/zombie.yaml", "store.json"), names);
    }

    @Test
    void largeFilesAreLeftOutAndTheTotalIsCapped() throws IOException {
        Path huge = write("a-huge.yml", (int) ConfigFiles.MAX_FILE_BYTES + 1);
        List<Path> chosen = new java.util.ArrayList<>(List.of(huge));
        for (int i = 0; i < 6; i++) chosen.add(write("b" + i + ".yml", 1024 * 1024 - 10));

        List<String> names = ConfigFiles.read(plugin, chosen).stream().map(ConfigFiles.Read::name).toList();

        assertEquals(List.of("b0.yml", "b1.yml", "b2.yml", "b3.yml", "b4.yml"), names);
    }

    @Test
    void settingsReplaceTheChoice() throws IOException {
        write("config.yml", 1);
        Path extra = write("extra.txt", 3);
        var settings = DebugSettings.builder().withConfigFiles(p -> List.of(extra)).build();

        var read = ConfigFiles.read(plugin, settings.configFiles(plugin));

        assertEquals(List.of(new ConfigFiles.Read("extra.txt", "xxx")), read);
    }
}
