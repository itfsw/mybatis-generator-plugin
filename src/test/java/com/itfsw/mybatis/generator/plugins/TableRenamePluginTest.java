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
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;

import java.io.IOException;
import java.sql.SQLException;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/7/7 9:36
 * ---------------------------------------------------------------------------
 */
public class TableRenamePluginTest {

    /**
     * 初始化
     */
    @BeforeClass
    public static void init() throws Exception{
        DBHelper.createDB("scripts/TableRenamePlugin/init.sql");
    }

    /**
     * 测试提示信息
     */
    @Test
    public void testWarnings() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        // searchString、replaceString 单独配置
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/TableRenamePlugin/mybatis-generator-with-wrong-properties.xml");
        tool.generate();
        Assert.assertEquals(tool.getWarnings().get(0), "itfsw:插件com.itfsw.mybatis.generator.plugins.TableRenamePlugin插件的searchString、replaceString属性需配合使用，不能单独存在！");

        // 和官方domainObjectName或者mapperName一起配置
        tool = MyBatisGeneratorTool.create("scripts/TableRenamePlugin/mybatis-generator-with-domainObject.xml");
        tool.generate();
        Assert.assertEquals(tool.getWarnings().get(0), "itfsw:插件com.itfsw.mybatis.generator.plugins.TableRenamePlugin插件请不要配合table的domainObjectName或者mapperName一起使用！");
    }

    /**
     * 测试具体生成
     */
    @Test
    public void testGenerate() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        // 规则1 ^T 替换成 Test， 同时tb2 使用了tableOverride 替换成 TestOverride
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/TableRenamePlugin/mybatis-generator-rule1.xml");
        MyBatisGenerator myBatisGenerator = tool.generate();
        for (GeneratedJavaFile file : myBatisGenerator.getGeneratedJavaFiles()){
            String name = file.getCompilationUnit().getType().getShortName();
            if (name.matches(".*1.*")){
                Assert.assertTrue(name.matches("Testb1.*"));
            } else {
                Assert.assertTrue(name.matches("TestOverride.*"));
            }
        }
    }

    /**
     * 测试替代 TablePrefixPlugin 插件
     */
    @Test
    public void testGenerateLikeTablePrefixPlugin() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/TableRenamePlugin/mybatis-generator-like-TablePrefixPlugin.xml");
        MyBatisGenerator myBatisGenerator = tool.generate();
        for (GeneratedJavaFile file : myBatisGenerator.getGeneratedJavaFiles()){
            String name = file.getCompilationUnit().getType().getShortName();
            Assert.assertTrue(name.matches("DB1.*"));
        }
    }
}
