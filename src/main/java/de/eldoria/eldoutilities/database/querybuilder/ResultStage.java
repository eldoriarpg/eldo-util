package de.eldoria.eldoutilities.database.querybuilder;

import de.eldoria.eldoutilities.functions.ThrowingFunction;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultStage<T> {

    /**
     * Extract results from a results set.
     * <p>
     * This function should not loop through the results set. It should only transform the current row to the requested object.
     *
     * @param mapper mapper to map the current row.
     * @return restrieval stage.
     */
    RetrievalStage<T> extractResults(ThrowingFunction<T, ResultSet, SQLException> mapper);

    /**
     * Mark this query as update query.
     *
     * @return update stage
     */
    UpdateStage update();

    /**
     * Mark this query as deletion query. Alias for {@link #update()}
     *
     * @return update stage
     */
    default UpdateStage delete() {
        return update();
    }

    /**
     * Insert data into a table. Alias for {@link #update()}
     *
     * @return update stage
     */
    default UpdateStage insert() {
        return update();
    }
}
