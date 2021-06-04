package de.eldoria.eldoutilities.database;

import java.sql.SQLException;

/**
 * Utilitites for database handling
 */
public final class DBUtil {
    private DBUtil() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    /**
     * Transforms a SQLException in an easy readable string with a message.
     *
     * @param ex sql exception
     * @return input as pretty string
     */
    public static String prettyException(SQLException ex) {
        return String.format("SQLException: %s%nSQLState: %s%nVendorError: %s",
                ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
    }

    /**
     * Transforms a SQLException in an easy readable string with a message.
     *
     * @param message Message
     * @param ex      sql exception
     * @return input as pretty string
     */
    public static String prettyException(String message, SQLException ex) {
        return String.format("%s:%nSQLException: %s%nSQLState: %s%nVendorError: %s",
                message, ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
    }
}
