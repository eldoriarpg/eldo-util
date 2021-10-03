package de.eldoria.eldoutilities.threading;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A task which iterates of a collection and cancels itself when done. *
 * <pre>{@code
 *         IteratingTask<T> iteratingTask = new IteratingTask<>(collection, (e) ->
 *         {
 *             if (didSomething(e)) {
 *                 return true;
 *             }
 *             return false;
 *         }, stats -> {
 *                 Bukkit.logger().info(
 *                          String.format("Processed %d elements in %dms",
 *                              stats.getProcessedElements(),
 *                              stats.getTime()));
 *             }
 *         });
 *
 *         iteratingTask.runTaskTimer(plugin, 5, 1);
 * </pre>
 *
 * @param <T> type of collection
 * @since 1.0.0
 */
public class IteratingTask<T> extends BukkitRunnable {
    private static final int MAX_DURATION_TARGET = 50; // assuming 50ms = 1 tick

    private final Iterator<T> iterator;
    private final Predicate<T> processor;
    private final Consumer<TaskStatistics> statisticsConsumer;
    private final TaskStatistics statistics;

    /**
     * Creates a new iterating task.
     *
     * @param iterable           iterable collection of elements of type {@link T}
     * @param processor          processor to process each element. Returns {@code true} if the element was processed or
     *                           {@code false} if it was skipped.
     * @param statisticsConsumer consumer which will be executed after all elements were processed
     */
    public IteratingTask(Iterable<T> iterable, Predicate<T> processor, Consumer<TaskStatistics> statisticsConsumer) {
        this.iterator = iterable.iterator();
        this.processor = processor;
        this.statisticsConsumer = statisticsConsumer;
        this.statistics = new TaskStatistics();
    }

    @Override
    public final void run() {
        long start = System.currentTimeMillis();
        long duration;
        do {
            T next;
            if (!iterator.hasNext()) {
                this.statistics.addTime(System.currentTimeMillis() - start);
                cancel();
                this.statisticsConsumer.accept(this.statistics);
                return;
            }
            next = iterator.next();
            if (this.processor.test(next)) {
                this.statistics.processElement();
            }
        } while ((duration = System.currentTimeMillis() - start) < MAX_DURATION_TARGET);
        this.statistics.addTime(duration);
    }
}