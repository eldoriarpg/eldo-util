/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.scheduling;

import de.eldoria.eldoutilities.threading.ReschedulingTask;
import org.bukkit.plugin.Plugin;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Predicate;

public abstract class QueuingSelfSchedulingTask<T> extends ReschedulingTask {
    /**
     * The length of one tick in milliseconds, which is also the longest time limit a run may have.
     */
    protected static final int DEFAULT_MAX_DURATION_TARGET = 50;

    protected static final int DEFAULT_MAX_IDLE_TICKS = 200;
    private final Queue<T> tasks;
    private final int maxIdleTicks;
    private final int maxDurationTarget;
    private int idleTicks;

    /**
     * Creates a task with its own limits.
     * <p>
     * A time limit above {@value #DEFAULT_MAX_DURATION_TARGET} ms, one tick, is lowered to it with a warning.
     *
     * @param plugin            plugin owning the task
     * @param maxIdleTicks      ticks without work after which the task stops until new work arrives
     * @param maxDurationTarget milliseconds a run may handle objects for
     */
    public QueuingSelfSchedulingTask(Plugin plugin, int maxIdleTicks, int maxDurationTarget) {
        super(plugin);
        tasks = getQueueImplementation();
        this.maxIdleTicks = maxIdleTicks;
        if (maxDurationTarget > DEFAULT_MAX_DURATION_TARGET) {
            plugin.getLogger()
                    .warning("%s: a time limit of %d ms is above one tick. Using %d ms."
                            .formatted(getClass().getSimpleName(), maxDurationTarget, DEFAULT_MAX_DURATION_TARGET));
        }
        this.maxDurationTarget = Math.max(0, Math.min(maxDurationTarget, DEFAULT_MAX_DURATION_TARGET));
    }

    public QueuingSelfSchedulingTask(Plugin plugin) {
        this(plugin, DEFAULT_MAX_IDLE_TICKS, DEFAULT_MAX_DURATION_TARGET);
    }

    /**
     * handle one object which was polled from the queue
     *
     * @param object object from queue
     */
    public abstract void execute(T object);

    /**
     * Tick is executed once per tick.
     */
    public void tick() {}

    @Override
    public final void run() {
        tick();
        var start = System.currentTimeMillis();
        long duration = 0;

        while (!tasks.isEmpty() && proceed(tasks.peek()) && duration < maxDurationTarget) {
            execute(tasks.poll());
            duration = System.currentTimeMillis() - start;
        }

        if (tasks.isEmpty()) {
            idleTicks++;
            if (idleTicks >= maxIdleTicks) {
                cancel();
            }
        }
    }

    /**
     * Define if this object should be polled from queue or if the scheduler should proceed to the next tick.
     *
     * @param object object to check
     * @return true if the object should be handeld. false if the task should wait.
     */
    protected boolean proceed(T object) {
        return true;
    }

    protected final void schedule(T object) {
        if (!isActive()) return;
        tasks.add(object);
        if (!isRunning()) {
            schedule();
        }
        idleTicks = 0;
    }

    protected Queue<T> getQueueImplementation() {
        return new ArrayDeque<>();
    }

    @Override
    public final void shutdown() {
        super.shutdown();
        for (var task : tasks) {
            execute(task);
        }
        tasks.clear();
    }

    /**
     * Clear all the queued objects and cancel the task. It can be scheduled again after this.
     */
    public void clear() {
        tasks.clear();
        super.cancel();
    }

    protected boolean remove(T o) {
        return tasks.remove(o);
    }

    protected boolean removeIf(Predicate<? super T> filter) {
        return tasks.removeIf(filter);
    }
}
