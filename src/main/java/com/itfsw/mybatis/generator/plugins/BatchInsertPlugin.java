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

import com.itfsw.mybatis.generator.plugins.utils.CommentTools;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * ---------------------------------------------------------------------------
 * 批量插入插件
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/1/13 9:33
 * ---------------------------------------------------------------------------
 */
public class BatchInsertPlugin extends PluginAdapter {
    private static final Logger logger = LoggerFactory.getLogger(BatchInsertPlugin.class);
    public static final String METHOD_BATCH_INSERT = "batchInsert";  // 方法名
    public static final String METHOD_BATCH_INSERT_SELECTIVE = "batchInsertSelective";  // 方法名
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    /**
     * Java Client Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param interfaze
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        logger.debug("itfsw:生成"+interfaze.getType()+"对应batchInsert方法...");
        // 方法生成
        Method method = new Method(METHOD_BATCH_INSERT);
        // 方法可见性 interface会忽略
        // method.setVisibility(JavaVisibility.PUBLIC);
        // 返回值类型
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        // 添加参数
        FullyQualifiedJavaType type = FullyQualifiedJavaType.getNewListInstance();
        type.addTypeArgument(interfaze.getType());
        method.addParameter(new Parameter(type, "list", "@Param(\"list\")"));
        // 添加方法说明
        CommentTools.addGeneralMethodComment(method, introspectedTable);

        // interface 增加方法
        interfaze.addMethod(method);

        logger.debug("itfsw:生成"+interfaze.getType()+"对应batchInsertSelective方法...");
        // 方法生成
        Method method1 = new Method(METHOD_BATCH_INSERT_SELECTIVE);
        // 方法可见性 interface会忽略
        // method1.setVisibility(JavaVisibility.PUBLIC);
        // 返回值类型
        method1.setReturnType(FullyQualifiedJavaType.getIntInstance());
        // 添加参数
        FullyQualifiedJavaType type1 = FullyQualifiedJavaType.getNewListInstance();
        type1.addTypeArgument(interfaze.getType());
        method1.addParameter(new Parameter(type1, "list", "@Param(\"list\")"));
        // 添加方法说明
        CommentTools.addGeneralMethodComment(method1, introspectedTable);

        // interface 增加方法
        interfaze.addMethod(method1);

        return true;
    }

    /**
     * SQL Map Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param document
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        return true;
    }
}
