package de.eldoria.eldoutilities.threading.futures;

import de.eldoria.eldoutilities.core.EldoUtilities;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

// proudly stolen from https://github.com/lucko/synapse/tree/master
public class BukkitFutureResult<T> implements FutureResult<T> {
    private final CompletableFuture<T> future;

    private BukkitFutureResult(CompletableFuture<T> future) {
        this.future = future;
    }

    public static <T> BukkitFutureResult<T> of(CompletableFuture<T> future) {
        return new BukkitFutureResult<>(future);
    }

    @Override
    public void whenComplete(@NotNull Consumer<? super T> callback) {
        whenComplete(EldoUtilities.getInstanceOwner(), callback);
    }

    @Override
    public void whenComplete(@NotNull Consumer<? super T> callback, Consumer<Throwable> throwable) {
        whenComplete(EldoUtilities.getInstanceOwner(), callback, throwable);
    }

    @Override
    public void whenComplete(@NotNull Plugin plugin, @NotNull Consumer<? super T> callback, Consumer<Throwable> throwableConsumer) {
        var executor = (Executor) r -> plugin.getServer().getScheduler().runTask(plugin, r);
        this.future.thenAcceptAsync(callback, executor).exceptionally(throwable -> {
            throwableConsumer.accept(throwable);
            return null;
        });
    }

    @Override
    public void whenComplete(@NotNull Plugin plugin, @NotNull Consumer<? super T> callback) {
        whenComplete(plugin, callback, throwable ->
                EldoUtilities.getInstanceOwner().getLogger().log(Level.SEVERE, "Exception in Future Result", throwable));
    }

    @Override
    public @Nullable T join() {
        return this.future.join();
    }

    @Override
    public @NotNull CompletableFuture<T> asFuture() {
        return this.future.thenApply(Function.identity());
    }
}
