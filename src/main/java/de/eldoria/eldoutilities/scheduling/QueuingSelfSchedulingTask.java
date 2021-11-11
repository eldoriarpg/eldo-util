package de.eldoria.eldoutilities.scheduling;

import de.eldoria.eldoutilities.threading.ReschedulingTask;
import org.bukkit.plugin.Plugin;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Predicate;

public abstract class QueuingSelfSchedulingTask<T> extends ReschedulingTask {
    protected static final int DEFAULT_MAX_DURATION_TARGET = 50; // assuming 50ms = 1 tick
    protected static final int DEFAULT_MAX_IDLE_TICKS = 200;
    private final Queue<T> tasks;
    private int idleTicks;
    private final int maxIdleTicks;
    private final int maxDurationTarget;

    public QueuingSelfSchedulingTask(Plugin plugin, int maxIdleTicks, int maxDurationTarget) {
        super(plugin);
        tasks = getQueueImplementation();
        this.maxIdleTicks = maxIdleTicks;
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
    public void tick() {
    }

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
