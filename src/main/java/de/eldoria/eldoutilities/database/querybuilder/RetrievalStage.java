package de.eldoria.eldoutilities.database.querybuilder;

import de.eldoria.eldoutilities.threading.futures.BukkitFutureResult;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

public interface RetrievalStage<T> {
    /**
     * Retrieve all results async as a list
     *
     * @return async execution action
     */
    BukkitFutureResult<List<T>> retrieveResultsAsync();

    /**
     * Retrieve all results async as a list
     *
     * @param executor the executor used for async call
     * @return async execution action
     */
    BukkitFutureResult<List<T>> retrieveResultsAsync(Executor executor);

    /**
     * Retrieve all results synced as a list
     *
     * @return results as list
     */
    List<T> retrieveResults();

    /**
     * Retrieve the first result from the results set async
     *
     * @return bukkit action which returns a result wrapped into an optional
     */
    BukkitFutureResult<Optional<T>> retrieveResultAsync();

    /**
     * Retrieve the first result from the results set async
     *
     * @param executor the executor used for async call
     * @return bukkit action which returns a result wrapped into an optional
     */
    BukkitFutureResult<Optional<T>> retrieveResultAsync(Executor executor);

    /**
     * Retrieve the first result from the results set synced
     *
     * @return result wrapped into an optional
     */
    Optional<T> retrieveResult();
}
