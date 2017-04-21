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

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;

/**
 * ---------------------------------------------------------------------------
 * Java ele 生成工具
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/4/21 16:22
 * ---------------------------------------------------------------------------
 */
public class JavaElementGeneratorTools {
    public static Field generateStaticFinalField(String fieldName, FullyQualifiedJavaType javaType, String initString, IntrospectedTable introspectedTable){
        Field field = new Field(fieldName, javaType);
        CommentTools.addFieldComment(field, introspectedTable);
        field.setVisibility(JavaVisibility.PUBLIC);
        field.setStatic(true);
        field.setFinal(true);
        if (initString != null){
            field.setInitializationString(initString);
        }
        return field;
    }
}
