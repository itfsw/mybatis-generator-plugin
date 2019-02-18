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
import java.util.List;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/7/7 16:59
 * ---------------------------------------------------------------------------
 */
public class LogicalDeletePluginTest {
    /**
     * 初始化数据库
     */
    @BeforeClass
    public static void init() throws SQLException, IOException, ClassNotFoundException {
        DBHelper.createDB("scripts/LogicalDeletePlugin/init.sql");
    }

    /**
     * 测试配置异常
     */
    @Test
    public void testWarnings() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        // 1. 不支持的类型
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/LogicalDeletePlugin/mybatis-generator-with-unsupport-type.xml");
        tool.generate();

        Assert.assertEquals(tool.getWarnings().get(0), "itfsw(逻辑删除插件):tb逻辑删除列(ts_2)的类型不在支持范围（请使用数字列，字符串列，布尔列）！");

        // 2. 没有找到配置的逻辑删除列
        tool = MyBatisGeneratorTool.create("scripts/LogicalDeletePlugin/mybatis-generator-with-unfind-column.xml");
        tool.generate();

        Assert.assertEquals(tool.getWarnings().get(0), "itfsw(逻辑删除插件):tb没有找到您配置的逻辑删除列(ts_999)！");

        // 3. 没有配置逻辑删除值
        tool = MyBatisGeneratorTool.create("scripts/LogicalDeletePlugin/mybatis-generator-with-unconfig-logicalDeleteValue.xml");
        tool.generate();

        Assert.assertEquals(tool.getWarnings().get(0), "itfsw(逻辑删除插件):tb没有找到您配置的逻辑删除值，请全局或者局部配置logicalDeleteValue和logicalUnDeleteValue值！");

        // 4. 保留关键词冲突
        tool = MyBatisGeneratorTool.create("scripts/LogicalDeletePlugin/mybatis-generator-with-keywords.xml");
        tool.generate();

