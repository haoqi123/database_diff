package org.hq.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @Date 2020/6/22 2:20 下午
 * @Created by haoqi
 */
public class SqlUtil {
    public static final String SOURCE = "source";
    public static final String TARGET = "targets";

    public static final String TABLE_NAME = "TABLE_NAME";
    public static final String SHOW_CREATE_TABLE = "SHOW CREATE TABLE `$1`";
    public static final String SHOW_INDEX_FROM = "SHOW INDEX FROM `$1`";
    public static final String SHOW_TABLE_NOT_VIEW = "SHOW FULL TABLES WHERE Table_type != 'VIEW'";
    public static final String SHOW_VIEW = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_SCHEMA = '$1' ORDER BY TABLE_NAME ASC";
    public static final String SHOW_ROUTINES = "SELECT SPECIFIC_NAME,ROUTINE_TYPE,ROUTINE_DEFINITION,ROUTINE_COMMENT FROM information_schema.ROUTINES WHERE ROUTINE_SCHEMA = '$1' ORDER BY ROUTINE_NAME";
    public static final String SHOW_COLUMN = "SHOW FULL COLUMNS FROM `$1`";

    public static Statement getStatement(Connection connection) throws SQLException {
        return connection.createStatement();
    }

    public static ResultSet getResultSet(Connection connection, String sql) throws SQLException {
        return getStatement(connection).executeQuery(sql);
    }

    public static String getTablesName(String schemaName) {
        return "Tables_in_" + schemaName;
    }

    public static String getFieldNameView(String schemaName) {
        return SHOW_VIEW.replace("$1", schemaName.trim());
    }

    public static String getFieldNameRoutines(String schemaName) {
        return SHOW_ROUTINES.replace("$1", schemaName.trim());
    }

    public static String getColumns(String tableName) {
        return SHOW_COLUMN.replace("$1", tableName.trim());
    }

    public static String getTableDetails(String tableName) {
        return SHOW_CREATE_TABLE.replace("$1", tableName.trim());
    }

    public static String getTableIndex(String tableName) {
        return SHOW_INDEX_FROM.replace("$1", tableName.trim());
    }
}
