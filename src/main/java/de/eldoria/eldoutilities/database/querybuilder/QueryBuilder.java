package de.eldoria.eldoutilities.database.querybuilder;

import de.eldoria.eldoutilities.consumer.ThrowingConsumer;
import de.eldoria.eldoutilities.functions.ThrowingFunction;
import de.eldoria.eldoutilities.threading.BukkitAsyncAction;
import org.bukkit.plugin.Plugin;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class QueryBuilder<T> implements QueryStage<T>, StatementStage<T>, ResultStage<T>, RetrievalStage<T>, UpdateStage {
    private final Plugin plugin;
    private final DataSource dataSource;
    private String query;
    ThrowingConsumer<PreparedStatement, SQLException> statementConsumer;
    ThrowingFunction<T, ResultSet, SQLException> resultMapper;

    /**
     * Create a neq query builder
     *
     * @param plugin plugin of query builder
     * @param dataSource data source to use
     * @param clazz clazz of result
     */
    public QueryBuilder(Plugin plugin, DataSource dataSource, Class<T> clazz) {
        this.plugin = plugin;
        this.dataSource = dataSource;
    }

    public static <T> QueryStage<T> builder(Plugin plugin, DataSource source, Class<T> clazz) {
        return new QueryBuilder<>(plugin, source, clazz);
    }

    @Override
    public StatementStage<T> setQuery(String query) {
        this.query = query;
        return this;
    }

    @Override
    public ResultStage<T> setStatements(ThrowingConsumer<PreparedStatement, SQLException> stmt) {
        this.statementConsumer = stmt;
        return this;
    }

    @Override
    public RetrievalStage<T> extractResults(ThrowingFunction<T, ResultSet, SQLException> mapper) {
        this.resultMapper = mapper;
        return this;
    }

    @Override
    public UpdateStage update() {
        return this;
    }

    @Override
    public BukkitAsyncAction<List<T>> retrieveResultsAsync() {
        return BukkitAsyncAction.supplyAsync(plugin, this::retrieveResults);
    }

    @Override
    public List<T> retrieveResults() {
        List<T> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            statementConsumer.accept(stmt);
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                results.add(resultMapper.apply(resultSet));
            }
        } catch (SQLException e) {
            logDbError(e);
        }
        return results;
    }

    @Override
    public BukkitAsyncAction<Optional<T>> retrieveResultAsync() {
        return BukkitAsyncAction.supplyAsync(plugin, this::retrieveResult);
    }

    @Override
    public Optional<T> retrieveResult() {
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            statementConsumer.accept(stmt);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return Optional.ofNullable(resultMapper.apply(resultSet));
            }
        } catch (SQLException e) {
            logDbError(e);
        }
        return Optional.empty();
    }

    public void logDbError(SQLException e) {
        plugin.getLogger().log(Level.SEVERE, "An SQL query occured:\n" + prettyException(e), e);
    }

    public static String prettyException(SQLException ex) {
        return "SQLException: " + ex.getMessage() + "\n"
                + "SQLState: " + ex.getSQLState() + "\n"
                + "VendorError: " + ex.getErrorCode();
    }

    @Override
    public BukkitAsyncAction<Integer> executeUpdateAsync() {
        return BukkitAsyncAction.supplyAsync(plugin, this::executeUpdate);
    }

    @Override
    public int executeUpdate() {
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            statementConsumer.accept(stmt);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            logDbError(e);
        }
        return 0;
    }
}
