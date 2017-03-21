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
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.GeneratedKey;
import org.mybatis.generator.config.PluginConfiguration;
import org.mybatis.generator.internal.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * ---------------------------------------------------------------------------
 * 批量插入插件
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/1/13 9:33
 * ---------------------------------------------------------------------------
 */
public class BatchInsertPlugin extends PluginAdapter {
    private static final Logger logger = LoggerFactory.getLogger(BatchInsertPlugin.class);
    public static final String METHOD_BATCH_INSERT = "batchInsert";  // 方法名
    public static final String METHOD_BATCH_INSERT_SELECTIVE = "batchInsertSelective";  // 方法名

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

        // 插件使用前提是数据库为MySQL或者SQLserver，因为返回主键使用了JDBC的getGenereatedKeys方法获取主键
        if ("com.mysql.jdbc.Driver".equalsIgnoreCase(this.getContext().getJdbcConnectionConfiguration().getDriverClass()) == false
                && "com.microsoft.jdbc.sqlserver.SQLServer".equalsIgnoreCase(this.getContext().getJdbcConnectionConfiguration().getDriverClass()) == false
                && "com.microsoft.sqlserver.jdbc.SQLServerDriver".equalsIgnoreCase(this.getContext().getJdbcConnectionConfiguration().getDriverClass()) == false){
            logger.warn("itfsw:插件" + this.getClass().getTypeName() + "插件使用前提是数据库为MySQL或者SQLserver，因为返回主键使用了JDBC的getGenereatedKeys方法获取主键！");
            return false;
        }


