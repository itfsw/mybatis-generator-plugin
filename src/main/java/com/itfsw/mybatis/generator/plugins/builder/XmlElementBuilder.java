package com.itfsw.mybatis.generator.plugins.builder;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: XmlElement 的建造者模式
 * <code>
 *     XmlElement xmlElement = new XmlElementBuilder().name("sql")
 *     .attribute("id","queryCondition")
 *     .element(whereElement)
 *     .build();
 * </code>
 * @see XmlElement
 * @Date : 2020/7/2 上午10:43
 * @Author : 石冬冬-Seig Heil
 */
public class XmlElementBuilder {

    /** The attributes. */
    private List<Attribute> attributes = new ArrayList<>();

    /** The elements. */
    private List<Element> elements = new ArrayList<>();

    /** The name. */
    private String name;


    public XmlElementBuilder name(String name){
        this.name = name;
        return this;
    }

    public XmlElementBuilder attribute(String name, String value){
        attributes.add(new Attribute(name,value));
        return this;
    }

    public XmlElementBuilder text(String text){
        elements.add(new TextElement(text));
        return this;
    }

    public XmlElementBuilder element(Element element){
        elements.add(element);
        return this;
    }

    public XmlElementBuilder elements(List<Element> list){
        elements.addAll(list);
        return this;
    }

    /**
     * 提供 XmlElement 实例对象属性的封装
     * @return
     */
    public XmlElement build(){
        XmlElement xmlElement = new XmlElement(this.name);
        attributes.forEach(each -> xmlElement.addAttribute(each));
        elements.forEach(each -> xmlElement.addElement(each));
        return xmlElement;
    }
}
