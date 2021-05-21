package de.eldoria.eldoutilities.database.querybuilder;

public interface QueryStage<T> {
    /**
     * Set the query to execute
     * @param query query
     * @return statement stage with query
     */
    StatementStage<T> setQuery(String query);
}
