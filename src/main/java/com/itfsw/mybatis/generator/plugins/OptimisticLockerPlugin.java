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

import com.itfsw.mybatis.generator.plugins.utils.*;
import com.itfsw.mybatis.generator.plugins.utils.hook.ILogicalDeletePluginHook;
import com.itfsw.mybatis.generator.plugins.utils.hook.IModelBuilderPluginHook;
import com.itfsw.mybatis.generator.plugins.utils.hook.IOptimisticLockerPluginHook;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.*;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.*;

/**
 * ---------------------------------------------------------------------------
 * 乐观锁插件
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2018/4/26 10:24
 * ---------------------------------------------------------------------------
 */
public class OptimisticLockerPlugin extends BasePlugin implements IModelBuilderPluginHook, ILogicalDeletePluginHook {
    public static final String METHOD_DELETE_WITH_VERSION_BY_EXAMPLE = "deleteWithVersionByExample";  // 方法名
    public static final String METHOD_DELETE_WITH_VERSION_BY_PRIMARY_KEY = "deleteWithVersionByPrimaryKey";  // 方法名
    public static final String METHOD_LOGICAL_DELETE_WITH_VERSION_BY_EXAMPLE = "logicalDeleteWithVersionByExample";  // 方法名
    public static final String METHOD_LOGICAL_DELETE_WITH_VERSION_BY_PRIMARY_KEY = "logicalDeleteWithVersionByPrimaryKey";  // 方法名
    public static final String METHOD_UPDATE_WITH_VERSION_BY_EXAMPLE_SELECTIVE = "updateWithVersionByExampleSelective";  // 方法名
    public static final String METHOD_UPDATE_WITH_VERSION_BY_EXAMPLE_WITH_BLOBS = "updateWithVersionByExampleWithBLOBs";  // 方法名
    public static final String METHOD_UPDATE_WITH_VERSION_BY_EXAMPLE_WITHOUT_BLOBS = "updateWithVersionByExample";  // 方法名
    public static final String METHOD_UPDATE_WITH_VERSION_BY_PRIMARY_KEY_SELECTIVE = "updateWithVersionByPrimaryKeySelective";  // 方法名
    public static final String METHOD_UPDATE_WITH_VERSION_BY_PRIMARY_KEY_WITH_BLOBS = "updateWithVersionByPrimaryKeyWithBLOBs";  // 方法名
    public static final String METHOD_UPDATE_WITH_VERSION_BY_PRIMARY_KEY_WITHOUT_BLOBS = "updateWithVersionByPrimaryKey";  // 方法名
    public static final String METHOD_NEXT_VERSION = "nextVersion";  // 方法名
    public static final String SQL_UPDATE_BY_EXAMPLE_WITH_VERSION_WHERE_CLAUSE = "Update_By_Example_With_Version_Where_Clause";

    public static final String PRO_VERSION_COLUMN = "versionColumn";  // 版本列-Key
    public static final String PRO_CUSTOMIZED_NEXT_VERSION = "customizedNextVersion";    // 使用用户自定义nextVersion key

    private Map<IntrospectedTable, List<XmlElement>> sqlMaps = new HashMap<>(); // sqlMap xml 节点
    private IntrospectedColumn versionColumn;   // 版本列
    private boolean customizedNextVersion;  // 使用用户自定义nextVersion

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        super.initialized(introspectedTable);
        sqlMaps.put(introspectedTable, new ArrayList<>());

        // 读取并验证版本列
        String versionColumn = introspectedTable.getTableConfigurationProperty(PRO_VERSION_COLUMN);
        if (versionColumn != null) {
            this.versionColumn = introspectedTable.getColumn(versionColumn);
            if (this.versionColumn == null) {
                warnings.add("itfsw(乐观锁插件):表" + introspectedTable.getFullyQualifiedTable() + "配置的版本列(" + introspectedTable.getTableConfigurationProperty(PRO_VERSION_COLUMN) + ")没有找到！");
            }
        } else {
            this.versionColumn = null;
        }

        // 自定义nextVersion
        // 首先获取全局配置
        Properties properties = getProperties();
        String customizedNextVersion = properties.getProperty(PRO_CUSTOMIZED_NEXT_VERSION);
        // 获取表单独配置，如果有则覆盖全局配置
        if (introspectedTable.getTableConfigurationProperty(PRO_CUSTOMIZED_NEXT_VERSION) != null) {
            customizedNextVersion = introspectedTable.getTableConfigurationProperty(PRO_CUSTOMIZED_NEXT_VERSION);
        }
        if (StringUtility.stringHasValue(customizedNextVersion) && StringUtility.isTrue(customizedNextVersion)) {
            this.customizedNextVersion = true;
        } else {
            this.customizedNextVersion = false;
        }

