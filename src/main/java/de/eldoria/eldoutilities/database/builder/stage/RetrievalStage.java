package de.eldoria.eldoutilities.database.builder.stage;

import de.eldoria.eldoutilities.database.builder.QueryBuilder;
import de.eldoria.eldoutilities.threading.futures.BukkitFutureResult;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

/**
 * Represents a RetrievalStage of a {@link QueryBuilder}.
 * <p>
 * A RetrievalStage is used to retrieve the actual data from the database.
 * <p>
 * The RetrievalStage defines in which way the results should be retreived.
 *
 * @param <T> type of RetrievalStage
 */
public interface RetrievalStage<T> {

    /**
     * Retrieve all results async as a list
     *
     * @return A {@link BukkitFutureResult} to retrieve the data.
     */
    BukkitFutureResult<List<T>> all();

    /**
     * Retrieve all results async as a list
     *
     * @param executor the executor used for async call
     * @return A {@link BukkitFutureResult} to retrieve the data.
     */
    BukkitFutureResult<List<T>> all(Executor executor);

    /**
     * Retrieve all results synced as a list
     *
     * @return results as list
     */
    List<T> allSync();

    /**
     * Retrieve the first result from the results set async
     *
     * @return A {@link BukkitFutureResult} to retrieve the data.
     */
    BukkitFutureResult<Optional<T>> first();

    /**
     * Retrieve the first result from the results set async
     *
     * @param executor the executor used for async call
     * @return A {@link BukkitFutureResult} to retrieve the data.
     */
    BukkitFutureResult<Optional<T>> first(Executor executor);

    /**
     * Retrieve the first result from the results set synced
     *
     * @return result wrapped into an optional
     */
    Optional<T> firstSync();
}
