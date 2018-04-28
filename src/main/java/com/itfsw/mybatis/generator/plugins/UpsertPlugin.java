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
import com.itfsw.mybatis.generator.plugins.utils.PluginTools;
import com.itfsw.mybatis.generator.plugins.utils.XmlElementGeneratorTools;
import com.itfsw.mybatis.generator.plugins.utils.hook.IUpsertPluginHook;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.*;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.Arrays;
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
    public static final String METHOD_UPSERT_WITH_BLOBS = "upsertWithBLOBs";  // 方法名
    public static final String METHOD_UPSERT_SELECTIVE = "upsertSelective";  // 方法名

    public static final String METHOD_UPSERT_BY_EXAMPLE = "upsertByExample";   // 方法名
    public static final String METHOD_UPSERT_BY_EXAMPLE_WITH_BLOBS = "upsertByExampleWithBLOBs";   // 方法名
    public static final String METHOD_UPSERT_BY_EXAMPLE_SELECTIVE = "upsertByExampleSelective";   // 方法名


    public static final String PRO_ALLOW_MULTI_QUERIES = "allowMultiQueries";   // property allowMultiQueries
    private boolean allowMultiQueries = false;  // 是否允许多sql提交

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(List<String> warnings) {

        // 插件使用前提是数据库为MySQL
        if ("com.mysql.jdbc.Driver".equalsIgnoreCase(this.getContext().getJdbcConnectionConfiguration().getDriverClass()) == false) {
            warnings.add("itfsw:插件" + this.getClass().getTypeName() + "插件使用前提是数据库为MySQL！");
            return false;
        }

        // 插件是否开启了多sql提交
        Properties properties = this.getProperties();
        String allowMultiQueries = properties.getProperty(PRO_ALLOW_MULTI_QUERIES);
        this.allowMultiQueries = allowMultiQueries == null ? false : StringUtility.isTrue(allowMultiQueries);
        if (this.allowMultiQueries) {
            // 提示用户注意信息
            warnings.add("itfsw:插件" + this.getClass().getTypeName() + "插件您开启了allowMultiQueries支持，注意在jdbc url 配置中增加“allowMultiQueries=true”支持（不怎么建议使用该功能，开启多sql提交会增加sql注入的风险，请确保你所有sql都使用MyBatis书写，请不要使用statement进行sql提交）！");
        }

        return super.validate(warnings);
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
        // ====================================== upsert ======================================
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

        // ====================================== upsertWithBLOBs ======================================
        // !!! 注意这里的行为不以有没有生成Model 的 WithBLOBs类为基准
        if (introspectedTable.hasBLOBColumns()) {
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
        }

        // ====================================== upsertSelective ======================================
        // 找出全字段对应的Model
        FullyQualifiedJavaType fullFieldModel = introspectedTable.getRules().calculateAllFieldsClass();
        Method mUpsertSelective = JavaElementGeneratorTools.generateMethod(
                METHOD_UPSERT_SELECTIVE,
                JavaVisibility.DEFAULT,
                FullyQualifiedJavaType.getIntInstance(),
                new Parameter(fullFieldModel, "record")
        );
        commentGenerator.addGeneralMethodComment(mUpsertSelective, introspectedTable);
        // hook
        if (PluginTools.getHook(IUpsertPluginHook.class).clientUpsertSelectiveMethodGenerated(mUpsertSelective, interfaze, introspectedTable)) {
            // interface 增加方法
            interfaze.addMethod(mUpsertSelective);
            logger.debug("itfsw(存在即更新插件):" + interfaze.getType().getShortName() + "增加upsertSelective方法。");
        }

        if (this.allowMultiQueries) {
            // ====================================== upsertByExample ======================================
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

            // ====================================== upsertByExampleWithBLOBs ======================================
            // !!! 注意这里的行为不以有没有生成Model 的 WithBLOBs类为基准
            if (introspectedTable.hasBLOBColumns()) {
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
            }

            // ====================================== upsertByExampleSelective ======================================
            Method mUpsertByExampleSelective = JavaElementGeneratorTools.generateMethod(
                    METHOD_UPSERT_BY_EXAMPLE_SELECTIVE,
                    JavaVisibility.DEFAULT,
                    FullyQualifiedJavaType.getIntInstance(),
                    new Parameter(introspectedTable.getRules().calculateAllFieldsClass(), "record", "@Param(\"record\")"),
                    new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example", "@Param(\"example\")")
            );
            commentGenerator.addGeneralMethodComment(mUpsertByExampleSelective, introspectedTable);
            // hook
            if (PluginTools.getHook(IUpsertPluginHook.class).clientUpsertByExampleSelectiveMethodGenerated(mUpsertByExampleSelective, interfaze, introspectedTable)) {
                // interface 增加方法
                interfaze.addMethod(mUpsertByExampleSelective);
                logger.debug("itfsw(存在即更新插件):" + interfaze.getType().getShortName() + "增加upsertByExampleSelective方法。");
            }
        }

        return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
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
        this.generateXmlElementWithSelective(document, introspectedTable);
        this.generateXmlElement(document, introspectedTable, false);
        if (introspectedTable.hasBLOBColumns()) {
            this.generateXmlElement(document, introspectedTable, true);
        }

        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    /**
     * 当Selective情况
     * @param document
     * @param introspectedTable
     */
    private void generateXmlElementWithSelective(Document document, IntrospectedTable introspectedTable) {
        List<IntrospectedColumn> columns = ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getAllColumns());

        // ====================================== upsertSelective ======================================
        XmlElement insertEle = new XmlElement("insert");
        insertEle.addAttribute(new Attribute("id", METHOD_UPSERT_SELECTIVE));
        // 添加注释(!!!必须添加注释，overwrite覆盖生成时，@see XmlFileMergerJaxp.isGeneratedNode会去判断注释中是否存在OLD_ELEMENT_TAGS中的一点，例子：@mbg.generated)
        commentGenerator.addComment(insertEle);

        // 参数类型
        insertEle.addAttribute(new Attribute("parameterType", introspectedTable.getRules().calculateAllFieldsClass().getFullyQualifiedName()));

        // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
        XmlElementGeneratorTools.useGeneratedKeys(insertEle, introspectedTable);

        // insert
        insertEle.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
        XmlElement insertColumnsEle = XmlElementGeneratorTools.generateKeysSelective(columns);
        insertEle.addElement(insertColumnsEle);
        insertEle.addElement(new TextElement("values"));
        XmlElement insertValuesEle = XmlElementGeneratorTools.generateValuesSelective(columns);
        insertEle.addElement(insertValuesEle);
        insertEle.addElement(new TextElement("on duplicate key update "));
        // set
        XmlElement setsEle = XmlElementGeneratorTools.generateSetsSelective(columns);
        insertEle.addElement(setsEle);

        // hook
        if (PluginTools.getHook(IUpsertPluginHook.class).sqlMapUpsertSelectiveElementGenerated(insertEle, columns, insertColumnsEle, insertValuesEle, setsEle, introspectedTable)) {
            document.getRootElement().addElement(insertEle);
            logger.debug("itfsw(存在即更新插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加upsertSelective实现方法。");
        }

        if (this.allowMultiQueries) {
            // ====================================== upsertByExampleSelective ======================================
            XmlElement updateEle = new XmlElement("update");
            updateEle.addAttribute(new Attribute("id", METHOD_UPSERT_BY_EXAMPLE_SELECTIVE));
            // 参数类型
            updateEle.addAttribute(new Attribute("parameterType", "map"));
            // 添加注释(!!!必须添加注释，overwrite覆盖生成时，@see XmlFileMergerJaxp.isGeneratedNode会去判断注释中是否存在OLD_ELEMENT_TAGS中的一点，例子：@mbg.generated)
            commentGenerator.addComment(updateEle);

            // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
            // XmlElementGeneratorTools.useGeneratedKeys(updateEle, introspectedTable, "record.");
            // upsertByExample 先执行的update，所以没法获取id

            // update
            updateEle.addElement(new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
            updateEle.addElement(new TextElement("set"));
            // set 操作增加增量插件支持
            setsEle = XmlElementGeneratorTools.generateSetsSelective(columns, "record.");
            updateEle.addElement(setsEle);

            // update where
            updateEle.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));

            // multiQueries
            updateEle.addElement(new TextElement(";"));

            // insert
            updateEle.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
            insertColumnsEle = XmlElementGeneratorTools.generateKeysSelective(columns, "record.");
            updateEle.addElement(insertColumnsEle);

            insertValuesEle = XmlElementGeneratorTools.generateValuesSelective(columns, "record.", false);
            // 查询值
            this.generateExistsClause(introspectedTable, updateEle, Arrays.asList(insertValuesEle));

            // hook
            if (PluginTools.getHook(IUpsertPluginHook.class).sqlMapUpsertByExampleSelectiveElementGenerated(updateEle, columns, insertColumnsEle, insertValuesEle, setsEle, introspectedTable)) {
                document.getRootElement().addElement(updateEle);
                logger.debug("itfsw(存在即更新插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加upsertSelective实现方法。");
            }
        }
    }

    /**
     * 生成xml
     * @param document
     * @param introspectedTable
     * @param withBLOBs
     */
    private void generateXmlElement(Document document, IntrospectedTable introspectedTable, boolean withBLOBs) {
        List<IntrospectedColumn> columns = ListUtilities.removeGeneratedAlwaysColumns(withBLOBs ? introspectedTable.getAllColumns() : introspectedTable.getNonBLOBColumns());
        // ====================================== upsert ======================================
        XmlElement insertEle = new XmlElement("insert");
        insertEle.addAttribute(new Attribute("id", withBLOBs ? METHOD_UPSERT_WITH_BLOBS : METHOD_UPSERT));
        // 添加注释(!!!必须添加注释，overwrite覆盖生成时，@see XmlFileMergerJaxp.isGeneratedNode会去判断注释中是否存在OLD_ELEMENT_TAGS中的一点，例子：@mbg.generated)
        commentGenerator.addComment(insertEle);

        // 参数类型
        insertEle.addAttribute(new Attribute("parameterType",
                withBLOBs ? JavaElementGeneratorTools.getModelTypeWithBLOBs(introspectedTable).getFullyQualifiedName() : JavaElementGeneratorTools.getModelTypeWithoutBLOBs(introspectedTable).getFullyQualifiedName()
        ));

        // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
        XmlElementGeneratorTools.useGeneratedKeys(insertEle, introspectedTable);

        // insert
        insertEle.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
        for (Element element : XmlElementGeneratorTools.generateKeys(columns)) {
            insertEle.addElement(element);
        }
        insertEle.addElement(new TextElement("values"));
        for (Element element : XmlElementGeneratorTools.generateValues(columns)) {
            insertEle.addElement(element);
        }
        insertEle.addElement(new TextElement("on duplicate key update "));
        // set
        for (Element set : XmlElementGeneratorTools.generateSets(columns)) {
            insertEle.addElement(set);
        }

        document.getRootElement().addElement(insertEle);
        logger.debug("itfsw(存在即更新插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加upsert实现方法。");

        if (this.allowMultiQueries) {
            // ====================================== upsertByExample ======================================
            XmlElement updateEle = new XmlElement("update");
            updateEle.addAttribute(new Attribute("id", withBLOBs ? METHOD_UPSERT_BY_EXAMPLE_WITH_BLOBS : METHOD_UPSERT_BY_EXAMPLE));
            // 参数类型
            updateEle.addAttribute(new Attribute("parameterType", "map"));
            // 添加注释(!!!必须添加注释，overwrite覆盖生成时，@see XmlFileMergerJaxp.isGeneratedNode会去判断注释中是否存在OLD_ELEMENT_TAGS中的一点，例子：@mbg.generated)
            commentGenerator.addComment(updateEle);

            // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
            // XmlElementGeneratorTools.useGeneratedKeys(updateEle, introspectedTable, "record.");
            // upsertByExample 先执行的update，所以没法获取id

            // update
            updateEle.addElement(new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
            updateEle.addElement(new TextElement("set"));
            // set
            for (Element set : XmlElementGeneratorTools.generateSets(columns, "record.")) {
                updateEle.addElement(set);
            }

            // update where
            updateEle.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));

            // multiQueries
            updateEle.addElement(new TextElement(";"));

            // insert
            updateEle.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
            for (Element element : XmlElementGeneratorTools.generateKeys(columns)) {
                updateEle.addElement(element);
            }
            this.generateExistsClause(introspectedTable, updateEle, XmlElementGeneratorTools.generateValues(columns, "record.", false));

            document.getRootElement().addElement(updateEle);
            logger.debug("itfsw(存在即更新插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加upsertByExample实现方法。");
        }
    }

    /**
     * exists 语句
     * +635
     * @param introspectedTable
     * @param element
     * @param values
     */
    private void generateExistsClause(IntrospectedTable introspectedTable, XmlElement element, List<Element> values) {
        element.addElement(new TextElement("select"));

        for (Element value : values) {
            element.addElement(value);
        }

        element.addElement(new TextElement("from dual where not exists"));
        element.addElement(new TextElement("("));
        element.addElement(new TextElement("select 1 from " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));

        // if example
        element.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));

        element.addElement(new TextElement(")"));
    }
}