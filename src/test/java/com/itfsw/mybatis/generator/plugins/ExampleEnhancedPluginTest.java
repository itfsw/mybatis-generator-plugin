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

import java.io.IOException;
import java.lang.reflect.*;
import java.sql.SQLException;
import java.util.List;

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
    public static void init() throws SQLException, IOException {
        DBHelper.createDB("scripts/ExampleEnhancedPlugin/init.sql");
    }

    /**
     * 测试生成的example方法
     */
    @Test
    public void testExample() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/ExampleEnhancedPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil tbExampleCriteria = new ObjectUtil(tbExample.invoke("createCriteria"));

                // 调用example方法能正常返回
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExampleCriteria.invoke("example"));
                Assert.assertEquals(sql, "select id, field1, field2 from tb");
            }
        });
    }

    /**
     * 测试生成的orderBy方法
     */
    @Test
    public void testOrderBy() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/ExampleEnhancedPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                Object order = Array.newInstance(String.class, 2);
                Array.set(order, 0, "id desc");
                Array.set(order, 1, "field1 asc");
                tbExample.invoke("orderBy", order);  // 可变参数方法直接设置order by

                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample.getObject());
                Assert.assertEquals(sql, "select id, field1, field2 from tb order by id desc , field1 asc");
            }
        });
    }

    /**
     * 测试静态 createCriteria
     * @throws Exception
     */
    @Test
    public void testStaticCreateCriteria() throws Exception{
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/ExampleEnhancedPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExampleCriteria = new ObjectUtil(loader.loadClass(packagz + ".TbExample").getMethod(ExampleEnhancedPlugin.METHOD_NEW_AND_CREATE_CRITERIA).invoke(null));

                // 调用example方法能正常返回
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExampleCriteria.invoke("example"));
                Assert.assertEquals(sql, "select id, field1, field2 from tb");
            }
        });
    }

    /**
     * 测试andIf方法
     */
    @Test
    public void testAndIf() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/ExampleEnhancedPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {

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
                Assert.assertEquals(sql, "select id, field1, field2 from tb WHERE ( id = '5' )");

                // 2. andIf false
                ObjectUtil tbExample1 = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil tbExampleCriteria1 = new ObjectUtil(tbExample1.invoke("createCriteria"));

                method = tbExampleCriteria1.getMethods("andIf").get(0);
                method.invoke(tbExampleCriteria1.getObject(), false, criteriaAdd);

                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample1.getObject());
                Assert.assertEquals(sql, "select id, field1, field2 from tb");
            }
        });
    }

    /**
     * 测试配置ModelColumnPlugin插件
     */
    @Test
    public void testWithModelColumnPlugin() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/ExampleEnhancedPlugin/mybatis-generator-with-ModelColumnPlugin.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil columnField2 = new ObjectUtil(loader, packagz + ".Tb$Column#field2");

                // 1. EqualTo
                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualToColumn", columnField2.getObject());

                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample.getObject());
                Assert.assertEquals(sql, "select id, field1, field2 from tb WHERE ( id = field2 )");
                List list = (List) tbMapper.invoke("selectByExample", tbExample.getObject());
                Assert.assertEquals(list.size(), 1);

                // 2. NotEqualTo
                tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdNotEqualToColumn", columnField2.getObject());

                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample.getObject());
                Assert.assertEquals(sql, "select id, field1, field2 from tb WHERE ( id <> field2 )");
                list = (List) tbMapper.invoke("selectByExample", tbExample.getObject());
                Assert.assertEquals(list.size(), 9);

                // 3. GreaterThan
                tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdGreaterThanColumn", columnField2.getObject());

                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample.getObject());
                Assert.assertEquals(sql, "select id, field1, field2 from tb WHERE ( id > field2 )");
                list = (List) tbMapper.invoke("selectByExample", tbExample.getObject());
                Assert.assertEquals(list.size(), 4);

                // 4. GreaterThanOrEqualTo
                tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdGreaterThanOrEqualToColumn", columnField2.getObject());

                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample.getObject());
                Assert.assertEquals(sql, "select id, field1, field2 from tb WHERE ( id >= field2 )");
                list = (List) tbMapper.invoke("selectByExample", tbExample.getObject());
                Assert.assertEquals(list.size(), 5);

                // 5. LessThan
                tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdLessThanColumn", columnField2.getObject());

                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample.getObject());
                Assert.assertEquals(sql, "select id, field1, field2 from tb WHERE ( id < field2 )");
                list = (List) tbMapper.invoke("selectByExample", tbExample.getObject());
                Assert.assertEquals(list.size(), 5);

                // 6. LessThanOrEqualTo
                tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdLessThanOrEqualToColumn", columnField2.getObject());

                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample.getObject());
                Assert.assertEquals(sql, "select id, field1, field2 from tb WHERE ( id <= field2 )");
                list = (List) tbMapper.invoke("selectByExample", tbExample.getObject());
                Assert.assertEquals(list.size(), 6);
            }
        });
    }

    /**
     * 代理实现
     */
    private class TestInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("add")) {
                ObjectUtil tbExampleCriteria = new ObjectUtil(args[0]);
                tbExampleCriteria.invoke("andIdEqualTo", 5l);
                return tbExampleCriteria.getObject();
            }
            return null;
        }
    }
}