        super.initialized(introspectedTable);
    }

    // ========================================= method 生成 ============================================
    @Override
    public boolean clientDeleteByExampleMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            FormatTools.addMethodWithBestPosition(
                    interfaze,
                    this.replaceDeleteExampleMethod(introspectedTable, method, interfaze, METHOD_DELETE_WITH_VERSION_BY_EXAMPLE)
            );
        }
        return super.clientDeleteByExampleMethodGenerated(method, interfaze, introspectedTable);
    }

    @Override
    public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            FormatTools.addMethodWithBestPosition(
                    interfaze,
                    this.replaceDeletePrimaryKeyMethod(introspectedTable, method, interfaze, METHOD_DELETE_WITH_VERSION_BY_PRIMARY_KEY)
            );
        }
        return super.clientDeleteByPrimaryKeyMethodGenerated(method, interfaze, introspectedTable);
    }

    @Override
    public boolean clientLogicalDeleteByExampleMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            if (this.customizedNextVersion) {
                Method newMethod = JavaElementGeneratorTools.generateMethod(
                        METHOD_LOGICAL_DELETE_WITH_VERSION_BY_EXAMPLE,
                        JavaVisibility.DEFAULT,
                        FullyQualifiedJavaType.getIntInstance(),
                        new Parameter(this.versionColumn.getFullyQualifiedJavaType(), "version", "@Param(\"version\")"),
                        new Parameter(this.versionColumn.getFullyQualifiedJavaType(), "nextVersion", "@Param(\"nextVersion\")"),
                        new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example", "@Param(\"example\")")
                );
                commentGenerator.addGeneralMethodComment(newMethod, introspectedTable);
                FormatTools.addMethodWithBestPosition(interfaze, newMethod);
            } else {
                FormatTools.addMethodWithBestPosition(
                        interfaze,
                        this.replaceDeleteExampleMethod(introspectedTable, method, interfaze, METHOD_LOGICAL_DELETE_WITH_VERSION_BY_EXAMPLE)
                );
            }
        }
        return true;
    }

    @Override
    public boolean clientLogicalDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            if (this.customizedNextVersion) {
                Method newMethod = JavaElementGeneratorTools.generateMethod(
                        METHOD_LOGICAL_DELETE_WITH_VERSION_BY_PRIMARY_KEY,
                        JavaVisibility.DEFAULT,
                        FullyQualifiedJavaType.getIntInstance(),
                        new Parameter(this.versionColumn.getFullyQualifiedJavaType(), "version", "@Param(\"version\")"),
                        new Parameter(this.versionColumn.getFullyQualifiedJavaType(), "nextVersion", "@Param(\"nextVersion\")"),
                        new Parameter(method.getParameters().get(0).getType(), method.getParameters().get(0).getName(), "@Param(\"key\")")
                );
                commentGenerator.addGeneralMethodComment(newMethod, introspectedTable);
                FormatTools.addMethodWithBestPosition(interfaze, newMethod);
            } else {
                FormatTools.addMethodWithBestPosition(
                        interfaze,
                        this.replaceDeletePrimaryKeyMethod(introspectedTable, method, interfaze, METHOD_LOGICAL_DELETE_WITH_VERSION_BY_PRIMARY_KEY)
                );
            }
        }
        return true;
    }

    @Override
    public boolean clientUpdateByExampleSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            Method withVersion = this.replaceUpdateExampleMethod(introspectedTable, method, interfaze, METHOD_UPDATE_WITH_VERSION_BY_EXAMPLE_SELECTIVE);
            if (PluginTools.getHook(IOptimisticLockerPluginHook.class).clientUpdateWithVersionByExampleSelectiveMethodGenerated(withVersion, interfaze, introspectedTable)) {
                FormatTools.addMethodWithBestPosition(interfaze, withVersion);
            }
        }
        return super.clientUpdateByExampleSelectiveMethodGenerated(method, interfaze, introspectedTable);
    }

    @Override
    public boolean clientUpdateByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            FormatTools.addMethodWithBestPosition(
                    interfaze,
                    this.replaceUpdateExampleMethod(introspectedTable, method, interfaze, METHOD_UPDATE_WITH_VERSION_BY_EXAMPLE_WITH_BLOBS)
            );
        }
        return super.clientUpdateByExampleWithBLOBsMethodGenerated(method, interfaze, introspectedTable);
    }

    @Override
    public boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            FormatTools.addMethodWithBestPosition(
                    interfaze,
                    this.replaceUpdateExampleMethod(introspectedTable, method, interfaze, METHOD_UPDATE_WITH_VERSION_BY_EXAMPLE_WITHOUT_BLOBS)
            );
        }
        return super.clientUpdateByExampleWithoutBLOBsMethodGenerated(method, interfaze, introspectedTable);
    }

    @Override
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            Method withVersion = this.replaceUpdatePrimaryKeyMethod(introspectedTable, method, interfaze, METHOD_UPDATE_WITH_VERSION_BY_PRIMARY_KEY_SELECTIVE);
            if (PluginTools.getHook(IOptimisticLockerPluginHook.class).clientUpdateWithVersionByPrimaryKeySelectiveMethodGenerated(withVersion, interfaze, introspectedTable)) {
                FormatTools.addMethodWithBestPosition(interfaze, withVersion);
            }
        }
        return super.clientUpdateByPrimaryKeySelectiveMethodGenerated(method, interfaze, introspectedTable);
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            FormatTools.addMethodWithBestPosition(
                    interfaze,
                    this.replaceUpdatePrimaryKeyMethod(introspectedTable, method, interfaze, METHOD_UPDATE_WITH_VERSION_BY_PRIMARY_KEY_WITH_BLOBS)
            );
        }
        return super.clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(method, interfaze, introspectedTable);
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            FormatTools.addMethodWithBestPosition(
                    interfaze,
                    this.replaceUpdatePrimaryKeyMethod(introspectedTable, method, interfaze, METHOD_UPDATE_WITH_VERSION_BY_PRIMARY_KEY_WITHOUT_BLOBS)
            );
        }
        return super.clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(method, interfaze, introspectedTable);
    }

    // ========================================= model 生成 ============================================

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        if (this.versionColumn != null && this.customizedNextVersion && introspectedColumn.getActualColumnName().equals(this.versionColumn.getActualColumnName())) {
            // nextVersion 方法
            Method nextVersion = JavaElementGeneratorTools.generateMethod(
                    METHOD_NEXT_VERSION,
                    JavaVisibility.PUBLIC,
                    topLevelClass.getType(),
                    new Parameter(this.versionColumn.getFullyQualifiedJavaType(), "version")
            );
            commentGenerator.addSetterComment(nextVersion, introspectedTable, this.versionColumn);
            JavaElementGeneratorTools.generateMethodBody(
                    nextVersion,
                    "this." + this.versionColumn.getJavaProperty() + " = version;",
                    "return this;"
            );

            FormatTools.addMethodWithBestPosition(topLevelClass, nextVersion);
        }

        return super.modelSetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
    }

    /**
     * Model builder set 方法生成
     * @param method
     * @param topLevelClass
     * @param builderClass
     * @param introspectedColumn
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelBuilderSetterMethodGenerated(Method method, TopLevelClass topLevelClass, InnerClass builderClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null && this.customizedNextVersion && introspectedColumn.getActualColumnName().equals(this.versionColumn.getActualColumnName())) {
            // nextVersion 方法
            Method nextVersion = JavaElementGeneratorTools.generateMethod(
                    METHOD_NEXT_VERSION,
                    JavaVisibility.PUBLIC,
                    builderClass.getType(),
                    new Parameter(this.versionColumn.getFullyQualifiedJavaType(), "version")
            );
            nextVersion.addAnnotation("@Deprecated");
            commentGenerator.addSetterComment(nextVersion, introspectedTable, this.versionColumn);

            Method setterMethod = JavaBeansUtil.getJavaBeansSetter(this.versionColumn, context, introspectedTable);

            JavaElementGeneratorTools.generateMethodBody(
                    nextVersion,
                    "obj." + setterMethod.getName() + "(version);",
                    "return this;"
            );

            FormatTools.addMethodWithBestPosition(builderClass, nextVersion);
        }

        return true;
    }

    /**
     * Model builder class 生成
     * @param topLevelClass
     * @param builderClass
     * @param columns
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelBuilderClassGenerated(TopLevelClass topLevelClass, InnerClass builderClass, List<IntrospectedColumn> columns, IntrospectedTable introspectedTable) {
        return true;
    }

    // ========================================= sqlMap 生成 ============================================

    @Override
    public boolean sqlMapDeleteByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            this.sqlMaps.get(introspectedTable).add(
                    this.replaceExampleXmlElement(introspectedTable, element, METHOD_DELETE_WITH_VERSION_BY_EXAMPLE)
            );
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
            this.sqlMaps.get(introspectedTable).add(
                    this.replacePrimaryKeyXmlElement(introspectedTable, element, METHOD_DELETE_WITH_VERSION_BY_PRIMARY_KEY, false)
            );
        }
        return super.sqlMapDeleteByPrimaryKeyElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            this.sqlMaps.get(introspectedTable).add(
                    this.generateSqlMapUpdate(
                            introspectedTable,
                            ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getAllColumns()),
                            METHOD_UPDATE_WITH_VERSION_BY_EXAMPLE_SELECTIVE,
                            true,
                            true
                    )
            );
        }
        return super.sqlMapUpdateByExampleSelectiveElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            this.sqlMaps.get(introspectedTable).add(
                    this.generateSqlMapUpdate(
                            introspectedTable,
                            ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getAllColumns()),
                            METHOD_UPDATE_WITH_VERSION_BY_EXAMPLE_WITH_BLOBS,
                            false,
                            true
                    )
            );
        }
        return super.sqlMapUpdateByExampleWithBLOBsElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            this.sqlMaps.get(introspectedTable).add(
                    this.generateSqlMapUpdate(
                            introspectedTable,
                            ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getNonBLOBColumns()),
                            METHOD_UPDATE_WITH_VERSION_BY_EXAMPLE_WITHOUT_BLOBS,
                            false,
                            true
                    )
            );
        }
        return super.sqlMapUpdateByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            this.sqlMaps.get(introspectedTable).add(
                    this.generateSqlMapUpdate(
                            introspectedTable,
                            ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getNonPrimaryKeyColumns()),
                            METHOD_UPDATE_WITH_VERSION_BY_PRIMARY_KEY_SELECTIVE,
                            true,
                            false
                    )
            );
        }
        return super.sqlMapUpdateByPrimaryKeySelectiveElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            this.sqlMaps.get(introspectedTable).add(
                    this.generateSqlMapUpdate(
                            introspectedTable,
                            ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getNonPrimaryKeyColumns()),
                            METHOD_UPDATE_WITH_VERSION_BY_PRIMARY_KEY_WITH_BLOBS,
                            false,
                            false
                    )
            );
        }
        return super.sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            this.sqlMaps.get(introspectedTable).add(
                    this.generateSqlMapUpdate(
                            introspectedTable,
                            ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getBaseColumns()),
                            METHOD_UPDATE_WITH_VERSION_BY_PRIMARY_KEY_WITHOUT_BLOBS,
                            false,
                            false
                    )
            );
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

    @Override
    public boolean sqlMapLogicalDeleteByExampleElementGenerated(Document document, XmlElement element, IntrospectedColumn logicalDeleteColumn, String logicalDeleteValue, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            FormatTools.addElementWithBestPosition(document.getRootElement(), this.generateSqlMapLogicalDelete(
                    introspectedTable,
                    METHOD_LOGICAL_DELETE_WITH_VERSION_BY_EXAMPLE,
                    logicalDeleteColumn,
                    logicalDeleteValue,
                    true
            ));
        }
        return true;
    }

    @Override
    public boolean sqlMapLogicalDeleteByPrimaryKeyElementGenerated(Document document, XmlElement element, IntrospectedColumn logicalDeleteColumn, String logicalDeleteValue, IntrospectedTable introspectedTable) {
        if (this.versionColumn != null) {
            FormatTools.addElementWithBestPosition(document.getRootElement(), this.generateSqlMapLogicalDelete(
                    introspectedTable,
                    METHOD_LOGICAL_DELETE_WITH_VERSION_BY_PRIMARY_KEY,
                    logicalDeleteColumn,
                    logicalDeleteValue,
                    false
            ));
        }
        return true;
    }

    @Override
    public boolean logicalDeleteEnumGenerated(IntrospectedColumn logicalDeleteColumn) {
        return false;
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
     * @return
     */
    private Method replaceUpdateExampleMethod(IntrospectedTable introspectedTable, Method method, Interface interfaze, String methodName) {
        Method withVersionMethod = new Method(method);

        // 替换方法名
        withVersionMethod.setName(methodName);
        FormatTools.replaceGeneralMethodComment(commentGenerator, withVersionMethod, introspectedTable);

        Parameter versionParam = new Parameter(this.versionColumn.getFullyQualifiedJavaType(), "version", "@Param(\"version\")");

        withVersionMethod.addParameter(0, versionParam);

        return withVersionMethod;
    }

    /**
     * 替换Example 方法
     * @param introspectedTable
     * @param method
     * @param interfaze
     * @param methodName
     * @return
     */
    private Method replaceDeleteExampleMethod(IntrospectedTable introspectedTable, Method method, Interface interfaze, String methodName) {
        Method withVersionMethod = new Method(method);

        // 替换方法名
        withVersionMethod.setName(methodName);
        FormatTools.replaceGeneralMethodComment(commentGenerator, withVersionMethod, introspectedTable);

        Parameter versionParam = new Parameter(this.versionColumn.getFullyQualifiedJavaType(), "version", "@Param(\"version\")");
        Parameter exampleParam = new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example", "@Param(\"example\")");

        withVersionMethod.getParameters().clear();
        withVersionMethod.addParameter(versionParam);
        withVersionMethod.addParameter(exampleParam);

        return withVersionMethod;
    }

    /**
     * 替换主键 方法
     * @param introspectedTable
     * @param method
     * @param interfaze
     * @param methodName
     * @return
     */
    private Method replaceUpdatePrimaryKeyMethod(IntrospectedTable introspectedTable, Method method, Interface interfaze, String methodName) {
        Method withVersionMethod = new Method(method);

        // 替换方法名
        withVersionMethod.setName(methodName);
        FormatTools.replaceGeneralMethodComment(commentGenerator, withVersionMethod, introspectedTable);

        Parameter versionParam = new Parameter(this.versionColumn.getFullyQualifiedJavaType(), "version", "@Param(\"version\")");
        Parameter recordParam = new Parameter(method.getParameters().get(0).getType(), "record", "@Param(\"record\")");

        withVersionMethod.getParameters().clear();
        withVersionMethod.addParameter(versionParam);
        withVersionMethod.addParameter(recordParam);

        return withVersionMethod;
    }

    /**
     * 替换主键 方法
     * @param introspectedTable
     * @param method
     * @param interfaze
     * @param methodName
     * @return
     */
    private Method replaceDeletePrimaryKeyMethod(IntrospectedTable introspectedTable, Method method, Interface interfaze, String methodName) {
        Method withVersionMethod = new Method(method);

        // 替换方法名
        withVersionMethod.setName(methodName);
        FormatTools.replaceGeneralMethodComment(commentGenerator, withVersionMethod, introspectedTable);

        Parameter versionParam = new Parameter(this.versionColumn.getFullyQualifiedJavaType(), "version", "@Param(\"version\")");
        Parameter keyParam = new Parameter(method.getParameters().get(0).getType(), method.getParameters().get(0).getName(), "@Param(\"key\")");

        withVersionMethod.getParameters().clear();
        withVersionMethod.addParameter(versionParam);
        withVersionMethod.addParameter(keyParam);

        return withVersionMethod;
    }

    /**
     * 替换Example
     * @param introspectedTable
     * @param element
     * @param id
     * @return
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

        return withVersionEle;
    }

    /**
     * 替换 主键查询
     * @param introspectedTable
     * @param element
     * @param id
     * @param update
     * @return
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

        return withVersionEle;
    }

    /**
     * 生成update sql map
     * @param introspectedTable
     * @param columns
     * @param id
     * @param selective
     * @param byExample
     * @return
     */
    private XmlElement generateSqlMapUpdate(IntrospectedTable introspectedTable, List<IntrospectedColumn> columns, String id, boolean selective, boolean byExample) {
        // 移除版本列
        Iterator<IntrospectedColumn> columnIterator = columns.iterator();
        while (columnIterator.hasNext()) {
            IntrospectedColumn introspectedColumn = columnIterator.next();
            if (introspectedColumn.getActualColumnName().equals(this.versionColumn.getActualColumnName())) {
                columnIterator.remove();
            }
        }

        XmlElement updateEle = new XmlElement("update");

        updateEle.addAttribute(new Attribute("id", id));
        updateEle.addAttribute(new Attribute("parameterType", "map"));
        commentGenerator.addComment(updateEle);

        updateEle.addElement(new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
        if (selective) {
            XmlElement setEle = new XmlElement("set");
            updateEle.addElement(setEle);

            // set 节点
            XmlElement setsEle = XmlElementGeneratorTools.generateSetsSelective(columns, "record.");
            setEle.addElement(setsEle);

            XmlElement needVersionEle;
            if (PluginTools.getHook(IOptimisticLockerPluginHook.class).generateSetsSelectiveElement(columns, this.versionColumn, setsEle)) {
                needVersionEle = setEle;
            } else {
                needVersionEle = setsEle;
            }

            // 版本自增
            needVersionEle.addElement(0, this.generateVersionSetEle(selective));
        } else {
            // 版本自增
            updateEle.addElement(this.generateVersionSetEle(selective));
            // set 节点
            List<Element> setsEles = XmlElementGeneratorTools.generateSets(columns, "record.");
            //  XmlElementGeneratorTools.generateSets, 因为传入参数不可能带IdentityAndGeneratedAlwaysColumn所以返回的是set列表而不可能是trim 元素
            for (Element ele : setsEles) {
                updateEle.addElement(ele);
            }
        }

        // 更新条件
        if (byExample) {
            XmlElement ifElement = new XmlElement("if");
            ifElement.addAttribute(new Attribute("test", "_parameter != null"));

            XmlElement includeElement = new XmlElement("include");
            includeElement.addAttribute(new Attribute("refid", SQL_UPDATE_BY_EXAMPLE_WITH_VERSION_WHERE_CLAUSE));
            ifElement.addElement(includeElement);

            updateEle.addElement(ifElement);
        } else {
            updateEle = this.replacePrimaryKeyXmlElement(introspectedTable, updateEle, id, true);
        }

        return updateEle;
    }

    /**
     * 生成LogicalDelete sql map
     * @param introspectedTable
     * @param id
     * @param logicalDeleteColumn
     * @param logicalDeleteValue
     * @param byExample
     * @return
     */
    private XmlElement generateSqlMapLogicalDelete(IntrospectedTable introspectedTable, String id, IntrospectedColumn logicalDeleteColumn, String logicalDeleteValue, boolean byExample) {
        XmlElement updateEle = new XmlElement("update");

        updateEle.addAttribute(new Attribute("id", id));
        updateEle.addAttribute(new Attribute("parameterType", "map"));
        commentGenerator.addComment(updateEle);

        updateEle.addElement(new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));

        StringBuilder sb = new StringBuilder("set ");
        // 版本自增
        if (this.customizedNextVersion) {
            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(this.versionColumn));
            sb.append(" = ");
            sb.append("#{nextVersion,jdbcType=");
            sb.append(this.versionColumn.getJdbcTypeName());
            if (StringUtility.stringHasValue(this.versionColumn.getTypeHandler())) {
                sb.append(",typeHandler=");
                sb.append(this.versionColumn.getTypeHandler());
            }
            sb.append("}");
        } else {
            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(this.versionColumn));
            sb.append(" = ");
            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(this.versionColumn));
            sb.append(" + 1");
        }
        sb.append(",");

        // 逻辑删除 set
        sb.append(logicalDeleteColumn.getActualColumnName());
        sb.append(" = ");
        sb.append(XmlElementGeneratorTools.generateLogicalDeleteColumnValue(logicalDeleteColumn, logicalDeleteValue));
        updateEle.addElement(new TextElement(sb.toString()));

        // 更新条件
        if (byExample) {
            XmlElement ifElement = new XmlElement("if");
            ifElement.addAttribute(new Attribute("test", "_parameter != null"));

            XmlElement includeElement = new XmlElement("include");
            includeElement.addAttribute(new Attribute("refid", SQL_UPDATE_BY_EXAMPLE_WITH_VERSION_WHERE_CLAUSE));
            ifElement.addElement(includeElement);

            updateEle.addElement(ifElement);
        } else {
            updateEle = this.replacePrimaryKeyXmlElement(introspectedTable, updateEle, id, false);
        }

        return updateEle;
    }

    /**
     * 生成版本号set节点
     * @param selective
     * @return
     */
    private TextElement generateVersionSetEle(boolean selective) {
        if (this.customizedNextVersion) {
            return new TextElement(
                    (selective ? "" : "set ")
                            + MyBatis3FormattingUtilities.getEscapedColumnName(this.versionColumn)
                            + " = "
                            + MyBatis3FormattingUtilities.getParameterClause(this.versionColumn, "record.")
                            + ","
            );
        } else {
            return new TextElement(
                    (selective ? "" : "set ")
                            + MyBatis3FormattingUtilities.getEscapedColumnName(this.versionColumn)
                            + " = "
                            + MyBatis3FormattingUtilities.getEscapedColumnName(this.versionColumn)
                            + " + 1,"
            );
        }
    }
}
