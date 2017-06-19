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
import com.itfsw.mybatis.generator.plugins.utils.JavaElementGeneratorTools;
import com.itfsw.mybatis.generator.plugins.utils.XmlElementGeneratorTools;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.internal.util.StringUtility;

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
public class UpsertPlugin extends BasePlugin {
    public static final String METHOD_UPSERT = "upsert";  // 方法名
    public static final String METHOD_UPSERT_SELECTIVE = "upsertSelective";  // 方法名
    public static final String METHOD_UPSERT_BY_EXAMPLE = "upsertByExample";   // 方法名
    public static final String METHOD_UPSERT_BY_EXAMPLE_SELECTIVE = "upsertByExampleSelective";   // 方法名

    public static final String METHOD_UPSERT_WITH_BLOBS = "upsertWithBLOBs";  // 方法名
    public static final String METHOD_UPSERT_SELECTIVE_WITH_BLOBS = "upsertSelectiveWithBLOBs";  // 方法名
    public static final String METHOD_UPSERT_BY_EXAMPLE_WITH_BLOBS = "upsertByExampleWithBLOBs";   // 方法名
    public static final String METHOD_UPSERT_BY_EXAMPLE_SELECTIVE_WITH_BLOBS = "upsertByExampleSelectiveWithBLOBs";   // 方法名

