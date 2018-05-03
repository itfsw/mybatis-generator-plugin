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
import com.itfsw.mybatis.generator.plugins.utils.FormatTools;
import com.itfsw.mybatis.generator.plugins.utils.XmlElementTools;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.xml.*;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ---------------------------------------------------------------------------
 * 乐观锁插件
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2018/4/26 10:24
 * ---------------------------------------------------------------------------
 */
public class OptimisticLockerPlugin extends BasePlugin {
    public static final String METHOD_DELETE_WITH_VERSION_BY_EXAMPLE = "deleteWithVersionByExample";  // 方法名
    public static final String METHOD_DELETE_WITH_VERSION_BY_PRIMARY_KEY = "deleteWithVersionByPrimaryKey";  // 方法名
    public static final String METHOD_UPDATE_WITH_VERSION_BY_EXAMPLE_SELECTIVE = "updateWithVersionByExampleSelective";  // 方法名
    public static final String METHOD_UPDATE_WITH_VERSION_BY_EXAMPLE_WITH_BLOBS = "updateWithVersionByExampleWithBLOBs";  // 方法名
    public static final String METHOD_UPDATE_WITH_VERSION_BY_EXAMPLE_WITHOUT_BLOBS = "updateWithVersionByExampleWithoutBLOBs";  // 方法名
    public static final String METHOD_UPDATE_WITH_VERSION_BY_PRIMARY_KEY_SELECTIVE = "updateWithVersionByPrimaryKeySelective";  // 方法名
    public static final String METHOD_UPDATE_WITH_VERSION_BY_PRIMARY_KEY_WITH_BLOBS = "updateWithVersionByPrimaryKeyWithBLOBs";  // 方法名
    public static final String METHOD_UPDATE_WITH_VERSION_BY_PRIMARY_KEY_WITHOUT_BLOBS = "updateWithVersionByPrimaryKeyWithoutBLOBs";  // 方法名
    public static final String SQL_UPDATE_BY_EXAMPLE_WITH_VERSION_WHERE_CLAUSE = "Update_By_Example_With_Version_Where_Clause";

    public static final String PRO_VERSION_COLUMN = "versionColumn";  // 版本列-Key

    private Map<IntrospectedTable, List<XmlElement>> sqlMaps = new HashMap<>(); // sqlMap xml 节点
    private IntrospectedColumn versionColumn;   // 版本列

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        sqlMaps.put(introspectedTable, new ArrayList<>());

