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
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.*;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
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


    public static final String PRE_ALLOW_MULTI_QUERIES = "allowMultiQueries";   // property allowMultiQueries
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
        String allowMultiQueries = properties.getProperty(PRE_ALLOW_MULTI_QUERIES);
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
        Method mUpsertSelective = JavaElementGeneratorTools.generateMethod(
                METHOD_UPSERT_SELECTIVE,
                JavaVisibility.DEFAULT,
                FullyQualifiedJavaType.getIntInstance(),
                new Parameter(introspectedTable.getRules().calculateAllFieldsClass(), "record")
        );
        commentGenerator.addGeneralMethodComment(mUpsertSelective, introspectedTable);
        // interface 增加方法
        interfaze.addMethod(mUpsertSelective);
        logger.debug("itfsw(存在即更新插件):" + interfaze.getType().getShortName() + "增加upsertSelective方法。");

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
            // interface 增加方法
            interfaze.addMethod(mUpsertByExampleSelective);
            logger.debug("itfsw(存在即更新插件):" + interfaze.getType().getShortName() + "增加upsertByExampleSelective方法。");
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
        this.generateXmlElementWithoutBLOBs(document, introspectedTable);
        this.generateXmlElementWithSelective(document, introspectedTable);
        this.generateXmlElementWithBLOBs(document, introspectedTable);
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
        XmlElement eleUpsertSelective = new XmlElement("insert");
        eleUpsertSelective.addAttribute(new Attribute("id", METHOD_UPSERT_SELECTIVE));
        // 添加注释(!!!必须添加注释，overwrite覆盖生成时，@see XmlFileMergerJaxp.isGeneratedNode会去判断注释中是否存在OLD_ELEMENT_TAGS中的一点，例子：@mbg.generated)
        commentGenerator.addComment(eleUpsertSelective);

        // 参数类型
        eleUpsertSelective.addAttribute(new Attribute("parameterType", introspectedTable.getRules().calculateAllFieldsClass().getFullyQualifiedName()));

        // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
        XmlElementGeneratorTools.useGeneratedKeys(eleUpsertSelective, introspectedTable);

        // insert
        eleUpsertSelective.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
        eleUpsertSelective.addElement(XmlElementGeneratorTools.generateKeysSelective(columns));
        eleUpsertSelective.addElement(new TextElement("values"));
        eleUpsertSelective.addElement(XmlElementGeneratorTools.generateValuesSelective(columns));
        eleUpsertSelective.addElement(new TextElement("on duplicate key update "));
        // set 操作增加增量插件支持
        this.incrementsSelectiveSupport(eleUpsertSelective, XmlElementGeneratorTools.generateSetsSelective(columns, null, false), introspectedTable, false);

        document.getRootElement().addElement(eleUpsertSelective);
        logger.debug("itfsw(存在即更新插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加upsertSelective实现方法。");
        if (this.allowMultiQueries) {
            // ====================================== upsertByExampleSelective ======================================
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
            this.generateExistsClause(introspectedTable, eleUpsertByExampleSelective, true, columns);

            // multiQueries
            eleUpsertByExampleSelective.addElement(new TextElement(";"));

            // update
            eleUpsertByExampleSelective.addElement(new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
            eleUpsertByExampleSelective.addElement(new TextElement("set"));
            // set 操作增加增量插件支持
            this.incrementsSelectiveSupport(eleUpsertByExampleSelective, XmlElementGeneratorTools.generateSetsSelective(ListUtilities.removeIdentityAndGeneratedAlwaysColumns(columns), "record."), introspectedTable, true);

            // update where
            eleUpsertByExampleSelective.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));

            document.getRootElement().addElement(eleUpsertByExampleSelective);
            logger.debug("itfsw(存在即更新插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加upsertSelective实现方法。");
        }
    }

    /**
     * 当Model有生成WithBLOBs类时的情况
     * @param document
     * @param introspectedTable
     */
    private void generateXmlElementWithBLOBs(Document document, IntrospectedTable introspectedTable) {
        if (introspectedTable.hasBLOBColumns()){
            List<IntrospectedColumn> columns = ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getAllColumns());
            // ====================================== upsertWithBLOBs ======================================
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
            for (Element element : XmlElementGeneratorTools.generateKeys(columns)) {
                eleUpsertWithBLOBs.addElement(element);
            }
            eleUpsertWithBLOBs.addElement(new TextElement("values"));
            for (Element element : XmlElementGeneratorTools.generateValues(columns)) {
                eleUpsertWithBLOBs.addElement(element);
            }
            eleUpsertWithBLOBs.addElement(new TextElement("on duplicate key update "));
            // set 操作增加增量插件支持
            this.incrementsSupport(eleUpsertWithBLOBs, XmlElementGeneratorTools.generateSets(columns), introspectedTable, false);

            document.getRootElement().addElement(eleUpsertWithBLOBs);
            logger.debug("itfsw(存在即更新插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加upsert实现方法。");

            if (this.allowMultiQueries) {
                // ====================================== upsertByExampleWithBLOBs ======================================
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
                for (Element element : XmlElementGeneratorTools.generateKeys(columns)) {
                    eleUpsertByExampleWithBLOBs.addElement(element);
                }
                this.generateExistsClause(introspectedTable, eleUpsertByExampleWithBLOBs, false, columns);

                // multiQueries
                eleUpsertByExampleWithBLOBs.addElement(new TextElement(";"));

                // update
                eleUpsertByExampleWithBLOBs.addElement(new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
                eleUpsertByExampleWithBLOBs.addElement(new TextElement("set"));
                // set 操作增加增量插件支持
                this.incrementsSupport(eleUpsertByExampleWithBLOBs, XmlElementGeneratorTools.generateSets(columns, "record."), introspectedTable, true);

                // update where
                eleUpsertByExampleWithBLOBs.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));

                document.getRootElement().addElement(eleUpsertByExampleWithBLOBs);
                logger.debug("itfsw(存在即更新插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加upsertSelective实现方法。");
            }
        }
    }

    /**
     * 当Model没有生成WithBLOBs类时的情况
     * @param document
     * @param introspectedTable
     */
    private void generateXmlElementWithoutBLOBs(Document document, IntrospectedTable introspectedTable) {
        List<IntrospectedColumn> columns = ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getNonBLOBColumns());

        // ====================================== upsert ======================================
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
        for (Element element : XmlElementGeneratorTools.generateKeys(columns)) {
            eleUpsert.addElement(element);
        }
        eleUpsert.addElement(new TextElement("values"));
        for (Element element : XmlElementGeneratorTools.generateValues(columns)) {
            eleUpsert.addElement(element);
        }
        eleUpsert.addElement(new TextElement("on duplicate key update "));
        // set 操作增加增量插件支持
        this.incrementsSupport(eleUpsert, XmlElementGeneratorTools.generateSets(columns), introspectedTable, false);


        document.getRootElement().addElement(eleUpsert);
        logger.debug("itfsw(存在即更新插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加upsert实现方法。");

        if (this.allowMultiQueries) {
            // ====================================== upsertByExample ======================================
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
            for (Element element : XmlElementGeneratorTools.generateKeys(columns)) {
                eleUpsertByExample.addElement(element);
            }
            this.generateExistsClause(introspectedTable, eleUpsertByExample, false, columns);

            // multiQueries
            eleUpsertByExample.addElement(new TextElement(";"));

            // update
            eleUpsertByExample.addElement(new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
            eleUpsertByExample.addElement(new TextElement("set"));
            // set 操作增加增量插件支持
            this.incrementsSupport(eleUpsertByExample, XmlElementGeneratorTools.generateSets(ListUtilities.removeIdentityAndGeneratedAlwaysColumns(columns), "record."), introspectedTable, true);

            // update where
            eleUpsertByExample.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));

            document.getRootElement().addElement(eleUpsertByExample);
            logger.debug("itfsw(存在即更新插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加upsertSelective实现方法。");
        }
    }

    /**
     * exists 语句
     * +635
     * @param introspectedTable
     * @param element
     * @param selective
     * @param columns
     */
    private void generateExistsClause(IntrospectedTable introspectedTable, XmlElement element, boolean selective, List<IntrospectedColumn> columns) {
        element.addElement(new TextElement("select"));
        if (selective) {
            element.addElement(XmlElementGeneratorTools.generateValuesSelective(columns, "record.", false));
        } else {
            for (Element element1 : XmlElementGeneratorTools.generateValues(columns, "record.", false)) {
                element.addElement(element1);
            }
        }
        element.addElement(new TextElement("from dual where not exists"));
        element.addElement(new TextElement("("));
        element.addElement(new TextElement("select 1 from " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));

        // if example
        element.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));

        element.addElement(new TextElement(")"));
    }

    /**
     * 增量操作支持
     * @param xmlElement
     * @param trimXmlElement
     * @param introspectedTable
     * @param hasPrefix
     */
    private void incrementsSelectiveSupport(XmlElement xmlElement, XmlElement trimXmlElement, IntrospectedTable introspectedTable, boolean hasPrefix) {
        IncrementsPluginTools incTools = IncrementsPluginTools.getTools(context, introspectedTable, warnings);
        if (incTools.support()) {
            List<Element> ifs = new ArrayList<>();
            // 获取if节点
            for (Element element : trimXmlElement.getElements()) {
                String text = ((TextElement) (((XmlElement) element).getElements().get(0))).getContent();
                String columnName = text.split("=")[0];
                IntrospectedColumn introspectedColumn = IntrospectedTableTools.safeGetColumn(introspectedTable, columnName);
                if (incTools.supportColumn(introspectedColumn)) {
                    // if 节点数据替换
                    ((XmlElement) element).getElements().clear();
                    ((XmlElement) element).getElements().addAll(incTools.generatedIncrementsElement(introspectedColumn, hasPrefix, true));
                    continue;
                }
                ifs.add(element);
            }
        }
        xmlElement.addElement(trimXmlElement);
    }

    /**
     * 增量操作支持
     * @param xmlElement
     * @param elements
     * @param introspectedTable
     * @param hasPrefix
     */
    private void incrementsSupport(XmlElement xmlElement, List<TextElement> elements, IntrospectedTable introspectedTable, boolean hasPrefix) {
        IncrementsPluginTools incTools = IncrementsPluginTools.getTools(context, introspectedTable, warnings);
        for (TextElement element : elements) {
            if (incTools.support()) {
                // 获取column
                String text = element.getContent().trim();
                String columnName = text.split("=")[0];
                IntrospectedColumn introspectedColumn = IntrospectedTableTools.safeGetColumn(introspectedTable, columnName);
                if (incTools.supportColumn(introspectedColumn)) {
                    xmlElement.getElements().addAll(incTools.generatedIncrementsElement(introspectedColumn, hasPrefix, text.endsWith(",")));
                    continue;
                }
            }
            xmlElement.addElement(element);
        }
    }
}