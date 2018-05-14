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
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.internal.util.JavaBeansUtil;

/**
 * ---------------------------------------------------------------------------
 * 数据Model属性对应Column获取插件
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/1/17 11:20
 * ---------------------------------------------------------------------------
 */
public class ModelColumnPlugin extends BasePlugin {
    public static final String ENUM_NAME = "Column";  // 内部Enum名

    /**
     * Model Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addInnerEnum(this.generateColumnEnum(topLevelClass, introspectedTable));
        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * Model Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addInnerEnum(this.generateColumnEnum(topLevelClass, introspectedTable));
        return super.modelRecordWithBLOBsClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addInnerEnum(this.generateColumnEnum(topLevelClass, introspectedTable));
        return super.modelPrimaryKeyClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * 生成Column字段枚举
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    private InnerEnum generateColumnEnum(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 生成内部枚举
        InnerEnum innerEnum = new InnerEnum(new FullyQualifiedJavaType(ENUM_NAME));
        innerEnum.setVisibility(JavaVisibility.PUBLIC);
        innerEnum.setStatic(true);
        commentGenerator.addEnumComment(innerEnum, introspectedTable);
        logger.debug("itfsw(数据Model属性对应Column获取插件):" + topLevelClass.getType().getShortName() + "增加内部Builder类。");

        // 生成属性和构造函数
        Field columnField = new Field("column", FullyQualifiedJavaType.getStringInstance());
        columnField.setVisibility(JavaVisibility.PRIVATE);
        columnField.setFinal(true);
        commentGenerator.addFieldComment(columnField, introspectedTable);
        innerEnum.addField(columnField);

        Field javaPropertyField = new Field("javaProperty", FullyQualifiedJavaType.getStringInstance());
        javaPropertyField.setVisibility(JavaVisibility.PRIVATE);
        javaPropertyField.setFinal(true);
        commentGenerator.addFieldComment(javaPropertyField, introspectedTable);
        innerEnum.addField(javaPropertyField);

        Field jdbcTypeField = new Field("jdbcType", FullyQualifiedJavaType.getStringInstance());
        jdbcTypeField.setVisibility(JavaVisibility.PRIVATE);
        jdbcTypeField.setFinal(true);
        commentGenerator.addFieldComment(jdbcTypeField, introspectedTable);
        innerEnum.addField(jdbcTypeField);

        Method mValue = new Method("value");
        mValue.setVisibility(JavaVisibility.PUBLIC);
        mValue.setReturnType(FullyQualifiedJavaType.getStringInstance());
        mValue.addBodyLine("return this.column;");
        commentGenerator.addGeneralMethodComment(mValue, introspectedTable);
        FormatTools.addMethodWithBestPosition(innerEnum, mValue);

        Method mGetValue = new Method("getValue");
        mGetValue.setVisibility(JavaVisibility.PUBLIC);
        mGetValue.setReturnType(FullyQualifiedJavaType.getStringInstance());
        mGetValue.addBodyLine("return this.column;");
        commentGenerator.addGeneralMethodComment(mGetValue, introspectedTable);
        FormatTools.addMethodWithBestPosition(innerEnum, mGetValue);

        Method mGetJavaProperty = JavaElementGeneratorTools.generateGetterMethod(javaPropertyField);
        commentGenerator.addGeneralMethodComment(mGetJavaProperty, introspectedTable);
        FormatTools.addMethodWithBestPosition(innerEnum, mGetJavaProperty);

        Method mGetJdbcType = JavaElementGeneratorTools.generateGetterMethod(jdbcTypeField);
        commentGenerator.addGeneralMethodComment(mGetJdbcType, introspectedTable);
        FormatTools.addMethodWithBestPosition(innerEnum, mGetJdbcType);

        Method constructor = new Method(ENUM_NAME);
        constructor.setConstructor(true);
        constructor.addBodyLine("this.column = column;");
        constructor.addBodyLine("this.javaProperty = javaProperty;");
        constructor.addBodyLine("this.jdbcType = jdbcType;");
        constructor.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "column"));
        constructor.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "javaProperty"));
        constructor.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "jdbcType"));
        commentGenerator.addGeneralMethodComment(constructor, introspectedTable);
        FormatTools.addMethodWithBestPosition(innerEnum, constructor);
        logger.debug("itfsw(数据Model属性对应Column获取插件):" + topLevelClass.getType().getShortName() + ".Column增加构造方法和column属性。");

        // Enum枚举
        for (IntrospectedColumn introspectedColumn : introspectedTable.getAllColumns()) {
            Field field = JavaBeansUtil.getJavaBeansField(introspectedColumn, context, introspectedTable);

            StringBuffer sb = new StringBuffer();
            sb.append(field.getName());
            sb.append("(\"");
            sb.append(introspectedColumn.getActualColumnName());
            sb.append("\", \"");
            sb.append(introspectedColumn.getJavaProperty());
            sb.append("\", \"");
            sb.append(introspectedColumn.getJdbcTypeName());
            sb.append("\")");

            innerEnum.addEnumConstant(sb.toString());
            logger.debug("itfsw(数据Model属性对应Column获取插件):" + topLevelClass.getType().getShortName() + ".Column增加" + field.getName() + "枚举。");
        }

        // asc 和 desc 方法
        Method desc = new Method("desc");
        desc.setVisibility(JavaVisibility.PUBLIC);
        desc.setReturnType(FullyQualifiedJavaType.getStringInstance());
        desc.addBodyLine("return this.column + \" DESC\";");
        commentGenerator.addGeneralMethodComment(desc, introspectedTable);
        FormatTools.addMethodWithBestPosition(innerEnum, desc);

        Method asc = new Method("asc");
        asc.setVisibility(JavaVisibility.PUBLIC);
        asc.setReturnType(FullyQualifiedJavaType.getStringInstance());
        asc.addBodyLine("return this.column + \" ASC\";");
        commentGenerator.addGeneralMethodComment(asc, introspectedTable);
        FormatTools.addMethodWithBestPosition(innerEnum, asc);
        logger.debug("itfsw(数据Model属性对应Column获取插件):" + topLevelClass.getType().getShortName() + ".Column增加asc()和desc()方法。");

        return innerEnum;
    }
}
