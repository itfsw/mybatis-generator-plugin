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

import java.util.Map;

public class MapperAnnotationPlugin extends BasePlugin {
    /**
     * This method is called when the entire client has been generated.
     * Implement this method to add additional methods or fields to a generated
     * client interface or implementation.
     *
     * @param interfaze         the generated interface if any, may be null
     * @param introspectedTable The class containing information about the table as
     *                          introspected from the database
     * @return true if the interface should be generated, false if the generated
     * interface should be ignored. In the case of multiple plugins, the
     * first plugin returning false will disable the calling of further
     * plugins.
     */
    @Override
    public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
        for (Map.Entry<Object, Object> prop : properties.entrySet()) {
            String annotationName = prop.getKey().toString().trim();
            String annotationImport = prop.getValue().toString().trim();
            interfaze.addImportedType(new FullyQualifiedJavaType(annotationImport));
            interfaze.addAnnotation(annotationName);
        }
        return true;
    }
}
