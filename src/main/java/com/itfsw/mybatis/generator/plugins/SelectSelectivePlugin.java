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
import com.itfsw.mybatis.generator.plugins.utils.hook.ISelectOneByExamplePluginHook;
import com.itfsw.mybatis.generator.plugins.utils.hook.ISelectSelectivePluginHook;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.*;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/6/29 13:34
 * ---------------------------------------------------------------------------
 */
public class SelectSelectivePlugin extends BasePlugin implements ISelectOneByExamplePluginHook {
    public static final String METHOD_SELECT_BY_EXAMPLE_SELECTIVE = "selectByExampleSelective";
    public static final String METHOD_SELECT_BY_PRIMARY_KEY_SELECTIVE = "selectByPrimaryKeySelective";
    public static final String METHOD_SELECT_ONE_BY_EXAMPLE_SELECTIVE = "selectOneByExampleSelective";
    public static final String ID_FOR_PROPERTY_BASED_RESULT_MAP = "BasePropertyResultMap";
    private XmlElement selectByExampleSelectiveEle;
    private XmlElement selectByPrimaryKeySelectiveEle;

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param warnings
     * @return
     */
    @Override
    public boolean validate(List<String> warnings) {

        // 插件使用前提是使用了ModelColumnPlugin插件
        if (!PluginTools.checkDependencyPlugin(getContext(), ModelColumnPlugin.class)) {
            warnings.add("itfsw:插件" + this.getClass().getTypeName() + "插件需配合com.itfsw.mybatis.generator.plugins.ModelColumnPlugin插件使用！");
            return false;
        }

        return super.validate(warnings);
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param introspectedTable
     */
    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        super.initialized(introspectedTable);

        // bug:26,27
        this.selectByExampleSelectiveEle = null;
        this.selectByPrimaryKeySelectiveEle = null;
    }

    // =========================================== client 方法生成 ===================================================

