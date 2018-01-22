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
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.MyBatisGenerator;

import java.lang.reflect.Array;
import java.util.List;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/6/29 13:34
 * ---------------------------------------------------------------------------
 */
public class SelectSelectivePluginTest {

    @BeforeClass
    public static void init() throws Exception {
        DBHelper.createDB("scripts/SelectSelectivePlugin/init.sql");
    }

    /**
     * 测试生成的方法
     * @throws Exception
     */
    @Test
    public void testSelectByExampleSelective() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/SelectSelectivePlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 测试sql
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdLessThan", 100l);
                tbExample.set("orderByClause", "field2 asc");

                ObjectUtil columnField1 = new ObjectUtil(loader, packagz + ".Tb$Column#field1");
                // java 动态参数不能有两个会冲突，最后一个封装成Array!!!必须使用反射创建指定类型数组，不然调用invoke对了可变参数会检查类型！
                Object columns1 = Array.newInstance(columnField1.getCls(), 1);
                Array.set(columns1, 0, columnField1.getObject());

                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExampleSelective", tbExample.getObject(), columns1);
                Assert.assertEquals(sql, "select field1 from tb WHERE (  id < '100' )  order by field2 asc");

                ObjectUtil columnField2 = new ObjectUtil(loader, packagz + ".Tb$Column#field2");
                Object columns2 = Array.newInstance(columnField1.getCls(), 2);
                Array.set(columns2, 0, columnField1.getObject());
                Array.set(columns2, 1, columnField2.getObject());

                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExampleSelective", tbExample.getObject(), columns2);
                Assert.assertEquals(sql, "select field1 ,  field2 from tb WHERE (  id < '100' )  order by field2 asc");


