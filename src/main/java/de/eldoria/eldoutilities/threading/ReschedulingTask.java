/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.threading;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class ReschedulingTask {
    private final Plugin plugin;
    private BukkitRunnable task;
    private boolean active = true;

    public ReschedulingTask(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Schedules the task if it is not running.
     */
    public void schedule() {
        if (!isActive()) return;
        if (!isRunning()) {
            task = new InternalTask(this::run);
            task.runTaskTimer(plugin, 0, 1);
            plugin.getLogger().fine(getClass().getSimpleName() + " of " + plugin.getName() + " started.");
        }
    }

    /**
     * Cancel the task if it is running.
     */
    public void cancel() {
        if (isRunning()) {
            task.cancel();
            task = null;
            plugin.getLogger().fine(getClass().getSimpleName() + " of " + plugin.getName() + " paused.");
        }
    }

    /**
     * Shuts down the scheduler. It can be not scheduled again after this.
     */
    public void shutdown() {
        active = false;
    }

    /**
     * Check if the task is running.
     *
     * @return true if the task is running
     */
    public boolean isRunning() {
        return task != null;
    }

    /**
     * Check if the task is active.
     * <p>
     * If the task is not active you cant schedule it.
     *
     * @return true if active
     */
    public boolean isActive() {
        return active;
    }

    public abstract void run();

    public Plugin getPlugin() {
        return plugin;
    }

    private static class InternalTask extends BukkitRunnable {
        private final Runnable runnable;

        public InternalTask(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            runnable.run();
        }
    }
}
