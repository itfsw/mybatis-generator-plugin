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
     * 测试生成的distinct方法
     */
    @Test
    public void testDistinct() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/ExampleEnhancedPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                Object rExample = tbExample.invoke("distinct", true);

                Assert.assertEquals(rExample.getClass().getTypeName(), packagz + ".TbExample");

                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample.getObject());
                Assert.assertEquals(sql, "select distinct id, field1, field2 from tb");
            }
        });
    }

    /**
     * 测试静态 createCriteria
     * @throws Exception
     */
    @Test
    public void testStaticCreateCriteria() throws Exception {
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
     * 测试when方法
     */
    @Test
    public void testWhen() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/ExampleEnhancedPlugin/mybatis-generator.xml");
        // Criteria When
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {

                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                // =============================== Criteria When ======================================
                // 1. when true
                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil tbExampleCriteria = new ObjectUtil(tbExample.invoke("createCriteria"));

                // 代理实现接口
                Object criteriaThen = Proxy.newProxyInstance(loader, new Class[]{
                        loader.loadClass(packagz + ".TbExample$ICriteriaWhen")
                }, new TestCriteriaWhenInvocationHandler(0));
                // 找到只有两个参数的when
                List<Method> methods = tbExampleCriteria.getMethods("when");
                Method method = null;
                for (Method m : methods) {
                    if (m.getParameters().length == 2) {
                        method = m;
                    }
                }
                method.invoke(tbExampleCriteria.getObject(), true, criteriaThen);

                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample.getObject());
                Assert.assertEquals(sql, "select id, field1, field2 from tb WHERE ( id = '5' )");

                // 2. when false
                ObjectUtil tbExample1 = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil tbExampleCriteria1 = new ObjectUtil(tbExample1.invoke("createCriteria"));

                // 找到只有两个参数的when
                methods = tbExampleCriteria1.getMethods("when");
                for (Method m : methods) {
                    if (m.getParameters().length == 2) {
                        method = m;
                    }
                }
                method.invoke(tbExampleCriteria1.getObject(), false, criteriaThen);

                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample1.getObject());
                Assert.assertEquals(sql, "select id, field1, field2 from tb");

                // 3. otherwise
                Object criteriaOtherwise = Proxy.newProxyInstance(loader, new Class[]{
                        loader.loadClass(packagz + ".TbExample$ICriteriaWhen")
                }, new TestCriteriaWhenInvocationHandler(1));

                ObjectUtil tbExample2 = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil tbExampleCriteria2 = new ObjectUtil(tbExample2.invoke("createCriteria"));

                // 找到只有三个参数的when
                methods = tbExampleCriteria2.getMethods("when");
                for (Method m : methods) {
                    if (m.getParameters().length == 3) {
                        method = m;
                    }
                }
                method.invoke(tbExampleCriteria2.getObject(), true, criteriaThen, criteriaOtherwise);
                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample2.getObject());
                Assert.assertEquals(sql, "select id, field1, field2 from tb WHERE ( id = '5' )");

                ObjectUtil tbExample3 = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil tbExampleCriteria3 = new ObjectUtil(tbExample3.invoke("createCriteria"));

                // 找到只有三个参数的when
                methods = tbExampleCriteria3.getMethods("when");
                for (Method m : methods) {
                    if (m.getParameters().length == 3) {
                        method = m;
                    }
                }
                method.invoke(tbExampleCriteria3.getObject(), false, criteriaThen, criteriaOtherwise);
                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample3.getObject());
                Assert.assertEquals(sql, "select id, field1, field2 from tb WHERE ( id = '10' )");

                // =============================== Example When ======================================
                // 1. when true
                ObjectUtil tbExample4 = new ObjectUtil(loader, packagz + ".TbExample");

                // 代理实现接口
                Object exampleThen = Proxy.newProxyInstance(loader, new Class[]{
                        loader.loadClass(packagz + ".TbExample$IExampleWhen")
                }, new TestExampleWhenInvocationHandler(0));
                // 找到只有两个参数的when
                methods = tbExample4.getMethods("when");
                for (Method m : methods) {
                    if (m.getParameters().length == 2) {
                        method = m;
                    }
                }
                method.invoke(tbExample4.getObject(), true, exampleThen);

                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample4.getObject());
                Assert.assertEquals(sql, "select id, field1, field2 from tb WHERE ( field1 = 'f3' )");

                // 2. when false
                ObjectUtil tbExample5 = new ObjectUtil(loader, packagz + ".TbExample");

                // 找到只有两个参数的when
                methods = tbExample5.getMethods("when");
                for (Method m : methods) {
                    if (m.getParameters().length == 2) {
                        method = m;
                    }
                }
                method.invoke(tbExample5.getObject(), false, exampleThen);

                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample5.getObject());
                Assert.assertEquals(sql, "select id, field1, field2 from tb");

                // 3. otherwise
                Object exampleOtherwise = Proxy.newProxyInstance(loader, new Class[]{
                        loader.loadClass(packagz + ".TbExample$IExampleWhen")
                }, new TestExampleWhenInvocationHandler(1));

                ObjectUtil tbExample6 = new ObjectUtil(loader, packagz + ".TbExample");

                // 找到只有三个参数的when
                methods = tbExample6.getMethods("when");
                for (Method m : methods) {
                    if (m.getParameters().length == 3) {
                        method = m;
                    }
                }
                method.invoke(tbExample6.getObject(), true, exampleThen, exampleOtherwise);
                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample6.getObject());
                Assert.assertEquals(sql, "select id, field1, field2 from tb WHERE ( field1 = 'f3' )");

                ObjectUtil tbExample7 = new ObjectUtil(loader, packagz + ".TbExample");

                // 找到只有三个参数的when
                methods = tbExample7.getMethods("when");
                for (Method m : methods) {
                    if (m.getParameters().length == 3) {
                        method = m;
                    }
                }
                method.invoke(tbExample7.getObject(), false, exampleThen, exampleOtherwise);
                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample7.getObject());
                Assert.assertEquals(sql, "select id, field1, field2 from tb WHERE ( field1 = 'f2' )");
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

    /**
     * 代理实现
     */
    private class TestCriteriaWhenInvocationHandler implements InvocationHandler {
        private Integer type;

        public TestCriteriaWhenInvocationHandler(Integer type) {
            this.type = type;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("criteria")) {
                ObjectUtil tbExampleCriteria = new ObjectUtil(args[0]);
                if (type == 0) {
                    tbExampleCriteria.invoke("andIdEqualTo", 5l);
                } else {
                    tbExampleCriteria.invoke("andIdEqualTo", 10l);
                }
                return tbExampleCriteria.getObject();
            }
            return null;
        }
    }

    /**
     * 代理实现
     */
    private class TestExampleWhenInvocationHandler implements InvocationHandler {
        private Integer type;

        public TestExampleWhenInvocationHandler(Integer type) {
            this.type = type;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("example")) {
                ObjectUtil tbExample = new ObjectUtil(args[0]);
                // !!!! 不要使用orderBy 动态代理后对于可变参数判断有问题
                if (type == 0) {
                    ObjectUtil criteria = new ObjectUtil(tbExample.invoke("or"));
                    criteria.invoke("andField1EqualTo", "f3");
                } else {
                    ObjectUtil criteria = new ObjectUtil(tbExample.invoke("or"));
                    criteria.invoke("andField1EqualTo", "f2");
                }
                return tbExample.getObject();
            }
            return null;
        }
    }
}
