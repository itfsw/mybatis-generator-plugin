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
import com.itfsw.mybatis.generator.plugins.utils.hook.IIncrementsPluginHook;
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
public class IncrementsPlugin extends BasePlugin implements IModelBuilderPluginHook, IIncrementsPluginHook {
    public static final String PRO_INCREMENTS_COLUMNS = "incrementsColumns";  // incrementsColumns property
    public static final String FIELD_INC_MAP = "incrementsColumnsInfoMap";    // 为了防止和用户数据库字段冲突，特殊命名
    public static final String METHOD_INC_CHECK = "hasIncsForColumn";   // inc 检查方法名称
    private IncrementsPluginTools incTools; // 增量插件工具

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param warnings
     * @return
     */
    @Override
    public boolean validate(List<String> warnings) {

        // 插件使用前提是使用了ModelBuilderPlugin插件
        if (!PluginTools.checkDependencyPlugin(getContext(), ModelBuilderPlugin.class)) {
            warnings.add("itfsw:插件" + this.getClass().getTypeName() + "插件需配合com.itfsw.mybatis.generator.plugins.ModelBuilderPlugin插件使用！");
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
        this.incTools = IncrementsPluginTools.getTools(context, introspectedTable, warnings);
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
        eIncrements.addMethod(mInc);

        Method mValue = JavaElementGeneratorTools.generateGetterMethod(fValue);
        commentGenerator.addGeneralMethodComment(mValue, introspectedTable);
        eIncrements.addMethod(mValue);

        builderClass.addInnerEnum(eIncrements);
        // 增加field
        Field fIncrements = JavaElementGeneratorTools.generateField(
                IncrementsPlugin.FIELD_INC_MAP,
                JavaVisibility.PROTECTED,
                new FullyQualifiedJavaType("Map<String, " + incTools.getIncEnum().getFullyQualifiedName() + ">"),
                "new HashMap<String, " + incTools.getIncEnum().getFullyQualifiedName() + ">()"
        );
        commentGenerator.addFieldComment(fIncrements, introspectedTable);
        topLevelClass.addField(fIncrements);
        topLevelClass.addImportedType("java.util.Map");
        topLevelClass.addImportedType("java.util.HashMap");
        // getter&setter
        Method mGetter = JavaElementGeneratorTools.generateGetterMethod(fIncrements);
        commentGenerator.addGetterComment(mGetter, introspectedTable, null);
        topLevelClass.addMethod(mGetter);
        Method mSetter = JavaElementGeneratorTools.generateSetterMethod(fIncrements);
        commentGenerator.addSetterComment(mSetter, introspectedTable, null);
        topLevelClass.addMethod(mSetter);
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

        // Builder 中 添加字段支持
        for (IntrospectedColumn column : columns) {
            if (incTools.supportColumn(column)) {
                Field field = JavaBeansUtil.getJavaBeansField(column, context, introspectedTable);
                // 增加方法
                Method mIncrements = JavaElementGeneratorTools.generateMethod(
                        field.getName(),
                        JavaVisibility.PUBLIC,
                        builderClass.getType(),
                        new Parameter(field.getType(), field.getName()),
                        new Parameter(incTools.getIncEnum(), "inc")
                );
                commentGenerator.addSetterComment(mIncrements, introspectedTable, column);

                Method setterMethod = JavaBeansUtil.getJavaBeansSetter(column, context, introspectedTable);
                mIncrements.addBodyLine("obj." + IncrementsPlugin.FIELD_INC_MAP + ".put(\"" + column.getActualColumnName() + "\", inc);");
                mIncrements.addBodyLine("obj." + setterMethod.getName() + "(" + field.getName() + ");");
                mIncrements.addBodyLine("return this;");

                FormatTools.addMethodWithBestPosition(builderClass, mIncrements);
            }
        }
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
    public List<Element> incrementElementGenerated(IntrospectedColumn introspectedColumn, String prefix, boolean hasComma) {
        List<Element> list = new ArrayList<>();

        if (incTools.supportColumn(introspectedColumn)){
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
                            + " ${" + (prefix != null ? prefix : "")
                            + IncrementsPlugin.FIELD_INC_MAP + "." + MyBatis3FormattingUtilities.escapeStringForMyBatis3(introspectedColumn.getActualColumnName()) + ".value} "
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
    // =================================================== 原生方法的支持 ====================================================

    /**
     * 有Selective代码生成
     * @param element
     */
    private void generatedWithSelective(XmlElement element, IntrospectedTable introspectedTable, boolean hasPrefix) {
        if (incTools.support()) {
            // 查找 set->if->text
            List<XmlElement> sets = XmlElementGeneratorTools.findXmlElements(element, "set");
            if (sets.size() > 0) {
                List<XmlElement> ifs = XmlElementGeneratorTools.findXmlElements(sets.get(0), "if");
                if (ifs.size() > 0) {
                    for (XmlElement xmlElement : ifs) {
                        // 下面为if的text节点
                        List<Element> textEles = xmlElement.getElements();
                        TextElement textEle = (TextElement) textEles.get(0);
                        String[] strs = textEle.getContent().split("=");
                        String columnName = strs[0].trim();
                        IntrospectedColumn introspectedColumn = IntrospectedTableTools.safeGetColumn(introspectedTable, columnName);
                        // 查找是否需要进行增量操作
                        List<Element> incrementEles = PluginTools.getHook(IIncrementsPluginHook.class).incrementElementGenerated(introspectedColumn, hasPrefix ? "record." : null, true);
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
        if (incTools.support()) {
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
                        List<Element> incrementEles = PluginTools.getHook(IIncrementsPluginHook.class).incrementElementGenerated(introspectedColumn, hasPrefix ? "record." : null, text.endsWith(","));
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
}
