/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The defaults every update data builder starts with.
 */
class UpdateDataBuilderTest {
    private final Plugin plugin = mock(Plugin.class);

    @BeforeEach
    @SuppressWarnings("deprecation")
    void setUp() {
        when(plugin.getName()).thenReturn("MyPlugin");
        when(plugin.getDescription()).thenReturn(mock(PluginDescriptionFile.class));
    }

    @Test
    void playersAreNotifiedByDefault() {
        assertTrue(new Builder(plugin).notifyUpdate);
    }

    @Test
    void theNotifyPermissionIsNamedAfterThePlugin() {
        assertEquals("myplugin.updates", new Builder(plugin).notifyPermission);
    }

    /**
     * A builder that only exposes the defaults.
     */
    private static class Builder extends UpdateDataBuilder<Builder, UpdateData<?>> {
        Builder(Plugin plugin) {
            super(plugin);
        }

        @Override
        public UpdateData<?> build() {
            throw new UnsupportedOperationException();
        }
    }
}
