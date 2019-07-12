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

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.TopLevelClass;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2018/10/31 17:27
 * ---------------------------------------------------------------------------
 */
public interface ILombokPluginHook {
    /**
     * Model builder class 生成
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    boolean modelBaseRecordBuilderClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable);

    /**
     * Model builder class 生成
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    boolean modelPrimaryKeyBuilderClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable);

    /**
     * Model builder class 生成
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    boolean modelRecordWithBLOBsBuilderClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable);
}