    public static final String PRE_ALLOW_MULTI_QUERIES = "allowMultiQueries";   // property allowMultiQueries
    private boolean allowMultiQueries = false;  // 是否允许多sql提交

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(List<String> warnings) {

        // 插件使用前提是数据库为MySQL
        if ("com.mysql.jdbc.Driver".equalsIgnoreCase(this.getContext().getJdbcConnectionConfiguration().getDriverClass()) == false){
            logger.error("itfsw:插件" + this.getClass().getTypeName() + "插件使用前提是数据库为MySQL！");
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

        return super.validate(warnings);
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
        Method mUpsert = JavaElementGeneratorTools.generateMethod(
                METHOD_UPSERT,
                JavaVisibility.DEFAULT,
                FullyQualifiedJavaType.getIntInstance(),
                new Parameter(JavaElementGeneratorTools.getModelTypeWithoutBLOBs(introspectedTable), "record")
        );
        commentGenerator.addGeneralMethodComment(mUpsert, introspectedTable);
        // interface 增加方法
        interfaze.addMethod(mUpsert);
        logger.debug("itfsw(存在即更新插件):" + interfaze.getType().getShortName() + "增加upsert方法。");

        // ====================================== 2. upsertSelective ======================================
        Method mUpsertSelective = JavaElementGeneratorTools.generateMethod(
                METHOD_UPSERT_SELECTIVE,
                JavaVisibility.DEFAULT,
                FullyQualifiedJavaType.getIntInstance(),
                new Parameter(JavaElementGeneratorTools.getModelTypeWithoutBLOBs(introspectedTable), "record")
        );
        commentGenerator.addGeneralMethodComment(mUpsertSelective, introspectedTable);
        // interface 增加方法
        interfaze.addMethod(mUpsertSelective);
        logger.debug("itfsw(存在即更新插件):" + interfaze.getType().getShortName() + "增加upsertSelective方法。");

        if (this.allowMultiQueries){
            // ====================================== 3. upsertByExample ======================================
            Method mUpsertByExample = JavaElementGeneratorTools.generateMethod(
                    METHOD_UPSERT_BY_EXAMPLE,
                    JavaVisibility.DEFAULT,
                    FullyQualifiedJavaType.getIntInstance(),
                    new Parameter(JavaElementGeneratorTools.getModelTypeWithoutBLOBs(introspectedTable), "record", "@Param(\"record\")"),
                    new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example", "@Param(\"example\")")
            );
            commentGenerator.addGeneralMethodComment(mUpsertByExample, introspectedTable);
            // interface 增加方法
            interfaze.addMethod(mUpsertByExample);
            logger.debug("itfsw(存在即更新插件):" + interfaze.getType().getShortName() + "增加upsertByExample方法。");

            // ====================================== 4. upsertByExampleSelective ======================================
            Method mUpsertByExampleSelective = JavaElementGeneratorTools.generateMethod(
                    METHOD_UPSERT_BY_EXAMPLE_SELECTIVE,
                    JavaVisibility.DEFAULT,
                    FullyQualifiedJavaType.getIntInstance(),
                    new Parameter(JavaElementGeneratorTools.getModelTypeWithoutBLOBs(introspectedTable), "record", "@Param(\"record\")"),
                    new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example", "@Param(\"example\")")
            );
            commentGenerator.addGeneralMethodComment(mUpsertByExampleSelective, introspectedTable);
            // interface 增加方法
            interfaze.addMethod(mUpsertByExampleSelective);
            logger.debug("itfsw(存在即更新插件):" + interfaze.getType().getShortName() + "增加upsertByExampleSelective方法。");
        }

        // !!! 注意这里的行为不以有没有生成Model 的 WithBLOBs类为基准
        if (introspectedTable.hasBLOBColumns()){
            // ====================================== 1. upsertWithBLOBs ======================================
            Method mUpsertWithBLOBs = JavaElementGeneratorTools.generateMethod(
                    METHOD_UPSERT_WITH_BLOBS,
                    JavaVisibility.DEFAULT,
                    FullyQualifiedJavaType.getIntInstance(),
                    new Parameter(JavaElementGeneratorTools.getModelTypeWithBLOBs(introspectedTable), "record")
            );
            commentGenerator.addGeneralMethodComment(mUpsertWithBLOBs, introspectedTable);
            // interface 增加方法
            interfaze.addMethod(mUpsertWithBLOBs);
            logger.debug("itfsw(存在即更新插件):" + interfaze.getType().getShortName() + "增加upsert方法。");

            // ====================================== 2. upsertSelectiveWithBLOBs ======================================
            Method mUpsertSelectiveWithBLOBs = JavaElementGeneratorTools.generateMethod(
                    METHOD_UPSERT_SELECTIVE_WITH_BLOBS,
                    JavaVisibility.DEFAULT,
                    FullyQualifiedJavaType.getIntInstance(),
                    new Parameter(JavaElementGeneratorTools.getModelTypeWithBLOBs(introspectedTable), "record")
            );
            commentGenerator.addGeneralMethodComment(mUpsertSelectiveWithBLOBs, introspectedTable);
            // interface 增加方法
            interfaze.addMethod(mUpsertSelectiveWithBLOBs);
            logger.debug("itfsw(存在即更新插件):" + interfaze.getType().getShortName() + "增加upsertSelective方法。");

            if (this.allowMultiQueries){
                // ====================================== 3. upsertByExampleWithBLOBs ======================================
                Method mUpsertByExampleWithBLOBs = JavaElementGeneratorTools.generateMethod(
                        METHOD_UPSERT_BY_EXAMPLE_WITH_BLOBS,
                        JavaVisibility.DEFAULT,
                        FullyQualifiedJavaType.getIntInstance(),
                        new Parameter(JavaElementGeneratorTools.getModelTypeWithBLOBs(introspectedTable), "record", "@Param(\"record\")"),
                        new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example", "@Param(\"example\")")
                );
                commentGenerator.addGeneralMethodComment(mUpsertByExampleWithBLOBs, introspectedTable);
                // interface 增加方法
                interfaze.addMethod(mUpsertByExampleWithBLOBs);
                logger.debug("itfsw(存在即更新插件):" + interfaze.getType().getShortName() + "增加upsertByExample方法。");

                // ====================================== 4. upsertByExampleSelectiveWithBLOBs ======================================
                Method mUpsertByExampleSelectiveWithBLOBs = JavaElementGeneratorTools.generateMethod(
                        METHOD_UPSERT_BY_EXAMPLE_SELECTIVE_WITH_BLOBS,
                        JavaVisibility.DEFAULT,
                        FullyQualifiedJavaType.getIntInstance(),
                        new Parameter(JavaElementGeneratorTools.getModelTypeWithBLOBs(introspectedTable), "record", "@Param(\"record\")"),
                        new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example", "@Param(\"example\")")
                );
                commentGenerator.addGeneralMethodComment(mUpsertByExampleSelectiveWithBLOBs, introspectedTable);
                // interface 增加方法
                interfaze.addMethod(mUpsertByExampleSelectiveWithBLOBs);
                logger.debug("itfsw(存在即更新插件):" + interfaze.getType().getShortName() + "增加upsertByExampleSelective方法。");
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

        this.generateXmlElementWithoutBLOBs(document, introspectedTable);

        // !!! 注意这里的行为不以有没有生成Model 的 WithBLOBs类为基准
        if (introspectedTable.hasBLOBColumns()){
            this.generateXmlElementWithBLOBs(document, introspectedTable);
        }

        return true;
    }

    /**
     * 当Model有生成WithBLOBs类时的情况
     *
     * @param document
     * @param introspectedTable
     */
    private void generateXmlElementWithBLOBs(Document document, IntrospectedTable introspectedTable){
        // ====================================== 1. upsert ======================================
        XmlElement eleUpsertWithBLOBs = new XmlElement("insert");
        eleUpsertWithBLOBs.addAttribute(new Attribute("id", METHOD_UPSERT_WITH_BLOBS));
        // 添加注释(!!!必须添加注释，overwrite覆盖生成时，@see XmlFileMergerJaxp.isGeneratedNode会去判断注释中是否存在OLD_ELEMENT_TAGS中的一点，例子：@mbg.generated)
        commentGenerator.addComment(eleUpsertWithBLOBs);

        // 参数类型
        eleUpsertWithBLOBs.addAttribute(new Attribute("parameterType", JavaElementGeneratorTools.getModelTypeWithBLOBs(introspectedTable).getFullyQualifiedName()));

        // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
        XmlElementGeneratorTools.useGeneratedKeys(eleUpsertWithBLOBs, introspectedTable);

        // insert
        eleUpsertWithBLOBs.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
        eleUpsertWithBLOBs.addElement(XmlElementGeneratorTools.generateKeys(introspectedTable.getAllColumns()));
        eleUpsertWithBLOBs.addElement(new TextElement("values"));
        eleUpsertWithBLOBs.addElement(XmlElementGeneratorTools.generateValues(introspectedTable.getAllColumns()));
        eleUpsertWithBLOBs.addElement(new TextElement("on duplicate key update "));
        eleUpsertWithBLOBs.addElement(XmlElementGeneratorTools.generateSets(introspectedTable.getAllColumns()));

        document.getRootElement().addElement(eleUpsertWithBLOBs);
        logger.debug("itfsw(存在即更新插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加upsert实现方法。");

        // ====================================== 2. upsertSelective ======================================
        XmlElement eleUpsertSelectiveWithBLOBs = new XmlElement("insert");
        eleUpsertSelectiveWithBLOBs.addAttribute(new Attribute("id", METHOD_UPSERT_SELECTIVE_WITH_BLOBS));
        // 添加注释(!!!必须添加注释，overwrite覆盖生成时，@see XmlFileMergerJaxp.isGeneratedNode会去判断注释中是否存在OLD_ELEMENT_TAGS中的一点，例子：@mbg.generated)
        commentGenerator.addComment(eleUpsertSelectiveWithBLOBs);

        // 参数类型
        eleUpsertSelectiveWithBLOBs.addAttribute(new Attribute("parameterType", JavaElementGeneratorTools.getModelTypeWithBLOBs(introspectedTable).getFullyQualifiedName()));

        // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
        XmlElementGeneratorTools.useGeneratedKeys(eleUpsertSelectiveWithBLOBs, introspectedTable);

        // insert
        eleUpsertSelectiveWithBLOBs.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
        eleUpsertSelectiveWithBLOBs.addElement(XmlElementGeneratorTools.generateKeysSelective(introspectedTable.getAllColumns()));
        eleUpsertSelectiveWithBLOBs.addElement(new TextElement("values"));
        eleUpsertSelectiveWithBLOBs.addElement(XmlElementGeneratorTools.generateValuesSelective(introspectedTable.getAllColumns()));
        eleUpsertSelectiveWithBLOBs.addElement(new TextElement("on duplicate key update "));
        eleUpsertSelectiveWithBLOBs.addElement(XmlElementGeneratorTools.generateSetsSelective(introspectedTable.getAllColumns(), null, false));

        document.getRootElement().addElement(eleUpsertSelectiveWithBLOBs);
        logger.debug("itfsw(存在即更新插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加upsertSelective实现方法。");

        if (this.allowMultiQueries){
            // ====================================== 2. upsertByExample ======================================
            XmlElement eleUpsertByExampleWithBLOBs = new XmlElement("insert");
            eleUpsertByExampleWithBLOBs.addAttribute(new Attribute("id", METHOD_UPSERT_BY_EXAMPLE_WITH_BLOBS));
            // 参数类型
            eleUpsertByExampleWithBLOBs.addAttribute(new Attribute("parameterType", "map"));
            // 添加注释(!!!必须添加注释，overwrite覆盖生成时，@see XmlFileMergerJaxp.isGeneratedNode会去判断注释中是否存在OLD_ELEMENT_TAGS中的一点，例子：@mbg.generated)
            commentGenerator.addComment(eleUpsertByExampleWithBLOBs);

            // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
            XmlElementGeneratorTools.useGeneratedKeys(eleUpsertByExampleWithBLOBs, introspectedTable, "record.");

            // insert
            eleUpsertByExampleWithBLOBs.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
            eleUpsertByExampleWithBLOBs.addElement(XmlElementGeneratorTools.generateKeys(introspectedTable.getAllColumns()));
            this.generateExistsClause(introspectedTable, eleUpsertByExampleWithBLOBs, false, true);

            // multiQueries
            eleUpsertByExampleWithBLOBs.addElement(new TextElement(";"));

            // update
            eleUpsertByExampleWithBLOBs.addElement(new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
            eleUpsertByExampleWithBLOBs.addElement(new TextElement("set"));
            eleUpsertByExampleWithBLOBs.addElement(XmlElementGeneratorTools.generateSets(ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns()), "record."));
            // update where
            eleUpsertByExampleWithBLOBs.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));

            document.getRootElement().addElement(eleUpsertByExampleWithBLOBs);
            logger.debug("itfsw(存在即更新插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加upsertSelective实现方法。");

            // ====================================== 4. upsertByExampleSelective ======================================
            XmlElement eleUpsertByExampleSelectiveWithBLOBs = new XmlElement("insert");
            eleUpsertByExampleSelectiveWithBLOBs.addAttribute(new Attribute("id", METHOD_UPSERT_BY_EXAMPLE_SELECTIVE_WITH_BLOBS));
            // 参数类型
            eleUpsertByExampleSelectiveWithBLOBs.addAttribute(new Attribute("parameterType", "map"));
            // 添加注释(!!!必须添加注释，overwrite覆盖生成时，@see XmlFileMergerJaxp.isGeneratedNode会去判断注释中是否存在OLD_ELEMENT_TAGS中的一点，例子：@mbg.generated)
            commentGenerator.addComment(eleUpsertByExampleSelectiveWithBLOBs);

            // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
            XmlElementGeneratorTools.useGeneratedKeys(eleUpsertByExampleSelectiveWithBLOBs, introspectedTable, "record.");

            // insert
            eleUpsertByExampleSelectiveWithBLOBs.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
            eleUpsertByExampleSelectiveWithBLOBs.addElement(XmlElementGeneratorTools.generateKeysSelective(introspectedTable.getAllColumns(), "record."));
            this.generateExistsClause(introspectedTable, eleUpsertByExampleSelectiveWithBLOBs, true, true);

            // multiQueries
            eleUpsertByExampleSelectiveWithBLOBs.addElement(new TextElement(";"));

            // update
            eleUpsertByExampleSelectiveWithBLOBs.addElement(new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
            eleUpsertByExampleSelectiveWithBLOBs.addElement(new TextElement("set"));
            eleUpsertByExampleSelectiveWithBLOBs.addElement(XmlElementGeneratorTools.generateSetsSelective(ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns()), "record."));

            // update where
            eleUpsertByExampleSelectiveWithBLOBs.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));

            document.getRootElement().addElement(eleUpsertByExampleSelectiveWithBLOBs);
            logger.debug("itfsw(存在即更新插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加upsertSelective实现方法。");
        }
    }

    /**
     * 当Model没有生成WithBLOBs类时的情况
     *
     * @param document
     * @param introspectedTable
     */
    private void generateXmlElementWithoutBLOBs(Document document, IntrospectedTable introspectedTable){
        // WithoutBLOBs也会存在只有一个时，不生成WithBLOBs对象的情况
        boolean flag = !introspectedTable.getRules().generateRecordWithBLOBsClass();
        List<IntrospectedColumn> columns = flag ? introspectedTable.getAllColumns() : introspectedTable.getNonBLOBColumns();

        // ====================================== 1. upsert ======================================
        XmlElement eleUpsert = new XmlElement("insert");
        eleUpsert.addAttribute(new Attribute("id", METHOD_UPSERT));
        // 添加注释(!!!必须添加注释，overwrite覆盖生成时，@see XmlFileMergerJaxp.isGeneratedNode会去判断注释中是否存在OLD_ELEMENT_TAGS中的一点，例子：@mbg.generated)
        commentGenerator.addComment(eleUpsert);

        // 参数类型
        eleUpsert.addAttribute(new Attribute("parameterType", JavaElementGeneratorTools.getModelTypeWithoutBLOBs(introspectedTable).getFullyQualifiedName()));

        // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
        XmlElementGeneratorTools.useGeneratedKeys(eleUpsert, introspectedTable);

        // insert
        eleUpsert.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
        eleUpsert.addElement(XmlElementGeneratorTools.generateKeys(columns));
        eleUpsert.addElement(new TextElement("values"));
        eleUpsert.addElement(XmlElementGeneratorTools.generateValues(columns));
        eleUpsert.addElement(new TextElement("on duplicate key update "));
        eleUpsert.addElement(XmlElementGeneratorTools.generateSets(columns));

        document.getRootElement().addElement(eleUpsert);
        logger.debug("itfsw(存在即更新插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加upsert实现方法。");

        // ====================================== 2. upsertSelective ======================================
        XmlElement eleUpsertSelective = new XmlElement("insert");
        eleUpsertSelective.addAttribute(new Attribute("id", METHOD_UPSERT_SELECTIVE));
        // 添加注释(!!!必须添加注释，overwrite覆盖生成时，@see XmlFileMergerJaxp.isGeneratedNode会去判断注释中是否存在OLD_ELEMENT_TAGS中的一点，例子：@mbg.generated)
        commentGenerator.addComment(eleUpsertSelective);

        // 参数类型
        eleUpsertSelective.addAttribute(new Attribute("parameterType", JavaElementGeneratorTools.getModelTypeWithoutBLOBs(introspectedTable).getFullyQualifiedName()));

        // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
        XmlElementGeneratorTools.useGeneratedKeys(eleUpsertSelective, introspectedTable);

        // insert
        eleUpsertSelective.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
        eleUpsertSelective.addElement(XmlElementGeneratorTools.generateKeysSelective(columns));
        eleUpsertSelective.addElement(new TextElement("values"));
        eleUpsertSelective.addElement(XmlElementGeneratorTools.generateValuesSelective(columns));
        eleUpsertSelective.addElement(new TextElement("on duplicate key update "));
        eleUpsertSelective.addElement(XmlElementGeneratorTools.generateSetsSelective(columns, null, false));

        document.getRootElement().addElement(eleUpsertSelective);
        logger.debug("itfsw(存在即更新插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加upsertSelective实现方法。");

        if (this.allowMultiQueries){
            // ====================================== 2. upsertByExample ======================================
            XmlElement eleUpsertByExample = new XmlElement("insert");
            eleUpsertByExample.addAttribute(new Attribute("id", METHOD_UPSERT_BY_EXAMPLE));
            // 参数类型
            eleUpsertByExample.addAttribute(new Attribute("parameterType", "map"));
            // 添加注释(!!!必须添加注释，overwrite覆盖生成时，@see XmlFileMergerJaxp.isGeneratedNode会去判断注释中是否存在OLD_ELEMENT_TAGS中的一点，例子：@mbg.generated)
            commentGenerator.addComment(eleUpsertByExample);

            // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
            XmlElementGeneratorTools.useGeneratedKeys(eleUpsertByExample, introspectedTable, "record.");

            // insert
            eleUpsertByExample.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
            eleUpsertByExample.addElement(XmlElementGeneratorTools.generateKeys(columns));
            this.generateExistsClause(introspectedTable, eleUpsertByExample, false, flag);

            // multiQueries
            eleUpsertByExample.addElement(new TextElement(";"));

            // update
            eleUpsertByExample.addElement(new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
            eleUpsertByExample.addElement(new TextElement("set"));
            eleUpsertByExample.addElement(XmlElementGeneratorTools.generateSets(ListUtilities.removeIdentityAndGeneratedAlwaysColumns(columns), "record."));
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
            commentGenerator.addComment(eleUpsertByExampleSelective);

            // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
            XmlElementGeneratorTools.useGeneratedKeys(eleUpsertByExampleSelective, introspectedTable, "record.");

            // insert
            eleUpsertByExampleSelective.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
            eleUpsertByExampleSelective.addElement(XmlElementGeneratorTools.generateKeysSelective(columns, "record."));
            this.generateExistsClause(introspectedTable, eleUpsertByExampleSelective, true, flag);

            // multiQueries
            eleUpsertByExampleSelective.addElement(new TextElement(";"));

            // update
            eleUpsertByExampleSelective.addElement(new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
            eleUpsertByExampleSelective.addElement(new TextElement("set"));
            eleUpsertByExampleSelective.addElement(XmlElementGeneratorTools.generateSetsSelective(ListUtilities.removeIdentityAndGeneratedAlwaysColumns(columns), "record."));

            // update where
            eleUpsertByExampleSelective.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));

            document.getRootElement().addElement(eleUpsertByExampleSelective);
            logger.debug("itfsw(存在即更新插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加upsertSelective实现方法。");
        }
    }

    /**
     * exists 语句
     *
     * @param introspectedTable
     * @param element
     * @param selective
     * @param allColumns
     */
    private void generateExistsClause(IntrospectedTable introspectedTable, XmlElement element, boolean selective, boolean allColumns){
        List<IntrospectedColumn> columns = allColumns ? introspectedTable.getAllColumns() : introspectedTable.getNonBLOBColumns();

        element.addElement(new TextElement("select"));
        if (selective){
            element.addElement(XmlElementGeneratorTools.generateValuesSelective(columns, "record.", false));
        } else {
            element.addElement(XmlElementGeneratorTools.generateValues(columns, "record.", false));
        }
        element.addElement(new TextElement("from dual where not exists"));
        element.addElement(new TextElement("("));
        element.addElement(new TextElement("select 1 from " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));

        // if example
        element.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));

        element.addElement(new TextElement(")"));
    }

}