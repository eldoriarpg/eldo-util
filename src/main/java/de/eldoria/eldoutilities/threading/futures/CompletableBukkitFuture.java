package de.eldoria.eldoutilities.threading.futures;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class CompletableBukkitFuture {
    public static <T> BukkitFutureResult<T> supplyAsync(Supplier<T> supplier){
        return BukkitFutureResult.of(CompletableFuture.supplyAsync(supplier));
    }
    public static <T> BukkitFutureResult<T> supplyAsync(Supplier<T> supplier, Executor executor){
        return BukkitFutureResult.of(CompletableFuture.supplyAsync(supplier, executor));
    }
}
