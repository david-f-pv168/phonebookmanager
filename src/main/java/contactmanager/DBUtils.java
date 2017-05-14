package contactmanager;

import common.IllegalEntityException;
import common.ServiceFailureException;
import org.apache.derby.jdbc.EmbeddedDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.log4j.helpers.Loader.getResource;

/**
 * Created by David Frankl on 24-Mar-17.
 */
public class DBUtils {

    private static final Logger logger = Logger.getLogger(
            DBUtils.class.getName());

    /**
     * Closes connection and logs possible error.
     *
     * @param connection connection to close
     * @param statements  statements to close
     */
    public static void closeQuietly(Connection connection, Statement... statements) {
        for (Statement statement : statements) {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ex) {
                    logger.log(Level.SEVERE, "Error during statement closing.", ex);
                }
            }
        }
        if (connection != null) {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error when setting autocommit back to true.", ex);
            }
            try {
                connection.close();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error during connection closing.", ex);
            }
        }
    }

    /**
     * Rolls back transaction and logs possible error.
     *
     * @param connection connection
     */
    public static void doRollbackQuietly(Connection connection) {
        if (connection != null) {
            try {
                if (connection.getAutoCommit()) {
                    throw new IllegalStateException("Connection is in the autocommit mode.");
                }
                connection.rollback();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error during rollback", ex);
            }
        }
    }

    /**
     * Extract key from Result set.
     *
     * @param key resultSet with key
     * @return key from given result set
     * @throws SQLException if operation fails
     */
    public static Long getId(ResultSet key) throws SQLException {
        if (key.getMetaData().getColumnCount() != 1) {
            throw new IllegalArgumentException("Given Result set contains more than one column.");
        }
        if (key.next()) {
            Long result = key.getLong(1);
            if (key.next()) {
                throw new IllegalArgumentException("Given Result set contains more than one row.");
            }
            return result;
        } else {
            throw new IllegalArgumentException("Given Result set contains no rows.");
        }
    }

    /**
     * Reads SQL statements from file.
     *
     * @param url url of the file
     * @return array of command  strings
     */
    private static String[] readSqlStatements(URL url) {
        try {
            char buffer[] = new char[256];
            StringBuilder result = new StringBuilder();
            InputStreamReader reader = new InputStreamReader(url.openStream(), "UTF-8");
            while (true) {
                int count = reader.read(buffer);
                if (count < 0) {
                    break;
                }
                result.append(buffer, 0, count);
            }
            return result.toString().split(";");
        } catch (IOException ex) {
            throw new RuntimeException("Could't read " + url, ex);
        }
    }

    /**
     * Executes SQL script.
     *
     * @param ds datasource
     * @param scriptUrl url of sql script to be executed
     * @throws SQLException if operation fails
     */
    public static void executeSqlScript(DataSource ds, URL scriptUrl) throws SQLException {
        Connection connection = null;
        try {
            connection = ds.getConnection();
            for (String sqlStatement : readSqlStatements(scriptUrl)) {
                if (!sqlStatement.trim().isEmpty()) {
                    connection.prepareStatement(sqlStatement).executeUpdate();
                }
            }
        } finally {
            closeQuietly(connection);
        }
    }

    /**
     * Check if the number of updates is one. Exception is thrown otherwise.
     *
     * @param count updates count.
     * @param entity updated entity (for including to error message)
     * @param insert flag if performed operation was insert
     * @throws IllegalEntityException when updates count is zero, so updated entity does not exist
     * @throws ServiceFailureException when updates count is unexpected number
     */
    public static void checkUpdatesCount(int count, Object entity, boolean insert)
            throws IllegalEntityException, ServiceFailureException {

        if (!insert && count == 0) {
            throw new IllegalEntityException("Entity " + entity + " does not exist in the db.");
        }
        if (count != 1) {
            throw new ServiceFailureException("Internal integrity error: Unexpected number of rows in database" +
                    " affected: " + count);
        }
    }

    /**
     * Converts SQL date to java LocalDate
     *
     * @param date: date in SQL format
     * @return date in java LocalDate format
     */
    public static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    /**
     * Converts java LocalDate to SQL date
     *
     * @param localDate: date in java LocalDate format
     * @return date in SQL format
     */
    public static Date toSqlDate(LocalDate localDate) {
        return localDate == null ? null : Date.valueOf(localDate);
    }

    public static DataSource createMemoryDatabaseWithTables(boolean withData) throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName("memory:contactsDB;create=true");
        executeSqlScript(ds, DBUtils.class.getResource("/createTables.sql"));

        if (withData) {
            executeSqlScript(ds, getResource("/populateTables.sql"));
        }

        return ds;
    }
}