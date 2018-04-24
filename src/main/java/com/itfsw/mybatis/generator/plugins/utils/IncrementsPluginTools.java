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
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
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
    private final static Logger logger = LoggerFactory.getLogger(IncrementsPluginTools.class);
    private Context context;    // 上下文
    private IntrospectedTable introspectedTable;    // 表
    private List<IntrospectedColumn> columns = new ArrayList<>();   // 表启用增量操作的字段

    /**
     * 构造函数
     * @param context
     * @param introspectedTable
     */
    private IncrementsPluginTools(Context context, IntrospectedTable introspectedTable) {
        this.context = context;
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
        IncrementsPluginTools tools = new IncrementsPluginTools(context, introspectedTable);
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

    /**
     * 生成sets Selective Ele
     * @param columns
     * @param prefix
     * @param bracket
     * @return
     */
    public XmlElement generateSetsSelective(List<IntrospectedColumn> columns, String prefix, boolean bracket) {
        XmlElement eleTrim = new XmlElement("trim");
        if (bracket) {
            eleTrim.addAttribute(new Attribute("prefix", "("));
            eleTrim.addAttribute(new Attribute("suffix", ")"));
            eleTrim.addAttribute(new Attribute("suffixOverrides", ","));
        } else {
            eleTrim.addAttribute(new Attribute("suffixOverrides", ","));
        }

        for (IntrospectedColumn introspectedColumn : columns) {
            XmlElement eleIf = new XmlElement("if");
            eleIf.addAttribute(new Attribute("test", introspectedColumn.getJavaProperty(prefix) + " != null"));
            if (this.supportColumn(introspectedColumn)) {
                for (Element ele : this.generatedIncrementsElement(introspectedColumn, prefix, true)) {
                    eleIf.addElement(ele);
                }
            } else {
                eleIf.addElement(new TextElement(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn) + " = " + MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, prefix) + ","));
            }
            eleTrim.addElement(eleIf);
        }

        return eleTrim;
    }

    /**
     * 生成sets Ele
     * @param columns
     * @param prefix
     * @param bracket
     * @return
     */
    public List<Element> generateSets(List<IntrospectedColumn> columns, String prefix, boolean bracket) {
        List<Element> list = new ArrayList<>();
        if (bracket) {
            list.add(new TextElement("("));
        }
        Iterator<IntrospectedColumn> columnIterator = columns.iterator();
        while (columnIterator.hasNext()) {
            IntrospectedColumn introspectedColumn = columnIterator.next();

            if (this.supportColumn(introspectedColumn)) {
                list.add(new TextElement(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn) + " = "));
                for (Element ele : this.generatedIncrementsElement(introspectedColumn, prefix, columnIterator.hasNext())) {
                    list.add(ele);
                }
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
                sb.append(" = ");
                sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, prefix));
                if (columnIterator.hasNext()) {
                    sb.append(", ");
                }
                list.add(new TextElement(sb.toString()));
            }
        }
        if (bracket) {
            list.add(new TextElement(")"));
        }

        return list;
    }

    /**
     * 生成增量操作节点
     * @param introspectedColumn
     * @param prefix
     * @param hasComma
     */
    public List<Element> generatedIncrementsElement(IntrospectedColumn introspectedColumn, String prefix, boolean hasComma) {
        List<Element> list = new ArrayList<>();

        // 1. column = 节点
        list.add(new TextElement(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn) + " = "));

        // 2. 选择节点
        // 条件
        XmlElement choose = new XmlElement("choose");

        // 没有启用增量操作
        XmlElement when = new XmlElement("when");
        when.addAttribute(new Attribute(
                "test",
                (prefix != null ? prefix : "_parameter.") + IncrementsPlugin.METHOD_INC_CHECK
                        + "('" + MyBatis3FormattingUtilities.escapeStringForMyBatis3(introspectedColumn.getActualColumnName()) + "')"
        ));
        TextElement spec = new TextElement(
                MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn)
                        + " ${" + (prefix != null ? prefix : "")
                        + IncrementsPlugin.FIELD_INC_MAP + "." + MyBatis3FormattingUtilities.escapeStringForMyBatis3(introspectedColumn.getActualColumnName()) + ".value} "
                        + MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, prefix));
        when.addElement(spec);
        choose.addElement(when);

        // 启用了增量操作
        XmlElement otherwise = new XmlElement("otherwise");
        TextElement normal = new TextElement(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, prefix));
        otherwise.addElement(normal);
        choose.addElement(otherwise);

        list.add(choose);

        // 3. 结尾逗号
        if (hasComma) {
            list.add(new TextElement(","));
        }

        return list;
    }

    /**
     * 创建 sets (SelectiveEnhancedPlugin)
     * @param setForeachEle
     */
    public void generateSetsSelectiveWithSelectiveEnhancedPlugin(XmlElement setForeachEle) {
        XmlElement choose = new XmlElement("choose");

        for (IntrospectedColumn introspectedColumn : columns) {
            XmlElement when = new XmlElement("when");

            // 需要 inc 的列
            StringBuilder sb = new StringBuilder();
            sb.append("'");
            sb.append(introspectedColumn.getActualColumnName());
            sb.append("'.toString()");
            sb.append(" == ");
            sb.append("column.value");

            when.addAttribute(new Attribute("test", sb.toString()));
            when.addElement(new TextElement("${column.value} = ${column.value} ${record.incrementsColumnsInfoMap."
                    + introspectedColumn.getActualColumnName()
                    + ".value} #{record.${column.javaProperty},jdbcType=${column.jdbcType}}"));
            choose.addElement(when);
        }

        XmlElement otherwise = new XmlElement("otherwise");
        otherwise.addElement(new TextElement("${column.value} = #{record.${column.javaProperty},jdbcType=${column.jdbcType}}"));
        choose.addElement(otherwise);

        setForeachEle.addElement(choose);
    }
}
