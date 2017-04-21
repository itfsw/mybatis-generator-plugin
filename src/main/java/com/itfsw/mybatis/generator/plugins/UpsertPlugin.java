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
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.internal.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

/**
 * ---------------------------------------------------------------------------
 * 存在即更新插件
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/3/21 10:59
 * ---------------------------------------------------------------------------
 */
public class UpsertPlugin extends PluginAdapter {
    private static final Logger logger = LoggerFactory.getLogger(BatchInsertPlugin.class);
    public static final String METHOD_UPSERT = "upsert";  // 方法名
    public static final String METHOD_UPSERT_SELECTIVE = "upsertSelective";  // 方法名
    public static final String METHOD_UPSERT_BY_EXAMPLE = "upsertByExample";   // 方法名
    public static final String METHOD_UPSERT_BY_EXAMPLE_SELECTIVE = "upsertByExampleSelective";   // 方法名

    public static final String PRE_ALLOW_MULTI_QUERIES = "allowMultiQueries";   // property allowMultiQueries
    private boolean allowMultiQueries = false;  // 是否允许多sql提交

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(List<String> warnings) {
        // 插件使用前提是targetRuntime为MyBatis3
        if (StringUtility.stringHasValue(getContext().getTargetRuntime()) && "MyBatis3".equalsIgnoreCase(getContext().getTargetRuntime()) == false) {
            logger.warn("itfsw:插件" + this.getClass().getTypeName() + "要求运行targetRuntime必须为MyBatis3！");
            return false;
        }

        // 插件使用前提是数据库为MySQL
        if ("com.mysql.jdbc.Driver".equalsIgnoreCase(this.getContext().getJdbcConnectionConfiguration().getDriverClass()) == false){
            logger.warn("itfsw:插件" + this.getClass().getTypeName() + "插件使用前提是数据库为MySQL！");
            return false;
        }

        // 插件是否开启了多sql提交
        Properties properties = this.getProperties();
        String allowMultiQueries = properties.getProperty(PRE_ALLOW_MULTI_QUERIES);
        this.allowMultiQueries = allowMultiQueries == null ? false : StringUtility.isTrue(allowMultiQueries);
        if (this.allowMultiQueries){
            // 提示用户注意信息
            logger.warn("itfsw:插件" + this.getClass().getTypeName() + "插件您开启了allowMultiQueries支持，注意在jdbc url 配置中增加“allowMultiQueries=true”支持（不怎么建议使用该功能，开启多sql提交会增加sql注入的风险，请确保你所有sql都使用MyBatis书写，请不要使用statement进行sql提交）！");
        }

        return true;
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
        // ====================================== 1. upsert ======================================
        Method mUpsert = new Method(METHOD_UPSERT);
        // 返回值类型
        mUpsert.setReturnType(FullyQualifiedJavaType.getIntInstance());
        // 添加参数
        mUpsert.addParameter(new Parameter(introspectedTable.getRules().calculateAllFieldsClass(), "record"));
        // 添加方法说明
        CommentTools.addMethodComment(mUpsert, introspectedTable);
        // interface 增加方法
        interfaze.addMethod(mUpsert);
        logger.debug("itfsw(存在即更新插件):" + interfaze.getType().getShortName() + "增加upsert方法。");

        // ====================================== 2. upsertSelective ======================================
        Method mUpsertSelective = new Method(METHOD_UPSERT_SELECTIVE);
        // 返回值类型
        mUpsertSelective.setReturnType(FullyQualifiedJavaType.getIntInstance());
        // 添加参数
        mUpsertSelective.addParameter(new Parameter(introspectedTable.getRules().calculateAllFieldsClass(), "record"));
        // 添加方法说明
        CommentTools.addMethodComment(mUpsertSelective, introspectedTable);
        // interface 增加方法
        interfaze.addMethod(mUpsertSelective);
        logger.debug("itfsw(存在即更新插件):" + interfaze.getType().getShortName() + "增加upsertSelective方法。");

        if (this.allowMultiQueries){
            // ====================================== 3. upsertByExample ======================================
            Method mUpsertByExample = new Method(METHOD_UPSERT_BY_EXAMPLE);
            // 返回值类型
            mUpsertByExample.setReturnType(null);
            // 添加参数
            mUpsertByExample.addParameter(new Parameter(introspectedTable.getRules().calculateAllFieldsClass(), "record", "@Param(\"record\")"));
            mUpsertByExample.addParameter(new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example", "@Param(\"example\")"));
            // 添加方法说明
            CommentTools.addMethodComment(mUpsertByExample, introspectedTable);
            // interface 增加方法
            interfaze.addMethod(mUpsertByExample);
            logger.debug("itfsw(存在即更新插件):" + interfaze.getType().getShortName() + "增加upsertByExample方法。");

            // ====================================== 4. upsertByExampleSelective ======================================
            Method mUpsertByExampleSelective = new Method(METHOD_UPSERT_BY_EXAMPLE_SELECTIVE);
            // 返回值类型
            mUpsertByExampleSelective.setReturnType(null);
            // 添加参数
            mUpsertByExampleSelective.addParameter(new Parameter(introspectedTable.getRules().calculateAllFieldsClass(), "record", "@Param(\"record\")"));
            mUpsertByExampleSelective.addParameter(new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example", "@Param(\"example\")"));
            // 添加方法说明
            CommentTools.addMethodComment(mUpsertByExampleSelective, introspectedTable);
            // interface 增加方法
            interfaze.addMethod(mUpsertByExampleSelective);
            logger.debug("itfsw(存在即更新插件):" + interfaze.getType().getShortName() + "增加upsertByExampleSelective方法。");
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

        // ====================================== 1. upsert ======================================
        XmlElement eleUpsert = new XmlElement("insert");
        eleUpsert.addAttribute(new Attribute("id", METHOD_UPSERT));
        // 添加注释(!!!必须添加注释，overwrite覆盖生成时，@see XmlFileMergerJaxp.isGeneratedNode会去判断注释中是否存在OLD_ELEMENT_TAGS中的一点，例子：@mbg.generated)
        CommentTools.addComment(eleUpsert);

        // 参数类型
        eleUpsert.addAttribute(new Attribute("parameterType", introspectedTable.getRules().calculateAllFieldsClass().getFullyQualifiedName()));

        // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
        XmlElementGeneratorTools.useGeneratedKeys(eleUpsert, introspectedTable);

        // insert
        eleUpsert.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
        eleUpsert.addElement(XmlElementGeneratorTools.generateKeys(introspectedTable.getAllColumns()));
        eleUpsert.addElement(new TextElement("values"));
        eleUpsert.addElement(XmlElementGeneratorTools.generateValues(introspectedTable.getAllColumns()));
        eleUpsert.addElement(new TextElement("on duplicate key update "));
        eleUpsert.addElement(XmlElementGeneratorTools.generateSets(introspectedTable.getAllColumns()));

        document.getRootElement().addElement(eleUpsert);
        logger.debug("itfsw(存在即更新插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加upsert实现方法。");

        // ====================================== 2. upsertSelective ======================================
        XmlElement eleUpsertSelective = new XmlElement("insert");
        eleUpsertSelective.addAttribute(new Attribute("id", METHOD_UPSERT_SELECTIVE));
        // 添加注释(!!!必须添加注释，overwrite覆盖生成时，@see XmlFileMergerJaxp.isGeneratedNode会去判断注释中是否存在OLD_ELEMENT_TAGS中的一点，例子：@mbg.generated)
        CommentTools.addComment(eleUpsertSelective);

        // 参数类型
        eleUpsertSelective.addAttribute(new Attribute("parameterType", introspectedTable.getRules().calculateAllFieldsClass().getFullyQualifiedName()));

        // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
        XmlElementGeneratorTools.useGeneratedKeys(eleUpsertSelective, introspectedTable);

        // insert
        eleUpsertSelective.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
        eleUpsertSelective.addElement(XmlElementGeneratorTools.generateKeysSelective(introspectedTable.getAllColumns()));
        eleUpsertSelective.addElement(new TextElement("values"));
        eleUpsertSelective.addElement(XmlElementGeneratorTools.generateValuesSelective(introspectedTable.getAllColumns()));
        eleUpsertSelective.addElement(new TextElement("on duplicate key update "));
        eleUpsertSelective.addElement(XmlElementGeneratorTools.generateSetsSelective(introspectedTable.getAllColumns(), null, false));

        document.getRootElement().addElement(eleUpsertSelective);
        logger.debug("itfsw(存在即更新插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加upsertSelective实现方法。");

        if (this.allowMultiQueries){
            // ====================================== 2. upsertByExample ======================================
            XmlElement eleUpsertByExample = new XmlElement("insert");
            eleUpsertByExample.addAttribute(new Attribute("id", METHOD_UPSERT_BY_EXAMPLE));
            // 参数类型
            eleUpsertByExample.addAttribute(new Attribute("parameterType", "map"));
            // 添加注释(!!!必须添加注释，overwrite覆盖生成时，@see XmlFileMergerJaxp.isGeneratedNode会去判断注释中是否存在OLD_ELEMENT_TAGS中的一点，例子：@mbg.generated)
            CommentTools.addComment(eleUpsertByExample);

            // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
            XmlElementGeneratorTools.useGeneratedKeys(eleUpsertByExample, introspectedTable, "record.");

            // insert
            eleUpsertByExample.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
            eleUpsertByExample.addElement(XmlElementGeneratorTools.generateKeys(introspectedTable.getAllColumns()));
            this.generateExistsClause(introspectedTable, eleUpsertByExample, false);

            // multiQueries
            eleUpsertByExample.addElement(new TextElement(";"));

            // update
            eleUpsertByExample.addElement(new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
            eleUpsertByExample.addElement(new TextElement("set"));
            eleUpsertByExample.addElement(XmlElementGeneratorTools.generateSets(ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns()), "record."));
            // update where
            eleUpsertByExample.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));

            document.getRootElement().addElement(eleUpsertByExample);
            logger.debug("itfsw(存在即更新插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加upsertSelective实现方法。");

            // ====================================== 4. upsertByExampleSelective ======================================
            XmlElement eleUpsertByExampleSelective = new XmlElement("insert");
            eleUpsertByExampleSelective.addAttribute(new Attribute("id", METHOD_UPSERT_BY_EXAMPLE_SELECTIVE));
            // 参数类型
            eleUpsertByExampleSelective.addAttribute(new Attribute("parameterType", "map"));
            // 添加注释(!!!必须添加注释，overwrite覆盖生成时，@see XmlFileMergerJaxp.isGeneratedNode会去判断注释中是否存在OLD_ELEMENT_TAGS中的一点，例子：@mbg.generated)
            CommentTools.addComment(eleUpsertByExampleSelective);

            // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
            XmlElementGeneratorTools.useGeneratedKeys(eleUpsertByExampleSelective, introspectedTable, "record.");

            // insert
            eleUpsertByExampleSelective.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
            eleUpsertByExampleSelective.addElement(XmlElementGeneratorTools.generateKeysSelective(introspectedTable.getAllColumns(), "record."));
            this.generateExistsClause(introspectedTable, eleUpsertByExampleSelective, true);

            // multiQueries
            eleUpsertByExampleSelective.addElement(new TextElement(";"));

            // update
            eleUpsertByExampleSelective.addElement(new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
            eleUpsertByExampleSelective.addElement(new TextElement("set"));
            eleUpsertByExampleSelective.addElement(XmlElementGeneratorTools.generateSets(ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns()), "record."));

            // update where
            eleUpsertByExampleSelective.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));

            document.getRootElement().addElement(eleUpsertByExampleSelective);
            logger.debug("itfsw(存在即更新插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加upsertSelective实现方法。");
        }

        return true;
    }

    /**
     * exists 语句
     *
     * @param introspectedTable
     * @param element
     * @param selective
     */
    private void generateExistsClause(IntrospectedTable introspectedTable, XmlElement element, boolean selective){
        element.addElement(new TextElement("select"));
        if (selective){
            element.addElement(XmlElementGeneratorTools.generateValuesSelective(introspectedTable.getAllColumns(), "record.", false));
        } else {
            element.addElement(XmlElementGeneratorTools.generateValues(introspectedTable.getAllColumns(), "record.", false));
        }
        element.addElement(new TextElement("from dual where not exists"));
        element.addElement(new TextElement("("));
        element.addElement(new TextElement("select 1 from " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));

        // if example
        element.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));

        element.addElement(new TextElement(")"));
    }

}