    @Override
    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        FormatTools.addMethodWithBestPosition(interfaze, this.replaceMethodWithSelective(
                method,
                METHOD_SELECT_BY_EXAMPLE_SELECTIVE,
                "@Param(\"example\")",
                introspectedTable
        ));
        return super.clientSelectByExampleWithBLOBsMethodGenerated(method, interfaze, introspectedTable);
    }

    @Override
    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (!introspectedTable.hasBLOBColumns()) {
            FormatTools.addMethodWithBestPosition(interfaze, this.replaceMethodWithSelective(
                    method,
                    METHOD_SELECT_BY_EXAMPLE_SELECTIVE,
                    "@Param(\"example\")",
                    introspectedTable
            ));
        }
        return super.clientSelectByExampleWithoutBLOBsMethodGenerated(method, interfaze, introspectedTable);
    }


    @Override
    public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            FormatTools.addMethodWithBestPosition(interfaze, this.replaceMethodWithSelective(
                    method,
                    METHOD_SELECT_BY_PRIMARY_KEY_SELECTIVE,
                    "@Param(\"record\")",
                    introspectedTable
            ));
        } else {
            Method withSelective = JavaElementTools.clone(method);
            FormatTools.replaceGeneralMethodComment(commentGenerator, withSelective, introspectedTable);

            withSelective.setName(METHOD_SELECT_BY_PRIMARY_KEY_SELECTIVE);

            withSelective.getParameters().clear();
            for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                withSelective.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(), introspectedColumn.getJavaProperty(), "@Param(\"" + introspectedColumn.getJavaProperty() + "\")"));
            }
            // selective
            withSelective.addParameter(
                    new Parameter(this.getModelColumnFullyQualifiedJavaType(introspectedTable), "selective", "@Param(\"selective\")", true)
            );

            FormatTools.addMethodWithBestPosition(interfaze, withSelective);
        }
        return super.clientSelectByPrimaryKeyMethodGenerated(method, interfaze, introspectedTable);
    }

    // ============================================== sqlMap 生成 ===================================================


    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        this.selectByExampleSelectiveEle = this.generateSelectSelectiveElement(METHOD_SELECT_BY_EXAMPLE_SELECTIVE, introspectedTable, false, true);
        return super.sqlMapSelectByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (!introspectedTable.hasBLOBColumns()) {
            this.selectByExampleSelectiveEle = this.generateSelectSelectiveElement(METHOD_SELECT_BY_EXAMPLE_SELECTIVE, introspectedTable, false, true);
        }
        return super.sqlMapSelectByExampleWithBLOBsElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        this.selectByPrimaryKeySelectiveEle = this.generateSelectSelectiveElement(METHOD_SELECT_BY_PRIMARY_KEY_SELECTIVE, introspectedTable, false, false);
        return super.sqlMapSelectByPrimaryKeyElementGenerated(element, introspectedTable);
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param document
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        // issues#16
        if (introspectedTable.isConstructorBased()) {
            XmlElement resultMapEle = new XmlElement("resultMap");
            resultMapEle.addAttribute(new Attribute("id", ID_FOR_PROPERTY_BASED_RESULT_MAP));
            resultMapEle.addAttribute(new Attribute("type", introspectedTable.getRules().calculateAllFieldsClass().getFullyQualifiedName()));
            commentGenerator.addComment(resultMapEle);

            for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                resultMapEle.addElement(XmlElementGeneratorTools.generateResultMapResultElement("id", introspectedColumn));
            }
            for (IntrospectedColumn introspectedColumn : introspectedTable.getNonPrimaryKeyColumns()) {
                resultMapEle.addElement(XmlElementGeneratorTools.generateResultMapResultElement("result", introspectedColumn));
            }
            document.getRootElement().addElement(0, resultMapEle);
        }

        // 1. selectByExampleSelective 方法
        if (this.selectByExampleSelectiveEle != null) {
            FormatTools.addElementWithBestPosition(document.getRootElement(), this.selectByExampleSelectiveEle);
            PluginTools.getHook(ISelectSelectivePluginHook.class).sqlMapSelectByExampleSelectiveElementGenerated(document, this.selectByExampleSelectiveEle, introspectedTable);
        }

        // 2. selectByPrimaryKeySelective
        if (this.selectByPrimaryKeySelectiveEle != null) {
            FormatTools.addElementWithBestPosition(document.getRootElement(), this.selectByPrimaryKeySelectiveEle);
        }

        return true;
    }

    // ===================================== ISelectOneByExamplePluginHook =========================================

    @Override
    public boolean clientSelectOneByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        FormatTools.addMethodWithBestPosition(interfaze, this.replaceMethodWithSelective(
                method,
                METHOD_SELECT_ONE_BY_EXAMPLE_SELECTIVE,
                "@Param(\"example\")",
                introspectedTable
        ));
        return true;
    }

    @Override
    public boolean clientSelectOneByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (!introspectedTable.hasBLOBColumns()) {
            FormatTools.addMethodWithBestPosition(interfaze, this.replaceMethodWithSelective(
                    method,
                    METHOD_SELECT_ONE_BY_EXAMPLE_SELECTIVE,
                    "@Param(\"example\")",
                    introspectedTable
            ));
        }
        return true;
    }

    @Override
    public boolean sqlMapSelectOneByExampleWithoutBLOBsElementGenerated(Document document, XmlElement element, IntrospectedTable introspectedTable) {
        if (!introspectedTable.hasBLOBColumns()) {
            FormatTools.addElementWithBestPosition(document.getRootElement(), this.generateSelectOneByExampleSelectiveElement(introspectedTable));
        }
        return true;
    }

    @Override
    public boolean sqlMapSelectOneByExampleWithBLOBsElementGenerated(Document document, XmlElement element, IntrospectedTable introspectedTable) {
        FormatTools.addElementWithBestPosition(document.getRootElement(), this.generateSelectOneByExampleSelectiveElement(introspectedTable));
        return true;
    }

    // =========================================== 一些私有方法 =====================================================

    /**
     * 生成selectOneByExampleSelective
     * @param introspectedTable
     * @return
     */
    private XmlElement generateSelectOneByExampleSelectiveElement(IntrospectedTable introspectedTable) {
        return this.generateSelectSelectiveElement(METHOD_SELECT_ONE_BY_EXAMPLE_SELECTIVE, introspectedTable, true, true);
    }

    /**
     * 生成selectOneByExampleSelective
     * @param introspectedTable
     * @return
     */
    private XmlElement generateSelectSelectiveElement(String id, IntrospectedTable introspectedTable, boolean selectOne, boolean byExample) {
        XmlElement selectSelectiveEle = new XmlElement("select");
        commentGenerator.addComment(selectSelectiveEle);

        selectSelectiveEle.addAttribute(new Attribute("id", id));
        // issues#16
        if (introspectedTable.isConstructorBased()) {
            selectSelectiveEle.addAttribute(new Attribute("resultMap", ID_FOR_PROPERTY_BASED_RESULT_MAP));
        } else if (introspectedTable.hasBLOBColumns()) {
            selectSelectiveEle.addAttribute(new Attribute("resultMap", introspectedTable.getResultMapWithBLOBsId()));
        } else {
            selectSelectiveEle.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
        }
        selectSelectiveEle.addAttribute(new Attribute("parameterType", "map"));

        if (byExample) {
            selectSelectiveEle.addElement(new TextElement("select"));

            if (!selectOne) {
                // issues#20
                XmlElement ifDistinctElement = new XmlElement("if");
                ifDistinctElement.addAttribute(new Attribute("test", "example != null and example.distinct"));
                ifDistinctElement.addElement(new TextElement("distinct"));
                selectSelectiveEle.addElement(ifDistinctElement);
            }

            //issue#102
            if (stringHasValue(introspectedTable.getSelectByExampleQueryId())) {
                selectSelectiveEle.addElement(new TextElement("'" + introspectedTable.getSelectByExampleQueryId() + "' as QUERYID,"));
            }
        } else {
            selectSelectiveEle.addElement(new TextElement("select"));
            if (stringHasValue(introspectedTable.getSelectByPrimaryKeyQueryId())) {
                selectSelectiveEle.addElement(new TextElement("'" + introspectedTable.getSelectByPrimaryKeyQueryId() + "' as QUERYID,"));
            }
        }

        selectSelectiveEle.addElement(this.generateSelectiveElement(introspectedTable));
        selectSelectiveEle.addElement(new TextElement("from " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));

        if (byExample) {
            XmlElement ifElement = new XmlElement("if");
            ifElement.addAttribute(new Attribute("test", "example != null"));

            XmlElement includeElement = new XmlElement("include");
            includeElement.addAttribute(new Attribute("refid", introspectedTable.getMyBatis3UpdateByExampleWhereClauseId()));
            ifElement.addElement(includeElement);

            selectSelectiveEle.addElement(ifElement);

            XmlElement ifElement1 = new XmlElement("if");
            ifElement1.addAttribute(new Attribute("test", "example != null and example.orderByClause != null"));
            ifElement1.addElement(new TextElement("order by ${example.orderByClause}"));
            selectSelectiveEle.addElement(ifElement1);
        } else {
            boolean and = false;
            StringBuffer sb = new StringBuffer();
            for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                sb.setLength(0);
                if (and) {
                    sb.append("  and ");
                } else {
                    sb.append("where ");
                    and = true;
                }

                sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(introspectedColumn));
                sb.append(" = ");
                sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, introspectedTable.getRules().generatePrimaryKeyClass() ? "record." : null));
                selectSelectiveEle.addElement(new TextElement(sb.toString()));
            }
        }

        if (selectOne) {
            // 只查询一条
            selectSelectiveEle.addElement(new TextElement("limit 1"));
        }

        return selectSelectiveEle;
    }

    /**
     * 生成Selective xml节点
     * @param introspectedTable
     * @return
     */
    private XmlElement generateSelectiveElement(IntrospectedTable introspectedTable) {
        XmlElement chooseEle = new XmlElement("choose");

        XmlElement whenEle = new XmlElement("when");
        whenEle.addAttribute(new Attribute("test", "selective != null and selective.length > 0"));
        chooseEle.addElement(whenEle);

        // 生成返回字段节点
        XmlElement keysEle = new XmlElement("foreach");
        whenEle.addElement(keysEle);
        keysEle.addAttribute(new Attribute("collection", "selective"));
        keysEle.addAttribute(new Attribute("item", "column"));
        keysEle.addAttribute(new Attribute("separator", ","));
        keysEle.addElement(new TextElement("${column.aliasedEscapedColumnName}"));

        XmlElement otherwiseEle = new XmlElement("otherwise");
        chooseEle.addElement(otherwiseEle);
        // 存在关键词column或者table定义了alias属性,这里直接使用对应的ColumnListElement
        if (introspectedTable.getRules().generateSelectByExampleWithBLOBs()) {
            otherwiseEle.addElement(XmlElementGeneratorTools.getBaseColumnListElement(introspectedTable));
            otherwiseEle.addElement(new TextElement(","));
            otherwiseEle.addElement(XmlElementGeneratorTools.getBlobColumnListElement(introspectedTable));
        } else {
            otherwiseEle.addElement(XmlElementGeneratorTools.getBaseColumnListElement(introspectedTable));
        }

        return chooseEle;
    }

    /**
     * 替换方法成withSelective
     * @param method
     * @param name
     * @param firstAnnotation
     * @param introspectedTable
     * @return
     */
    private Method replaceMethodWithSelective(Method method, String name, String firstAnnotation, IntrospectedTable introspectedTable) {
        Method withSelective = JavaElementTools.clone(method);
        FormatTools.replaceGeneralMethodComment(commentGenerator, withSelective, introspectedTable);

        withSelective.setName(name);
        // example
        withSelective.getParameters().get(0).addAnnotation(firstAnnotation);
        // selective
        withSelective.addParameter(
                new Parameter(this.getModelColumnFullyQualifiedJavaType(introspectedTable), "selective", "@Param(\"selective\")", true)
        );

        return withSelective;
    }

    /**
     * 获取ModelColumn type
     * @param introspectedTable
     * @return
     */
    private FullyQualifiedJavaType getModelColumnFullyQualifiedJavaType(IntrospectedTable introspectedTable) {
        return new FullyQualifiedJavaType(introspectedTable.getRules().calculateAllFieldsClass().getShortName() + "." + ModelColumnPlugin.ENUM_NAME);
    }
}
