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
import com.itfsw.mybatis.generator.plugins.utils.hook.ISelectSelectivePluginHook;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.List;

/**
 * ---------------------------------------------------------------------------
 * 增加分页方法
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2016/12/29 18:14
 * ---------------------------------------------------------------------------
 */
public class LimitPlugin extends BasePlugin implements ISelectSelectivePluginHook {
    /**
     * 分页开始页码
     */
    public final static String PRO_START_PAGE = "startPage";
    private final static int DEFAULT_START_PAGE = 0;
    private int startPage;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(List<String> warnings) {
        // 该插件只支持MYSQL
        if ("com.mysql.jdbc.Driver".equalsIgnoreCase(this.getContext().getJdbcConnectionConfiguration().getDriverClass()) == false
                && "com.mysql.cj.jdbc.Driver".equalsIgnoreCase(this.getContext().getJdbcConnectionConfiguration().getDriverClass()) == false) {
            warnings.add("itfsw:插件" + this.getClass().getTypeName() + "只支持MySQL数据库！");
            return false;
        }
        return super.validate(warnings);
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param introspectedTable
     */
    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        super.initialized(introspectedTable);

        // 获取配置
        String startPage = this.getProperties().getProperty(LimitPlugin.PRO_START_PAGE);
        if (StringUtility.stringHasValue(startPage)) {
            this.startPage = Integer.valueOf(startPage);
        } else {
            this.startPage = DEFAULT_START_PAGE;
        }
    }

