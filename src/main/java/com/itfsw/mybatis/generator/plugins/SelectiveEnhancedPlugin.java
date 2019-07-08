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

import com.itfsw.mybatis.generator.plugins.utils.*;
import com.itfsw.mybatis.generator.plugins.utils.hook.IIncrementPluginHook;
import com.itfsw.mybatis.generator.plugins.utils.hook.IIncrementsPluginHook;
import com.itfsw.mybatis.generator.plugins.utils.hook.IOptimisticLockerPluginHook;
import com.itfsw.mybatis.generator.plugins.utils.hook.IUpsertPluginHook;
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
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * ---------------------------------------------------------------------------
 * Selective 增强插件
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2018/4/20 15:39
 * ---------------------------------------------------------------------------
 */
public class SelectiveEnhancedPlugin extends BasePlugin implements IUpsertPluginHook, IOptimisticLockerPluginHook {

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

        FormatTools.replaceGeneralMethodComment(commentGenerator, method, introspectedTable);

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

        FormatTools.replaceGeneralMethodComment(commentGenerator, method, introspectedTable);
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

        FormatTools.replaceGeneralMethodComment(commentGenerator, method, introspectedTable);

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
        XmlElement answer = new XmlElement("insert");
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
                    XmlElementGeneratorTools.useGeneratedKeys(answer, introspectedTable, "record.");
                } else {
                    answer.addElement(XmlElementGeneratorTools.getSelectKey(introspectedColumn, gk, "record."));
                }
            }
        }

        StringBuilder sb = new StringBuilder();

        sb.append("insert into ");
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        // columns
        answer.addElement(this.generateInsertColumnSelective(ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns())));
        // values
        answer.addElement(new TextElement("values"));
        answer.addElement(this.generateInsertValuesSelective(ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns())));

        XmlElementTools.replaceXmlElement(element, answer);

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
        // 清空
        XmlElement answer = new XmlElement("update");
        answer.addAttribute(new Attribute("id", introspectedTable.getUpdateByExampleSelectiveStatementId()));
        answer.addAttribute(new Attribute("parameterType", "map"));

        commentGenerator.addComment(answer);

        StringBuilder sb = new StringBuilder();
        sb.append("update ");
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        // selective
        answer.addElement(new TextElement("SET"));
        answer.addElement(this.generateSetsSelective(ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getAllColumns())));

        answer.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));

        XmlElementTools.replaceXmlElement(element, answer);

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
        // 清空
        XmlElement answer = new XmlElement("update");
        answer.addAttribute(new Attribute("id", introspectedTable.getUpdateByPrimaryKeySelectiveStatementId()));
        answer.addAttribute(new Attribute("parameterType", "map"));

        commentGenerator.addComment(answer);

        StringBuilder sb = new StringBuilder();

        sb.append("update ");
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        // selective
        answer.addElement(new TextElement("SET"));
        answer.addElement(this.generateSetsSelective(ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getNonPrimaryKeyColumns())));

        XmlElementGeneratorTools.generateWhereByPrimaryKeyTo(answer, introspectedTable.getPrimaryKeyColumns(), "record.");

        XmlElementTools.replaceXmlElement(element, answer);
        return super.sqlMapUpdateByPrimaryKeySelectiveElementGenerated(element, introspectedTable);
    }


    // =============================================== IUpsertPluginHook ===================================================

    /**
     * upsertSelective 方法
     * @param method
     * @param interfaze
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean clientUpsertSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        // @Param("record")
        method.getParameters().get(0).addAnnotation("@Param(\"record\")");
        // column枚举
        // 找出全字段对应的Model
        FullyQualifiedJavaType fullFieldModel = introspectedTable.getRules().calculateAllFieldsClass();
        FullyQualifiedJavaType selectiveType = new FullyQualifiedJavaType(fullFieldModel.getShortName() + "." + ModelColumnPlugin.ENUM_NAME);
        method.addParameter(new Parameter(selectiveType, "selective", "@Param(\"selective\")", true));
        return true;
    }

    /**
     * upsertByExampleSelective 方法
     * @param method
     * @param interfaze
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean clientUpsertByExampleSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        // column枚举
        // 找出全字段对应的Model
        FullyQualifiedJavaType fullFieldModel = introspectedTable.getRules().calculateAllFieldsClass();
        FullyQualifiedJavaType selectiveType = new FullyQualifiedJavaType(fullFieldModel.getShortName() + "." + ModelColumnPlugin.ENUM_NAME);
        method.addParameter(new Parameter(selectiveType, "selective", "@Param(\"selective\")", true));
        return true;
    }

    /**
     * upsertSelective xml
     * @param element
     * @param columns
     * @param insertColumnsEle
     * @param insertValuesEle
     * @param setsEle
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapUpsertSelectiveElementGenerated(XmlElement element, List<IntrospectedColumn> columns, XmlElement insertColumnsEle, XmlElement insertValuesEle, XmlElement setsEle, IntrospectedTable introspectedTable) {
        // parameterType
        XmlElementTools.replaceAttribute(element, new Attribute("parameterType", "map"));
        // mybatis 3.5.0 之后对keyProperty处理有变更
        element.getAttributes().removeIf(attribute -> {
            String name = attribute.getName();
            return name.equals("useGeneratedKeys") || name.equals("keyProperty") || name.equals("keyColumn");
        });
        XmlElementGeneratorTools.useGeneratedKeys(element, introspectedTable, "record.");


        // 替换insert column
        XmlElementTools.replaceXmlElement(insertColumnsEle, this.generateInsertColumnSelective(columns));

        // 替换insert values
        XmlElementTools.replaceXmlElement(insertValuesEle, this.generateInsertValuesSelective(columns));

        // 替换update set
        XmlElementTools.replaceXmlElement(setsEle, this.generateSetsSelective(columns));

        return true;
    }

    /**
     * upsertByExampleSelective xml
     * @param element
     * @param columns
     * @param insertColumnsEle
     * @param insertValuesEle
     * @param setsEle
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapUpsertByExampleSelectiveElementGenerated(XmlElement element, List<IntrospectedColumn> columns, XmlElement insertColumnsEle, XmlElement insertValuesEle, XmlElement setsEle, IntrospectedTable introspectedTable) {

        // 替换insert column
        XmlElementTools.replaceXmlElement(insertColumnsEle, this.generateInsertColumnSelective(columns));

        // 替换insert values
        XmlElementTools.replaceXmlElement(insertValuesEle, this.generateInsertValuesSelective(columns, false));

        // 替换update set
        XmlElementTools.replaceXmlElement(setsEle, this.generateSetsSelective(columns));

        return true;
    }

    // ================================================= IOptimisticLockerPluginHook ===============================================

    @Override
    public boolean clientUpdateWithVersionByExampleSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        // issue#69 OptimisticLockerPlugin 插件updateWithVersionByExampleSelective方法的生成是基于updateByExampleSelective的，
        // 这个方法在配置了SelectiveEnhancedPlugin时可能已经被先配置的SelectiveEnhancedPlugin改变了
        if (!"selective".equals(method.getParameters().get(method.getParameters().size() - 1).getName())) {
            // column枚举,找出全字段对应的Model
            FullyQualifiedJavaType fullFieldModel = introspectedTable.getRules().calculateAllFieldsClass();
            FullyQualifiedJavaType selectiveType = new FullyQualifiedJavaType(fullFieldModel.getShortName() + "." + ModelColumnPlugin.ENUM_NAME);
            method.addParameter(new Parameter(selectiveType, "selective", "@Param(\"selective\")", true));
        }

        return true;
    }

    @Override
    public boolean clientUpdateWithVersionByPrimaryKeySelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        // issue#69 OptimisticLockerPlugin 插件updateWithVersionByExampleSelective方法的生成是基于updateByExampleSelective的，
        // 这个方法在配置了SelectiveEnhancedPlugin时可能已经被先配置的SelectiveEnhancedPlugin改变了
        if (!"selective".equals(method.getParameters().get(method.getParameters().size() - 1).getName())) {
            // column枚举,找出全字段对应的Model
            FullyQualifiedJavaType fullFieldModel = introspectedTable.getRules().calculateAllFieldsClass();
            FullyQualifiedJavaType selectiveType = new FullyQualifiedJavaType(fullFieldModel.getShortName() + "." + ModelColumnPlugin.ENUM_NAME);
            method.addParameter(new Parameter(selectiveType, "selective", "@Param(\"selective\")", true));
        }
        return true;
    }

    @Override
    public boolean generateSetsSelectiveElement(List<IntrospectedColumn> columns, IntrospectedColumn versionColumn, XmlElement setsElement) {
        // 替换update set
        XmlElementTools.replaceXmlElement(setsElement, this.generateSetsSelective(columns, versionColumn));
        return true;
    }

    // ====================================================== 一些私有节点生成方法 =========================================================

    /**
     * insert column selective
     * @param columns
     * @return
     */
    private XmlElement generateInsertColumnSelective(List<IntrospectedColumn> columns) {
        XmlElement insertColumnsChooseEle = new XmlElement("choose");

        XmlElement insertWhenEle = new XmlElement("when");
        insertWhenEle.addAttribute(new Attribute("test", "selective != null and selective.length > 0"));
        insertColumnsChooseEle.addElement(insertWhenEle);

        XmlElement insertForeachEle = new XmlElement("foreach");
        insertForeachEle.addAttribute(new Attribute("collection", "selective"));
        insertForeachEle.addAttribute(new Attribute("item", "column"));
        insertForeachEle.addAttribute(new Attribute("open", "("));
        insertForeachEle.addAttribute(new Attribute("separator", ","));
        insertForeachEle.addAttribute(new Attribute("close", ")"));
        insertForeachEle.addElement(new TextElement("${column.escapedColumnName}"));
        insertWhenEle.addElement(insertForeachEle);

        XmlElement insertOtherwiseEle = new XmlElement("otherwise");
        insertOtherwiseEle.addElement(XmlElementGeneratorTools.generateKeysSelective(columns, "record."));
        insertColumnsChooseEle.addElement(insertOtherwiseEle);

        XmlElement insertTrimElement = new XmlElement("trim");
        insertTrimElement.addAttribute(new Attribute("prefix", "("));
        insertTrimElement.addAttribute(new Attribute("suffix", ")"));
        insertTrimElement.addAttribute(new Attribute("suffixOverrides", ","));
        insertOtherwiseEle.addElement(insertTrimElement);

        return insertColumnsChooseEle;
    }

    /**
     * insert column selective
     * @param columns
     * @return
     */
    private XmlElement generateInsertValuesSelective(List<IntrospectedColumn> columns) {
        return generateInsertValuesSelective(columns, true);
    }

    /**
     * insert column selective
     * @param columns
     * @param bracket
     * @return
     */
    private XmlElement generateInsertValuesSelective(List<IntrospectedColumn> columns, boolean bracket) {
        XmlElement insertValuesChooseEle = new XmlElement("choose");

        XmlElement valuesWhenEle = new XmlElement("when");
        valuesWhenEle.addAttribute(new Attribute("test", "selective != null and selective.length > 0"));
        insertValuesChooseEle.addElement(valuesWhenEle);

        XmlElement valuesForeachEle = new XmlElement("foreach");
        valuesForeachEle.addAttribute(new Attribute("collection", "selective"));
        valuesForeachEle.addAttribute(new Attribute("item", "column"));
        valuesForeachEle.addAttribute(new Attribute("separator", ","));
        if (bracket) {
            valuesForeachEle.addAttribute(new Attribute("open", "("));
            valuesForeachEle.addAttribute(new Attribute("close", ")"));
        }
        valuesForeachEle.addElement(new TextElement("#{record.${column.javaProperty},jdbcType=${column.jdbcType}}"));
        valuesWhenEle.addElement(valuesForeachEle);

        XmlElement valuesOtherwiseEle = new XmlElement("otherwise");
        insertValuesChooseEle.addElement(valuesOtherwiseEle);
        valuesOtherwiseEle.addElement(XmlElementGeneratorTools.generateValuesSelective(columns, "record.", bracket));

        return insertValuesChooseEle;
    }

    /**
     * sets selective
     * @param columns
     * @return
     */
    private XmlElement generateSetsSelective(List<IntrospectedColumn> columns) {
        return generateSetsSelective(columns, null);
    }

    /**
     * sets selective
     * @param columns
     * @return
     */
    private XmlElement generateSetsSelective(List<IntrospectedColumn> columns, IntrospectedColumn versionColumn) {
        XmlElement setsChooseEle = new XmlElement("choose");

        XmlElement setWhenEle = new XmlElement("when");
        setWhenEle.addAttribute(new Attribute("test", "selective != null and selective.length > 0"));
        setsChooseEle.addElement(setWhenEle);

        XmlElement setForeachEle = new XmlElement("foreach");
        setWhenEle.addElement(setForeachEle);
        setForeachEle.addAttribute(new Attribute("collection", "selective"));
        setForeachEle.addAttribute(new Attribute("item", "column"));
        setForeachEle.addAttribute(new Attribute("separator", ","));

        // 1. 先要排除versionColumn
        XmlElement versionColumnCheckEle = null;
        if (versionColumn != null) {
            versionColumnCheckEle = new XmlElement("if");
            versionColumnCheckEle.addAttribute(new Attribute("test", "column.value != '" + versionColumn.getActualColumnName() + "'.toString()"));
        }
        // 2. Increment Sets
        List<XmlElement> incrementSetEles = PluginTools.getHook(IIncrementPluginHook.class).generateIncrementSetForSelectiveEnhancedPlugin(columns);
        if (incrementSetEles == null) {
            incrementSetEles = PluginTools.getHook(IIncrementsPluginHook.class).incrementSetsWithSelectiveEnhancedPluginElementGenerated(columns);
        }
        // 3. typeHandler 节点
        List<XmlElement> typeHandlerSetEles = new ArrayList<>();
        for (IntrospectedColumn column : columns) {
            if (StringUtility.stringHasValue(column.getTypeHandler())
                    && !(PluginTools.getHook(IIncrementsPluginHook.class).supportIncrement(column))
                    || PluginTools.getHook(IIncrementPluginHook.class).supportIncrement(column)
                    ) {
                XmlElement whenEle = new XmlElement("when");
                whenEle.addAttribute(new Attribute("test", "'" + column.getActualColumnName() + "'.toString() == column.value"));
                whenEle.addElement(new TextElement("${column.escapedColumnName} = " + XmlElementGeneratorTools.getParameterClause("record.${column.javaProperty}", column)));

                typeHandlerSetEles.add(whenEle);
            }
        }
        // 3. 普通节点
        TextElement normalEle = new TextElement("${column.escapedColumnName} = #{record.${column.javaProperty},jdbcType=${column.jdbcType}}");

        // 4. 如果Increment Sets不为空 或者 typeHandler不为空，生成Choose节点
        XmlElement chooseEle = null;
        if (incrementSetEles != null || !typeHandlerSetEles.isEmpty()) {
            chooseEle = new XmlElement("choose");
            if (incrementSetEles != null) {
                for (XmlElement whenIncEle : incrementSetEles) {
                    chooseEle.addElement(whenIncEle);
                }
            }

            for (XmlElement whenEle : typeHandlerSetEles) {
                chooseEle.addElement(whenEle);
            }

            XmlElement otherwiseEle = new XmlElement("otherwise");
            otherwiseEle.addElement(normalEle);
            chooseEle.addElement(otherwiseEle);
        }

        // 5. 如果version不为空
        if (versionColumnCheckEle != null) {
            versionColumnCheckEle.addElement(chooseEle != null ? chooseEle : normalEle);

            setForeachEle.addElement(versionColumnCheckEle);
        } else {
            setForeachEle.addElement(chooseEle != null ? chooseEle : normalEle);
        }

        // 普通Selective
        XmlElement setOtherwiseEle = new XmlElement("otherwise");
        setOtherwiseEle.addElement(XmlElementGeneratorTools.generateSetsSelective(columns, "record."));
        setsChooseEle.addElement(setOtherwiseEle);

        return setsChooseEle;
    }
}
