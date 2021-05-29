package de.eldoria.eldoutilities.database.querybuilder;

import de.eldoria.eldoutilities.consumer.ThrowingConsumer;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface StatementStage<T> {
    /**
     * Set the parameter of the {@link PreparedStatement} of the query.
     *
     * @param stmt statement to change
     * @return The {@link QueryBuilder} in a {@link ResultStage} with the parameters applied to the query.
     */
    ResultStage<T> params(ThrowingConsumer<PreparedStatement, SQLException> stmt);

    /**
     * Skip this stage and set no parameters in the query.
     * <p>
     * You can also call {@link QueryStage#queryWithoutParams(String)} on the previous {@link QueryStage} instead to avoid this step completely.
     *
     * @return The {@link QueryBuilder} in a {@link ResultStage} with no parameters set.
     */
    default ResultStage<T> emptyParams() {
        return params(s -> {
        });
    }


}
