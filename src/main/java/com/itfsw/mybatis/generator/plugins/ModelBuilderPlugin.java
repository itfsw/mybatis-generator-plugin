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
import com.itfsw.mybatis.generator.plugins.utils.IncrementsPluginTools;
import com.itfsw.mybatis.generator.plugins.utils.JavaElementGeneratorTools;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.List;

/**
 * ---------------------------------------------------------------------------
 * 增加Model Builder方法
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2016/12/28 14:56
 * ---------------------------------------------------------------------------
 */
public class ModelBuilderPlugin extends BasePlugin {
    public static final String BUILDER_CLASS_NAME = "Builder";  // Builder 类名

    /**
     * Model Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 判断是否有生成Model的WithBLOBs类
        List<IntrospectedColumn> columns = introspectedTable.getRules().generateRecordWithBLOBsClass() ? introspectedTable.getNonBLOBColumns() : introspectedTable.getAllColumns();
        InnerClass innerClass = this.generateModelBuilder(topLevelClass, introspectedTable, columns, true);
        topLevelClass.addInnerClass(innerClass);
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
        InnerClass innerClass = this.generateModelBuilder(topLevelClass, introspectedTable, introspectedTable.getAllColumns(), false);
        topLevelClass.addInnerClass(innerClass);
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
        InnerClass innerClass = this.generateModelBuilder(topLevelClass, introspectedTable, introspectedTable.getPrimaryKeyColumns(), false);
        topLevelClass.addInnerClass(innerClass);
        return super.modelPrimaryKeyClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * 生成ModelBuilder
     * @param topLevelClass
     * @param introspectedTable
     * @param columns
     * @param modelBaseRecord
     * @return
     */
    private InnerClass generateModelBuilder(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, List<IntrospectedColumn> columns, boolean modelBaseRecord) {
        // 生成内部Builder类
        InnerClass innerClass = new InnerClass(BUILDER_CLASS_NAME);
        innerClass.setVisibility(JavaVisibility.PUBLIC);
        innerClass.setStatic(true);
        commentGenerator.addClassComment(innerClass, introspectedTable);
        logger.debug("itfsw(数据Model链式构建插件):" + topLevelClass.getType().getShortName() + "增加内部Builder类。");

        // 构建内部obj变量
        Field f = JavaElementGeneratorTools.generateField("obj", JavaVisibility.PRIVATE, topLevelClass.getType(), null);
        commentGenerator.addFieldComment(f, introspectedTable);
        innerClass.addField(f);

        // 构造构造方法
        Method constructor = new Method(BUILDER_CLASS_NAME);
        constructor.setVisibility(JavaVisibility.PUBLIC);
        constructor.setConstructor(true);
        constructor.addBodyLine(new StringBuilder("this.obj = new ").append(topLevelClass.getType().getShortName()).append("();").toString());
        commentGenerator.addGeneralMethodComment(constructor, introspectedTable);
        innerClass.addMethod(constructor);
        logger.debug("itfsw(数据Model链式构建插件):" + topLevelClass.getType().getShortName() + ".Builder增加的构造方法。");

        for (IntrospectedColumn introspectedColumn : columns) {
            Field field = JavaBeansUtil.getJavaBeansField(introspectedColumn, context, introspectedTable);
            Method setterMethod = JavaBeansUtil.getJavaBeansSetter(introspectedColumn, context, introspectedTable);

            Method method = JavaElementGeneratorTools.generateMethod(
                    field.getName(),
                    JavaVisibility.PUBLIC,
                    innerClass.getType(),
                    new Parameter(field.getType(), field.getName())
            );
            commentGenerator.addSetterComment(method, introspectedTable, introspectedColumn);
            method = JavaElementGeneratorTools.generateMethodBody(
                    method,
                    "obj." + setterMethod.getName() + "(" + field.getName() + ");",
                    "return this;"
            );
            innerClass.addMethod(method);
            logger.debug("itfsw(数据Model链式构建插件):" + topLevelClass.getType().getShortName() + ".Builder增加" + method.getName() + "方法(复合主键)。");
        }

        Method build = JavaElementGeneratorTools.generateMethod(
                "build",
                JavaVisibility.PUBLIC,
                topLevelClass.getType()
        );
        build.addBodyLine("return this.obj;");
        commentGenerator.addGeneralMethodComment(build, introspectedTable);
        innerClass.addMethod(build);

        logger.debug("itfsw(数据Model链式构建插件):" + topLevelClass.getType().getShortName() + ".Builder增加build方法。");


        // ========================================== IncrementsPlugin =======================================
        IncrementsPluginTools incTools = IncrementsPluginTools.getTools(context, introspectedTable, warnings);
        if (incTools.support()) {
            if (modelBaseRecord) {
                // 增加枚举
                InnerEnum eIncrements = new InnerEnum(new FullyQualifiedJavaType("Inc"));
                eIncrements.setVisibility(JavaVisibility.PUBLIC);
                eIncrements.setStatic(true);
                eIncrements.addEnumConstant("INC(\"+\")");
                eIncrements.addEnumConstant("DEC(\"-\")");
                commentGenerator.addEnumComment(eIncrements, introspectedTable);
                // 生成属性和构造函数
                Field fValue = new Field("value", FullyQualifiedJavaType.getStringInstance());
                fValue.setVisibility(JavaVisibility.PRIVATE);
                fValue.setFinal(true);
                commentGenerator.addFieldComment(fValue, introspectedTable);
                eIncrements.addField(fValue);

                Method mInc = new Method("Inc");
                mInc.setConstructor(true);
                mInc.addBodyLine("this.value = value;");
                mInc.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "value"));
                commentGenerator.addGeneralMethodComment(mInc, introspectedTable);
                eIncrements.addMethod(mInc);

                Method mValue = JavaElementGeneratorTools.generateGetterMethod(fValue);
                commentGenerator.addGeneralMethodComment(mValue, introspectedTable);
                eIncrements.addMethod(mValue);

                innerClass.addInnerEnum(eIncrements);
                // 增加field
                Field fIncrements = JavaElementGeneratorTools.generateField(
                        IncrementsPlugin.FIELD_INC_MAP,
                        JavaVisibility.PROTECTED,
                        new FullyQualifiedJavaType("Map<String, " + incTools.getIncEnum().getFullyQualifiedName() + ">"),
                        "new HashMap<String, " + incTools.getIncEnum().getFullyQualifiedName() + ">()"
                );
                commentGenerator.addFieldComment(fIncrements, introspectedTable);
                topLevelClass.addField(fIncrements);
                topLevelClass.addImportedType("java.util.Map");
                topLevelClass.addImportedType("java.util.HashMap");
                // getter&setter
                Method mGetter = JavaElementGeneratorTools.generateGetterMethod(fIncrements);
                commentGenerator.addGetterComment(mGetter, introspectedTable, null);
                topLevelClass.addMethod(mGetter);
                Method mSetter = JavaElementGeneratorTools.generateSetterMethod(fIncrements);
                commentGenerator.addSetterComment(mSetter, introspectedTable, null);
                topLevelClass.addMethod(mSetter);
                // 增加判断方法
                Method mHasIncsForColumn = JavaElementGeneratorTools.generateMethod(
                        IncrementsPlugin.METHOD_INC_CHECK,
                        JavaVisibility.PUBLIC,
                        FullyQualifiedJavaType.getBooleanPrimitiveInstance(),
                        new Parameter(FullyQualifiedJavaType.getStringInstance(), "column")
                );
                commentGenerator.addGeneralMethodComment(mHasIncsForColumn, introspectedTable);
                mHasIncsForColumn.addBodyLine("return " + IncrementsPlugin.FIELD_INC_MAP + ".get(column) != null;");
                FormatTools.addMethodWithBestPosition(topLevelClass, mHasIncsForColumn);
            }

            // Builder 中 添加字段支持
            for (IntrospectedColumn column : columns) {
                if (incTools.supportColumn(column)) {
                    Field field = JavaBeansUtil.getJavaBeansField(column, context, introspectedTable);
                    // 增加方法
                    Method mIncrements = JavaElementGeneratorTools.generateMethod(
                            field.getName(),
                            JavaVisibility.PUBLIC,
                            innerClass.getType(),
                            new Parameter(field.getType(), field.getName()),
                            new Parameter(incTools.getIncEnum(), "inc")
                    );
                    commentGenerator.addSetterComment(mIncrements, introspectedTable, column);

                    Method setterMethod = JavaBeansUtil.getJavaBeansSetter(column, context, introspectedTable);
                    mIncrements.addBodyLine("obj." + IncrementsPlugin.FIELD_INC_MAP + ".put(\"" + column.getActualColumnName() + "\", inc);");
                    mIncrements.addBodyLine("obj." + setterMethod.getName() + "(" + field.getName() + ");");
                    mIncrements.addBodyLine("return this;");

                    FormatTools.addMethodWithBestPosition(innerClass, mIncrements);
                }
            }
        }

        return innerClass;
    }

}
