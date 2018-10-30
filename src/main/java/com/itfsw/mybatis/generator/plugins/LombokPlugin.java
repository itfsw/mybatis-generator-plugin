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
import org.mybatis.generator.internal.util.StringUtility;

import java.util.List;
import java.util.Properties;

/**
 * ---------------------------------------------------------------------------
 * LombokPlugin
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2018/10/29 14:33
 * ---------------------------------------------------------------------------
 */
public class LombokPlugin extends BasePlugin {
    public static final String PRO_BUILDER = "@Builder";  // 是否支持 Builder 注解
    public static final String PRO_ALL_ARGS_CONSTRUCTOR = "@AllArgsConstructor"; // 是否支持 AllArgsConstructor 注解
    public static final String PRO_NO_ARGS_CONSTRUCTOR = "@NoArgsConstructor"; // 是否支持 NoArgsConstructor 注解

    private boolean hasBuilder;
    private boolean hasAllArgsConstructor;
    private boolean hasNoArgsConstructor;

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param warnings
     * @return
     */
    @Override
    public boolean validate(List<String> warnings) {
        Properties properties = this.getProperties();

        String builder = properties.getProperty(PRO_BUILDER);
        String allArgsConstructor = properties.getProperty(PRO_ALL_ARGS_CONSTRUCTOR);
        String noArgsConstructor = properties.getProperty(PRO_NO_ARGS_CONSTRUCTOR);

        this.hasBuilder = builder == null ? false : StringUtility.isTrue(builder);
        this.hasAllArgsConstructor = allArgsConstructor == null ? false : StringUtility.isTrue(allArgsConstructor);
        this.hasNoArgsConstructor = noArgsConstructor == null ? false : StringUtility.isTrue(noArgsConstructor);

        return super.validate(warnings);
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // @Data
        this.addAnnotations(topLevelClass, EnumLombokAnnotations.DATA);
        if (topLevelClass.getSuperClass() != null) {
            this.addAnnotations(topLevelClass, EnumLombokAnnotations.EQUALS_AND_HASH_CODE_CALL_SUPER);
            this.addAnnotations(topLevelClass, EnumLombokAnnotations.TO_STRING_CALL_SUPER);
        }

        // @Builder
        if (this.hasBuilder) {
            // 有子类或者父类
            if (introspectedTable.getRules().generateRecordWithBLOBsClass() || introspectedTable.getRules().generatePrimaryKeyClass() || topLevelClass.getSuperClass() != null) {
                this.addAnnotations(topLevelClass, EnumLombokAnnotations.SUPER_BUILDER);
            } else {
                this.addAnnotations(topLevelClass, EnumLombokAnnotations.BUILDER);
            }
        }

        // @Constructor
        if (this.hasNoArgsConstructor) {
            this.addAnnotations(topLevelClass, EnumLombokAnnotations.NO_ARGS_CONSTRUCTOR);
        }
        if (this.hasAllArgsConstructor) {
            this.addAnnotations(topLevelClass, EnumLombokAnnotations.ALL_ARGS_CONSTRUCTOR);
        }
        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // @Data
        this.addAnnotations(topLevelClass, EnumLombokAnnotations.DATA);
        if (topLevelClass.getSuperClass() != null) {
            this.addAnnotations(topLevelClass, EnumLombokAnnotations.EQUALS_AND_HASH_CODE_CALL_SUPER);
            this.addAnnotations(topLevelClass, EnumLombokAnnotations.TO_STRING_CALL_SUPER);
        }

        // @Builder
        if (this.hasBuilder) {
            // 有子类或者父类
            if (introspectedTable.getRules().generateRecordWithBLOBsClass() || introspectedTable.getRules().generateBaseRecordClass() || topLevelClass.getSuperClass() != null) {
                this.addAnnotations(topLevelClass, EnumLombokAnnotations.SUPER_BUILDER);
            } else {
                this.addAnnotations(topLevelClass, EnumLombokAnnotations.BUILDER);
            }
        }

        // @Constructor
        if (this.hasNoArgsConstructor) {
            this.addAnnotations(topLevelClass, EnumLombokAnnotations.NO_ARGS_CONSTRUCTOR);
        }
        if (this.hasAllArgsConstructor) {
            this.addAnnotations(topLevelClass, EnumLombokAnnotations.ALL_ARGS_CONSTRUCTOR);
        }
        return super.modelPrimaryKeyClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // @Data
        this.addAnnotations(topLevelClass, EnumLombokAnnotations.DATA);
        if (topLevelClass.getSuperClass() != null) {
            this.addAnnotations(topLevelClass, EnumLombokAnnotations.EQUALS_AND_HASH_CODE_CALL_SUPER);
            this.addAnnotations(topLevelClass, EnumLombokAnnotations.TO_STRING_CALL_SUPER);
        }

        // @Builder
        if (this.hasBuilder) {
            // 有子类或者父类
            if (introspectedTable.getRules().generateBaseRecordClass() || introspectedTable.getRules().generatePrimaryKeyClass() || topLevelClass.getSuperClass() != null) {
                this.addAnnotations(topLevelClass, EnumLombokAnnotations.SUPER_BUILDER);
            } else {
                this.addAnnotations(topLevelClass, EnumLombokAnnotations.BUILDER);
            }
        }

        // @Constructor
        if (this.hasNoArgsConstructor) {
            this.addAnnotations(topLevelClass, EnumLombokAnnotations.NO_ARGS_CONSTRUCTOR);
        }
        if (this.hasAllArgsConstructor) {
            this.addAnnotations(topLevelClass, EnumLombokAnnotations.ALL_ARGS_CONSTRUCTOR);
        }
        return super.modelRecordWithBLOBsClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param method
     * @param topLevelClass
     * @param introspectedColumn
     * @param introspectedTable
     * @param modelClassType
     * @return
     */
    @Override
    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return false;
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param method
     * @param topLevelClass
     * @param introspectedColumn
     * @param introspectedTable
     * @param modelClassType
     * @return
     */
    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return false;
    }

    /**
     * 添加注解
     * @param topLevelClass
     * @param annotations
     */
    private void addAnnotations(TopLevelClass topLevelClass, EnumLombokAnnotations annotations) {
        topLevelClass.addImportedType(annotations.getClazz());
        topLevelClass.addAnnotation(annotations.getAnnotation());
    }

    /**
     * lombok 类型
     */
    public enum EnumLombokAnnotations {
        DATA("@Data", "lombok.Data"),
        BUILDER("@Builder", "lombok.Builder"),
        SUPER_BUILDER("@SuperBuilder", "lombok.experimental.SuperBuilder"),
        ALL_ARGS_CONSTRUCTOR("@AllArgsConstructor", "lombok.AllArgsConstructor"),
        NO_ARGS_CONSTRUCTOR("@NoArgsConstructor", "lombok.NoArgsConstructor"),
        EQUALS_AND_HASH_CODE_CALL_SUPER("@EqualsAndHashCode(callSuper = true)", "lombok.EqualsAndHashCode"),
        TO_STRING_CALL_SUPER("@ToString(callSuper = true)", "lombok.ToString");

        private final String annotation;
        private final String clazz;

        EnumLombokAnnotations(String annotation, String clazz) {
            this.annotation = annotation;
            this.clazz = clazz;
        }

        /**
         * Getter method for property <tt>annotation</tt>.
         * @return property value of annotation
         * @author hewei
         */
        public String getAnnotation() {
            return annotation;
        }

        /**
         * Getter method for property <tt>clazz</tt>.
         * @return property value of clazz
         * @author hewei
         */
        public String getClazz() {
            return clazz;
        }
    }
}
