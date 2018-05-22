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
import com.itfsw.mybatis.generator.plugins.utils.BeanUtils;
import com.itfsw.mybatis.generator.plugins.utils.hook.ITableConfigurationHook;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.ColumnRenamingRule;
import org.mybatis.generator.config.DomainObjectRenamingRule;
import org.mybatis.generator.config.TableConfiguration;

import java.util.List;
import java.util.Properties;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2018/5/21 11:23
 * ---------------------------------------------------------------------------
 */
public class TableConfigurationPlugin extends BasePlugin implements ITableConfigurationHook {
    public static final String PRO_TABLE_SEARCH_STRING = "domainObjectRenamingRule.searchString";  // 查找 property
    public static final String PRO_TABLE_REPLACE_STRING = "domainObjectRenamingRule.replaceString";  // 替换 property
    public static final String PRO_COLUMN_SEARCH_STRING = "columnRenamingRule.searchString";  // 查找 property
    public static final String PRO_COLUMN_REPLACE_STRING = "columnRenamingRule.replaceString";  // 替换 property

    public static final String PRO_CLIENT_SUFFIX = "clientSuffix";  // client 结尾
    public static final String PRO_SQL_MAP_SUFFIX = "sqlMapSuffix";  // sqlMap 结尾
    public static final String PRO_EXAMPLE_SUFFIX = "exampleSuffix"; // example 结尾
    public static final String PRO_MODEL_SUFFIX = "modelSuffix"; // model 结尾

    private String tableSearchString;
    private String tableReplaceString;
    private String columnSearchString;
    private String columnReplaceString;

    private String clientSuffix;  // client 结尾
    private String sqlMapSuffix;    // sqlMap 结尾
    private String exampleSuffix; // example 结尾
    private String modelSuffix; // model 结尾

    /**
     * {@inheritDoc}
     * @param warnings
     * @return
     */
    @Override
    public boolean validate(List<String> warnings) {
        Properties properties = this.getProperties();
        // 如果配置了searchString 或者 replaceString，二者不允许单独存在
        if ((properties.getProperty(PRO_TABLE_SEARCH_STRING) == null && properties.getProperty(PRO_TABLE_REPLACE_STRING) != null)
                || (properties.getProperty(PRO_TABLE_SEARCH_STRING) != null && properties.getProperty(PRO_TABLE_REPLACE_STRING) == null)
                || (properties.getProperty(PRO_COLUMN_SEARCH_STRING) == null && properties.getProperty(PRO_COLUMN_REPLACE_STRING) != null)
                || (properties.getProperty(PRO_COLUMN_SEARCH_STRING) != null && properties.getProperty(PRO_COLUMN_REPLACE_STRING) == null)) {
            warnings.add("itfsw:插件" + this.getClass().getTypeName() + "插件的searchString、replaceString属性需配合使用，不能单独存在！");
            return false;
        }

        this.tableSearchString = properties.getProperty(PRO_TABLE_SEARCH_STRING);
        this.tableReplaceString = properties.getProperty(PRO_TABLE_REPLACE_STRING);
        this.columnSearchString = properties.getProperty(PRO_COLUMN_SEARCH_STRING);
        this.columnReplaceString = properties.getProperty(PRO_COLUMN_REPLACE_STRING);
        this.exampleSuffix = properties.getProperty(PRO_EXAMPLE_SUFFIX);
        this.clientSuffix = properties.getProperty(PRO_CLIENT_SUFFIX);
        this.sqlMapSuffix = properties.getProperty(PRO_SQL_MAP_SUFFIX);
        this.modelSuffix = properties.getProperty(PRO_MODEL_SUFFIX);

        return super.validate(warnings);
    }

