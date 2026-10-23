/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug;

import de.eldoria.EldoUtilities;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The consent asked for on the first debug request outlives a restart.
 */
class DebugConsentTest {
    @TempDir
    Path plugins;

    private Path configFile;
    private Plugin plugin;
    private CommandSender sender;

    @BeforeEach
    void setUp() {
        configFile = plugins.resolve("EldoUtilities").resolve("config.yml");
        plugin = mock(Plugin.class);
        when(plugin.getDataFolder()).thenReturn(plugins.resolve("SomePlugin").toFile());
        when(plugin.getName()).thenReturn("SomePlugin");
        sender = mock(CommandSender.class);
    }

    @Test
    void theFirstRequestAsksAndStoresTheConsentOnDisk() {
        EldoUtilities.ignite(plugin);

        DebugUtil.dispatchDebug(sender, plugin, DebugSettings.DEFAULT);

        verify(sender, times(1)).sendMessage(any(Component.class));
        assertTrue(Files.exists(configFile));
        assertTrue(YamlConfiguration.loadConfiguration(configFile.toFile()).getBoolean(DebugUtil.CONSENT));
    }

    @Test
    void aWithdrawnConsentIsAskedForAgainAndKeepsTheOtherSettings() throws IOException {
        Files.createDirectories(configFile.getParent());
        Files.writeString(configFile, DebugUtil.CONSENT + ": false\nother: kept\n");
        EldoUtilities.ignite(plugin);

        DebugUtil.dispatchDebug(sender, plugin, DebugSettings.DEFAULT);

        verify(sender, times(1)).sendMessage(any(Component.class));
        var saved = YamlConfiguration.loadConfiguration(configFile.toFile());
        assertTrue(saved.getBoolean(DebugUtil.CONSENT));
        assertEquals("kept", saved.getString("other"));
    }
}
