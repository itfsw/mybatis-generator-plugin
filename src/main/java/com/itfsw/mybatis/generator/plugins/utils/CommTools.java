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

package com.itfsw.mybatis.generator.plugins.utils;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.GeneratedKey;

/**
 * ---------------------------------------------------------------------------
 * 一些通用工具集
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/3/22 14:03
 * ---------------------------------------------------------------------------
 */
public class CommTools {

    /**
     * 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
     *
     * @param element
     * @param introspectedTable
     */
    public static void useGeneratedKeys(XmlElement element, IntrospectedTable introspectedTable){
        useGeneratedKeys(element, introspectedTable, null);
    }

    /**
     * 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
     *
     * @param element
     * @param introspectedTable
     * @param prefix
     */
    public static void useGeneratedKeys(XmlElement element, IntrospectedTable introspectedTable, String prefix){
        GeneratedKey gk = introspectedTable.getGeneratedKey();
        if (gk != null) {
            IntrospectedColumn introspectedColumn = introspectedTable.getColumn(gk.getColumn());
            // if the column is null, then it's a configuration error. The
            // warning has already been reported
            if (introspectedColumn != null) {
                // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
                element.addAttribute(new Attribute("useGeneratedKeys", "true")); //$NON-NLS-1$ //$NON-NLS-2$
                element.addAttribute(new Attribute("keyProperty", (prefix == null ? "" : prefix) + introspectedColumn.getJavaProperty())); //$NON-NLS-1$
                element.addAttribute(new Attribute("keyColumn", introspectedColumn.getActualColumnName())); //$NON-NLS-1$
            }
        }
    }
}
