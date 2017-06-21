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
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.*;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

import java.util.List;

/**
 * ---------------------------------------------------------------------------
 * 批量插入插件
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/1/13 9:33
 * ---------------------------------------------------------------------------
 */
public class BatchInsertPlugin extends BasePlugin {
    public static final String METHOD_BATCH_INSERT = "batchInsert";  // 方法名
    public static final String METHOD_BATCH_INSERT_SELECTIVE = "batchInsertSelective";  // 方法名

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(List<String> warnings) {

        // 插件使用前提是数据库为MySQL或者SQLserver，因为返回主键使用了JDBC的getGenereatedKeys方法获取主键
        if ("com.mysql.jdbc.Driver".equalsIgnoreCase(this.getContext().getJdbcConnectionConfiguration().getDriverClass()) == false
                && "com.microsoft.jdbc.sqlserver.SQLServer".equalsIgnoreCase(this.getContext().getJdbcConnectionConfiguration().getDriverClass()) == false
                && "com.microsoft.sqlserver.jdbc.SQLServerDriver".equalsIgnoreCase(this.getContext().getJdbcConnectionConfiguration().getDriverClass()) == false) {
            logger.error("itfsw:插件" + this.getClass().getTypeName() + "插件使用前提是数据库为MySQL或者SQLserver，因为返回主键使用了JDBC的getGenereatedKeys方法获取主键！");
            return false;
        }


        // 插件使用前提是使用了ModelColumnPlugin插件
        if (!PluginTools.checkDependencyPlugin(getContext(), ModelColumnPlugin.class)) {
            logger.error("itfsw:插件" + this.getClass().getTypeName() + "插件需配合com.itfsw.mybatis.generator.plugins.ModelColumnPlugin插件使用！");
            return false;
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
        // 1. batchInsert
        FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
        listType.addTypeArgument(introspectedTable.getRules().calculateAllFieldsClass());
        Method mBatchInsert = JavaElementGeneratorTools.generateMethod(
                METHOD_BATCH_INSERT,
                JavaVisibility.DEFAULT,
                FullyQualifiedJavaType.getIntInstance(),
                new Parameter(listType, "list", "@Param(\"list\")")

        );
        commentGenerator.addGeneralMethodComment(mBatchInsert, introspectedTable);
        // interface 增加方法
        interfaze.addMethod(mBatchInsert);
        logger.debug("itfsw(批量插入插件):" + interfaze.getType().getShortName() + "增加batchInsert方法。");

        // 2. batchInsertSelective
        FullyQualifiedJavaType selectiveType = new FullyQualifiedJavaType(introspectedTable.getRules().calculateAllFieldsClass().getShortName() + "." + ModelColumnPlugin.ENUM_NAME);
        Method mBatchInsertSelective = JavaElementGeneratorTools.generateMethod(
                METHOD_BATCH_INSERT_SELECTIVE,
                JavaVisibility.DEFAULT,
                FullyQualifiedJavaType.getIntInstance(),
                new Parameter(listType, "list", "@Param(\"list\")"),
                new Parameter(selectiveType, "selective", "@Param(\"selective\")", true)
        );
        commentGenerator.addGeneralMethodComment(mBatchInsertSelective, introspectedTable);
        // interface 增加方法
        interfaze.addMethod(mBatchInsertSelective);
        logger.debug("itfsw(批量插入插件):" + interfaze.getType().getShortName() + "增加batchInsertSelective方法。");

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
        // 1. batchInsert
        XmlElement batchInsertEle = new XmlElement("insert");
        batchInsertEle.addAttribute(new Attribute("id", METHOD_BATCH_INSERT));
        // 参数类型
        batchInsertEle.addAttribute(new Attribute("parameterType", "map"));
        // 添加注释(!!!必须添加注释，overwrite覆盖生成时，@see XmlFileMergerJaxp.isGeneratedNode会去判断注释中是否存在OLD_ELEMENT_TAGS中的一点，例子：@mbg.generated)
        commentGenerator.addComment(batchInsertEle);

        // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
        XmlElementGeneratorTools.useGeneratedKeys(batchInsertEle, introspectedTable);

        batchInsertEle.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
        for (Element element : XmlElementGeneratorTools.generateKeys(ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns()))) {
            batchInsertEle.addElement(element);
        }

        // 添加foreach节点
        XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("collection", "list"));
        foreachElement.addAttribute(new Attribute("item", "item"));
        foreachElement.addAttribute(new Attribute("separator", ","));

