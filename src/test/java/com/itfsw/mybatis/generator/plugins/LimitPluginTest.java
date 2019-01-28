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

import com.itfsw.mybatis.generator.plugins.tools.*;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.List;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/7/7 15:02
 * ---------------------------------------------------------------------------
 */
public class LimitPluginTest {

    /**
     * 初始化数据库
     */
    @BeforeClass
    public static void init() throws SQLException, IOException, ClassNotFoundException {
        DBHelper.createDB("scripts/LimitPlugin/init.sql");
    }

    /**
     * 测试配置异常
     */
    @Test
    public void testWarnings() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/LimitPlugin/mybatis-generator-with-error-driver.xml");
        tool.generate();

        Assert.assertEquals(tool.getWarnings().get(1), "itfsw:插件com.itfsw.mybatis.generator.plugins.LimitPlugin只支持MySQL数据库！");
    }

    /**
     * 测试生成的Sql语句和具体执行
     */
    @Test
    public void testSqlAndExecute() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/LimitPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 测试limit 方法
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                tbExample.invoke("limit", 5);

                // 调用limit(5)方法
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample.getObject());
                Assert.assertEquals(sql, "select id, field1 from tb limit 5");
                // 调用limit(1, 5)方法
                tbExample.invoke("limit", 1, 5);
                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample.getObject());
                Assert.assertEquals(sql, "select id, field1 from tb limit 1, 5");
                // 执行一次看结果
                List list = (List) tbMapper.invoke("selectByExample", tbExample.getObject());
                Assert.assertEquals(list.size(), 5);
                Assert.assertEquals(new ObjectUtil(list.get(0)).get("id"), 2l);

                // 2. 测试page 方法
                tbExample.invoke("page", 2, 3);
                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample.getObject());
                Assert.assertEquals(sql, "select id, field1 from tb limit 6, 3");
                // 执行
                list = (List) tbMapper.invoke("selectByExample", tbExample.getObject());
                Assert.assertEquals(list.size(), 3);
                Assert.assertEquals(new ObjectUtil(list.get(0)).get("id"), 7l);
            }
        });
    }

    /**
     * 测试startPage
     */
    @Test
    public void testWithStartPage() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/LimitPlugin/mybatis-generator-with-startPage.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 测试limit 方法
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));
                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");

                // 2. 测试page 方法
                tbExample.invoke("page", 2, 3);
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample.getObject());
                Assert.assertEquals(sql, "select id, field1 from tb limit 3, 3");
                // 执行
                List list = (List) tbMapper.invoke("selectByExample", tbExample.getObject());
                Assert.assertEquals(list.size(), 3);
                Assert.assertEquals(new ObjectUtil(list.get(0)).get("id"), 4l);
            }
        });
    }

    /**
     * 整合SelectSelectivePlugin
     * @throws Exception
     */
    @Test
    public void testWithSelectSelectivePlugin() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/LimitPlugin/mybatis-generator-with-SelectSelectivePlugin.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 测试limit 方法
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                tbExample.invoke("limit", 5);

                ObjectUtil columnField1 = new ObjectUtil(loader, packagz + ".Tb$Column#field1");
                // java 动态参数不能有两个会冲突，最后一个封装成Array!!!必须使用反射创建指定类型数组，不然调用invoke对了可变参数会检查类型！
                Object columns = Array.newInstance(columnField1.getCls(), 1);
                Array.set(columns, 0, columnField1.getObject());

                // 调用limit(5)方法
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExampleSelective", tbExample.getObject(), columns);
                Assert.assertEquals(sql, "select field1 from tb limit 5");
                // 调用limit(1, 5)方法
                tbExample.invoke("limit", 1, 5);
                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExampleSelective", tbExample.getObject(), columns);
                Assert.assertEquals(sql, "select field1 from tb limit 1, 5");
                // 执行一次看结果
                List list = (List) tbMapper.invoke("selectByExampleSelective", tbExample.getObject(), columns);
                Assert.assertEquals(list.size(), 5);
            }
        });
    }
}
