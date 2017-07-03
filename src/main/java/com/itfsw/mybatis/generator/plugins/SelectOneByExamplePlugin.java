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

package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import com.itfsw.mybatis.generator.plugins.utils.FormatTools;
import com.itfsw.mybatis.generator.plugins.utils.JavaElementGeneratorTools;
import com.itfsw.mybatis.generator.plugins.utils.XmlElementGeneratorTools;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * ---------------------------------------------------------------------------
 * 增加查询一条数据方法
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2016/12/28 14:56
 * ---------------------------------------------------------------------------
 */
public class SelectOneByExamplePlugin extends BasePlugin {
    public static final String METHOD_SELECT_ONE_BY_EXAMPLE = "selectOneByExample";  // 方法名
    public static final String METHOD_SELECT_ONE_BY_EXAMPLE_WITH_BLOBS = "selectOneByExampleWithBLOBs";  // 方法名

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        // 有种情况下ModelBaseRecordClass不会生成不包含BLOBs的构造方法，造成selectOneByExample返回结果找不到对应的构造方法异常
        if (introspectedTable.hasBLOBColumns() && !introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            // 添加没有BLOBs的构造方法
            Method method = new Method();
            method.setVisibility(JavaVisibility.PUBLIC);
            method.setConstructor(true);
            method.setName(topLevelClass.getType().getShortName());
            commentGenerator.addGeneralMethodComment(method, introspectedTable);

            // 使用没有blobs的字段
            List<IntrospectedColumn> constructorColumns = introspectedTable.getNonBLOBColumns();

            for (IntrospectedColumn introspectedColumn : constructorColumns) {
                method.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(), introspectedColumn.getJavaProperty()));
                topLevelClass.addImportedType(introspectedColumn.getFullyQualifiedJavaType());
            }

            StringBuilder sb = new StringBuilder();
            if (introspectedTable.getRules().generatePrimaryKeyClass()) {
                boolean comma = false;
                sb.append("super(");
                for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                    if (comma) {
                        sb.append(", ");
                    } else {
                        comma = true;
                    }
                    sb.append(introspectedColumn.getJavaProperty());
                }
                sb.append(");");
                method.addBodyLine(sb.toString());
            }

            List<IntrospectedColumn> introspectedColumns;
            if (!introspectedTable.getRules().generatePrimaryKeyClass() && introspectedTable.hasPrimaryKeyColumns()) {
                introspectedColumns = introspectedTable.getNonBLOBColumns();
            } else {
                introspectedColumns = introspectedTable.getBaseColumns();
            }

            for (IntrospectedColumn introspectedColumn : introspectedColumns) {
                sb.setLength(0);
                sb.append("this.");
                sb.append(introspectedColumn.getJavaProperty());
                sb.append(" = ");
                sb.append(introspectedColumn.getJavaProperty());
                sb.append(';');
                method.addBodyLine(sb.toString());
            }

            // 找到原有构造方法位置，保持构造方法放在一起美观
            ArrayList<Method> methods = (ArrayList<Method>) topLevelClass.getMethods();
            int index = 0;
            for (Method m : methods) {
                index++;
                if (m.getName().equals(topLevelClass.getType().getShortName())) {
                    break;
                }
            }
            methods.add(index, method);
        }

        return true;
    }

    /**
     * Java Client Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param interfaze
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 方法生成 selectOneByExample
        Method method = JavaElementGeneratorTools.generateMethod(
                METHOD_SELECT_ONE_BY_EXAMPLE,
                JavaVisibility.DEFAULT,
                JavaElementGeneratorTools.getModelTypeWithoutBLOBs(introspectedTable),
                new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example")
        );
        commentGenerator.addGeneralMethodComment(method, introspectedTable);

        // interface 增加方法
        FormatTools.addMethodWithBestPosition(interfaze, method);
        logger.debug("itfsw(查询单条数据插件):" + interfaze.getType().getShortName() + "增加selectOneByExample方法。");

        // 方法生成 selectOneByExampleWithBLOBs !!! 注意这里的行为不以有没有生成Model 的 WithBLOBs类为基准
        if (introspectedTable.hasBLOBColumns()) {
            // 方法生成 selectOneByExample
            Method method1 = JavaElementGeneratorTools.generateMethod(
                    METHOD_SELECT_ONE_BY_EXAMPLE_WITH_BLOBS,
                    JavaVisibility.DEFAULT,
                    JavaElementGeneratorTools.getModelTypeWithBLOBs(introspectedTable),
                    new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example")
            );
            commentGenerator.addGeneralMethodComment(method1, introspectedTable);

            // interface 增加方法
            FormatTools.addMethodWithBestPosition(interfaze, method1);
            logger.debug("itfsw(查询单条数据插件):" + interfaze.getType().getShortName() + "增加selectOneByExampleWithBLOBs方法。");
        }

        return true;
    }

    /**
     * SQL Map Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param document
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        // ------------------------------------ selectOneByExample ----------------------------------
        // 生成查询语句
        XmlElement selectOneElement = new XmlElement("select");
        // 添加注释(!!!必须添加注释，overwrite覆盖生成时，@see XmlFileMergerJaxp.isGeneratedNode会去判断注释中是否存在OLD_ELEMENT_TAGS中的一点，例子：@mbg.generated)
        commentGenerator.addComment(selectOneElement);

        // 添加ID
        selectOneElement.addAttribute(new Attribute("id", METHOD_SELECT_ONE_BY_EXAMPLE));
        // 添加返回类型
        selectOneElement.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
        // 添加参数类型
        selectOneElement.addAttribute(new Attribute("parameterType", introspectedTable.getExampleType()));
        selectOneElement.addElement(new TextElement("select"));

        StringBuilder sb = new StringBuilder();
        if (stringHasValue(introspectedTable.getSelectByExampleQueryId())) {
            sb.append('\'');
            sb.append(introspectedTable.getSelectByExampleQueryId());
            sb.append("' as QUERYID,");
            selectOneElement.addElement(new TextElement(sb.toString()));
        }
        selectOneElement.addElement(XmlElementGeneratorTools.getBaseColumnListElement(introspectedTable));

        sb.setLength(0);
        sb.append("from ");
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        selectOneElement.addElement(new TextElement(sb.toString()));
        selectOneElement.addElement(XmlElementGeneratorTools.getExampleIncludeElement(introspectedTable));

        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", "orderByClause != null"));  //$NON-NLS-2$
        ifElement.addElement(new TextElement("order by ${orderByClause}"));
        selectOneElement.addElement(ifElement);

        // 只查询一条
        selectOneElement.addElement(new TextElement("limit 1"));
        // 添加到根节点
        FormatTools.addElementWithBestPosition(document.getRootElement(), selectOneElement);
        logger.debug("itfsw(查询单条数据插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加selectOneByExample方法。");

        // ------------------------------------ selectOneByExampleWithBLOBs ----------------------------------
        // !!! 注意这里的行为不以有没有生成Model 的 WithBLOBs类为基准
        if (introspectedTable.hasBLOBColumns()) {
            // 生成查询语句
            XmlElement selectOneWithBLOBsElement = new XmlElement("select");
            // 添加注释(!!!必须添加注释，overwrite覆盖生成时，@see XmlFileMergerJaxp.isGeneratedNode会去判断注释中是否存在OLD_ELEMENT_TAGS中的一点，例子：@mbg.generated)
            commentGenerator.addComment(selectOneWithBLOBsElement);

            // 添加ID
            selectOneWithBLOBsElement.addAttribute(new Attribute("id", METHOD_SELECT_ONE_BY_EXAMPLE_WITH_BLOBS));
            // 添加返回类型
            selectOneWithBLOBsElement.addAttribute(new Attribute("resultMap", introspectedTable.getResultMapWithBLOBsId()));
            // 添加参数类型
            selectOneWithBLOBsElement.addAttribute(new Attribute("parameterType", introspectedTable.getExampleType()));
            // 添加查询SQL
            selectOneWithBLOBsElement.addElement(new TextElement("select"));

            sb.setLength(0);
            if (stringHasValue(introspectedTable.getSelectByExampleQueryId())) {
                sb.append('\'');
                sb.append(introspectedTable.getSelectByExampleQueryId());
                sb.append("' as QUERYID,");
                selectOneWithBLOBsElement.addElement(new TextElement(sb.toString()));
            }

            selectOneWithBLOBsElement.addElement(XmlElementGeneratorTools.getBaseColumnListElement(introspectedTable));
            selectOneWithBLOBsElement.addElement(new TextElement(","));
            selectOneWithBLOBsElement.addElement(XmlElementGeneratorTools.getBlobColumnListElement(introspectedTable));

            sb.setLength(0);
            sb.append("from ");
            sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
            selectOneWithBLOBsElement.addElement(new TextElement(sb.toString()));
            selectOneWithBLOBsElement.addElement(XmlElementGeneratorTools.getExampleIncludeElement(introspectedTable));

            XmlElement ifElement1 = new XmlElement("if");
            ifElement1.addAttribute(new Attribute("test", "orderByClause != null"));  //$NON-NLS-2$
            ifElement1.addElement(new TextElement("order by ${orderByClause}"));
            selectOneWithBLOBsElement.addElement(ifElement1);

            // 只查询一条
            selectOneWithBLOBsElement.addElement(new TextElement("limit 1"));

            // 添加到根节点
            FormatTools.addElementWithBestPosition(document.getRootElement(), selectOneWithBLOBsElement);
            logger.debug("itfsw(查询单条数据插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加selectOneByExampleWithBLOBs方法。");
        }


        return true;
    }
}