                // 2. 执行sql
                List list = (List) tbMapper.invoke("selectByExampleSelective", tbExample.getObject(), columns1);
                Assert.assertEquals(list.size(), 3);
                int index = 0;
                for (Object obj : list) {
                    if (index == 1) {
                        Assert.assertNull(obj);
                    } else {
                        ObjectUtil objectUtil = new ObjectUtil(obj);
                        // 没有查询这两个字段
                        if (objectUtil.get("id") != null || objectUtil.get("field2") != null) {
                            Assert.assertTrue(false);
                        }
                        if (index == 0) {
                            Assert.assertEquals(objectUtil.get("field1"), "fd1");
                        } else {
                            Assert.assertEquals(objectUtil.get("field1"), "fd3");
                        }
                    }

                    index++;
                }
            }
        });
    }

    /**
     * 测试生成的方法
     * @throws Exception
     */
    @Test
    public void testSelectByPrimaryKeySelective() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/SelectSelectivePlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 测试sql
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil columnField1 = new ObjectUtil(loader, packagz + ".TbKeys$Column#field1");
                // java 动态参数不能有两个会冲突，最后一个封装成Array!!!必须使用反射创建指定类型数组，不然调用invoke对了可变参数会检查类型！
                Object columns1 = Array.newInstance(columnField1.getCls(), 1);
                Array.set(columns1, 0, columnField1.getObject());

                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByPrimaryKeySelective", 1, columns1);
                Assert.assertEquals(sql, "select field1 from tb where id = 1");

                // 2. 测试xxxKey
                ObjectUtil tbKeysMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbKeysMapper")));
                ObjectUtil tbKeysKey = new ObjectUtil(loader, packagz + ".TbKeysKey");
                tbKeysKey.set("key1", 1l);
                tbKeysKey.set("key2", "2");

                ObjectUtil columnField2 = new ObjectUtil(loader, packagz + ".TbKeys$Column#field2");
                // java 动态参数不能有两个会冲突，最后一个封装成Array!!!必须使用反射创建指定类型数组，不然调用invoke对了可变参数会检查类型！
                Object columns2 = Array.newInstance(columnField2.getCls(), 1);
                Array.set(columns2, 0, columnField2.getObject());

                sql = SqlHelper.getFormatMapperSql(tbKeysMapper.getObject(), "selectByPrimaryKeySelective", tbKeysKey.getObject(), columns2);
                Assert.assertEquals(sql, "select field2 from tb_keys where key1 = 1 and key2 = '2'");

                // 3. 执行sql
                Object tbKeys = tbKeysMapper.invoke("selectByPrimaryKeySelective", tbKeysKey.getObject(), columns1);
                Assert.assertEquals(new ObjectUtil(tbKeys).get("field1"), "fd1");
            }
        });
    }

    /**
     * 测试生成的方法
     * @throws Exception
     */
    @Test
    public void testSelectOneByExampleSelective() throws Exception {
        // 没有配置SelectOneByExamplePlugin插件时不生成对应方法
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/SelectSelectivePlugin/mybatis-generator.xml");
        MyBatisGenerator myBatisGenerator = tool.generate();
        List<GeneratedJavaFile> list = myBatisGenerator.getGeneratedJavaFiles();
        for (GeneratedJavaFile file : list) {
            if (file.getFileName().equals("TbMapper.java")) {
                Assert.assertFalse(file.getFormattedContent().matches(".*selectByExampleSelective.*"));
            }
        }

        // 配置了SelectOneByExamplePlugin
        tool = MyBatisGeneratorTool.create("scripts/SelectSelectivePlugin/mybatis-generator-with-SelectOneByExamplePlugin.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 测试sql
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 3l);
                tbExample.set("orderByClause", "field2 asc");

                ObjectUtil columnField1 = new ObjectUtil(loader, packagz + ".Tb$Column#field1");
                // java 动态参数不能有两个会冲突，最后一个封装成Array!!!必须使用反射创建指定类型数组，不然调用invoke对了可变参数会检查类型！
                Object columns1 = Array.newInstance(columnField1.getCls(), 1);
                Array.set(columns1, 0, columnField1.getObject());

                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectOneByExampleSelective", tbExample.getObject(), columns1);
                Assert.assertEquals(sql, "select field1 from tb WHERE (  id = '3' )  order by field2 asc limit 1");

                // 2. 执行sql
                Object result = tbMapper.invoke("selectOneByExampleSelective", tbExample.getObject(), columns1);
                ObjectUtil tb = new ObjectUtil(result);
                Assert.assertEquals(tb.get("field1"), "fd3");
                Assert.assertNull(tb.get("field2"));
            }
        });
    }

    /**
     * 测试生成的方法
     * @throws Exception
     */
    @Test
    public void testSelectiveWithOrWithoutConstructorBased() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/SelectSelectivePlugin/mybatis-generator-with-constructorBased-false.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 测试sql
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdLessThan", 100l);
                tbExample.set("orderByClause", "field1 asc");

                ObjectUtil columnField1 = new ObjectUtil(loader, packagz + ".Tb$Column#tsF1");
                // java 动态参数不能有两个会冲突，最后一个封装成Array!!!必须使用反射创建指定类型数组，不然调用invoke对了可变参数会检查类型！
                Object columns1 = Array.newInstance(columnField1.getCls(), 1);
                Array.set(columns1, 0, columnField1.getObject());

                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExampleSelective", tbExample.getObject(), columns1);
                Assert.assertEquals(sql, "select field1 from tb WHERE (  id < '100' )  order by field1 asc");

                ObjectUtil columnField2 = new ObjectUtil(loader, packagz + ".Tb$Column#field2");
                Object columns2 = Array.newInstance(columnField1.getCls(), 2);
                Array.set(columns2, 0, columnField1.getObject());
                Array.set(columns2, 1, columnField2.getObject());

                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExampleSelective", tbExample.getObject(), columns2);
                Assert.assertEquals(sql, "select field1 ,  field2 from tb WHERE (  id < '100' )  order by field1 asc");


                // 2. 执行sql
                List list = (List) tbMapper.invoke("selectByExampleSelective", tbExample.getObject(), columns1);
                Assert.assertEquals(list.size(), 3);
                int index = 0;
                for (Object obj : list) {
                    if (index == 0) {
                        Assert.assertNull(obj);
                    } else {
                        ObjectUtil objectUtil = new ObjectUtil(obj);
                        // 没有查询这两个字段
                        if (objectUtil.get("id") != null || objectUtil.get("field2") != null) {
                            Assert.assertTrue(false);
                        }
                        if (index == 1) {
                            Assert.assertEquals(objectUtil.get("tsF1"), "fd1");
                        } else {
                            Assert.assertEquals(objectUtil.get("tsF1"), "fd3");
                        }
                    }

                    index++;
                }
            }
        });

        tool = MyBatisGeneratorTool.create("scripts/SelectSelectivePlugin/mybatis-generator-with-constructorBased-true.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 测试sql
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdLessThan", 100l);
                tbExample.set("orderByClause", "field1 asc");

                ObjectUtil columnField1 = new ObjectUtil(loader, packagz + ".Tb$Column#tsF1");
                // java 动态参数不能有两个会冲突，最后一个封装成Array!!!必须使用反射创建指定类型数组，不然调用invoke对了可变参数会检查类型！
                Object columns1 = Array.newInstance(columnField1.getCls(), 1);
                Array.set(columns1, 0, columnField1.getObject());

                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExampleSelective", tbExample.getObject(), columns1);
                Assert.assertEquals(sql, "select field1 from tb WHERE (  id < '100' )  order by field1 asc");

                ObjectUtil columnField2 = new ObjectUtil(loader, packagz + ".Tb$Column#field2");
                Object columns2 = Array.newInstance(columnField1.getCls(), 2);
                Array.set(columns2, 0, columnField1.getObject());
                Array.set(columns2, 1, columnField2.getObject());

                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExampleSelective", tbExample.getObject(), columns2);
                Assert.assertEquals(sql, "select field1 ,  field2 from tb WHERE (  id < '100' )  order by field1 asc");


                // 2. 执行sql
                List list = (List) tbMapper.invoke("selectByExampleSelective", tbExample.getObject(), columns1);
                Assert.assertEquals(list.size(), 3);
                int index = 0;
                for (Object obj : list) {
                    if (index == 0) {
                        Assert.assertNull(obj);
                    } else {
                        ObjectUtil objectUtil = new ObjectUtil(obj);
                        // 没有查询这两个字段
                        if (objectUtil.get("id") != null || objectUtil.get("field2") != null) {
                            Assert.assertTrue(false);
                        }
                        if (index == 1) {
                            Assert.assertEquals(objectUtil.get("tsF1"), "fd1");
                        } else {
                            Assert.assertEquals(objectUtil.get("tsF1"), "fd3");
                        }
                    }

                    index++;
                }
            }
        });
    }
}
