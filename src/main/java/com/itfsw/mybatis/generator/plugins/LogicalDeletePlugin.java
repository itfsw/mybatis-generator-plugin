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

import com.itfsw.mybatis.generator.plugins.utils.CommentTools;
import com.itfsw.mybatis.generator.plugins.utils.XmlElementGeneratorTools;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.internal.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.JDBCType;
import java.util.*;

/**
 * ---------------------------------------------------------------------------
 * 逻辑删除插件
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/1/13 14:08
 * ---------------------------------------------------------------------------
 */
public class LogicalDeletePlugin extends PluginAdapter {
    private static final Logger logger = LoggerFactory.getLogger(LogicalDeletePlugin.class);

    public static final String METHOD_LOGICAL_DELETE_BY_EXAMPLE = "logicalDeleteByExample";  // 方法名
    public static final String METHOD_LOGICAL_DELETE_BY_PRIMARY_KEY = "logicalDeleteByPrimaryKey";  // 方法名

    public static final String LOGICAL_DELETE_COLUMN_KEY = "logicalDeleteColumn";  // 逻辑删除列-Key
    public static final String LOGICAL_DELETE_VALUE_KEY = "logicalDeleteValue";  // 逻辑删除值-Key

    public static final String DEL_FLAG_NAME = "DEL_FLAG";  // 逻辑删除标志位常量名称

    public static final String METHOD_LOGICAL_DELETE = "andDeleted"; // 逻辑删除查询方法

    private IntrospectedColumn logicalDeleteColumn; // 逻辑删除列
    private String logicalDeleteValue;  // 逻辑删除值

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(List<String> warnings) {
        // 插件使用前提是targetRuntime为MyBatis3
        if (StringUtility.stringHasValue(getContext().getTargetRuntime()) && "MyBatis3".equalsIgnoreCase(getContext().getTargetRuntime()) == false ){
            logger.warn("itfsw:插件"+this.getClass().getTypeName()+"要求运行targetRuntime必须为MyBatis3！");
            return false;
        }
        return true;
    }