        // 读取并验证版本列
        String versionColumn = introspectedTable.getTableConfigurationProperty(PRO_VERSION_COLUMN);
        if (versionColumn != null) {
            this.versionColumn = introspectedTable.getColumn(versionColumn);
            if (this.versionColumn == null) {
                warnings.add("itfsw(乐观锁插件):表" + introspectedTable.getFullyQualifiedTable() + "配置的版本列(" + introspectedTable.getTableConfigurationProperty(PRO_VERSION_COLUMN) + ")没有找到！");
            }
        }
        super.initialized(introspectedTable);
    }

    // ========================================= method 生成 ============================================
    @Override
    public boolean clientDeleteByExampleMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            this.replaceDeleteExampleMethod(introspectedTable, method, interfaze, METHOD_DELETE_WITH_VERSION_BY_EXAMPLE);
        }
        return super.clientDeleteByExampleMethodGenerated(method, interfaze, introspectedTable);
    }

    @Override
    public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            this.replaceDeletePrimaryKeyMethod(introspectedTable, method, interfaze, METHOD_DELETE_WITH_VERSION_BY_PRIMARY_KEY);
        }
        return super.clientDeleteByPrimaryKeyMethodGenerated(method, interfaze, introspectedTable);
    }

    @Override
    public boolean clientUpdateByExampleSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            this.replaceUpdateExampleMethod(introspectedTable, method, interfaze, METHOD_UPDATE_WITH_VERSION_BY_EXAMPLE_SELECTIVE);
        }
        return super.clientUpdateByExampleSelectiveMethodGenerated(method, interfaze, introspectedTable);
    }

    @Override
    public boolean clientUpdateByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            this.replaceUpdateExampleMethod(introspectedTable, method, interfaze, METHOD_UPDATE_WITH_VERSION_BY_EXAMPLE_WITH_BLOBS);
        }
        return super.clientUpdateByExampleWithBLOBsMethodGenerated(method, interfaze, introspectedTable);
    }

    @Override
    public boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            this.replaceUpdateExampleMethod(introspectedTable, method, interfaze, METHOD_UPDATE_WITH_VERSION_BY_EXAMPLE_WITHOUT_BLOBS);
        }
        return super.clientUpdateByExampleWithoutBLOBsMethodGenerated(method, interfaze, introspectedTable);
    }

    @Override
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            this.replaceUpdatePrimaryKeyXmlMethod(introspectedTable, method, interfaze, METHOD_UPDATE_WITH_VERSION_BY_PRIMARY_KEY_SELECTIVE);
        }
        return super.clientUpdateByPrimaryKeySelectiveMethodGenerated(method, interfaze, introspectedTable);
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            this.replaceUpdatePrimaryKeyXmlMethod(introspectedTable, method, interfaze, METHOD_UPDATE_WITH_VERSION_BY_PRIMARY_KEY_WITH_BLOBS);
        }
        return super.clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(method, interfaze, introspectedTable);
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            this.replaceUpdatePrimaryKeyXmlMethod(introspectedTable, method, interfaze, METHOD_UPDATE_WITH_VERSION_BY_PRIMARY_KEY_WITHOUT_BLOBS);
        }
        return super.clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(method, interfaze, introspectedTable);
    }

    // ========================================= sqlMap 生成 ============================================

    @Override
    public boolean sqlMapDeleteByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            this.replaceExampleXmlElement(introspectedTable, element, METHOD_DELETE_WITH_VERSION_BY_EXAMPLE);
        }
        return super.sqlMapDeleteByExampleElementGenerated(element, introspectedTable);
    }

    /**
     * @param element
     * @param introspectedTable
     * @return
     * @see org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.DeleteByPrimaryKeyElementGenerator#addElements(XmlElement)
     */
    @Override
    public boolean sqlMapDeleteByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            this.replacePrimaryKeyXmlElement(introspectedTable, element, METHOD_DELETE_WITH_VERSION_BY_PRIMARY_KEY, false);
        }
        return super.sqlMapDeleteByPrimaryKeyElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            this.replaceExampleXmlElement(introspectedTable, element, METHOD_UPDATE_WITH_VERSION_BY_EXAMPLE_SELECTIVE);
        }
        return super.sqlMapUpdateByExampleSelectiveElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            this.replaceExampleXmlElement(introspectedTable, element, METHOD_UPDATE_WITH_VERSION_BY_EXAMPLE_WITH_BLOBS);
        }
        return super.sqlMapUpdateByExampleWithBLOBsElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            this.replaceExampleXmlElement(introspectedTable, element, METHOD_UPDATE_WITH_VERSION_BY_EXAMPLE_WITHOUT_BLOBS);
        }
        return super.sqlMapUpdateByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            this.replaceUpdateByPrimaryKeyXmlElement(introspectedTable, element, METHOD_UPDATE_WITH_VERSION_BY_PRIMARY_KEY_SELECTIVE);
        }
        return super.sqlMapUpdateByPrimaryKeySelectiveElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            this.replaceUpdateByPrimaryKeyXmlElement(introspectedTable, element, METHOD_UPDATE_WITH_VERSION_BY_PRIMARY_KEY_WITH_BLOBS);
        }
        return super.sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            this.replaceUpdateByPrimaryKeyXmlElement(introspectedTable, element, METHOD_UPDATE_WITH_VERSION_BY_PRIMARY_KEY_WITHOUT_BLOBS);
        }
        return super.sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            for (XmlElement element : sqlMaps.get(introspectedTable)) {
                FormatTools.addElementWithBestPosition(document.getRootElement(), element);
            }
        }
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    @Override
    public boolean sqlMapExampleWhereClauseElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            if (XmlElementTools.getAttribute(element, "id").getValue().equals("Update_By_Example_Where_Clause")) {
                XmlElement withVersionEle = new XmlElement("sql");
                withVersionEle.addAttribute(new Attribute("id", SQL_UPDATE_BY_EXAMPLE_WITH_VERSION_WHERE_CLAUSE));
                commentGenerator.addComment(withVersionEle);

                // 版本语句
                XmlElement whereEle = new XmlElement("where ");
                withVersionEle.addElement(whereEle);

                whereEle.addElement(new TextElement(this.generateVersionEleStr()));

                // 附加原生语句
                XmlElement ifEle = new XmlElement("if");
                whereEle.addElement(ifEle);

                ifEle.addAttribute(new Attribute("test", "example.oredCriteria.size() > 0"));

                // 原生的foreach
                XmlElement foreachEle = (XmlElement) XmlElementTools.findXmlElements(element, "where").get(0).getElements().get(0);
                ifEle.addElement(foreachEle);

                foreachEle.addAttribute(new Attribute("open", "and ("));
                foreachEle.addAttribute(new Attribute("close", ")"));

                sqlMaps.get(introspectedTable).add(withVersionEle);

                context.getPlugins().sqlMapExampleWhereClauseElementGenerated(withVersionEle, introspectedTable);
            }
        }
        return super.sqlMapExampleWhereClauseElementGenerated(element, introspectedTable);
    }


    // =================================================== 一些生成方法 ========================================

    /**
     * 生成版本判断节点
     * @return
     */
    private String generateVersionEleStr() {
        StringBuilder sb = new StringBuilder();
        sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(this.versionColumn));
        sb.append(" = ");
        sb.append("#{version,jdbcType=");
        sb.append(this.versionColumn.getJdbcTypeName());
        if (StringUtility.stringHasValue(this.versionColumn.getTypeHandler())) {
            sb.append(",typeHandler=");
            sb.append(this.versionColumn.getTypeHandler());
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * 替换Example 方法
     * @param introspectedTable
     * @param method
     * @param interfaze
     * @param methodName
     */
    private void replaceUpdateExampleMethod(IntrospectedTable introspectedTable, Method method, Interface interfaze, String methodName) {
        Method withVersionMethod = new Method(method);

        // 替换方法名
        withVersionMethod.setName(methodName);
        FormatTools.replaceGeneralMethodComment(commentGenerator, withVersionMethod, introspectedTable);

        Parameter versionParam = new Parameter(this.versionColumn.getFullyQualifiedJavaType(), "version", "@Param(\"version\")");

        withVersionMethod.addParameter(0, versionParam);

        FormatTools.addMethodWithBestPosition(interfaze, withVersionMethod);
    }

    /**
     * 替换Example 方法
     * @param introspectedTable
     * @param method
     * @param interfaze
     * @param methodName
     */
    private void replaceDeleteExampleMethod(IntrospectedTable introspectedTable, Method method, Interface interfaze, String methodName) {
        Method withVersionMethod = new Method(method);

        // 替换方法名
        withVersionMethod.setName(methodName);
        FormatTools.replaceGeneralMethodComment(commentGenerator, withVersionMethod, introspectedTable);

        Parameter versionParam = new Parameter(this.versionColumn.getFullyQualifiedJavaType(), "version", "@Param(\"version\")");
        Parameter exampleParam = new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example", "@Param(\"example\")");

        withVersionMethod.getParameters().clear();
        withVersionMethod.addParameter(versionParam);
        withVersionMethod.addParameter(exampleParam);

        FormatTools.addMethodWithBestPosition(interfaze, withVersionMethod);
    }

    /**
     * 替换主键 方法
     * @param introspectedTable
     * @param method
     * @param interfaze
     * @param methodName
     */
    private void replaceUpdatePrimaryKeyXmlMethod(IntrospectedTable introspectedTable, Method method, Interface interfaze, String methodName) {
        Method withVersionMethod = new Method(method);

        // 替换方法名
        withVersionMethod.setName(methodName);
        FormatTools.replaceGeneralMethodComment(commentGenerator, withVersionMethod, introspectedTable);

        Parameter versionParam = new Parameter(this.versionColumn.getFullyQualifiedJavaType(), "version", "@Param(\"version\")");
        Parameter recordParam = new Parameter(method.getParameters().get(0).getType(), "record", "@Param(\"record\")");

        withVersionMethod.getParameters().clear();
        withVersionMethod.addParameter(versionParam);
        withVersionMethod.addParameter(recordParam);

        FormatTools.addMethodWithBestPosition(interfaze, withVersionMethod);
    }

    /**
     * 替换主键 方法
     * @param introspectedTable
     * @param method
     * @param interfaze
     * @param methodName
     */
    private void replaceDeletePrimaryKeyMethod(IntrospectedTable introspectedTable, Method method, Interface interfaze, String methodName) {
        Method withVersionMethod = new Method(method);

        // 替换方法名
        withVersionMethod.setName(methodName);
        FormatTools.replaceGeneralMethodComment(commentGenerator, withVersionMethod, introspectedTable);

        Parameter versionParam = new Parameter(this.versionColumn.getFullyQualifiedJavaType(), "version", "@Param(\"version\")");
        Parameter keyParam = new Parameter(method.getParameters().get(0).getType(), "key", "@Param(\"key\")");

        withVersionMethod.getParameters().clear();
        withVersionMethod.addParameter(versionParam);
        withVersionMethod.addParameter(keyParam);

        FormatTools.addMethodWithBestPosition(interfaze, withVersionMethod);
    }

    /**
     * updateByPrimaryKeyXXXXX 因为替换了传入参数，使用注解record来赋值
     * @param introspectedTable
     * @param element
     * @param id
     * @return
     */
    private XmlElement replaceUpdateByPrimaryKeyXmlElement(IntrospectedTable introspectedTable, XmlElement element, String id) {
        XmlElement withVersionEle = XmlElementTools.clone(element);

        // 查找所有文本节点，替换set操作text xml 节点
        for (TextElement textElement : XmlElementTools.findAllTextElements(withVersionEle)) {
            if (textElement.getContent().matches(".*#\\{\\w+,.*}.*")) {

                Matcher matcher = Pattern.compile("(.*#\\{)(\\w+,.*}.*)").matcher(textElement.getContent());
                if (matcher.find()) {
                    String context = matcher.group(1) + "record." + matcher.group(2);
                    try {
                        BeanUtils.setProperty(textElement, "content", context);
                    } catch (Exception e) {
                        warnings.add("Java反射失败！");
                    }
                }
            }
        }

        return replacePrimaryKeyXmlElement(introspectedTable, withVersionEle, id, true);
    }

    /**
     * 替换Example
     * @param introspectedTable
     * @param element
     * @param id
     */
    private XmlElement replaceExampleXmlElement(IntrospectedTable introspectedTable, XmlElement element, String id) {
        XmlElement withVersionEle = XmlElementTools.clone(element);

        XmlElementTools.replaceAttribute(withVersionEle, new Attribute("id", id));
        XmlElementTools.replaceAttribute(withVersionEle, new Attribute("parameterType", "map"));

        FormatTools.replaceComment(commentGenerator, withVersionEle);

        // 替换查询语句
        List<XmlElement> ifEles = XmlElementTools.findXmlElements(withVersionEle, "if");
        for (XmlElement ifEle : ifEles) {
            List<XmlElement> includeEles = XmlElementTools.findXmlElements(ifEle, "include");
            for (XmlElement includeEle : includeEles) {
                if (XmlElementTools.getAttribute(includeEle, "refid") != null) {
                    XmlElementTools.replaceAttribute(includeEle, new Attribute("refid", SQL_UPDATE_BY_EXAMPLE_WITH_VERSION_WHERE_CLAUSE));
                }
            }
        }

        this.sqlMaps.get(introspectedTable).add(withVersionEle);

        return withVersionEle;
    }

    /**
     * 替换 主键查询
     * @param introspectedTable
     * @param element
     * @param id
     * @param update
     */
    private XmlElement replacePrimaryKeyXmlElement(IntrospectedTable introspectedTable, XmlElement element, String id, boolean update) {
        XmlElement withVersionEle = XmlElementTools.clone(element);

        XmlElementTools.replaceAttribute(withVersionEle, new Attribute("id", id));
        XmlElementTools.replaceAttribute(withVersionEle, new Attribute("parameterType", "map"));

        FormatTools.replaceComment(commentGenerator, withVersionEle);

        // 替换查询语句
        Iterator<Element> elementIterator = withVersionEle.getElements().iterator();
        boolean flag = false;
        while (elementIterator.hasNext()) {
            Element ele = elementIterator.next();
            if (ele instanceof TextElement && ((TextElement) ele).getContent().matches(".*where.*")) {
                flag = true;
            }

            if (flag) {
                elementIterator.remove();
            }
        }

        // where 语句
        withVersionEle.addElement(new TextElement("where " + this.generateVersionEleStr()));
        if (introspectedTable.getPrimaryKeyColumns().size() == 1) {
            IntrospectedColumn introspectedColumn = introspectedTable.getPrimaryKeyColumns().get(0);
            StringBuilder sb = new StringBuilder();
            sb.append("  and ");
            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(" = ");
            if (update) {
                sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, "record."));
            } else {
                sb.append("#{key,jdbcType=");
                sb.append(introspectedColumn.getJdbcTypeName());
                if (StringUtility.stringHasValue(introspectedColumn.getTypeHandler())) {
                    sb.append(",typeHandler=");
                    sb.append(introspectedColumn.getTypeHandler());
                }
                sb.append("}");
            }
            withVersionEle.addElement(new TextElement(sb.toString()));
        } else {
            for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                StringBuilder sb = new StringBuilder();
                sb.append("  and ");
                sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
                sb.append(" = ");
                sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, update ? "record." : "key."));
                withVersionEle.addElement(new TextElement(sb.toString()));
            }
        }

        this.sqlMaps.get(introspectedTable).add(withVersionEle);

        return withVersionEle;
    }
}
