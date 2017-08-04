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
import com.itfsw.mybatis.generator.plugins.utils.IntrospectedTableTools;
import com.itfsw.mybatis.generator.plugins.utils.PluginTools;
import com.itfsw.mybatis.generator.plugins.utils.XmlElementGeneratorTools;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.*;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ---------------------------------------------------------------------------
 * Selective 增强插件
 * ---------------------------------------------------------------------------
 *
 * @author: hewei
 * @time:2017/4/20 15:39
 * ---------------------------------------------------------------------------
 */
public class SelectiveEnhancedPlugin extends BasePlugin {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(List<String> warnings) {

        // 插件使用前提是使用了ModelColumnPlugin插件
        if (!PluginTools.checkDependencyPlugin(getContext(), ModelColumnPlugin.class)) {
            warnings.add("itfsw:插件" + this.getClass().getTypeName() + "插件需配合com.itfsw.mybatis.generator.plugins.ModelColumnPlugin插件使用！");
            return false;
        }

        // 插件位置
        PluginTools.shouldAfterPlugins(getContext(), this.getClass(), warnings, UpsertPlugin.class);

        return super.validate(warnings);
    }

    /**
     * Model Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // import
        topLevelClass.addImportedType(FullyQualifiedJavaType.getNewMapInstance());
        topLevelClass.addImportedType(FullyQualifiedJavaType.getNewHashMapInstance());

        // field
        Field selectiveColumnsField = new Field("selectiveColumns", new FullyQualifiedJavaType("Map<String, Boolean>"));
        commentGenerator.addFieldComment(selectiveColumnsField, introspectedTable);
        selectiveColumnsField.setVisibility(JavaVisibility.PRIVATE);
        selectiveColumnsField.setInitializationString("new HashMap<String, Boolean>()");
        topLevelClass.addField(selectiveColumnsField);

        // Method isSelective
        Method mIsSelective = new Method("isSelective");
        commentGenerator.addGeneralMethodComment(mIsSelective, introspectedTable);
        mIsSelective.setVisibility(JavaVisibility.PUBLIC);
        mIsSelective.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        mIsSelective.addBodyLine("return this.selectiveColumns.size() > 0;");
        topLevelClass.addMethod(mIsSelective);

        // Method isSelective
        Method mIsSelective1 = new Method("isSelective");
        commentGenerator.addGeneralMethodComment(mIsSelective1, introspectedTable);
        mIsSelective1.setVisibility(JavaVisibility.PUBLIC);
        mIsSelective1.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        mIsSelective1.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "column"));
        mIsSelective1.addBodyLine("return this.selectiveColumns.get(column) != null;");
        topLevelClass.addMethod(mIsSelective1);

        // Method selective
        Method mSelective = new Method("selective");
        commentGenerator.addGeneralMethodComment(mSelective, introspectedTable);
        mSelective.setVisibility(JavaVisibility.PUBLIC);
        mSelective.setReturnType(topLevelClass.getType());
        mSelective.addParameter(new Parameter(new FullyQualifiedJavaType(ModelColumnPlugin.ENUM_NAME), "columns", true));
        mSelective.addBodyLine("this.selectiveColumns.clear();");
        mSelective.addBodyLine("if (columns != null) {");
        mSelective.addBodyLine("for (" + ModelColumnPlugin.ENUM_NAME + " column : columns) {");
        mSelective.addBodyLine("this.selectiveColumns.put(column.value(), true);");
        mSelective.addBodyLine("}");
        mSelective.addBodyLine("}");
        mSelective.addBodyLine("return this;");
        topLevelClass.addMethod(mSelective);

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
        List<Element> rootElements = document.getRootElement().getElements();
        for (Element rootElement : rootElements) {
            if (rootElement instanceof XmlElement) {
                XmlElement xmlElement = (XmlElement) rootElement;
                List<Attribute> attributes = xmlElement.getAttributes();
                // 查找ID
                String id = "";
                for (Attribute attribute : attributes) {
                    if (attribute.getName().equals("id")) {
                        id = attribute.getValue();
                    }
                }

                // ====================================== 1. insertSelective ======================================
                if ("insertSelective".equals(id)) {
                    List<XmlElement> eles = XmlElementGeneratorTools.findXmlElements(xmlElement, "trim");
                    for (XmlElement ele : eles) {
                        this.replaceEle(ele, "_parameter.", introspectedTable);
                    }
                }
                // ====================================== 2. updateByExampleSelective ======================================
                if ("updateByExampleSelective".equals(id)) {
                    List<XmlElement> eles = XmlElementGeneratorTools.findXmlElements(xmlElement, "set");
                    for (XmlElement ele : eles) {
                        this.replaceEle(ele, "record.", introspectedTable);
                    }
                }
                // ====================================== 3. updateByPrimaryKeySelective ======================================
                if ("updateByPrimaryKeySelective".equals(id)) {
                    List<XmlElement> eles = XmlElementGeneratorTools.findXmlElements(xmlElement, "set");
                    for (XmlElement ele : eles) {
                        this.replaceEle(ele, "_parameter.", introspectedTable);
                    }
                }
                // ====================================== 4. upsertSelective ======================================
                if ("upsertSelective".equals(id)) {
                    List<XmlElement> eles = XmlElementGeneratorTools.findXmlElements(xmlElement, "trim");
                    for (XmlElement ele : eles) {
                        this.replaceEle(ele, "_parameter.", introspectedTable);
                    }
                }
                // ====================================== 5. upsertByExampleSelective ======================================
                if ("upsertByExampleSelective".equals(id)) {
                    List<XmlElement> eles = XmlElementGeneratorTools.findXmlElements(xmlElement, "trim");
                    this.replaceEle(eles.get(0), "record.", introspectedTable);
                    // upsertByExampleSelective的第二个trim比较特殊，需另行处理
                    this.replaceEleForUpsertByExampleSelective(eles.get(1), "record.", introspectedTable, !introspectedTable.getRules().generateRecordWithBLOBsClass());

                    List<XmlElement> eles1 = XmlElementGeneratorTools.findXmlElements(xmlElement, "set");
                    for (XmlElement ele : eles1) {
                        this.replaceEle(ele, "record.", introspectedTable);
                    }
                }
            }
        }
        return true;
    }

    /**
     * 替换节点if信息
     *
     * @param element
     * @param prefix
     * @param introspectedTable
     */
    private void replaceEle(XmlElement element, String prefix, IntrospectedTable introspectedTable) {
        // choose
        XmlElement chooseEle = new XmlElement("choose");
        // when
        XmlElement whenEle = new XmlElement("when");
        whenEle.addAttribute(new Attribute("test", prefix + "isSelective()"));
        for (Element ele : element.getElements()) {
            // 对于字符串主键，是没有if判断节点的
            if (ele instanceof XmlElement) {
                // if的text节点
                XmlElement xmlElement = (XmlElement) ele;

                // 找出field 名称
                String text = ((TextElement) xmlElement.getElements().get(0)).getContent();

                String columnName = "";
                if (text.matches("#\\{.*\\},?")) {
                    Pattern pattern = Pattern.compile("#\\{(.*?),.*\\},?");
                    Matcher matcher = pattern.matcher(text);
                    if (matcher.find()) {
                        String field = matcher.group(1);
                        // 查找对应column
                        for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
                            if (column.getJavaProperty().equals(field)) {
                                columnName = column.getActualColumnName();
                            }
                        }
                    }
                } else {
                    if (text.matches(".*=.*")) {
                        columnName = text.split("=")[0];
                    } else {
                        columnName = text.replaceAll(",", "");
                    }
                    // bug fixed: 修正使用autoDelimitKeywords过滤关键词造成的field前后加了特殊字符的问题
                    // columnName = columnName.trim().replaceAll("`", "").replaceAll("\"", "").replaceAll("'", "");
                }

                IntrospectedColumn column = IntrospectedTableTools.safeGetColumn(introspectedTable, columnName);

                XmlElement ifEle = new XmlElement("if");

                ifEle.addAttribute(new Attribute("test", prefix + "isSelective(\'" + column.getActualColumnName() + "\')"));
                for (Element ifChild : xmlElement.getElements()) {
                    ifEle.addElement(ifChild);
                }
                whenEle.addElement(ifEle);
            } else {
                whenEle.addElement(ele);
            }
        }

