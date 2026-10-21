/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater;

import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ServerTasksTest {
    private final Runnable task = () -> {
    };
    private Plugin plugin;
    private GlobalRegionScheduler scheduler;

    @BeforeEach
    void setUp() {
        plugin = mock(Plugin.class);
        var server = mock(Server.class);
        scheduler = mock(GlobalRegionScheduler.class);
        when(plugin.getServer()).thenReturn(server);
        when(server.getGlobalRegionScheduler()).thenReturn(scheduler);
    }

    @Test
    void tasksRunOnTheGlobalRegionScheduler() {
        when(plugin.isEnabled()).thenReturn(true);

        ServerTasks.runGlobal(plugin, task);

        verify(scheduler).execute(plugin, task);
    }

    @Test
    void nothingIsScheduledForADisabledPlugin() {
        when(plugin.isEnabled()).thenReturn(false);

        ServerTasks.runGlobal(plugin, task);

        verifyNoInteractions(scheduler);
    }
}
