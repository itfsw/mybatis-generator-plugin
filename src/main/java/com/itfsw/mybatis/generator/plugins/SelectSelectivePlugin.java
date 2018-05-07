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
import com.itfsw.mybatis.generator.plugins.utils.hook.ISelectOneByExamplePluginHook;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.ResultMapWithBLOBsElementGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.ResultMapWithoutBLOBsElementGenerator;

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
public class SelectSelectivePlugin extends BasePlugin implements ISelectOneByExamplePluginHook {
    public static final String METHOD_SELECT_BY_EXAMPLE_SELECTIVE = "selectByExampleSelective";
    public static final String METHOD_SELECT_BY_PRIMARY_KEY_SELECTIVE = "selectByPrimaryKeySelective";
    public static final String METHOD_SELECT_ONE_BY_EXAMPLE_SELECTIVE = "selectOneByExampleSelective";
    public static final String ID_FOR_PROPERTY_BASED_RESULT_MAP = "BasePropertyResultMap";

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

    // =========================================== client 方法生成 ===================================================

    @Override
    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        FormatTools.addMethodWithBestPosition(interfaze, this.replaceMethodWithSelective(
                method,
                METHOD_SELECT_BY_EXAMPLE_SELECTIVE,
                "@Param(\"example\")",
                introspectedTable
        ));
        return super.clientSelectByExampleWithBLOBsMethodGenerated(method, interfaze, introspectedTable);
    }

    @Override
    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (!introspectedTable.hasBLOBColumns()) {
            FormatTools.addMethodWithBestPosition(interfaze, this.replaceMethodWithSelective(
                    method,
                    METHOD_SELECT_BY_EXAMPLE_SELECTIVE,
                    "@Param(\"example\")",
                    introspectedTable
            ));
        }
        return super.clientSelectByExampleWithoutBLOBsMethodGenerated(method, interfaze, introspectedTable);
    }


    @Override
    public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            FormatTools.addMethodWithBestPosition(interfaze, this.replaceMethodWithSelective(
                    method,
                    METHOD_SELECT_BY_PRIMARY_KEY_SELECTIVE,
                    "@Param(\"record\")",
                    introspectedTable
            ));
        } else {
            Method withSelective = JavaElementTools.clone(method);
            FormatTools.replaceGeneralMethodComment(commentGenerator, withSelective, introspectedTable);

            withSelective.setName(METHOD_SELECT_BY_PRIMARY_KEY_SELECTIVE);

            withSelective.getParameters().clear();
            for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                withSelective.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(), introspectedColumn.getJavaProperty(), "@Param(\"" + introspectedColumn.getJavaProperty() + "\")"));
            }
            // selective
            withSelective.addParameter(
                    new Parameter(this.getModelColumnFullyQualifiedJavaType(introspectedTable), "selective", "@Param(\"selective\")", true)
            );

            FormatTools.addMethodWithBestPosition(interfaze, withSelective);
        }
        return super.clientSelectByPrimaryKeyMethodGenerated(method, interfaze, introspectedTable);
    }

    @Override
    public boolean clientSelectOneByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        FormatTools.addMethodWithBestPosition(interfaze, this.replaceMethodWithSelective(
                method,
                METHOD_SELECT_ONE_BY_EXAMPLE_SELECTIVE,
                "@Param(\"example\")",
                introspectedTable
        ));
        return true;
    }

    @Override
    public boolean clientSelectOneByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (!introspectedTable.hasBLOBColumns()) {
            FormatTools.addMethodWithBestPosition(interfaze, this.replaceMethodWithSelective(
                    method,
                    METHOD_SELECT_ONE_BY_EXAMPLE_SELECTIVE,
                    "@Param(\"example\")",
                    introspectedTable
            ));
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
        // issues#16
        if (introspectedTable.isConstructorBased()) {
            XmlElement resultMapEle = new XmlElement("resultMap");
            resultMapEle.addAttribute(new Attribute("id", ID_FOR_PROPERTY_BASED_RESULT_MAP));
            String returnType;
            if (introspectedTable.getRules().generateBaseRecordClass()) {
                returnType = introspectedTable.getBaseRecordType();
            } else {
                returnType = introspectedTable.getPrimaryKeyType();
            }

            resultMapEle.addAttribute(new Attribute("type", returnType));
            commentGenerator.addComment(resultMapEle);

            if (introspectedTable.getRules().generateResultMapWithBLOBs()) {
                addResultMapElementsWithBLOBs(resultMapEle, introspectedTable);
            } else if (introspectedTable.getRules().generateBaseResultMap()) {
                addResultMapElementsWithoutBLOBs(resultMapEle, introspectedTable);
            }
            document.getRootElement().getElements().add(0, resultMapEle);
        }


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
        // issues#16
        if (introspectedTable.isConstructorBased()) {
            selectByExampleSelectiveEle.addAttribute(new Attribute("resultMap", ID_FOR_PROPERTY_BASED_RESULT_MAP));
        } else {
            selectByExampleSelectiveEle.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
        }
        selectByExampleSelectiveEle.addAttribute(new Attribute("parameterType", "map"));

        selectByExampleSelectiveEle.addElement(new TextElement("select"));
        if (stringHasValue(introspectedTable.getSelectByExampleQueryId())) {
            selectByExampleSelectiveEle.addElement(new TextElement("'" + introspectedTable.getSelectByExampleQueryId() + "' as QUERYID,"));
        }

        // issues#20
        XmlElement ifDistinctElement = new XmlElement("if");
        ifDistinctElement.addAttribute(new Attribute("test", "example.distinct"));
        ifDistinctElement.addElement(new TextElement("distinct"));
        selectByExampleSelectiveEle.addElement(ifDistinctElement);

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
        // issues#16
        if (introspectedTable.isConstructorBased()) {
            selectByPrimaryKeySelectiveEle.addAttribute(new Attribute("resultMap", ID_FOR_PROPERTY_BASED_RESULT_MAP));
        } else {
            selectByPrimaryKeySelectiveEle.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
        }
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
            // issues#16
            if (introspectedTable.isConstructorBased()) {
                selectOneByExampleSelectiveEle.addAttribute(new Attribute("resultMap", ID_FOR_PROPERTY_BASED_RESULT_MAP));
            } else {
                selectOneByExampleSelectiveEle.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
            }
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

    // =========================================== 一些私有方法 =====================================================

    /**
     * 替换方法成withSelective
     * @param method
     * @param name
     * @param firstAnnotation
     * @param introspectedTable
     * @return
     */
    private Method replaceMethodWithSelective(Method method, String name, String firstAnnotation, IntrospectedTable introspectedTable) {
        Method withSelective = JavaElementTools.clone(method);
        FormatTools.replaceGeneralMethodComment(commentGenerator, withSelective, introspectedTable);

        withSelective.setName(name);
        // example
        withSelective.getParameters().get(0).addAnnotation(firstAnnotation);
        // selective
        withSelective.addParameter(
                new Parameter(this.getModelColumnFullyQualifiedJavaType(introspectedTable), "selective", "@Param(\"selective\")", true)
        );

        return withSelective;
    }

    /**
     * 获取ModelColumn type
     * @param introspectedTable
     * @return
     */
    private FullyQualifiedJavaType getModelColumnFullyQualifiedJavaType(IntrospectedTable introspectedTable) {
        return new FullyQualifiedJavaType(introspectedTable.getRules().calculateAllFieldsClass().getShortName() + "." + ModelColumnPlugin.ENUM_NAME);
    }

    /**
     * @param answer
     * @param introspectedTable
     * @see ResultMapWithoutBLOBsElementGenerator#addResultMapElements(XmlElement)
     */
    private void addResultMapElementsWithoutBLOBs(XmlElement answer, IntrospectedTable introspectedTable) {
        for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
            answer.addElement(XmlElementGeneratorTools.generateResultMapResultElement("id", introspectedColumn));
        }

        List<IntrospectedColumn> columns = introspectedTable.getBaseColumns();
        for (IntrospectedColumn introspectedColumn : columns) {
            answer.addElement(XmlElementGeneratorTools.generateResultMapResultElement("result", introspectedColumn));
        }
    }

    /**
     * @param answer
     * @param introspectedTable
     * @see ResultMapWithBLOBsElementGenerator#addResultMapElements(XmlElement)
     */
    private void addResultMapElementsWithBLOBs(XmlElement answer, IntrospectedTable introspectedTable) {
        for (IntrospectedColumn introspectedColumn : introspectedTable.getBLOBColumns()) {
            answer.addElement(XmlElementGeneratorTools.generateResultMapResultElement("result", introspectedColumn));
        }
    }
}
