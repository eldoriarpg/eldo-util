package de.eldoria.eldoutilities.database.querybuilder;

import de.eldoria.eldoutilities.consumer.ThrowingConsumer;
import de.eldoria.eldoutilities.database.DBUtil;
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

public class QueryBuilder<T> implements QueryStage<T>, StatementStage<T>, ResultStage<T>, RetrievalStage<T>, UpdateStage {
    private final Plugin plugin;
    private final DataSource dataSource;
    private final Queue<QueryTask> tasks = new ArrayDeque<>();
    private final SQLException executionException;

    private String currQuery;
    private ThrowingConsumer<PreparedStatement, SQLException> currStatementConsumer;
    private ThrowingFunction<T, ResultSet, SQLException> currResultMapper;
    private boolean atomic;

    private QueryBuilder(Plugin plugin, DataSource dataSource, Class<T> clazz) {
        this.plugin = plugin;
        this.dataSource = dataSource;
        executionException = new QueryExecutionException("An error occured while executing a query.");
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
    public static <T> QueryStage<T> builder(Plugin plugin, DataSource source, Class<T> clazz) {
        return new QueryBuilder<>(plugin, source, clazz);
    }

    /**
     * Create a new Query builder without a defined return type. Use it for updates.
     *
     * @param plugin plugin to execute async stuff
     * @param source datasource for connection
     * @return a new query builder in a {@link QueryStage}
     */
    public static QueryStage<?> builder(Plugin plugin, DataSource source) {
        return new QueryBuilder<>(plugin, source, null);
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

    // LISTS

    @Override
    public RetrievalStage<T> notAtomic(boolean isAtomic) {
        this.atomic = isAtomic;
        return this;
    }

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
            conn.setAutoCommit(!atomic);
            List<T> results = executeAndGetLast(conn).retrieveResults(conn);
            if (atomic) {
                conn.commit();
            }
            return results;
        } catch (SQLException e) {
            executionException.initCause(e);
            logDbError(executionException);
        }
        return Collections.emptyList();
    }

    // SINGLE

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
            conn.setAutoCommit(!atomic);
            Optional<T> t = executeAndGetLast(conn).retrieveResult(conn);
            if (atomic) {
                conn.commit();
            }
            return t;
        } catch (SQLException e) {
            logDbError(e);
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
            conn.setAutoCommit(!atomic);
            int update = executeAndGetLast(conn).update(conn);
            if (atomic) {
                conn.commit();
            }
            return update;
        } catch (SQLException e) {
            logDbError(e);
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

    public void logDbError(SQLException e) {
        plugin.getLogger().log(Level.SEVERE, "An SQL query occured:\n" + DBUtil.prettyException(e), e);
    }

    private class QueryTask {
        private final String query;
        private final ThrowingConsumer<PreparedStatement, SQLException> statementConsumer;
        private final ThrowingFunction<T, ResultSet, SQLException> resultMapper;


        public QueryTask(String currQuery, ThrowingConsumer<PreparedStatement, SQLException> statementConsumer,
                         ThrowingFunction<T, ResultSet, SQLException> resultMapper) {
            query = currQuery;
            this.statementConsumer = statementConsumer;
            this.resultMapper = resultMapper;
        }

        public List<T> retrieveResults(Connection conn) throws SQLException {
            List<T> results = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                statementConsumer.accept(stmt);
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    results.add(resultMapper.apply(resultSet));
                }
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
            }
            return Optional.empty();
        }

        public void execute(Connection conn) throws SQLException {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                statementConsumer.accept(stmt);
                stmt.execute();
            }
        }

        public int update(Connection conn) throws SQLException {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                statementConsumer.accept(stmt);
                return stmt.executeUpdate();
            }
        }
    }
}
