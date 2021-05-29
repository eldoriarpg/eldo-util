package de.eldoria.eldoutilities.database.querybuilder;

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
     * Set that the queries are not executed atomic.
     * <p>
     * When the queries are atomic they will be executed in one transaction. This will cause that no data will be changed if any query fails to execute.
     * <p>
     * On default queries will be also executed atomic. This method just exists for convenience. No queries will be executed after one query fails in any way.
     * <p>
     *
     * @return The {@link QueryBuilder} in {@link RetrievalStage} with the atomic value set to false.
     */
    default RetrievalStage<T> notAtomic() {
        return notAtomic(false);
    }

    /**
     * Define whether the queries should be atomic.
     * <p>
     * When the queries are atomic they will be executed in one transaction. This will cause that no data will be changed if any query fails to execute.
     * <p>
     * Caling this method with {@code false} is equal to calling {@link #notAtomic()}.
     *
     * @param isAtomic set the queries as atomic or not.
     * @return The {@link QueryBuilder} in {@link RetrievalStage} with the atomic value set.
     */
    RetrievalStage<T> notAtomic(boolean isAtomic);

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
