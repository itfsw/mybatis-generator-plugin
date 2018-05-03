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

package com.itfsw.mybatis.generator.plugins.utils;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2018/5/2 17:52
 * ---------------------------------------------------------------------------
 */
public class XmlElementTools {
    /**
     * 获取属性
     * @param element
     * @param name
     */
    public static Attribute getAttribute(XmlElement element, String name) {
        Iterator<Attribute> iterator = element.getAttributes().iterator();
        while (iterator.hasNext()) {
            Attribute attribute = iterator.next();
            if (attribute.getName().equals(name)) {
                return attribute;
            }
        }
        return null;
    }

    /**
     * 移除属性
     * @param element
     * @param name
     */
    public static void removeAttribute(XmlElement element, String name) {
        Iterator<Attribute> iterator = element.getAttributes().iterator();
        while (iterator.hasNext()) {
            Attribute attribute = iterator.next();
            if (attribute.getName().equals(name)) {
                iterator.remove();
            }
        }
    }

    /**
     * 替换属性
     * @param element
     * @param attribute
     */
    public static void replaceAttribute(XmlElement element, Attribute attribute) {
        removeAttribute(element, attribute.getName());
        element.addAttribute(attribute);
    }

    /**
     * xmlElement 替换
     * @param srcEle
     * @param destEle
     */
    public static void replaceXmlElement(XmlElement srcEle, XmlElement destEle) {
        srcEle.setName(destEle.getName());
        srcEle.getAttributes().clear();
        srcEle.getAttributes().addAll(destEle.getAttributes());
        srcEle.getElements().clear();
        srcEle.getElements().addAll(destEle.getElements());
    }

    /**
     * 查找指定xml节点下指定节点名称的元素
     * @param xmlElement
     * @param name
     * @return
     */
    public static List<XmlElement> findXmlElements(XmlElement xmlElement, String name) {
        List<XmlElement> list = new ArrayList<>();
        List<Element> elements = xmlElement.getElements();
        for (Element ele : elements) {
            if (ele instanceof XmlElement) {
                XmlElement xmlElement1 = (XmlElement) ele;
                if (name.equalsIgnoreCase(xmlElement1.getName())) {
                    list.add(xmlElement1);
                }
            }
        }
        return list;
    }

    /**
     * 查询指定xml下所有text xml 节点
     * @param xmlElement
     * @return
     */
    public static List<TextElement> findAllTextElements(XmlElement xmlElement){
        List<TextElement> textElements = new ArrayList<>();
        for (Element element : xmlElement.getElements()){
            if (element instanceof XmlElement){
                textElements.addAll(findAllTextElements((XmlElement) element));
            } else if (element instanceof TextElement){
                textElements.add((TextElement) element);
            }
        }
        return textElements;
    }

    /**
     * 拷贝
     * @param element
     * @return
     */
    public static XmlElement clone(XmlElement element) {
        XmlElement destEle = new XmlElement(element.getName());
        for (Attribute attribute : element.getAttributes()) {
            destEle.addAttribute(XmlElementTools.clone(attribute));
        }
        for (Element ele : element.getElements()) {
            if (ele instanceof XmlElement) {
                destEle.addElement(XmlElementTools.clone((XmlElement) ele));
            } else if (ele instanceof TextElement) {
                destEle.addElement(XmlElementTools.clone((TextElement) ele));
            }
        }
        return destEle;
    }

    /**
     * 拷贝
     * @param attribute
     * @return
     */
    public static Attribute clone(Attribute attribute) {
        return new Attribute(attribute.getName(), attribute.getValue());
    }

    /**
     * 拷贝
     * @param textElement
     * @return
     */
    public static TextElement clone(TextElement textElement) {
        return new TextElement(textElement.getContent());
    }
}
