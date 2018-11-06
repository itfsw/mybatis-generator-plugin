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

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/7/5 15:32
 * ---------------------------------------------------------------------------
 */
public class IncrementsPluginTest {
    /**
     * 初始化
     * @throws Exception
     */
    @BeforeClass
    public static void init() throws Exception {
        DBHelper.createDB("scripts/IncrementsPlugin/init.sql");
    }

    /**
     * 测试没有配置ModelBuilderPlugin
     */
    @Test
    public void testWarningsWithoutModelBuilderPlugin() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/IncrementsPlugin/mybatis-generator-without-model-builder-plugin.xml");
        tool.generate();

        Assert.assertEquals(tool.getWarnings().get(0), "itfsw:插件com.itfsw.mybatis.generator.plugins.IncrementsPlugin插件需配合com.itfsw.mybatis.generator.plugins.ModelBuilderPlugin或者com.itfsw.mybatis.generator.plugins.LombokPlugin插件使用！");
    }

    /**
     * 测试ModelBuilder是否按配置正常生成了对应方法
     */
    @Test
    public void testModelBuilderMethod() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/IncrementsPlugin/mybatis-generator.xml");
        tool.generate(() -> DBHelper.createDB("scripts/IncrementsPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 测试生成的方法
                ObjectUtil tbBuilder = new ObjectUtil(loader, packagz + ".Tb$Builder");
                List<Method> methods = getDeclaredMethods(tbBuilder.getCls(), "incF1");
                Assert.assertEquals(methods.size(), 2);
                // 自增方法
                Method incMethod = methods.get(0).getParameterTypes().length == 1 ? methods.get(1) : methods.get(0);
                Assert.assertEquals(incMethod.getParameters()[1].getParameterizedType().getTypeName(), packagz + ".Tb$Builder$Inc");

                // 2. 测试有空格
                ObjectUtil tbKeysBuilder = new ObjectUtil(loader, packagz + ".TbKeys$Builder");
                Assert.assertEquals(getDeclaredMethods(tbKeysBuilder.getCls(), "incF1").size(), 2);
                Assert.assertEquals(getDeclaredMethods(tbKeysBuilder.getCls(), "incF2").size(), 2);
                Assert.assertEquals(getDeclaredMethods(tbKeysBuilder.getCls(), "incF3").size(), 2);

                // 3. 测试在WithBlobs正确生成
                ObjectUtil tbBlobsWithBLOBs = new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs$Builder");
                Assert.assertEquals(getDeclaredMethods(tbBlobsWithBLOBs.getCls(), "incF1").size(), 2);
                Assert.assertEquals(getDeclaredMethods(tbBlobsWithBLOBs.getCls(), "incF2").size(), 1);
                Assert.assertEquals(getDeclaredMethods(tbBlobsWithBLOBs.getCls(), "incF3").size(), 2);
            }
        });
    }

    /**
     * 获取类方法
     * @param cls
     * @param name
     * @return
     */
    private List<Method> getDeclaredMethods(Class cls, String name) {
        List<Method> list = new ArrayList<>();
        Method[] methods = cls.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(name) && cls.equals(method.getReturnType())) {
                list.add(method);
            }
        }
        return list;
    }

    /**
     * 测试生成的sql和具体执行
     */
    @Test
    public void testSqlAndExecute() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/IncrementsPlugin/mybatis-generator.xml");
        tool.generate(() -> DBHelper.createDB("scripts/IncrementsPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {

                // 1. 测试updateByExample、updateByExampleSelective
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 3l);

                ObjectUtil tbBuilder = new ObjectUtil(loader, packagz + ".Tb$Builder");
                ObjectUtil tbBuilderInc = new ObjectUtil(loader, packagz + ".Tb$Builder$Inc#INC");
                tbBuilder.invoke("incF1", 100l, tbBuilderInc.getObject());

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "updateByExample", tbBuilder.invoke("build"), tbExample.getObject());
                Assert.assertEquals(sql, "update tb set id = null, field1 = 'null', inc_f1 = inc_f1 + 100 , inc_f2 = null, inc_f3 = null WHERE ( id = '3' )");
                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "updateByExampleSelective", tbBuilder.invoke("build"), tbExample.getObject());
                Assert.assertEquals(sql, "update tb SET inc_f1 = inc_f1 + 100 WHERE ( id = '3' )");
                // 执行
                // inc_f1 增加100
                Object result = tbMapper.invoke("updateByExampleSelective", tbBuilder.invoke("build"), tbExample.getObject());
                Assert.assertEquals(result, 1);
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select inc_f1 from tb where id = 3");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 103);

                // inc_f1 再减去50
                ObjectUtil tbBuilderDec = new ObjectUtil(loader, packagz + ".Tb$Builder$Inc#DEC");
                tbBuilder.invoke("incF1", 50l, tbBuilderDec.getObject());
                result = tbMapper.invoke("updateByExampleSelective", tbBuilder.invoke("build"), tbExample.getObject());
                Assert.assertEquals(result, 1);
                // 验证执行结果
                rs = DBHelper.execute(sqlSession.getConnection(), "select inc_f1 from tb where id = 3");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 53);


                // 2. 测试updateByPrimaryKey、updateByPrimaryKeySelective
                ObjectUtil tbKeysMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbKeysMapper")));

                ObjectUtil tbKeysBuilderInc = new ObjectUtil(loader, packagz + ".TbKeysKey$Builder$Inc#INC");

                ObjectUtil tbKeysBuilder = new ObjectUtil(loader, packagz + ".TbKeys$Builder");
                tbKeysBuilder.invoke("key1", 1l);
                tbKeysBuilder.invoke("key2", "k1");
                tbKeysBuilder.invoke("incF1", 10l, tbKeysBuilderInc.getObject());
                tbKeysBuilder.invoke("incF3", 30l, tbKeysBuilderInc.getObject());

                // sql
                sql = SqlHelper.getFormatMapperSql(tbKeysMapper.getObject(), "updateByPrimaryKey", tbKeysBuilder.invoke("build"));
                Assert.assertEquals(sql, "update tb_keys set field1 = 'null', field2 = null, inc_f1 = inc_f1 + 10 , inc_f2 = null , inc_f3 = inc_f3 + 30 where key1 = 1 and key2 = 'k1'");
                sql = SqlHelper.getFormatMapperSql(tbKeysMapper.getObject(), "updateByPrimaryKeySelective", tbKeysBuilder.invoke("build"));
                Assert.assertEquals(sql, "update tb_keys SET inc_f1 = inc_f1 + 10 , inc_f3 = inc_f3 + 30 where key1 = 1 and key2 = 'k1'");
                // 执行
                result = tbKeysMapper.invoke("updateByPrimaryKeySelective", tbKeysBuilder.invoke("build"));
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

                ObjectUtil tbBlobsWithBLOBsBuilder = new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs$Builder");
                ObjectUtil tbBlobsBuilderInc = new ObjectUtil(loader, packagz + ".TbBlobs$Builder$Inc#INC");
                tbBlobsWithBLOBsBuilder.invoke("incF1", 100l, tbBlobsBuilderInc.getObject());
                tbBlobsWithBLOBsBuilder.invoke("incF2", 50l);
                tbBlobsWithBLOBsBuilder.invoke("field3", "blob");

                // sql
                sql = SqlHelper.getFormatMapperSql(tbBlobsMapper.getObject(), "updateByExampleWithBLOBs", tbBlobsWithBLOBsBuilder.invoke("build"), tbBlobsExample.getObject());
                Assert.assertEquals(sql, "update tb_blobs set id = null, field1 = 'null', inc_f1 = inc_f1 + 100 , inc_f2 = 50, inc_f3 = null , field2 = 'null', field3 = 'blob' WHERE ( id = '3' )");

                tbBlobsWithBLOBsBuilder.invoke("id", 3l);
                sql = SqlHelper.getFormatMapperSql(tbBlobsMapper.getObject(), "updateByPrimaryKeyWithBLOBs", tbBlobsWithBLOBsBuilder.invoke("build"));
                Assert.assertEquals(sql, "update tb_blobs set field1 = 'null', inc_f1 = inc_f1 + 100 , inc_f2 = 50, inc_f3 = null , field2 = 'null', field3 = 'blob' where id = 3");

                // 执行
                tbBlobsWithBLOBsBuilder.invoke("incF3", 10l);
                // 测试自增字段没有配置自增参数
                sql = SqlHelper.getFormatMapperSql(tbBlobsMapper.getObject(), "updateByPrimaryKeyWithBLOBs", tbBlobsWithBLOBsBuilder.invoke("build"));
                Assert.assertEquals(sql, "update tb_blobs set field1 = 'null', inc_f1 = inc_f1 + 100 , inc_f2 = 50, inc_f3 = 10 , field2 = 'null', field3 = 'blob' where id = 3");
                result = tbBlobsMapper.invoke("updateByPrimaryKeyWithBLOBs", tbBlobsWithBLOBsBuilder.invoke("build"));
                Assert.assertEquals(result, 1);
            }
        });
    }

    /**
     * 测试整合 SelectiveEnhancedPlugin 插件
     */
    @Test
    public void testWithSelectiveEnhancedPlugin() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/IncrementsPlugin/mybatis-generator-with-selective-enhanced-plugin.xml");
        tool.generate(() -> DBHelper.createDB("scripts/IncrementsPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 测试updateByExampleSelective
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 3l);

                ObjectUtil tbBuilder = new ObjectUtil(loader, packagz + ".Tb$Builder");
                ObjectUtil tbBuilderInc = new ObjectUtil(loader, packagz + ".Tb$Builder$Inc#INC");
                tbBuilder.invoke("incF1", 100l, tbBuilderInc.getObject());
                tbBuilder.invoke("incF2", 200l);


                // selective
                ObjectUtil TbColumnField1 = new ObjectUtil(loader, packagz + ".Tb$Column#field1");
                ObjectUtil TbColumnIncF1 = new ObjectUtil(loader, packagz + ".Tb$Column#incF1");
                ObjectUtil TbColumnIncF2 = new ObjectUtil(loader, packagz + ".Tb$Column#incF2");
                Object columns = Array.newInstance(TbColumnField1.getCls(), 3);
                Array.set(columns, 0, TbColumnField1.getObject());
                Array.set(columns, 1, TbColumnIncF1.getObject());
                Array.set(columns, 2, TbColumnIncF2.getObject());

                // sql
                // 非空判断
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "updateByExampleSelective", tbBuilder.invoke("build"), tbExample.getObject());
                Assert.assertEquals(sql, "update tb SET inc_f1 = inc_f1 + 100 , inc_f2 = 200 WHERE ( id = '3' )");
                // selective 指定
                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "updateByExampleSelective", tbBuilder.invoke("build"), tbExample.getObject(), columns);
                Assert.assertEquals(sql, "update tb SET field1 = 'null' , inc_f1 = inc_f1 + 100 , inc_f2 = 200 WHERE ( id = '3' )");
                // 执行
                // inc_f1 增加100
                Object result = tbMapper.invoke("updateByExampleSelective", tbBuilder.invoke("build"), tbExample.getObject(), columns);
                Assert.assertEquals(result, 1);
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select inc_f1 from tb where id = 3");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 103);

                // inc_f1 再减去50
                ObjectUtil tbBuilderDec = new ObjectUtil(loader, packagz + ".Tb$Builder$Inc#DEC");
                tbBuilder.invoke("incF1", 50l, tbBuilderDec.getObject());
                result = tbMapper.invoke("updateByExampleSelective", tbBuilder.invoke("build"), tbExample.getObject(), Array.newInstance(TbColumnField1.getCls(), 0));
                Assert.assertEquals(result, 1);
                // 验证执行结果
                rs = DBHelper.execute(sqlSession.getConnection(), "select inc_f1 from tb where id = 3");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 53);


                // 2. 测试updateByPrimaryKeySelective
                ObjectUtil tbKeysMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbKeysMapper")));

                ObjectUtil tbKeysBuilderInc = new ObjectUtil(loader, packagz + ".TbKeysKey$Builder$Inc#INC");

                ObjectUtil tbKeysBuilder = new ObjectUtil(loader, packagz + ".TbKeys$Builder");
                tbKeysBuilder.invoke("key1", 1l);
                tbKeysBuilder.invoke("key2", "k1");
                tbKeysBuilder.invoke("incF1", 10l, tbKeysBuilderInc.getObject());
                tbKeysBuilder.invoke("incF3", 30l, tbKeysBuilderInc.getObject());

                // selective
                ObjectUtil TbColumnKey1 = new ObjectUtil(loader, packagz + ".TbKeys$Column#key1");
                TbColumnIncF1 = new ObjectUtil(loader, packagz + ".TbKeys$Column#incF1");
                columns = Array.newInstance(TbColumnKey1.getCls(), 2);
                Array.set(columns, 0, TbColumnKey1.getObject());
                Array.set(columns, 1, TbColumnIncF1.getObject());

                // sql
                // 非空判断
                sql = SqlHelper.getFormatMapperSql(tbKeysMapper.getObject(), "updateByPrimaryKeySelective", tbKeysBuilder.invoke("build"));
                Assert.assertEquals(sql, "update tb_keys SET inc_f1 = inc_f1 + 10 , inc_f3 = inc_f3 + 30 where key1 = 1 and key2 = 'k1'");
                // selective 指定
                sql = SqlHelper.getFormatMapperSql(tbKeysMapper.getObject(), "updateByPrimaryKeySelective", tbKeysBuilder.invoke("build"), columns);
                Assert.assertEquals(sql, "update tb_keys SET key1 = 1 , inc_f1 = inc_f1 + 10 where key1 = 1 and key2 = 'k1'");
                // 执行
                result = tbKeysMapper.invoke("updateByPrimaryKeySelective", tbKeysBuilder.invoke("build"), columns);
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
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/IncrementsPlugin/mybatis-generator-with-upsert-plugin.xml");
        tool.generate(() -> DBHelper.createDB("scripts/IncrementsPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbBuilder = new ObjectUtil(loader, packagz + ".Tb$Builder");
                ObjectUtil tbBuilderInc = new ObjectUtil(loader, packagz + ".Tb$Builder$Inc#INC");
                tbBuilder.invoke("id", 10L);
                tbBuilder.invoke("field1", "ts1");
                tbBuilder.invoke("incF1", 10L, tbBuilderInc.getObject());
                tbBuilder.invoke("incF2", 1L);
                tbBuilder.invoke("incF3", 1L);

                // --------------------------- upsert ---------------------------------
                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsert", tbBuilder.invoke("build"));
                Assert.assertEquals(sql, "insert into tb (id, field1, inc_f1, inc_f2, inc_f3) values (10, 'ts1', 10, 1, 1) on duplicate key update id = 10, field1 = 'ts1', inc_f1 = inc_f1 + 10 , inc_f2 = 1, inc_f3 = 1");
                Object result = tbMapper.invoke("upsert", tbBuilder.invoke("build"));
                Assert.assertEquals(result, 1);
                // 再次执行触发update
                tbBuilder.invoke("incF1", 10L, tbBuilderInc.getObject());
                tbMapper.invoke("upsert", tbBuilder.invoke("build"));
                ResultSet rs = DBHelper.execute(sqlSession, "select * from tb where id = 10");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 20);

                // --------------------------- upsertByExample ---------------------------------
                tbBuilder.invoke("field1", "ts2");
                tbBuilder.invoke("id", 20L);

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andField1EqualTo", "ts2");

                // sql
                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsertByExample", tbBuilder.invoke("build"), tbExample.getObject());
                Assert.assertEquals(sql, "update tb set id = 20, field1 = 'ts2', inc_f1 = inc_f1 + 10 , inc_f2 = 1, inc_f3 = 1 WHERE ( field1 = 'ts2' ) ; insert into tb (id, field1, inc_f1, inc_f2, inc_f3) select 20, 'ts2', 10, 1, 1 from dual where not exists ( select 1 from tb WHERE ( field1 = 'ts2' ) )");
                tbMapper.invoke("upsertByExample", tbBuilder.invoke("build"), tbExample.getObject());

                // 再次执行触发update
                tbBuilder.invoke("incF1", 10L, tbBuilderInc.getObject());
                tbMapper.invoke("upsertByExample", tbBuilder.invoke("build"), tbExample.getObject());
                rs = DBHelper.execute(sqlSession, "select * from tb where id = 20");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 20);

                // --------------------------- upsertSelective ---------------------------------
                tbBuilder.set("obj.incF3", null);
                tbBuilder.invoke("id", 30L);

                // sql
                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsertSelective", tbBuilder.invoke("build"));
                Assert.assertEquals(sql, "insert into tb ( id, field1, inc_f1, inc_f2 ) values ( 30, 'ts2', 10, 1 ) on duplicate key update id = 30, field1 = 'ts2', inc_f1 = inc_f1 + 10 , inc_f2 = 1");
                result = tbMapper.invoke("upsertSelective", tbBuilder.invoke("build"));
                Assert.assertEquals(result, 1);
                // 再次执行触发update
                tbBuilder.invoke("incF1", 10L, tbBuilderInc.getObject());
                result = tbMapper.invoke("upsertSelective", tbBuilder.invoke("build"));
                Assert.assertEquals(result, 2);
                rs = DBHelper.execute(sqlSession, "select * from tb where id = 30");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 20);

                // --------------------------- upsertByExampleSelective ---------------------------------
                tbBuilder.invoke("field1", "ts3");
                tbBuilder.invoke("id", 40L);
                tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andField1EqualTo", "ts3");

                // sql
                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsertByExampleSelective", tbBuilder.invoke("build"), tbExample.getObject());
                Assert.assertEquals(sql, "update tb set id = 40, field1 = 'ts3', inc_f1 = inc_f1 + 10 , inc_f2 = 1 WHERE ( field1 = 'ts3' ) ; insert into tb ( id, field1, inc_f1, inc_f2 ) select 40, 'ts3', 10, 1 from dual where not exists ( select 1 from tb WHERE ( field1 = 'ts3' ) )");
                result = tbMapper.invoke("upsertByExampleSelective", tbBuilder.invoke("build"), tbExample.getObject());
                Assert.assertEquals(result, 0);
                // 再次执行触发update
                tbBuilder.invoke("incF1", 10L, tbBuilderInc.getObject());
                result = tbMapper.invoke("upsertByExampleSelective", tbBuilder.invoke("build"), tbExample.getObject());
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
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/IncrementsPlugin/mybatis-generator-with-autoDelimitKeywords.xml");
        tool.generate(() -> DBHelper.createDB("scripts/IncrementsPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 测试updateByExample、updateByExampleSelective
                ObjectUtil TbKeyWord = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbKeyWordMapper")));

                ObjectUtil TbKeyWordExample = new ObjectUtil(loader, packagz + ".TbKeyWordExample");
                ObjectUtil criteria = new ObjectUtil(TbKeyWordExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 1l);

                ObjectUtil TbKeyWordBuilder = new ObjectUtil(loader, packagz + ".TbKeyWord$Builder");
                ObjectUtil TbKeyWordBuilderInc = new ObjectUtil(loader, packagz + ".TbKeyWord$Builder$Inc#INC");
                TbKeyWordBuilder.invoke("update", 100l, TbKeyWordBuilderInc.getObject());

                // 执行
                // inc_f1 增加100
                Object result = TbKeyWord.invoke("updateByExampleSelective", TbKeyWordBuilder.invoke("build"), TbKeyWordExample.getObject());
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
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/IncrementsPlugin/mybatis-generator-with-upsert-and-selective-enhanced-plugin.xml");

        // upsertSelective 基于原生非空判断
        tool.generate(() -> DBHelper.createDB("scripts/IncrementsPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbBuilder = new ObjectUtil(loader, packagz + ".Tb$Builder");
                ObjectUtil tbBuilderInc = new ObjectUtil(loader, packagz + ".Tb$Builder$Inc#INC");
                tbBuilder.invoke("id", 10L);
                tbBuilder.invoke("field1", "ts1");
                tbBuilder.invoke("incF1", 10L, tbBuilderInc.getObject());

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsertSelective", tbBuilder.invoke("build"));
                Assert.assertEquals(sql, "insert into tb ( id, field1, inc_f1 ) values ( 10, 'ts1', 10 ) on duplicate key update id = 10, field1 = 'ts1', inc_f1 = inc_f1 + 10");
                Object result = tbMapper.invoke("upsertSelective", tbBuilder.invoke("build"), Array.newInstance(new ObjectUtil(loader, packagz + ".Tb$Column#field1").getCls(), 0));
                Assert.assertEquals(result, 1);
                // 再次执行触发update
                tbBuilder.invoke("incF1", 10L, tbBuilderInc.getObject());
                tbMapper.invoke("upsertSelective", tbBuilder.invoke("build"), Array.newInstance(new ObjectUtil(loader, packagz + ".Tb$Column#field1").getCls(), 0));
                ResultSet rs = DBHelper.execute(sqlSession, "select * from tb where id = 10");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 20);
            }
        });

        // upsertByExampleSelective 基于原生非空判断
        tool.generate(() -> DBHelper.createDB("scripts/IncrementsPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 测试updateByExampleSelective
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andField1EqualTo", "ts123");

                ObjectUtil tbBuilder = new ObjectUtil(loader, packagz + ".Tb$Builder");
                ObjectUtil tbBuilderInc = new ObjectUtil(loader, packagz + ".Tb$Builder$Inc#INC");
                tbBuilder.invoke("id", 11L);
                tbBuilder.invoke("field1", "ts123");
                tbBuilder.invoke("incF1", 100L, tbBuilderInc.getObject());

                // sql
                // 非空判断
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsertByExampleSelective", tbBuilder.invoke("build"), tbExample.getObject());
                Assert.assertEquals(sql, "update tb set id = 11, field1 = 'ts123', inc_f1 = inc_f1 + 100 WHERE ( field1 = 'ts123' ) ; insert into tb ( id, field1, inc_f1 ) select 11, 'ts123', 100 from dual where not exists ( select 1 from tb WHERE ( field1 = 'ts123' ) )");
                // 执行
                Object result = tbMapper.invoke("upsertByExampleSelective", tbBuilder.invoke("build"), tbExample.getObject(), Array.newInstance(new ObjectUtil(loader, packagz + ".Tb$Column#field1").getCls(), 0));
                Assert.assertEquals(result, 0);
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select inc_f1 from tb where id = 11");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 100);


                ObjectUtil tbBuilderDec = new ObjectUtil(loader, packagz + ".Tb$Builder$Inc#INC");
                tbBuilder.invoke("incF1", 50L, tbBuilderDec.getObject());
                result = tbMapper.invoke("upsertByExampleSelective", tbBuilder.invoke("build"), tbExample.getObject(), Array.newInstance(new ObjectUtil(loader, packagz + ".Tb$Column#field1").getCls(), 0));
                Assert.assertEquals(result, 1);
                // 验证执行结果
                rs = DBHelper.execute(sqlSession.getConnection(), "select inc_f1 from tb where id = 11");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 150);
            }
        });

        // upsertSelective 基于指定字段
        tool.generate(() -> DBHelper.createDB("scripts/IncrementsPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbBuilder = new ObjectUtil(loader, packagz + ".Tb$Builder");
                ObjectUtil tbBuilderInc = new ObjectUtil(loader, packagz + ".Tb$Builder$Inc#INC");
                tbBuilder.invoke("id", 20L);
                tbBuilder.invoke("field1", "ts1");
                tbBuilder.invoke("incF1", 20L, tbBuilderInc.getObject());

                ObjectUtil TbColumnId = new ObjectUtil(loader, packagz + ".Tb$Column#id");
                ObjectUtil TbColumnField1 = new ObjectUtil(loader, packagz + ".Tb$Column#field1");
                ObjectUtil TbColumnIncF1 = new ObjectUtil(loader, packagz + ".Tb$Column#incF1");
                ObjectUtil TbColumnIncF2 = new ObjectUtil(loader, packagz + ".Tb$Column#incF2");
                Object columns = Array.newInstance(TbColumnField1.getCls(), 4);
                Array.set(columns, 0, TbColumnId.getObject());
                Array.set(columns, 1, TbColumnField1.getObject());
                Array.set(columns, 2, TbColumnIncF1.getObject());
                Array.set(columns, 3, TbColumnIncF2.getObject());

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsertSelective", tbBuilder.invoke("build"), columns);
                Assert.assertEquals(sql, "insert into tb ( id , field1 , inc_f1 , inc_f2 ) values ( 20 , 'ts1' , 20 , null ) on duplicate key update id = 20 , field1 = 'ts1' , inc_f1 = inc_f1 + 20 , inc_f2 = null");
                Object result = tbMapper.invoke("upsertSelective", tbBuilder.invoke("build"), columns);
                Assert.assertEquals(result, 1);
                // 再次执行触发update
                tbBuilder.invoke("incF1", 20L, tbBuilderInc.getObject());
                result = tbMapper.invoke("upsertSelective", tbBuilder.invoke("build"), columns);
                Assert.assertEquals(result, 2);
                ResultSet rs = DBHelper.execute(sqlSession, "select * from tb where id = 20");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 40);
            }
        });

        // upsertByExampleSelective 基于指定字段
        tool.generate(() -> DBHelper.createDB("scripts/IncrementsPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 测试updateByExampleSelective
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andField1EqualTo", "ts123");

                ObjectUtil tbBuilder = new ObjectUtil(loader, packagz + ".Tb$Builder");
                ObjectUtil tbBuilderInc = new ObjectUtil(loader, packagz + ".Tb$Builder$Inc#DEC");
                tbBuilder.invoke("id", 11L);
                tbBuilder.invoke("field1", "ts123");
                tbBuilder.invoke("incF1", 100L, tbBuilderInc.getObject());

                ObjectUtil TbColumnId = new ObjectUtil(loader, packagz + ".Tb$Column#id");
                ObjectUtil TbColumnField1 = new ObjectUtil(loader, packagz + ".Tb$Column#field1");
                ObjectUtil TbColumnIncF1 = new ObjectUtil(loader, packagz + ".Tb$Column#incF1");
                ObjectUtil TbColumnIncF2 = new ObjectUtil(loader, packagz + ".Tb$Column#incF2");
                Object columns = Array.newInstance(TbColumnField1.getCls(), 4);
                Array.set(columns, 0, TbColumnId.getObject());
                Array.set(columns, 1, TbColumnField1.getObject());
                Array.set(columns, 2, TbColumnIncF1.getObject());
                Array.set(columns, 3, TbColumnIncF2.getObject());

                // sql
                // 非空判断
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsertByExampleSelective", tbBuilder.invoke("build"), tbExample.getObject(), columns);
                Assert.assertEquals(sql, "update tb set id = 11 , field1 = 'ts123' , inc_f1 = inc_f1 - 100 , inc_f2 = null WHERE ( field1 = 'ts123' ) ; insert into tb ( id , field1 , inc_f1 , inc_f2 ) select 11 , 'ts123' , 100 , null from dual where not exists ( select 1 from tb WHERE ( field1 = 'ts123' ) )");
                // 执行
                Object result = tbMapper.invoke("upsertByExampleSelective", tbBuilder.invoke("build"), tbExample.getObject(), columns);
                Assert.assertEquals(result, 0);
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select inc_f1 from tb where id = 11");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 100);


                ObjectUtil tbBuilderDec = new ObjectUtil(loader, packagz + ".Tb$Builder$Inc#DEC");
                tbBuilder.invoke("incF1", 50L, tbBuilderDec.getObject());
                result = tbMapper.invoke("upsertByExampleSelective", tbBuilder.invoke("build"), tbExample.getObject(), columns);
                Assert.assertEquals(result, 1);
                // 验证执行结果
                rs = DBHelper.execute(sqlSession.getConnection(), "select inc_f1 from tb where id = 11");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 50);
            }
        });
    }

    /**
     * 测试同时整合 LombokPlugin
     */
    @Test
    public void testWithLombokPlugin() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/IncrementsPlugin/mybatis-generator-with-LombokPlugin.xml");

        tool.generate(() -> DBHelper.createDB("scripts/IncrementsPlugin/init-lombok.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // ------------------------------------- builder ----------------------------------------
                // normal builder
                ObjectUtil tbBuilder = new ObjectUtil(loader.loadClass(packagz + ".Tb").getMethod("builder").invoke(null));
                tbBuilder = new ObjectUtil(tbBuilder.invoke("id", 1L));
                tbBuilder.invoke("field1", "ts1");
                ObjectUtil tb = new ObjectUtil(tbBuilder.invoke("build"));
                Assert.assertEquals(tb.invoke("toString"), "Tb(id=1, field1=ts1, field2=null, incrementsColumnsInfoMap={})");
                // super
                ObjectUtil tbLombokWithBLOBsBuilder = new ObjectUtil(loader.loadClass(packagz + ".TbLombokWithBLOBs").getMethod("builder").invoke(null));
                tbLombokWithBLOBsBuilder.invoke("field3", "ts3");
                Assert.assertEquals(tbLombokWithBLOBsBuilder.invoke("toString"), "TbLombokWithBLOBs.TbLombokWithBLOBsBuilder(super=TbLombok.TbLombokBuilder(super=TbLombokKey.TbLombokKeyBuilder(id=null, key1=null, incrementsColumnsInfoMap={}), field1=null, incF1=null), field3=ts3, field4=null)");
                tbLombokWithBLOBsBuilder.invoke("field1", "ts1");
                Assert.assertEquals(tbLombokWithBLOBsBuilder.invoke("toString"), "TbLombokWithBLOBs.TbLombokWithBLOBsBuilder(super=TbLombok.TbLombokBuilder(super=TbLombokKey.TbLombokKeyBuilder(id=null, key1=null, incrementsColumnsInfoMap={}), field1=ts1, incF1=null), field3=ts3, field4=null)");
                tbLombokWithBLOBsBuilder.invoke("id", 100L);
                Assert.assertEquals(tbLombokWithBLOBsBuilder.invoke("toString"), "TbLombokWithBLOBs.TbLombokWithBLOBsBuilder(super=TbLombok.TbLombokBuilder(super=TbLombokKey.TbLombokKeyBuilder(id=100, key1=null, incrementsColumnsInfoMap={}), field1=ts1, incF1=null), field3=ts3, field4=null)");

                // ------------------------------------- 测试 sql 执行 ----------------------------------------
                // 1. 测试updateByExample、updateByExampleSelective
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 3l);

                tbBuilder = new ObjectUtil(loader.loadClass(packagz + ".Tb").getMethod("builder").invoke(null));
                ObjectUtil tbBuilderInc = new ObjectUtil(loader, packagz + ".Tb$TbBuilder$Inc#INC");
                tbBuilder.invoke("field2", 100, tbBuilderInc.getObject());

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "updateByExample", tbBuilder.invoke("build"), tbExample.getObject());
                Assert.assertEquals(sql, "update tb set id = null, field1 = 'null', field2 = field2 + 100 WHERE ( id = '3' )");
                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "updateByExampleSelective", tbBuilder.invoke("build"), tbExample.getObject());
                Assert.assertEquals(sql, "update tb SET field2 = field2 + 100 WHERE ( id = '3' )");
                // 执行
                // inc_f1 增加100
                Object result = tbMapper.invoke("updateByExampleSelective", tbBuilder.invoke("build"), tbExample.getObject());
                Assert.assertEquals(result, 1);
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select field2 from tb where id = 3");
                rs.first();
                Assert.assertEquals(rs.getInt("field2"), 103);

                // 2. 测试有SuperBuilder的情况
                ObjectUtil tbLombokMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbLombokMapper")));

                ObjectUtil tbLombokExample = new ObjectUtil(loader, packagz + ".TbLombokExample");
                criteria = new ObjectUtil(tbLombokExample.invoke("createCriteria"));
                criteria.invoke("andKey1EqualTo", "key1");


                tbLombokWithBLOBsBuilder = new ObjectUtil(loader.loadClass(packagz + ".TbLombokWithBLOBs").getMethod("builder").invoke(null));
                tbLombokWithBLOBsBuilder.invoke("field3", "f3");
                ObjectUtil tbLombokKeyBuilderIncINC = new ObjectUtil(loader, packagz + ".TbLombokKey$TbLombokKeyBuilder$Inc#INC");
                tbLombokWithBLOBsBuilder.invoke("incF1", (short)1, tbLombokKeyBuilderIncINC.getObject());
                tbLombokWithBLOBsBuilder.invoke("field1", "ts33");
                ObjectUtil tbLombokKeyBuilderIncDEC = new ObjectUtil(loader, packagz + ".TbLombokKey$TbLombokKeyBuilder$Inc#DEC");
                tbLombokWithBLOBsBuilder.invoke("id", 100L, tbLombokKeyBuilderIncDEC.getObject());
                tbLombokWithBLOBsBuilder.invoke("key1", "key100");

                // sql
                sql = SqlHelper.getFormatMapperSql(tbLombokMapper.getObject(), "updateByExampleSelective", tbLombokWithBLOBsBuilder.invoke("build"), tbLombokExample.getObject());
                Assert.assertEquals(sql, "update tb_lombok SET id = id - 100 , key1 = 'key100', field1 = 'ts33', inc_f1 = inc_f1 + 1 , field3 = 'f3' WHERE ( key1 = 'key1' )");
                // 执行
                result = tbLombokMapper.invoke("updateByExampleSelective",  tbLombokWithBLOBsBuilder.invoke("build"), tbLombokExample.getObject());
                Assert.assertEquals(result, 1);
                rs = DBHelper.execute(sqlSession.getConnection(), "select * from tb_lombok where key1 = 'key100'");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 1);
                Assert.assertEquals(rs.getInt("id"), -99);

            }
        });
    }
}
