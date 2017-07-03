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

import com.itfsw.mybatis.generator.plugins.utils.*;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/6/29 13:34
 * ---------------------------------------------------------------------------
 */
public class SelectSelectivePlugin extends BasePlugin {
    public static final String METHOD_SELECT_BY_EXAMPLE_SELECTIVE = "selectByExampleSelective";
    public static final String METHOD_SELECT_BY_PRIMARY_KEY_SELECTIVE = "selectByPrimaryKeySelective";
    public static final String METHOD_SELECT_ONE_BY_EXAMPLE_SELECTIVE = "selectOneByExampleSelective";

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param warnings
     * @return
     */
    @Override
    public boolean validate(List<String> warnings) {

        // 插件使用前提是使用了ModelColumnPlugin插件
        if (!PluginTools.checkDependencyPlugin(getContext(), ModelColumnPlugin.class)) {
            warnings.add("itfsw:插件" + this.getClass().getTypeName() + "插件需配合com.itfsw.mybatis.generator.plugins.ModelColumnPlugin插件使用！");
            return false;
        }

        return super.validate(warnings);
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param interfaze
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 找出全字段对应的Model
        FullyQualifiedJavaType fullFieldModel = introspectedTable.getRules().calculateAllFieldsClass();
        // 返回list类型
        FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
        listType.addTypeArgument(fullFieldModel);
        // column枚举
        FullyQualifiedJavaType selectiveType = new FullyQualifiedJavaType(fullFieldModel.getShortName() + "." + ModelColumnPlugin.ENUM_NAME);

        // 1. selectByExampleSelective 方法
        Method mSelectByExampleSelective = JavaElementGeneratorTools.generateMethod(
                METHOD_SELECT_BY_EXAMPLE_SELECTIVE,
                JavaVisibility.DEFAULT,
                listType,
                new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example", "@Param(\"example\")"),
                new Parameter(selectiveType, "selective", "@Param(\"selective\")", true)
        );
        commentGenerator.addGeneralMethodComment(mSelectByExampleSelective, introspectedTable);
        FormatTools.addMethodWithBestPosition(interfaze, mSelectByExampleSelective);

        // 2. selectByPrimaryKeySelective
        Method mSelectByPrimaryKeySelective = JavaElementGeneratorTools.generateMethod(
                METHOD_SELECT_BY_PRIMARY_KEY_SELECTIVE,
                JavaVisibility.DEFAULT,
                fullFieldModel
        );
        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
            mSelectByPrimaryKeySelective.addParameter(new Parameter(type, "key", "@Param(\"record\")"));
        } else {
            // no primary key class - fields are in the base class
            // if more than one PK field, then we need to annotate the
            // parameters
            // for MyBatis3
            List<IntrospectedColumn> introspectedColumns = introspectedTable.getPrimaryKeyColumns();

            for (IntrospectedColumn introspectedColumn : introspectedColumns) {
                FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
                Parameter parameter = new Parameter(type, introspectedColumn.getJavaProperty());
                parameter.addAnnotation("@Param(\"" + introspectedColumn.getJavaProperty() + "\")");
                mSelectByPrimaryKeySelective.addParameter(parameter);
            }
        }
        mSelectByPrimaryKeySelective.addParameter(new Parameter(selectiveType, "selective", "@Param(\"selective\")", true));
        commentGenerator.addGeneralMethodComment(mSelectByPrimaryKeySelective, introspectedTable);
        FormatTools.addMethodWithBestPosition(interfaze, mSelectByPrimaryKeySelective);

        // 3. selectOneByExampleSelective
        if (PluginTools.getPluginConfiguration(context, SelectOneByExamplePlugin.class) != null) {
            Method mSelectOneByExampleSelective = JavaElementGeneratorTools.generateMethod(
                    METHOD_SELECT_ONE_BY_EXAMPLE_SELECTIVE,
                    JavaVisibility.DEFAULT,
                    fullFieldModel,
                    new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example", "@Param(\"example\")"),
                    new Parameter(selectiveType, "selective", "@Param(\"selective\")", true)
            );
            commentGenerator.addGeneralMethodComment(mSelectOneByExampleSelective, introspectedTable);
            FormatTools.addMethodWithBestPosition(interfaze, mSelectOneByExampleSelective);
        }

        return true;
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param document
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        // 生成返回字段节点
        XmlElement columnsEle = new XmlElement("foreach");
        columnsEle.addAttribute(new Attribute("collection", "selective"));
        columnsEle.addAttribute(new Attribute("item", "column"));
        columnsEle.addAttribute(new Attribute("separator", ","));
        columnsEle.addElement(new TextElement("${column.value}"));

        // 1. selectByExampleSelective 方法
        XmlElement selectByExampleSelectiveEle = new XmlElement("select");
        commentGenerator.addComment(selectByExampleSelectiveEle);