    /**
     * 表配置
     * @param introspectedTable
     */
    @Override
    public void tableConfiguration(IntrospectedTable introspectedTable) {
        try {
            TableConfiguration tableConfiguration = introspectedTable.getTableConfiguration();
            FullyQualifiedTable fullyQualifiedTable = introspectedTable.getFullyQualifiedTable();

            String javaClientInterfacePackage = (String) BeanUtils.invoke(introspectedTable, IntrospectedTable.class, "calculateJavaClientInterfacePackage");
            String sqlMapPackage = (String) BeanUtils.invoke(introspectedTable, IntrospectedTable.class, "calculateSqlMapPackage");
            String javaModelPackage = (String) BeanUtils.invoke(introspectedTable, IntrospectedTable.class, "calculateJavaModelPackage");
            // --------------------- table 重命名 ----------------------------
            if (tableConfiguration.getDomainObjectRenamingRule() == null
                    && this.tableSearchString != null && this.tableReplaceString != null) {
                DomainObjectRenamingRule rule = new DomainObjectRenamingRule();
                rule.setSearchString(this.tableSearchString);
                rule.setReplaceString(this.tableReplaceString);
                tableConfiguration.setDomainObjectRenamingRule(rule);
            }
            // --------------------- column 重命名 ---------------------------
            if (tableConfiguration.getColumnRenamingRule() == null
                    && this.columnSearchString != null && this.columnReplaceString != null) {
                ColumnRenamingRule rule = new ColumnRenamingRule();
                rule.setSearchString(this.columnSearchString);
                rule.setReplaceString(this.columnReplaceString);
                tableConfiguration.setColumnRenamingRule(rule);
            }

            // ---------------------- 后缀修正 -------------------------------
            // 1. client
            if (this.clientSuffix != null) {
                // mapper
                StringBuilder sb = new StringBuilder();
                sb.append(javaClientInterfacePackage);
                sb.append('.');
                if (stringHasValue(tableConfiguration.getMapperName())) {
                    sb.append(tableConfiguration.getMapperName());
                } else {
                    if (stringHasValue(fullyQualifiedTable.getDomainObjectSubPackage())) {
                        sb.append(fullyQualifiedTable.getDomainObjectSubPackage());
                        sb.append('.');
                    }
                    sb.append(fullyQualifiedTable.getDomainObjectName());
                    sb.append(this.clientSuffix);
                }
                introspectedTable.setMyBatis3JavaMapperType(sb.toString());
                // xml mapper namespace
                sb.setLength(0);
                sb.append(sqlMapPackage);
                sb.append('.');
                if (stringHasValue(tableConfiguration.getMapperName())) {
                    sb.append(tableConfiguration.getMapperName());
                } else {
                    sb.append(fullyQualifiedTable.getDomainObjectName());
                    sb.append(this.clientSuffix);
                }
                introspectedTable.setMyBatis3FallbackSqlMapNamespace(sb.toString());
            }
            // 2. sqlMap
            if (!stringHasValue(tableConfiguration.getMapperName())
                    && (this.sqlMapSuffix != null || this.clientSuffix != null)) {
                StringBuilder sb = new StringBuilder();

                sb.append(fullyQualifiedTable.getDomainObjectName());
                sb.append(this.sqlMapSuffix != null ? this.sqlMapSuffix : this.clientSuffix);
                sb.append(".xml");

                introspectedTable.setMyBatis3XmlMapperFileName(sb.toString());
            }

            // 2. example
            if (this.exampleSuffix != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(javaModelPackage);
                sb.append('.');
                sb.append(fullyQualifiedTable.getDomainObjectName());
                sb.append(this.exampleSuffix);
                introspectedTable.setExampleType(sb.toString());
            }
            // 3. model
            if (this.modelSuffix != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(javaModelPackage);
                sb.append('.');
                sb.append(fullyQualifiedTable.getDomainObjectName());
                sb.append("Key");
                sb.append(this.modelSuffix);
                introspectedTable.setPrimaryKeyType(sb.toString());

                sb.setLength(0);
                sb.append(javaModelPackage);
                sb.append('.');
                sb.append(fullyQualifiedTable.getDomainObjectName());
                sb.append(this.modelSuffix);
                introspectedTable.setBaseRecordType(sb.toString());

                sb.setLength(0);
                sb.append(javaModelPackage);
                sb.append('.');
                sb.append(fullyQualifiedTable.getDomainObjectName());
                sb.append("WithBLOBs");
                sb.append(this.modelSuffix);
                introspectedTable.setRecordWithBLOBsType(sb.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