    /**
     * ModelExample Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        PrimitiveTypeWrapper integerWrapper = FullyQualifiedJavaType.getIntInstance().getPrimitiveTypeWrapper();
        // 添加offset和rows字段
        Field offsetField = JavaElementGeneratorTools.generateField(
                "offset",
                JavaVisibility.PROTECTED,
                integerWrapper,
                null
        );
        commentGenerator.addFieldComment(offsetField, introspectedTable);
        topLevelClass.addField(offsetField);

        Field rowsField = JavaElementGeneratorTools.generateField(
                "rows",
                JavaVisibility.PROTECTED,
                integerWrapper,
                null
        );
        commentGenerator.addFieldComment(rowsField, introspectedTable);
        topLevelClass.addField(rowsField);
        logger.debug("itfsw(MySQL分页插件):" + topLevelClass.getType().getShortName() + "增加offset和rows字段");

        // 增加getter && setter 方法
        Method mSetOffset = JavaElementGeneratorTools.generateSetterMethod(offsetField);
        commentGenerator.addGeneralMethodComment(mSetOffset, introspectedTable);
        FormatTools.addMethodWithBestPosition(topLevelClass, mSetOffset);

        Method mGetOffset = JavaElementGeneratorTools.generateGetterMethod(offsetField);
        commentGenerator.addGeneralMethodComment(mGetOffset, introspectedTable);
        FormatTools.addMethodWithBestPosition(topLevelClass, mGetOffset);

        Method mSetRows = JavaElementGeneratorTools.generateSetterMethod(rowsField);
        commentGenerator.addGeneralMethodComment(mSetRows, introspectedTable);
        FormatTools.addMethodWithBestPosition(topLevelClass, mSetRows);

        Method mGetRows = JavaElementGeneratorTools.generateGetterMethod(rowsField);
        commentGenerator.addGeneralMethodComment(mGetRows, introspectedTable);
        FormatTools.addMethodWithBestPosition(topLevelClass, mGetRows);
        logger.debug("itfsw(MySQL分页插件):" + topLevelClass.getType().getShortName() + "增加offset和rows的getter和setter实现。");

        // 提供几个快捷方法
        Method setLimit = JavaElementGeneratorTools.generateMethod(
                "limit",
                JavaVisibility.PUBLIC,
                topLevelClass.getType(),
                new Parameter(integerWrapper, "rows")
        );
        commentGenerator.addGeneralMethodComment(setLimit, introspectedTable);
        setLimit = JavaElementGeneratorTools.generateMethodBody(
                setLimit,
                "this.rows = rows;",
                "return this;"
        );
        FormatTools.addMethodWithBestPosition(topLevelClass, setLimit);

        Method setLimit2 = JavaElementGeneratorTools.generateMethod(
                "limit",
                JavaVisibility.PUBLIC,
                topLevelClass.getType(),
                new Parameter(integerWrapper, "offset"),
                new Parameter(integerWrapper, "rows")
        );
        commentGenerator.addGeneralMethodComment(setLimit2, introspectedTable);
        setLimit2 = JavaElementGeneratorTools.generateMethodBody(
                setLimit2,
                "this.offset = offset;",
                "this.rows = rows;",
                "return this;"
        );
        FormatTools.addMethodWithBestPosition(topLevelClass, setLimit2);
        logger.debug("itfsw(MySQL分页插件):" + topLevelClass.getType().getShortName() + "增加limit方法。");

        Method setPage = JavaElementGeneratorTools.generateMethod(
                "page",
                JavaVisibility.PUBLIC,
                topLevelClass.getType(),
                new Parameter(integerWrapper, "page"),
                new Parameter(integerWrapper, "pageSize")
        );
        commentGenerator.addGeneralMethodComment(setPage, introspectedTable);
        setPage = JavaElementGeneratorTools.generateMethodBody(
                setPage,
                "this.offset = " + (this.startPage == 0 ? "page" : "(page - " + this.startPage + ")") + " * pageSize;",
                "this.rows = pageSize;",
                "return this;"
        );
        FormatTools.addMethodWithBestPosition(topLevelClass, setPage);
        logger.debug("itfsw(MySQL分页插件):" + topLevelClass.getType().getShortName() + "增加page方法");

        // !!! clear 方法增加 offset 和 rows的清理
        List<Method> methodList = topLevelClass.getMethods();
        for (Method method : methodList) {
            if (method.getName().equals("clear")) {
                method.addBodyLine("rows = null;");
                method.addBodyLine("offset = null;");
                logger.debug("itfsw(MySQL分页插件):" + topLevelClass.getType().getShortName() + "修改clear方法,增加rows和offset字段的清空");
            }
        }

        return true;
    }

    /**
     * SQL Map Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param element
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        this.generateLimitElement(element);
        return true;
    }

    /**
     * SQL Map Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param element
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        this.generateLimitElement(element);
        return true;
    }

    // ============================================ ISelectSelectivePluginHook ==========================================

    @Override
    public boolean sqlMapSelectByExampleSelectiveElementGenerated(Document document, XmlElement element, IntrospectedTable introspectedTable) {
        this.generateLimitElementWithExample(element);
        return false;
    }

    /**
     * 生成limit节点
     * @param element
     */
    private void generateLimitElement(XmlElement element) {
        XmlElement ifLimitNotNullElement = new XmlElement("if");
        ifLimitNotNullElement.addAttribute(new Attribute("test", "rows != null"));

        XmlElement ifOffsetNotNullElement = new XmlElement("if");
        ifOffsetNotNullElement.addAttribute(new Attribute("test", "offset != null"));
        ifOffsetNotNullElement.addElement(new TextElement("limit ${offset}, ${rows}"));
        ifLimitNotNullElement.addElement(ifOffsetNotNullElement);

        XmlElement ifOffsetNullElement = new XmlElement("if");
        ifOffsetNullElement.addAttribute(new Attribute("test", "offset == null"));
        ifOffsetNullElement.addElement(new TextElement("limit ${rows}"));
        ifLimitNotNullElement.addElement(ifOffsetNullElement);

        element.addElement(ifLimitNotNullElement);
    }

    /**
     * 生成limit节点
     * @param element
     */
    private void generateLimitElementWithExample(XmlElement element) {
        XmlElement ifLimitNotNullElement = new XmlElement("if");
        ifLimitNotNullElement.addAttribute(new Attribute("test", "example != null and example.rows != null"));

        XmlElement ifOffsetNotNullElement = new XmlElement("if");
        ifOffsetNotNullElement.addAttribute(new Attribute("test", "example.offset != null"));
        ifOffsetNotNullElement.addElement(new TextElement("limit ${example.offset}, ${example.rows}"));
        ifLimitNotNullElement.addElement(ifOffsetNotNullElement);

        XmlElement ifOffsetNullElement = new XmlElement("if");
        ifOffsetNullElement.addAttribute(new Attribute("test", "example.offset == null"));
        ifOffsetNullElement.addElement(new TextElement("limit ${example.rows}"));
        ifLimitNotNullElement.addElement(ifOffsetNullElement);

        element.addElement(ifLimitNotNullElement);
    }
}
