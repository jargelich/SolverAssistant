/*
 * TFG: Daniel Casanovas Gayoso 
 * Grau en Enginyeria Informàtica - Escola Politècnica Superior de Lleida
 * 2014/2015
 * DataBaseManager Manager
 */
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataBaseManager {

    private final String sDriver = "org.sqlite.JDBC";
    private final String sUrl = "jdbc:sqlite:solverAssistant.db";
    private final int timeout = 30;
    private Connection conn = null;
    private PreparedStatement statement = null;

    private final String creationTableSolverSQL = "CREATE TABLE IF NOT EXISTS `Solver` ("
            + "  `SolverId` INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "  `Name` TEXT DEFAULT NULL,"
            + "  `Benchmark` TEXT DEFAULT NULL,"
            + "  `Type` TEXT DEFAULT NULL,"
            + "  `TimeOut` INTEGER DEFAULT NULL,"
            + "  `Memory` INTEGER DEFAULT NULL,"
            + "  `NumberOfCores` INTEGER DEFAULT NULL"
            + ");";

    private final String creationTableSolverInstanceSQL = " CREATE TABLE IF NOT EXISTS `SolverInstance` ("
            + "  `SolverInstanceId` INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "  `Instance` TEXT DEFAULT NULL,"
            + "  `Time` NUMERIC DEFAULT NULL,"
            + "  `Optimum` NUMERIC DEFAULT NULL,"
            + "  `Solution` INTEGER DEFAULT NULL,"
            + "  `Info` INTEGER DEFAULT NULL,"
            + "  `TimeOut` INTEGER DEFAULT NULL,"
            + "  `Buggy` INTEGER DEFAULT NULL,"
            + "  `SegmentationFault` INTEGER DEFAULT NULL,"
            + "  `Log` TEXT DEFAULT NULL,"
            + "  `OutOfMemory` INTEGER DEFAULT NULL,"
            + "  `NumberOfVariables` INTEGER DEFAULT NULL,"
            + "  `NumberOfClause` INTEGER DEFAULT NULL,"
            + "  `NumberOfHardClause` INTEGER DEFAULT NULL,"
            + "  `NumberOfSoftClause` INTEGER DEFAULT NULL,"
            + "  `NumberOfUnsatClause` INTEGER DEFAULT NULL,"
            + "  `NumberOfUnsatClauseWeigth` INTEGER DEFAULT NULL,"
            + "  `SolverId` INTEGER REFERENCES `Solver`(`SolverId`)"
            + "    ON DELETE CASCADE"
            + "    ON UPDATE CASCADE"
            + ");";

    // To use, use empty constructor and after call init function with the db name or use the second constructor with the db name
    // After that execute executeUp or executeQr
    public DataBaseManager() {
        init();
    }

    private void init() {
        try {
            runInitialScript();
        } catch (Exception ex) {
            System.err.println("[ERROR-INFO (DB Manager)] Failed to initiate DB: " + ex.getMessage());
        }
    }

    public void openConnection() {
        try {
            Class.forName(sDriver);
            conn = DriverManager.getConnection(getUrl());
        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("[ERROR-INFO (DB Manager)] Failed to open connection DB: " + ex.getMessage());
        }
    }

    public String getUrl() {
        return sUrl;
    }

    public Connection getConnection() {
        return conn;
    }

    public PreparedStatement getStatement(String sql) throws SQLException {
        statement = conn.prepareStatement(sql);
        statement.setQueryTimeout(timeout);
        return statement;
    }

    public void closeConnection() {
        try {
            conn.close();
        } catch (Exception ignore) {
            System.err.println("[ERROR-INFO] - " + ignore);
        }
    }

    public void runInitialScript() {
        PreparedStatement pst = null;
        try {
            openConnection();
            pst = getStatement(creationTableSolverSQL);
            pst.executeUpdate();
            pst = getStatement(creationTableSolverInstanceSQL);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ERROR-INFO (Initial Script)] - " + e);
        } finally {
            try {
                if (pst != null) {
                    pst.close();
                }
            } catch (SQLException ex) {
                System.err.println("[ERROR-INFO (Initial Script)] - " + ex);
            }
            closeConnection();
        }
    }
}
