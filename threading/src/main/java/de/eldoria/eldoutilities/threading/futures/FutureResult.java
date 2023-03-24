/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.threading.futures;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

// proudly stolen from https://github.com/lucko/synapse/tree/master
public interface FutureResult<T> {
    /**
     * Attaches a completion callback to this {@link FutureResult}.
     * <p>
     * If the action is already complete, the runnable will be called immediately.
     * <p>
     * If it is not complete, the consumer will be called synchronously using
     * the Bukkit scheduler when the action is completed.</p>
     *
     * @param callback the callback
     */
    void whenComplete(@NotNull Consumer<? super T> callback);

    void whenComplete(@NotNull Consumer<? super T> callback, Consumer<Throwable> throwable);

    void whenComplete(@NotNull Plugin plugin, @NotNull Consumer<? super T> callback, Consumer<Throwable> throwable);

    /**
     * Attaches a completion callback to this {@link FutureResult}.
     * <p>
     * If the action is already complete, the runnable will be called immediately.
     * <p>
     * If it is not complete, the consumer will be called synchronously using
     * the Bukkit scheduler when the action is completed.</p>
     *
     * @param plugin   used for callback.
     * @param callback the callback
     */
    void whenComplete(@NotNull Plugin plugin, @NotNull Consumer<? super T> callback);

    /**
     * Blocks the current thread until the action has completed.
     *
     * <p>This method should only be called from an async task!</p>
     *
     * @return the result
     */
    @Nullable T join();

    /**
     * Encapsulates this {@link FutureResult} as a {@link CompletableFuture}.
     *
     * @return a future
     */
    @NotNull
    CompletableFuture<T> asFuture();
}
