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
import com.itfsw.mybatis.generator.plugins.utils.JavaElementGeneratorTools;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;

/**
 * ---------------------------------------------------------------------------
 * 增加分页方法
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2016/12/29 18:14
 * ---------------------------------------------------------------------------
 */
public class LimitPlugin extends BasePlugin {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(List<String> warnings) {
        // 该插件只支持MYSQL
        if ("com.mysql.jdbc.Driver".equalsIgnoreCase(this.getContext().getJdbcConnectionConfiguration().getDriverClass()) == false){
            logger.error("itfsw:插件"+this.getClass().getTypeName()+"只支持MySQL数据库！");
            return false;
        }
        return super.validate(warnings);
    }

    /**
     * ModelExample Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
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
        logger.debug("itfsw(MySQL分页插件):"+topLevelClass.getType().getShortName()+"增加offset和rows字段");

        // 增加getter && setter 方法
        Method mSetOffset = JavaElementGeneratorTools.generateSetterMethod(offsetField);
        commentGenerator.addGeneralMethodComment(mSetOffset, introspectedTable);
        topLevelClass.addMethod(mSetOffset);

        Method mGetOffset = JavaElementGeneratorTools.generateGetterMethod(offsetField);
        commentGenerator.addGeneralMethodComment(mGetOffset, introspectedTable);
        topLevelClass.addMethod(mGetOffset);

        Method mSetRows = JavaElementGeneratorTools.generateSetterMethod(rowsField);
        commentGenerator.addGeneralMethodComment(mSetRows, introspectedTable);
        topLevelClass.addMethod(mSetRows);

        Method mGetRows = JavaElementGeneratorTools.generateGetterMethod(rowsField);
        commentGenerator.addGeneralMethodComment(mGetRows, introspectedTable);
        topLevelClass.addMethod(mGetRows);
        logger.debug("itfsw(MySQL分页插件):"+topLevelClass.getType().getShortName()+"增加offset和rows的getter和setter实现。");

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
        topLevelClass.addMethod(setLimit);

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
        topLevelClass.addMethod(setLimit2);
        logger.debug("itfsw(MySQL分页插件):"+topLevelClass.getType().getShortName()+"增加limit方法。");

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
                "this.offset = page * pageSize;",
                "this.rows = pageSize;",
                "return this;"
        );
        topLevelClass.addMethod(setPage);
        logger.debug("itfsw(MySQL分页插件):"+topLevelClass.getType().getShortName()+"增加page方法");

        // !!! clear 方法增加 offset 和 rows的清理
        List<Method> methodList = topLevelClass.getMethods();
        for (Method method: methodList){
            if (method.getName().equals("clear")){
                method.addBodyLine("rows = null;");
                method.addBodyLine("offset = null;");
                logger.debug("itfsw(MySQL分页插件):"+topLevelClass.getType().getShortName()+"修改clear方法,增加rows和offset字段的清空");
            }
        }

        return true;
    }

    /**
     * SQL Map Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param element
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
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

        logger.debug("itfsw(MySQL分页插件):"+introspectedTable.getMyBatis3XmlMapperFileName()+"selectByExample方法增加分页条件。");

        return true;
    }

    /**
     * SQL Map Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param element
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return this.sqlMapSelectByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
    }
}
