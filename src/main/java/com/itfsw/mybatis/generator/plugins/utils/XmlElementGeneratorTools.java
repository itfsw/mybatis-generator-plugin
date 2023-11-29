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

import com.itfsw.mybatis.generator.plugins.utils.hook.IIncrementPluginHook;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.VisitableElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.GeneratedKey;

import java.util.*;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * Xml 节点生成工具 参考 org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator
 */
public class XmlElementGeneratorTools {

    /**
     * This method should return an XmlElement for the select key used to
     * automatically generate keys.
     *
     * @param introspectedColumn the column related to the select key statement
     * @param generatedKey       the generated key for the current table
     * @return the selectKey element
     */
    public static VisitableElement getSelectKey(IntrospectedColumn introspectedColumn, GeneratedKey generatedKey) {
        return getSelectKey(introspectedColumn, generatedKey, null);
    }

    public static VisitableElement getSelectKey(IntrospectedColumn introspectedColumn, GeneratedKey generatedKey, String prefix) {
        String identityColumnType = introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName();

        XmlElement answer = new XmlElement("selectKey");
        answer.addAttribute(new Attribute("resultType", identityColumnType));
        answer.addAttribute(new Attribute("keyProperty", (prefix == null ? "" : prefix) + introspectedColumn.getJavaProperty()));
        answer.addAttribute(new Attribute("order", generatedKey.getMyBatis3Order()));

        answer.addElement(new TextElement(generatedKey.getRuntimeSqlStatement()));

        return answer;
    }

    public static VisitableElement getBaseColumnListElement(IntrospectedTable introspectedTable) {
        XmlElement answer = new XmlElement("include");
        answer.addAttribute(new Attribute("refid", introspectedTable.getBaseColumnListId()));
        return answer;
    }

    public static VisitableElement getBlobColumnListElement(IntrospectedTable introspectedTable) {
        XmlElement answer = new XmlElement("include");
        answer.addAttribute(new Attribute("refid", introspectedTable.getBlobColumnListId()));
        return answer;
    }

    public static VisitableElement getExampleIncludeElement(IntrospectedTable introspectedTable) {
        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", "_parameter != null"));

        XmlElement includeElement = new XmlElement("include");
        includeElement.addAttribute(new Attribute("refid", introspectedTable.getExampleWhereClauseId()));
        ifElement.addElement(includeElement);

        return ifElement;
    }

    public static VisitableElement getUpdateByExampleIncludeElement(IntrospectedTable introspectedTable) {
        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", "_parameter != null"));

        XmlElement includeElement = new XmlElement("include");
        includeElement.addAttribute(new Attribute("refid", introspectedTable.getMyBatis3UpdateByExampleWhereClauseId()));
        ifElement.addElement(includeElement);

        return ifElement;
    }

    /**
     * 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
     */
    public static void useGeneratedKeys(XmlElement element, IntrospectedTable introspectedTable) {
        useGeneratedKeys(element, introspectedTable, null);
    }

    /**
     * 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
     */
    public static void useGeneratedKeys(XmlElement element, IntrospectedTable introspectedTable, String prefix) {
        Optional<GeneratedKey> generatedKeyOptional = introspectedTable.getGeneratedKey();
        if (generatedKeyOptional.isPresent()) {
            IntrospectedColumn introspectedColumn = IntrospectedTableTools.safeGetColumn(introspectedTable, generatedKeyOptional.get().getColumn());
            // if the column is null, then it's a configuration error. The
            // warning has already been reported
            if (introspectedColumn != null) {
                // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
                element.addAttribute(new Attribute("useGeneratedKeys", "true"));
                element.addAttribute(new Attribute("keyProperty", (prefix == null ? "" : prefix) + introspectedColumn.getJavaProperty()));
                element.addAttribute(new Attribute("keyColumn", introspectedColumn.getActualColumnName()));
            }
        }
    }

    /**
     * 生成keys Ele
     */
    public static List<VisitableElement> generateKeys(IntrospectedTable introspectedTable, List<IntrospectedColumn> columns) {
        return generateKeys(introspectedTable, columns, false);
    }

    /**
     * 生成keys Ele
     */
    public static List<VisitableElement> generateKeys(IntrospectedTable introspectedTable, List<IntrospectedColumn> columns, boolean bracket) {
        return generateCommColumns(introspectedTable, columns, null, bracket, 1);
    }

    /**
     * 生成keys Selective Ele
     */
    public static XmlElement generateKeysSelective(IntrospectedTable introspectedTable, List<IntrospectedColumn> columns) {
        return generateKeysSelective(introspectedTable, columns, null);
    }

