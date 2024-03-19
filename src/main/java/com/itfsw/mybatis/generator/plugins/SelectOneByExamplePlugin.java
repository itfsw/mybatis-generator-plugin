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
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.HashMap;
import java.util.Map;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * 增加查询一条数据方法
 */
public class SelectOneByExamplePlugin extends BasePlugin {
    public static final String METHOD_SELECT_ONE_BY_EXAMPLE = "selectOneByExample";
    public static final String METHOD_SELECT_ONE_BY_EXAMPLE_WITH_BLOBS = "selectOneByExampleWithBLOBs";
    private final Map<IntrospectedTable, XmlElement> selectOneByExampleEls = new HashMap<>();
    private final Map<IntrospectedTable, XmlElement> selectOneByExampleWithBLOBsEls = new HashMap<>();

    /**
     * Java Client Methods 生成
     * <a href="http://www.mybatis.org/generator/reference/pluggingIn.html">具体执行顺序</a>
     */
    @Override
    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        // 方法生成 selectOneByExample
        Method selectOneMethod = JavaElementGeneratorTools.generateAbstractMethod(
                METHOD_SELECT_ONE_BY_EXAMPLE_WITH_BLOBS,
                JavaVisibility.DEFAULT,
                JavaElementGeneratorTools.getModelTypeWithBLOBs(introspectedTable),
                new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example")
        );
        commentGenerator.addGeneralMethodComment(selectOneMethod, introspectedTable);

        // hook
        if (PluginTools.getHook(ISelectOneByExamplePluginHook.class).clientSelectOneByExampleWithBLOBsMethodGenerated(selectOneMethod, interfaze, introspectedTable)) {
            // interface 增加方法
            FormatTools.addMethodWithBestPosition(interfaze, selectOneMethod);
            logger.debug("itfsw(查询单条数据插件):" + interfaze.getType().getShortName() + "增加selectOneByExampleWithBLOBs方法。");
        }
        return super.clientSelectByExampleWithBLOBsMethodGenerated(method, interfaze, introspectedTable);
    }

    /**
     * Java Client Methods 生成
     * <a href="http://www.mybatis.org/generator/reference/pluggingIn.html">具体执行顺序</a>
     */
    @Override
    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        // 方法生成 selectOneByExample
        Method selectOneMethod = JavaElementGeneratorTools.generateAbstractMethod(
                METHOD_SELECT_ONE_BY_EXAMPLE,
                JavaVisibility.DEFAULT,
                JavaElementGeneratorTools.getModelTypeWithoutBLOBs(introspectedTable),
                new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example")
        );
        commentGenerator.addGeneralMethodComment(selectOneMethod, introspectedTable);

        // hook
        if (PluginTools.getHook(ISelectOneByExamplePluginHook.class).clientSelectOneByExampleWithoutBLOBsMethodGenerated(selectOneMethod, interfaze, introspectedTable)) {
            // interface 增加方法
            FormatTools.addMethodWithBestPosition(interfaze, selectOneMethod);
            logger.debug("itfsw(查询单条数据插件):" + interfaze.getType().getShortName() + "增加selectOneByExample方法。");
        }
        return super.clientSelectByExampleWithoutBLOBsMethodGenerated(selectOneMethod, interfaze, introspectedTable);
    }

    /**
     * <a href="http://www.mybatis.org/generator/reference/pluggingIn.html">具体执行顺序</a>
     */
    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
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
        selectOneByExampleEls.put(introspectedTable, selectOneElement);
        return super.sqlMapSelectByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
    }

    /**
     * <a href="http://www.mybatis.org/generator/reference/pluggingIn.html">具体执行顺序</a>
     */
    @Override
    public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
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

        StringBuilder sb = new StringBuilder();
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

        selectOneByExampleWithBLOBsEls.put(introspectedTable, selectOneWithBLOBsElement);
        return super.sqlMapSelectByExampleWithBLOBsElementGenerated(element, introspectedTable);
    }

    /**
     * SQL Map Methods 生成
     * <a href="http://www.mybatis.org/generator/reference/pluggingIn.html">具体执行顺序</a>
     */
    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        if (selectOneByExampleEls.containsKey(introspectedTable)) {
            XmlElement selectOneByExampleEle = selectOneByExampleEls.get(introspectedTable);
            // hook
            if (PluginTools.getHook(ISelectOneByExamplePluginHook.class).sqlMapSelectOneByExampleWithoutBLOBsElementGenerated(document, selectOneByExampleEle, introspectedTable)) {
                // 添加到根节点
                FormatTools.addElementWithBestPosition(document.getRootElement(), selectOneByExampleEle);
                logger.debug("itfsw(查询单条数据插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加selectOneByExample方法。");
            }
        }

        if (selectOneByExampleWithBLOBsEls.containsKey(introspectedTable)) {
            XmlElement selectOneByExampleWithBLOBsEle = selectOneByExampleWithBLOBsEls.get(introspectedTable);
            // hook
            if (PluginTools.getHook(ISelectOneByExamplePluginHook.class).sqlMapSelectOneByExampleWithBLOBsElementGenerated(document, selectOneByExampleWithBLOBsEle, introspectedTable)) {
                // 添加到根节点
                FormatTools.addElementWithBestPosition(document.getRootElement(), selectOneByExampleWithBLOBsEle);
                logger.debug("itfsw(查询单条数据插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加selectOneByExampleWithBLOBs方法。");
            }
        }

        return true;
    }
}
