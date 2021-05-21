package de.eldoria.eldoutilities.database.querybuilder;

import de.eldoria.eldoutilities.threading.BukkitAsyncAction;

import java.util.List;
import java.util.Optional;

public interface RetrievalStage<T> {
    /**
     * Retrieve all results async as a list
     *
     * @return async execution action
     */
    BukkitAsyncAction<List<T>> retrieveResultsAsync();

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
    BukkitAsyncAction<Optional<T>> retrieveResultAsync();

    /**
     * Retrieve the first result from the results set synced
     *
     * @return result wrapped into an optional
     */
    Optional<T> retrieveResult();
}
