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

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.GeneratedKey;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ---------------------------------------------------------------------------
 * Xml 节点生成工具 参考 org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2016/12/29 16:47
 * ---------------------------------------------------------------------------
 */
public class XmlElementGeneratorTools {

    /**
     * This method should return an XmlElement for the select key used to
     * automatically generate keys.
     * @param introspectedColumn the column related to the select key statement
     * @param generatedKey       the generated key for the current table
     * @return the selectKey element
     */
    public static Element getSelectKey(IntrospectedColumn introspectedColumn, GeneratedKey generatedKey) {
        String identityColumnType = introspectedColumn
                .getFullyQualifiedJavaType().getFullyQualifiedName();

        XmlElement answer = new XmlElement("selectKey");
        answer.addAttribute(new Attribute("resultType", identityColumnType));
        answer.addAttribute(new Attribute(
                "keyProperty", introspectedColumn.getJavaProperty()));
        answer.addAttribute(new Attribute("order",
                generatedKey.getMyBatis3Order()));

        answer.addElement(new TextElement(generatedKey
                .getRuntimeSqlStatement()));

        return answer;
    }

    public static Element getBaseColumnListElement(IntrospectedTable introspectedTable) {
        XmlElement answer = new XmlElement("include");
        answer.addAttribute(new Attribute("refid",
                introspectedTable.getBaseColumnListId()));
        return answer;
    }

    public static Element getBlobColumnListElement(IntrospectedTable introspectedTable) {
        XmlElement answer = new XmlElement("include");
        answer.addAttribute(new Attribute("refid",
                introspectedTable.getBlobColumnListId()));
        return answer;
    }

    public static Element getExampleIncludeElement(IntrospectedTable introspectedTable) {
        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", "_parameter != null"));

        XmlElement includeElement = new XmlElement("include");
        includeElement.addAttribute(new Attribute("refid",
                introspectedTable.getExampleWhereClauseId()));
        ifElement.addElement(includeElement);

        return ifElement;
    }

    public static Element getUpdateByExampleIncludeElement(IntrospectedTable introspectedTable) {
        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", "_parameter != null"));

        XmlElement includeElement = new XmlElement("include");
        includeElement.addAttribute(new Attribute("refid",
                introspectedTable.getMyBatis3UpdateByExampleWhereClauseId()));
        ifElement.addElement(includeElement);

        return ifElement;
    }

    /**
     * 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
     * @param element
     * @param introspectedTable
     */
    public static void useGeneratedKeys(XmlElement element, IntrospectedTable introspectedTable) {
        useGeneratedKeys(element, introspectedTable, null);
    }

