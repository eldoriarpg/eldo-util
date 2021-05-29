package de.eldoria.eldoutilities.database.querybuilder;

import de.eldoria.eldoutilities.threading.futures.BukkitFutureResult;

import java.util.concurrent.Executor;

public interface UpdateStage<T> {
    /**
     * Execute the update async.
     *
     * @return bukkit action which returns the number of changed rows
     */
    BukkitFutureResult<Integer> executeUpdateAsync();
    /**
     * Execute the update async.
     *
     * @param executor executor used for async call
     * @return bukkit action which returns the number of changed rows
     */
    BukkitFutureResult<Integer> executeUpdateAsync(Executor executor);

    /**
     * Execute the update synced.
     * @return number of changed rows
     */
    int executeUpdate();
}
