package org.hq.utils;


import org.hq.enums.ColumnType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class DataDiff {

    /**
     * 获取表的对比结果
     * 包含没有添加的表，不同的字段属性
     */
    public static Map<String, Object> diffTablesOrViews(Map<String, Map<String, Map<ColumnType, Object>>> source,
                                                        Map<String, Map<String, Map<ColumnType, Object>>> target) {


        Map<String, Map<String, Object>> diffFields = new HashMap<>();
        List<String> diffTables = new ArrayList<>();

        source.forEach((tableName, tableDetails) -> {

            //对比缺少的表
            //table2对比table1没有的表名
            if (!target.containsKey(tableName)) {
                diffTables.add(tableName);
                return;
            }
            //对比不同的字段
            Map<String, Object> lackField = diffLackField(tableDetails, target.get(tableName));
            if (!lackField.isEmpty()) {
                diffFields.put(tableName, lackField);
            }
        });

        Map<String, Object> map = new HashMap<>();
        map.put("diffFields", diffFields);
        map.put("newTables", diffTables);
        return map;
    }

    /**
     * 对比两张表是否相等
     */
    private static Boolean compareFieldVal(Object source, Object target) {
        return (source == null && target == null) || ((source != null && target != null) && (source.equals(target)));
    }


    /**
     * 对比不同的字段
     *
     * @author: haoqi
     * @date: 2020/8/19 4:17 下午
     */
    public static Map<String, Object> diffLackField(Map<String, Map<ColumnType, Object>> source,
                                                    Map<String, Map<ColumnType, Object>> target) {

        Map<String, Object> diffTable = new HashMap<>();
        source.forEach((fieldName, sourceFieldVals) -> {

            /*
             * 对比缺少的字段
             */
            if (!target.containsKey(fieldName)) {
                diffTable.put(fieldName, sourceFieldVals);
                return;
            }


            Map<ColumnType, Object> targetFieldVals = target.get(fieldName);
            //对每一个字段的各个类型进行比较
            for (ColumnType fieldType : ColumnType.columnTypes()) {
                //比对两个表字段中不同的属性
                Object sourceFieldVal = sourceFieldVals.get(fieldType);
                Object targetFieldVal = targetFieldVals.get(fieldType);

                if (!compareFieldVal(sourceFieldVal, targetFieldVal)) {
                    Map<String, Object> diffFieldVal = new HashMap<>();
                    diffFieldVal.put(SqlUtil.SOURCE, sourceFieldVals);
                    diffFieldVal.put(SqlUtil.TARGET, targetFieldVals);
                    diffTable.put(fieldName, diffFieldVal);
                    break;
                }
            }

        });
        return diffTable;
    }
}
