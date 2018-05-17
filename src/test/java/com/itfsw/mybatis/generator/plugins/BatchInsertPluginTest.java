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
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/6/26 18:23
 * ---------------------------------------------------------------------------
 */
public class BatchInsertPluginTest {

    /**
     * 初始化
     * @throws IOException
     * @throws SQLException
     */
    @BeforeClass
    public static void init() throws Exception {
        DBHelper.createDB("scripts/BatchInsertPlugin/init.sql");
    }

    /**
     * 测试插件依赖
     * @throws IOException
     * @throws XMLParserException
     * @throws InvalidConfigurationException
     * @throws SQLException
     * @throws InterruptedException
     */
    @Test
    public void testWarnings1() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/BatchInsertPlugin/mybatis-generator-without-model-column-plugin.xml");
        tool.generate();

        Assert.assertTrue(tool.getWarnings().size() == 2);
        Assert.assertEquals(tool.getWarnings().get(0), "itfsw:插件com.itfsw.mybatis.generator.plugins.BatchInsertPlugin插件需配合com.itfsw.mybatis.generator.plugins.ModelColumnPlugin插件使用！");

        // 2. 普通提示
        tool = MyBatisGeneratorTool.create("scripts/BatchInsertPlugin/mybatis-generator-with-allowMultiQueries.xml");
        tool.generate();
        Assert.assertEquals(tool.getWarnings().get(0), "itfsw:插件com.itfsw.mybatis.generator.plugins.BatchInsertPlugin插件您开启了allowMultiQueries支持，注意在jdbc url 配置中增加“allowMultiQueries=true”支持（不怎么建议使用该功能，开启多sql提交会增加sql注入的风险，请确保你所有sql都使用MyBatis书写，请不要使用statement进行sql提交）！");
    }

    /**
     * 测试错误的支持driver
     * @throws IOException
     * @throws XMLParserException
     * @throws InvalidConfigurationException
     * @throws SQLException
     * @throws InterruptedException
     */
    @Test
    public void testWarnings2() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/BatchInsertPlugin/mybatis-generator-with-error-driver.xml");
        tool.generate();

        Assert.assertTrue(tool.getWarnings().size() == 3);
        Assert.assertEquals(tool.getWarnings().get(1), "itfsw:插件com.itfsw.mybatis.generator.plugins.BatchInsertPlugin插件使用前提是数据库为MySQL或者SQLserver，因为返回主键使用了JDBC的getGenereatedKeys方法获取主键！");
    }

    /**
     * 测试生成的方法
     * @throws IOException
     * @throws XMLParserException
     * @throws InvalidConfigurationException
     * @throws SQLException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     */
    @Test
    public void testMethods() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/BatchInsertPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 普通Mapper参数中List泛型为普通Model
                Class clsTbMapper = loader.loadClass(packagz + ".TbMapper");
                int count = 0;
                for (Method method : clsTbMapper.getDeclaredMethods()) {
                    if (method.getName().equals("batchInsert")) {
                        Assert.assertEquals(Util.getListActualType(method.getGenericParameterTypes()[0]), packagz + ".Tb");
                        count++;
                    }
                    if (method.getName().equals("batchInsertSelective")) {
                        Assert.assertEquals(Util.getListActualType(method.getGenericParameterTypes()[0]), packagz + ".Tb");
                        Assert.assertEquals(method.getGenericParameterTypes()[1].getTypeName(), packagz + ".Tb$Column[]");
                        count++;
                    }
                }
                Assert.assertEquals(count, 2);

                // 2. 带有WithBlobs
                Class clsTbBlobsMapper = loader.loadClass(packagz + ".TbBlobsMapper");
                count = 0;
                for (Method method : clsTbBlobsMapper.getDeclaredMethods()) {
                    if (method.getName().equals("batchInsert")) {
                        Assert.assertEquals(Util.getListActualType(method.getGenericParameterTypes()[0]), packagz + ".TbBlobsWithBLOBs");
                        count++;
                    }
                    if (method.getName().equals("batchInsertSelective")) {
                        Assert.assertEquals(Util.getListActualType(method.getGenericParameterTypes()[0]), packagz + ".TbBlobsWithBLOBs");
                        Assert.assertEquals(method.getGenericParameterTypes()[1].getTypeName(), packagz + ".TbBlobsWithBLOBs$Column[]");
                        count++;
                    }
                }
                Assert.assertEquals(count, 2);
            }
        });
    }

    /**
     * 测试生成的sql
     */
    @Test
    public void testBatchInsert() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/BatchInsertPlugin/mybatis-generator.xml");
        tool.generate(() -> DBHelper.resetDB("scripts/BatchInsertPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 测试sql
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));
                List<Object> params = new ArrayList<>();
                params.add(
                        new ObjectUtil(loader, packagz + ".Tb")
                                .set("field1", "test")
                                .getObject()
                );
                params.add(
                        new ObjectUtil(loader, packagz + ".Tb")
                                .set("field1", "test")
                                .set("field2", 1)
                                .getObject()
                );
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "batchInsert", params);
                Assert.assertEquals(sql, "insert into tb (field1, field2) values ('test', null) , ('test', 1)");
                // 2. 执行sql
                Object count = tbMapper.invoke("batchInsert", params);
                Assert.assertEquals(count, 2);

                for (int i = 0; i < params.size(); i++) {
                    ObjectUtil item = new ObjectUtil(params.get(i));
                    Assert.assertEquals(item.get("id"), 1L + i);
                }
            }
        });
    }

    /**
     * 测试生成的sql
     */
    @Test
    public void testBatchInsertSelective() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/BatchInsertPlugin/mybatis-generator.xml");
        tool.generate(() -> DBHelper.resetDB("scripts/BatchInsertPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 测试sql
                ObjectUtil tbBlobsMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbBlobsMapper")));
                List<Object> params = new ArrayList<>();
                params.add(
                        new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs")
                                .set("field1", "test")
                                .getObject()
                );
                params.add(
                        new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs")
                                .set("field1", "test")
                                .set("field2", "test123")
                                .getObject()
                );
                ObjectUtil columnField2 = new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs$Column#field2");
                // java 动态参数不能有两个会冲突，最后一个封装成Array!!!必须使用反射创建指定类型数组，不然调用invoke对了可变参数会检查类型！
                Object columns = Array.newInstance(columnField2.getCls(), 1);
                Array.set(columns, 0, columnField2.getObject());

                String sql = SqlHelper.getFormatMapperSql(tbBlobsMapper.getObject(), "batchInsertSelective", params, columns);
                Assert.assertEquals(sql, "insert into tb_blobs ( field2 ) values ( 'null' ) , ( 'test123' )");
                // 2. 执行sql
                Object count = tbBlobsMapper.invoke("batchInsertSelective", params, columns);
                Assert.assertEquals(count, 2);
            }
        });
    }

    /**
     * 测试开启 AllowMultiQueries 支持
     */
    @Test
    public void testAllowMultiQueries() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/BatchInsertPlugin/mybatis-generator-with-allowMultiQueries.xml");

        // 1. 测试增强的selective
        tool.generate(() -> DBHelper.resetDB("scripts/BatchInsertPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));
                List<Object> params = new ArrayList<>();
                params.add(new ObjectUtil(loader, packagz + ".Tb").set("field1", "test").getObject());
                params.add(new ObjectUtil(loader, packagz + ".Tb").set("field1", "test").set("field2", 1).getObject());

                ObjectUtil columnField2 = new ObjectUtil(loader, packagz + ".Tb$Column#field2");
                Object columns = Array.newInstance(columnField2.getCls(), 1);
                Array.set(columns, 0, columnField2.getObject());

                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "batchInsertSelective", params, columns);
                Assert.assertEquals(sql, "insert into tb ( field2 ) values ( null ) , ( 1 )");
                // 2. 执行sql
                Object count = tbMapper.invoke("batchInsertSelective", params, columns);
                Assert.assertEquals(count, 2);
            }
        });

        // 2. 测试原生非空判断
        tool.generate(() -> DBHelper.resetDB("scripts/BatchInsertPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));
                List<Object> params = new ArrayList<>();
                params.add(new ObjectUtil(loader, packagz + ".Tb").set("field1", "test").getObject());
                params.add(new ObjectUtil(loader, packagz + ".Tb").set("field1", "test").set("field2", 1).getObject());

                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "batchInsertSelective", params, null);
                Assert.assertEquals(sql, "insert into tb ( field1 ) values ( 'test' ) ; insert into tb ( field1, field2 ) values ( 'test', 1 )");
                // 2. 执行sql
                Object count = tbMapper.invoke("batchInsertSelective", params, null);
                Assert.assertTrue((int) count > 0);
            }
        });
    }
}
