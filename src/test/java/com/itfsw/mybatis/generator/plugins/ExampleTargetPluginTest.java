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
import org.apache.ibatis.io.Resources;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
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
    private DBHelper helper;

    /**
     * 初始化
     * @throws IOException
     * @throws SQLException
     */
    @Before
    public void init() throws IOException, SQLException {
        helper = DBHelper.getHelper("scripts/ExampleTargetPlugin/init.sql");
    }

    @Test
    public void testNormalPath() throws IOException, XMLParserException, InvalidConfigurationException, SQLException, InterruptedException {
        List<String> warnings = new ArrayList<String>();
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(Resources.getResourceAsStream("scripts/ExampleTargetPlugin/mybatis-generator-without-plugin.xml"));

        DefaultShellCallback shellCallback = new DefaultShellCallback(true);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, shellCallback, warnings);
        myBatisGenerator.generate(null, null, null, false);
        List<GeneratedJavaFile> list = myBatisGenerator.getGeneratedJavaFiles();
        for (GeneratedJavaFile file : list){
            if (file.getFileName().equals("TbExample.java")){
                Assert.assertEquals(file.getTargetPackage(), "com.itfsw.mybatis.generator.plugins.dao.model");
            }
        }
    }

    @Test
    public void testConfigPath() throws IOException, XMLParserException, InvalidConfigurationException, SQLException, InterruptedException {
        List<String> warnings = new ArrayList<String>();
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(Resources.getResourceAsStream("scripts/ExampleTargetPlugin/mybatis-generator.xml"));

        DefaultShellCallback shellCallback = new DefaultShellCallback(true);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, shellCallback, warnings);
        myBatisGenerator.generate(null, null, null, false);
        List<GeneratedJavaFile> list = myBatisGenerator.getGeneratedJavaFiles();
        for (GeneratedJavaFile file : list){
            if (file.getFileName().equals("TbExample.java")){
                Assert.assertEquals(file.getTargetPackage(), "com.itfsw.mybatis.generator.plugins.dao.example");
            }
        }
    }

    @After
    public void clean(){
        DBHelper.reset();
    }
}
