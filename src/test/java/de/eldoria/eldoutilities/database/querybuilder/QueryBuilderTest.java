package de.eldoria.eldoutilities.database.querybuilder;

import java.util.List;
import java.util.Optional;

class QueryBuilderTest {
    public void syncedResult() {
        Optional<String> syncedResult = builder(String.class)
                .setQuery("SELECT something FROM table WHERE key = ?")
                .setStatements(stmt -> stmt.setString(1, "foo"))
                .extractResults(rs -> rs.getString("something"))
                .retrieveResult();
    }

    public void asyncResult() {
        builder(String.class)
                .setQuery("SELECT something FROM table WHERE key = ?")
                .setStatements(stmt -> stmt.setString(1, "foo"))
                .extractResults(rs -> rs.getString("something"))
                .retrieveResultAsync()
                .queue(result -> {
                    Optional<String> rs = result;
                });
    }

    public void syncedResults() {
        // Retrieve list of results synced
        List<String> syncedResults = builder(String.class)
                .setQuery("SELECT something FROM table WHERE key = ?")
                .setStatements(stmt -> stmt.setString(1, "foo"))
                .extractResults(rs -> rs.getString("something"))
                .retrieveResults();
    }

    public void asyncResults() {
        builder(String.class)
                .setQuery("SELECT something FROM table WHERE key = ?")
                .setStatements(stmt -> stmt.setString(1, "foo"))
                .extractResults(rs -> rs.getString("something"))
                .retrieveResultAsync()
                .queue(results -> {
                    // do something
                });
    }

    public void updateSynced() {
        builder(null)
                .setQuery("UPDATE table SET col1 = ? WHERE key = ?")
                .setStatements(stmt -> {
                    stmt.setString(1, "newVal");
                    stmt.setString(2, "some");
                })
                .update()
                .executeUpdate();

        // Without statements
        builder(null)
                .setQuery("DELETE FROM table")
                .emptyStatements()
                .update()
                .executeUpdate();
    }

    public void updateAsync() {
        builder(null)
                .setQuery("UPDATE table SET col1 = ? WHERE key = ?")
                .setStatements(stmt -> {
                    stmt.setString(1, "newVal");
                    stmt.setString(2, "some");
                })
                .update()
                .executeUpdateAsync()
                .queue();

        // Without statements
        builder(null)
                .setQuery("DELETE FROM table")
                .emptyStatements()
                .update()
                .executeUpdateAsync()
                .queue();
    }

    private <T> QueryStage<T> builder(Class<T> clazz) {
        return QueryBuilder.builder(null, null, clazz);
    }
}