package de.eldoria.eldoutilities.database.builder.stage;

import de.eldoria.eldoutilities.database.builder.QueryBuilder;
import de.eldoria.eldoutilities.database.builder.QueryBuilderConfig;
import de.eldoria.eldoutilities.database.builder.QueryBuilderFactory;

/**
 * Configuration stage of a {@link QueryBuilder}
 *
 * @param <T> type
 */
public interface ConfigurationStage<T> {
    /**
     * Configure the query builder.
     * <p>
     * A configured {@link QueryBuilderFactory} can be used to skip this step.
     *
     * @param config The config of the {@link QueryBuilder}
     * @return The {@link QueryBuilder} in {@link QueryStage} with the config set.
     */
    QueryStage<T> configure(QueryBuilderConfig config);

    /**
     * Set the settings to default values.
     * <p>
     * Default will be atomic transactions and not throwing any exception.
     * <p>
     * A configured {@link QueryBuilderFactory} can be used to skip this step.
     *
     * @return The {@link QueryBuilder} in
     */
    QueryStage<T> defaultConfig();


}
