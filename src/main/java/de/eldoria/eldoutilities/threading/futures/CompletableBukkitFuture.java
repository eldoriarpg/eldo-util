/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.threading.futures;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class CompletableBukkitFuture {
    public static <T> BukkitFutureResult<T> supplyAsync(Supplier<T> supplier) {
        return BukkitFutureResult.of(CompletableFuture.supplyAsync(supplier));
    }

    public static <T> BukkitFutureResult<T> supplyAsync(Supplier<T> supplier, Executor executor) {
        return BukkitFutureResult.of(CompletableFuture.supplyAsync(supplier, executor));
    }

    public static BukkitFutureResult<Void> runAsync(Runnable supplier) {
        return BukkitFutureResult.of(CompletableFuture.runAsync(supplier));
    }

    public static BukkitFutureResult<Void> runAsync(Runnable supplier, Executor executor) {
        return BukkitFutureResult.of(CompletableFuture.runAsync(supplier, executor));
    }
}
