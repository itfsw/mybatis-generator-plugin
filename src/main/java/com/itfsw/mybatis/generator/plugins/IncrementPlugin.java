/*
 * Copyright (c) 2019.
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
import com.itfsw.mybatis.generator.plugins.utils.hook.IIncrementPluginHook;
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
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2019/7/4 11:01
 * ---------------------------------------------------------------------------
 */
public class IncrementPlugin extends BasePlugin implements IIncrementPluginHook {
    public static final String PRO_INCREMENT_COLUMNS = "incrementColumns";

    /**
     * 为了防止和用户数据库字段冲突，特殊命名
     */
    public static final String FIELD_INC_MAP = "incrementColumnsInfoMap";
    /**
     * 自增方法名称
     */
    public static final String METHOD_INCREMENT = "increment";

    /**
     * Increment 类
     */
    public static final String ENUM_INCREMENT = "Increment";
    public static final String CLASS_INCREMENT_ITEM = "Item";
    public static final String FIELD_COLUMN_FOR_CLASS_INCREMENT = "column";
    public static final String FIELD_VALUE_FOR_CLASS_INCREMENT = "value";
    public static final String FIELD_OPERATE_FOR_CLASS_INCREMENT = "operate";

    /**
     * 增减方法
     */
    public static final String METHOD_INC = "inc";
    public static final String METHOD_DEC = "dec";

