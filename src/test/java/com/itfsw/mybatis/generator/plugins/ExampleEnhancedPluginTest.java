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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/7/7 15:38
 * ---------------------------------------------------------------------------
 */
public class ExampleEnhancedPluginTest {
    /**
     * 初始化数据库
     */
    @BeforeClass
    public static void init() throws SQLException, IOException, ClassNotFoundException {
        DBHelper.createDB("scripts/ExampleEnhancedPlugin/init.sql");
    }

    /**
     * 测试生成的example方法
     */
    @Test
    public void testExample() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/ExampleEnhancedPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) {
                try {
                    ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                    ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                    ObjectUtil tbExampleCriteria = new ObjectUtil(tbExample.invoke("createCriteria"));

                    // 调用example方法能正常返回
                    String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExampleCriteria.invoke("example"));
                    Assert.assertEquals(sql, "select id, field1 from tb");
                } catch (Exception e) {
                    e.printStackTrace();
                    Assert.assertTrue(false);
                }
            }
        });
    }

    /**
     * 测试生成的orderBy方法
     */
    @Test
    public void testOrderBy() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/ExampleEnhancedPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) {
                try {
                    ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                    ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                    Object order = Array.newInstance(String.class, 2);
                    Array.set(order, 0, "id desc");
                    Array.set(order, 1, "field1 asc");
                    tbExample.invoke("orderBy", order);  // 可变参数方法直接设置order by

                    String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample.getObject());
                    Assert.assertEquals(sql, "select id, field1 from tb order by id desc , field1 asc");
                } catch (Exception e) {
                    e.printStackTrace();
                    Assert.assertTrue(false);
                }
            }
        });
    }

    /**
     * 测试andIf方法
     */
    @Test
    public void testAndIf() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/ExampleEnhancedPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) {
                try {
                    ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                    // 1. andIf true
                    ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                    ObjectUtil tbExampleCriteria = new ObjectUtil(tbExample.invoke("createCriteria"));

                    // 代理实现接口
                    Object criteriaAdd = Proxy.newProxyInstance(loader, new Class[]{
                            loader.loadClass(packagz + ".TbExample$Criteria$ICriteriaAdd")
                    }, new TestInvocationHandler());
                    Method method = tbExampleCriteria.getMethods("andIf").get(0);
                    method.invoke(tbExampleCriteria.getObject(), true, criteriaAdd);

                    String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample.getObject());
                    Assert.assertEquals(sql, "select id, field1 from tb WHERE (  id = '5' )");

                    // 2. andIf false
                    ObjectUtil tbExample1 = new ObjectUtil(loader, packagz + ".TbExample");
                    ObjectUtil tbExampleCriteria1 = new ObjectUtil(tbExample1.invoke("createCriteria"));

                    method = tbExampleCriteria1.getMethods("andIf").get(0);
                    method.invoke(tbExampleCriteria1.getObject(), false, criteriaAdd);

                    sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample1.getObject());
                    Assert.assertEquals(sql, "select id, field1 from tb");
                } catch (Exception e) {
                    e.printStackTrace();
                    Assert.assertTrue(false);
                }
            }
        });
    }

    /**
     * 代理实现
     */
    private class TestInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("add")){
                ObjectUtil tbExampleCriteria = new ObjectUtil(args[0]);
                tbExampleCriteria.invoke("andIdEqualTo", 5l);
                return tbExampleCriteria.getObject();
            }
            return null;
        }
    }
}
