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

package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

/**
 * ModelAnnotationPlugin
 */
public class ModelAnnotationPlugin extends BasePlugin {
    /**
     * <a href="http://www.mybatis.org/generator/reference/pluggingIn.html">具体执行顺序</a>
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addAnnotations(topLevelClass, introspectedTable);
        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * <a href="http://www.mybatis.org/generator/reference/pluggingIn.html">具体执行顺序</a>
     */
    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addAnnotations(topLevelClass, introspectedTable);
        return super.modelPrimaryKeyClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * <a href="http://www.mybatis.org/generator/reference/pluggingIn.html">具体执行顺序</a>
     */
    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addAnnotations(topLevelClass, introspectedTable);
        return super.modelRecordWithBLOBsClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * <a href="http://www.mybatis.org/generator/reference/pluggingIn.html">具体执行顺序</a>
     */
    @Override
    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        for (Object key : properties.keySet()) {
            if (key instanceof String) {
                String annotation = (String) key;
                if (annotation.startsWith("@Data") || annotation.startsWith("@Getter")) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * <a href="http://www.mybatis.org/generator/reference/pluggingIn.html">具体执行顺序</a>
     */
    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        for (Object key : properties.keySet()) {
            if (key instanceof String) {
                String annotation = (String) key;
                if (annotation.startsWith("@Data") || annotation.startsWith("@Setter")) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 添加注解
     */
    private void addAnnotations(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (Object key : properties.keySet()) {
            if (key instanceof String) {
                String annotation = (String) key;
                String pkg = properties.getProperty(annotation);
                // @Data
                if (annotation.startsWith("@Data")) {
                    this.addAnnotation(topLevelClass, annotation, pkg);
                    if (topLevelClass.getSuperClass().isPresent()) {
                        this.addAnnotation(topLevelClass, "@EqualsAndHashCode(callSuper = true)", "lombok.EqualsAndHashCode");
                        this.addAnnotation(topLevelClass, "@ToString(callSuper = true)", "lombok.ToString");
                    }
                } else if (annotation.startsWith("@Builder")) {
                    // 有子类或者父类
                    int count = 0;
                    if (introspectedTable.getRules().generatePrimaryKeyClass()) {
                        count++;
                    }
                    if (introspectedTable.getRules().generateBaseRecordClass()) {
                        count++;
                    }
                    if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
                        count++;
                    }

                    if (topLevelClass.getSuperClass().isPresent() || count >= 2) {
                        this.addAnnotation(topLevelClass, "@SuperBuilder", "lombok.experimental.SuperBuilder");
                    } else {
                        this.addAnnotation(topLevelClass, annotation, pkg);
                    }
                } else {
                    this.addAnnotation(topLevelClass, annotation, pkg);
                }
            }
        }
    }

    /**
     * 添加注解
     */
    private void addAnnotation(TopLevelClass topLevelClass, String annotation, String pkg) {
        topLevelClass.addImportedType(pkg);
        topLevelClass.addAnnotation(annotation);
    }
}
