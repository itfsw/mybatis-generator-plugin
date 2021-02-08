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

package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.tools.AbstractShellCallback;
import com.itfsw.mybatis.generator.plugins.tools.DBHelper;
import com.itfsw.mybatis.generator.plugins.tools.MyBatisGeneratorTool;
import com.itfsw.mybatis.generator.plugins.tools.ObjectUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.SQLException;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2018/11/27 21:03
 * ---------------------------------------------------------------------------
 */
public class EnumTypeStatusPluginTest {
    /**
     * 初始化数据库
     */
    @BeforeClass
    public static void init() throws SQLException, IOException {
        DBHelper.createDB("scripts/EnumTypeStatusPlugin/init.sql");
    }

    /**
     * 测试配置异常
     */
    @Test
    public void testWarnings() throws Exception {
        // 1. 注释格式不对
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/EnumTypeStatusPlugin/mybatis-generator-with-wrong-comment.xml");
        tool.generate();

        Assert.assertEquals(tool.getWarnings().get(0), "itfsw:插件" + EnumTypeStatusPlugin.class.getTypeName() + "没有找到column为field2对应格式的注释的字段！");


        // 2. 不支持的类型
        tool = MyBatisGeneratorTool.create("scripts/EnumTypeStatusPlugin/mybatis-generator-with-unsupport-type.xml");
        tool.generate();

        Assert.assertEquals(tool.getWarnings().get(0), "itfsw:插件" + EnumTypeStatusPlugin.class.getTypeName() + "找到column为field2对应Java类型不在支持范围内！");
    }

    /**
     * 测试生成的enum
     * @throws Exception
     */
    @Test
    public void testEnum() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/EnumTypeStatusPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 测试标准注释
                ObjectUtil enumField2Success = new ObjectUtil(loader, packagz + ".Tb$Field2#SUCCESS");
                Assert.assertEquals(enumField2Success.invoke("value"), (short) 0);
                Assert.assertEquals(enumField2Success.invoke("getValue"), (short) 0);
                Assert.assertEquals(enumField2Success.invoke("getName"), "禁用");

                ObjectUtil enumField2FailType = new ObjectUtil(loader, packagz + ".Tb$Field2#FAIL_TYPE");
                Assert.assertEquals(enumField2FailType.invoke("value"), (short) 1);
                Assert.assertEquals(enumField2FailType.invoke("getValue"), (short) 1);
                Assert.assertEquals(enumField2FailType.invoke("getName"), "启用");

                // 2. 字符串类型的
                ObjectUtil enumField3StrSuccess = new ObjectUtil(loader, packagz + ".Tb$Field3Str#SUCCESS");
                Assert.assertEquals(enumField3StrSuccess.invoke("value"), "成都");
                Assert.assertEquals(enumField3StrSuccess.invoke("getValue"), "成都");
                Assert.assertEquals(enumField3StrSuccess.invoke("getName"), "禁用");

                // 3. 全局支持
                ObjectUtil enumStatusSuccess = new ObjectUtil(loader, packagz + ".Tb$Status#SUCCESS");
                Assert.assertEquals(enumStatusSuccess.invoke("value"), (short) 0);
                Assert.assertEquals(enumStatusSuccess.invoke("getValue"), (short) 0);
                Assert.assertEquals(enumStatusSuccess.invoke("getName"), "禁用");

                // 4. 特殊格式的注释
                ObjectUtil enumTypeSuccess = new ObjectUtil(loader, packagz + ".Tb$Type#SUCCESS");
                Assert.assertEquals(enumTypeSuccess.invoke("value"), 0L);
                Assert.assertEquals(enumTypeSuccess.invoke("getValue"), 0L);
                Assert.assertEquals(enumTypeSuccess.invoke("getName"), "禁用");
                ObjectUtil enumTypeFailType = new ObjectUtil(loader, packagz + ".Tb$Type#FAIL_TYPE");
                Assert.assertEquals(enumTypeFailType.invoke("value"), 1L);
                Assert.assertEquals(enumTypeFailType.invoke("getValue"), 1L);
                Assert.assertEquals(enumTypeFailType.invoke("getName"), "启用");

                // 5. 有换行的
                ObjectUtil enumBreakLineSuccess = new ObjectUtil(loader, packagz + ".Tb$BreakLine#SUCCESS");
                Assert.assertEquals(enumBreakLineSuccess.invoke("value"), 0L);
                Assert.assertEquals(enumBreakLineSuccess.invoke("getValue"), 0L);
                Assert.assertEquals(enumBreakLineSuccess.invoke("getName"), "禁用");
                ObjectUtil enumBreakLineFailType = new ObjectUtil(loader, packagz + ".Tb$BreakLine#FAIL_TYPE");
                Assert.assertEquals(enumBreakLineFailType.invoke("value"), 1L);
                Assert.assertEquals(enumBreakLineFailType.invoke("getValue"), 1L);
                Assert.assertEquals(enumBreakLineFailType.invoke("getName"), "启用");

                // 6. 测试 parse
                Class enumBreakLine = loader.loadClass(packagz + ".Tb$BreakLine");
                Method mParseValue = enumBreakLine.getMethod("parseValue", Long.class);
                Assert.assertNull(mParseValue.invoke("parseValue", new Object[]{null}));
                Object em1 = mParseValue.invoke("parseValue", 0L);
                Assert.assertEquals(em1.toString(), "SUCCESS");
                Object em2 = mParseValue.invoke("parseValue", 1L);
                Assert.assertEquals(em2.toString(), "FAIL_TYPE");

                // parseName
                Method mParseName = enumBreakLine.getMethod("parseName", String.class);
                Assert.assertNull(mParseName.invoke("parseName", new Object[]{null}));
                Object em3 = mParseName.invoke("parseName", "禁用");
                Assert.assertEquals(em3.toString(), "SUCCESS");
                Object em4 = mParseName.invoke("parseName", "启用");
                Assert.assertEquals(em4.toString(), "FAIL_TYPE");
            }
        });
    }

    /**
     * 测试生成的enum
     * @throws Exception
     */
    @Test
    public void testEnumWithConfigColumns() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/EnumTypeStatusPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                try {
                    new ObjectUtil(loader, packagz + ".Tb$Field3#SUCCESS");
                } catch (ClassNotFoundException e) {
                    Assert.fail();
                }
            }
        });

        tool = MyBatisGeneratorTool.create("scripts/EnumTypeStatusPlugin/mybatis-generator-with-config-columns.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                try {
                    new ObjectUtil(loader, packagz + ".Tb$Field3#SUCCESS");
                    Assert.fail();
                } catch (ClassNotFoundException e) {
                }
            }
        });
    }
}