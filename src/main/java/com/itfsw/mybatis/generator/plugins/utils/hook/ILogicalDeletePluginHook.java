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

package com.itfsw.mybatis.generator.plugins.utils.hook;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2018/9/11 11:00
 * ---------------------------------------------------------------------------
 */
public interface ILogicalDeletePluginHook {
    /**
     * logicalDeleteByExample
     * @param method
     * @param interfaze
     * @param introspectedTable
     * @return
     */
    boolean clientLogicalDeleteByExampleMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable);

    /**
     * logicalDeleteByPrimaryKey
     * @param method
     * @param interfaze
     * @param introspectedTable
     * @return
     */
    boolean clientLogicalDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable);

    /**
     * logicalDeleteByExample
     * @param document
     * @param element
     * @param logicalDeleteColumn
     * @param logicalDeleteValue
     * @param introspectedTable
     * @return
     */
    boolean sqlMapLogicalDeleteByExampleElementGenerated(Document document, XmlElement element, IntrospectedColumn logicalDeleteColumn, String logicalDeleteValue, IntrospectedTable introspectedTable);

    /**
     * logicalDeleteByPrimaryKey
     * @param document
     * @param element
     * @param logicalDeleteColumn
     * @param logicalDeleteValue
     * @param introspectedTable
     * @return
     */
    boolean sqlMapLogicalDeleteByPrimaryKeyElementGenerated(Document document, XmlElement element, IntrospectedColumn logicalDeleteColumn, String logicalDeleteValue, IntrospectedTable introspectedTable);

    /**
     * 逻辑删除枚举是否生成
     * @param logicalDeleteColumn
     * @return
     */
    boolean logicalDeleteEnumGenerated(IntrospectedColumn logicalDeleteColumn);
}
