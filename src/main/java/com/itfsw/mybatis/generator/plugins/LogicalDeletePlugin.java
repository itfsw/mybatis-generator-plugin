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

package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.*;
import com.itfsw.mybatis.generator.plugins.utils.hook.ILogicalDeletePluginHook;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.*;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * 逻辑删除插件
 */
public class LogicalDeletePlugin extends BasePlugin {
    public static final String METHOD_LOGICAL_DELETE_BY_EXAMPLE = "logicalDeleteByExample";
    public static final String METHOD_LOGICAL_DELETE_BY_PRIMARY_KEY = "logicalDeleteByPrimaryKey";

    public static final String PRO_LOGICAL_DELETE_COLUMN = "logicalDeleteColumn";
    public static final String PRO_LOGICAL_DELETE_VALUE = "logicalDeleteValue";
    public static final String PRO_LOGICAL_UN_DELETE_VALUE = "logicalUnDeleteValue";

    /**
     * 是否支持不推荐的常量配置方式
     */
    public static final String PRO_ENABLE_LOGICAL_DELETE_CONST = "enableLogicalDeleteConst";
    /**
     * 逻辑删除常量
     */
    public static final String PRO_LOGICAL_DELETE_CONST_NAME = "logicalDeleteConstName";
    public static final String PRO_LOGICAL_UN_DELETE_CONST_NAME = "logicalUnDeleteConstName";

    /**
     * 逻辑删除标志位名称(默认)
     */
    public static final String DEFAULT_LOGICAL_DELETE_NAME = "IS_DELETED";
    public static final String DEFAULT_LOGICAL_UN_DELETE_NAME = "NOT_DELETED";

    /**
     * 逻辑删除查询方法
     */
    public static final String METHOD_LOGICAL_DELETED = "andLogicalDeleted";
    /**
     * 增强selectByPrimaryKey是参数名称
     */
    public static final String PARAMETER_LOGICAL_DELETED = METHOD_LOGICAL_DELETED;
    /**
     * selectByPrimaryKey 的逻辑删除增强
     */
    public static final String METHOD_SELECT_BY_PRIMARY_KEY_WITH_LOGICAL_DELETE = "selectByPrimaryKeyWithLogicalDelete";

    private final Map<IntrospectedTable, TableConfig> configs = new HashMap<>();

    /**
     * 初始化阶段
     * <a href="http://www.mybatis.org/generator/reference/pluggingIn.html">具体执行顺序</a>
     */
    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        super.initialized(introspectedTable);
        TableConfig config = new TableConfig();

        config.logicalUnDeleteEnumIndex = 0;
        config.logicalDeleteEnumIndex = 1;

        // 1. 获取配置的逻辑删除列
        String logicalDeleteColumn = properties.getProperty(PRO_LOGICAL_DELETE_COLUMN);
        if (introspectedTable.getTableConfigurationProperty(PRO_LOGICAL_DELETE_COLUMN) != null) {
            logicalDeleteColumn = introspectedTable.getTableConfigurationProperty(PRO_LOGICAL_DELETE_COLUMN);
        }
        config.logicalDeleteColumn = IntrospectedTableTools.safeGetColumn(introspectedTable, logicalDeleteColumn);
        // 判断如果表单独配置了逻辑删除列，但是却没有找到对应列进行提示
        if (introspectedTable.getTableConfigurationProperty(PRO_LOGICAL_DELETE_COLUMN) != null && config.logicalDeleteColumn == null) {
            warnings.add("itfsw(逻辑删除插件):" + introspectedTable.getFullyQualifiedTable() + "没有找到您配置的逻辑删除列(" + introspectedTable.getTableConfigurationProperty(PRO_LOGICAL_DELETE_COLUMN) + ")！");
        }

        // 2. 获取逻辑删除常量值
        config.enableLogicalDeleteConst = properties.getProperty(PRO_ENABLE_LOGICAL_DELETE_CONST) == null || StringUtility.isTrue(properties.getProperty(PRO_ENABLE_LOGICAL_DELETE_CONST));
        if (config.enableLogicalDeleteConst) {
            if (properties.getProperty(PRO_LOGICAL_DELETE_CONST_NAME) != null) {
                config.logicalDeleteConstName = properties.getProperty(PRO_LOGICAL_DELETE_CONST_NAME).toUpperCase();
            }
            if (properties.getProperty(PRO_LOGICAL_UN_DELETE_CONST_NAME) != null) {
                config.logicalUnDeleteConstName = properties.getProperty(PRO_LOGICAL_UN_DELETE_CONST_NAME).toUpperCase();
            }
        }

