/*
 * Copyright (c) 2019.
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
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2018/4/28 17:50
 * ---------------------------------------------------------------------------
 */
public interface IIncrementPluginHook {
    /**
     * 生成增量操作节点
     * @param introspectedColumn
     * @param prefix
     * @param hasComma
     * @return
     */
    XmlElement generateIncrementSet(IntrospectedColumn introspectedColumn, String prefix, boolean hasComma);

    /**
     * 生成增量操作节点
     * @param introspectedColumn
     * @param prefix
     * @return
     */
    XmlElement generateIncrementSetSelective(IntrospectedColumn introspectedColumn, String prefix);

    /**
     * 生成增量操作节点(SelectiveEnhancedPlugin)
     * @param columns
     * @return
     */
    List<XmlElement> generateIncrementSetForSelectiveEnhancedPlugin(List<IntrospectedColumn> columns);

    /**
     * 是否支持increment
     * @param column
     * @return
     */
    boolean supportIncrement(IntrospectedColumn column);
}
