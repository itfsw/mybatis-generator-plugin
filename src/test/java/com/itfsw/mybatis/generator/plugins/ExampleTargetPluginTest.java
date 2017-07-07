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
import java.util.List;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/6/26 17:46
 * ---------------------------------------------------------------------------
 */
public class ExampleTargetPluginTest {

    /**
     * 初始化
     * @throws IOException
     * @throws SQLException
     */
    @BeforeClass
    public static void init() throws Exception {
        DBHelper.createDB("scripts/ExampleTargetPlugin/init.sql");
    }

    /**
     * 测试异常配置
     */
    @Test
    public void testWarnings() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/ExampleTargetPlugin/mybatis-generator-without-target.xml");
        tool.generate();
        Assert.assertEquals(tool.getWarnings().get(0), "请配置com.itfsw.mybatis.generator.plugins.ExampleTargetPlugin插件的目标包名(targetPackage)！");
    }

    @Test
    public void testNormalPath() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/ExampleTargetPlugin/mybatis-generator-without-plugin.xml");
        MyBatisGenerator myBatisGenerator = tool.generate();

        List<GeneratedJavaFile> list = myBatisGenerator.getGeneratedJavaFiles();
        for (GeneratedJavaFile file : list){
            if (file.getFileName().equals("TbExample.java")){
                Assert.assertEquals(file.getTargetPackage(), tool.getTargetPackage());
            }
        }
    }

    @Test
    public void testConfigPath() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/ExampleTargetPlugin/mybatis-generator.xml");
        MyBatisGenerator myBatisGenerator = tool.generate();

        List<GeneratedJavaFile> list = myBatisGenerator.getGeneratedJavaFiles();
        for (GeneratedJavaFile file : list){
            if (file.getFileName().equals("TbExample.java")){
                Assert.assertEquals(file.getTargetPackage(), "com.itfsw.mybatis.generator.plugins.dao.example");
            }
        }
    }

}
