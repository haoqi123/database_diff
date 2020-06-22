package com.example.database_diff.controllers;

import com.example.database_diff.enums.ColumnType;
import com.example.database_diff.utils.DbUtil;
import com.example.database_diff.utils.DbUtilV2;
import com.example.database_diff.utils.TextDiff;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * @Date 2020/6/12 10:11 上午
 * @Created by haoqi
 */
@Slf4j
@RestController
@RequestMapping("sql")
public class SqlDifferenceController {
    private static final String SHOW_TABLE = "SHOW TABLES";
    private static final String SHOW_TABLE_NOT_VIEW = "SHOW FULL TABLES WHERE Table_type != 'VIEW'";
    private static final String SHOW_COLUMN = "SHOW COLUMNS FROM ";

    public Statement getStatement(Connection connection) throws SQLException {
        return connection.createStatement();
    }

    public ResultSet getResultV1() throws SQLException {
        return getStatement(DbUtil.getConnection()).executeQuery(SHOW_TABLE_NOT_VIEW);
    }

    public ResultSet getResultV2() throws SQLException {
        return getStatement(DbUtilV2.getConnection()).executeQuery(SHOW_TABLE_NOT_VIEW);
    }

    public String tableName() {
        return "Tables_in_" + DbUtil.databaseName;
    }

    public String tableName2() {
        return "Tables_in_" + DbUtilV2.databaseName;
    }

    @GetMapping("getTables")
    public List<String> getTables() throws SQLException {
        ResultSet rs = getResultV1();
        List<String> tables = new ArrayList<>();
        while (rs.next()) {
            String tables_in_tdasapp = rs.getString(tableName());
            tables.add(tables_in_tdasapp);
        }
        return tables;
    }

    @GetMapping("getTablesV2")
    public List<String> getTablesV2() throws SQLException {
        ResultSet rs = getResultV2();
        List<String> tables = new ArrayList<>();
        while (rs.next()) {
            String tables_in_tdasapp = rs.getString(tableName2());
            tables.add(tables_in_tdasapp);
        }
        return tables;
    }

    @GetMapping("getTableColumns/{tableName}")
    public Object getTableColumns(@PathVariable String tableName) throws SQLException {
        Connection conn = DbUtil.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(addTableName(tableName));

        List<Map<String, Object>> list = new ArrayList<>();
        while (rs.next()) {
            String field = rs.getString(ColumnType.Field.name());
            String type = rs.getString(ColumnType.Type.name());
            String aNull = rs.getString(ColumnType.Null.name());
            String key = rs.getString(ColumnType.Key.name());
            String aDefault = rs.getString(ColumnType.Default.name());

            Map<String, Object> map = new HashMap<>();
            map.put(ColumnType.Field.name(), field);
            map.put(ColumnType.Type.name(), type);
            map.put(ColumnType.Null.name(), aNull);
            map.put(ColumnType.Key.name(), key);
            map.put(ColumnType.Default.name(), aDefault);
            list.add(map);
        }
        return list;
    }

    @PostMapping("getTablesColumns")
    public Map<String, Map<String, Map<ColumnType, Object>>> getTablesColumns() throws SQLException {
        return getTables().stream().collect(HashMap::new, (a, b) -> {
            try {
                ResultSet rs = getStatement(DbUtil.getConnection()).executeQuery(addTableName(b));
                a.putAll(addTable(rs, b));
                rs.close();
            } catch (SQLException throwables) {
                log.error(b);
            }
        }, HashMap::putAll);
    }

    private static String addTableName(String tableName) {
        return SHOW_COLUMN + "`" + tableName + "`";
    }

    @PostMapping("getTablesColumnsV2")
    public Map<String, Map<String, Map<ColumnType, Object>>> getTablesColumnsV2() throws SQLException {
        return getTablesV2().stream().collect(HashMap::new, (a, b) -> {
            try {
                ResultSet rs = getStatement(DbUtilV2.getConnection()).executeQuery(addTableName(b));
                a.putAll(addTable(rs, b));
                rs.close();
            } catch (SQLException throwables) {
                log.error(b);
            }
        }, HashMap::putAll);
    }

    @PostMapping("getDiffTables")
    public Object getDiffTables() throws SQLException {
        return new TreeMap<>(TextDiff.diff(getTablesColumns(), getTablesColumnsV2()));
    }

    public Map<String, Map<String, Map<ColumnType, Object>>> addTable(ResultSet rs, String tableName) throws SQLException {
        Map<String, Map<String, Map<ColumnType, Object>>> tables = new HashMap<>();

        Map<String, Map<ColumnType, Object>> table = new HashMap<>();
        while (rs.next()) {
            String field = rs.getString(ColumnType.Field.name());
            String type = rs.getString(ColumnType.Type.name());
            String aNull = rs.getString(ColumnType.Null.name());
            String key = rs.getString(ColumnType.Key.name());
            String aDefault = rs.getString(ColumnType.Default.name());

            Map<ColumnType, Object> map = new HashMap<>();
            map.put(ColumnType.TableName, tableName);
            map.put(ColumnType.Field, field);
            map.put(ColumnType.Type, type);
            map.put(ColumnType.Null, aNull);
            map.put(ColumnType.Key, key);
            map.put(ColumnType.Default, aDefault);

            table.put(field, map);
        }
        tables.put(tableName, table);
        return tables;
    }

}
