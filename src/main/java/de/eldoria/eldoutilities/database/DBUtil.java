package de.eldoria.eldoutilities.database;

import java.sql.SQLException;

public final class DBUtil {
    private DBUtil() {
        throw new UnsupportedOperationException("This is a utility class.");
    }
    public static String prettyException(SQLException ex) {
        return String.format("SQLException: %s%nSQLState: %s%nVendorError: %s",
                ex.getMessage(), ex.getSQLState(),  ex.getErrorCode() );
    }

    public static String prettyException(String message, SQLException ex) {
        return String.format("%s:%nSQLException: %s%nSQLState: %s%nVendorError: %s",
                message, ex.getMessage(), ex.getSQLState(),  ex.getErrorCode() );
    }
}
