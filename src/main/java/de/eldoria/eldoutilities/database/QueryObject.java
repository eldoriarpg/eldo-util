package de.eldoria.eldoutilities.database;

import de.eldoria.eldoutilities.database.builder.QueryBuilder;
import de.eldoria.eldoutilities.database.builder.QueryBuilderFactory;
import de.eldoria.eldoutilities.database.builder.stage.ConfigurationStage;
import org.bukkit.plugin.Plugin;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Base class which can be used for classes which call the database.
 * <p>
 * Provides convinience methods for connection retrieval, logging and a querybuilder.
 * <p>
 * You may use a {@link QueryBuilderFactory} for builder creation.
 */
public class QueryObject {
    private final Plugin plugin;
    private final DataSource dataSource;

    public QueryObject(Plugin plugin, DataSource dataSource) {
        this.plugin = plugin;
        this.dataSource = dataSource;
    }

    /**
     * Get a query builder for easy sql execution.
     *
     * @param clazz clazz which should be retrieved. Doesnt matter if you want a list and multiple results or not.
     * @param <T>   type of result
     * @return query builder in a query stage
     */
    protected <T> ConfigurationStage<T> queryBuilder(Class<T> clazz) {
        return QueryBuilder.builder(plugin, dataSource, clazz);
    }

    public void logDbError(String message, SQLException e) {
        plugin.getLogger().log(Level.SEVERE, message + ":\n" + DBUtil.prettyException(e), e);
    }

    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
