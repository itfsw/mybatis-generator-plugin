/*
 * Copyright (c) 2014.
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

import com.itfsw.mybatis.generator.plugins.utils.CommentTools;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * ---------------------------------------------------------------------------
 * 增加Model Builder方法
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2016/12/28 14:56
 * ---------------------------------------------------------------------------
 */
public class ModelBuilderPlugin extends PluginAdapter {
    public static final String BUILDER_CLASS_NAME = "Builder";  // Builder 类名
    private static final Logger logger = LoggerFactory.getLogger(ModelBuilderPlugin.class);

    /**
     * {@inheritDoc}
     */
    public boolean validate(List<String> warnings) {
        return true;
    }

    /**
     * Model Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param topLevelClass
     * @param introspectedTable
     * @return
     * @author hewei
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        List<Field> fields = topLevelClass.getFields();

        // 生成内部Builder类
        InnerClass innerClass = new InnerClass(BUILDER_CLASS_NAME);
        innerClass.setVisibility(JavaVisibility.PUBLIC);
        innerClass.setStatic(true);
        CommentTools.addClassComment(innerClass, introspectedTable);
        logger.info("hw:生成内部Builder类");

        // 构建内部obj变量
        Field f = new Field("obj", topLevelClass.getType());
        f.setVisibility(JavaVisibility.PRIVATE);
        innerClass.addField(f);

        // 构造构造方法
        Method constructor = new Method(BUILDER_CLASS_NAME);
        constructor.setVisibility(JavaVisibility.PUBLIC);
        constructor.setConstructor(true);
        constructor.addBodyLine(new StringBuilder("this.obj = new ")
                .append(topLevelClass.getType().getShortName()).append("();").toString());
        innerClass.addMethod(constructor);
        logger.info("hw:生成内部Builder类构造方法");

        // ！！可能Model存在复合主键情况，字段要加上这些
        if (topLevelClass.getSuperClass() != null && topLevelClass.getSuperClass().compareTo(new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType())) == 0){
            for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                Field field = JavaBeansUtil.getJavaBeansField(introspectedColumn, context, introspectedTable);
                Method setterMethod = JavaBeansUtil.getJavaBeansSetter(introspectedColumn, context, introspectedTable);

                Method method = new Method(field.getName());
                method.setVisibility(JavaVisibility.PUBLIC);
                method.setReturnType(innerClass.getType());
                method.addParameter(new Parameter(field.getType(), field.getName()));
                method.addBodyLine(new StringBuilder().append("obj.").append(setterMethod.getName())
                        .append("(").append(field.getName()).append(");").toString());
                method.addBodyLine(new StringBuilder().append("return this;").toString());
                CommentTools.addGeneralMethodComment(method, introspectedTable);
                innerClass.addMethod(method);
                logger.info("hw:生成内部Builder类的复合主键字段对应方法"+field.getName());
            }
        }

        // 根据Model属性生成链式赋值方法
        for (Field field : fields) {
            if (field.isStatic())
                continue;

            Method method = new Method(field.getName());
            method.setVisibility(JavaVisibility.PUBLIC);
            method.setReturnType(innerClass.getType());
            method.addParameter(new Parameter(field.getType(), field.getName()));
            method.addBodyLine(new StringBuilder().append("obj.").append(field.getName())
                    .append(" = ").append(field.getName()).append(";").toString());
            method.addBodyLine(new StringBuilder().append("return this;").toString());
            CommentTools.addGeneralMethodComment(method, introspectedTable);
            innerClass.addMethod(method);
            logger.info("hw:生成内部Builder类的普通字段对应方法"+field.getName());
        }

        Method build = new Method("build");
        build.setReturnType(topLevelClass.getType());
        build.setVisibility(JavaVisibility.PUBLIC);
        build.addBodyLine("return this.obj;");
        CommentTools.addGeneralMethodComment(build, introspectedTable);
        innerClass.addMethod(build);
        logger.info("hw:生成内部Builder类的build方法");

        topLevelClass.addInnerClass(innerClass);
        return true;
    }
}
