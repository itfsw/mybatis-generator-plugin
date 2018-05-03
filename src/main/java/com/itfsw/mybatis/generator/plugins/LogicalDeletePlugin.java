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

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import com.itfsw.mybatis.generator.plugins.utils.FormatTools;
import com.itfsw.mybatis.generator.plugins.utils.JavaElementGeneratorTools;
import com.itfsw.mybatis.generator.plugins.utils.XmlElementGeneratorTools;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.sql.JDBCType;
import java.util.*;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * ---------------------------------------------------------------------------
 * 逻辑删除插件
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/1/13 14:08
 * ---------------------------------------------------------------------------
 */
public class LogicalDeletePlugin extends BasePlugin {
    public static final String METHOD_LOGICAL_DELETE_BY_EXAMPLE = "logicalDeleteByExample";  // 方法名
    public static final String METHOD_LOGICAL_DELETE_BY_PRIMARY_KEY = "logicalDeleteByPrimaryKey";  // 方法名

    public static final String PRO_LOGICAL_DELETE_COLUMN = "logicalDeleteColumn";  // 逻辑删除列-Key
    public static final String PRO_LOGICAL_DELETE_VALUE = "logicalDeleteValue";  // 逻辑删除值-Key
    public static final String PRO_LOGICAL_UN_DELETE_VALUE = "logicalUnDeleteValue";  // 逻辑删除未删除值-Key

    public static final String PRO_LOGICAL_DELETE_CONST_NAME = "logicalDeleteConstName"; // 逻辑删除常量
    public static final String PRO_LOGICAL_UN_DELETE_CONST_NAME = "logicalUnDeleteConstName"; // 逻辑删除常量
    public static final String DEFAULT_LOGICAL_DELETE_CONST_NAME = "IS_DELETED";  // 逻辑删除标志位常量名称
    public static final String DEFAULT_LOGICAL_UN_DELETE_CONST_NAME = "NOT_DELETED";  // 逻辑删除标志位常量名称(未删除)

    public static final String METHOD_LOGICAL_DELETED = "andLogicalDeleted"; // 逻辑删除查询方法
    public static final String PARAMETER_LOGICAL_DELETED = METHOD_LOGICAL_DELETED;  // 增强selectByPrimaryKey是参数名称

    public static final String METHOD_SELECT_BY_PRIMARY_KEY_WITH_LOGICAL_DELETE = "selectByPrimaryKeyWithLogicalDelete";  // selectByPrimaryKey 的逻辑删除增强

    private IntrospectedColumn logicalDeleteColumn; // 逻辑删除列
    private String logicalDeleteValue;  // 逻辑删除值
    private String logicalUnDeleteValue;    // 逻辑删除值（未删除）
    private String logicalDeleteConstName;  // 逻辑删除常量
    private String logicalUnDeleteConstName;    // 逻辑删除常量（未删除）

    /**
     * 初始化阶段
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param introspectedTable
     * @return
     */
    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        // 1. 首先获取全局配置
        Properties properties = getProperties();
        String logicalDeleteColumn = properties.getProperty(PRO_LOGICAL_DELETE_COLUMN);
        this.logicalDeleteValue = properties.getProperty(PRO_LOGICAL_DELETE_VALUE);
        this.logicalUnDeleteValue = properties.getProperty(PRO_LOGICAL_UN_DELETE_VALUE);
        // 2. 获取表单独配置，如果有则覆盖全局配置
        if (introspectedTable.getTableConfigurationProperty(PRO_LOGICAL_DELETE_COLUMN) != null) {
            logicalDeleteColumn = introspectedTable.getTableConfigurationProperty(PRO_LOGICAL_DELETE_COLUMN);
        }
        if (introspectedTable.getTableConfigurationProperty(PRO_LOGICAL_DELETE_VALUE) != null) {
            this.logicalDeleteValue = introspectedTable.getTableConfigurationProperty(PRO_LOGICAL_DELETE_VALUE);
        }
        if (introspectedTable.getTableConfigurationProperty(PRO_LOGICAL_UN_DELETE_VALUE) != null) {
            this.logicalUnDeleteValue = introspectedTable.getTableConfigurationProperty(PRO_LOGICAL_UN_DELETE_VALUE);
        }
        // 3. 判断该表是否存在逻辑删除列
        this.logicalDeleteColumn = null;
        List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
        for (IntrospectedColumn column : columns) {
            if (column.getActualColumnName().equalsIgnoreCase(logicalDeleteColumn)) {
                // 判断字段类型
                JDBCType type = JDBCType.valueOf(column.getJdbcType());
                if (JDBCType.BIGINT == type
                        || JDBCType.BIT == type
                        || JDBCType.BOOLEAN == type
                        || JDBCType.CHAR == type
                        || JDBCType.DECIMAL == type
                        || JDBCType.DOUBLE == type
                        || JDBCType.FLOAT == type
                        || JDBCType.INTEGER == type
                        || JDBCType.LONGNVARCHAR == type
                        || JDBCType.LONGVARCHAR == type
                        || JDBCType.NCHAR == type
                        || JDBCType.NUMERIC == type
                        || JDBCType.NVARCHAR == type
                        || JDBCType.SMALLINT == type
                        || JDBCType.TINYINT == type
                        || JDBCType.VARCHAR == type) {
                    this.logicalDeleteColumn = column;
                } else {
                    warnings.add("itfsw(逻辑删除插件):" + introspectedTable.getFullyQualifiedTable() + "逻辑删除列(" + introspectedTable.getTableConfigurationProperty(PRO_LOGICAL_DELETE_COLUMN) + ")的类型不在支持范围（请使用数字列，字符串列，布尔列）！");
                }
            }
        }

