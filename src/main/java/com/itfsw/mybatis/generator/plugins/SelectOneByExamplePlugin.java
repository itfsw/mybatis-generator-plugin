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
import org.mybatis.generator.internal.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * ---------------------------------------------------------------------------
 * 增加查询一条数据方法
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2016/12/28 14:56
 * ---------------------------------------------------------------------------
 */
public class SelectOneByExamplePlugin extends PluginAdapter {
    public static final String METHOD_NAME = "selectOneByExample";  // 方法名
    private static final Logger logger = LoggerFactory.getLogger(SelectOneByExamplePlugin.class);

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
        // 方法生成
        Method method = new Method(METHOD_NAME);
        // 方法可见性 interface会忽略
        // method.setVisibility(JavaVisibility.PUBLIC);
        // 返回值类型
        FullyQualifiedJavaType returnType = introspectedTable.getRules().calculateAllFieldsClass();
        method.setReturnType(returnType);
        // 添加参数
        FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        method.addParameter(new Parameter(type, "example"));
        // 添加方法说明
        CommentTools.addGeneralMethodComment(method, introspectedTable);

        // interface 增加方法
        interfaze.addMethod(method);
        logger.debug("itfsw(查询单条数据插件):"+interfaze.getType().getShortName()+"增加SelectOneByExample方法。");
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
        //数据库表名
        String tableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();

        // 生成查询语句
        XmlElement selectOneElement = new XmlElement("select");
        // 添加注释(!!!必须添加注释，overwrite覆盖生成时，@see XmlFileMergerJaxp.isGeneratedNode会去判断注释中是否存在OLD_ELEMENT_TAGS中的一点，例子：@mbg.generated)
        CommentTools.addComment(selectOneElement);

        // 添加ID
        selectOneElement.addAttribute(new Attribute("id", METHOD_NAME));

        // ----------------------------------------- 表中是否有blob类型字段 ---------------------------------------
        if (introspectedTable.hasBLOBColumns()){
            // 添加返回类型
            selectOneElement.addAttribute(new Attribute("resultMap", introspectedTable.getResultMapWithBLOBsId()));
            // 添加参数类型
            selectOneElement.addAttribute(new Attribute("parameterType", introspectedTable.getExampleType()));
            // 添加查询SQL
            selectOneElement.addElement(new TextElement("select")); //$NON-NLS-1$

            StringBuilder sb = new StringBuilder();
            if (stringHasValue(introspectedTable.getSelectByExampleQueryId())) {
                sb.append('\'');
                sb.append(introspectedTable.getSelectByExampleQueryId());
                sb.append("' as QUERYID,"); //$NON-NLS-1$
                selectOneElement.addElement(new TextElement(sb.toString()));
            }

            selectOneElement.addElement(XmlElementGeneratorTools.getBaseColumnListElement(introspectedTable));
            selectOneElement.addElement(new TextElement(",")); //$NON-NLS-1$
            selectOneElement.addElement(XmlElementGeneratorTools.getBlobColumnListElement(introspectedTable));

            sb.setLength(0);
            sb.append("from "); //$NON-NLS-1$
            sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
            selectOneElement.addElement(new TextElement(sb.toString()));
            selectOneElement.addElement(XmlElementGeneratorTools.getExampleIncludeElement(introspectedTable));

            XmlElement ifElement = new XmlElement("if"); //$NON-NLS-1$
            ifElement.addAttribute(new Attribute("test", "orderByClause != null")); //$NON-NLS-1$ //$NON-NLS-2$
            ifElement.addElement(new TextElement("order by ${orderByClause}")); //$NON-NLS-1$
            selectOneElement.addElement(ifElement);

            // 只查询一条
            selectOneElement.addElement(new TextElement("limit 1"));
        } else {
            // 添加返回类型
            selectOneElement.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
            // 添加参数类型
            selectOneElement.addAttribute(new Attribute("parameterType", introspectedTable.getExampleType()));
            selectOneElement.addElement(new TextElement("select")); //$NON-NLS-1$

            StringBuilder sb = new StringBuilder();
            if (stringHasValue(introspectedTable.getSelectByExampleQueryId())) {
                sb.append('\'');
                sb.append(introspectedTable.getSelectByExampleQueryId());
                sb.append("' as QUERYID,"); //$NON-NLS-1$
                selectOneElement.addElement(new TextElement(sb.toString()));
            }
            selectOneElement.addElement(XmlElementGeneratorTools.getBaseColumnListElement(introspectedTable));

            sb.setLength(0);
            sb.append("from "); //$NON-NLS-1$
            sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
            selectOneElement.addElement(new TextElement(sb.toString()));
            selectOneElement.addElement(XmlElementGeneratorTools.getExampleIncludeElement(introspectedTable));

            XmlElement ifElement = new XmlElement("if"); //$NON-NLS-1$
            ifElement.addAttribute(new Attribute("test", "orderByClause != null")); //$NON-NLS-1$ //$NON-NLS-2$
            ifElement.addElement(new TextElement("order by ${orderByClause}")); //$NON-NLS-1$
            selectOneElement.addElement(ifElement);

            // 只查询一条
            selectOneElement.addElement(new TextElement("limit 1"));
        }

        // 添加到根节点
        document.getRootElement().addElement(selectOneElement);
        logger.debug("itfsw(查询单条数据插件):"+introspectedTable.getMyBatis3XmlMapperFileName()+"增加SelectOneByExample方法("+(introspectedTable.hasBLOBColumns() ? "有" : "无")+"Blob类型))。");

        return true;
    }
}
