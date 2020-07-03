package com.itfsw.mybatis.generator.plugins.builder;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: XmlElementBuilder
 * @Date : 2020/7/2 上午10:56
 * @Author : 石冬冬-Seig Heil
 */
@Slf4j
public class XmlElementBuilderTest {

    /**
     * for console
     *
     * <code>
     *   <sql id="queryCondition">
             <where>
                 <if test="year != null">
                    and year = #{year}
                 </if>
                 <if test="month != null">
                    and month = #{month}
                 </if>
             </where>
         </sql>
        </code>
     */
    @Test
    public void test(){

        List<Element> ifElements = new ArrayList<>();

        ifElements.add(new XmlElementBuilder().name("if").attribute("test","year != null").text(" and year = #{year}").build());
        ifElements.add(new XmlElementBuilder().name("if").attribute("test","month != null").text(" and month = #{month}").build());

        XmlElement whereElement = new XmlElementBuilder().name("where").elements(ifElements).build();

        XmlElement xmlElement = new XmlElementBuilder().name("sql")
                .attribute("id","queryCondition")
                .element(whereElement)
                .build();
        String sql = xmlElement.getFormattedContent(1);
        log.info("\n{}",sql);
    }
}
