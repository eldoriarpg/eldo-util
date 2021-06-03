package de.eldoria.eldoutilities.threading;

import de.eldoria.eldoutilities.scheduling.QueuingSelfSchedulingTask;
import de.eldoria.eldoutilities.threading.futures.BukkitFutureResult;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Scheduler Service which allows to execute a async call and handle the retrieved data in the main thread.
 * Preserves the main thread from overloading
 *
 * @deprecated Use {@link BukkitFutureResult} instead.
 */
@Deprecated
public final class AsyncSyncingCallbackExecutor extends QueuingSelfSchedulingTask<AsyncSyncingCallbackExecutor.Callback<?>> {

    private final BukkitScheduler scheduler;

    private AsyncSyncingCallbackExecutor(Plugin plugin) {
        super(plugin);
        scheduler = Bukkit.getScheduler();
    }

    /**
     * Returns a new running executor instance.
     *
     * @param plugin plugin of executor
     * @return running executor instance
     */
    public static AsyncSyncingCallbackExecutor create(Plugin plugin) {
        return new AsyncSyncingCallbackExecutor(plugin);
    }

    @Override
    public void execute(Callback<?> object) {
        object.invoke();
    }

    /**
     * Schedules a new task for execution.
     *
     * @param asyncProvider Supplier which is executed async and provides data for the consumer
     * @param syncAction    Consumer which consumes the data in main thread provided by the supplier
     * @param <T>           type of data
     */
    public <T> void schedule(Supplier<T> asyncProvider, Consumer<T> syncAction) {
        if (!isActive()) return;
        scheduler.runTaskAsynchronously(getPlugin(), () -> schedule(new Callback<>(asyncProvider.get(), syncAction)));
    }

    protected static class Callback<T> {
        private final T data;
        private final Consumer<T> consumer;

        public Callback(T data, Consumer<T> consumer) {
            this.data = data;
            this.consumer = consumer;
        }

        private void invoke() {
            consumer.accept(data);
        }
    }
}
