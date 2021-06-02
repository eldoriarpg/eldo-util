package de.eldoria.eldoutilities.database.builder;

import de.eldoria.eldoutilities.consumer.ThrowingConsumer;
import de.eldoria.eldoutilities.database.DBUtil;
import de.eldoria.eldoutilities.database.builder.exception.QueryExecutionException;
import de.eldoria.eldoutilities.database.builder.exception.WrappedQueryExecutionException;
import de.eldoria.eldoutilities.database.builder.stage.ConfigurationStage;
import de.eldoria.eldoutilities.database.builder.stage.QueryStage;
import de.eldoria.eldoutilities.database.builder.stage.ResultStage;
import de.eldoria.eldoutilities.database.builder.stage.RetrievalStage;
import de.eldoria.eldoutilities.database.builder.stage.StatementStage;
import de.eldoria.eldoutilities.database.builder.stage.UpdateStage;
import de.eldoria.eldoutilities.functions.ThrowingFunction;
import de.eldoria.eldoutilities.threading.futures.BukkitFutureResult;
import de.eldoria.eldoutilities.threading.futures.CompletableBukkitFuture;
import org.bukkit.plugin.Plugin;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.logging.Level;

/**
 * This query builder can be used to execute one or more queries onto a database via a connection provided by a datasource.
 * <p>
 * The query builder is a stage organized object. Every call will be invoked on a stage of the same query builder.
 * <p>
 * You may execute as many updates as you want. You may only get the returned results of one query, which must be the last.
 * <p>
 * All methods which are not labeled with a {@code Sync} suffix will be executed in an async context. Async method will
 * provide a callback via a {@link BukkitFutureResult}. This callback will be executed by the bukkit main thread.
 * <p>
 * All queries will be executed in an atomic transaction. This means that data will be only written if all queries are executed successfully.
 * This behaviour can be changed by calling {@link QueryBuilderConfig.Builder#notAtomic()} ()} on config creation.
 * <p>
 * Any exception thrown while executing queries will be wrapped into an {@link QueryExecutionException}. This exception
 * was created on query submission to the query builder. Not that this is not the execution, which may be called on
 * another thread. This exception will help you to trace back async calls.
 * <p>
 * Any {@link SQLException} thrown inside the query builder will not be thrown but logged by default.
 *
 * @param <T> type of query return type
 */
public class QueryBuilder<T> implements ConfigurationStage<T>, QueryStage<T>, StatementStage<T>, ResultStage<T>, RetrievalStage<T>, UpdateStage {
    private final Plugin plugin;
    private final DataSource dataSource;
    private final Queue<QueryTask> tasks = new ArrayDeque<>();

    private String currQuery;
    private ThrowingConsumer<PreparedStatement, SQLException> currStatementConsumer;
    private ThrowingFunction<T, ResultSet, SQLException> currResultMapper;

    private QueryBuilderConfig config;

    private final QueryExecutionException executionException;
    private final WrappedQueryExecutionException wrappedExecutionException;

    private QueryBuilder(Plugin plugin, DataSource dataSource, Class<T> clazz) {
        this.plugin = plugin;
        this.dataSource = dataSource;
        executionException = new QueryExecutionException("An error occured while executing a query.");
        wrappedExecutionException = new WrappedQueryExecutionException("An error occured while executing a query.");
    }

    /**
     * Create a new query builder with a defined return type. Use it for selects.
     *
     * @param plugin plugin to execute async stuff
     * @param source datasource for connection
     * @param clazz  class of required return type. Doesnt matter if you want a list or single result.
     * @param <T>    type of return type
     * @return a new query builder in a {@link QueryStage}
     */
    public static <T> ConfigurationStage<T> builder(Plugin plugin, DataSource source, Class<T> clazz) {
        return new QueryBuilder<>(plugin, source, clazz);
    }

    /**
     * Create a new Query builder without a defined return type. Use it for updates.
     *
     * @param plugin plugin to execute async stuff
     * @param source datasource for connection
     * @return a new query builder in a {@link QueryStage}
     */
    public static ConfigurationStage<?> builder(Plugin plugin, DataSource source) {
        return new QueryBuilder<>(plugin, source, null);
    }

    // CONFIGURATION STAGE

    @Override
    public QueryStage<T> configure(QueryBuilderConfig config) {
        this.config = config;
        return this;
    }

    @Override
    public QueryStage<T> defaultConfig() {
        config = QueryBuilderConfig.DEFAULT;
        return this;
    }

    // QUERY STAGE

    @Override
    public StatementStage<T> query(String query) {
        this.currQuery = query;
        return this;
    }

    @Override
    public ResultStage<T> queryWithoutParams(String query) {
        this.currQuery = query;
        return emptyParams();
    }

    // STATEMENT STAGE

    @Override
    public ResultStage<T> params(ThrowingConsumer<PreparedStatement, SQLException> stmt) {
        this.currStatementConsumer = stmt;
        return this;
    }

    // RESULT STAGE

    @Override
    public RetrievalStage<T> readRow(ThrowingFunction<T, ResultSet, SQLException> mapper) {
        this.currResultMapper = mapper;
        queueTask();
        return this;
    }

    @Override
    public UpdateStage update() {
        currResultMapper = s -> null;
        queueTask();
        return this;
    }

