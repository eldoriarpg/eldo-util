/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.scheduling;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Scheduler service to schedule actions with only one scheduler and preserving the main thread from overloading.
 *
 * @since 1.2.3
 */
public final class DelayedActions extends QueuingSelfSchedulingTask<DelayedActions.DelayedTask> {
    private int currentTick;

    private DelayedActions(Plugin plugin) {
        super(plugin);
    }

    /**
     * Start a delayed action scheduler for a plugin.
     * <p>
     * This scheduler allows to schedule multiple task without starting a new scheduler for each task.
     * <p>
     * The scheduler will also ensure that the main thread is not abused by overloading operations per tick.
     *
     * @param plugin plugin which owns the instance
     * @return new delayed action instance
     */
    public static DelayedActions start(Plugin plugin) {
        return new DelayedActions(plugin);
    }

    @Override
    public void execute(DelayedTask object) {
        object.invoke();
    }

    @Override
    public void tick() {
        currentTick++;
    }

    @Override
    protected Queue<DelayedTask> getQueueImplementation() {
        return new PriorityQueue<>();
    }

    /**
     * Delays an action by a specific amount of ticks
     *
     * @param runnable runnable to execute
     * @param delay    delay for execution.
     */
    public void schedule(Runnable runnable, int delay) {
        if (delay == 0) {
            runnable.run();
            return;
        }
        schedule(new DelayedTask(runnable, currentTick + delay));
    }

    @Override
    protected boolean proceed(DelayedTask object) {
        return object.tick <= currentTick;
    }

    protected static class DelayedTask implements Comparable<DelayedTask> {
        private final Runnable runnable;
        private final int tick;

        public DelayedTask(Runnable runnable, int tick) {
            this.runnable = runnable;
            this.tick = tick;
        }

        @Override
        public int compareTo(@NotNull DelayedActions.DelayedTask o) {
            return Integer.compare(tick, o.tick);
        }

        public void invoke() {
            runnable.run();
        }
    }
}
