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
 * @time:2017/7/7 14:45
 * ---------------------------------------------------------------------------
 */
public class TablePrefixPluginTest {
    /**
     * 初始化
     */
    @BeforeClass
    public static void init() throws Exception{
        DBHelper.createDB("scripts/TablePrefixPlugin/init.sql");
    }

    /**
     * 测试提示信息
     */
    @Test
    public void testWarnings() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        // 和官方domainObjectName或者mapperName一起配置
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/TablePrefixPlugin/mybatis-generator-with-domainObject.xml");
        tool.generate();
        Assert.assertEquals(tool.getWarnings().get(0), "itfsw:插件com.itfsw.mybatis.generator.plugins.TablePrefixPlugin插件请不要配合table的domainObjectName或者mapperName一起使用！");
    }

    /**
     * 测试具体生成
     */
    @Test
    public void testGenerate() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        // 全局规则增加 DB, tb2 单独规则增加Tt
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/TablePrefixPlugin/mybatis-generator.xml");
        MyBatisGenerator myBatisGenerator = tool.generate();
        for (GeneratedJavaFile file : myBatisGenerator.getGeneratedJavaFiles()){
            String name = file.getCompilationUnit().getType().getShortName();
            if (name.matches(".*1.*")){
                Assert.assertTrue(name.matches("DB.*"));
            } else {
                Assert.assertTrue(name.matches("Tt.*"));
            }
        }
    }
}
