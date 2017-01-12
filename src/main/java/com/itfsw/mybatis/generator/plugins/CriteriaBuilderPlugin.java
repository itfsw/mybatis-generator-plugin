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
import com.itfsw.mybatis.generator.plugins.utils.InnerInterface;
import com.itfsw.mybatis.generator.plugins.utils.InnerInterfaceWrapperToInnerClass;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * ---------------------------------------------------------------------------
 * 增加Criteria Builder方法
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2016/12/28 14:56
 * ---------------------------------------------------------------------------
 */
public class CriteriaBuilderPlugin extends PluginAdapter {
    private static final Logger logger = LoggerFactory.getLogger(CriteriaBuilderPlugin.class);
    /**
     * {@inheritDoc}
     */
    public boolean validate(List<String> warnings) {
        return true;
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
        List<InnerClass> innerClasses = topLevelClass.getInnerClasses();
        for (InnerClass innerClass : innerClasses) {
            if ("Criteria".equals(innerClass.getType().getShortName())) {
                // 工厂方法
                addFactoryMethodToCriteria(topLevelClass, innerClass, introspectedTable);
                // andIf
                addAndIfMethodToCriteria(topLevelClass, innerClass, introspectedTable);
            }
        }

        List<Method> methods = topLevelClass.getMethods();
        for (Method method : methods) {
            if (!"createCriteriaInternal".equals(method.getName()))
                continue;
            method.getBodyLines().set(0, "Criteria criteria = new Criteria(this);");
            logger.debug("itfsw:CriteriaBuilder修改Example的createCriteriaInternal方法，修改构造Criteria时传入Example对象");
        }
        return true;
    }

    /**
     * 添加工厂方法
     *
     * @param topLevelClass
     * @param innerClass
     * @param introspectedTable
     */
    private void addFactoryMethodToCriteria(TopLevelClass topLevelClass, InnerClass innerClass, IntrospectedTable introspectedTable) {
        Field f = new Field("example", topLevelClass.getType());
        f.setVisibility(JavaVisibility.PRIVATE);
        innerClass.addField(f);

        // overwrite constructor
        List<Method> methods = innerClass.getMethods();
        for (Method method : methods) {
            if (method.isConstructor()) {
                method.addParameter(new Parameter(topLevelClass.getType(), "example"));
                method.addBodyLine("this.example = example;");
                logger.debug("itfsw:CriteriaBuilder修改Criteria的构造方法，增加example参数");
            }
        }

        // add factory method "example"
        Method method = new Method("example");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(topLevelClass.getType());
        method.addBodyLine("return this.example;");
        CommentTools.addGeneralMethodComment(method, introspectedTable);
        innerClass.addMethod(method);
        logger.debug("itfsw:CriteriaBuilder增加工厂方法example");
    }


    /**
     * 增强Criteria的链式调用，添加andIf(boolean addIf, CriteriaAdd add)方法，实现链式调用中按条件增加查询语句
     *
     * @param topLevelClass
     * @param innerClass
     * @param introspectedTable
     */
    private void addAndIfMethodToCriteria(TopLevelClass topLevelClass, InnerClass innerClass, IntrospectedTable introspectedTable){
        // 添加接口CriteriaAdd
        InnerInterface criteriaAddInterface = new InnerInterface("ICriteriaAdd");
        criteriaAddInterface.setVisibility(JavaVisibility.PUBLIC);
        CommentTools.addInterfaceComment(criteriaAddInterface, introspectedTable);

        // ICriteriaAdd增加接口add
        Method addMethod = new Method("add");
        addMethod.setReturnType(innerClass.getType());
        addMethod.addParameter(new Parameter(innerClass.getType(), "add"));
        CommentTools.addGeneralMethodComment(addMethod, introspectedTable);
        criteriaAddInterface.addMethod(addMethod);
        logger.debug("itfsw:Criteria.ICriteriaAdd增加接口add");

        InnerClass innerClassWrapper = new InnerInterfaceWrapperToInnerClass(criteriaAddInterface);
        innerClass.addInnerClass(innerClassWrapper);

        // 添加andIf方法
        Method method = new Method("andIf");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(innerClass.getType());
        method.addParameter(new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "ifAdd"));
        method.addParameter(new Parameter(criteriaAddInterface.getType(), "add"));

        method.addBodyLine("if (ifAdd) {");
        method.addBodyLine("add.add(this);");
        method.addBodyLine("}");
        method.addBodyLine("return this;");
        CommentTools.addGeneralMethodComment(method, introspectedTable);
        innerClass.addMethod(method);
        logger.debug("itfsw:Criteria增加方法andIf");
    }
}
