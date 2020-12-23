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

package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.Map;
import java.util.Properties;

/**
 * ---------------------------------------------------------------------------
 * <p>
 * ---------------------------------------------------------------------------
 *
 * @author: hewei
 * @time:2019/7/9 14:30
 * ---------------------------------------------------------------------------
 */
public class MapperAnnotationPlugin extends BasePlugin {
    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param interfaze
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        Properties properties = getProperties();
        // 和官方插件一致支持，没有配置特殊注解时默认开启@Mapper
        if ("true".equalsIgnoreCase(properties.getProperty("@Mapper", "true")) && introspectedTable.getTargetRuntime() == IntrospectedTable.TargetRuntime.MYBATIS3) {
            // don't need to do this for MYBATIS3_DSQL as that runtime already adds this annotation
            interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Mapper"));
            interfaze.addAnnotation("@Mapper");
        }

        for (Map.Entry<Object, Object> prop : properties.entrySet()) {
            String annotationName = prop.getKey().toString().trim();
            String annotationImport = prop.getValue().toString().trim();
            // TODO 兼容老版本
            if ("@Repository".equals(annotationName) && ("true".equalsIgnoreCase(annotationImport) || "false".equalsIgnoreCase(annotationImport))) {
                if (StringUtility.isTrue(annotationImport)) {
                    interfaze.addImportedType(new FullyQualifiedJavaType("org.springframework.stereotype.Repository"));
                    interfaze.addAnnotation("@Repository");
                }
            } else if (!"@Mapper".equals(annotationName)) {
                interfaze.addImportedType(new FullyQualifiedJavaType(annotationImport));
                interfaze.addAnnotation(annotationName);
            }
        }

        return true;
    }

}