        if (introspectedTable.getTableConfigurationProperty(PRO_LOGICAL_DELETE_COLUMN) != null && this.logicalDeleteColumn == null) {
            warnings.add("itfsw(逻辑删除插件):" + introspectedTable.getFullyQualifiedTable() + "没有找到您配置的逻辑删除列(" + introspectedTable.getTableConfigurationProperty(PRO_LOGICAL_DELETE_COLUMN) + ")！");
        }

        // 4. 判断逻辑删除值是否配置了
        if (this.logicalDeleteColumn != null && (this.logicalDeleteValue == null || this.logicalUnDeleteValue == null)) {
            warnings.add("itfsw(逻辑删除插件):" + introspectedTable.getFullyQualifiedTable() + "没有找到您配置的逻辑删除值，请全局或者局部配置logicalDeleteValue和logicalUnDeleteValue值！");
        }

        // 5. 获取逻辑删除常量值
        this.logicalDeleteConstName = properties.getProperty(PRO_LOGICAL_DELETE_CONST_NAME) != null ? properties.getProperty(PRO_LOGICAL_DELETE_CONST_NAME).toUpperCase() : DEFAULT_LOGICAL_DELETE_CONST_NAME;
        this.logicalUnDeleteConstName = properties.getProperty(PRO_LOGICAL_UN_DELETE_CONST_NAME) != null ? properties.getProperty(PRO_LOGICAL_UN_DELETE_CONST_NAME).toUpperCase() : DEFAULT_LOGICAL_UN_DELETE_CONST_NAME;

