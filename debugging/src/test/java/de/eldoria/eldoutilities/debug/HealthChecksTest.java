/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug;

import de.eldoria.eldoutilities.debug.data.EntryData;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Health checks are gathered from every provider the plugin reaches, each provider once.
 */
class HealthChecksTest {
    private record Provider(List<HealthCheck> checks, List<DebugDataProvider> next) implements DebugDataProvider {
        @Override
        public EntryData @NotNull [] getDebugInformations() {
            return new EntryData[0];
        }

        @Override
        public @NotNull List<DebugDataProvider> getDebugProviders() {
            return next;
        }

        @Override
        public @NotNull List<HealthCheck> getHealthChecks() {
            return checks;
        }
    }

    @Test
    void checksComeFromEveryProviderOnce() {
        var database = new Provider(List.of(HealthCheck.ok("Database", "Reachable")), List.of());
        var worlds = new Provider(List.of(HealthCheck.error("World nether", "Not loaded")), List.of(database));
        Plugin plugin = mock(Plugin.class, withSettings().extraInterfaces(DebugDataProvider.class));
        when(((DebugDataProvider) plugin).getDebugProviders()).thenReturn(List.of(worlds, database));
        when(((DebugDataProvider) plugin).getHealthChecks())
                .thenReturn(List.of(HealthCheck.warn("Version", "Outdated")));
        when(plugin.getLogger()).thenReturn(Logger.getLogger("test"));

        assertEquals(
                List.of(
                        HealthCheck.warn("Version", "Outdated"),
                        HealthCheck.error("World nether", "Not loaded"),
                        HealthCheck.ok("Database", "Reachable")),
                DebugUtil.getHealthChecks(plugin));
    }

    @Test
    void aPluginThatProvidesNothingHasNoChecks() {
        assertEquals(List.of(), DebugUtil.getHealthChecks(mock(Plugin.class)));
    }
}
