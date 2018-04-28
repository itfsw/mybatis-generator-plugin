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
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2018/4/27 11:09
 * ---------------------------------------------------------------------------
 */
public interface IUpsertPluginHook {
    /**
     * upsertSelective 方法
     * @param method
     * @param interfaze
     * @param introspectedTable
     * @return
     */
    boolean clientUpsertSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable);

    /**
     * upsertByExampleSelective 方法
     * @param method
     * @param interfaze
     * @param introspectedTable
     * @return
     */
    boolean clientUpsertByExampleSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable);

    /**
     * upsertSelective xml
     * @param element
     * @param columns
     * @param insertColumnsEle
     * @param insertValuesEle
     * @param setsEle
     * @param introspectedTable
     * @return
     */
    boolean sqlMapUpsertSelectiveElementGenerated(XmlElement element, List<IntrospectedColumn> columns, XmlElement insertColumnsEle, XmlElement insertValuesEle, XmlElement setsEle, IntrospectedTable introspectedTable);

    /**
     * upsertByExampleSelective xml
     * @param element
     * @param columns
     * @param insertColumnsEle
     * @param insertValuesEle
     * @param setsEle
     * @param introspectedTable
     * @return
     */
    boolean sqlMapUpsertByExampleSelectiveElementGenerated(XmlElement element, List<IntrospectedColumn> columns, XmlElement insertColumnsEle, XmlElement insertValuesEle, XmlElement setsEle, IntrospectedTable introspectedTable);
}
