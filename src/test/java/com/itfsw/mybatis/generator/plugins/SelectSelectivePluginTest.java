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
     * 测试selectByExampleSelective
     * @throws Exception
     */
    @Test
    public void testSelectByExampleSelective() throws Exception {
        // 一些基础测试
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/SelectSelectivePlugin/mybatis-generator.xml");
        tool.generate(() -> DBHelper.resetDB("scripts/SelectSelectivePlugin/init.sql"), new AbstractShellCallback() {
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
                Assert.assertEquals(sql, "select field1 from tb WHERE ( id < '100' ) order by field2 asc");

                ObjectUtil columnField2 = new ObjectUtil(loader, packagz + ".Tb$Column#field2");
                Object columns2 = Array.newInstance(columnField1.getCls(), 2);
                Array.set(columns2, 0, columnField1.getObject());
                Array.set(columns2, 1, columnField2.getObject());

                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExampleSelective", tbExample.getObject(), columns2);
                Assert.assertEquals(sql, "select field1 , field2 from tb WHERE ( id < '100' ) order by field2 asc");


                // 2. 执行sql
                List list = (List) tbMapper.invoke("selectByExampleSelective", tbExample.getObject(), columns1);
                Assert.assertEquals(list.size(), 4);
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

                // 3. 测试 distinct
                tbExample.invoke("setDistinct", true);
                tbExample.set("orderByClause", "field1 asc");
                String sql1 = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExampleSelective", tbExample.getObject(), columns1);
                Assert.assertEquals(sql1, "select distinct field1 from tb WHERE ( id < '100' ) order by field1 asc");
                List list1 = (List) tbMapper.invoke("selectByExampleSelective", tbExample.getObject(), columns1);
                Assert.assertEquals(list1.size(), 3);
            }
        });
        // 测试Selective不传
        tool.generate(() -> DBHelper.resetDB("scripts/SelectSelectivePlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdLessThan", 100l);

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExampleSelective", tbExample.getObject());
                Assert.assertEquals(sql, "select id, field1, field2 from tb WHERE ( id < '100' )");

                // 测试执行
                List list = (List) tbMapper.invoke("selectByExampleSelective", tbExample.getObject(), null);
                // 取第四条数据出来比较
                ObjectUtil result = new ObjectUtil(list.get(3));
                Assert.assertEquals(result.get("id"), 4L);
                Assert.assertEquals(result.get("field1"), "fd3");
                Assert.assertEquals(result.get("field2"), 4);
            }
        });
        // 测试WithBLOBs的情况
        tool.generate(() -> DBHelper.resetDB("scripts/SelectSelectivePlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbBlobsMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbBlobsMapper")));

                ObjectUtil tbBlobsExample = new ObjectUtil(loader, packagz + ".TbBlobsExample");
                ObjectUtil criteria = new ObjectUtil(tbBlobsExample.invoke("createCriteria"));
                criteria.invoke("andIdLessThan", 100l);

                // selective
                // 从base model 和 WithBLOBs都各取一个，更有代表性
                ObjectUtil columnId = new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs$Column#id");
                ObjectUtil columnField2 = new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs$Column#field2");

                Object columns = Array.newInstance(columnId.getCls(), 2);
                Array.set(columns, 0, columnId.getObject());
                Array.set(columns, 1, columnField2.getObject());

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbBlobsMapper.getObject(), "selectByExampleSelective", tbBlobsExample.getObject(), columns);
                Assert.assertEquals(sql, "select id , field2 from tb_blobs WHERE ( id < '100' )");

                // 测试执行
                List list = (List) tbBlobsMapper.invoke("selectByExampleSelective", tbBlobsExample.getObject(), columns);
                // 取第四条数据出来比较(这条数据所有字段是全的)
                ObjectUtil result = new ObjectUtil(list.get(3));
                Assert.assertEquals(result.get("id"), 4L);
                Assert.assertNull(result.get("field1"));
                Assert.assertEquals(result.get("field2"), "L4");
                Assert.assertNull(result.get("field3"));
            }
        });
        // 测试Key的情况
        tool.generate(() -> DBHelper.resetDB("scripts/SelectSelectivePlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbKeysMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbKeysMapper")));

                ObjectUtil tbKeysExample = new ObjectUtil(loader, packagz + ".TbKeysExample");
                ObjectUtil criteria = new ObjectUtil(tbKeysExample.invoke("createCriteria"));
                criteria.invoke("andKey1LessThan", 100l);

                // selective
                // 从base model 和 key model都各取一个，更有代表性
                ObjectUtil columnKey1 = new ObjectUtil(loader, packagz + ".TbKeys$Column#key1");
                ObjectUtil columnField1 = new ObjectUtil(loader, packagz + ".TbKeys$Column#field1");

                Object columns = Array.newInstance(columnKey1.getCls(), 2);
                Array.set(columns, 0, columnKey1.getObject());
                Array.set(columns, 1, columnField1.getObject());

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbKeysMapper.getObject(), "selectByExampleSelective", tbKeysExample.getObject(), columns);
                Assert.assertEquals(sql, "select key1 , field1 from tb_keys WHERE ( key1 < '100' )");

                // 测试执行
                List list = (List) tbKeysMapper.invoke("selectByExampleSelective", tbKeysExample.getObject(), columns);
                // 取第三条数据出来比较(这条数据所有字段是全的)
                ObjectUtil result = new ObjectUtil(list.get(2));
                Assert.assertEquals(result.get("key1"), 3L);
                Assert.assertNull(result.get("key2"));
                Assert.assertEquals(result.get("field1"), "fd2");
                Assert.assertNull(result.get("field2"));
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
        // 一些基础测试
        tool.generate(() -> DBHelper.resetDB("scripts/SelectSelectivePlugin/init.sql"), new AbstractShellCallback() {
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
        // 测试Selective不传
        tool.generate(() -> DBHelper.resetDB("scripts/SelectSelectivePlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByPrimaryKeySelective", 4L);
                Assert.assertEquals(sql, "select id, field1, field2 from tb where id = 4");

                // 测试执行
                ObjectUtil result = new ObjectUtil(tbMapper.invoke("selectByPrimaryKeySelective", 4L, null));
                // 取数据出来比较
                Assert.assertEquals(result.get("id"), 4L);
                Assert.assertEquals(result.get("field1"), "fd3");
                Assert.assertEquals(result.get("field2"), 4);
            }
        });
        // 测试WithBLOBs的情况
        tool.generate(() -> DBHelper.resetDB("scripts/SelectSelectivePlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbBlobsMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbBlobsMapper")));

                // selective
                // 从base model 和 WithBLOBs都各取一个，更有代表性
                ObjectUtil columnId = new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs$Column#id");
                ObjectUtil columnField2 = new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs$Column#field2");

                Object columns = Array.newInstance(columnId.getCls(), 2);
                Array.set(columns, 0, columnId.getObject());
                Array.set(columns, 1, columnField2.getObject());

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbBlobsMapper.getObject(), "selectByPrimaryKeySelective", 4L, columns);
                Assert.assertEquals(sql, "select id , field2 from tb_blobs where id = 4");

                // 测试执行
                ObjectUtil result = new ObjectUtil(tbBlobsMapper.invoke("selectByPrimaryKeySelective", 4L, columns));
                // 取第数据出来比较(这条数据所有字段是全的)
                Assert.assertEquals(result.get("id"), 4L);
                Assert.assertNull(result.get("field1"));
                Assert.assertEquals(result.get("field2"), "L4");
                Assert.assertNull(result.get("field3"));
            }
        });
        // 测试Key的情况
        tool.generate(() -> DBHelper.resetDB("scripts/SelectSelectivePlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbKeysMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbKeysMapper")));

                // selective
                // 从base model 和 key model都各取一个，更有代表性
                ObjectUtil columnKey1 = new ObjectUtil(loader, packagz + ".TbKeys$Column#key1");
                ObjectUtil columnField1 = new ObjectUtil(loader, packagz + ".TbKeys$Column#field1");

                Object columns = Array.newInstance(columnKey1.getCls(), 2);
                Array.set(columns, 0, columnKey1.getObject());
                Array.set(columns, 1, columnField1.getObject());

                ObjectUtil tbKeysKey = new ObjectUtil(loader, packagz + ".TbKeysKey");
                tbKeysKey.set("key1", 3L);
                tbKeysKey.set("key2", "4");

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbKeysMapper.getObject(), "selectByPrimaryKeySelective", tbKeysKey.getObject(), columns);
                Assert.assertEquals(sql, "select key1 , field1 from tb_keys where key1 = 3 and key2 = '4'");

                // 测试执行
                ObjectUtil result = new ObjectUtil(tbKeysMapper.invoke("selectByPrimaryKeySelective", tbKeysKey.getObject(), columns));
                // 取第三条数据出来比较(这条数据所有字段是全的)
                Assert.assertEquals(result.get("key1"), 3L);
                Assert.assertNull(result.get("key2"));
                Assert.assertEquals(result.get("field1"), "fd2");
                Assert.assertNull(result.get("field2"));
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
                Assert.assertEquals(sql, "select field1 from tb WHERE ( id = '3' ) order by field2 asc limit 1");

                // 2. 执行sql
                Object result = tbMapper.invoke("selectOneByExampleSelective", tbExample.getObject(), columns1);
                ObjectUtil tb = new ObjectUtil(result);
                Assert.assertEquals(tb.get("field1"), "fd3");
                Assert.assertNull(tb.get("field2"));
            }
        });

        // 测试Selective不传
        tool.generate(() -> DBHelper.resetDB("scripts/SelectSelectivePlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 4L);

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectOneByExampleSelective", tbExample.getObject());
                Assert.assertEquals(sql, "select id, field1, field2 from tb WHERE ( id = '4' ) limit 1");

                // 测试执行
                ObjectUtil result = new ObjectUtil( tbMapper.invoke("selectOneByExampleSelective", tbExample.getObject(), null));
                // 取第四条数据出来比较
                Assert.assertEquals(result.get("id"), 4L);
                Assert.assertEquals(result.get("field1"), "fd3");
                Assert.assertEquals(result.get("field2"), 4);
            }
        });
        // 测试WithBLOBs的情况
        tool.generate(() -> DBHelper.resetDB("scripts/SelectSelectivePlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbBlobsMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbBlobsMapper")));

                ObjectUtil tbBlobsExample = new ObjectUtil(loader, packagz + ".TbBlobsExample");
                ObjectUtil criteria = new ObjectUtil(tbBlobsExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 4L);

                // selective
                // 从base model 和 WithBLOBs都各取一个，更有代表性
                ObjectUtil columnId = new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs$Column#id");
                ObjectUtil columnField2 = new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs$Column#field2");

                Object columns = Array.newInstance(columnId.getCls(), 2);
                Array.set(columns, 0, columnId.getObject());
                Array.set(columns, 1, columnField2.getObject());

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbBlobsMapper.getObject(), "selectOneByExampleSelective", tbBlobsExample.getObject(), columns);
                Assert.assertEquals(sql, "select id , field2 from tb_blobs WHERE ( id = '4' ) limit 1");

                // 测试执行
                ObjectUtil result = new ObjectUtil( tbBlobsMapper.invoke("selectOneByExampleSelective", tbBlobsExample.getObject(), columns));
                // 取数据出来比较(这条数据所有字段是全的)
                Assert.assertEquals(result.get("id"), 4L);
                Assert.assertNull(result.get("field1"));
                Assert.assertEquals(result.get("field2"), "L4");
                Assert.assertNull(result.get("field3"));
            }
        });
        // 测试Key的情况
        tool.generate(() -> DBHelper.resetDB("scripts/SelectSelectivePlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbKeysMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbKeysMapper")));

                ObjectUtil tbKeysExample = new ObjectUtil(loader, packagz + ".TbKeysExample");
                ObjectUtil criteria = new ObjectUtil(tbKeysExample.invoke("createCriteria"));
                criteria.invoke("andKey1EqualTo", 3L);
                criteria.invoke("andKey2EqualTo", "4");

                // selective
                // 从base model 和 key model都各取一个，更有代表性
                ObjectUtil columnKey1 = new ObjectUtil(loader, packagz + ".TbKeys$Column#key1");
                ObjectUtil columnField1 = new ObjectUtil(loader, packagz + ".TbKeys$Column#field1");

                Object columns = Array.newInstance(columnKey1.getCls(), 2);
                Array.set(columns, 0, columnKey1.getObject());
                Array.set(columns, 1, columnField1.getObject());

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbKeysMapper.getObject(), "selectOneByExampleSelective", tbKeysExample.getObject(), columns);
                Assert.assertEquals(sql, "select key1 , field1 from tb_keys WHERE ( key1 = '3' and key2 = '4' ) limit 1");

                // 测试执行
                ObjectUtil result = new ObjectUtil(tbKeysMapper.invoke("selectOneByExampleSelective", tbKeysExample.getObject(), columns));
                // 取第三条数据出来比较(这条数据所有字段是全的)
                Assert.assertEquals(result.get("key1"), 3L);
                Assert.assertNull(result.get("key2"));
                Assert.assertEquals(result.get("field1"), "fd2");
                Assert.assertNull(result.get("field2"));
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
                Assert.assertEquals(sql, "select field1 from tb WHERE ( id < '100' ) order by field1 asc");

                ObjectUtil columnField2 = new ObjectUtil(loader, packagz + ".Tb$Column#field2");
                Object columns2 = Array.newInstance(columnField1.getCls(), 2);
                Array.set(columns2, 0, columnField1.getObject());
                Array.set(columns2, 1, columnField2.getObject());

                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExampleSelective", tbExample.getObject(), columns2);
                Assert.assertEquals(sql, "select field1 , field2 from tb WHERE ( id < '100' ) order by field1 asc");


                // 2. 执行sql
                List list = (List) tbMapper.invoke("selectByExampleSelective", tbExample.getObject(), columns1);
                Assert.assertEquals(list.size(), 4);
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
                Assert.assertEquals(sql, "select field1 from tb WHERE ( id < '100' ) order by field1 asc");

                ObjectUtil columnField2 = new ObjectUtil(loader, packagz + ".Tb$Column#field2");
                Object columns2 = Array.newInstance(columnField1.getCls(), 2);
                Array.set(columns2, 0, columnField1.getObject());
                Array.set(columns2, 1, columnField2.getObject());

                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExampleSelective", tbExample.getObject(), columns2);
                Assert.assertEquals(sql, "select field1 , field2 from tb WHERE ( id < '100' ) order by field1 asc");


                // 2. 执行sql
                List list = (List) tbMapper.invoke("selectByExampleSelective", tbExample.getObject(), columns1);
                Assert.assertEquals(list.size(), 4);
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
