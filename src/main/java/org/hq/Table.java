package org.hq;


import org.apache.commons.lang3.StringUtils;
import org.hq.enums.ColumnType;
import org.hq.utils.DataDiff;
import org.hq.utils.SqlUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.hq.utils.DataConnect.DataSource;
import static org.hq.utils.DataConnect.DataTarget;

/**
 * @Date 2020/6/12 10:11 上午
 * @Created by haoqi
 */
public class Table {

    public static List<String> getSourceTables() throws SQLException {
        try (ResultSet rs = SqlUtil.getResultSet(DataSource.getConnection(), SqlUtil.SHOW_TABLE_NOT_VIEW)) {
            List<String> tables = new ArrayList<>();
            while (rs.next()) {
                tables.add(rs.getString(SqlUtil.getTablesName(DataSource.SCHEMA_NAME)));
            }
            return tables;
        }
    }

    public static List<String> getTargetTables() throws SQLException {
        try (ResultSet rs = SqlUtil.getResultSet(DataTarget.getConnection(), SqlUtil.SHOW_TABLE_NOT_VIEW)) {
            List<String> tables = new ArrayList<>();
            while (rs.next()) {
                tables.add(rs.getString(SqlUtil.getTablesName(DataTarget.SCHEMA_NAME)));
            }
            return tables;
        }
    }

    public static Map<String, Map<String, Map<ColumnType, Object>>> getSourceTablesColumns() throws SQLException {
        return getSourceTables().stream().collect(HashMap::new, (a, b) -> {
            try (ResultSet rs = SqlUtil.getResultSet(DataSource.getConnection(), SqlUtil.getColumns(b))) {
                a.putAll(addTable(rs, b));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }, HashMap::putAll);
    }

    public static Map<String, Map<String, Map<ColumnType, Object>>> getTargetTablesColumns() throws SQLException {
        return getTargetTables().stream().collect(HashMap::new, (a, b) -> {
            try (ResultSet rs = SqlUtil.getResultSet(DataTarget.getConnection(), SqlUtil.getColumns(b))) {
                a.putAll(addTable(rs, b));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }, HashMap::putAll);
    }

    public static StringJoiner getDiffTables() throws SQLException {
        Map<String, Object> map = DataDiff.diffTablesOrViews(getSourceTablesColumns(), getTargetTablesColumns());
        StringJoiner joiner = new StringJoiner(";\n");

        Map diffTables = (HashMap) map.get("diffFields");
        diffTables.forEach((tableName, diffFields) -> {
            //fieldsKey 字段名称
            //fieldsValue 属性/值集合
            ((HashMap) diffFields).forEach((fieldsKey, fieldsValue) -> {
                joiner.add(getColumns(fieldsValue));
            });
        });


        List<String> newTables = (ArrayList<String>) map.get("newTables");
        newTables.forEach(tableName -> {
            try (ResultSet rs = SqlUtil.getResultSet(DataSource.getConnection(), SqlUtil.getTableDetails(tableName))) {
                while (rs.next()) {
                    //创建表语句
                    joiner.add(rs.getString("Create Table"));
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        return joiner;
    }

    public static Map<String, Map<String, Map<ColumnType, Object>>> addTable(ResultSet rs, String tableName) throws SQLException {
        Map<String, Map<String, Map<ColumnType, Object>>> tables = new HashMap<>();

        Map<String, Map<ColumnType, Object>> table = new HashMap<>();
        while (rs.next()) {
            String field = rs.getString(ColumnType.Field.name());

            Map<ColumnType, Object> map = new HashMap<>();
            map.put(ColumnType.TableName, tableName);
            map.put(ColumnType.Field, field);
            map.put(ColumnType.Type, rs.getString(ColumnType.Type.name()));
            map.put(ColumnType.Null, rs.getString(ColumnType.Null.name()));
            map.put(ColumnType.Key, rs.getString(ColumnType.Key.name()));
            map.put(ColumnType.Default, rs.getString(ColumnType.Default.name()));
            map.put(ColumnType.Extra, rs.getString(ColumnType.Extra.name()));
            map.put(ColumnType.Comment, rs.getString(ColumnType.Comment.name()));

            table.put(field, map);
        }
        tables.put(tableName, table);
        return tables;
    }

    private static String getColumns(Object fieldsValue) {
        StringBuilder builder = new StringBuilder();
        HashMap field = (HashMap) fieldsValue;
        HashMap<ColumnType, String> fieldVal;
        if (field.keySet().size() == 2) {
            //存在字段差异
            fieldVal = (HashMap<ColumnType, String>) field.get(SqlUtil.SOURCE);
            builder.append("alter table ")
                    .append(fieldVal.get(ColumnType.TableName))
                    .append(" modify ");

        } else {
            fieldVal = (HashMap<ColumnType, String>) field;
            builder.append("alter table ")
                    .append(fieldVal.get(ColumnType.TableName))
                    .append(" add ");
        }

        builder.append(fieldVal.get(ColumnType.Field))
                .append(" ")
                .append(fieldVal.get(ColumnType.Type));
        if (!StringUtils.isEmpty(fieldVal.get(ColumnType.Default))) {
            builder.append(" default ");
            if (StringUtils.contains(fieldVal.get(ColumnType.Default), "'")) {
                builder.append(fieldVal.get(ColumnType.Default));
            } else {
                builder.append("'").append(fieldVal.get(ColumnType.Default)).append("'");
            }
        }

        if (!StringUtils.isEmpty(fieldVal.get(ColumnType.Extra))) {
            builder.append(" ").append(fieldVal.get(ColumnType.Extra));
        }

        if (StringUtils.contains(fieldVal.get(ColumnType.Null), "YES")) {
            builder.append(" NULL ");
        } else {
            builder.append(" NOT NULL ");
        }

        if (!StringUtils.isEmpty(fieldVal.get(ColumnType.Comment))) {
            builder.append("comment '").append(fieldVal.get(ColumnType.Comment)).append("'");
        }
        return builder.toString();
    }
}
