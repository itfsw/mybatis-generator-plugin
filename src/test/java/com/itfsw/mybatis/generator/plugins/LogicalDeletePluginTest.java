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
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;

import java.io.IOException;
import java.sql.SQLException;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/7/7 16:59
 * ---------------------------------------------------------------------------
 */
public class LogicalDeletePluginTest {
    /**
     * 初始化数据库
     */
    @BeforeClass
    public static void init() throws SQLException, IOException, ClassNotFoundException {
        DBHelper.createDB("scripts/LogicalDeletePlugin/init.sql");
    }

    /**
     * 测试配置异常
     */
    @Test
    public void testWarnings() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        // 1. 不支持的类型
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/LogicalDeletePlugin/mybatis-generator-with-unsupport-type.xml");
        tool.generate();

        Assert.assertEquals(tool.getWarnings().get(0), "itfsw(逻辑删除插件):tb逻辑删除列(ts_2)的类型不在支持范围（请使用数字列，字符串列，布尔列）！");

        // 2. 没有找到配置的逻辑删除列
        tool = MyBatisGeneratorTool.create("scripts/LogicalDeletePlugin/mybatis-generator-with-unfind-column.xml");
        tool.generate();

        Assert.assertEquals(tool.getWarnings().get(0), "itfsw(逻辑删除插件):tb没有找到您配置的逻辑删除列(ts_999)！");

        // 3. 没有配置逻辑删除值
        tool = MyBatisGeneratorTool.create("scripts/LogicalDeletePlugin/mybatis-generator-with-unconfig-logicalDeleteValue.xml");
        tool.generate();

        Assert.assertEquals(tool.getWarnings().get(0), "itfsw(逻辑删除插件):tb没有找到您配置的逻辑删除值，请全局或者局部配置logicalDeleteValue和logicalUnDeleteValue值！");
    }
}
