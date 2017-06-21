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
import com.itfsw.mybatis.generator.plugins.utils.IncrementsPluginTools;
import com.itfsw.mybatis.generator.plugins.utils.PluginTools;
import com.itfsw.mybatis.generator.plugins.utils.XmlElementGeneratorTools;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

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
public class IncrementsPlugin extends BasePlugin {
    public static final String PRE_INCREMENTS_COLUMNS = "incrementsColumns";  // incrementsColumns property
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
            logger.error("itfsw:插件" + this.getClass().getTypeName() + "插件需配合com.itfsw.mybatis.generator.plugins.ModelBuilderPlugin插件使用！");
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
        this.incTools = IncrementsPluginTools.getTools(context, introspectedTable);
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 具体实现在 ModelBuilderPlugin.generateModelBuilder
        return true;
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 具体实现在 ModelBuilderPlugin.generateModelBuilder
        return true;
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
        return true;
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
        return true;
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
        return true;
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
        return true;
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
        return true;
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
        return true;
    }


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
                        IntrospectedColumn introspectedColumn = introspectedTable.getColumn(columnName);
                        // 查找是否需要进行增量操作
                        if (incTools.supportColumn(introspectedColumn)) {
                            xmlElement.getElements().clear();
                            xmlElement.getElements().addAll(incTools.generatedIncrementsElement(introspectedColumn, hasPrefix, true));
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
                    if (text.matches("(set\\s)?\\S+\\s?=.*")) {
                        // 清理 set 操作
                        text = text.replaceFirst("set\\s", "").trim();
                        String columnName = text.split("=")[0].trim();
                        IntrospectedColumn introspectedColumn = introspectedTable.getColumn(columnName);
                        // 查找判断是否需要进行节点替换
                        if (incTools.supportColumn(introspectedColumn)) {
                            newEles.addAll(incTools.generatedIncrementsElement(introspectedColumn, hasPrefix, text.endsWith(",")));

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
