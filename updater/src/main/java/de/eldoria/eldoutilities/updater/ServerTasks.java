/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater;

import org.bukkit.plugin.Plugin;

/**
 * Runs work on the thread a server expects for global state such as commands and listeners.
 * <p>
 * Paper's global region scheduler is that thread on Folia and the main thread everywhere else, so it serves both.
 */
final class ServerTasks {
    private ServerTasks() {}

    /**
     * Runs a task on the global thread of the server. Does nothing once the plugin is disabled, since the scheduler
     * no longer accepts tasks then.
     *
     * @param plugin plugin owning the task
     * @param task   task to run
     */
    static void runGlobal(Plugin plugin, Runnable task) {
        if (!plugin.isEnabled()) return;
        plugin.getServer().getGlobalRegionScheduler().execute(plugin, task);
    }
}
