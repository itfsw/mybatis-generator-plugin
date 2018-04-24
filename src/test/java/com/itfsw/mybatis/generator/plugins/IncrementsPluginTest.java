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
import org.junit.Before;
import org.junit.Test;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
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
     * 初始化数据库
     */
    @Before
    public void init() throws Exception {
        DBHelper.createDB("scripts/IncrementsPlugin/init.sql");
    }

    /**
     * 测试没有配置ModelBuilderPlugin
     */
    @Test
    public void testWarningsWithoutModelBuilderPlugin() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/IncrementsPlugin/mybatis-generator-without-model-builder-plugin.xml");
        tool.generate();

        Assert.assertEquals(tool.getWarnings().get(0), "itfsw:插件com.itfsw.mybatis.generator.plugins.IncrementsPlugin插件需配合com.itfsw.mybatis.generator.plugins.ModelBuilderPlugin插件使用！");
    }

    /**
     * 测试ModelBuilder是否按配置正常生成了对应方法
     */
    @Test
    public void testModelBuilderMethod() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/IncrementsPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
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
        tool.generate(new AbstractShellCallback() {
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

                ObjectUtil tbKeysBuilderInc = new ObjectUtil(loader, packagz + ".TbKeys$Builder$Inc#INC");

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
    public void testWithSelectiveEnhancedPlugin() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/IncrementsPlugin/mybatis-generator-with-selective-enhanced-plugin.xml");
        tool.generate(new AbstractShellCallback() {
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

                ObjectUtil tbKeysBuilderInc = new ObjectUtil(loader, packagz + ".TbKeys$Builder$Inc#INC");

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
     * 测试 autoDelimitKeywords
     */
    @Test
    public void testWithAutoDelimitKeywords() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/IncrementsPlugin/mybatis-generator-with-autoDelimitKeywords.xml");
        tool.generate(new AbstractShellCallback() {
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
}