    /**
     * 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
     * @param element
     * @param introspectedTable
     * @param prefix
     */
    public static void useGeneratedKeys(XmlElement element, IntrospectedTable introspectedTable, String prefix) {
        GeneratedKey gk = introspectedTable.getGeneratedKey();
        if (gk != null) {
            IntrospectedColumn introspectedColumn = IntrospectedTableTools.safeGetColumn(introspectedTable, gk.getColumn());
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
     * @param columns
     * @return
     */
    public static List<TextElement> generateKeys(List<IntrospectedColumn> columns) {
        return generateKeys(columns, true);
    }

    /**
     * 生成keys Ele
     * @param columns
     * @param bracket
     * @return
     */
    public static List<TextElement> generateKeys(List<IntrospectedColumn> columns, boolean bracket) {
        return generateCommColumns(columns, null, bracket, 1);
    }

    /**
     * 生成keys Selective Ele
     * @param columns
     * @return
     */
    public static Element generateKeysSelective(List<IntrospectedColumn> columns) {
        return generateKeysSelective(columns, null);
    }

    /**
     * 生成keys Selective Ele
     * @param columns
     * @param prefix
     * @return
     */
    public static Element generateKeysSelective(List<IntrospectedColumn> columns, String prefix) {
        return generateKeysSelective(columns, prefix, true);
    }

    /**
     * 生成keys Selective Ele
     * @param columns
     * @param prefix
     * @param bracket
     * @return
     */
    public static XmlElement generateKeysSelective(List<IntrospectedColumn> columns, String prefix, boolean bracket) {
        return generateCommColumnsSelective(columns, prefix, bracket, 1);
    }

    /**
     * 生成values Ele
     * @param columns
     * @return
     */
    public static List<TextElement> generateValues(List<IntrospectedColumn> columns) {
        return generateValues(columns, null);
    }

    /**
     * 生成values Ele
     * @param columns
     * @param prefix
     * @return
     */
    public static List<TextElement> generateValues(List<IntrospectedColumn> columns, String prefix) {
        return generateValues(columns, prefix, true);
    }

    /**
     * 生成values Ele
     * @param columns
     * @param prefix
     * @param bracket
     * @return
     */
    public static List<TextElement> generateValues(List<IntrospectedColumn> columns, String prefix, boolean bracket) {
        return generateCommColumns(columns, prefix, bracket, 2);
    }

    /**
     * 生成values Selective Ele
     * @param columns
     * @return
     */
    public static Element generateValuesSelective(List<IntrospectedColumn> columns) {
        return generateValuesSelective(columns, null);
    }

    /**
     * 生成values Selective Ele
     * @param columns
     * @param prefix
     * @return
     */
    public static Element generateValuesSelective(List<IntrospectedColumn> columns, String prefix) {
        return generateValuesSelective(columns, prefix, true);
    }

    /**
     * 生成values Selective Ele
     * @param columns
     * @param prefix
     * @param bracket
     * @return
     */
    public static XmlElement generateValuesSelective(List<IntrospectedColumn> columns, String prefix, boolean bracket) {
        return generateCommColumnsSelective(columns, prefix, bracket, 2);
    }

    /**
     * 生成sets Ele
     * @param columns
     * @return
     */
    public static List<TextElement> generateSets(List<IntrospectedColumn> columns) {
        return generateSets(columns, null, false);
    }

    /**
     * 生成sets Ele
     * @param columns
     * @param prefix
     * @return
     */
    public static List<TextElement> generateSets(List<IntrospectedColumn> columns, String prefix) {
        return generateSets(columns, prefix, false);
    }

    /**
     * 生成sets Ele
     * @param columns
     * @param prefix
     * @param bracket
     * @return
     */
    public static List<TextElement> generateSets(List<IntrospectedColumn> columns, String prefix, boolean bracket) {
        return generateCommColumns(columns, prefix, bracket, 3);
    }

    /**
     * 生成sets Selective Ele
     * @param columns
     * @return
     */
    public static XmlElement generateSetsSelective(List<IntrospectedColumn> columns) {
        return generateSetsSelective(columns, null, false);
    }

    /**
     * 生成sets Selective Ele
     * @param columns
     * @param prefix
     * @return
     */
    public static XmlElement generateSetsSelective(List<IntrospectedColumn> columns, String prefix) {
        return generateSetsSelective(columns, prefix, false);
    }

    /**
     * 生成sets Selective Ele
     * @param columns
     * @param prefix
     * @param bracket
     * @return
     */
    public static XmlElement generateSetsSelective(List<IntrospectedColumn> columns, String prefix, boolean bracket) {
        return generateCommColumnsSelective(columns, prefix, bracket, 3);
    }

    /**
     * 通用遍历columns
     * @param columns
     * @param prefix
     * @param bracket
     * @param type    1:key,2:value,3:set
     * @return
     */
    private static List<TextElement> generateCommColumns(List<IntrospectedColumn> columns, String prefix, boolean bracket, int type) {
        List<TextElement> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder(bracket ? "(" : "");
        Iterator<IntrospectedColumn> columnIterator = columns.iterator();
        while (columnIterator.hasNext()) {
            IntrospectedColumn introspectedColumn = columnIterator.next();

            switch (type) {
                case 3:
                    sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
                    sb.append(" = ");
                    sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, prefix));
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
        if (sb.length() > 0 || bracket){
            list.add(new TextElement(sb.append(bracket ? ")" : "").toString()));
        }

        return list;
    }

    /**
     * 通用遍历columns
     * @param columns
     * @param prefix
     * @param bracket
     * @param type    1:key,2:value,3:set
     * @return
     */
    private static XmlElement generateCommColumnsSelective(List<IntrospectedColumn> columns, String prefix, boolean bracket, int type) {
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

            switch (type) {
                case 3:
                    eleIf.addElement(new TextElement(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn) + " = " + MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, prefix) + ","));
                    break;
                case 2:
                    eleIf.addElement(new TextElement(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, prefix) + ","));
                    break;
                case 1:
                    eleIf.addElement(new TextElement(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn) + ","));
                    break;
            }

            eleTrim.addElement(eleIf);
        }

        return eleTrim;
    }

    /**
     * 查找指定xml节点下指定节点名称的元素
     * @param xmlElement
     * @param name
     * @return
     */
    public static List<XmlElement> findXmlElements(XmlElement xmlElement, String name) {
        List<XmlElement> list = new ArrayList<>();
        List<Element> elements = xmlElement.getElements();
        for (Element ele : elements) {
            if (ele instanceof XmlElement) {
                XmlElement xmlElement1 = (XmlElement) ele;
                if (name.equalsIgnoreCase(xmlElement1.getName())) {
                    list.add(xmlElement1);
                }
            }
        }
        return list;
    }

}
