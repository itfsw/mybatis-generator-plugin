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

import com.itfsw.mybatis.generator.plugins.tools.DBHelper;
import com.itfsw.mybatis.generator.plugins.tools.MyBatisGeneratorTool;
import com.itfsw.mybatis.generator.plugins.tools.ObjectUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.MergeConstants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/6/29 13:29
 * ---------------------------------------------------------------------------
 */
public class CommentPluginTest {
    /**
     * 初始化
     */
    @BeforeClass
    public static void init() throws Exception {
        DBHelper.createDB("scripts/CommentPlugin/init.sql");
    }

    /**
     * 测试配置了模板参数转换
     */
    @Test
    public void testGenerateWithTemplate() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/CommentPlugin/mybatis-generator.xml");
        MyBatisGenerator myBatisGenerator = tool.generate();

        // java中的注释
        for (GeneratedJavaFile file : myBatisGenerator.getGeneratedJavaFiles()) {
            if (file.getFileName().equals("Tb.java")) {
                TopLevelClass topLevelClass = (TopLevelClass) file.getCompilationUnit();
                // addJavaFileComment
                Assert.assertEquals(topLevelClass.getFileCommentLines().get(0), "TestAddJavaFileComment:Tb:" + new SimpleDateFormat("yyyy-MM").format(new Date()));
                // addFieldComment 同时测试 if 判断和 mbg
                Field id = topLevelClass.getFields().get(0);
                Assert.assertEquals(id.getJavaDocLines().get(0), "注释1");
                Assert.assertEquals(id.getJavaDocLines().get(1), MergeConstants.NEW_ELEMENT_TAG);
                // addGeneralMethodComment
                Method cons = topLevelClass.getMethods().get(0);
                Assert.assertEquals(cons.getJavaDocLines().get(0), "addGeneralMethodComment:Tb:tb");
                // addSetterComment
                Method setter = topLevelClass.getMethods().get(5);
                Assert.assertEquals(setter.getJavaDocLines().get(0), "addSetterComment:field1:field1");
            }
        }

        // xml注释
        ObjectUtil xml = new ObjectUtil(myBatisGenerator.getGeneratedXmlFiles().get(0));
        Document doc = (Document) xml.get("document");
        List<Element> els = ((XmlElement) (doc.getRootElement().getElements().get(0))).getElements();
        String comment = ((TextElement) els.get(0)).getContent();
        Assert.assertEquals(comment, "addComment:BaseResultMap");
    }

    /**
     * 测试配置了模板参数转换
     */
    @Test
    public void testGenerateWithOutComment() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/CommentPlugin/mybatis-generator-without-comment.xml");
        MyBatisGenerator myBatisGenerator = tool.generate();

        // java中的注释
        for (GeneratedJavaFile file : myBatisGenerator.getGeneratedJavaFiles()) {
            if (file.getFileName().equals("Tb.java")) {
                TopLevelClass topLevelClass = (TopLevelClass) file.getCompilationUnit();
                // addJavaFileComment
                Assert.assertEquals(topLevelClass.getFileCommentLines().size(), 0);
                // addFieldComment
                Field id = topLevelClass.getFields().get(0);
                Assert.assertEquals(id.getJavaDocLines().size(), 0);
                // addGeneralMethodComment
                Method cons = topLevelClass.getMethods().get(0);
                Assert.assertEquals(cons.getJavaDocLines().size(), 0);
                // addSetterComment
                Method setter = topLevelClass.getMethods().get(5);
                Assert.assertEquals(setter.getJavaDocLines().size(), 0);
            }
        }
    }
}
