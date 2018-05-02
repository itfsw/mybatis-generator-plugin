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

import com.itfsw.mybatis.generator.plugins.IncrementsPlugin;
import com.itfsw.mybatis.generator.plugins.ModelBuilderPlugin;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * ---------------------------------------------------------------------------
 * 增量插件工具
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/6/21 16:12
 * ---------------------------------------------------------------------------
 */
public class IncrementsPluginTools {
    private IntrospectedTable introspectedTable;    // 表
    private List<IntrospectedColumn> columns = new ArrayList<>();   // 表启用增量操作的字段

    /**
     * 构造函数
     * @param introspectedTable
     */
    private IncrementsPluginTools(IntrospectedTable introspectedTable) {
        this.introspectedTable = introspectedTable;
    }

    /**
     * 获取工具
     * @param context
     * @param introspectedTable
     * @param warnings
     * @return
     */
    public static IncrementsPluginTools getTools(Context context, IntrospectedTable introspectedTable, List<String> warnings) {
        IncrementsPluginTools tools = new IncrementsPluginTools(introspectedTable);
        // 判断是否启用了插件
        if (PluginTools.getPluginConfiguration(context, IncrementsPlugin.class) != null) {
            String incrementsColumns = introspectedTable.getTableConfigurationProperty(IncrementsPlugin.PRO_INCREMENTS_COLUMNS);
            if (StringUtility.stringHasValue(incrementsColumns)) {
                // 切分
                String[] incrementsColumnsStrs = incrementsColumns.split(",");
                for (String incrementsColumnsStr : incrementsColumnsStrs) {
                    IntrospectedColumn column = IntrospectedTableTools.safeGetColumn(introspectedTable, incrementsColumnsStr);
                    if (column == null) {
                        warnings.add("itfsw:插件" + IncrementsPlugin.class.getTypeName() + "插件没有找到column为" + incrementsColumnsStr.trim() + "的字段！");
                    } else {
                        tools.columns.add(column);
                    }
                }
            }
        }
        return tools;
    }

    /**
     * 获取INC Enum
     * @return
     */
    public FullyQualifiedJavaType getIncEnum() {
        return new FullyQualifiedJavaType(this.introspectedTable.getFullyQualifiedTable().getDomainObjectName() + "." + ModelBuilderPlugin.BUILDER_CLASS_NAME + ".Inc");
    }

    /**
     * 是否启用了
     * @return
     */
    public boolean support() {
        return this.columns.size() > 0;
    }

    /**
     * Getter method for property <tt>columns</tt>.
     * @return property value of columns
     * @author hewei
     */
    public List<IntrospectedColumn> getColumns() {
        return columns;
    }

    /**
     * 判断是否为需要进行增量操作的column
     * @param searchColumn
     * @return
     */
    public boolean supportColumn(IntrospectedColumn searchColumn) {
        for (IntrospectedColumn column : this.columns) {
            if (column.getActualColumnName().equals(searchColumn.getActualColumnName())) {
                return true;
            }
        }
        return false;
    }
}
