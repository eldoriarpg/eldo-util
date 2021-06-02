package de.eldoria.eldoutilities.database.builder;

import de.eldoria.eldoutilities.database.builder.stage.QueryStage;

import java.util.List;
import java.util.Optional;

class QueryBuilderTest {
    public void syncedResult() {
        Optional<String> syncedResult = builder(String.class)
                .query("SELECT something FROM table WHERE key = ?")
                .params(stmt -> stmt.setString(1, "foo"))
                .readRow(rs -> rs.getString("something"))
                .firstSync();
    }

    public void asyncResult() {
        builder(String.class)
                .query("SELECT something FROM table WHERE key = ?")
                .params(stmt -> stmt.setString(1, "foo"))
                .readRow(rs -> rs.getString("something"))
                .first()
                .whenComplete(result -> {
                    Optional<String> rs = result;
                });
    }

    public void syncedResults() {
        // Retrieve list of results synced
        List<String> syncedResults = builder(String.class)
                .query("SELECT something FROM table WHERE key = ?")
                .params(stmt -> stmt.setString(1, "foo"))
                .readRow(rs -> rs.getString("something"))
                .allSync();
    }

    public void asyncResults() {
        builder(String.class)
                .query("SELECT something FROM table WHERE key = ?")
                .params(stmt -> stmt.setString(1, "foo"))
                .readRow(rs -> rs.getString("something"))
                .first()
                .whenComplete(results -> {
                    // do something
                });
    }

    public void updateSynced() {
        builder(null)
                .query("UPDATE table SET col1 = ? WHERE key = ?")
                .params(stmt -> {
                    stmt.setString(1, "newVal");
                    stmt.setString(2, "some");
                })
                .update()
                .executeSync();

        // Without statements
        builder(null)
                .query("DELETE FROM table")
                .emptyParams()
                .update()
                .executeSync();
    }

    public void updateAsync() {
        builder(null)
                .query("UPDATE table SET col1 = ? WHERE key = ?")
                .params(stmt -> {
                    stmt.setString(1, "newVal");
                    stmt.setString(2, "some");
                })
                .update()
                .execute();

        // Without statements
        builder(null)
                .query("DELETE FROM table")
                .emptyParams()
                .update()
                .execute();
    }

    public void updateAsyncAppend() {
        builder(Long.class)
                .query("INSERT INTO table(first, second) VALUES(?, ?)")
                .params(stmt -> {
                    stmt.setString(1, "newVal");
                    stmt.setString(2, "some");
                })
                .append()
                .queryWithoutParams("SELECT LAST_INSERT_ID()")
                .readRow(r -> r.getLong(1))
                .first()
                .whenComplete(id -> {
                    System.out.println("Inserted new entry with id " + id);
                });
    }

    private <T> QueryStage<T> builder(Class<T> clazz) {
        return QueryBuilder.builder(null, null, clazz);
    }
}