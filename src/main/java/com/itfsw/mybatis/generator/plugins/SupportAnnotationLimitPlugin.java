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

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页插件支持注解生成-[加强]
 *
 * @author simon
 * @email 812135023@qq.com
 * @date 2018-9-1 16:48
 */
public class SupportAnnotationLimitPlugin extends LimitPlugin {


    /**
     * 支持注解的sql-provider的SQL-WithBLOBs
     */
    @Override
    public boolean providerSelectByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        super.providerSelectByExampleWithBLOBsMethodGenerated(method, topLevelClass, introspectedTable);
        return this.generateLimitHandler(method, topLevelClass, introspectedTable);
    }

    /**
     * 支持注解的sql-provider的SQL-WithoutBLOBs
     */
    @Override
    public boolean providerSelectByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        super.providerSelectByExampleWithoutBLOBsMethodGenerated(method, topLevelClass, introspectedTable);
        return this.generateLimitHandler(method, topLevelClass, introspectedTable);
    }

    /**
     * 生成limit逻辑
     */
    private boolean generateLimitHandler(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        List<String> bodyLines = method.getBodyLines();
        int i = bodyLines.indexOf("return SQL();");
        if (i == -1) {
            i = bodyLines.indexOf("return sql.toString();");
        }

        List<String> limitSql = new ArrayList<>(3);

        limitSql.add("if (example != null && null != example.getRows() && null != example.getOffset()) {");
        limitSql.add("return sql.toString().concat(String.format(\" LIMIT %d,%d \", example.getOffset(), example.getRows()));");
        limitSql.add("}");

        method.getBodyLines().addAll(i, limitSql);
        return true;
    }
}
