package de.eldoria.eldoutilities.scheduling;

import de.eldoria.eldoutilities.threading.ReschedulingTask;
import org.bukkit.plugin.Plugin;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Predicate;

public abstract class QueuingSelfSchedulingTask<T> extends ReschedulingTask {
    private static final int MAX_DURATION_TARGET = 50; // assuming 50ms = 1 tick
    private final Queue<T> tasks;
    private final int maxDurationTarget;
    private int idleTicks;
    private int maxIdleTicks = 200;

    public QueuingSelfSchedulingTask(Plugin plugin, int maxIdleTicks) {
        this(plugin, maxIdleTicks, MAX_DURATION_TARGET);
    }

    public QueuingSelfSchedulingTask(Plugin plugin, int maxIdleTicks, int maxDurationTarget) {
        this(plugin, maxDurationTarget);
        this.maxIdleTicks = maxIdleTicks;
    }

    public QueuingSelfSchedulingTask(Plugin plugin) {
        this(plugin, MAX_DURATION_TARGET);
    }

    public QueuingSelfSchedulingTask(Plugin plugin, int maxDurationTarget) {
        super(plugin);
        tasks = getQueueImplementation();
        this.maxDurationTarget = Math.max(0, Math.min(maxDurationTarget, MAX_DURATION_TARGET));
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

    protected boolean remove(T o) {
        return tasks.remove(o);
    }

    protected boolean removeIf(Predicate<? super T> filter) {
        return tasks.removeIf(filter);
    }
}
