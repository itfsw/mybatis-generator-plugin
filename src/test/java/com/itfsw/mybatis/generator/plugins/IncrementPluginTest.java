/*
 * Copyright (c) 2019.
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

import java.lang.reflect.Array;
import java.sql.ResultSet;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/7/5 15:32
 * ---------------------------------------------------------------------------
 */
public class IncrementPluginTest {
    /**
     * 初始化
     * @throws Exception
     */
    @BeforeClass
    public static void init() throws Exception {
        DBHelper.createDB("scripts/IncrementPlugin/init.sql");
    }

    /**
     * 测试没有配置ModelColumnPlugin
     */
    @Test
    public void testWarningsWithoutModelColumnPlugin() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/IncrementPlugin/mybatis-generator-without-model-column-plugin.xml");
        tool.generate();

        Assert.assertEquals(tool.getWarnings().get(0), "itfsw:插件com.itfsw.mybatis.generator.plugins.IncrementPlugin插件需配合com.itfsw.mybatis.generator.plugins.ModelColumnPlugin插件使用！");
    }

    /**
     * 测试生成的sql和具体执行
     */
    @Test
    public void testSqlAndExecute() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/IncrementPlugin/mybatis-generator.xml");
        tool.generate(() -> DBHelper.createDB("scripts/IncrementPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {

                // 1. 测试updateByExample、updateByExampleSelective
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 3l);

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                ObjectUtil tbColumnIncF1 = new ObjectUtil(loader, packagz + ".Tb$Column#incF1");
                tb.invoke("increment", tbColumnIncF1.invoke("inc", 100L));

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "updateByExample", tb.getObject(), tbExample.getObject());
                Assert.assertEquals(sql, "update tb set id = null, field1 = 'null', inc_f1 = inc_f1 + 100, inc_f2 = null, inc_f3 = null WHERE ( id = '3' )");
                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "updateByExampleSelective", tb.getObject(), tbExample.getObject());
                Assert.assertEquals(sql, "update tb SET inc_f1 = inc_f1 + 100 WHERE ( id = '3' )");
                // 执行
                // inc_f1 增加100
                Object result = tbMapper.invoke("updateByExampleSelective", tb.getObject(), tbExample.getObject());
                Assert.assertEquals(result, 1);
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select inc_f1 from tb where id = 3");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 103);

                // inc_f1 再减去50
                tb.invoke("increment", tbColumnIncF1.invoke("dec", 50L));
                result = tbMapper.invoke("updateByExampleSelective", tb.getObject(), tbExample.getObject());
                Assert.assertEquals(result, 1);
                // 验证执行结果
                rs = DBHelper.execute(sqlSession.getConnection(), "select inc_f1 from tb where id = 3");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 53);


                // 2. 测试updateByPrimaryKey、updateByPrimaryKeySelective
                ObjectUtil tbKeysMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbKeysMapper")));

                ObjectUtil tbKeysColumnIncF1 = new ObjectUtil(loader, packagz + ".TbKeys$Column#incF1");
                ObjectUtil tbKeysColumnIncF3 = new ObjectUtil(loader, packagz + ".TbKeys$Column#incF3");

                ObjectUtil tbKeys = new ObjectUtil(loader, packagz + ".TbKeys");
                tbKeys.set("key1", 1l);
                tbKeys.set("key2", "k1");
                tbKeys.invoke("increment", tbKeysColumnIncF1.invoke("inc", 10L));
                tbKeys.invoke("increment", tbKeysColumnIncF3.invoke("inc", 30L));

                // sql
                sql = SqlHelper.getFormatMapperSql(tbKeysMapper.getObject(), "updateByPrimaryKey", tbKeys.getObject());
                Assert.assertEquals(sql, "update tb_keys set field1 = 'null', field2 = null, inc_f1 = inc_f1 + 10, inc_f2 = null, inc_f3 = inc_f3 + 30 where key1 = 1 and key2 = 'k1'");
                sql = SqlHelper.getFormatMapperSql(tbKeysMapper.getObject(), "updateByPrimaryKeySelective", tbKeys.getObject());
                Assert.assertEquals(sql, "update tb_keys SET inc_f1 = inc_f1 + 10, inc_f3 = inc_f3 + 30 where key1 = 1 and key2 = 'k1'");
                // 执行
                result = tbKeysMapper.invoke("updateByPrimaryKeySelective", tbKeys.getObject());
                Assert.assertEquals(result, 1);
                // 验证执行结果
                rs = DBHelper.execute(sqlSession.getConnection(), "select inc_f1, inc_f3 from tb_keys where key1 = 1 and key2 = 'k1'");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 11);
                Assert.assertEquals(rs.getInt("inc_f3"), 33);

                // 3. 测试updateByExampleWithBLOBs、updateByPrimaryKeyWithBLOBs
                ObjectUtil tbBlobsMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbBlobsMapper")));

                ObjectUtil tbBlobsExample = new ObjectUtil(loader, packagz + ".TbBlobsExample");
                ObjectUtil tbBlobsExampleCriteria = new ObjectUtil(tbBlobsExample.invoke("createCriteria"));
                tbBlobsExampleCriteria.invoke("andIdEqualTo", 3l);

                ObjectUtil tbBlobsWithBLOBs = new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs");
                ObjectUtil tbBlobsWithBLOBsIncF1 = new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs$Column#incF1");
                tbBlobsWithBLOBs.invoke("increment", tbBlobsWithBLOBsIncF1.invoke("inc", 100L));

                tbBlobsWithBLOBs.set("incF2", 50L);
                tbBlobsWithBLOBs.set("field3", "blob");

                // sql
                sql = SqlHelper.getFormatMapperSql(tbBlobsMapper.getObject(), "updateByExampleWithBLOBs", tbBlobsWithBLOBs.getObject(), tbBlobsExample.getObject());
                Assert.assertEquals(sql, "update tb_blobs set id = null, field1 = 'null', inc_f1 = inc_f1 + 100, inc_f2 = 50, inc_f3 = null, field2 = 'null', field3 = 'blob' WHERE ( id = '3' )");

                tbBlobsWithBLOBs.set("id", 3l);
                sql = SqlHelper.getFormatMapperSql(tbBlobsMapper.getObject(), "updateByPrimaryKeyWithBLOBs", tbBlobsWithBLOBs.getObject());
                Assert.assertEquals(sql, "update tb_blobs set field1 = 'null', inc_f1 = inc_f1 + 100, inc_f2 = 50, inc_f3 = null, field2 = 'null', field3 = 'blob' where id = 3");

                // 执行
                tbBlobsWithBLOBs.set("incF3", 10l);
                // 测试自增字段没有配置自增参数
                sql = SqlHelper.getFormatMapperSql(tbBlobsMapper.getObject(), "updateByPrimaryKeyWithBLOBs", tbBlobsWithBLOBs.getObject());
                Assert.assertEquals(sql, "update tb_blobs set field1 = 'null', inc_f1 = inc_f1 + 100, inc_f2 = 50, inc_f3 = 10, field2 = 'null', field3 = 'blob' where id = 3");
                result = tbBlobsMapper.invoke("updateByPrimaryKeyWithBLOBs", tbBlobsWithBLOBs.getObject());
                Assert.assertEquals(result, 1);
            }
        });
    }

    /**
     * 测试整合 SelectiveEnhancedPlugin 插件
     */
    @Test
    public void testWithSelectiveEnhancedPlugin() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/IncrementPlugin/mybatis-generator-with-selective-enhanced-plugin.xml");
        tool.generate(() -> DBHelper.createDB("scripts/IncrementPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 测试updateByExampleSelective
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 3l);

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                ObjectUtil tbColumnIncF1 = new ObjectUtil(loader, packagz + ".Tb$Column#incF1");

                tb.invoke("increment", tbColumnIncF1.invoke("inc", 100L));
                tb.set("incF2", 200l);

                // selective
                ObjectUtil TbColumnField1 = new ObjectUtil(loader, packagz + ".Tb$Column#field1");
                ObjectUtil TbColumnIncF2 = new ObjectUtil(loader, packagz + ".Tb$Column#incF2");
                Object columns = Array.newInstance(TbColumnField1.getCls(), 3);
                Array.set(columns, 0, TbColumnField1.getObject());
                Array.set(columns, 1, tbColumnIncF1.getObject());
                Array.set(columns, 2, TbColumnIncF2.getObject());

                // sql
                // 非空判断
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "updateByExampleSelective", tb.getObject(), tbExample.getObject());
                Assert.assertEquals(sql, "update tb SET inc_f1 = inc_f1 + 100, inc_f2 = 200 WHERE ( id = '3' )");
                // selective 指定
                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "updateByExampleSelective", tb.getObject(), tbExample.getObject(), columns);
                Assert.assertEquals(sql, "update tb SET field1 = 'null' , inc_f1 = inc_f1 + 100 , inc_f2 = 200 WHERE ( id = '3' )");
                // 执行
                // inc_f1 增加100
                Object result = tbMapper.invoke("updateByExampleSelective", tb.getObject(), tbExample.getObject(), columns);
                Assert.assertEquals(result, 1);
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select inc_f1 from tb where id = 3");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 103);

                // inc_f1 再减去50
                tb.invoke("increment", tbColumnIncF1.invoke("dec", 50L));
                result = tbMapper.invoke("updateByExampleSelective", tb.getObject(), tbExample.getObject(), Array.newInstance(TbColumnField1.getCls(), 0));
                Assert.assertEquals(result, 1);
                // 验证执行结果
                rs = DBHelper.execute(sqlSession.getConnection(), "select inc_f1 from tb where id = 3");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 53);


                // 2. 测试updateByPrimaryKeySelective
                ObjectUtil tbKeysMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbKeysMapper")));


                ObjectUtil tbKeys = new ObjectUtil(loader, packagz + ".TbKeys");
                ObjectUtil tbKeysColumnIncF1 = new ObjectUtil(loader, packagz + ".TbKeys$Column#incF1");
                ObjectUtil tbKeysColumnIncF3 = new ObjectUtil(loader, packagz + ".TbKeys$Column#incF3");
                tbKeys.set("key1", 1l);
                tbKeys.set("key2", "k1");
                tbKeys.invoke("increment", tbKeysColumnIncF1.invoke("inc", 10L));
                tbKeys.invoke("increment", tbKeysColumnIncF3.invoke("inc", 30L));

                // selective
                ObjectUtil TbColumnKey1 = new ObjectUtil(loader, packagz + ".TbKeys$Column#key1");
                tbColumnIncF1 = new ObjectUtil(loader, packagz + ".TbKeys$Column#incF1");
                columns = Array.newInstance(TbColumnKey1.getCls(), 2);
                Array.set(columns, 0, TbColumnKey1.getObject());
                Array.set(columns, 1, tbColumnIncF1.getObject());

                // sql
                // 非空判断
                sql = SqlHelper.getFormatMapperSql(tbKeysMapper.getObject(), "updateByPrimaryKeySelective", tbKeys.getObject());
                Assert.assertEquals(sql, "update tb_keys SET inc_f1 = inc_f1 + 10, inc_f3 = inc_f3 + 30 where key1 = 1 and key2 = 'k1'");
                // selective 指定
                sql = SqlHelper.getFormatMapperSql(tbKeysMapper.getObject(), "updateByPrimaryKeySelective", tbKeys.getObject(), columns);
                Assert.assertEquals(sql, "update tb_keys SET key1 = 1 , inc_f1 = inc_f1 + 10 where key1 = 1 and key2 = 'k1'");
                // 执行
                result = tbKeysMapper.invoke("updateByPrimaryKeySelective", tbKeys.getObject(), columns);
                Assert.assertEquals(result, 1);
                // 验证执行结果
                rs = DBHelper.execute(sqlSession.getConnection(), "select inc_f1, inc_f3 from tb_keys where key1 = 1 and key2 = 'k1'");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 11);
            }
        });
    }

    /**
     * 测试整合 UpsertPlugin
     */
    @Test
    public void testWithUpsertPlugin() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/IncrementPlugin/mybatis-generator-with-upsert-plugin.xml");
        tool.generate(() -> DBHelper.createDB("scripts/IncrementPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                ObjectUtil tbColumnIncF1 = new ObjectUtil(loader, packagz + ".Tb$Column#incF1");
                tb.set("id", 10L);
                tb.set("field1", "ts1");
                tb.set("incF1", 10L);
                tb.set("incF2", 1L);
                tb.set("incF3", 1L);
                tb.invoke("increment", tbColumnIncF1.invoke("inc", 10L));

                // --------------------------- upsert ---------------------------------
                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsert", tb.getObject());
                Assert.assertEquals(sql, "insert into tb (id, field1, inc_f1, inc_f2, inc_f3) values (10, 'ts1', 10, 1, 1) on duplicate key update id = 10, field1 = 'ts1', inc_f1 = inc_f1 + 10 , inc_f2 = 1, inc_f3 = 1");
                Object result = tbMapper.invoke("upsert", tb.getObject());
                Assert.assertEquals(result, 1);
                // 再次执行触发update
                tb.invoke("increment", tbColumnIncF1.invoke("inc", 10L));
                tbMapper.invoke("upsert", tb.getObject());
                ResultSet rs = DBHelper.execute(sqlSession, "select * from tb where id = 10");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 20);

                // --------------------------- upsertByExample ---------------------------------
                tb.set("field1", "ts2");
                tb.set("id", 20L);

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andField1EqualTo", "ts2");

                // sql
                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsertByExample", tb.getObject(), tbExample.getObject());
                Assert.assertEquals(sql, "update tb set id = 20, field1 = 'ts2', inc_f1 = inc_f1 + 10 , inc_f2 = 1, inc_f3 = 1 WHERE ( field1 = 'ts2' ) ; insert into tb (id, field1, inc_f1, inc_f2, inc_f3) select 20, 'ts2', 10, 1, 1 from dual where not exists ( select 1 from tb WHERE ( field1 = 'ts2' ) )");
                tbMapper.invoke("upsertByExample", tb.getObject(), tbExample.getObject());

                // 再次执行触发update
                tb.invoke("increment", tbColumnIncF1.invoke("inc", 10L));
                tbMapper.invoke("upsertByExample", tb.getObject(), tbExample.getObject());
                rs = DBHelper.execute(sqlSession, "select * from tb where id = 20");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 20);

                // --------------------------- upsertSelective ---------------------------------
                tb.set("incF3", null);
                tb.set("id", 30L);

                // sql
                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsertSelective", tb.getObject());
                Assert.assertEquals(sql, "insert into tb ( id, field1, inc_f1, inc_f2 ) values ( 30, 'ts2', 10, 1 ) on duplicate key update id = 30, field1 = 'ts2', inc_f1 = inc_f1 + 10, inc_f2 = 1");
                result = tbMapper.invoke("upsertSelective", tb.getObject());
                Assert.assertEquals(result, 1);
                // 再次执行触发update
                tb.invoke("increment", tbColumnIncF1.invoke("inc", 10L));
                result = tbMapper.invoke("upsertSelective", tb.getObject());
                Assert.assertEquals(result, 2);
                rs = DBHelper.execute(sqlSession, "select * from tb where id = 30");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 20);

                // --------------------------- upsertByExampleSelective ---------------------------------
                tb.set("field1", "ts3");
                tb.set("id", 40L);
                tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andField1EqualTo", "ts3");

                // sql
                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsertByExampleSelective", tb.getObject(), tbExample.getObject());
                Assert.assertEquals(sql, "update tb set id = 40, field1 = 'ts3', inc_f1 = inc_f1 + 10, inc_f2 = 1 WHERE ( field1 = 'ts3' ) ; insert into tb ( id, field1, inc_f1, inc_f2 ) select 40, 'ts3', 10, 1 from dual where not exists ( select 1 from tb WHERE ( field1 = 'ts3' ) )");
                result = tbMapper.invoke("upsertByExampleSelective", tb.getObject(), tbExample.getObject());
                Assert.assertEquals(result, 0);
                // 再次执行触发update
                tb.invoke("increment", tbColumnIncF1.invoke("inc", 10L));
                result = tbMapper.invoke("upsertByExampleSelective", tb.getObject(), tbExample.getObject());
                Assert.assertEquals(result, 1);
                rs = DBHelper.execute(sqlSession, "select * from tb where id = 40");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 20);
            }
        });
    }

    /**
     * 测试 autoDelimitKeywords
     */
    @Test
    public void testWithAutoDelimitKeywords() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/IncrementPlugin/mybatis-generator-with-autoDelimitKeywords.xml");
        tool.generate(() -> DBHelper.createDB("scripts/IncrementPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 测试updateByExample、updateByExampleSelective
                ObjectUtil tbKeyWordMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbKeyWordMapper")));

                ObjectUtil TbKeyWordExample = new ObjectUtil(loader, packagz + ".TbKeyWordExample");
                ObjectUtil criteria = new ObjectUtil(TbKeyWordExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 1l);

                ObjectUtil tbKeyWord = new ObjectUtil(loader, packagz + ".TbKeyWord");
                ObjectUtil tbKeyWordColumnUpdate = new ObjectUtil(loader, packagz + ".TbKeyWord$Column#update");
                tbKeyWord.invoke("increment", tbKeyWordColumnUpdate.invoke("inc", 100L));

                // 执行
                // inc_f1 增加100
                Object result = tbKeyWordMapper.invoke("updateByExampleSelective", tbKeyWord.getObject(), TbKeyWordExample.getObject());
                Assert.assertEquals(result, 1);
                // 验证更新
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select * from tb_key_word where id = 1");
                rs.first();
                Assert.assertEquals(rs.getLong("update"), 101);
            }
        });
    }

    /**
     * 测试同时整合 UpsertPlugin 和 SelectiveEnhancedPlugin
     */
    @Test
    public void testWithUpsertAndSelectiveEnhancedPlugin() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/IncrementPlugin/mybatis-generator-with-upsert-and-selective-enhanced-plugin.xml");

        // upsertSelective 基于原生非空判断
        tool.generate(() -> DBHelper.createDB("scripts/IncrementPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                ObjectUtil tbColumnIncF1 = new ObjectUtil(loader, packagz + ".Tb$Column#incF1");

                tb.set("id", 10L);
                tb.set("field1", "ts1");
                tb.set("incF1", 10L);
                tb.invoke("increment", tbColumnIncF1.invoke("inc", 10L));

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsertSelective", tb.getObject());
                Assert.assertEquals(sql, "insert into tb ( id, field1, inc_f1 ) values ( 10, 'ts1', 10 ) on duplicate key update id = 10, field1 = 'ts1', inc_f1 = inc_f1 + 10");
                Object result = tbMapper.invoke("upsertSelective", tb.getObject(), Array.newInstance(new ObjectUtil(loader, packagz + ".Tb$Column#field1").getCls(), 0));
                Assert.assertEquals(result, 1);
                // 再次执行触发update
                tb.invoke("increment", tbColumnIncF1.invoke("inc", 10L));
                tbMapper.invoke("upsertSelective", tb.getObject(), Array.newInstance(new ObjectUtil(loader, packagz + ".Tb$Column#field1").getCls(), 0));
                ResultSet rs = DBHelper.execute(sqlSession, "select * from tb where id = 10");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 20);
            }
        });

        // upsertByExampleSelective 基于原生非空判断
        tool.generate(() -> DBHelper.createDB("scripts/IncrementPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 测试updateByExampleSelective
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andField1EqualTo", "ts123");

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                ObjectUtil tbColumnIncF1 = new ObjectUtil(loader, packagz + ".Tb$Column#incF1");

                tb.set("id", 11L);
                tb.set("field1", "ts123");
                tb.set("incF1", 100L);
                tb.invoke("increment", tbColumnIncF1.invoke("inc", 100L));

                // sql
                // 非空判断
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsertByExampleSelective", tb.getObject(), tbExample.getObject());
                Assert.assertEquals(sql, "update tb set id = 11, field1 = 'ts123', inc_f1 = inc_f1 + 100 WHERE ( field1 = 'ts123' ) ; insert into tb ( id, field1, inc_f1 ) select 11, 'ts123', 100 from dual where not exists ( select 1 from tb WHERE ( field1 = 'ts123' ) )");
                // 执行
                Object result = tbMapper.invoke("upsertByExampleSelective", tb.getObject(), tbExample.getObject(), Array.newInstance(new ObjectUtil(loader, packagz + ".Tb$Column#field1").getCls(), 0));
                Assert.assertEquals(result, 0);
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select inc_f1 from tb where id = 11");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 100);


                tb.invoke("increment", tbColumnIncF1.invoke("dec", 50L));
                result = tbMapper.invoke("upsertByExampleSelective", tb.getObject(), tbExample.getObject(), Array.newInstance(new ObjectUtil(loader, packagz + ".Tb$Column#field1").getCls(), 0));
                System.out.println("kks" + SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsertByExampleSelective", tb.getObject(), tbExample.getObject(), Array.newInstance(new ObjectUtil(loader, packagz + ".Tb$Column#field1").getCls(), 0)));

                Assert.assertEquals(result, 1);
                // 验证执行结果
                rs = DBHelper.execute(sqlSession.getConnection(), "select inc_f1 from tb where id = 11");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 50);
            }
        });

        // upsertSelective 基于指定字段
        tool.generate(() -> DBHelper.createDB("scripts/IncrementPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                ObjectUtil tbColumnIncF1 = new ObjectUtil(loader, packagz + ".Tb$Column#incF1");

                tb.set("id", 20L);
                tb.set("field1", "ts1");
                tb.set("incF1", 20L);
                tb.invoke("increment", tbColumnIncF1.invoke("inc", 20L));

                ObjectUtil TbColumnId = new ObjectUtil(loader, packagz + ".Tb$Column#id");
                ObjectUtil TbColumnField1 = new ObjectUtil(loader, packagz + ".Tb$Column#field1");
                ObjectUtil TbColumnIncF2 = new ObjectUtil(loader, packagz + ".Tb$Column#incF2");
                Object columns = Array.newInstance(TbColumnField1.getCls(), 4);
                Array.set(columns, 0, TbColumnId.getObject());
                Array.set(columns, 1, TbColumnField1.getObject());
                Array.set(columns, 2, tbColumnIncF1.getObject());
                Array.set(columns, 3, TbColumnIncF2.getObject());

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsertSelective", tb.getObject(), columns);
                Assert.assertEquals(sql, "insert into tb ( id , field1 , inc_f1 , inc_f2 ) values ( 20 , 'ts1' , 20 , null ) on duplicate key update id = 20 , field1 = 'ts1' , inc_f1 = inc_f1 + 20 , inc_f2 = null");
                Object result = tbMapper.invoke("upsertSelective", tb.getObject(), columns);
                Assert.assertEquals(result, 1);
                // 再次执行触发update
                tb.invoke("increment", tbColumnIncF1.invoke("inc", 20L));
                result = tbMapper.invoke("upsertSelective", tb.getObject(), columns);
                Assert.assertEquals(result, 2);
                ResultSet rs = DBHelper.execute(sqlSession, "select * from tb where id = 20");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 40);
            }
        });

        // upsertByExampleSelective 基于指定字段
        tool.generate(() -> DBHelper.createDB("scripts/IncrementPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 测试updateByExampleSelective
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andField1EqualTo", "ts123");

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                ObjectUtil tbColumnIncF1 = new ObjectUtil(loader, packagz + ".Tb$Column#incF1");

                tb.set("id", 11L);
                tb.set("field1", "ts123");
                tb.set("incF1", 100L);
                tb.invoke("increment", tbColumnIncF1.invoke("dec", 100L));

                ObjectUtil TbColumnId = new ObjectUtil(loader, packagz + ".Tb$Column#id");
                ObjectUtil TbColumnField1 = new ObjectUtil(loader, packagz + ".Tb$Column#field1");
                ObjectUtil TbColumnIncF2 = new ObjectUtil(loader, packagz + ".Tb$Column#incF2");
                Object columns = Array.newInstance(TbColumnField1.getCls(), 4);
                Array.set(columns, 0, TbColumnId.getObject());
                Array.set(columns, 1, TbColumnField1.getObject());
                Array.set(columns, 2, tbColumnIncF1.getObject());
                Array.set(columns, 3, TbColumnIncF2.getObject());

                // sql
                // 非空判断
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsertByExampleSelective", tb.getObject(), tbExample.getObject(), columns);
                Assert.assertEquals(sql, "update tb set id = 11 , field1 = 'ts123' , inc_f1 = inc_f1 - 100 , inc_f2 = null WHERE ( field1 = 'ts123' ) ; insert into tb ( id , field1 , inc_f1 , inc_f2 ) select 11 , 'ts123' , 100 , null from dual where not exists ( select 1 from tb WHERE ( field1 = 'ts123' ) )");
                // 执行
                Object result = tbMapper.invoke("upsertByExampleSelective", tb.getObject(), tbExample.getObject(), columns);
                Assert.assertEquals(result, 0);
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select inc_f1 from tb where id = 11");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 100);


                tb.invoke("increment", tbColumnIncF1.invoke("dec", 50L));
                result = tbMapper.invoke("upsertByExampleSelective", tb.getObject(), tbExample.getObject(), columns);
                Assert.assertEquals(result, 1);
                // 验证执行结果
                rs = DBHelper.execute(sqlSession.getConnection(), "select inc_f1 from tb where id = 11");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 50);
            }
        });
    }

    /**
     * 测试同时整合 LombokPlugin(老IncrementsPlugin遗留测试)
     */
    @Test
    public void testWithLombokPlugin() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/IncrementPlugin/mybatis-generator-with-LombokPlugin.xml");

        tool.generate(() -> DBHelper.createDB("scripts/IncrementPlugin/init-lombok.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {

                // ------------------------------------- 测试 sql 执行 ----------------------------------------
                // 1. 测试updateByExample、updateByExampleSelective
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 3l);

                ObjectUtil tbBuilder = new ObjectUtil(loader.loadClass(packagz + ".Tb").getMethod("builder").invoke(null));
                ObjectUtil tbColumnField2 = new ObjectUtil(loader, packagz + ".Tb$Column#field2");
                ObjectUtil tb = new ObjectUtil(tbBuilder.invoke("build"));

                tb.invoke("increment", tbColumnField2.invoke("inc", 100));

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "updateByExample", tb.getObject(), tbExample.getObject());
                Assert.assertEquals(sql, "update tb set id = null, field1 = 'null', field2 = field2 + 100 WHERE ( id = '3' )");
                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "updateByExampleSelective", tb.getObject(), tbExample.getObject());
                Assert.assertEquals(sql, "update tb SET field2 = field2 + 100 WHERE ( id = '3' )");
                // 执行
                // inc_f1 增加100
                Object result = tbMapper.invoke("updateByExampleSelective", tb.getObject(), tbExample.getObject());
                Assert.assertEquals(result, 1);
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select field2 from tb where id = 3");
                rs.first();
                Assert.assertEquals(rs.getInt("field2"), 103);

                // 2. 测试有SuperBuilder的情况
                ObjectUtil tbLombokMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbLombokMapper")));

                ObjectUtil tbLombokExample = new ObjectUtil(loader, packagz + ".TbLombokExample");
                criteria = new ObjectUtil(tbLombokExample.invoke("createCriteria"));
                criteria.invoke("andKey1EqualTo", "key1");


                ObjectUtil tbLombokWithBLOBsBuilder = new ObjectUtil(loader.loadClass(packagz + ".TbLombokWithBLOBs").getMethod("builder").invoke(null));
                tbLombokWithBLOBsBuilder.invoke("field3", "f3");
                tbLombokWithBLOBsBuilder.invoke("field1", "ts33");
                tbLombokWithBLOBsBuilder.invoke("key1", "key100");

                ObjectUtil tbLombokWithBLOBs = new ObjectUtil(tbLombokWithBLOBsBuilder.invoke("build"));

                ObjectUtil tbLombokWithBLOBsColumnIncF1 = new ObjectUtil(loader, packagz + ".TbLombokWithBLOBs$Column#incF1");
                tbLombokWithBLOBs.invoke("increment", tbLombokWithBLOBsColumnIncF1.invoke("inc", (short) 1));
                ObjectUtil tbLombokWithBLOBsColumnId = new ObjectUtil(loader, packagz + ".TbLombokWithBLOBs$Column#id");
                tbLombokWithBLOBs.invoke("increment", tbLombokWithBLOBsColumnId.invoke("dec", 100L));

                // sql
                sql = SqlHelper.getFormatMapperSql(tbLombokMapper.getObject(), "updateByExampleSelective", tbLombokWithBLOBs.getObject(), tbLombokExample.getObject());
                Assert.assertEquals(sql, "update tb_lombok SET id = id - 100, key1 = 'key100', field1 = 'ts33', inc_f1 = inc_f1 + 1, field3 = 'f3' WHERE ( key1 = 'key1' )");
                // 执行
                result = tbLombokMapper.invoke("updateByExampleSelective", tbLombokWithBLOBs.getObject(), tbLombokExample.getObject());
                Assert.assertEquals(result, 1);
                rs = DBHelper.execute(sqlSession.getConnection(), "select * from tb_lombok where key1 = 'key100'");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 1);
                Assert.assertEquals(rs.getInt("id"), -99);

            }
        });
    }
}