        // otherwise
        XmlElement otherwiseEle = new XmlElement("otherwise");
        for (Element ele : element.getElements()) {
            otherwiseEle.addElement(ele);
        }

        chooseEle.addElement(whenEle);
        chooseEle.addElement(otherwiseEle);

        // 清空原始节点，新增choose节点
        element.getElements().clear();
        element.addElement(chooseEle);
    }

    /**
     * 替换节点upsertByExampleSelective if信息
     *
     * @param element
     * @param prefix
     * @param introspectedTable
     * @param allColumns
     */
    private void replaceEleForUpsertByExampleSelective(XmlElement element, String prefix, IntrospectedTable introspectedTable, boolean allColumns) {
        // choose
        XmlElement chooseEle = new XmlElement("choose");
        // when
        XmlElement whenEle = new XmlElement("when");
        whenEle.addAttribute(new Attribute("test", prefix + "isSelective()"));
        for (IntrospectedColumn introspectedColumn : (allColumns ? introspectedTable.getAllColumns() : introspectedTable.getNonBLOBColumns())) {
            XmlElement eleIf = new XmlElement("if");
            eleIf.addAttribute(new Attribute("test", prefix + "isSelective(\'" + introspectedColumn.getActualColumnName() + "\')"));

            eleIf.addElement(new TextElement(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, prefix) + ","));
            whenEle.addElement(eleIf);
        }

        // otherwise
        XmlElement otherwiseEle = new XmlElement("otherwise");
        for (Element ele : element.getElements()) {
            otherwiseEle.addElement(ele);
        }

        chooseEle.addElement(whenEle);
        chooseEle.addElement(otherwiseEle);

        // 清空原始节点，新增choose节点
        element.getElements().clear();
        element.addElement(chooseEle);
    }
}