    /**
     * 生成keys Selective Ele
     */
    public static XmlElement generateKeysSelective(IntrospectedTable introspectedTable, List<IntrospectedColumn> columns, String prefix) {
        return generateKeysSelective(introspectedTable, columns, prefix, true);
    }

    /**
     * 生成keys Selective Ele
     */
    public static XmlElement generateKeysSelective(IntrospectedTable introspectedTable, List<IntrospectedColumn> columns, String prefix, boolean bracket) {
        return generateCommColumnsSelective(introspectedTable, columns, prefix, bracket, 1);
    }

    /**
     * 生成values Ele
     */
    public static List<VisitableElement> generateValues(IntrospectedTable introspectedTable, List<IntrospectedColumn> columns) {
        return generateValues(introspectedTable, columns, null);
    }

    /**
     * 生成values Ele
     */
    public static List<VisitableElement> generateValues(IntrospectedTable introspectedTable, List<IntrospectedColumn> columns, String prefix) {
        return generateValues(introspectedTable, columns, prefix, true);
    }

    /**
     * 生成values Ele
     */
    public static List<VisitableElement> generateValues(IntrospectedTable introspectedTable, List<IntrospectedColumn> columns, String prefix, boolean bracket) {
        return generateCommColumns(introspectedTable, columns, prefix, bracket, 2);
    }

    /**
     * 生成values Selective Ele
     */
    public static XmlElement generateValuesSelective(IntrospectedTable introspectedTable, List<IntrospectedColumn> columns) {
        return generateValuesSelective(introspectedTable, columns, null);
    }

    /**
     * 生成values Selective Ele
     */
    public static XmlElement generateValuesSelective(IntrospectedTable introspectedTable, List<IntrospectedColumn> columns, String prefix) {
        return generateValuesSelective(introspectedTable, columns, prefix, true);
    }

    /**
     * 生成values Selective Ele
     */
    public static XmlElement generateValuesSelective(IntrospectedTable introspectedTable, List<IntrospectedColumn> columns, String prefix, boolean bracket) {
        return generateCommColumnsSelective(introspectedTable, columns, prefix, bracket, 2);
    }

    /**
     * 生成sets Ele
     */
    public static List<VisitableElement> generateSets(IntrospectedTable introspectedTable, List<IntrospectedColumn> columns) {
        return generateSets(introspectedTable, columns, null);
    }

    /**
     * 生成sets Ele
     */
    public static List<VisitableElement> generateSets(IntrospectedTable introspectedTable, List<IntrospectedColumn> columns, String prefix) {
        return generateCommColumns(introspectedTable, columns, prefix, false, 3);
    }

    /**
     * 生成sets Selective Ele
     */
    public static XmlElement generateSetsSelective(IntrospectedTable introspectedTable, List<IntrospectedColumn> columns) {
        return generateSetsSelective(introspectedTable, columns, null);
    }

    /**
     * 生成sets Selective Ele
     */
    public static XmlElement generateSetsSelective(IntrospectedTable introspectedTable, List<IntrospectedColumn> columns, String prefix) {
        return generateCommColumnsSelective(introspectedTable, columns, prefix, false, 3);
    }

    /**
     * 生成keys Ele (upsert)
     */
    public static List<VisitableElement> generateUpsertKeys(IntrospectedTable introspectedTable, List<IntrospectedColumn> columns, String prefix) {
        return generateCommColumns(introspectedTable, columns, prefix, true, 1, true);
    }

    /**
     * 生成values Ele (upsert)
     */
    public static List<VisitableElement> generateUpsertValues(IntrospectedTable introspectedTable, List<IntrospectedColumn> columns, String prefix, boolean bracket) {
        return generateCommColumns(introspectedTable, columns, prefix, bracket, 2, true);
    }

    /**
     * 生成sets Ele (upsert)
     */
    public static List<VisitableElement> generateUpsertSets(IntrospectedTable introspectedTable, List<IntrospectedColumn> columns, String prefix) {
        return generateCommColumns(introspectedTable, columns, prefix, false, 3, true);
    }

    /**
     * 通用遍历columns
     *
     * @param type 1:key,2:value,3:set
     */
    private static List<VisitableElement> generateCommColumns(IntrospectedTable introspectedTable, List<IntrospectedColumn> columns, String prefix, boolean bracket, int type) {
        return generateCommColumns(introspectedTable, columns, prefix, bracket, type, false);
    }