    /**
     * 表启用增量操作的字段
     */
    private List<IntrospectedColumn> incColumns;

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param warnings
     * @return
     */
    @Override
    public boolean validate(List<String> warnings) {

        // 插件使用前提是使用了ModelBuilderPlugin插件
        if (!PluginTools.checkDependencyPlugin(getContext(), ModelColumnPlugin.class)) {
            warnings.add("itfsw:插件" + this.getClass().getTypeName() + "插件需配合" + ModelColumnPlugin.class.getTypeName() + "插件使用！");
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

        String incrementColumns = introspectedTable.getTableConfigurationProperty(PRO_INCREMENT_COLUMNS);
        if (StringUtility.stringHasValue(incrementColumns)) {
            // 切分
            String[] incrementsColumnsStrs = incrementColumns.split(",");
            for (String incrementsColumnsStr : incrementsColumnsStrs) {
                IntrospectedColumn column = IntrospectedTableTools.safeGetColumn(introspectedTable, incrementsColumnsStr);
                if (column == null) {
                    warnings.add("itfsw:插件" + IncrementPlugin.class.getTypeName() + "插件没有找到column为" + incrementsColumnsStr.trim() + "的字段！");
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

    /**
     * Model Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.generateIncrement(topLevelClass, introspectedTable);
        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * Model Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.generateIncrement(topLevelClass, introspectedTable);
        return super.modelRecordWithBLOBsClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.generateIncrement(topLevelClass, introspectedTable);
        return super.modelPrimaryKeyClassGenerated(topLevelClass, introspectedTable);
    }

    // ================================================== IIncrementPluginHook =================================================

    @Override
    public XmlElement generateIncrementSet(IntrospectedColumn introspectedColumn, String prefix, boolean hasComma) {
        if (this.supportIncrement(introspectedColumn)) {
            // 条件
            XmlElement choose = new XmlElement("choose");

            // 启用增量操作
            String columnMap = (prefix != null ? prefix : "_parameter.") + FIELD_INC_MAP + "." + MyBatis3FormattingUtilities.escapeStringForMyBatis3(introspectedColumn.getActualColumnName());
            XmlElement whenIncEle = new XmlElement("when");
            whenIncEle.addAttribute(new Attribute("test", columnMap + " != null"));
            TextElement spec = new TextElement(
                    MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn) + " = " +
                            MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn)
                            + " ${" + columnMap + "." + FIELD_OPERATE_FOR_CLASS_INCREMENT + "} "
                            + XmlElementGeneratorTools.getParameterClause(columnMap + "." + FIELD_VALUE_FOR_CLASS_INCREMENT, introspectedColumn)
                            + (hasComma ? "," : ""));
            whenIncEle.addElement(spec);
            choose.addElement(whenIncEle);

            // 没有启用增量操作
            XmlElement otherwiseEle = new XmlElement("otherwise");
            otherwiseEle.addElement(new TextElement(
                    MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn) + " = " + MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, prefix)
                            + (hasComma ? "," : "")
            ));
            choose.addElement(otherwiseEle);

            return choose;
        }
        return null;
    }

    @Override
    public XmlElement generateIncrementSetSelective(IntrospectedColumn introspectedColumn, String prefix) {
        if (this.supportIncrement(introspectedColumn)) {
            // 条件
            XmlElement choose = new XmlElement("choose");

            // 启用增量操作
            String columnMap = (prefix != null ? prefix : "_parameter.") + FIELD_INC_MAP + "." + MyBatis3FormattingUtilities.escapeStringForMyBatis3(introspectedColumn.getActualColumnName());
            XmlElement whenIncEle = new XmlElement("when");
            whenIncEle.addAttribute(new Attribute("test", columnMap + " != null"));
            TextElement spec = new TextElement(
                    MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn) + " = " +
                            MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn)
                            + " ${" + columnMap + "." + FIELD_OPERATE_FOR_CLASS_INCREMENT + "} "
                            + XmlElementGeneratorTools.getParameterClause(columnMap + "." + FIELD_VALUE_FOR_CLASS_INCREMENT, introspectedColumn)
                            + ",");
            whenIncEle.addElement(spec);
            choose.addElement(whenIncEle);

            // 没有启用增量操作
            XmlElement whenEle = new XmlElement("when");
            whenEle.addAttribute(new Attribute("test", introspectedColumn.getJavaProperty(prefix) + " != null"));
            whenEle.addElement(new TextElement(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn) + " = " + MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, prefix) + ","));
            choose.addElement(whenEle);

            return choose;
        }
        return null;
    }

    /**
     * 生成增量操作节点(SelectiveEnhancedPlugin)
     * @param columns
     * @return
     * @see SelectiveEnhancedPlugin#generateSetsSelective(List, IntrospectedColumn)
     */
    @Override
    public List<XmlElement> generateIncrementSetForSelectiveEnhancedPlugin(List<IntrospectedColumn> columns) {
        if (this.support()) {
            List<XmlElement> results = new ArrayList<>();
            for (IntrospectedColumn incColumn : this.incColumns) {
                // !!! 不能用contains,IntrospectedColumn对象不同
                for (IntrospectedColumn column : columns) {
                    if (incColumn.getActualColumnName().equals(column.getActualColumnName())) {
                        XmlElement when = new XmlElement("when");

                        // 需要 inc 的列
                        String columnMap = "record." + FIELD_INC_MAP + "." + MyBatis3FormattingUtilities.escapeStringForMyBatis3(incColumn.getActualColumnName());

                        when.addAttribute(new Attribute("test", "'" + column.getActualColumnName() + "'.toString() == column.value and " + columnMap + " != null"));
                        when.addElement(new TextElement("${column.escapedColumnName} = ${column.escapedColumnName} "
                                + "${" + columnMap + "." + FIELD_OPERATE_FOR_CLASS_INCREMENT + "} "
                                + XmlElementGeneratorTools.getParameterClause(columnMap + "." + FIELD_VALUE_FOR_CLASS_INCREMENT, incColumn))
                        );

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
     * @param introspectedTable
     */
    private void generateIncrement(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (this.support()) {
            // 向topLevelClass 添加必要的操作函数
            this.addIncMethodToTopLevelClass(topLevelClass, introspectedTable);

            // 构建IncrementEnum
            topLevelClass.addInnerEnum(this.generateIncrementEnum(topLevelClass, introspectedTable));
        }
    }

    /**
     * 构建Increment Enum
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    private InnerEnum generateIncrementEnum(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        InnerEnum innerEnum = new InnerEnum(new FullyQualifiedJavaType(ENUM_INCREMENT));
        innerEnum.setVisibility(JavaVisibility.PUBLIC);
        innerEnum.setStatic(true);
        commentGenerator.addEnumComment(innerEnum, introspectedTable);

        // 生成属性和构造函数
        Field columnField = new Field("column", new FullyQualifiedJavaType(ModelColumnPlugin.ENUM_NAME));
        columnField.setVisibility(JavaVisibility.PRIVATE);
        columnField.setFinal(true);
        innerEnum.addField(columnField);

        Method constructor = new Method(ENUM_INCREMENT);
        constructor.setConstructor(true);
        constructor.addBodyLine("this.column = column;");
        constructor.addParameter(new Parameter(new FullyQualifiedJavaType(ModelColumnPlugin.ENUM_NAME), "column"));
        FormatTools.addMethodWithBestPosition(innerEnum, constructor);

        // 属性的Getter方法
        innerEnum.addMethod(JavaElementGeneratorTools.generateGetterMethod(columnField));

        // 添加增减方法
        Method mInc = JavaElementGeneratorTools.generateMethod(
                METHOD_INC,
                JavaVisibility.PUBLIC,
                new FullyQualifiedJavaType(ENUM_INCREMENT + "." + CLASS_INCREMENT_ITEM),
                new Parameter(FullyQualifiedJavaType.getObjectInstance(), "value")
        );
        mInc.addBodyLine("return new " + ENUM_INCREMENT + "." + CLASS_INCREMENT_ITEM + "(this.column, \"+\", value);");
        innerEnum.addMethod(mInc);

        Method mDec = JavaElementGeneratorTools.generateMethod(
                METHOD_DEC,
                JavaVisibility.PUBLIC,
                new FullyQualifiedJavaType(ENUM_INCREMENT + "." + CLASS_INCREMENT_ITEM),
                new Parameter(FullyQualifiedJavaType.getObjectInstance(), "value")
        );
        mDec.addBodyLine("return new " + ENUM_INCREMENT + "." + CLASS_INCREMENT_ITEM + "(this.column, \"-\", value);");
        innerEnum.addMethod(mDec);

        // Enum枚举
        for (IntrospectedColumn introspectedColumn : this.incColumns) {
            Field field = JavaBeansUtil.getJavaBeansField(introspectedColumn, context, introspectedTable);

            StringBuffer sb = new StringBuffer();
            sb.append(field.getName());
            sb.append("(");
            sb.append(ModelColumnPlugin.ENUM_NAME);
            sb.append(".");
            sb.append(field.getName());
            sb.append(")");

            innerEnum.addEnumConstant(sb.toString());
        }


        // 添加内部类IncrementItem
        innerEnum.addInnerClass(this.generateIncrementItemClass(introspectedTable));

        return innerEnum;
    }

    /**
     * 向topLevelClass 添加必要的操作函数
     * @param topLevelClass
     * @param introspectedTable
     */
    private void addIncMethodToTopLevelClass(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 增加field
        Field fIncrements = JavaElementGeneratorTools.generateField(
                IncrementPlugin.FIELD_INC_MAP,
                JavaVisibility.PROTECTED,
                new FullyQualifiedJavaType("Map<String, Object>"),
                "new HashMap<>()"
        );
        fIncrements.setFinal(true);
        commentGenerator.addFieldComment(fIncrements, introspectedTable);
        topLevelClass.addField(fIncrements);
        topLevelClass.addImportedType("java.util.Map");
        topLevelClass.addImportedType("java.util.HashMap");
        // !!! Increment Class需要
        topLevelClass.addImportedType("java.util.Arrays");

        // 增加自增方法
        Method mIncrement = JavaElementGeneratorTools.generateMethod(
                METHOD_INCREMENT,
                JavaVisibility.PUBLIC,
                topLevelClass.getType(),
                new Parameter(new FullyQualifiedJavaType(ENUM_INCREMENT + "." + CLASS_INCREMENT_ITEM), "increment")
        );
        commentGenerator.addGeneralMethodComment(mIncrement, introspectedTable);

        mIncrement.addBodyLine("this." + FIELD_INC_MAP + ".put(increment.getColumn().value(), increment);");
        mIncrement.addBodyLine("return this;");
        FormatTools.addMethodWithBestPosition(topLevelClass, mIncrement);
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
                        if (this.supportIncrement(introspectedColumn)) {
                            XmlElementTools.replaceXmlElement(xmlElement, PluginTools.getHook(IIncrementPluginHook.class).generateIncrementSetSelective(introspectedColumn, hasPrefix ? "record." : null));
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
        for (int i = 0; i < xmlElement.getElements().size(); i++) {
            Element ele = xmlElement.getElements().get(i);
            // 找到text节点且格式为 set xx = xx 或者 xx = xx
            if (ele instanceof TextElement) {
                String text = ((TextElement) ele).getContent().trim();
                if (text.matches("(^set\\s)?\\S+\\s?=.*")) {
                    // 清理 set 操作
                    text = text.replaceFirst("^set\\s", "").trim();
                    String columnName = text.split("=")[0].trim();
                    IntrospectedColumn introspectedColumn = IntrospectedTableTools.safeGetColumn(introspectedTable, columnName);
                    // 查找判断是否需要进行节点替换
                    if (this.supportIncrement(introspectedColumn)) {
                        xmlElement.getElements().set(i, PluginTools.getHook(IIncrementPluginHook.class).generateIncrementSet(introspectedColumn, hasPrefix ? "record." : null, text.endsWith(",")));
                    }
                }
            }
        }
    }

    /**
     * 是否启用了
     * @return
     */
    private boolean support() {
        return this.incColumns.size() > 0;
    }

    /**
     * 生成Increment类
     * @param introspectedTable
     * @return
     */
    private InnerClass generateIncrementItemClass(IntrospectedTable introspectedTable) {
        InnerClass incCls = new InnerClass(CLASS_INCREMENT_ITEM);
        this.commentGenerator.addClassComment(incCls, introspectedTable);

        // 添加Filed
        Field columnField = new Field(FIELD_COLUMN_FOR_CLASS_INCREMENT, new FullyQualifiedJavaType(ModelColumnPlugin.ENUM_NAME));
        columnField.setVisibility(JavaVisibility.PRIVATE);
        Field operateField = new Field(FIELD_OPERATE_FOR_CLASS_INCREMENT, FullyQualifiedJavaType.getStringInstance());
        operateField.setVisibility(JavaVisibility.PRIVATE);
        Field valueField = new Field(FIELD_VALUE_FOR_CLASS_INCREMENT, FullyQualifiedJavaType.getObjectInstance());
        valueField.setVisibility(JavaVisibility.PRIVATE);

        incCls.addField(columnField);
        incCls.addField(operateField);
        incCls.addField(valueField);

        // 添加Getter
        incCls.addMethod(JavaElementGeneratorTools.generateGetterMethod(columnField));
        incCls.addMethod(JavaElementGeneratorTools.generateGetterMethod(operateField));
        incCls.addMethod(JavaElementGeneratorTools.generateGetterMethod(valueField));

        // 添加构造函数
        Method mConstructor = JavaElementGeneratorTools.generateMethod(
                CLASS_INCREMENT_ITEM,
                JavaVisibility.PUBLIC,
                incCls.getType(),
                new Parameter(columnField.getType(), FIELD_COLUMN_FOR_CLASS_INCREMENT),
                new Parameter(operateField.getType(), FIELD_OPERATE_FOR_CLASS_INCREMENT),
                new Parameter(valueField.getType(), FIELD_VALUE_FOR_CLASS_INCREMENT)
        );
        mConstructor.setConstructor(true);
        mConstructor.addBodyLine("this." + FIELD_COLUMN_FOR_CLASS_INCREMENT + " = " + FIELD_COLUMN_FOR_CLASS_INCREMENT + ";");
        mConstructor.addBodyLine("this." + FIELD_OPERATE_FOR_CLASS_INCREMENT + " = " + FIELD_OPERATE_FOR_CLASS_INCREMENT + ";");
        mConstructor.addBodyLine("this." + FIELD_VALUE_FOR_CLASS_INCREMENT + " = " + FIELD_VALUE_FOR_CLASS_INCREMENT + ";");
        incCls.addMethod(mConstructor);

        return incCls;
    }
}
