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
import com.itfsw.mybatis.generator.plugins.utils.EnumModelType;
import com.itfsw.mybatis.generator.plugins.utils.PluginTools;
import com.itfsw.mybatis.generator.plugins.utils.hook.ILombokPluginHook;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.PluginConfiguration;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ---------------------------------------------------------------------------
 * LombokPlugin
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2018/10/29 14:33
 * ---------------------------------------------------------------------------
 */
public class LombokPlugin extends BasePlugin {
    /**
     * 开启IDEA（老版本） SuperBuilder 支持
     */
    public final static String PRO_SUPPORT_SUPER_BUILDER_FOR_IDEA = "supportSuperBuilderForIdea";

    private final static List<String> LOMBOK_FEATURES;
    private final static List<String> LOMBOK_EXPERIMENTAL_FEATURES;
    private final static Pattern LOMBOK_ANNOTATION = Pattern.compile("@([a-zA-z]+)(\\(.*\\))?");

    static {
        LOMBOK_FEATURES = Arrays.asList(
                "Getter", "Setter", "ToString", "EqualsAndHashCode", "NoArgsConstructor",
                "RequiredArgsConstructor", "AllArgsConstructor", "Data", "Value", "Builder", "Log"
        );
        LOMBOK_EXPERIMENTAL_FEATURES = Arrays.asList(
                "Accessors", "FieldDefaults", "Wither", "UtilityClass", "Helper", "FieldNameConstants", "SuperBuilder"
        );
    }

    private List<String> annotations;
    private boolean suportSuperBuilderForIdea;

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param warnings
     * @return
     */
    @Override
    public boolean validate(List<String> warnings) {
        Properties properties = this.getProperties();
        for (Object key : properties.keySet()) {
            String annotation = key.toString().trim();
            if (!(annotation.matches(LOMBOK_ANNOTATION.pattern()) || PRO_SUPPORT_SUPER_BUILDER_FOR_IDEA.equals(annotation))) {
                warnings.add("itfsw:插件" + LombokPlugin.class.getTypeName() + "不能识别的注解（" + annotation + "）！");
                return false;
            }
        }

        return super.validate(warnings);
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param introspectedTable
     * @return
     */
    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        super.initialized(introspectedTable);

        this.annotations = new ArrayList<>();
        Properties properties = this.getProperties();
        boolean findData = false;
        for (Object key : properties.keySet()) {
            String annotation = key.toString().trim();
            if (annotation.startsWith("@Data")) {
                findData = true;
            }

            if (StringUtility.isTrue(properties.getProperty(key.toString())) && !PRO_SUPPORT_SUPER_BUILDER_FOR_IDEA.equals(annotation)) {
                this.annotations.add(annotation);
            }
        }

        if (!findData) {
            this.annotations.add(0, "@Data");
        }

        // 老版本IDEA SuperBuilder支持
        this.suportSuperBuilderForIdea = StringUtility.isTrue(properties.getProperty(PRO_SUPPORT_SUPER_BUILDER_FOR_IDEA));
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addAnnotations(topLevelClass, introspectedTable, EnumModelType.MODEL_BASE_RECORD);
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
        this.addAnnotations(topLevelClass, introspectedTable, EnumModelType.MODEL_PRIMARY_KEY);
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
        this.addAnnotations(topLevelClass, introspectedTable, EnumModelType.MODEL_RECORD_WITH_BLOBS);
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
        for (String annotation : this.annotations) {
            if (annotation.startsWith("@Data") || annotation.startsWith("@Getter")) {
                return false;
            }
        }
        return true;
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
        for (String annotation : this.annotations) {
            if (annotation.startsWith("@Data") || annotation.startsWith("@Setter")) {
                return false;
            }
        }
        return true;
    }

    /**
     * 添加注解
     * @param topLevelClass
     * @param introspectedTable
     * @param modelType
     */
    private void addAnnotations(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, EnumModelType modelType) {
        for (String annotation : this.annotations) {
            // @Data
            if (annotation.startsWith("@Data")) {
                this.addAnnotation(topLevelClass, annotation);
                if (topLevelClass.getSuperClass() != null) {
                    this.addAnnotation(topLevelClass, "@EqualsAndHashCode(callSuper = true)");
                    this.addAnnotation(topLevelClass, "@ToString(callSuper = true)");
                }
            } else if (annotation.startsWith("@Builder")) {
                // TODO 配合IncrementsPlugin,以后删除
                boolean checkIncrementsPlugin = true;
                if (!this.suportSuperBuilderForIdea) {
                    switch (modelType) {
                        case MODEL_PRIMARY_KEY:
                            checkIncrementsPlugin = PluginTools.getHook(ILombokPluginHook.class).modelPrimaryKeyBuilderClassGenerated(topLevelClass, introspectedTable);
                            break;
                        case MODEL_BASE_RECORD:
                            checkIncrementsPlugin = PluginTools.getHook(ILombokPluginHook.class).modelBaseRecordBuilderClassGenerated(topLevelClass, introspectedTable);
                            break;
                        case MODEL_RECORD_WITH_BLOBS:
                            checkIncrementsPlugin = PluginTools.getHook(ILombokPluginHook.class).modelRecordWithBLOBsBuilderClassGenerated(topLevelClass, introspectedTable);
                            break;
                    }
                }

                if (checkIncrementsPlugin) {
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

                    if (topLevelClass.getSuperClass() != null || count >= 2) {
                        if (this.suportSuperBuilderForIdea) {
                            // TODO 兼容老版本
                            PluginConfiguration configuration = new PluginConfiguration();
                            configuration.setConfigurationType(ModelBuilderPlugin.class.getTypeName());
                            ModelBuilderPlugin modelBuilderPlugin = (ModelBuilderPlugin) ObjectFactory.createPlugin(this.context, configuration);
                            switch (modelType) {
                                case MODEL_PRIMARY_KEY:
                                    modelBuilderPlugin.modelPrimaryKeyBuilderClassGenerated(topLevelClass, introspectedTable);
                                    break;
                                case MODEL_BASE_RECORD:
                                    modelBuilderPlugin.modelBaseRecordBuilderClassGenerated(topLevelClass, introspectedTable);
                                    break;
                                case MODEL_RECORD_WITH_BLOBS:
                                    modelBuilderPlugin.modelRecordWithBLOBsBuilderClassGenerated(topLevelClass, introspectedTable);
                                    break;
                            }
                        } else {
                            this.addAnnotation(topLevelClass, "@SuperBuilder");
                        }
                    } else {
                        this.addAnnotation(topLevelClass, annotation);
                    }
                }
            } else {
                this.addAnnotation(topLevelClass, annotation);
            }
        }
    }

    /**
     * 添加注解
     * @param topLevelClass
     * @param annotation
     */
    private void addAnnotation(TopLevelClass topLevelClass, String annotation) {
        // 正则提取annotation
        Matcher matcher = LOMBOK_ANNOTATION.matcher(annotation);
        if (matcher.find()) {
            String annotationName = matcher.group(1);
            if (LOMBOK_FEATURES.contains(annotationName)) {
                topLevelClass.addImportedType("lombok." + annotationName);
            } else if (LOMBOK_EXPERIMENTAL_FEATURES.contains(annotationName)) {
                topLevelClass.addImportedType("lombok.experimental." + annotationName);
            } else {
                this.warnings.add("itfsw:插件" + LombokPlugin.class.getTypeName() + "没有找到注解（" + annotation + "）！");
                return;
            }
            topLevelClass.addAnnotation(annotation);
        }
    }
}
