/*
 * Copyright (c) 2017.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.itfsw.mybatis.generator.plugins.utils;

import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.util.StringUtility;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * IntrospectedTable 的一些拓展增强
 */
public class IntrospectedTableTools {

    /**
     * 设置DomainObjectName和MapperName
     */
    public static void setDomainObjectName(IntrospectedTable introspectedTable, Context context, String domainObjectName) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        // 配置信息（没啥用）
        introspectedTable.getTableConfiguration().setDomainObjectName(domainObjectName);

        // FullyQualifiedTable修正
        Field domainObjectNameField = FullyQualifiedTable.class.getDeclaredField("domainObjectName");
        domainObjectNameField.setAccessible(true);
        domainObjectNameField.set(introspectedTable.getFullyQualifiedTable(), domainObjectName);

        // 重新修正introspectedTable属性信息
        Method calculateJavaClientAttributes = IntrospectedTable.class.getDeclaredMethod("calculateJavaClientAttributes");
        calculateJavaClientAttributes.setAccessible(true);
        calculateJavaClientAttributes.invoke(introspectedTable);

        Method calculateModelAttributes = IntrospectedTable.class.getDeclaredMethod("calculateModelAttributes");
        calculateModelAttributes.setAccessible(true);
        calculateModelAttributes.invoke(introspectedTable);

        Method calculateXmlAttributes = IntrospectedTable.class.getDeclaredMethod("calculateXmlAttributes");
        calculateXmlAttributes.setAccessible(true);
        calculateXmlAttributes.invoke(introspectedTable);
    }

    /**
     * 安全获取column 通过正则获取的name可能包含beginningDelimiter&&endingDelimiter
     */
    public static IntrospectedColumn safeGetColumn(IntrospectedTable introspectedTable, String columnName) {
        // columnName
        columnName = columnName.trim();
        // 过滤
        String beginningDelimiter = introspectedTable.getContext().getBeginningDelimiter();
        if (StringUtility.stringHasValue(beginningDelimiter)) {
            columnName = columnName.replaceFirst("^" + beginningDelimiter, "");
        }
        String endingDelimiter = introspectedTable.getContext().getEndingDelimiter();
        if (StringUtility.stringHasValue(endingDelimiter)) {
            columnName = columnName.replaceFirst(endingDelimiter + "$", "");
        }

        if (introspectedTable.getColumn(columnName).isPresent()) {
            return introspectedTable.getColumn(columnName).get();
        }
        return null;
    }

    /**
     * 获取生成model baseRecord的列
     */
    public static List<IntrospectedColumn> getModelBaseRecordColumns(IntrospectedTable introspectedTable) {
        List<IntrospectedColumn> introspectedColumns;
        if (includePrimaryKeyColumns(introspectedTable)) {
            if (includeBLOBColumns(introspectedTable)) {
                introspectedColumns = introspectedTable.getAllColumns();
            } else {
                introspectedColumns = introspectedTable.getNonBLOBColumns();
            }
        } else {
            if (includeBLOBColumns(introspectedTable)) {
                introspectedColumns = introspectedTable
                        .getNonPrimaryKeyColumns();
            } else {
                introspectedColumns = introspectedTable.getBaseColumns();
            }
        }

        return introspectedColumns;
    }

    /**
     * 是否有primaryKey 列
     */
    public static boolean includePrimaryKeyColumns(IntrospectedTable introspectedTable) {
        return !introspectedTable.getRules().generatePrimaryKeyClass()
                && introspectedTable.hasPrimaryKeyColumns();
    }

    /**
     * 是否有 blob 列
     */
    public static boolean includeBLOBColumns(IntrospectedTable introspectedTable) {
        return !introspectedTable.getRules().generateRecordWithBLOBsClass()
                && introspectedTable.hasBLOBColumns();
    }
}