    /**
     * 通用遍历columns
     *
     * @param type 1:key,2:value,3:set
     */
    private static List<VisitableElement> generateCommColumns(IntrospectedTable introspectedTable, List<IntrospectedColumn> columns, String prefix, boolean bracket, int type, boolean upsert) {
        List<VisitableElement> list = new ArrayList<>();

        // 只有upsert插件才会传入 IdentityAndGeneratedAlwaysColumn
        if (upsert && hasIdentityAndGeneratedAlwaysColumns(columns)) {
            XmlElement trimEle = generateTrim(bracket);

            for (IntrospectedColumn introspectedColumn : columns) {
                if (introspectedColumn.isGeneratedAlways() || introspectedColumn.isIdentity()) {
                    generateSelectiveToTrimEleTo(introspectedTable, trimEle, introspectedColumn, prefix, type);
                } else if (type == 3 && PluginTools.getHook(IIncrementPluginHook.class).generateIncrementSetSelective(introspectedTable, introspectedColumn, prefix) != null) {
                    // IncrementPlugin 插件支持
                    trimEle.addElement(PluginTools.getHook(IIncrementPluginHook.class).generateIncrementSetSelective(introspectedTable, introspectedColumn, prefix));
                } else {
                    generateSelectiveCommColumnTo(trimEle, introspectedColumn, prefix, type);
                }
            }

            return Collections.singletonList(trimEle);
        } else {
            StringBuilder sb = new StringBuilder(bracket ? "(" : "");
            Iterator<IntrospectedColumn> columnIterator = columns.iterator();
            while (columnIterator.hasNext()) {
                IntrospectedColumn introspectedColumn = columnIterator.next();

                switch (type) {
                    case 3:
                        XmlElement incrementEle = PluginTools.getHook(IIncrementPluginHook.class).generateIncrementSet(introspectedTable, introspectedColumn, prefix, false);

                        if (incrementEle != null) {
                            // 增量插件支持
                            if (sb.length() > 0) {
                                list.add(new TextElement(sb.toString()));
                                sb.setLength(0);
                            }
                            list.add(incrementEle);
                        } else {
                            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
                            sb.append(" = ");
                            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, prefix));
                        }

                        break;
                    case 2:
                        sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, prefix));
                        break;
                    case 1:
                        sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
                        break;
                }

                if (columnIterator.hasNext()) {
                    sb.append(", ");
                }

                // 保持和官方一致 80 进行换行
                if (type == 1 || type == 2) {
                    if (sb.length() > 80) {
                        list.add(new TextElement(sb.toString()));
                        sb.setLength(0);
                        OutputUtilities.xmlIndent(sb, 1);
                    }
                } else {
                    list.add(new TextElement(sb.toString()));
                    sb.setLength(0);
                }
            }
            if (sb.length() > 0 || bracket) {
                list.add(new TextElement(sb.append(bracket ? ")" : "").toString()));
            }

            return list;
        }
    }

    /**
     * 通用遍历columns
     *
     * @param type 1:key,2:value,3:set
     */
    private static XmlElement generateCommColumnsSelective(IntrospectedTable introspectedTable, List<IntrospectedColumn> columns, String prefix, boolean bracket, int type) {
        XmlElement trimEle = generateTrim(bracket);
        for (IntrospectedColumn introspectedColumn : columns) {
            generateSelectiveToTrimEleTo(introspectedTable, trimEle, introspectedColumn, prefix, type);
        }
        return trimEle;
    }

    /**
     * trim 节点
     */
    private static XmlElement generateTrim(boolean bracket) {
        XmlElement trimEle = new XmlElement("trim");
        if (bracket) {
            trimEle.addAttribute(new Attribute("prefix", "("));
            trimEle.addAttribute(new Attribute("suffix", ")"));
            trimEle.addAttribute(new Attribute("suffixOverrides", ","));
        } else {
            trimEle.addAttribute(new Attribute("suffixOverrides", ","));
        }
        return trimEle;
    }

    /**
     * 生成选择列到trim 节点
     *
     * @param type 1:key,2:value,3:set
     */
    private static void generateSelectiveToTrimEleTo(IntrospectedTable introspectedTable, XmlElement trimEle, IntrospectedColumn introspectedColumn, String prefix, int type) {
        if (type != 3 && (introspectedColumn.isSequenceColumn() || introspectedColumn.getFullyQualifiedJavaType().isPrimitive())) {
            // if it is a sequence column, it is not optional
            // This is required for MyBatis3 because MyBatis3 parses
            // and calculates the SQL before executing the selectKey

            // if it is primitive, we cannot do a null check
            generateSelectiveCommColumnTo(trimEle, introspectedColumn, prefix, type);
        } else if (type == 3 && PluginTools.getHook(IIncrementPluginHook.class).generateIncrementSetSelective(introspectedTable, introspectedColumn, prefix) != null) {
            // IncrementPlugin 插件支持
            trimEle.addElement(PluginTools.getHook(IIncrementPluginHook.class).generateIncrementSetSelective(introspectedTable, introspectedColumn, prefix));
        } else {
            XmlElement eleIf = new XmlElement("if");
            eleIf.addAttribute(new Attribute("test", introspectedColumn.getJavaProperty(prefix) + " != null"));

            generateSelectiveCommColumnTo(eleIf, introspectedColumn, prefix, type);

            trimEle.addElement(eleIf);
        }
    }

    /**
     * 生成
     *
     * @param type 1:key,2:value,3:set
     */
    private static void generateSelectiveCommColumnTo(XmlElement element, IntrospectedColumn introspectedColumn, String prefix, int type) {
        switch (type) {
            case 3:
                element.addElement(new TextElement(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn) + " = " + MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, prefix) + ","));
                break;
            case 2:
                element.addElement(new TextElement(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, prefix) + ","));
                break;
            case 1:
                element.addElement(new TextElement(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn) + ","));
                break;
        }
    }

    /**
     * 生成 xxxByPrimaryKey 的where 语句
     */
    public static void generateWhereByPrimaryKeyTo(XmlElement element, List<IntrospectedColumn> primaryKeyColumns) {
        generateWhereByPrimaryKeyTo(element, primaryKeyColumns, null);
    }

    /**
     * 生成 xxxByPrimaryKey 的where 语句
     */
    public static void generateWhereByPrimaryKeyTo(XmlElement element, List<IntrospectedColumn> primaryKeyColumns, String prefix) {
        StringBuilder sb = new StringBuilder();
        boolean and = false;
        for (IntrospectedColumn introspectedColumn : primaryKeyColumns) {
            sb.setLength(0);
            if (and) {
                sb.append("  and ");
            } else {
                sb.append("where ");
                and = true;
            }

            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(" = ");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, prefix));
            element.addElement(new TextElement(sb.toString()));
        }
    }

    /**
     * 是否存在自增或者生成的column
     */
    private static boolean hasIdentityAndGeneratedAlwaysColumns(List<IntrospectedColumn> columns) {
        for (IntrospectedColumn ic : columns) {
            if (ic.isGeneratedAlways() || ic.isIdentity()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成resultMap的result 节点
     */
    public static XmlElement generateResultMapResultElement(String name, IntrospectedColumn introspectedColumn) {
        XmlElement resultElement = new XmlElement(name);

        resultElement.addAttribute(new Attribute("column", MyBatis3FormattingUtilities.getRenamedColumnNameForResultMap(introspectedColumn)));
        resultElement.addAttribute(new Attribute("property", introspectedColumn.getJavaProperty()));
        resultElement.addAttribute(new Attribute("jdbcType", introspectedColumn.getJdbcTypeName()));

        if (stringHasValue(introspectedColumn.getTypeHandler())) {
            resultElement.addAttribute(new Attribute("typeHandler", introspectedColumn.getTypeHandler()));
        }
        return resultElement;
    }

    /**
     * 生成逻辑删除列的删除值
     */
    public static String generateLogicalDeleteColumnValue(IntrospectedColumn logicalDeleteColumn, String value) {
        StringBuilder sb = new StringBuilder();
        // 判断字段类型
        if (value == null || "NULL".equalsIgnoreCase(value)) {
            sb.append("NULL");
        } else if (logicalDeleteColumn.isStringColumn()) {
            sb.append("'");
            sb.append(value);
            sb.append("'");
        } else if (logicalDeleteColumn.getFullyQualifiedJavaType().getFullyQualifiedName().equals(Long.class.getName())) {
            sb.append(value.replaceAll("[Ll]", ""));
        } else if (logicalDeleteColumn.getFullyQualifiedJavaType().getFullyQualifiedName().equals(Float.class.getName())) {
            sb.append(value.replaceAll("[Ff]", ""));
        } else {
            sb.append(value);
        }
        return sb.toString();
    }

    /**
     * Gets the parameter clause.
     *
     * @param introspectedColumn the introspected column
     * @return the parameter clause
     */
    public static String getParameterClause(String valueStr, IntrospectedColumn introspectedColumn) {
        StringBuilder sb = new StringBuilder();

        sb.append("#{");
        sb.append(valueStr);
        sb.append(",jdbcType=");
        sb.append(introspectedColumn.getJdbcTypeName());

        if (stringHasValue(introspectedColumn.getTypeHandler())) {
            sb.append(",typeHandler=");
            sb.append(introspectedColumn.getTypeHandler());
        }

        sb.append('}');

        return sb.toString();
    }
}