        // 3.判断逻辑删除值是否配置了
        config.logicalDeleteValue = properties.getProperty(PRO_LOGICAL_DELETE_VALUE);
        config.logicalUnDeleteValue = properties.getProperty(PRO_LOGICAL_UN_DELETE_VALUE);
        if (introspectedTable.getTableConfigurationProperty(PRO_LOGICAL_DELETE_VALUE) != null) {
            config.logicalDeleteValue = introspectedTable.getTableConfigurationProperty(PRO_LOGICAL_DELETE_VALUE);
        }
        if (introspectedTable.getTableConfigurationProperty(PRO_LOGICAL_UN_DELETE_VALUE) != null) {
            config.logicalUnDeleteValue = introspectedTable.getTableConfigurationProperty(PRO_LOGICAL_UN_DELETE_VALUE);
        }
        if (config.logicalDeleteValue == null || config.logicalUnDeleteValue == null) {
            config.logicalDeleteColumn = null;
            warnings.add("itfsw(逻辑删除插件):" + introspectedTable.getFullyQualifiedTable() + "没有找到您配置的逻辑删除值，请全局或者局部配置logicalDeleteValue和logicalUnDeleteValue值！");
        }

        // 4. 优先借助 EnumTypeStatusPlugin 插件，去注解里面解析枚举
        if (config.logicalDeleteColumn != null) {
            EnumTypeStatusPlugin.EnumInfo enumInfo = null;
            try {
                enumInfo = new EnumTypeStatusPlugin.EnumInfo(config.logicalDeleteColumn);
                // 解析注释
                enumInfo.parseRemarks(config.logicalDeleteColumn.getRemarks());
            } catch (EnumTypeStatusPlugin.EnumInfo.NotSupportTypeException e) {
                config.logicalDeleteColumn = null;
                warnings.add("itfsw(逻辑删除插件):" + introspectedTable.getFullyQualifiedTable() + "逻辑删除列(" + introspectedTable.getTableConfigurationProperty(PRO_LOGICAL_DELETE_COLUMN) + ")的类型不在支持范围（请使用数字列，字符串列，布尔列）！");
            } catch (EnumTypeStatusPlugin.EnumInfo.CannotParseException e) {
                // 这个异常不管，没有配置是正常的
            } finally {

                if (enumInfo != null) {
                    // 这个是注释里配置了枚举
                    if (enumInfo.hasItems()) {
                        if (enumInfo.getItems().size() < 2) {
                            config.logicalDeleteColumn = null;
                            warnings.add("itfsw(逻辑删除插件):" + introspectedTable.getFullyQualifiedTable() + "在配合EnumTypeStatusPlugin插件使用时，枚举数量必须大于等于2！");
                        } else {
                            config.logicalDeleteEnumIndex = -1;
                            config.logicalUnDeleteEnumIndex = -1;
                            for (int i = 0; i < enumInfo.getItems().size(); i++) {
                                EnumTypeStatusPlugin.EnumInfo.EnumItemInfo enumItemInfo = enumInfo.getItems().get(i);
                                if (config.logicalDeleteValue.equals(enumItemInfo.getOriginalValue())) {
                                    config.logicalDeleteEnumIndex = i;
                                } else if (config.logicalUnDeleteValue.equals(enumItemInfo.getOriginalValue())) {
                                    config.logicalUnDeleteEnumIndex = i;
                                }
                            }
                            if (config.logicalDeleteEnumIndex == -1) {
                                config.logicalDeleteColumn = null;
                                warnings.add("itfsw(逻辑删除插件):" + introspectedTable.getFullyQualifiedTable() + "在配合EnumTypeStatusPlugin插件使用时，逻辑删除值“" + config.logicalDeleteValue + "”未在枚举中找到！");
                            } else if (config.logicalDeleteEnumIndex == -1) {
                                config.logicalDeleteColumn = null;
                                warnings.add("itfsw(逻辑删除插件):" + introspectedTable.getFullyQualifiedTable() + "在配合EnumTypeStatusPlugin插件使用时，逻辑未删除值“" + config.logicalUnDeleteValue + "”未在枚举中找到！");
                            } else {
                                config.logicalDeleteEnum = enumInfo.generateEnum(commentGenerator, introspectedTable);
                            }

                        }
                    } else {
                        // 兼容处理以前一些老用户配置的Long 和 Float配置问题
                        if (config.logicalDeleteColumn.getFullyQualifiedJavaType().getFullyQualifiedName().equals(Long.class.getName())) {
                            config.logicalUnDeleteValue = config.logicalUnDeleteValue.replaceAll("[Ll]", "");
                            config.logicalDeleteValue = config.logicalDeleteValue.replaceAll("[Ll]", "");
                        } else if (config.logicalDeleteColumn.getFullyQualifiedJavaType().getFullyQualifiedName().equals(Float.class.getName())) {
                            config.logicalUnDeleteValue = config.logicalUnDeleteValue.replaceAll("[Ff]", "");
                            config.logicalDeleteValue = config.logicalDeleteValue.replaceAll("[Ff]", "");
                        }

                        enumInfo.addItem(config.logicalUnDeleteConstName, "未删除", config.logicalUnDeleteValue);
                        enumInfo.addItem(config.logicalDeleteConstName, "已删除", config.logicalDeleteValue);
                        config.logicalDeleteEnum = enumInfo.generateEnum(commentGenerator, introspectedTable);
                    }
                }
            }
        }

