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
import com.itfsw.mybatis.generator.plugins.utils.enhanced.SpecTypeArgumentsFullyQualifiedJavaType;
import com.itfsw.mybatis.generator.plugins.utils.hook.IIncrementsPluginHook;
import com.itfsw.mybatis.generator.plugins.utils.hook.ILombokPluginHook;
import com.itfsw.mybatis.generator.plugins.utils.hook.IModelBuilderPluginHook;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * ---------------------------------------------------------------------------
 * 增量插件
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/6/19 15:20
 * ---------------------------------------------------------------------------
 */
@Deprecated
public class IncrementsPlugin extends BasePlugin implements IModelBuilderPluginHook, IIncrementsPluginHook, ILombokPluginHook {
    public static final String PRO_INCREMENTS_COLUMNS = "incrementsColumns";  // incrementsColumns property
    public static final String FIELD_INC_MAP = "incrementsColumnsInfoMap";    // 为了防止和用户数据库字段冲突，特殊命名
    public static final String METHOD_GET_INC_MAP = "incrementsColumnsInfoMap"; // 获取inc map
    public static final String METHOD_INC_CHECK = "hasIncsForColumn";   // inc 检查方法名称

    private List<IntrospectedColumn> incColumns;   // 表启用增量操作的字段
    private InnerEnum incEnum;  // 增量Enum
    private InnerClass incEnumBuilder;  // 添加了增量Enum的builder

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param warnings
     * @return
     */
    @Override
    public boolean validate(List<String> warnings) {

        // 插件使用前提是使用了ModelBuilderPlugin插件
        if (!(PluginTools.checkDependencyPlugin(getContext(), ModelBuilderPlugin.class) || PluginTools.checkDependencyPlugin(getContext(), LombokPlugin.class))) {
            warnings.add("itfsw:插件" + this.getClass().getTypeName() + "插件需配合" + ModelBuilderPlugin.class.getTypeName() + "或者" + LombokPlugin.class.getTypeName() + "插件使用！");
            return false;
        }

        // 插件使用前提是使用了ModelBuilderPlugin插件
        if (PluginTools.checkDependencyPlugin(getContext(), IncrementPlugin.class)) {
            warnings.add("itfsw:插件" + this.getClass().getTypeName() + "插件和" + IncrementPlugin.class.getTypeName() + "插件冲突！");
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
        this.incColumns = new ArrayList<>();
        this.incEnum = null;
        this.incEnumBuilder = null;

        String incrementsColumns = introspectedTable.getTableConfigurationProperty(IncrementsPlugin.PRO_INCREMENTS_COLUMNS);
        if (StringUtility.stringHasValue(incrementsColumns)) {
            // 切分
            String[] incrementsColumnsStrs = incrementsColumns.split(",");
            for (String incrementsColumnsStr : incrementsColumnsStrs) {
                IntrospectedColumn column = IntrospectedTableTools.safeGetColumn(introspectedTable, incrementsColumnsStr);
                if (column == null) {
                    warnings.add("itfsw:插件" + IncrementsPlugin.class.getTypeName() + "插件没有找到column为" + incrementsColumnsStr.trim() + "的字段！");
                } else {
                    incColumns.add(column);
                }
            }
        }
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param element
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        generatedWithSelective(element, introspectedTable, true);
        return super.sqlMapUpdateByExampleSelectiveElementGenerated(element, introspectedTable);
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param element
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapUpdateByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        generatedWithoutSelective(element, introspectedTable, true);
        return super.sqlMapUpdateByExampleWithBLOBsElementGenerated(element, introspectedTable);
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param element
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapUpdateByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        generatedWithoutSelective(element, introspectedTable, true);
        return super.sqlMapUpdateByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param element
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        generatedWithSelective(element, introspectedTable, false);
        return super.sqlMapUpdateByPrimaryKeySelectiveElementGenerated(element, introspectedTable);
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param element
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        generatedWithoutSelective(element, introspectedTable, false);
        return super.sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(element, introspectedTable);
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param element
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        generatedWithoutSelective(element, introspectedTable, false);
        return super.sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(element, introspectedTable);
    }

    // =============================================== ILombokPluginHook ===================================================

    @Override
    public boolean modelBaseRecordBuilderClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return this.lombokBuilderClassGenerated(topLevelClass, IntrospectedTableTools.getModelBaseRecordClomns(introspectedTable), introspectedTable);
    }