        for (Element element : XmlElementGeneratorTools.generateValues(ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns()), "item.")) {
            foreachElement.addElement(element);
        }

        // values 构建
        batchInsertEle.addElement(new TextElement("values"));
        batchInsertEle.addElement(foreachElement);
        if (context.getPlugins().sqlMapInsertElementGenerated(batchInsertEle, introspectedTable)) {
            document.getRootElement().addElement(batchInsertEle);
            logger.debug("itfsw(批量插入插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加batchInsert实现方法。");
        }

        // 2. batchInsertSelective
        XmlElement element = new XmlElement("insert");
        element.addAttribute(new Attribute("id", METHOD_BATCH_INSERT_SELECTIVE));
        // 参数类型
        element.addAttribute(new Attribute("parameterType", "map"));
        // 添加注释(!!!必须添加注释，overwrite覆盖生成时，@see XmlFileMergerJaxp.isGeneratedNode会去判断注释中是否存在OLD_ELEMENT_TAGS中的一点，例子：@mbg.generated)
        commentGenerator.addComment(element);

        // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
        XmlElementGeneratorTools.useGeneratedKeys(element, introspectedTable);

        element.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime() + " ("));

        XmlElement foreachInsertColumns = new XmlElement("foreach");
        foreachInsertColumns.addAttribute(new Attribute("collection", "selective"));
        foreachInsertColumns.addAttribute(new Attribute("item", "column"));
        foreachInsertColumns.addAttribute(new Attribute("separator", ","));
        foreachInsertColumns.addElement(new TextElement("${column.value}"));

        element.addElement(foreachInsertColumns);

        element.addElement(new TextElement(")"));

        // values
        element.addElement(new TextElement("values"));

        // foreach values
        XmlElement foreachValues = new XmlElement("foreach");
        foreachValues.addAttribute(new Attribute("collection", "list"));
        foreachValues.addAttribute(new Attribute("item", "item"));
        foreachValues.addAttribute(new Attribute("separator", ","));

        foreachValues.addElement(new TextElement("("));

        // foreach 所有插入的列，比较是否存在
        XmlElement foreachInsertColumnsCheck = new XmlElement("foreach");
        foreachInsertColumnsCheck.addAttribute(new Attribute("collection", "selective"));
        foreachInsertColumnsCheck.addAttribute(new Attribute("item", "column"));
        foreachInsertColumnsCheck.addAttribute(new Attribute("separator", ","));

        // 所有表字段
        List<IntrospectedColumn> columns = ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns());
        List<IntrospectedColumn> columns1 = ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns());
        for (int i = 0; i < columns1.size(); i++) {
            IntrospectedColumn introspectedColumn = columns.get(i);
            XmlElement check = new XmlElement("if");
            check.addAttribute(new Attribute("test", "'" + introspectedColumn.getActualColumnName() + "' == column.value"));
            check.addElement(new TextElement(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, "item.")));

            foreachInsertColumnsCheck.addElement(check);
        }
        foreachValues.addElement(foreachInsertColumnsCheck);

        foreachValues.addElement(new TextElement(")"));

        element.addElement(foreachValues);

        if (context.getPlugins().sqlMapInsertElementGenerated(element, introspectedTable)) {
            document.getRootElement().addElement(element);
            logger.debug("itfsw(批量插入插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加batchInsertSelective实现方法。");
        }

        return true;
    }

}