package de.eldoria.eldoutilities.threading.futures;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

// proudly stolen from https://github.com/lucko/synapse/tree/master
public class BukkitFuture<T> implements FutureResult<T> {
    private final CompletableFuture<T> future;

    public BukkitFuture(CompletableFuture<T> future) {
        this.future = future;
    }

    @Override
    public void whenComplete(@NotNull Plugin plugin, @NotNull Consumer<? super T> callback) {
        Executor executor = r -> plugin.getServer().getScheduler().runTask(plugin, r);
        this.future.thenAcceptAsync(callback, executor);
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