    @Override
    public boolean modelPrimaryKeyBuilderClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return this.lombokBuilderClassGenerated(topLevelClass, introspectedTable.getPrimaryKeyColumns(), introspectedTable);
    }

    @Override
    public boolean modelRecordWithBLOBsBuilderClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return this.lombokBuilderClassGenerated(topLevelClass, introspectedTable.getBLOBColumns(), introspectedTable);
    }

    /**
     * Lombok Builder 生成
     * @param topLevelClass
     * @param columns
     * @param introspectedTable
     * @return
     */
    private boolean lombokBuilderClassGenerated(TopLevelClass topLevelClass, List<IntrospectedColumn> columns, IntrospectedTable introspectedTable) {
        if (this.support()) {
            boolean find = false;
            for (IntrospectedColumn column : columns) {
                if (this.supportIncrement(column)) {
                    find = true;
                    break;
                }
            }
            if (find) {
                // ----------------------------------- topLevelClass 方法 --------------------------------
                FullyQualifiedJavaType builderType = new FullyQualifiedJavaType(topLevelClass.getType().getShortName() + "." + topLevelClass.getType().getShortName() + "Builder");
                builderType.addTypeArgument(new SpecTypeArgumentsFullyQualifiedJavaType("<?, ?>"));

                // 增加构造函数
                Method constructor = new Method(topLevelClass.getType().getShortName());
                commentGenerator.addGeneralMethodComment(constructor, introspectedTable);
                constructor.setVisibility(JavaVisibility.PROTECTED);
                constructor.setConstructor(true);
                constructor.addParameter(new Parameter(builderType, "builder"));
                // 是否调用父类构造函数
                if (topLevelClass.getSuperClass() != null) {
                    constructor.addBodyLine("super(builder);");
                }
                for (IntrospectedColumn column : columns) {
                    Field field = JavaBeansUtil.getJavaBeansField(column, context, introspectedTable);
                    constructor.addBodyLine("this." + field.getName() + " = builder." + field.getName() + ";");
                }
                FormatTools.addMethodWithBestPosition(topLevelClass, constructor);

                // 增加静态builder方法实现和lombok一样
                Method builderMethod = JavaElementGeneratorTools.generateMethod(
                        "builder",
                        JavaVisibility.PUBLIC,
                        builderType
                );
                commentGenerator.addGeneralMethodComment(builderMethod, introspectedTable);
                builderMethod.setStatic(true);
                builderMethod.addBodyLine("return new " + topLevelClass.getType().getShortName() + "." + topLevelClass.getType().getShortName() + "BuilderImpl();");
                FormatTools.addMethodWithBestPosition(topLevelClass, builderMethod);

                // ------------------------------ builder Class ----------------------------------
                InnerClass builderCls = new InnerClass(topLevelClass.getType().getShortName() + "Builder");
                commentGenerator.addClassComment(builderCls, introspectedTable);
                builderCls.setVisibility(JavaVisibility.PUBLIC);
                builderCls.setStatic(true);
                builderCls.setAbstract(true);

                builderCls.getType().addTypeArgument(
                        new SpecTypeArgumentsFullyQualifiedJavaType("<C extends " + topLevelClass.getType().getShortName()
                                + ", B extends " + topLevelClass.getType().getShortName()
                                + "." + topLevelClass.getType().getShortName() + "Builder<C, B>>")
                );

                if (topLevelClass.getSuperClass() != null) {
                    FullyQualifiedJavaType superBuilderCls = new FullyQualifiedJavaType(topLevelClass.getSuperClass().getShortName() + "Builder");
                    superBuilderCls.addTypeArgument(new SpecTypeArgumentsFullyQualifiedJavaType("<C, B>"));
                    builderCls.setSuperClass(superBuilderCls);
                }

                // 类注解
                topLevelClass.addImportedType("lombok.Setter");
                builderCls.addAnnotation("@Setter");
                topLevelClass.addImportedType("lombok.experimental.Accessors");
                builderCls.addAnnotation("@Accessors(fluent = true)");
                if (topLevelClass.getSuperClass() != null) {
                    topLevelClass.addImportedType("lombok.ToString");
                    builderCls.addAnnotation("@ToString(callSuper = true)");
                } else {
                    topLevelClass.addImportedType("lombok.ToString");
                    builderCls.addAnnotation("@ToString");
                }


                for (IntrospectedColumn introspectedColumn : columns) {
                    Field field = JavaBeansUtil.getJavaBeansField(introspectedColumn, context, introspectedTable);
                    field.getJavaDocLines().clear();
                    commentGenerator.addFieldComment(field, introspectedTable);

                    builderCls.addField(field);
                }

                // self 方法
                Method selfMethod1 = JavaElementGeneratorTools.generateMethod(
                        "self",
                        JavaVisibility.PROTECTED,
                        new FullyQualifiedJavaType("B")
                );
                commentGenerator.addGeneralMethodComment(selfMethod1, introspectedTable);
                FormatTools.addMethodWithBestPosition(builderCls, selfMethod1);

                // build 方法
                Method buildMethod1 = JavaElementGeneratorTools.generateMethod(
                        "build",
                        JavaVisibility.PUBLIC,
                        new FullyQualifiedJavaType("C")
                );
                commentGenerator.addGeneralMethodComment(buildMethod1, introspectedTable);
                FormatTools.addMethodWithBestPosition(builderCls, buildMethod1);


                topLevelClass.addInnerClass(builderCls);


                // --------------------------------- 生成自增插件需要的方法 --------------------------------------
                // 增加枚举
                if (this.incEnum == null) {
                    this.incEnum = this.generatedIncEnum(introspectedTable);
                    this.incEnumBuilder = builderCls;

                    builderCls.addInnerEnum(this.incEnum);
                    // topLevel class 添加必要的操作方法
                    this.addIncMethodToTopLevelClass(topLevelClass, builderCls, introspectedTable, true);

                    // Builder 中 添加字段支持
                    Field fIncrements = JavaElementGeneratorTools.generateField(
                            IncrementsPlugin.FIELD_INC_MAP,
                            JavaVisibility.PROTECTED,
                            new FullyQualifiedJavaType("Map<String, " + this.getIncEnum(builderCls, introspectedTable).getFullyQualifiedName() + ">"),
                            "new HashMap<String, " + this.getIncEnum(builderCls, introspectedTable).getFullyQualifiedName() + ">()"
                    );
                    commentGenerator.addFieldComment(fIncrements, introspectedTable);
                    builderCls.addField(fIncrements);

                    // Builder 构造函数增加 自增Map
                    constructor.addBodyLine("this." + IncrementsPlugin.FIELD_INC_MAP + ".putAll(builder." + IncrementsPlugin.FIELD_INC_MAP + ");");
                }

                FullyQualifiedJavaType builderType2 = new FullyQualifiedJavaType(topLevelClass.getType().getShortName() + "." + topLevelClass.getType().getShortName() + "Builder");
                builderType2.addTypeArgument(new SpecTypeArgumentsFullyQualifiedJavaType("<C, B>"));
                for (IntrospectedColumn column : columns) {
                    if (this.supportIncrement(column)) {
                        Field field = JavaBeansUtil.getJavaBeansField(column, context, introspectedTable);
                        // 增加方法
                        Method mIncrements = JavaElementGeneratorTools.generateMethod(
                                field.getName(),
                                JavaVisibility.PUBLIC,
                                builderType2,
                                new Parameter(field.getType(), field.getName()),
                                new Parameter(this.getIncEnum(builderCls, introspectedTable), "inc")
                        );
                        commentGenerator.addSetterComment(mIncrements, introspectedTable, column);
                        mIncrements.addBodyLine("this." + IncrementsPlugin.FIELD_INC_MAP + ".put(\"" + column.getActualColumnName() + "\", inc);");
                        mIncrements.addBodyLine("return this." + field.getName() + "(" + field.getName() + ");");

                        FormatTools.addMethodWithBestPosition(builderCls, mIncrements);
                    }
                }

                // ------------------------------ builderImpl Class ----------------------------------
                InnerClass builderImplCls = new InnerClass(topLevelClass.getType().getShortName() + "BuilderImpl");
                commentGenerator.addClassComment(builderImplCls, introspectedTable);
                FullyQualifiedJavaType builderType1 = new FullyQualifiedJavaType(topLevelClass.getType().getShortName() + "." + topLevelClass.getType().getShortName() + "Builder");
                builderType1.addTypeArgument(new SpecTypeArgumentsFullyQualifiedJavaType(
                        "<" + topLevelClass.getType().getShortName() + ", " + topLevelClass.getType().getShortName() + "." + topLevelClass.getType().getShortName() + "BuilderImpl" + ">"
                ));
                builderImplCls.setSuperClass(builderType1);
                builderImplCls.setVisibility(JavaVisibility.PRIVATE);
                builderImplCls.setFinal(true);
                builderImplCls.setStatic(true);

                topLevelClass.addInnerClass(builderImplCls);

                // self 方法
                Method selfMethod = JavaElementGeneratorTools.generateMethod(
                        "self",
                        JavaVisibility.PROTECTED,
                        new FullyQualifiedJavaType(topLevelClass.getType().getShortName() + "." + topLevelClass.getType().getShortName() + "BuilderImpl")
                );
                commentGenerator.addGeneralMethodComment(selfMethod, introspectedTable);
                selfMethod.addBodyLine("return this;");
                FormatTools.addMethodWithBestPosition(builderImplCls, selfMethod);

                // build 方法
                Method buildMethod = JavaElementGeneratorTools.generateMethod(
                        "build",
                        JavaVisibility.PUBLIC,
                        topLevelClass.getType()
                );
                commentGenerator.addGeneralMethodComment(buildMethod, introspectedTable);
                buildMethod.addBodyLine("return new " + topLevelClass.getType().getShortName() + "(this);");
                FormatTools.addMethodWithBestPosition(builderImplCls, buildMethod);

                return false;
            }
        }
        return true;
    }

    // =============================================== IModelBuilderPluginHook ===================================================

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
        if (this.support()) {
            if (this.incEnum == null) {
                this.incEnum = this.generatedIncEnum(introspectedTable);
                this.incEnumBuilder = builderClass;
                // 增加枚举
                builderClass.addInnerEnum(this.incEnum);
                // topLevel class 添加必要的操作方法
                this.addIncMethodToTopLevelClass(topLevelClass, builderClass, introspectedTable, false);
            }


            // Builder 中 添加字段支持
            for (IntrospectedColumn column : columns) {
                if (this.supportIncrement(column)) {
                    Field field = JavaBeansUtil.getJavaBeansField(column, context, introspectedTable);
                    // 增加方法
                    Method mIncrements = JavaElementGeneratorTools.generateMethod(
                            field.getName(),
                            JavaVisibility.PUBLIC,
                            builderClass.getType(),
                            new Parameter(field.getType(), field.getName()),
                            new Parameter(this.getIncEnum(builderClass, introspectedTable), "inc")
                    );
                    commentGenerator.addSetterComment(mIncrements, introspectedTable, column);

                    Method setterMethod = JavaBeansUtil.getJavaBeansSetter(column, context, introspectedTable);
                    mIncrements.addBodyLine("obj." + IncrementsPlugin.FIELD_INC_MAP + ".put(\"" + column.getActualColumnName() + "\", inc);");
                    mIncrements.addBodyLine("obj." + setterMethod.getName() + "(" + field.getName() + ");");
                    mIncrements.addBodyLine("return this;");

                    FormatTools.addMethodWithBestPosition(builderClass, mIncrements);
                }
            }
        }
        return true;
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
        return true;
    }

    // =============================================== IIncrementsPluginHook ===================================================

    /**
     * 生成增量操作节点
     * @param introspectedColumn
     * @param prefix
     * @param hasComma
     * @return
     */
    @Override
    public List<Element> incrementSetElementGenerated(IntrospectedColumn introspectedColumn, String prefix, boolean hasComma) {
        List<Element> list = new ArrayList<>();

        if (this.supportIncrement(introspectedColumn)) {
            // 1. column = 节点
            list.add(new TextElement(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn) + " = "));

            // 2. 选择节点
            // 条件
            XmlElement choose = new XmlElement("choose");

            // 没有启用增量操作
            XmlElement when = new XmlElement("when");
            when.addAttribute(new Attribute(
                    "test",
                    (prefix != null ? prefix : "_parameter.") + IncrementsPlugin.METHOD_INC_CHECK
                            + "('" + MyBatis3FormattingUtilities.escapeStringForMyBatis3(introspectedColumn.getActualColumnName()) + "')"
            ));
            TextElement spec = new TextElement(
                    MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn)
                            + " ${" + (prefix != null ? prefix : "_parameter.")
                            + IncrementsPlugin.METHOD_GET_INC_MAP + "()." + MyBatis3FormattingUtilities.escapeStringForMyBatis3(introspectedColumn.getActualColumnName()) + ".value} "
                            + MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, prefix));
            when.addElement(spec);
            choose.addElement(when);

            // 启用了增量操作
            XmlElement otherwise = new XmlElement("otherwise");
            TextElement normal = new TextElement(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, prefix));
            otherwise.addElement(normal);
            choose.addElement(otherwise);

            list.add(choose);

            // 3. 结尾逗号
            if (hasComma) {
                list.add(new TextElement(","));
            }
        }

        return list;
    }

    /**
     * 生成增量操作节点(SelectiveEnhancedPlugin)
     * @param columns
     * @return
     */
    @Override
    public List<XmlElement> incrementSetsWithSelectiveEnhancedPluginElementGenerated(List<IntrospectedColumn> columns) {
        if (this.support()) {
            List<XmlElement> results = new ArrayList<>();

            for (IntrospectedColumn incColumn : this.incColumns) {

                // !!! 不能用contains,IntrospectedColumn对象不同
                for (IntrospectedColumn column : columns) {
                    if (incColumn.getActualColumnName().equals(column.getActualColumnName())) {
                        XmlElement when = new XmlElement("when");

                        // 需要 inc 的列
                        when.addAttribute(new Attribute("test", "'" + column.getActualColumnName() + "'.toString() == column.value"));
                        when.addElement(new TextElement("${column.escapedColumnName} = ${column.escapedColumnName} ${record." + METHOD_GET_INC_MAP + "()."
                                + incColumn.getActualColumnName()
                                + ".value} "
                                + XmlElementGeneratorTools.getParameterClause("record.${column.javaProperty}", incColumn)));
                        results.add(when);
                    }
                }
            }

            return results.isEmpty() ? null : results;
        }
        return null;
    }

    /**
     * 判断是否为需要进行增量操作的column
     * @param column
     * @return
     */
    @Override
    public boolean supportIncrement(IntrospectedColumn column) {
        for (IntrospectedColumn incColumn : this.incColumns) {
            if (incColumn.getActualColumnName().equals(column.getActualColumnName())) {
                return true;
            }
        }
        return false;
    }

    // =================================================== 原生方法的支持 ====================================================

    /**
     * 向topLevelClass 添加必要的操作函数
     * @param topLevelClass
     * @param builderCls
     * @param introspectedTable
     * @param withLombok
     */
    private void addIncMethodToTopLevelClass(TopLevelClass topLevelClass, InnerClass builderCls, IntrospectedTable introspectedTable, boolean withLombok) {
        // 增加field
        Field fIncrements = JavaElementGeneratorTools.generateField(
                IncrementsPlugin.FIELD_INC_MAP,
                JavaVisibility.PROTECTED,
                new FullyQualifiedJavaType("Map<String, " + this.getIncEnum(builderCls, introspectedTable).getFullyQualifiedName() + ">"),
                "new HashMap<String, " + this.getIncEnum(builderCls, introspectedTable).getFullyQualifiedName() + ">()"
        );
        fIncrements.setFinal(true);
        commentGenerator.addFieldComment(fIncrements, introspectedTable);
        topLevelClass.addField(fIncrements);
        topLevelClass.addImportedType("java.util.Map");
        topLevelClass.addImportedType("java.util.HashMap");
        // inc map 获取方法
        if (withLombok) {
            topLevelClass.addImportedType("lombok.experimental.Accessors");
            fIncrements.addAnnotation("@Accessors(fluent = true)");
        } else {
            Method getIncMapMethod = JavaElementGeneratorTools.generateMethod(
                    METHOD_GET_INC_MAP,
                    JavaVisibility.PUBLIC,
                    fIncrements.getType()
            );
            commentGenerator.addGeneralMethodComment(getIncMapMethod, introspectedTable);
            getIncMapMethod.addBodyLine("return this." + FIELD_INC_MAP + ";");
            FormatTools.addMethodWithBestPosition(topLevelClass, getIncMapMethod);
        }
        // 增加判断方法
        Method mHasIncsForColumn = JavaElementGeneratorTools.generateMethod(
                IncrementsPlugin.METHOD_INC_CHECK,
                JavaVisibility.PUBLIC,
                FullyQualifiedJavaType.getBooleanPrimitiveInstance(),
                new Parameter(FullyQualifiedJavaType.getStringInstance(), "column")
        );
        commentGenerator.addGeneralMethodComment(mHasIncsForColumn, introspectedTable);
        mHasIncsForColumn.addBodyLine("return " + IncrementsPlugin.FIELD_INC_MAP + ".get(column) != null;");
        FormatTools.addMethodWithBestPosition(topLevelClass, mHasIncsForColumn);
    }

    /**
     * 生成Inc enum
     * @param introspectedTable
     * @return
     */
    private InnerEnum generatedIncEnum(IntrospectedTable introspectedTable) {
        // 增加枚举
        InnerEnum eIncrements = new InnerEnum(new FullyQualifiedJavaType("Inc"));
        eIncrements.setVisibility(JavaVisibility.PUBLIC);
        eIncrements.setStatic(true);
        eIncrements.addEnumConstant("INC(\"+\")");
        eIncrements.addEnumConstant("DEC(\"-\")");
        commentGenerator.addEnumComment(eIncrements, introspectedTable);
        // 生成属性和构造函数
        Field fValue = new Field("value", FullyQualifiedJavaType.getStringInstance());
        fValue.setVisibility(JavaVisibility.PRIVATE);
        fValue.setFinal(true);
        commentGenerator.addFieldComment(fValue, introspectedTable);
        eIncrements.addField(fValue);

        Method mInc = new Method("Inc");
        mInc.setConstructor(true);
        mInc.addBodyLine("this.value = value;");
        mInc.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "value"));
        commentGenerator.addGeneralMethodComment(mInc, introspectedTable);
        FormatTools.addMethodWithBestPosition(eIncrements, mInc);

        Method mValue = JavaElementGeneratorTools.generateGetterMethod(fValue);
        commentGenerator.addGeneralMethodComment(mValue, introspectedTable);
        FormatTools.addMethodWithBestPosition(eIncrements, mValue);

        return eIncrements;
    }

    /**
     * 有Selective代码生成
     * @param element
     */
    private void generatedWithSelective(XmlElement element, IntrospectedTable introspectedTable, boolean hasPrefix) {
        if (this.support()) {
            // 查找 set->if->text
            List<XmlElement> sets = XmlElementTools.findXmlElements(element, "set");
            if (sets.size() > 0) {
                List<XmlElement> ifs = XmlElementTools.findXmlElements(sets.get(0), "if");
                if (ifs.size() > 0) {
                    for (XmlElement xmlElement : ifs) {
                        // 下面为if的text节点
                        List<Element> textEles = xmlElement.getElements();
                        TextElement textEle = (TextElement) textEles.get(0);
                        String[] strs = textEle.getContent().split("=");
                        String columnName = strs[0].trim();
                        IntrospectedColumn introspectedColumn = IntrospectedTableTools.safeGetColumn(introspectedTable, columnName);
                        // 查找是否需要进行增量操作
                        List<Element> incrementEles = PluginTools.getHook(IIncrementsPluginHook.class).incrementSetElementGenerated(introspectedColumn, hasPrefix ? "record." : null, true);
                        if (!incrementEles.isEmpty()) {
                            xmlElement.getElements().clear();
                            xmlElement.getElements().addAll(incrementEles);
                        }
                    }
                }
            }
        }
    }

    /**
     * 无Selective代码生成
     * @param xmlElement
     * @param introspectedTable
     * @param hasPrefix
     */
    private void generatedWithoutSelective(XmlElement xmlElement, IntrospectedTable introspectedTable, boolean hasPrefix) {
        if (this.support()) {
            List<Element> newEles = new ArrayList<>();
            for (Element ele : xmlElement.getElements()) {
                // 找到text节点且格式为 set xx = xx 或者 xx = xx
                if (ele instanceof TextElement) {
                    String text = ((TextElement) ele).getContent().trim();
                    if (text.matches("(^set\\s)?\\S+\\s?=.*")) {
                        // 清理 set 操作
                        text = text.replaceFirst("^set\\s", "").trim();
                        String columnName = text.split("=")[0].trim();
                        IntrospectedColumn introspectedColumn = IntrospectedTableTools.safeGetColumn(introspectedTable, columnName);
                        // 查找判断是否需要进行节点替换
                        List<Element> incrementEles = PluginTools.getHook(IIncrementsPluginHook.class).incrementSetElementGenerated(introspectedColumn, hasPrefix ? "record." : null, text.endsWith(","));
                        if (!incrementEles.isEmpty()) {
                            newEles.addAll(incrementEles);

                            continue;
                        }
                    }
                }
                newEles.add(ele);
            }

            // 替换节点
            xmlElement.getElements().clear();
            xmlElement.getElements().addAll(newEles);
        }
    }

    /**
     * 获取INC Enum
     * @param builderCls
     * @return
     */
    private FullyQualifiedJavaType getIncEnum(InnerClass builderCls, IntrospectedTable introspectedTable) {
        try {
            return new FullyQualifiedJavaType(BeanUtils.getProperty(this.incEnumBuilder.getType(), "baseShortName").toString() + "." + this.incEnum.getType().getShortName());
        } catch (Exception e) {
            logger.error("获取Inc enum 失败！", e);
        }
        return null;
    }

    /**
     * 是否启用了
     * @return
     */
    private boolean support() {
        return this.incColumns.size() > 0;
    }
}
