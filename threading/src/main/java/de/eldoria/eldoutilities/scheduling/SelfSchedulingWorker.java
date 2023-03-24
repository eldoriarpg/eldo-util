/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.scheduling;

import de.eldoria.eldoutilities.threading.ReschedulingTask;
import org.bukkit.plugin.Plugin;

import java.util.Collection;

/**
 * A self scheduling worker which will schedule itself when getting tasks.
 * <p>
 * Will unschedule itself if no tasks are left for some time.
 *
 * @param <V> type of collection
 * @param <T> type of collection implementation
 * @since 1.4.0
 */
public abstract class SelfSchedulingWorker<V, T extends Collection<V>> extends ReschedulingTask {
    private final T tasks;
    private int idleTicks;
    private int maxIdleTicks = 200;

    public SelfSchedulingWorker(Plugin plugin, int maxIdleTicks) {
        this(plugin);
        this.maxIdleTicks = maxIdleTicks;
    }

    public SelfSchedulingWorker(Plugin plugin) {
        super(plugin);
        tasks = getQueueImplementation();
    }

    /**
     * handle one object which was polled from the queue
     *
     * @param object object from queue
     */
    protected abstract void execute(V object);

    /**
     * Tick is executed once per tick.
     */
    protected void tick() {
    }

    @Override
    public final void run() {
        if (!tasks.isEmpty()) {
            tick();
            for (var task : tasks) {
                execute(task);
            }
        } else {
            idleTicks++;
            if (idleTicks >= maxIdleTicks) {
                cancel();
            }
        }
    }

    protected final void register(V object) {
        if (!isActive()) return;
        tasks.add(object);
        if (!isRunning()) {
            schedule();
        }
        idleTicks = 0;
    }

    public final void unregister(V object) {
        tasks.remove(object);
    }

    protected abstract T getQueueImplementation();

    @Override
    public final void shutdown() {
        super.shutdown();
        for (var task : tasks) {
            execute(task);
        }
        tasks.clear();
    }
}
