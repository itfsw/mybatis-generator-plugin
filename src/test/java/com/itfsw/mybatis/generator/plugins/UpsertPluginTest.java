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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/7/31 14:45
 * ---------------------------------------------------------------------------
 */
public class UpsertPluginTest {
    /**
     * 初始化
     */
    @BeforeClass
    public static void init() throws Exception {
        DBHelper.createDB("scripts/UpsertPlugin/init.sql");
    }

    /**
     * 测试配置异常
     */
    @Test
    public void testWarnings() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        // 1. 没有使用mysql数据库
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/UpsertPlugin/mybatis-generator-with-error-driver.xml");
        tool.generate();
        Assert.assertEquals(tool.getWarnings().get(1), "itfsw:插件com.itfsw.mybatis.generator.plugins.UpsertPlugin插件使用前提是数据库为MySQL！");

        // 2. 普通提示
        tool = MyBatisGeneratorTool.create("scripts/UpsertPlugin/mybatis-generator.xml");
        tool.generate();
        Assert.assertEquals(tool.getWarnings().get(0), "itfsw:插件com.itfsw.mybatis.generator.plugins.UpsertPlugin插件您开启了allowMultiQueries支持，注意在jdbc url 配置中增加“allowMultiQueries=true”支持（不怎么建议使用该功能，开启多sql提交会增加sql注入的风险，请确保你所有sql都使用MyBatis书写，请不要使用statement进行sql提交）！");
    }

    /**
     * 测试 upsert
     */
    @Test
    public void testUpsert() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/UpsertPlugin/mybatis-generator.xml");
        tool.generate(() -> DBHelper.resetDB("scripts/UpsertPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                tb.set("id", 10l);
                tb.set("field1", "ts1");
                tb.set("field2", 5);

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsert", tb.getObject());
                Assert.assertEquals(sql, "insert into tb ( id, field1, field2 ) values ( 10, 'ts1', 5 ) on duplicate key update id = 10, field1 = 'ts1', field2 = 5");
                Object result = tbMapper.invoke("upsert", tb.getObject());
                Assert.assertEquals(result, 1);

                tb.set("field2", 20);
                tbMapper.invoke("upsert", tb.getObject());

                ResultSet rs = DBHelper.execute(sqlSession, "select * from tb where id = 10");
                rs.first();
                Assert.assertEquals(rs.getInt("field2"), 20);

                // 自增主键
                tb.set("id", null);
                tbMapper.invoke("upsert", tb.getObject());
                Assert.assertEquals(tb.get("id"), 11L);
            }
        });
    }

    /**
     * 测试 upsertWithBLOBs
     */
    @Test
    public void testUpsertWithBLOBs() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/UpsertPlugin/mybatis-generator.xml");
        tool.generate(() -> DBHelper.resetDB("scripts/UpsertPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 多个 blob
                ObjectUtil TbBlobsMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbBlobsMapper")));

                ObjectUtil TbBlobsWithBLOBs = new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs");
                TbBlobsWithBLOBs.set("id", 10l);
                TbBlobsWithBLOBs.set("field1", "ts1");
                TbBlobsWithBLOBs.set("field2", "ts2");

                // sql
                String sql = SqlHelper.getFormatMapperSql(TbBlobsMapper.getObject(), "upsertWithBLOBs", TbBlobsWithBLOBs.getObject());
                Assert.assertEquals(sql, "insert into tb_blobs (id, field1, field2, field3) values (10, 'ts1', 'ts2', 'null') on duplicate key update id = 10, field1 = 'ts1', field2 = 'ts2', field3 = 'null'");
                Object result = TbBlobsMapper.invoke("upsertWithBLOBs", TbBlobsWithBLOBs.getObject());
                Assert.assertEquals(result, 1);

                TbBlobsWithBLOBs.set("field2", "ts3");
                TbBlobsMapper.invoke("upsertWithBLOBs", TbBlobsWithBLOBs.getObject());

                ResultSet rs = DBHelper.execute(sqlSession, "select * from tb_blobs where id = 10");
                rs.first();
                Assert.assertEquals(rs.getString("field2"), "ts3");

                // 1. 单个blob
                ObjectUtil TbSingleBlobMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbSingleBlobMapper")));

                ObjectUtil TbSingleBlob = new ObjectUtil(loader, packagz + ".TbSingleBlob");
                TbSingleBlob.set("id", 10l);
                TbSingleBlob.set("field1", "ts1");
                TbSingleBlob.set("field2", 3);

                // sql
                sql = SqlHelper.getFormatMapperSql(TbSingleBlobMapper.getObject(), "upsertWithBLOBs", TbSingleBlob.getObject());
                Assert.assertEquals(sql, "insert into tb_single_blob (id, field2, field1) values (10, 3, 'ts1' ) on duplicate key update id = 10, field2 = 3, field1 = 'ts1'");
                result = TbSingleBlobMapper.invoke("upsertWithBLOBs", TbSingleBlob.getObject());
                Assert.assertEquals(result, 1);

                TbSingleBlob.set("field1", "ts2");
                TbSingleBlobMapper.invoke("upsertWithBLOBs", TbSingleBlob.getObject());

                rs = DBHelper.execute(sqlSession, "select * from tb_single_blob where id = 10");
                rs.first();
                Assert.assertEquals(rs.getString("field1"), "ts2");
            }
        });
    }

    /**
     * 测试 upsertSelective
     */
    @Test
    public void testUpsertSelective() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/UpsertPlugin/mybatis-generator.xml");
        tool.generate(() -> DBHelper.resetDB("scripts/UpsertPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 普通
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                tb.set("id", 20l);
                tb.set("field1", "ts1");

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsertSelective", tb.getObject());
                Assert.assertEquals(sql, "insert into tb ( id, field1 ) values ( 20, 'ts1' ) on duplicate key update id = 20, field1 = 'ts1'");
                Object result = tbMapper.invoke("upsertSelective", tb.getObject());
                Assert.assertEquals(result, 1);

                // 自增主键
                tb.set("id", null);
                tbMapper.invoke("upsertSelective", tb.getObject());
                Assert.assertEquals(tb.get("id"), 21L);

                // 2. blobs
                ObjectUtil TbBlobsMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbBlobsMapper")));

                ObjectUtil TbBlobsWithBLOBs = new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs");
                TbBlobsWithBLOBs.set("id", 500l);
                TbBlobsWithBLOBs.set("field1", "ts1");
                TbBlobsWithBLOBs.set("field2", "ts2");

                // sql
                sql = SqlHelper.getFormatMapperSql(TbBlobsMapper.getObject(), "upsertSelective", TbBlobsWithBLOBs.getObject());
                Assert.assertEquals(sql, "insert into tb_blobs ( id, field1, field2 ) values ( 500, 'ts1', 'ts2' ) on duplicate key update id = 500, field1 = 'ts1', field2 = 'ts2'");
                result = TbBlobsMapper.invoke("upsertSelective", TbBlobsWithBLOBs.getObject());
                Assert.assertEquals(result, 1);
            }
        });
    }

    /**
     * 测试 upsertByExample
     */
    @Test
    public void testUpsertByExample() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/UpsertPlugin/mybatis-generator.xml");
        tool.generate(() -> DBHelper.resetDB("scripts/UpsertPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil TbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(TbExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 50l);

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                tb.set("id", 50l);
                tb.set("field1", "ts1");
                tb.set("field2", 5);

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsertByExample", tb.getObject(), TbExample.getObject());
                Assert.assertEquals(sql, "update tb set id = 50, field1 = 'ts1', field2 = 5 WHERE ( id = '50' ) ; insert into tb ( id, field1, field2 ) select 50, 'ts1', 5 from dual where not exists ( select 1 from tb WHERE ( id = '50' ) )");
                tbMapper.invoke("upsertByExample", tb.getObject(), TbExample.getObject());


                tb.set("field2", 20);
                tbMapper.invoke("upsertByExample", tb.getObject(), TbExample.getObject());

                ResultSet rs = DBHelper.execute(sqlSession, "select * from tb where id = 50");
                rs.first();
                Assert.assertEquals(rs.getInt("field2"), 20);
            }
        });
    }

    /**
     * 测试 upsertByExampleWithBLOBs
     */
    @Test
    public void testUpsertByExampleWithBLOBs() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/UpsertPlugin/mybatis-generator.xml");
        tool.generate(() -> DBHelper.resetDB("scripts/UpsertPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 多个 blob
                ObjectUtil TbBlobsMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbBlobsMapper")));

                ObjectUtil TbBlobsExample = new ObjectUtil(loader, packagz + ".TbBlobsExample");
                ObjectUtil criteria = new ObjectUtil(TbBlobsExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 60l);

                ObjectUtil TbBlobsWithBLOBs = new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs");
                TbBlobsWithBLOBs.set("id", 60l);
                TbBlobsWithBLOBs.set("field1", "ts1");
                TbBlobsWithBLOBs.set("field2", "ts2");

                // sql
                String sql = SqlHelper.getFormatMapperSql(TbBlobsMapper.getObject(), "upsertByExampleWithBLOBs", TbBlobsWithBLOBs.getObject(), TbBlobsExample.getObject());
                Assert.assertEquals(sql, "update tb_blobs set id = 60, field1 = 'ts1', field2 = 'ts2', field3 = 'null' WHERE ( id = '60' ) ; insert into tb_blobs (id, field1, field2, field3) select 60, 'ts1', 'ts2', 'null' from dual where not exists ( select 1 from tb_blobs WHERE ( id = '60' ) )");
                TbBlobsMapper.invoke("upsertByExampleWithBLOBs", TbBlobsWithBLOBs.getObject(), TbBlobsExample.getObject());

                TbBlobsWithBLOBs.set("field2", "ts3");
                TbBlobsMapper.invoke("upsertByExampleWithBLOBs", TbBlobsWithBLOBs.getObject(), TbBlobsExample.getObject());

                ResultSet rs = DBHelper.execute(sqlSession, "select * from tb_blobs where id = 60");
                rs.first();
                Assert.assertEquals(rs.getString("field2"), "ts3");

                // 1. 单个blob
                ObjectUtil TbSingleBlobMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbSingleBlobMapper")));

                ObjectUtil TbSingleBlobExample = new ObjectUtil(loader, packagz + ".TbSingleBlobExample");
                ObjectUtil criteria1 = new ObjectUtil(TbSingleBlobExample.invoke("createCriteria"));
                criteria1.invoke("andIdEqualTo", 70l);


                ObjectUtil TbSingleBlob = new ObjectUtil(loader, packagz + ".TbSingleBlob");
                TbSingleBlob.set("id", 70l);
                TbSingleBlob.set("field1", "ts1");
                TbSingleBlob.set("field2", 3);

                // sql
                sql = SqlHelper.getFormatMapperSql(TbSingleBlobMapper.getObject(), "upsertByExampleWithBLOBs", TbSingleBlob.getObject(), TbSingleBlobExample.getObject());
                Assert.assertEquals(sql, "update tb_single_blob set id = 70, field2 = 3, field1 = 'ts1' WHERE ( id = '70' ) ; insert into tb_single_blob (id, field2, field1) select 70, 3, 'ts1' from dual where not exists ( select 1 from tb_single_blob WHERE ( id = '70' ) )");
                TbSingleBlobMapper.invoke("upsertByExampleWithBLOBs", TbSingleBlob.getObject(), TbSingleBlobExample.getObject());

                TbSingleBlob.set("field1", "ts2");
                TbSingleBlobMapper.invoke("upsertByExampleWithBLOBs", TbSingleBlob.getObject(), TbSingleBlobExample.getObject());

                rs = DBHelper.execute(sqlSession, "select * from tb_single_blob where id = 70");
                rs.first();
                Assert.assertEquals(rs.getString("field1"), "ts2");
            }
        });
    }

    /**
     * 测试 upsertByExampleSelective
     */
    @Test
    public void testUpsertByExampleSelective() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/UpsertPlugin/mybatis-generator.xml");
        tool.generate(() -> DBHelper.resetDB("scripts/UpsertPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 普通
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil TbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(TbExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 100l);

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                tb.set("id", 100l);
                tb.set("field1", "ts1");

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsertByExampleSelective", tb.getObject(), TbExample.getObject());
                Assert.assertEquals(sql, "update tb set id = 100, field1 = 'ts1' WHERE ( id = '100' ) ; insert into tb ( id, field1 ) select 100, 'ts1' from dual where not exists ( select 1 from tb WHERE ( id = '100' ) )");
                tbMapper.invoke("upsertByExampleSelective", tb.getObject(), TbExample.getObject());

                // 2. blobs
                ObjectUtil TbBlobsMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbBlobsMapper")));

                ObjectUtil TbBlobsExample = new ObjectUtil(loader, packagz + ".TbBlobsExample");
                ObjectUtil criteria1 = new ObjectUtil(TbBlobsExample.invoke("createCriteria"));
                criteria1.invoke("andIdEqualTo", 60l);

                ObjectUtil TbBlobsWithBLOBs = new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs");
                TbBlobsWithBLOBs.set("id", 200l);
                TbBlobsWithBLOBs.set("field1", "ts1");
                TbBlobsWithBLOBs.set("field2", "ts2");

                // sql
                sql = SqlHelper.getFormatMapperSql(TbBlobsMapper.getObject(), "upsertByExampleSelective", TbBlobsWithBLOBs.getObject(), TbBlobsExample.getObject());
                Assert.assertEquals(sql, "update tb_blobs set id = 200, field1 = 'ts1', field2 = 'ts2' WHERE ( id = '60' ) ; insert into tb_blobs ( id, field1, field2 ) select 200, 'ts1', 'ts2' from dual where not exists ( select 1 from tb_blobs WHERE ( id = '60' ) )");
                TbBlobsMapper.invoke("upsertByExampleSelective", TbBlobsWithBLOBs.getObject(), TbBlobsExample.getObject());
            }
        });
    }

    /**
     * 测试 存在自增主键的情况
     */
    @Test
    public void testWithIdentityAndGeneratedAlwaysColumns() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/UpsertPlugin/mybatis-generator.xml");
        tool.generate(() -> DBHelper.resetDB("scripts/UpsertPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // ------------------------------------------ upsert ---------------------------------------------------
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbWithIncIdMapper")));

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".TbWithIncId");
                tb.set("field1", "ts1");
                tb.set("field2", 5);

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsert", tb.getObject());
                Assert.assertEquals(sql, "insert into tb_with_inc_id ( field1, field2 ) values ( 'ts1', 5 ) on duplicate key update field1 = 'ts1', field2 = 5");
                tbMapper.invoke("upsert", tb.getObject());
                // 获取ID
                Long id = (Long) tb.get("id");

                tb.set("field2", 20);
                tbMapper.invoke("upsert", tb.getObject());

                ResultSet rs = DBHelper.execute(sqlSession, "select * from tb_with_inc_id where id = " + id);
                rs.first();
                Assert.assertEquals(rs.getInt("field2"), 20);

                // ------------------------------------------ upsertWithBlobs ---------------------------------------------------
                tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbBlobsWithIncIdMapper")));

                ObjectUtil tbWithBLOBs = new ObjectUtil(loader, packagz + ".TbBlobsWithIncIdWithBLOBs");
                tbWithBLOBs.set("field1", "ts1");
                tbWithBLOBs.set("field2", "ts2");

                // sql
                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsertWithBLOBs", tbWithBLOBs.getObject());
                Assert.assertEquals(sql, "insert into tb_blobs_with_inc_id ( field1, field2, field3 ) values ( 'ts1', 'ts2', 'null' ) on duplicate key update field1 = 'ts1', field2 = 'ts2', field3 = 'null'");
                tbMapper.invoke("upsert", tbWithBLOBs.getObject());
                // 获取ID
                id = (Long) tbWithBLOBs.get("id");

                tbWithBLOBs.set("field2", "ts3");
                tbMapper.invoke("upsertWithBLOBs", tbWithBLOBs.getObject());

                rs = DBHelper.execute(sqlSession, "select * from tb_blobs_with_inc_id where id = " + id);
                rs.first();
                Assert.assertEquals(rs.getString("field2"), "ts3");

                // sql(withOutBlobs)
                tb = new ObjectUtil(loader, packagz + ".TbBlobsWithIncId");
                tb.set("field1", "ts4");

                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsert", tb.getObject());
                Assert.assertEquals(sql, "insert into tb_blobs_with_inc_id ( field1 ) values ( 'ts4' ) on duplicate key update field1 = 'ts4'");
                tbMapper.invoke("upsert", tb.getObject());
                // 获取ID
                id = (Long) tb.get("id");

                tb.set("field1", "ts5");
                tbMapper.invoke("upsert", tb.getObject());

                rs = DBHelper.execute(sqlSession, "select * from tb_blobs_with_inc_id where id = " + id);
                rs.first();
                Assert.assertEquals(rs.getString("field1"), "ts5");

                // ------------------------------------------ upsertByExample ---------------------------------------------------
                tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbWithIncIdMapper")));

                tb = new ObjectUtil(loader, packagz + ".TbWithIncId");
                tb.set("field1", "ts6");
                tb.set("field2", 5);

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbWithIncIdExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andField1EqualTo", "ts6");

                // sql
                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsertByExample", tb.getObject(), tbExample.getObject());
                Assert.assertEquals(sql, "update tb_with_inc_id set field1 = 'ts6', field2 = 5 WHERE ( field1 = 'ts6' ) ; insert into tb_with_inc_id ( field1, field2 ) select 'ts6', 5 from dual where not exists ( select 1 from tb_with_inc_id WHERE ( field1 = 'ts6' ) )");
                tbMapper.invoke("upsertByExample", tb.getObject(), tbExample.getObject());

                tb.set("field2", 21);
                tbMapper.invoke("upsertByExample", tb.getObject(), tbExample.getObject());

                rs = DBHelper.execute(sqlSession, "select * from tb_with_inc_id where field1 = 'ts6'");
                rs.first();
                Assert.assertEquals(rs.getInt("field2"), 21);
            }
        });
    }

    /**
     * 测试批量batchUpsert
     */
    @Test
    public void testBatchUpsert() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/UpsertPlugin/mybatis-generator-with-allowBatchUpsert.xml");
        tool.generate(() -> DBHelper.resetDB("scripts/UpsertPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));
                List<Object> params = new ArrayList<>();
                params.add(new ObjectUtil(loader, packagz + ".Tb").set("id", 1L).set("field1", "ts1").getObject());
                params.add(new ObjectUtil(loader, packagz + ".Tb").set("id", 6L).set("field1", "ts2").set("field2", 1).getObject());

                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "batchUpsert", params);
                Assert.assertEquals(sql, "insert into tb (id, field1, field2) values (1, 'ts1', null ) , (6, 'ts2', 1 ) on duplicate key update id = values(id), field1 = values(field1), field2 = values(field2)");
                // 2. 执行sql
                Object count = tbMapper.invoke("batchUpsert", params);
                Assert.assertEquals(count, 3);

                // 验证
                ResultSet rs = DBHelper.execute(sqlSession, "select * from tb where id = 1");
                rs.first();
                Assert.assertEquals(rs.getString("field1"), "ts1");

                rs = DBHelper.execute(sqlSession, "select * from tb where id = 6");
                rs.first();
                Assert.assertEquals(rs.getString("field1"), "ts2");
            }
        });
    }

    /**
     * 测试批量batchUpsertWithBLOBs
     */
    @Test
    public void testBatchUpsertWithBLOBs() throws Exception {
        // 1. batchUpsert
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/UpsertPlugin/mybatis-generator-with-allowBatchUpsert.xml");
        tool.generate(() -> DBHelper.resetDB("scripts/UpsertPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbBlobsMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbBlobsMapper")));
                List<Object> params = new ArrayList<>();
                params.add(new ObjectUtil(loader, packagz + ".TbBlobs").set("id", 1L).set("field1", "ts1").getObject());
                params.add(new ObjectUtil(loader, packagz + ".TbBlobs").set("id", 6L).set("field1", "ts2").getObject());

                String sql = SqlHelper.getFormatMapperSql(tbBlobsMapper.getObject(), "batchUpsert", params);
                Assert.assertEquals(sql, "insert into tb_blobs (id, field1) values (1, 'ts1') , (6, 'ts2') on duplicate key update id = values(id), field1 = values(field1)");
                // 2. 执行sql
                Object count = tbBlobsMapper.invoke("batchUpsert", params);
                Assert.assertEquals(count, 3);

                // 验证
                ResultSet rs = DBHelper.execute(sqlSession, "select * from tb_blobs where id = 1");
                rs.first();
                Assert.assertEquals(rs.getString("field1"), "ts1");

                rs = DBHelper.execute(sqlSession, "select * from tb_blobs where id = 6");
                rs.first();
                Assert.assertEquals(rs.getString("field1"), "ts2");
            }
        });

        // 2. batchUpsertWithBLOBs
        tool.generate(() -> DBHelper.resetDB("scripts/UpsertPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbBlobsMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbBlobsMapper")));
                List<Object> params = new ArrayList<>();
                params.add(new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs").set("id", 1L).set("field1", "ts1").set("field3", "ff1").getObject());
                params.add(new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs").set("id", 6L).set("field1", "ts2").set("field3", "ff2").getObject());

                String sql = SqlHelper.getFormatMapperSql(tbBlobsMapper.getObject(), "batchUpsertWithBLOBs", params);
                Assert.assertEquals(sql, "insert into tb_blobs (id, field1, field2, field3) values (1, 'ts1', 'null', 'ff1') , (6, 'ts2', 'null', 'ff2') on duplicate key update id = values(id), field1 = values(field1), field2 = values(field2), field3 = values(field3)");
                // 2. 执行sql
                Object count = tbBlobsMapper.invoke("batchUpsertWithBLOBs", params);
                Assert.assertEquals(count, 3);

                // 验证
                ResultSet rs = DBHelper.execute(sqlSession, "select * from tb_blobs where id = 1");
                rs.first();
                Assert.assertEquals(rs.getString("field1"), "ts1");
                Assert.assertEquals(rs.getString("field3"), "ff1");

                rs = DBHelper.execute(sqlSession, "select * from tb_blobs where id = 6");
                rs.first();
                Assert.assertEquals(rs.getString("field1"), "ts2");
                Assert.assertEquals(rs.getString("field3"), "ff2");
            }
        });
    }

    /**
     * 测试批量batchUpsertSelective
     */
    @Test
    public void testBatchUpsertSelective() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/UpsertPlugin/mybatis-generator-with-allowBatchUpsert.xml");
        tool.generate(() -> DBHelper.resetDB("scripts/UpsertPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));
                List<Object> params = new ArrayList<>();
                params.add(new ObjectUtil(loader, packagz + ".Tb").set("id", 1L).set("field1", "ts1").getObject());
                params.add(new ObjectUtil(loader, packagz + ".Tb").set("id", 6L).set("field1", "ts2").set("field2", 1).getObject());

                ObjectUtil columnId = new ObjectUtil(loader, packagz + ".Tb$Column#id");
                ObjectUtil columnField1 = new ObjectUtil(loader, packagz + ".Tb$Column#field1");
                Object columns = Array.newInstance(columnId.getCls(), 2);
                Array.set(columns, 0, columnId.getObject());
                Array.set(columns, 1, columnField1.getObject());

                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "batchUpsertSelective", params, columns);
                Assert.assertEquals(sql, "insert into tb ( id , field1 ) values ( 1 , 'ts1' ) , ( 6 , 'ts2' ) on duplicate key update id = values(id) , field1 = values(field1)");
                // 2. 执行sql
                Object count = tbMapper.invoke("batchUpsertSelective", params, columns);
                Assert.assertEquals(count, 3);

                // 验证
                ResultSet rs = DBHelper.execute(sqlSession, "select * from tb where id = 1");
                rs.first();
                Assert.assertEquals(rs.getString("field1"), "ts1");

                rs = DBHelper.execute(sqlSession, "select * from tb where id = 6");
                rs.first();
                Assert.assertEquals(rs.getString("field1"), "ts2");
                Assert.assertNull(rs.getString("field2"));
            }
        });
    }
}
