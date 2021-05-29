package de.eldoria.eldoutilities.database.querybuilder;

import de.eldoria.eldoutilities.threading.futures.BukkitFutureResult;

import java.util.concurrent.Executor;

/**
 * Represents a UpdateStage of a {@link QueryBuilder}.
 * <p>
 * A UpdateStage is used to execute an update and get the changed rows.
 */
public interface UpdateStage {
    /**
     * Execute the update async.
     *
     * @return A {@link BukkitFutureResult} which returns the number of changed rows.
     */
    BukkitFutureResult<Integer> execute();
    /**
     * Execute the update async.
     *
     * @param executor executor used for async call
     * @return A {@link BukkitFutureResult} which returns the number of changed rows.
     */
    BukkitFutureResult<Integer> execute(Executor executor);

    /**
     * Execute the update synced.
     * @return Number of changed rows
     */
    int executeSync();
}
