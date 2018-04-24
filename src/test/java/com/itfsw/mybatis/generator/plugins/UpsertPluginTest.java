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
import java.sql.ResultSet;
import java.sql.SQLException;

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
     * 初始化数据库
     */
    @BeforeClass
    public static void init() throws SQLException, IOException, ClassNotFoundException {
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
    public void testUpsert() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/UpsertPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                tb.set("id", 10l);
                tb.set("field1", "ts1");
                tb.set("field2", 5);

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsert", tb.getObject());
                Assert.assertEquals(sql, "insert into tb (id, field1, field2) values (10, 'ts1', 5) on duplicate key update id = 10, field1 = 'ts1', field2 = 5");
                Object result = tbMapper.invoke("upsert", tb.getObject());
                Assert.assertEquals(result, 1);

                tb.set("field2", 20);
                tbMapper.invoke("upsert", tb.getObject());

                ResultSet rs = DBHelper.execute(sqlSession, "select * from tb where id = 10");
                rs.first();
                Assert.assertEquals(rs.getInt("field2"), 20);
            }
        });
    }

    /**
     * 测试 upsertWithBLOBs
     */
    @Test
    public void testUpsertWithBLOBs() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/UpsertPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
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
    public void testUpsertSelective() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/UpsertPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 普通
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                tb.set("id", 20l);
                tb.set("field1", "ts1");

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsertSelective", tb.getObject());
                Assert.assertEquals(sql, "insert into tb ( id, field1 ) values ( 20, 'ts1' ) on duplicate key update field1 = 'ts1'");
                Object result = tbMapper.invoke("upsertSelective", tb.getObject());
                Assert.assertEquals(result, 1);

                // 2. blobs
                ObjectUtil TbBlobsMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbBlobsMapper")));

                ObjectUtil TbBlobsWithBLOBs = new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs");
                TbBlobsWithBLOBs.set("id", 500l);
                TbBlobsWithBLOBs.set("field1", "ts1");
                TbBlobsWithBLOBs.set("field2", "ts2");

                // sql
                sql = SqlHelper.getFormatMapperSql(TbBlobsMapper.getObject(), "upsertSelective", TbBlobsWithBLOBs.getObject());
                Assert.assertEquals(sql, "insert into tb_blobs ( id, field1, field2 ) values ( 500, 'ts1', 'ts2' ) on duplicate key update field1 = 'ts1', field2 = 'ts2'");
                result = TbBlobsMapper.invoke("upsertSelective", TbBlobsWithBLOBs.getObject());
                Assert.assertEquals(result, 1);
            }
        });
    }

    /**
     * 测试 upsertByExample
     */
    @Test
    public void testUpsertByExample() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/UpsertPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
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
                Assert.assertEquals(sql, "update tb set field1 = 'ts1', field2 = 5 WHERE ( id = '50' ) ; insert into tb (id, field1, field2) select 50, 'ts1', 5 from dual where not exists ( select 1 from tb WHERE ( id = '50' ) )");
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
    public void testUpsertByExampleWithBLOBs() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/UpsertPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
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
    public void testUpsertByExampleSelective() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/UpsertPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
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
                Assert.assertEquals(sql, "update tb set field1 = 'ts1' WHERE ( id = '100' ) ; insert into tb ( id, field1 ) select 100, 'ts1' from dual where not exists ( select 1 from tb WHERE ( id = '100' ) )");
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
                Assert.assertEquals(sql, "update tb_blobs set field1 = 'ts1', field2 = 'ts2' WHERE ( id = '60' ) ; insert into tb_blobs ( id, field1, field2 ) select 200, 'ts1', 'ts2' from dual where not exists ( select 1 from tb_blobs WHERE ( id = '60' ) )");
                TbBlobsMapper.invoke("upsertByExampleSelective", TbBlobsWithBLOBs.getObject(), TbBlobsExample.getObject());
            }
        });
    }
}