        // 6. 防止增强的selectByPrimaryKey中逻辑删除键冲突
        if (this.logicalDeleteColumn != null) {
            Field logicalDeleteField = JavaBeansUtil.getJavaBeansField(this.logicalDeleteColumn, context, introspectedTable);
            if (logicalDeleteField.getName().equals(PARAMETER_LOGICAL_DELETED)) {
                this.logicalDeleteColumn = null;
                warnings.add("itfsw(逻辑删除插件):" + introspectedTable.getFullyQualifiedTable() + "配置的逻辑删除列和插件保留关键字(" + PARAMETER_LOGICAL_DELETED + ")冲突！");
            }
        }
    }

    /**
     * Java Client Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param interfaze
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (this.logicalDeleteColumn != null) {
            // 1. 逻辑删除ByExample
            Method mLogicalDeleteByExample = JavaElementGeneratorTools.generateMethod(
                    METHOD_LOGICAL_DELETE_BY_EXAMPLE,
                    JavaVisibility.DEFAULT,
                    FullyQualifiedJavaType.getIntInstance(),
                    new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example", "@Param(\"example\")")
            );

            // 添加方法说明
            commentGenerator.addGeneralMethodComment(mLogicalDeleteByExample, introspectedTable);
            // interface 增加方法
            interfaze.addMethod(mLogicalDeleteByExample);
            logger.debug("itfsw(逻辑删除插件):" + interfaze.getType().getShortName() + "增加方法logicalDeleteByExample。");

            // 2. 判断是否有主键，生成主键删除方法
            if (introspectedTable.hasPrimaryKeyColumns()) {
                // 2.1. 逻辑删除ByExample
                Method mLogicalDeleteByPrimaryKey = JavaElementGeneratorTools.generateMethod(
                        METHOD_LOGICAL_DELETE_BY_PRIMARY_KEY,
                        JavaVisibility.DEFAULT,
                        FullyQualifiedJavaType.getIntInstance()
                );
                commentGenerator.addGeneralMethodComment(mLogicalDeleteByPrimaryKey, introspectedTable);

                // 2.2 增强selectByPrimaryKey
                Method mSelectByPrimaryKey = JavaElementGeneratorTools.generateMethod(
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
                interfaze.addMethod(mLogicalDeleteByPrimaryKey);
                logger.debug("itfsw(逻辑删除插件):" + interfaze.getType().getShortName() + "增加方法logicalDeleteByPrimaryKey。");

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
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param document
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        if (this.logicalDeleteColumn != null) {
            // 1. 逻辑删除ByExample
            XmlElement logicalDeleteByExample = new XmlElement("update"); 
            logicalDeleteByExample.addAttribute(new Attribute("id", METHOD_LOGICAL_DELETE_BY_EXAMPLE));
            logicalDeleteByExample.addAttribute(new Attribute("parameterType", "map"));  //$NON-NLS-2$
            commentGenerator.addComment(logicalDeleteByExample);

            StringBuilder sb = new StringBuilder();
            sb.append("update "); 
            sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
            sb.append(" set ");
            // 更新逻辑删除字段
            sb.append(this.logicalDeleteColumn.getActualColumnName());
            sb.append(" = ");

            // 判断字段类型
            JDBCType type = JDBCType.valueOf(this.logicalDeleteColumn.getJdbcType());
            if (this.logicalDeleteValue == null || "NULL".equalsIgnoreCase(this.logicalDeleteValue)) {
                sb.append("NULL");
            } else if (JDBCType.CHAR == type
                    || JDBCType.LONGNVARCHAR == type
                    || JDBCType.LONGVARCHAR == type
                    || JDBCType.NCHAR == type
                    || JDBCType.NVARCHAR == type
                    || JDBCType.VARCHAR == type) {
                sb.append("'");
                sb.append(this.logicalDeleteValue);
                sb.append("'");
            } else {
                sb.append(this.logicalDeleteValue);
            }

            logicalDeleteByExample.addElement(new TextElement(sb.toString()));

            logicalDeleteByExample.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));
            document.getRootElement().addElement(logicalDeleteByExample);
            logger.debug("itfsw(逻辑删除插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加方法logicalDeleteByExample的实现。");

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

                StringBuilder sb1 = new StringBuilder();
                sb1.append("update "); 
                sb1.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
                sb1.append(" set ");
                // 更新逻辑删除字段
                sb1.append(this.logicalDeleteColumn.getActualColumnName());
                sb1.append(" = ");

                // 判断字段类型
                JDBCType type1 = JDBCType.valueOf(this.logicalDeleteColumn.getJdbcType());
                if (this.logicalDeleteValue == null || "NULL".equalsIgnoreCase(this.logicalDeleteValue)) {
                    sb1.append("NULL");
                } else if (JDBCType.CHAR == type1
                        || JDBCType.LONGNVARCHAR == type1
                        || JDBCType.LONGVARCHAR == type1
                        || JDBCType.NCHAR == type1
                        || JDBCType.NVARCHAR == type1
                        || JDBCType.VARCHAR == type1) {
                    sb1.append("'");
                    sb1.append(this.logicalDeleteValue);
                    sb1.append("'");
                } else {
                    sb1.append(this.logicalDeleteValue);
                }

                logicalDeleteByPrimaryKey.addElement(new TextElement(sb1.toString()));

                XmlElementGeneratorTools.generateWhereByPrimaryKeyTo(logicalDeleteByPrimaryKey, introspectedTable.getPrimaryKeyColumns());

                document.getRootElement().addElement(logicalDeleteByPrimaryKey);
                logger.debug("itfsw(逻辑删除插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加方法logicalDeleteByPrimaryKey的实现。");


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

                context.getCommentGenerator().addComment(selectByPrimaryKey);

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
                sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(this.logicalDeleteColumn));
                sb.append(" = ");
                selectByPrimaryKey.addElement(new TextElement(sb.toString()));

                XmlElement chooseEle = new XmlElement("choose");
                XmlElement whenEle = new XmlElement("when");
                whenEle.addAttribute(new Attribute("test", PARAMETER_LOGICAL_DELETED));
                whenEle.addElement(new TextElement("'" + this.logicalDeleteValue + "'"));
                chooseEle.addElement(whenEle);
                XmlElement otherwiseEle = new XmlElement("otherwise");
                otherwiseEle.addElement(new TextElement("'" + this.logicalUnDeleteValue + "'"));
                chooseEle.addElement(otherwiseEle);
                selectByPrimaryKey.addElement(chooseEle);

                FormatTools.addElementWithBestPosition(document.getRootElement(), selectByPrimaryKey);
            }
        }
        return true;
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.generateModelMethodsAndFields(topLevelClass, introspectedTable);
        return super.modelPrimaryKeyClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * Model 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.generateModelMethodsAndFields(topLevelClass, introspectedTable);
        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * 生成Model 逻辑删除相关方法和常量
     * !!! bugfix：逻辑删除列作为复合主键的一部分的情况
     * @param topLevelClass
     * @param introspectedTable
     */
    private void generateModelMethodsAndFields(TopLevelClass topLevelClass, IntrospectedTable introspectedTable){
        if (this.logicalDeleteColumn != null) {
            // 常量和逻辑删除方法跟随 逻辑删除列走
            boolean flag = false;
            for (Field field : topLevelClass.getFields()){
                if (this.logicalDeleteColumn.getJavaProperty().equals(field.getName())){
                    flag = true;
                    break;
                }
            }

            if (flag){
                ArrayList<Field> fields = (ArrayList<Field>) topLevelClass.getFields();

                // 添加删除标志位常量
                String logicalDeleteValue;
                if (this.logicalDeleteValue == null || "NULL".equalsIgnoreCase(this.logicalDeleteValue)) {
                    logicalDeleteValue = "null";
                } else if (this.logicalDeleteColumn.getFullyQualifiedJavaType().getShortNameWithoutTypeArguments().equalsIgnoreCase("String")) {
                    logicalDeleteValue = "\"" + this.logicalDeleteValue + "\"";
                } else if (this.logicalDeleteColumn.getFullyQualifiedJavaType().getShortNameWithoutTypeArguments().equalsIgnoreCase("Boolean")) {
                    logicalDeleteValue = (this.logicalDeleteValue.equals("1") || this.logicalDeleteValue.equalsIgnoreCase("true")) ? "true" : "false";
                } else {
                    logicalDeleteValue = this.logicalDeleteValue;
                }
                // TODO 过期
                Field field = JavaElementGeneratorTools.generateStaticFinalField("DEL_FLAG_OFF", this.logicalDeleteColumn.getFullyQualifiedJavaType(), logicalDeleteValue);
                field.addAnnotation("@Deprecated");
                commentGenerator.addFieldComment(field, introspectedTable);
                // 常量插入到第一位
                fields.add(0, field);

                Field logicalDeleteConstField = JavaElementGeneratorTools.generateStaticFinalField(this.logicalDeleteConstName, this.logicalDeleteColumn.getFullyQualifiedJavaType(), logicalDeleteValue);
                commentGenerator.addFieldComment(logicalDeleteConstField, introspectedTable);
                fields.add(0, logicalDeleteConstField);


                // 添加删除标志位常量 DEL_FLAG_ON
                String logicalUnDeleteValue;
                if (this.logicalUnDeleteValue == null || "NULL".equalsIgnoreCase(this.logicalUnDeleteValue)) {
                    logicalUnDeleteValue = "null";
                } else if (this.logicalDeleteColumn.getFullyQualifiedJavaType().getShortNameWithoutTypeArguments().equalsIgnoreCase("String")) {
                    logicalUnDeleteValue = "\"" + this.logicalUnDeleteValue + "\"";
                } else if (this.logicalDeleteColumn.getFullyQualifiedJavaType().getShortNameWithoutTypeArguments().equalsIgnoreCase("Boolean")) {
                    logicalUnDeleteValue = (this.logicalUnDeleteValue.equals("1") || this.logicalUnDeleteValue.equalsIgnoreCase("true")) ? "true" : "false";
                } else {
                    logicalUnDeleteValue = this.logicalUnDeleteValue;
                }

                // TODO 过期
                Field field1 = JavaElementGeneratorTools.generateStaticFinalField("DEL_FLAG_ON", this.logicalDeleteColumn.getFullyQualifiedJavaType(), logicalUnDeleteValue);
                field1.addAnnotation("@Deprecated");
                commentGenerator.addFieldComment(field1, introspectedTable);
                // 常量插入到第一位
                fields.add(0, field1);

                Field logicalUnDeleteConstField = JavaElementGeneratorTools.generateStaticFinalField(this.logicalUnDeleteConstName, this.logicalDeleteColumn.getFullyQualifiedJavaType(), logicalUnDeleteValue);
                commentGenerator.addFieldComment(logicalUnDeleteConstField, introspectedTable);
                fields.add(0, logicalUnDeleteConstField);

                // ================================================= andLogicalDeleted =============================================
                Method mAndLogicalDeleted = JavaElementGeneratorTools.generateMethod(
                        METHOD_LOGICAL_DELETED,
                        JavaVisibility.PUBLIC,
                        null,
                        new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "deleted")
                );
                commentGenerator.addGeneralMethodComment(mAndLogicalDeleted, introspectedTable);
                Method logicalDeleteSetter = JavaBeansUtil.getJavaBeansSetter(this.logicalDeleteColumn, context, introspectedTable);
                mAndLogicalDeleted.addBodyLine(logicalDeleteSetter.getName() +"(deleted ? " + this.logicalDeleteConstName + " : " + this.logicalUnDeleteConstName + ");");
                FormatTools.addMethodWithBestPosition(topLevelClass, mAndLogicalDeleted);
            }
        }
    }

    /**
     * ModelExample Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (this.logicalDeleteColumn != null) {
            // 引入 Model类
            FullyQualifiedJavaType model = introspectedTable.getRules().calculateAllFieldsClass();
            topLevelClass.addImportedType(model);

            List<InnerClass> innerClasses = topLevelClass.getInnerClasses();
            for (InnerClass innerClass : innerClasses) {
                if ("Criteria".equals(innerClass.getType().getShortName())) {
                    // 增加逻辑删除条件
                    Method method = new Method(METHOD_LOGICAL_DELETED);
                    commentGenerator.addGeneralMethodComment(method, introspectedTable);
                    method.setVisibility(JavaVisibility.PUBLIC);
                    method.setReturnType(innerClass.getType());
                    method.addParameter(new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "deleted"));

                    StringBuffer sb = new StringBuffer();
                    sb.append("return deleted ? ");

                    String modelName = model.getShortName();

                    // 调用EqualTo方法
                    StringBuilder equalToMethodName = new StringBuilder();
                    equalToMethodName.append(this.logicalDeleteColumn.getJavaProperty());
                    equalToMethodName.setCharAt(0, Character.toUpperCase(equalToMethodName.charAt(0)));
                    equalToMethodName.insert(0, "and"); 
                    equalToMethodName.append("EqualTo");
                    sb.append(equalToMethodName);
                    sb.append("(" + modelName + "." + this.logicalDeleteConstName + ")");

                    sb.append(" : ");

                    // 调用NotEqualTo 方法
                    StringBuilder notEqualToMethodName = new StringBuilder();
                    notEqualToMethodName.append(this.logicalDeleteColumn.getJavaProperty());
                    notEqualToMethodName.setCharAt(0, Character.toUpperCase(notEqualToMethodName.charAt(0)));
                    notEqualToMethodName.insert(0, "and"); 
                    notEqualToMethodName.append("NotEqualTo");
                    sb.append(notEqualToMethodName);
                    sb.append("(" + modelName + "." + this.logicalDeleteConstName + ")");

                    sb.append(";");

                    method.addBodyLine(sb.toString());

                    innerClass.addMethod(method);

                    // TODO 过期方法
                    Method mAndDeleted = new Method(method);
                    mAndDeleted.setName("andDeleted");
                    mAndDeleted.addAnnotation("@Deprecated");
                    innerClass.addMethod(mAndDeleted);
                    logger.debug("itfsw(逻辑删除插件):" + topLevelClass.getType().getShortName() + "." + innerClass.getType().getShortName() + "增加andDeleted方法。");
                }
            }
        }
        return true;
    }
}