        // 5. 防止增强的selectByPrimaryKey中逻辑删除键冲突
        if (config.logicalDeleteColumn != null) {
            Field logicalDeleteField = JavaBeansUtil.getJavaBeansField(config.logicalDeleteColumn, context, introspectedTable);
            if (logicalDeleteField.getName().equals(PARAMETER_LOGICAL_DELETED)) {
                config.logicalDeleteColumn = null;
                warnings.add("itfsw(逻辑删除插件):" + introspectedTable.getFullyQualifiedTable() + "配置的逻辑删除列和插件保留关键字(" + PARAMETER_LOGICAL_DELETED + ")冲突！");
            }
        }

        configs.put(introspectedTable, config);
    }

    /**
     * Java Client Methods 生成
     * <a href="http://www.mybatis.org/generator/reference/pluggingIn.html">具体执行顺序</a>
     */
    @Override
    public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
        TableConfig config = configs.get(introspectedTable);
        if (config.logicalDeleteColumn != null) {
            // 1. 逻辑删除ByExample
            Method mLogicalDeleteByExample = JavaElementGeneratorTools.generateAbstractMethod(
                    METHOD_LOGICAL_DELETE_BY_EXAMPLE,
                    JavaVisibility.DEFAULT,
                    FullyQualifiedJavaType.getIntInstance(),
                    new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example", "@Param(\"example\")")
            );
            // 添加方法说明
            commentGenerator.addGeneralMethodComment(mLogicalDeleteByExample, introspectedTable);
            // interface 增加方法
            FormatTools.addMethodWithBestPosition(interfaze, mLogicalDeleteByExample);
            logger.debug("itfsw(逻辑删除插件):" + interfaze.getType().getShortName() + "增加方法logicalDeleteByExample。");
            // hook
            PluginTools.getHook(ILogicalDeletePluginHook.class).clientLogicalDeleteByExampleMethodGenerated(mLogicalDeleteByExample, interfaze, introspectedTable);

            // 2. 判断是否有主键，生成主键删除方法
            if (introspectedTable.hasPrimaryKeyColumns()) {
                // 2.1. 逻辑删除ByExample
                Method mLogicalDeleteByPrimaryKey = JavaElementGeneratorTools.generateAbstractMethod(
                        METHOD_LOGICAL_DELETE_BY_PRIMARY_KEY,
                        JavaVisibility.DEFAULT,
                        FullyQualifiedJavaType.getIntInstance()
                );
                commentGenerator.addGeneralMethodComment(mLogicalDeleteByPrimaryKey, introspectedTable);

                // 2.2 增强selectByPrimaryKey
                Method mSelectByPrimaryKey = JavaElementGeneratorTools.generateAbstractMethod(
                        METHOD_SELECT_BY_PRIMARY_KEY_WITH_LOGICAL_DELETE,
                        JavaVisibility.DEFAULT,
                        introspectedTable.getRules().calculateAllFieldsClass()
                );

                // 添加参数
                Set<FullyQualifiedJavaType> importedTypes = new TreeSet<>();
                importedTypes.add(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));

                if (introspectedTable.getRules().generatePrimaryKeyClass()) {
                    FullyQualifiedJavaType type1 = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
                    importedTypes.add(type1);
                    mLogicalDeleteByPrimaryKey.addParameter(new Parameter(type1, "key"));
                    mSelectByPrimaryKey.addParameter(new Parameter(type1, "key", "@Param(\"key\")"));
                } else {
                    // no primary key class - fields are in the base class
                    // if more than one PK field, then we need to annotate the
                    // parameters
                    // for MyBatis
                    List<IntrospectedColumn> introspectedColumns = introspectedTable.getPrimaryKeyColumns();
                    boolean annotate = introspectedColumns.size() > 1;
                    StringBuilder sb = new StringBuilder();
                    for (IntrospectedColumn introspectedColumn : introspectedColumns) {
                        FullyQualifiedJavaType type1 = introspectedColumn.getFullyQualifiedJavaType();
                        importedTypes.add(type1);
                        Parameter parameter = new Parameter(type1, introspectedColumn.getJavaProperty());
                        if (annotate) {
                            sb.setLength(0);
                            sb.append("@Param(\"");
                            sb.append(introspectedColumn.getJavaProperty());
                            sb.append("\")");
                            parameter.addAnnotation(sb.toString());
                        }
                        mLogicalDeleteByPrimaryKey.addParameter(parameter);


                        Parameter parameter1 = new Parameter(type1, introspectedColumn.getJavaProperty());
                        sb.setLength(0);
                        sb.append("@Param(\"");
                        sb.append(introspectedColumn.getJavaProperty());
                        sb.append("\")");
                        parameter1.addAnnotation(sb.toString());

                        mSelectByPrimaryKey.addParameter(parameter1);
                    }
                }

                // interface 增加方法
                interfaze.addImportedTypes(importedTypes);
                FormatTools.addMethodWithBestPosition(interfaze, mLogicalDeleteByPrimaryKey);
                logger.debug("itfsw(逻辑删除插件):" + interfaze.getType().getShortName() + "增加方法logicalDeleteByPrimaryKey。");
                // hook
                PluginTools.getHook(ILogicalDeletePluginHook.class).clientLogicalDeleteByPrimaryKeyMethodGenerated(mLogicalDeleteByPrimaryKey, interfaze, introspectedTable);

                // 增强selectByPrimaryKey
                mSelectByPrimaryKey.addParameter(new Parameter(
                        FullyQualifiedJavaType.getBooleanPrimitiveInstance(),
                        PARAMETER_LOGICAL_DELETED,
                        "@Param(\"" + PARAMETER_LOGICAL_DELETED + "\")"
                ));
                commentGenerator.addGeneralMethodComment(mSelectByPrimaryKey, introspectedTable);
                FormatTools.addMethodWithBestPosition(interfaze, mSelectByPrimaryKey);
            }
        }
        return true;
    }

    /**
     * SQL Map Methods 生成
     * <a href="http://www.mybatis.org/generator/reference/pluggingIn.html">具体执行顺序</a>
     */
    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        TableConfig config = configs.get(introspectedTable);
        if (config.logicalDeleteColumn != null) {
            // 1. 逻辑删除ByExample
            XmlElement logicalDeleteByExample = new XmlElement("update");
            logicalDeleteByExample.addAttribute(new Attribute("id", METHOD_LOGICAL_DELETE_BY_EXAMPLE));
            logicalDeleteByExample.addAttribute(new Attribute("parameterType", "map"));
            commentGenerator.addComment(logicalDeleteByExample);

            StringBuilder sb = new StringBuilder();
            sb.append("update ");
            sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
            sb.append(" set ");
            // 更新逻辑删除字段
            sb.append(config.logicalDeleteColumn.getActualColumnName());
            sb.append(" = ");
            sb.append(XmlElementGeneratorTools.generateLogicalDeleteColumnValue(config.logicalDeleteColumn, config.logicalDeleteValue));

            logicalDeleteByExample.addElement(new TextElement(sb.toString()));

            logicalDeleteByExample.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));
            document.getRootElement().addElement(logicalDeleteByExample);
            logger.debug("itfsw(逻辑删除插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加方法logicalDeleteByExample的实现。");
            // hook
            PluginTools.getHook(ILogicalDeletePluginHook.class).sqlMapLogicalDeleteByExampleElementGenerated(document, logicalDeleteByExample, config.logicalDeleteColumn, config.logicalDeleteValue, introspectedTable);

            // 2. 判断是否有主键，生成主键删除方法
            if (introspectedTable.hasPrimaryKeyColumns()) {
                XmlElement logicalDeleteByPrimaryKey = new XmlElement("update");
                logicalDeleteByPrimaryKey.addAttribute(new Attribute("id", METHOD_LOGICAL_DELETE_BY_PRIMARY_KEY));

                String parameterClass;
                if (introspectedTable.getRules().generatePrimaryKeyClass()) {
                    parameterClass = introspectedTable.getPrimaryKeyType();
                } else {
                    // PK fields are in the base class. If more than on PK
                    // field, then they are coming in a map.
                    if (introspectedTable.getPrimaryKeyColumns().size() > 1) {
                        parameterClass = "map";
                    } else {
                        parameterClass = introspectedTable.getPrimaryKeyColumns().get(0).getFullyQualifiedJavaType().toString();
                    }
                }
                logicalDeleteByPrimaryKey.addAttribute(new Attribute("parameterType", parameterClass));
                commentGenerator.addComment(logicalDeleteByPrimaryKey);

                String sb1 = "update " +
                        introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime() +
                        " set " +
                        // 更新逻辑删除字段
                        config.logicalDeleteColumn.getActualColumnName() +
                        " = " +
                        XmlElementGeneratorTools.generateLogicalDeleteColumnValue(config.logicalDeleteColumn, config.logicalDeleteValue);

                logicalDeleteByPrimaryKey.addElement(new TextElement(sb1));

                XmlElementGeneratorTools.generateWhereByPrimaryKeyTo(logicalDeleteByPrimaryKey, introspectedTable.getPrimaryKeyColumns());

                document.getRootElement().addElement(logicalDeleteByPrimaryKey);
                logger.debug("itfsw(逻辑删除插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加方法logicalDeleteByPrimaryKey的实现。");
                // hook
                PluginTools.getHook(ILogicalDeletePluginHook.class).sqlMapLogicalDeleteByPrimaryKeyElementGenerated(document, logicalDeleteByPrimaryKey, config.logicalDeleteColumn, config.logicalDeleteValue, introspectedTable);


                // 3. 增强selectByPrimaryKey
                XmlElement selectByPrimaryKey = new XmlElement("select");
                commentGenerator.addComment(selectByPrimaryKey);

                selectByPrimaryKey.addAttribute(new Attribute("id", METHOD_SELECT_BY_PRIMARY_KEY_WITH_LOGICAL_DELETE));
                if (introspectedTable.getRules().generateResultMapWithBLOBs()) {
                    selectByPrimaryKey.addAttribute(new Attribute("resultMap", introspectedTable.getResultMapWithBLOBsId()));
                } else {
                    selectByPrimaryKey.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
                }

                selectByPrimaryKey.addAttribute(new Attribute("parameterType", "map"));

                sb = new StringBuilder();
                sb.append("select ");

                if (stringHasValue(introspectedTable.getSelectByPrimaryKeyQueryId())) {
                    sb.append('\'');
                    sb.append(introspectedTable.getSelectByPrimaryKeyQueryId());
                    sb.append("' as QUERYID,");
                }
                selectByPrimaryKey.addElement(new TextElement(sb.toString()));
                selectByPrimaryKey.addElement(XmlElementGeneratorTools.getBaseColumnListElement(introspectedTable));
                if (introspectedTable.hasBLOBColumns()) {
                    selectByPrimaryKey.addElement(new TextElement(","));
                    selectByPrimaryKey.addElement(XmlElementGeneratorTools.getBlobColumnListElement(introspectedTable));
                }

                sb.setLength(0);
                sb.append("from ");
                sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
                selectByPrimaryKey.addElement(new TextElement(sb.toString()));

                XmlElementGeneratorTools.generateWhereByPrimaryKeyTo(selectByPrimaryKey, introspectedTable.getPrimaryKeyColumns());

                // 逻辑删除的判断
                sb.setLength(0);
                sb.append("  and ");
                sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(config.logicalDeleteColumn));
                sb.append(" = ");
                selectByPrimaryKey.addElement(new TextElement(sb.toString()));

                XmlElement chooseEle = new XmlElement("choose");
                XmlElement whenEle = new XmlElement("when");
                whenEle.addAttribute(new Attribute("test", PARAMETER_LOGICAL_DELETED));
                whenEle.addElement(new TextElement(XmlElementGeneratorTools.generateLogicalDeleteColumnValue(config.logicalDeleteColumn, config.logicalDeleteValue)));
                chooseEle.addElement(whenEle);
                XmlElement otherwiseEle = new XmlElement("otherwise");
                otherwiseEle.addElement(new TextElement(XmlElementGeneratorTools.generateLogicalDeleteColumnValue(config.logicalDeleteColumn, config.logicalUnDeleteValue)));
                chooseEle.addElement(otherwiseEle);
                selectByPrimaryKey.addElement(chooseEle);

                FormatTools.addElementWithBestPosition(document.getRootElement(), selectByPrimaryKey);
            }
        }
        return true;
    }

    /**
     * <a href="http://www.mybatis.org/generator/reference/pluggingIn.html">具体执行顺序</a>
     */
    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        TableConfig config = configs.get(introspectedTable);
        if (config.logicalDeleteColumn != null) {
            // 常量、枚举和逻辑删除方法跟随 逻辑删除列走
            if (config.logicalDeleteColumn.getJavaProperty().equals(field.getName())) {
                // 1. 添加枚举
                if (!PluginTools.getHook(ILogicalDeletePluginHook.class).logicalDeleteEnumGenerated(introspectedTable, config.logicalDeleteColumn)) {
                    topLevelClass.addInnerEnum(config.logicalDeleteEnum);
                }
                // 2. andLogicalDeleted 方法
                Method mAndLogicalDeleted = JavaElementGeneratorTools.generateMethod(
                        METHOD_LOGICAL_DELETED,
                        JavaVisibility.PUBLIC,
                        null,
                        new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "deleted")
                );
                commentGenerator.addGeneralMethodComment(mAndLogicalDeleted, introspectedTable);
                Method logicalDeleteSetter = JavaBeansUtil.getJavaBeansSetter(config.logicalDeleteColumn, context, introspectedTable);
                mAndLogicalDeleted.addBodyLine(logicalDeleteSetter.getName() + "(deleted ? " + this.getEnumConstantValue(introspectedTable, true) + " : " + this.getEnumConstantValue(introspectedTable, false) + ");");
                FormatTools.addMethodWithBestPosition(topLevelClass, mAndLogicalDeleted);

                // 3. 添加逻辑删除常量
                if (config.enableLogicalDeleteConst) {
                    Field logicalUnDeleteConstField = JavaElementGeneratorTools.generateStaticFinalField(
                            config.logicalUnDeleteConstName,
                            config.logicalDeleteColumn.getFullyQualifiedJavaType(),
                            this.getEnumConstantValue(introspectedTable, false)
                    );
                    commentGenerator.addFieldComment(logicalUnDeleteConstField, introspectedTable);
                    Field logicalDeleteConstField = JavaElementGeneratorTools.generateStaticFinalField(
                            config.logicalDeleteConstName,
                            config.logicalDeleteColumn.getFullyQualifiedJavaType(),
                            this.getEnumConstantValue(introspectedTable, true)
                    );
                    commentGenerator.addFieldComment(logicalDeleteConstField, introspectedTable);

                    // 常量放在字段开头
                    ArrayList<Field> fields = (ArrayList<Field>) topLevelClass.getFields();
                    fields.add(0, logicalUnDeleteConstField);
                    fields.add(0, logicalDeleteConstField);
                }
            }
        }
        return super.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
    }

    /**
     * ModelExample Methods 生成
     * <a href="http://www.mybatis.org/generator/reference/pluggingIn.html">具体执行顺序</a>
     */
    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        TableConfig config = configs.get(introspectedTable);
        if (config.logicalDeleteColumn != null) {
            // 引入 Model类
            topLevelClass.addImportedType(this.getColumnInModelType(introspectedTable));

            List<InnerClass> innerClasses = topLevelClass.getInnerClasses();
            for (InnerClass innerClass : innerClasses) {
                if ("Criteria".equals(innerClass.getType().getShortName())) {
                    // 增加逻辑删除条件
                    Method method = new Method(METHOD_LOGICAL_DELETED);
                    commentGenerator.addGeneralMethodComment(method, introspectedTable);
                    method.setVisibility(JavaVisibility.PUBLIC);
                    method.setReturnType(innerClass.getType());
                    method.addParameter(new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "deleted"));

                    StringBuilder sb = new StringBuilder();
                    sb.append("return deleted ? ");

                    String modelName = Objects.requireNonNull(this.getColumnInModelType(introspectedTable)).getShortName();

                    // 调用EqualTo方法
                    StringBuilder equalToMethodName = new StringBuilder();
                    equalToMethodName.append(config.logicalDeleteColumn.getJavaProperty());
                    equalToMethodName.setCharAt(0, Character.toUpperCase(equalToMethodName.charAt(0)));
                    equalToMethodName.insert(0, "and");
                    equalToMethodName.append("EqualTo");
                    sb.append(equalToMethodName);
                    sb.append("(").append(modelName).append(".").append(this.getEnumConstantValue(introspectedTable, true)).append(")");

                    sb.append(" : ");

                    // 调用NotEqualTo 方法
                    StringBuilder notEqualToMethodName = new StringBuilder();
                    notEqualToMethodName.append(config.logicalDeleteColumn.getJavaProperty());
                    notEqualToMethodName.setCharAt(0, Character.toUpperCase(notEqualToMethodName.charAt(0)));
                    notEqualToMethodName.insert(0, "and");
                    notEqualToMethodName.append("NotEqualTo");
                    sb.append(notEqualToMethodName);
                    sb.append("(").append(modelName).append(".").append(this.getEnumConstantValue(introspectedTable, true)).append(")");

                    sb.append(";");

                    method.addBodyLine(sb.toString());

                    FormatTools.addMethodWithBestPosition(innerClass, method);
                }
            }
        }
        return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * 获取逻辑删除枚举
     */
    private String getEnumConstantValue(IntrospectedTable introspectedTable, boolean delete) {
        TableConfig config = configs.get(introspectedTable);
        if (config.logicalDeleteEnum != null) {
            String enumConstant = config.logicalDeleteEnum.getEnumConstants().get(delete ? config.logicalDeleteEnumIndex : config.logicalUnDeleteEnumIndex);
            enumConstant = enumConstant.split("\\(")[0];
            return config.logicalDeleteEnum.getType().getShortName() + "." + enumConstant + ".value()";
        }
        return null;
    }

    /**
     * 获取逻辑删除列所在model(modelExampleClassGenerated执行顺序在前面！！！！！和官网上不一样，没办法只有自己去找）
     */
    private FullyQualifiedJavaType getColumnInModelType(IntrospectedTable introspectedTable) {
        TableConfig config = configs.get(introspectedTable);
        if (config.logicalDeleteColumn != null) {
            // primaryKey
            for (IntrospectedColumn column : introspectedTable.getPrimaryKeyColumns()) {
                if (column.getActualColumnName().equals(config.logicalDeleteColumn.getActualColumnName())) {
                    if (introspectedTable.getRules().generatePrimaryKeyClass()) {
                        return new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
                    } else {
                        return new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
                    }
                }
            }

            // base record
            for (IntrospectedColumn column : introspectedTable.getBaseColumns()) {
                if (column.getActualColumnName().equals(config.logicalDeleteColumn.getActualColumnName())) {
                    if (introspectedTable.getRules().generateBaseRecordClass()) {
                        return new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
                    }
                }
            }

            // blob record
            for (IntrospectedColumn column : introspectedTable.getBLOBColumns()) {
                if (column.getActualColumnName().equals(config.logicalDeleteColumn.getActualColumnName())) {
                    if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
                        return new FullyQualifiedJavaType(introspectedTable.getRecordWithBLOBsType());
                    } else {
                        return new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
                    }
                }
            }
        }
        return null;
    }

    protected static class TableConfig {
        /**
         * 逻辑删除列
         */
        private IntrospectedColumn logicalDeleteColumn;
        /**
         * 逻辑删除值
         */
        private String logicalDeleteValue;
        /**
         * 逻辑删除值（未删除）
         */
        private String logicalUnDeleteValue;
        /**
         * 逻辑删除常量
         */
        private String logicalDeleteConstName = DEFAULT_LOGICAL_DELETE_NAME;
        /**
         * 逻辑删除常量（未删除）
         */
        private String logicalUnDeleteConstName = DEFAULT_LOGICAL_UN_DELETE_NAME;
        /**
         * 是否支持常量类型
         */
        private Boolean enableLogicalDeleteConst;
        /**
         * 逻辑删除枚举
         */
        private InnerEnum logicalDeleteEnum;
        private int logicalUnDeleteEnumIndex = 0;
        private int logicalDeleteEnumIndex = 1;
    }
}
