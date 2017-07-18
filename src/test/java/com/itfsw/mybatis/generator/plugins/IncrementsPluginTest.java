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

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
                List<Method> methods = tbBuilder.getMethods("incF1");
                Assert.assertEquals(methods.size(), 2);
                // 自增方法
                Method incMethod = methods.get(0).getParameterTypes().length == 1 ? methods.get(1) : methods.get(0);
                Assert.assertEquals(incMethod.getParameters()[1].getParameterizedType().getTypeName(), packagz + ".Tb$Builder$Inc");

                // 2. 测试有空格
                ObjectUtil tbKeysBuilder = new ObjectUtil(loader, packagz + ".TbKeys$Builder");
                Assert.assertEquals(tbKeysBuilder.getMethods("incF1").size(), 2);
                Assert.assertEquals(tbKeysBuilder.getMethods("incF2").size(), 2);
                Assert.assertEquals(tbKeysBuilder.getMethods("incF3").size(), 2);

                // 3. 测试在WithBlobs正确生成
                ObjectUtil tbBlobsWithBLOBs = new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs$Builder");
                Assert.assertEquals(tbBlobsWithBLOBs.getMethods("incF1").size(), 2);
                Assert.assertEquals(tbBlobsWithBLOBs.getMethods("incF2").size(), 1);
                Assert.assertEquals(tbBlobsWithBLOBs.getMethods("incF3").size(), 2);
            }
        });
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
                Assert.assertEquals(sql, "update tb set id = null, field1 = 'null', inc_f1 =  inc_f1 + 100 , inc_f2 = null, inc_f3 = null WHERE (  id = '3' )");
                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "updateByExampleSelective", tbBuilder.invoke("build"), tbExample.getObject());
                Assert.assertEquals(sql, "update tb SET inc_f1 =  inc_f1 + 100 WHERE (  id = '3' )");
                // 执行
                // inc_f1 增加100
                Object result = tbMapper.invoke("updateByExampleSelective", tbBuilder.invoke("build"), tbExample.getObject());
                Assert.assertEquals(result, 1);
                PreparedStatement preparedStatement = sqlSession.getConnection().prepareStatement("select inc_f1 from tb where id = 3");
                preparedStatement.execute();
                ResultSet rs = preparedStatement.getResultSet();
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 103);

                // inc_f1 再减去50
                ObjectUtil tbBuilderDec = new ObjectUtil(loader, packagz + ".Tb$Builder$Inc#DEC");
                tbBuilder.invoke("incF1", 50l, tbBuilderDec.getObject());
                result = tbMapper.invoke("updateByExampleSelective", tbBuilder.invoke("build"), tbExample.getObject());
                Assert.assertEquals(result, 1);
                // 验证执行结果
                preparedStatement = sqlSession.getConnection().prepareStatement("select inc_f1 from tb where id = 3");
                preparedStatement.execute();
                rs = preparedStatement.getResultSet();
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
                Assert.assertEquals(sql, "update tb_keys set field1 = 'null', field2 = null, inc_f1 =  inc_f1 + 10 , inc_f2 =  null , inc_f3 =  inc_f3 + 30 where key1 = 1 and key2 = 'k1'");
                sql = SqlHelper.getFormatMapperSql(tbKeysMapper.getObject(), "updateByPrimaryKeySelective", tbKeysBuilder.invoke("build"));
                Assert.assertEquals(sql, "update tb_keys SET inc_f1 =  inc_f1 + 10 , inc_f3 =  inc_f3 + 30 where key1 = 1 and key2 = 'k1'");
                // 执行
                result = tbKeysMapper.invoke("updateByPrimaryKeySelective", tbKeysBuilder.invoke("build"));
                Assert.assertEquals(result, 1);
                // 验证执行结果
                preparedStatement = sqlSession.getConnection().prepareStatement("select inc_f1, inc_f3 from tb_keys where key1 = 1 and key2 = 'k1'");
                preparedStatement.execute();
                rs = preparedStatement.getResultSet();
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
                Assert.assertEquals(sql, "update tb_blobs set id = null, field1 = 'null', inc_f1 =  inc_f1 + 100 , inc_f2 = 50, inc_f3 =  null , field2 = 'null', field3 = 'blob' WHERE (  id = '3' )");

                tbBlobsWithBLOBsBuilder.invoke("id", 3l);
                sql = SqlHelper.getFormatMapperSql(tbBlobsMapper.getObject(), "updateByPrimaryKeyWithBLOBs", tbBlobsWithBLOBsBuilder.invoke("build"));
                Assert.assertEquals(sql, "update tb_blobs set field1 = 'null', inc_f1 =  inc_f1 + 100 , inc_f2 = 50, inc_f3 =  null , field2 = 'null', field3 = 'blob' where id = 3");

                // 执行
                tbBlobsWithBLOBsBuilder.invoke("incF3", 10l);
                // 测试自增字段没有配置自增参数
                sql = SqlHelper.getFormatMapperSql(tbBlobsMapper.getObject(), "updateByPrimaryKeyWithBLOBs", tbBlobsWithBLOBsBuilder.invoke("build"));
                Assert.assertEquals(sql, "update tb_blobs set field1 = 'null', inc_f1 =  inc_f1 + 100 , inc_f2 = 50, inc_f3 =  10 , field2 = 'null', field3 = 'blob' where id = 3");
                result = tbBlobsMapper.invoke("updateByPrimaryKeyWithBLOBs", tbBlobsWithBLOBsBuilder.invoke("build"));
                Assert.assertEquals(result, 1);
            }
        });
    }
}