        // 插件使用前提是使用了ModelColumnPlugin插件
        try {
            Context ctx = getContext();
            // 利用反射获取pluginConfigurations属性
            java.lang.reflect.Field field = Context.class.getDeclaredField("pluginConfigurations");
            field.setAccessible(true);
            List<PluginConfiguration> list = (List<PluginConfiguration>) field.get(ctx);
            // 检查是否配置了ModelColumnPlugin插件
            boolean unFind = true;
            for (PluginConfiguration config: list) {
                if (ModelColumnPlugin.class.getName().equals(config.getConfigurationType())){
                    unFind = false;
                }
            }

            // 建议使用ModelColumnPlugin插件
            if (unFind){
                logger.warn("itfsw:插件" + this.getClass().getTypeName() + "插件需配合com.itfsw.mybatis.generator.plugins.ModelColumnPlugin插件使用！");
                return false;
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
        }

        // TODO
        logger.warn("itfsw:插件" + this.getClass().getTypeName() + "插件如之前使用1.0.5-的插件可继续使用com.itfsw.mybatis.generator.plugins.BatchInsertOldPlugin或者修改batchInsert(@Param(\"list\") List<Tb> list, @Param(\"insertColumns\") Tb.Column ... selective)调用为batchInsertSelective！");

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
        // 1. batchInsert
        Method mBatchInsert = new Method(METHOD_BATCH_INSERT);
        mBatchInsert.setReturnType(FullyQualifiedJavaType.getIntInstance());
        // 添加参数
        FullyQualifiedJavaType tList = FullyQualifiedJavaType.getNewListInstance();
        tList.addTypeArgument(introspectedTable.getRules().calculateAllFieldsClass());
        mBatchInsert.addParameter(new Parameter(tList, "list", "@Param(\"list\")"));
        // 添加方法说明
        CommentTools.addGeneralMethodComment(mBatchInsert, introspectedTable);
        // interface 增加方法
        interfaze.addMethod(mBatchInsert);
        logger.debug("itfsw(批量插入插件):" + interfaze.getType().getShortName() + "增加batchInsert方法。");

        // 2. batchInsertSelective
        Method mBatchInsertSelective = new Method(METHOD_BATCH_INSERT_SELECTIVE);
        mBatchInsertSelective.setReturnType(FullyQualifiedJavaType.getIntInstance());
        // 添加参数
        FullyQualifiedJavaType tSelective = new FullyQualifiedJavaType(introspectedTable.getRules().calculateAllFieldsClass().getShortName()+"."+ModelColumnPlugin.ENUM_NAME);
        mBatchInsertSelective.addParameter(new Parameter(tList, "list", "@Param(\"list\")"));
        mBatchInsertSelective.addParameter(new Parameter(tSelective, "selective", "@Param(\"selective\")", true));
        // 添加方法说明
        CommentTools.addGeneralMethodComment(mBatchInsertSelective, introspectedTable);
        // interface 增加方法
        interfaze.addMethod(mBatchInsertSelective);
        logger.debug("itfsw(批量插入插件):" + interfaze.getType().getShortName() + "增加batchInsertSelective方法。");

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
        // 1. batchInsert
        XmlElement batchInsertEle = new XmlElement("insert");
        batchInsertEle.addAttribute(new Attribute("id", METHOD_BATCH_INSERT));
        // 参数类型
        batchInsertEle.addAttribute(new Attribute("parameterType", "map"));
        // 添加注释(!!!必须添加注释，overwrite覆盖生成时，@see XmlFileMergerJaxp.isGeneratedNode会去判断注释中是否存在OLD_ELEMENT_TAGS中的一点，例子：@mbg.generated)
        CommentTools.addComment(batchInsertEle);

        StringBuilder insertClause = new StringBuilder();
        StringBuilder valuesClause = new StringBuilder();

        insertClause.append("insert into "); //$NON-NLS-1$
        insertClause.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        insertClause.append(" ("); //$NON-NLS-1$

        valuesClause.append(" ("); //$NON-NLS-1$

        List<String> valuesClauses = new ArrayList<String>();
        List<IntrospectedColumn> columns = ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns());
        for (int i = 0; i < columns.size(); i++) {
            IntrospectedColumn introspectedColumn = columns.get(i);

            insertClause.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));

            // 生成foreach下插入values
            valuesClause.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, "item."));
            if (i + 1 < columns.size()) {
                insertClause.append(", "); //$NON-NLS-1$
                valuesClause.append(", "); //$NON-NLS-1$
            }
        }

        insertClause.append(')');
        batchInsertEle.addElement(new TextElement(insertClause.toString()));

        valuesClause.append(')');
        valuesClauses.add(valuesClause.toString());


        // 添加foreach节点
        XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("collection", "list"));
        foreachElement.addAttribute(new Attribute("item", "item"));
        foreachElement.addAttribute(new Attribute("separator", ","));

        for (String clause : valuesClauses) {
            foreachElement.addElement(new TextElement(clause));
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
        CommentTools.addComment(element);

        GeneratedKey gk = introspectedTable.getGeneratedKey();
        if (gk != null) {
            IntrospectedColumn introspectedColumn = introspectedTable.getColumn(gk.getColumn());
            // if the column is null, then it's a configuration error. The
            // warning has already been reported
            if (introspectedColumn != null) {
                // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
                element.addAttribute(new Attribute("useGeneratedKeys", "true")); //$NON-NLS-1$ //$NON-NLS-2$
                element.addAttribute(new Attribute("keyProperty", introspectedColumn.getJavaProperty())); //$NON-NLS-1$
                element.addAttribute(new Attribute("keyColumn", introspectedColumn.getActualColumnName())); //$NON-NLS-1$
            }
        }

        element.addElement(new TextElement("insert into "+introspectedTable.getFullyQualifiedTableNameAtRuntime()+" ("));

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
        List<IntrospectedColumn> columns1 = ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns());
        for (int i = 0; i < columns1.size(); i++) {
            IntrospectedColumn introspectedColumn = columns.get(i);
            XmlElement check = new XmlElement("if");
            check.addAttribute(new Attribute("test", "'"+introspectedColumn.getActualColumnName()+"' == column.value"));
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