        selectByExampleSelectiveEle.addAttribute(new Attribute("id", METHOD_SELECT_BY_EXAMPLE_SELECTIVE));
        selectByExampleSelectiveEle.addAttribute(new Attribute("resultType", introspectedTable.getRules().calculateAllFieldsClass().getFullyQualifiedName()));
        selectByExampleSelectiveEle.addAttribute(new Attribute("parameterType", "map"));

        selectByExampleSelectiveEle.addElement(new TextElement("select"));
        if (stringHasValue(introspectedTable.getSelectByExampleQueryId())) {
            selectByExampleSelectiveEle.addElement(new TextElement("'" + introspectedTable.getSelectByExampleQueryId() + "' as QUERYID,"));
        }
        selectByExampleSelectiveEle.addElement(columnsEle);
        selectByExampleSelectiveEle.addElement(new TextElement("from " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));

        selectByExampleSelectiveEle.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));

        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", "example.orderByClause != null"));
        ifElement.addElement(new TextElement("order by ${example.orderByClause}"));
        selectByExampleSelectiveEle.addElement(ifElement);

        FormatTools.addElementWithBestPosition(document.getRootElement(), selectByExampleSelectiveEle);

        // 2. selectByPrimaryKeySelective
        XmlElement selectByPrimaryKeySelectiveEle = new XmlElement("select"); 
        commentGenerator.addComment(selectByPrimaryKeySelectiveEle);

        selectByPrimaryKeySelectiveEle.addAttribute(new Attribute("id", METHOD_SELECT_BY_PRIMARY_KEY_SELECTIVE));
        selectByPrimaryKeySelectiveEle.addAttribute(new Attribute("resultType", introspectedTable.getRules().calculateAllFieldsClass().getFullyQualifiedName()));
        selectByPrimaryKeySelectiveEle.addAttribute(new Attribute("parameterType", "map"));

        selectByPrimaryKeySelectiveEle.addElement(new TextElement("select"));
        if (stringHasValue(introspectedTable.getSelectByExampleQueryId())) {
            selectByPrimaryKeySelectiveEle.addElement(new TextElement("'" + introspectedTable.getSelectByExampleQueryId() + "' as QUERYID,"));
        }
        selectByPrimaryKeySelectiveEle.addElement(columnsEle);
        selectByPrimaryKeySelectiveEle.addElement(new TextElement("from " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));

        boolean and = false;
        StringBuffer sb = new StringBuffer();
        for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
            sb.setLength(0);
            if (and) {
                sb.append("  and "); 
            } else {
                sb.append("where "); 
                and = true;
            }

            sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(introspectedColumn));
            sb.append(" = "); 
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, introspectedTable.getRules().generatePrimaryKeyClass() ? "record." : null));
            selectByPrimaryKeySelectiveEle.addElement(new TextElement(sb.toString()));
        }

        FormatTools.addElementWithBestPosition(document.getRootElement(), selectByPrimaryKeySelectiveEle);

        // 3. selectOneByExampleSelective
        if (PluginTools.getPluginConfiguration(context, SelectOneByExamplePlugin.class) != null) {
            XmlElement selectOneByExampleSelectiveEle = new XmlElement("select");
            commentGenerator.addComment(selectOneByExampleSelectiveEle);

            selectOneByExampleSelectiveEle.addAttribute(new Attribute("id", METHOD_SELECT_ONE_BY_EXAMPLE_SELECTIVE));
            selectOneByExampleSelectiveEle.addAttribute(new Attribute("resultType", introspectedTable.getRules().calculateAllFieldsClass().getFullyQualifiedName()));
            selectOneByExampleSelectiveEle.addAttribute(new Attribute("parameterType", "map"));

            selectOneByExampleSelectiveEle.addElement(new TextElement("select"));
            if (stringHasValue(introspectedTable.getSelectByExampleQueryId())) {
                selectOneByExampleSelectiveEle.addElement(new TextElement("'" + introspectedTable.getSelectByExampleQueryId() + "' as QUERYID,"));
            }
            selectOneByExampleSelectiveEle.addElement(columnsEle);
            selectOneByExampleSelectiveEle.addElement(new TextElement("from " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));

            selectOneByExampleSelectiveEle.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));

            XmlElement ifElement1 = new XmlElement("if");
            ifElement1.addAttribute(new Attribute("test", "example.orderByClause != null"));
            ifElement1.addElement(new TextElement("order by ${example.orderByClause}"));
            selectOneByExampleSelectiveEle.addElement(ifElement1);

            // 只查询一条
            selectOneByExampleSelectiveEle.addElement(new TextElement("limit 1"));

            // 添加到根节点
            FormatTools.addElementWithBestPosition(document.getRootElement(), selectOneByExampleSelectiveEle);
        }

        return true;
    }
}
