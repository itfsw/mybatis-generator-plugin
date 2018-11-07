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
import com.itfsw.mybatis.generator.plugins.utils.JavaElementGeneratorTools;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;

/**
 * ---------------------------------------------------------------------------
 * Cloneable
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2018/11/7 15:26
 * ---------------------------------------------------------------------------
 */
public class ModelCloneablePlugin extends BasePlugin {
    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.supportCloneable(topLevelClass, introspectedTable);
        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.supportCloneable(topLevelClass, introspectedTable);
        return super.modelPrimaryKeyClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.supportCloneable(topLevelClass, introspectedTable);
        return super.modelRecordWithBLOBsClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * 支持Cloneable
     * @param topLevelClass
     * @param introspectedTable
     */
    private void supportCloneable(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // implement
        topLevelClass.addSuperInterface(new FullyQualifiedJavaType("java.lang.Cloneable"));
        // clone
        Method cloneMethod = JavaElementGeneratorTools.generateMethod(
                "clone",
                JavaVisibility.PUBLIC,
                topLevelClass.getType()
        );
        commentGenerator.addGeneralMethodComment(cloneMethod, introspectedTable);
        cloneMethod.addAnnotation("@Override");
        cloneMethod.addException(new FullyQualifiedJavaType("java.lang.CloneNotSupportedException"));
        cloneMethod.addBodyLine("return (" + topLevelClass.getType().getShortName() + ") super.clone();");
        topLevelClass.addMethod(cloneMethod);
    }
}
