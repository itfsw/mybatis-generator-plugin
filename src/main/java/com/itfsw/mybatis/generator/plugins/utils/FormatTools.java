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

package com.itfsw.mybatis.generator.plugins.utils;

import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;

/**
 * ---------------------------------------------------------------------------
 * 格式化工具，优化输出
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/6/30 10:53
 * ---------------------------------------------------------------------------
 */
public class FormatTools {
    /**
     * 在最佳位置添加方法
     *
     * @param innerClass
     * @param method
     */
    public static void addMethodWithBestPosition(InnerClass innerClass, Method method){
        addMethodWithBestPosition(method, innerClass.getMethods());
    }

    /**
     * 在最佳位置添加方法
     *
     * @param interfacz
     * @param method
     */
    public static void addMethodWithBestPosition(Interface interfacz, Method method){
        addMethodWithBestPosition(method, interfacz.getMethods());
    }

    /**
     * 在最佳位置添加方法
     *
     * @param topLevelClass
     * @param method
     */
    public static void addMethodWithBestPosition(TopLevelClass topLevelClass, Method method){
        addMethodWithBestPosition(method, topLevelClass.getMethods());
    }

    /**
     * 在最佳位置添加节点
     *
     * @param rootElement
     * @param element
     */
    public static void addElementWithBestPosition(XmlElement rootElement, XmlElement element){
        String id = getIdFromElement(element);
        if (id == null){
            rootElement.addElement(element);
        } else {
            List<Element> elements = rootElement.getElements();
            int index = -1;
            for (int i = 0; i < elements.size(); i++){
                Element ele = elements.get(i);
                if (ele instanceof XmlElement){
                    String eleId = getIdFromElement((XmlElement) ele);
                    if (eleId != null){
                        if (eleId.startsWith(id)){
                            if (index == - 1){
                                index = i;
                            }
                        } else if (id.startsWith(eleId)){
                            index = i + 1;
                        }
                    }
                }
            }

            if (index == -1 || index >= elements.size()){
                rootElement.addElement(element);
            } else {
                elements.add(index, element);
            }
        }
    }

    /**
     * 找出节点ID值
     *
     * @param element
     * @return
     */
    private static String getIdFromElement(XmlElement element){
        for (Attribute attribute : element.getAttributes()){
            if (attribute.getName().equals("id")){
                return attribute.getValue();
            }
        }
        return null;
    }

    /**
     * 获取最佳添加位置
     *
     * @param method
     * @param methods
     * @return
     */
    private static void addMethodWithBestPosition(Method method, List<Method> methods){
        int index = -1;
        for (int i = 0; i < methods.size(); i++){
            Method m = methods.get(i);
            if (m.getName().equals(method.getName())){
                if (m.getParameters().size() <= method.getParameters().size()){
                    index = i + 1;
                } else {
                    index = i;
                }
            } else if (m.getName().startsWith(method.getName())){
                if (index == - 1){
                    index = i;
                }
            } else if (method.getName().startsWith(m.getName())){
                index = i + 1;
            }
        }
        if (index == -1 || index >= methods.size()){
            methods.add(methods.size(), method);
        } else {
            methods.add(index, method);
        }
    }
}
