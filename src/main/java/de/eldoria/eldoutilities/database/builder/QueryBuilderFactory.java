package de.eldoria.eldoutilities.database.builder;

import de.eldoria.eldoutilities.database.builder.stage.ConfigurationStage;
import de.eldoria.eldoutilities.database.builder.stage.QueryStage;
import org.bukkit.plugin.Plugin;

import javax.sql.DataSource;

/**
 * This class provides simple methods to create preconfigured {@link QueryBuilder}.
 * <p>
 * The factory will always initialize the {@link QueryBuilder} with the plugin, datasource and configuration provided on creation.
 * <p>
 * This results in a QueryBuilder in the {@link QueryStage} and skips the {@link ConfigurationStage}.
 */
public class QueryBuilderFactory {
    private final QueryBuilderConfig config;
    private final DataSource dataSource;
    private final Plugin plugin;

    public QueryBuilderFactory(QueryBuilderConfig config, DataSource dataSource, Plugin plugin) {
        this.config = config;
        this.dataSource = dataSource;
        this.plugin = plugin;
    }

    /**
     * Create a new query builder with a defined return type. Use it for selects.
     *
     * @param clazz class of required return type. Doesnt matter if you want a list or single result.
     * @param <T>   type of return type
     * @return a new query builder in a {@link QueryStage}
     */
    public <T> QueryStage<T> builder(Class<T> clazz) {
        return QueryBuilder.builder(plugin, dataSource, clazz).configure(config);
    }

    /**
     * Create a new Query builder without a defined return type. Use it for updates.
     *
     * @return a new query builder in a {@link QueryStage}
     */
    public QueryStage<?> builder() {
        return QueryBuilder.builder(plugin, dataSource, null).configure(config);
    }
}
