/*
 * Copyright (c) 2018.
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
import com.itfsw.mybatis.generator.plugins.utils.IncrementsPluginTools;
import com.itfsw.mybatis.generator.plugins.utils.PluginTools;
import com.itfsw.mybatis.generator.plugins.utils.XmlElementGeneratorTools;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.config.GeneratedKey;

import java.util.List;

/**
 * ---------------------------------------------------------------------------
 * Selective 增强插件
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2018/4/20 15:39
 * ---------------------------------------------------------------------------
 */
public class SelectiveEnhancedPlugin extends BasePlugin {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(List<String> warnings) {

        // 插件使用前提是使用了ModelColumnPlugin插件
        if (!PluginTools.checkDependencyPlugin(getContext(), ModelColumnPlugin.class)) {
            warnings.add("itfsw:插件" + this.getClass().getTypeName() + "插件需配合" + ModelColumnPlugin.class.getTypeName() + "插件使用！");
            return false;
        }

        // 和 OldSelectiveEnhancedPlugin 不能同时使用
        if (PluginTools.checkDependencyPlugin(getContext(), OldSelectiveEnhancedPlugin.class)) {
            warnings.add("itfsw:插件" + this.getClass().getTypeName() + "不能和" + OldSelectiveEnhancedPlugin.class.getTypeName() + "插件同时使用！");
            return false;
        }

        return super.validate(warnings);
    }