    @Override
    public QueryStage<T> append() {
        currResultMapper = s -> null;
        queueTask();
        return this;
    }

    private void queueTask() {
        tasks.add(new QueryTask(currQuery, currStatementConsumer, currResultMapper));
    }

    // RETRIEVAL STAGE

    // LISTS  RETRIEVAL

    @Override
    public BukkitFutureResult<List<T>> all() {
        return CompletableBukkitFuture.supplyAsync(this::allSync);
    }

    @Override
    public BukkitFutureResult<List<T>> all(Executor executor) {
        return CompletableBukkitFuture.supplyAsync(this::allSync, executor);
    }

    @Override
    public List<T> allSync() {
        try (Connection conn = dataSource.getConnection()) {
            autoCommit(conn);
            List<T> results = executeAndGetLast(conn).retrieveResults(conn);
            commit(conn);
            return results;
        } catch (QueryExecutionException e) {
            logDbError(e);
        } catch (SQLException e) {
            handleException(e);
        }
        return Collections.emptyList();
    }

    // SINGLE RETRIEVAL

    @Override
    public BukkitFutureResult<Optional<T>> first() {
        return CompletableBukkitFuture.supplyAsync(this::firstSync);
    }

    @Override
    public BukkitFutureResult<Optional<T>> first(Executor executor) {
        return CompletableBukkitFuture.supplyAsync(this::firstSync, executor);
    }

    @Override
    public Optional<T> firstSync() {
        try (Connection conn = dataSource.getConnection()) {
            autoCommit(conn);
            Optional<T> result = executeAndGetLast(conn).retrieveResult(conn);
            commit(conn);
            return result;
        } catch (SQLException e) {
            handleException(e);
        }
        return Optional.empty();
    }

    // UPDATE STAGE

    @Override
    public BukkitFutureResult<Integer> execute() {
        return BukkitFutureResult.of(CompletableFuture.supplyAsync(this::executeSync));
    }

    @Override
    public BukkitFutureResult<Integer> execute(Executor executor) {
        return BukkitFutureResult.of(CompletableFuture.supplyAsync(this::executeSync, executor));
    }

    @Override
    public int executeSync() {
        try (Connection conn = dataSource.getConnection()) {
            autoCommit(conn);
            int update = executeAndGetLast(conn).update(conn);
            commit(conn);
            return update;
        } catch (SQLException e) {
            handleException(e);
        }
        return 0;
    }


    /*
    Execute all queries, since we are only interested in the result of the last of our queries.
    Thats why we will execute all queries inside this method regardless of the method which calls this method
    We will use a single connection for this, since the user may be interested in the last inserted id or something.
    */

    private QueryTask executeAndGetLast(Connection conn) throws SQLException {
        while (tasks.size() > 1) {
            tasks.poll().execute(conn);
        }
        return tasks.poll();
    }

    private void logDbError(SQLException e) {
        plugin.getLogger().log(Level.SEVERE, "An SQL query occured:\n" + DBUtil.prettyException(e), e);
    }

    private void handleException(SQLException e) {
        if (config.isThrowing()) {
            wrappedExecutionException.initCause(e);
            throw wrappedExecutionException;
        }
        executionException.initCause(e);
        logDbError(e);
    }

    private void autoCommit(Connection conn) throws SQLException {
        conn.setAutoCommit(!config.isAtomic());
    }

    private void commit(Connection conn) throws SQLException {
        if (config.isAtomic()) conn.commit();
    }

    private class QueryTask {
        private final String query;
        private final ThrowingConsumer<PreparedStatement, SQLException> statementConsumer;
        private final ThrowingFunction<T, ResultSet, SQLException> resultMapper;
        private final QueryExecutionException executionException;

        public QueryTask(String currQuery, ThrowingConsumer<PreparedStatement, SQLException> statementConsumer,
                         ThrowingFunction<T, ResultSet, SQLException> resultMapper) {
            query = currQuery;
            this.statementConsumer = statementConsumer;
            this.resultMapper = resultMapper;
            executionException = new QueryExecutionException("An error occured while executing a query.");
        }

        private void initAndThrow(SQLException e) throws SQLException {
            executionException.initCause(e);
            throw executionException;
        }

        public List<T> retrieveResults(Connection conn) throws SQLException {
            List<T> results = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                statementConsumer.accept(stmt);
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    results.add(resultMapper.apply(resultSet));
                }
            } catch (SQLException e) {
                initAndThrow(e);
            }
            return results;
        }

        public Optional<T> retrieveResult(Connection conn) throws SQLException {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                statementConsumer.accept(stmt);
                ResultSet resultSet = stmt.executeQuery();
                if (resultSet.next()) {
                    return Optional.ofNullable(resultMapper.apply(resultSet));
                }
            } catch (SQLException e) {
                initAndThrow(e);
            }
            return Optional.empty();
        }

        public void execute(Connection conn) throws SQLException {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                statementConsumer.accept(stmt);
                stmt.execute();
            } catch (SQLException e) {
                initAndThrow(e);
            }
        }

        public int update(Connection conn) throws SQLException {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                statementConsumer.accept(stmt);
                return stmt.executeUpdate();
            } catch (SQLException e) {
                initAndThrow(e);
            }
            return 0;
        }
    }
}