    /**
     * 初始化阶段
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param introspectedTable
     * @return
     */
    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        // 1. 首先获取全局配置
        Properties properties = getProperties();
        String logicalDeleteColumn = properties.getProperty(LOGICAL_DELETE_COLUMN_KEY);
        this.logicalDeleteValue = properties.getProperty(LOGICAL_DELETE_VALUE_KEY);
        // 2. 获取表单独配置，如果有则覆盖全局配置
        if (introspectedTable.getTableConfigurationProperty(LOGICAL_DELETE_COLUMN_KEY) != null){
            logicalDeleteColumn = introspectedTable.getTableConfigurationProperty(LOGICAL_DELETE_COLUMN_KEY);
        }
        if (introspectedTable.getTableConfigurationProperty(LOGICAL_DELETE_VALUE_KEY) != null){
            this.logicalDeleteValue = introspectedTable.getTableConfigurationProperty(LOGICAL_DELETE_VALUE_KEY);
        }
        // 3. 判断该表是否存在逻辑删除列
        this.logicalDeleteColumn = null;
        List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
        for (IntrospectedColumn column : columns) {
            if (column.getActualColumnName().equalsIgnoreCase(logicalDeleteColumn)){
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
                        || JDBCType.VARCHAR == type){
                    this.logicalDeleteColumn = column;
                } else {
                    logger.warn("itfsw:插件"+this.getClass().getTypeName()+"逻辑删除列的类型不支持（请使用数字列，字符串列，布尔列）！");
                }
            }
        }
    }

    /**
     * Java Client Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param interfaze
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (this.logicalDeleteColumn != null){
            // 1. 逻辑删除ByExample
            Method mLogicalDeleteByExample = new Method(METHOD_LOGICAL_DELETE_BY_EXAMPLE);
            // 返回值类型
            mLogicalDeleteByExample.setReturnType(FullyQualifiedJavaType.getIntInstance());
            // 添加参数
            FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getExampleType());
            mLogicalDeleteByExample.addParameter(new Parameter(type, "example"));
            // 添加方法说明
            CommentTools.addGeneralMethodComment(mLogicalDeleteByExample, introspectedTable);
            // interface 增加方法
            interfaze.addMethod(mLogicalDeleteByExample);

            // 2. 判断是否有主键，生成主键删除方法
            if (introspectedTable.hasPrimaryKeyColumns()){
                // 1. 逻辑删除ByExample
                Method mLogicalDeleteByPrimaryKey = new Method(METHOD_LOGICAL_DELETE_BY_PRIMARY_KEY);
                // 返回值类型
                mLogicalDeleteByPrimaryKey.setReturnType(FullyQualifiedJavaType.getIntInstance());

                // 添加参数
                Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
                if (introspectedTable.getRules().generatePrimaryKeyClass()) {
                    FullyQualifiedJavaType type1 = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
                    importedTypes.add(type1);
                    mLogicalDeleteByPrimaryKey.addParameter(new Parameter(type1, "key")); //$NON-NLS-1$
                } else {
                    // no primary key class - fields are in the base class
                    // if more than one PK field, then we need to annotate the
                    // parameters
                    // for MyBatis
                    List<IntrospectedColumn> introspectedColumns = introspectedTable.getPrimaryKeyColumns();
                    boolean annotate = introspectedColumns.size() > 1;
                    if (annotate) {
                        importedTypes.add(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param")); //$NON-NLS-1$
                    }
                    StringBuilder sb = new StringBuilder();
                    for (IntrospectedColumn introspectedColumn : introspectedColumns) {
                        FullyQualifiedJavaType type1 = introspectedColumn.getFullyQualifiedJavaType();
                        importedTypes.add(type1);
                        Parameter parameter = new Parameter(type1, introspectedColumn.getJavaProperty());
                        if (annotate) {
                            sb.setLength(0);
                            sb.append("@Param(\""); //$NON-NLS-1$
                            sb.append(introspectedColumn.getJavaProperty());
                            sb.append("\")"); //$NON-NLS-1$
                            parameter.addAnnotation(sb.toString());
                        }
                        mLogicalDeleteByPrimaryKey.addParameter(parameter);
                    }
                }

                // 添加方法说明
                CommentTools.addGeneralMethodComment(mLogicalDeleteByPrimaryKey, introspectedTable);
                // interface 增加方法
                interfaze.addImportedTypes(importedTypes);
                interfaze.addMethod(mLogicalDeleteByPrimaryKey);
            }
        }
        return true;
    }

    /**
     * SQL Map Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param document
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        if (this.logicalDeleteColumn != null){
            // 1. 逻辑删除ByExample
            XmlElement logicalDeleteByExample = new XmlElement("update"); //$NON-NLS-1$
            logicalDeleteByExample.addAttribute(new Attribute("id", METHOD_LOGICAL_DELETE_BY_EXAMPLE));
            logicalDeleteByExample.addAttribute(new Attribute("parameterType", "map")); //$NON-NLS-1$ //$NON-NLS-2$
            CommentTools.addComment(logicalDeleteByExample);

            StringBuilder sb = new StringBuilder();
            sb.append("update "); //$NON-NLS-1$
            sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
            sb.append(" set ");
            // 更新逻辑删除字段
            sb.append(this.logicalDeleteColumn.getActualColumnName());
            sb.append(" = ");

            // 判断字段类型
            JDBCType type = JDBCType.valueOf(this.logicalDeleteColumn.getJdbcType());
            if (this.logicalDeleteValue == null || "NULL".equalsIgnoreCase(this.logicalDeleteValue)){
                sb.append("NULL");
            } else if (JDBCType.CHAR == type
                    || JDBCType.LONGNVARCHAR == type
                    || JDBCType.LONGVARCHAR == type
                    || JDBCType.NCHAR == type
                    || JDBCType.NVARCHAR == type
                    || JDBCType.VARCHAR == type){
                sb.append("'");
                sb.append(this.logicalDeleteValue);
                sb.append("'");
            } else {
                sb.append(this.logicalDeleteValue);
            }

            logicalDeleteByExample.addElement(new TextElement(sb.toString()));

            logicalDeleteByExample.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));
            document.getRootElement().addElement(logicalDeleteByExample);

            // 2. 判断是否有主键，生成主键删除方法
            if (introspectedTable.hasPrimaryKeyColumns()){
                XmlElement logicalDeleteByPrimaryKey = new XmlElement("update"); //$NON-NLS-1$
                logicalDeleteByPrimaryKey.addAttribute(new Attribute("id", METHOD_LOGICAL_DELETE_BY_PRIMARY_KEY));

                String parameterClass;
                if (introspectedTable.getRules().generatePrimaryKeyClass()) {
                    parameterClass = introspectedTable.getPrimaryKeyType();
                } else {
                    // PK fields are in the base class. If more than on PK
                    // field, then they are coming in a map.
                    if (introspectedTable.getPrimaryKeyColumns().size() > 1) {
                        parameterClass = "map"; //$NON-NLS-1$
                    } else {
                        parameterClass = introspectedTable.getPrimaryKeyColumns().get(0).getFullyQualifiedJavaType().toString();
                    }
                }
                logicalDeleteByPrimaryKey.addAttribute(new Attribute("parameterType", parameterClass));
                CommentTools.addComment(logicalDeleteByPrimaryKey);

                StringBuilder sb1 = new StringBuilder();
                sb1.append("update "); //$NON-NLS-1$
                sb1.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
                sb1.append(" set ");
                // 更新逻辑删除字段
                sb1.append(this.logicalDeleteColumn.getActualColumnName());
                sb1.append(" = ");

                // 判断字段类型
                JDBCType type1 = JDBCType.valueOf(this.logicalDeleteColumn.getJdbcType());
                if (this.logicalDeleteValue == null || "NULL".equalsIgnoreCase(this.logicalDeleteValue)){
                    sb1.append("NULL");
                } else if (JDBCType.CHAR == type1
                        || JDBCType.LONGNVARCHAR == type1
                        || JDBCType.LONGVARCHAR == type1
                        || JDBCType.NCHAR == type1
                        || JDBCType.NVARCHAR == type1
                        || JDBCType.VARCHAR == type1){
                    sb1.append("'");
                    sb1.append(this.logicalDeleteValue);
                    sb1.append("'");
                } else {
                    sb1.append(this.logicalDeleteValue);
                }

                logicalDeleteByPrimaryKey.addElement(new TextElement(sb1.toString()));

                boolean and = false;
                for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                    sb.setLength(0);
                    if (and) {
                        sb.append("  and "); //$NON-NLS-1$
                    } else {
                        sb.append("where "); //$NON-NLS-1$
                        and = true;
                    }

                    sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
                    sb.append(" = "); //$NON-NLS-1$
                    sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
                    logicalDeleteByPrimaryKey.addElement(new TextElement(sb.toString()));
                }

                document.getRootElement().addElement(logicalDeleteByPrimaryKey);
            }
        }
        return true;
    }

    /**
     * Model 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (this.logicalDeleteColumn != null){
            // 添加删除标志位常量
            Field field = new Field(DEL_FLAG_NAME, this.logicalDeleteColumn.getFullyQualifiedJavaType());
            field.setVisibility(JavaVisibility.PUBLIC);
            field.setStatic(true);
            field.setFinal(true);

            if (this.logicalDeleteValue == null || "NULL".equalsIgnoreCase(this.logicalDeleteValue)){
                field.setInitializationString("null");
            } else if (this.logicalDeleteColumn.getFullyQualifiedJavaType().getShortNameWithoutTypeArguments().equalsIgnoreCase("String")){
                field.setInitializationString("\"" + this.logicalDeleteValue + "\"");
            } else if (this.logicalDeleteColumn.getFullyQualifiedJavaType().getShortNameWithoutTypeArguments().equalsIgnoreCase("Boolean")){
                field.setInitializationString((this.logicalDeleteValue.equals("1") || this.logicalDeleteValue.equalsIgnoreCase("true")) ? "true" : "false");
            } else {
                field.setInitializationString(this.logicalDeleteValue);
            }
            CommentTools.addFieldComment(field, introspectedTable);

            // 常量插入到第一位
            ArrayList<Field> fields = (ArrayList<Field>) topLevelClass.getFields();
            fields.add(0, field);
        }
        return true;
    }

    /**
     * ModelExample Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (this.logicalDeleteColumn != null){
            // 引入 Model类
            FullyQualifiedJavaType model = introspectedTable.getRules().calculateAllFieldsClass();
            topLevelClass.addImportedType(model);

            List<InnerClass> innerClasses = topLevelClass.getInnerClasses();
            for (InnerClass innerClass : innerClasses) {
                if ("Criteria".equals(innerClass.getType().getShortName())) {
                    // 增加逻辑删除条件
                    Method method = new Method(METHOD_LOGICAL_DELETE);
                    CommentTools.addGeneralMethodComment(method, introspectedTable);
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
                    equalToMethodName.insert(0, "and"); //$NON-NLS-1$
                    equalToMethodName.append("EqualTo");
                    sb.append(equalToMethodName);
                    sb.append("("+modelName + "." + DEL_FLAG_NAME + ")");

                    sb.append(" : ");

                    // 调用NotEqualTo 方法
                    StringBuilder notEqualToMethodName = new StringBuilder();
                    notEqualToMethodName.append(this.logicalDeleteColumn.getJavaProperty());
                    notEqualToMethodName.setCharAt(0, Character.toUpperCase(notEqualToMethodName.charAt(0)));
                    notEqualToMethodName.insert(0, "and"); //$NON-NLS-1$
                    notEqualToMethodName.append("NotEqualTo");
                    sb.append(notEqualToMethodName);
                    sb.append("("+modelName + "." + DEL_FLAG_NAME + ")");

                    sb.append(";");

                    method.addBodyLine(sb.toString());

                    innerClass.addMethod(method);
                }
            }
        }
        return true;
    }
}