        Assert.assertEquals(tool.getWarnings().get(0), "itfsw(逻辑删除插件):tb配置的逻辑删除列和插件保留关键字(andLogicalDeleted)冲突！");
    }

    /**
     * 测试 logicalDeleteByExample
     */
    @Test
    public void testLogicalDeleteByExample() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/LogicalDeletePlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception{
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 1l);

                // 验证sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "logicalDeleteByExample", tbExample.getObject());
                Assert.assertEquals(sql, "update tb set del_flag = 1 WHERE ( id = '1' )");
                // 验证执行
                Object result = tbMapper.invoke("logicalDeleteByExample", tbExample.getObject());
                Assert.assertEquals(result, 1);
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select del_flag from tb where id = 1");
                rs.first();
                Assert.assertEquals(rs.getInt("del_flag"), 1);
            }
        });
    }

    /**
     * 测试 logicalDeleteByPrimaryKey
     */
    @Test
    public void testLogicalDeleteByPrimaryKey() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/LogicalDeletePlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception{
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                // 验证sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "logicalDeleteByPrimaryKey", 2l);
                Assert.assertEquals(sql, "update tb set del_flag = 1 where id = 2");
                // 验证执行
                Object result = tbMapper.invoke("logicalDeleteByPrimaryKey", 2l);
                Assert.assertEquals(result, 1);
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select del_flag from tb where id = 2");
                rs.first();
                Assert.assertEquals(rs.getInt("del_flag"), 1);
            }
        });
    }

    /**
     * 测试关联生成的方法和常量
     */
    @Test
    public void testOtherMethods() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/LogicalDeletePlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception{
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andLogicalDeleted", true);
                criteria.invoke("andIdEqualTo", 3l);


                // 验证sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample.getObject());
                Assert.assertEquals(sql, "select id, del_flag, and_logical_deleted, ts_1, ts_3, ts_4 from tb WHERE ( del_flag = '1' and id = '3' )");
                // 验证执行
                Object result = tbMapper.invoke("selectByExample", tbExample.getObject());
                Assert.assertEquals(((List)result).size(), 1);
            }
        });
    }

    /**
     * 测试自定义常量
     */
    @Test
    public void testCustomConst() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/LogicalDeletePlugin/mybatis-generator-with-customConstName.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception{
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));
                ObjectUtil Tb = new ObjectUtil(loader, packagz + ".Tb");

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andDelFlagEqualTo", Tb.get("UN_DEL"));
                criteria.invoke("andIdEqualTo", 3l);

                // 验证sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample.getObject());
                Assert.assertEquals(sql, "select id, del_flag, and_logical_deleted, ts_1, ts_3, ts_4 from tb WHERE ( del_flag = '0' and id = '3' )");
                // 验证执行
                Object result = tbMapper.invoke("selectByExample", tbExample.getObject());
                Assert.assertEquals(((List)result).size(), 0);
            }
        });
    }

    /**
     * 测试Model andLogicalDeleted 方法
     */
    @Test
    public void testModelAndLogicalDeletedMethod() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/LogicalDeletePlugin/mybatis-generator-with-customConstName.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception{
                ObjectUtil Tb = new ObjectUtil(loader, packagz + ".Tb");

                Tb.invoke("andLogicalDeleted", true);
                Assert.assertEquals(Tb.get("delFlag"), (short)1);

                Tb.invoke("andLogicalDeleted", false);
                Assert.assertEquals(Tb.get("delFlag"), (short)0);
            }
        });
    }

    /**
     * 测试 selectByPrimaryKeyWithLogicalDelete
     */
    @Test
    public void testSelectByPrimaryKeyWithLogicalDelete() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/LogicalDeletePlugin/mybatis-generator-with-customConstName.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception{
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                // 验证sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByPrimaryKeyWithLogicalDelete", 5l, true);
                Assert.assertEquals(sql, "select id, del_flag, and_logical_deleted, ts_1, ts_3, ts_4 , ts_2 from tb where id = 5 and del_flag = 1");
                // 验证执行
                Object result = tbMapper.invoke("selectByPrimaryKeyWithLogicalDelete", 5l, true);
                Assert.assertNull(result);

                // 验证执行
                result = tbMapper.invoke("selectByPrimaryKeyWithLogicalDelete", 5l, false);
                Assert.assertNotNull(result);
            }
        });
    }

    /**
     * 测试基于注释生成的逻辑删除枚举
     * @throws Exception
     */
    @Test
    public void testWithRemarkEnum() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/LogicalDeletePlugin/mybatis-generator-with-remarks-enum.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception{
                // 验证是否已经按要求生成了枚举
                ObjectUtil enumDelFlagEnable = new ObjectUtil(loader, packagz + ".TbRemark$DelFlag#ENABLE");
                Assert.assertEquals(enumDelFlagEnable.invoke("value"), (short)1);
                Assert.assertEquals(enumDelFlagEnable.invoke("getValue"), (short)1);
                Assert.assertEquals(enumDelFlagEnable.invoke("getName"), "启用");

                // 验证andLogicalDeleted方法
                ObjectUtil tbRemark = new ObjectUtil(loader, packagz + ".TbRemark");
                tbRemark.invoke("andLogicalDeleted", true);
                Assert.assertEquals(tbRemark.get("delFlag"), (short)1);
                tbRemark.invoke("andLogicalDeleted", false);
                Assert.assertEquals(tbRemark.get("delFlag"), (short)0);

                // 验证sql执行
                ObjectUtil tbRemarkMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbRemarkMapper")));
                ObjectUtil tbRemarkExample = new ObjectUtil(loader, packagz + ".TbRemarkExample");
                ObjectUtil criteria = new ObjectUtil(tbRemarkExample.invoke("createCriteria"));
                criteria.invoke("andLogicalDeleted", true);
                String sql = SqlHelper.getFormatMapperSql(tbRemarkMapper.getObject(), "selectByExample", tbRemarkExample.getObject());
                Assert.assertEquals(sql, "select id, del_flag from tb_remark WHERE ( del_flag = '1' )");
                Object result = tbRemarkMapper.invoke("selectByExample", tbRemarkExample.getObject());
                Assert.assertEquals(((List)result).size(), 1);
            }
        });
    }
}
