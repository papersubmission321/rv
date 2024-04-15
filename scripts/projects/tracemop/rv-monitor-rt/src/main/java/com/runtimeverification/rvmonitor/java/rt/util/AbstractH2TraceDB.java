package com.runtimeverification.rvmonitor.java.rt.util;

import org.h2.tools.Csv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractH2TraceDB implements TraceDB{
    private Connection connection;

    String dbDir = "/tmp/tracedb";

    private final String h2Options =";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;DEFRAG_ALWAYS=TRUE;LOB_TIMEOUT=30000000;CACHE_SIZE=2048000";

    private String jdbcURL = "jdbc:h2:" + dbDir + h2Options;
    private String jdbcUsername = "tdb";

    private String jdbcPassword = "";

    public AbstractH2TraceDB() {
        updateSystemProperty();
        this.connection = getConnection();
    }

    public AbstractH2TraceDB(String dbFilePath) {
        this.jdbcURL =  "jdbc:h2:" + dbFilePath + h2Options;
        setDbDir(dbFilePath);
        updateSystemProperty();
        this.connection = getConnection();
    }

    public int size(String query) {
        int count = -1;
        try(Statement statement =  getConnection().createStatement()){
            ResultSet rs = statement.executeQuery(query);
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return count;
    }

    public int uniqueTraces(String query) {
        int count = -1;
        try (Statement statement = getConnection().createStatement()) {
            ResultSet rs = statement.executeQuery(query);
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return count;
    }

    public void dump(String csvDir, String tableName) {
        final String SELECT_QUERY = "select * from " + tableName;
        try(PreparedStatement preparedStatement = getConnection().prepareStatement(SELECT_QUERY)){
            ResultSet rs = preparedStatement.executeQuery();
            new Csv().write(csvDir, rs, null);
        } catch (SQLException e) {
            printSQLException(e);
        }
    }

    protected Connection getConnection() {
        if (connection != null) {
            return connection;
        }
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
        } catch (SQLException e) {
            printSQLException(e);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        return connection;
    }

    public List<Integer> getTraceLengths(String query) {
        List<Integer> lengths =  new ArrayList<>();
        try (Statement statement = getConnection().createStatement()) {
            ResultSet rs =  statement.executeQuery(query);
            while (rs.next()) {
                lengths.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return lengths;
    }

    public Map<String, Integer> getTraceFrequencies(String query) {
        Map<String, Integer> traceFrequency = new HashMap<>();
        try(Statement statement = getConnection().createStatement()) {
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
                traceFrequency.put(rs.getString(2), rs.getInt(1));
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return traceFrequency;
    }

    protected void printSQLException(SQLException ex) {
        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                e.printStackTrace(System.err);
                System.err.println("SQLState: " + ((SQLException) e).getSQLState());
                System.err.println("Error Code: " + ((SQLException) e).getErrorCode());
                System.err.println("Message: " + e.getMessage());
                Throwable t = ex.getCause();
                while (t != null) {
                    System.out.println("Cause: " + t);
                    t = t.getCause();
                }
            }
        }
    }

    public String getDbDir() {
        return dbDir;
    }

    public void setDbDir(String dbDir) {
        this.dbDir = dbDir;
    }

    /**
     * Update system properties to include dbDir information so that JUnit Listeners that work on the DB have access.
     */
    private void updateSystemProperty() {
        System.setProperty("dbFilePath", this.dbDir);
        System.err.println("[AbstractH2TraceDB] Set dbFilePath to: " + System.getProperty("dbFilePath"));
    }
}