    /**
     * insertSelective 方法生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param method
     * @param interfaze
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean clientInsertSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        method.getParameters().clear();

        FullyQualifiedJavaType parameterType = introspectedTable.getRules().calculateAllFieldsClass();
        method.addParameter(new Parameter(parameterType, "record", "@Param(\"record\")"));

        // 找出全字段对应的Model
        FullyQualifiedJavaType fullFieldModel = introspectedTable.getRules().calculateAllFieldsClass();
        // column枚举
        FullyQualifiedJavaType selectiveType = new FullyQualifiedJavaType(fullFieldModel.getShortName() + "." + ModelColumnPlugin.ENUM_NAME);
        method.addParameter(new Parameter(selectiveType, "selective", "@Param(\"selective\")", true));

        method.getJavaDocLines().clear();
        commentGenerator.addGeneralMethodComment(method, introspectedTable);

        return super.clientInsertSelectiveMethodGenerated(method, interfaze, introspectedTable);
    }

    /**
     * updateByExampleSelective
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param method
     * @param interfaze
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean clientUpdateByExampleSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        method.getParameters().clear();

        FullyQualifiedJavaType parameterType = introspectedTable.getRules().calculateAllFieldsClass();
        method.addParameter(new Parameter(parameterType, "record", "@Param(\"record\")"));

        FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        method.addParameter(new Parameter(exampleType, "example", "@Param(\"example\")"));

        // 找出全字段对应的Model
        FullyQualifiedJavaType fullFieldModel = introspectedTable.getRules().calculateAllFieldsClass();
        // column枚举
        FullyQualifiedJavaType selectiveType = new FullyQualifiedJavaType(fullFieldModel.getShortName() + "." + ModelColumnPlugin.ENUM_NAME);
        method.addParameter(new Parameter(selectiveType, "selective", "@Param(\"selective\")", true));

        method.getJavaDocLines().clear();
        commentGenerator.addGeneralMethodComment(method, introspectedTable);
        return super.clientUpdateByExampleSelectiveMethodGenerated(method, interfaze, introspectedTable);
    }

    /**
     * updateByPrimaryKeySelective
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param method
     * @param interfaze
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        method.getParameters().clear();

        FullyQualifiedJavaType parameterType;
        if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            parameterType = new FullyQualifiedJavaType(introspectedTable.getRecordWithBLOBsType());
        } else {
            parameterType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        }

        method.addParameter(new Parameter(parameterType, "record", "@Param(\"record\")"));

        // 找出全字段对应的Model
        FullyQualifiedJavaType fullFieldModel = introspectedTable.getRules().calculateAllFieldsClass();
        // column枚举
        FullyQualifiedJavaType selectiveType = new FullyQualifiedJavaType(fullFieldModel.getShortName() + "." + ModelColumnPlugin.ENUM_NAME);
        method.addParameter(new Parameter(selectiveType, "selective", "@Param(\"selective\")", true));

        method.getJavaDocLines().clear();
        commentGenerator.addGeneralMethodComment(method, introspectedTable);

        return super.clientUpdateByPrimaryKeySelectiveMethodGenerated(method, interfaze, introspectedTable);
    }

    /**
     * insertSelective
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param element
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        // 清空
        XmlElement answer = element;
        answer.getElements().clear();
        answer.getAttributes().clear();

        answer.addAttribute(new Attribute("id", introspectedTable.getInsertSelectiveStatementId()));

        answer.addAttribute(new Attribute("parameterType", "map"));

        commentGenerator.addComment(answer);

        GeneratedKey gk = introspectedTable.getGeneratedKey();
        if (gk != null) {
            IntrospectedColumn introspectedColumn = introspectedTable.getColumn(gk.getColumn());
            // if the column is null, then it's a configuration error. The
            // warning has already been reported
            if (introspectedColumn != null) {
                if (gk.isJdbcStandard()) {
                    answer.addAttribute(new Attribute("useGeneratedKeys", "true"));
                    answer.addAttribute(new Attribute("keyProperty", introspectedColumn.getJavaProperty()));
                    answer.addAttribute(new Attribute("keyColumn", introspectedColumn.getActualColumnName()));
                } else {
                    answer.addElement(XmlElementGeneratorTools.getSelectKey(introspectedColumn, gk));
                }
            }
        }

        StringBuilder sb = new StringBuilder();

        sb.append("insert into ");
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        // selective
        XmlElement insertChooseEle = new XmlElement("choose");
        answer.addElement(insertChooseEle);

        XmlElement insertWhenEle = new XmlElement("when");
        insertWhenEle.addAttribute(new Attribute("test", "selective != null and selective.length > 0"));
        insertChooseEle.addElement(insertWhenEle);

        XmlElement insertForeachEle = new XmlElement("foreach");
        insertForeachEle.addAttribute(new Attribute("collection", "selective"));
        insertForeachEle.addAttribute(new Attribute("item", "column"));
        insertForeachEle.addAttribute(new Attribute("open", "("));
        insertForeachEle.addAttribute(new Attribute("separator", ","));
        insertForeachEle.addAttribute(new Attribute("close", ")"));
        insertForeachEle.addElement(new TextElement("${column.value}"));
        insertWhenEle.addElement(insertForeachEle);

        XmlElement insertOtherwiseEle = new XmlElement("otherwise");
        insertOtherwiseEle.addElement(XmlElementGeneratorTools.generateKeysSelective(
                ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns()),
                "record."
        ));
        insertChooseEle.addElement(insertOtherwiseEle);

        XmlElement insertTrimElement = new XmlElement("trim");
        insertTrimElement.addAttribute(new Attribute("prefix", "("));
        insertTrimElement.addAttribute(new Attribute("suffix", ")"));
        insertTrimElement.addAttribute(new Attribute("suffixOverrides", ","));
        insertOtherwiseEle.addElement(insertTrimElement);


        XmlElement valuesChooseEle = new XmlElement("choose");
        answer.addElement(valuesChooseEle);

        XmlElement valuesWhenEle = new XmlElement("when");
        valuesWhenEle.addAttribute(new Attribute("test", "selective != null and selective.length > 0"));
        valuesChooseEle.addElement(valuesWhenEle);

        XmlElement valuesForeachEle = new XmlElement("foreach");
        valuesForeachEle.addAttribute(new Attribute("collection", "selective"));
        valuesForeachEle.addAttribute(new Attribute("item", "column"));
        valuesForeachEle.addAttribute(new Attribute("open", "values ("));
        valuesForeachEle.addAttribute(new Attribute("separator", ","));
        valuesForeachEle.addAttribute(new Attribute("close", ")"));
        valuesForeachEle.addElement(new TextElement("#{record.${column.javaProperty},jdbcType=${column.jdbcType}}"));
        valuesWhenEle.addElement(valuesForeachEle);

        XmlElement valuesOtherwiseEle = new XmlElement("otherwise");
        valuesOtherwiseEle.addElement(XmlElementGeneratorTools.generateValuesSelective(
                ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns()),
                "record."
        ));
        valuesChooseEle.addElement(valuesOtherwiseEle);

        XmlElement valuesTrimElement = new XmlElement("trim");
        valuesTrimElement.addAttribute(new Attribute("prefix", "values ("));
        valuesTrimElement.addAttribute(new Attribute("suffix", ")"));
        valuesTrimElement.addAttribute(new Attribute("suffixOverrides", ","));
        valuesOtherwiseEle.addElement(valuesTrimElement);

        return super.sqlMapInsertSelectiveElementGenerated(element, introspectedTable);
    }

    /**
     * updateByExampleSelective
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param element
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        IncrementsPluginTools incTools = IncrementsPluginTools.getTools(context, introspectedTable, warnings);
        // 清空
        XmlElement answer = element;
        answer.getElements().clear();
        answer.getAttributes().clear();

        answer.addAttribute(new Attribute("id", introspectedTable.getUpdateByExampleSelectiveStatementId()));
        answer.addAttribute(new Attribute("parameterType", "map"));

        commentGenerator.addComment(answer);

        StringBuilder sb = new StringBuilder();
        sb.append("update ");
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        // selective
        answer.addElement(new TextElement("SET"));
        XmlElement setChooseEle = new XmlElement("choose");
        answer.addElement(setChooseEle);

        XmlElement setWhenEle = new XmlElement("when");
        setWhenEle.addAttribute(new Attribute("test", "selective != null and selective.length > 0"));
        setChooseEle.addElement(setWhenEle);

        XmlElement setForeachEle = new XmlElement("foreach");
        setForeachEle.addAttribute(new Attribute("collection", "selective"));
        setForeachEle.addAttribute(new Attribute("item", "column"));
        setForeachEle.addAttribute(new Attribute("separator", ","));
        // 自增插件支持
        if (incTools.support()) {
            incTools.generateSetsSelectiveWithSelectiveEnhancedPlugin(setForeachEle);
        } else {
            setForeachEle.addElement(new TextElement("${column.value} = #{record.${column.javaProperty},jdbcType=${column.jdbcType}}"));
        }
        setWhenEle.addElement(setForeachEle);

        XmlElement setOtherwiseEle = new XmlElement("otherwise");

        // 自增插件支持
        if (incTools.support()) {
            setOtherwiseEle.addElement(incTools.generateSetsSelective(
                    ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getAllColumns()),
                    "record.",
                    false

            ));
        } else {
            setOtherwiseEle.addElement(XmlElementGeneratorTools.generateSetsSelective(
                    ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getAllColumns()),
                    "record."
            ));
        }

        setChooseEle.addElement(setOtherwiseEle);

        answer.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));

        return super.sqlMapUpdateByExampleSelectiveElementGenerated(element, introspectedTable);
    }

    /**
     * updateByPrimaryKeySelective
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param element
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        IncrementsPluginTools incTools = IncrementsPluginTools.getTools(context, introspectedTable, warnings);
        // 清空
        XmlElement answer = element;
        answer.getElements().clear();
        answer.getAttributes().clear();

        answer.addAttribute(new Attribute("id", introspectedTable.getUpdateByPrimaryKeySelectiveStatementId()));
        answer.addAttribute(new Attribute("parameterType", "map"));

        commentGenerator.addComment(answer);

        StringBuilder sb = new StringBuilder();

        sb.append("update ");
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        // selective
        answer.addElement(new TextElement("SET"));
        XmlElement setChooseEle = new XmlElement("choose");
        answer.addElement(setChooseEle);

        XmlElement setWhenEle = new XmlElement("when");
        setWhenEle.addAttribute(new Attribute("test", "selective != null and selective.length > 0"));
        setChooseEle.addElement(setWhenEle);

        XmlElement setForeachEle = new XmlElement("foreach");
        setForeachEle.addAttribute(new Attribute("collection", "selective"));
        setForeachEle.addAttribute(new Attribute("item", "column"));
        setForeachEle.addAttribute(new Attribute("separator", ","));
        // 自增插件支持
        if (incTools.support()) {
            incTools.generateSetsSelectiveWithSelectiveEnhancedPlugin(setForeachEle);
        } else {
            setForeachEle.addElement(new TextElement("${column.value} = #{record.${column.javaProperty},jdbcType=${column.jdbcType}}"));
        }
        setWhenEle.addElement(setForeachEle);

        XmlElement setOtherwiseEle = new XmlElement("otherwise");
        setChooseEle.addElement(setOtherwiseEle);
        // 自增插件支持
        if (incTools.support()) {
            setOtherwiseEle.addElement(incTools.generateSetsSelective(
                    ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getNonPrimaryKeyColumns()),
                    "record.",
                    false

            ));
        } else {
            setOtherwiseEle.addElement(XmlElementGeneratorTools.generateSetsSelective(
                    ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getNonPrimaryKeyColumns()),
                    "record."
            ));
        }

        XmlElementGeneratorTools.generateWhereByPrimaryKeyTo(answer, introspectedTable.getPrimaryKeyColumns(), "record.");

        return super.sqlMapUpdateByPrimaryKeySelectiveElementGenerated(element, introspectedTable);
    }
}
