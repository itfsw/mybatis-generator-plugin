/*
 * Copyright (c) 2018.
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
import java.sql.SQLException;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2018/4/20 10:57
 * ---------------------------------------------------------------------------
 */
public class SelectiveEnhancedPluginTest {
    /**
     * 初始化数据库
     */
    @BeforeClass
    public static void init() throws SQLException, IOException, ClassNotFoundException {
        DBHelper.createDB("scripts/SelectiveEnhancedPlugin/init.sql");
    }

    /**
     * 测试配置异常
     */
    @Test
    public void testWarnings() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        // 1. 没有配置ModelColumnPlugin插件
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/SelectiveEnhancedPlugin/mybatis-generator-without-ModelColumnPlugin.xml");
        tool.generate();
        Assert.assertEquals(tool.getWarnings().get(0), "itfsw:插件com.itfsw.mybatis.generator.plugins.SelectiveEnhancedPlugin插件需配合com.itfsw.mybatis.generator.plugins.ModelColumnPlugin插件使用！");
    }

    /**
     * 测试insertSelective增强
     */
    @Test
    public void testInsertSelective() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/SelectiveEnhancedPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                tb.set("id", 121L);
                tb.set("incF3", 10L);
                tb.set("tsIncF2", 5L);
                // selective
                ObjectUtil TbColumnId = new ObjectUtil(loader, packagz + ".Tb$Column#id");
                ObjectUtil TbColumnField1 = new ObjectUtil(loader, packagz + ".Tb$Column#field1");
                ObjectUtil TbColumnTsIncF2 = new ObjectUtil(loader, packagz + ".Tb$Column#tsIncF2");
                Object columns = Array.newInstance(TbColumnField1.getCls(), 3);
                Array.set(columns, 0, TbColumnId.getObject());
                Array.set(columns, 1, TbColumnField1.getObject());
                Array.set(columns, 2, TbColumnTsIncF2.getObject());

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "insertSelective", tb.getObject(), columns);
                Assert.assertEquals(sql, "insert into tb ( id , field_1 , inc_f2 ) values ( 121 , 'null' , 5 )");
                Object result = tbMapper.invoke("insertSelective", tb.getObject(), columns);
                Assert.assertEquals(result, 1);
            }
        });
    }

    /**
     * 测试 updateByExampleSelective
     */
    @Test
    public void testUpdateByExampleSelective() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/SelectiveEnhancedPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil TbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(TbExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 1l);

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                tb.set("incF3", 10l);
                tb.set("tsIncF2", 5l);
                // selective
                ObjectUtil TbColumnField1 = new ObjectUtil(loader, packagz + ".Tb$Column#field1");
                ObjectUtil TbColumnTsIncF2 = new ObjectUtil(loader, packagz + ".Tb$Column#tsIncF2");
                Object columns = Array.newInstance(TbColumnField1.getCls(), 2);
                Array.set(columns, 0, TbColumnField1.getObject());
                Array.set(columns, 1, TbColumnTsIncF2.getObject());

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "updateByExampleSelective", tb.getObject(), TbExample.getObject(), columns);
                Assert.assertEquals(sql, "update tb SET field_1 = 'null' , inc_f2 = 5 WHERE ( id = '1' )");
                Object result = tbMapper.invoke("updateByExampleSelective", tb.getObject(), TbExample.getObject(), columns);
                Assert.assertEquals(result, 1);
            }
        });
    }

    /**
     * 测试 updateByPrimaryKeySelective
     */
    @Test
    public void testUpdateByPrimaryKeySelective() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/SelectiveEnhancedPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                tb.set("id", 2l);
                tb.set("incF3", 10l);
                tb.set("tsIncF2", 5l);
                // selective
                ObjectUtil TbColumnField1 = new ObjectUtil(loader, packagz + ".Tb$Column#field1");
                ObjectUtil TbColumnTsIncF2 = new ObjectUtil(loader, packagz + ".Tb$Column#tsIncF2");
                Object columns = Array.newInstance(TbColumnField1.getCls(), 2);
                Array.set(columns, 0, TbColumnField1.getObject());
                Array.set(columns, 1, TbColumnTsIncF2.getObject());

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "updateByPrimaryKeySelective", tb.getObject(), columns);
                Assert.assertEquals(sql, "update tb SET field_1 = 'null' , inc_f2 = 5 where id = 2");
                Object result = tbMapper.invoke("updateByPrimaryKeySelective", tb.getObject(), columns);
                Assert.assertEquals(result, 1);
            }
        });
    }

    /**
     * 测试 upsertSelective
     */
    @Test
    public void testUpsertSelective() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/SelectiveEnhancedPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                tb.set("id", 10l);
                tb.set("incF3", 10l);
                tb.set("tsIncF2", 5l);
                // selective
                ObjectUtil TbColumnId = new ObjectUtil(loader, packagz + ".Tb$Column#id");
                ObjectUtil TbColumnField1 = new ObjectUtil(loader, packagz + ".Tb$Column#field1");
                ObjectUtil TbColumnTsIncF2 = new ObjectUtil(loader, packagz + ".Tb$Column#tsIncF2");
                Object columns = Array.newInstance(TbColumnField1.getCls(), 3);
                Array.set(columns, 0, TbColumnId.getObject());
                Array.set(columns, 1, TbColumnField1.getObject());
                Array.set(columns, 2, TbColumnTsIncF2.getObject());

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsertSelective", tb.getObject(), columns);
                Assert.assertEquals(sql, "insert into tb ( id , field_1 , inc_f2 ) values ( 10 , 'null' , 5 ) on duplicate key update id = 10 , field_1 = 'null' , inc_f2 = 5");
                Object result = tbMapper.invoke("upsertSelective", tb.getObject(), columns);
                Assert.assertEquals(result, 1);
            }
        });
    }

    /**
     * 测试 upsertByExampleSelective
     */
    @Test
    public void testUpsertByExampleSelective() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/SelectiveEnhancedPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil TbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(TbExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 99l);

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                tb.set("id", 99l);
                tb.set("incF3", 10l);
                tb.set("tsIncF2", 5l);
                // selective
                ObjectUtil TbColumnId = new ObjectUtil(loader, packagz + ".Tb$Column#id");
                ObjectUtil TbColumnField1 = new ObjectUtil(loader, packagz + ".Tb$Column#field1");
                ObjectUtil TbColumnTsIncF2 = new ObjectUtil(loader, packagz + ".Tb$Column#tsIncF2");
                Object columns = Array.newInstance(TbColumnField1.getCls(), 3);
                Array.set(columns, 0, TbColumnId.getObject());
                Array.set(columns, 1, TbColumnField1.getObject());
                Array.set(columns, 2, TbColumnTsIncF2.getObject());

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsertByExampleSelective", tb.getObject(), TbExample.getObject(), columns);
                Assert.assertEquals(sql, "update tb set id = 99 , field_1 = 'null' , inc_f2 = 5 WHERE ( id = '99' ) ; insert into tb ( id , field_1 , inc_f2 ) select 99 , 'null' , 5 from dual where not exists ( select 1 from tb WHERE ( id = '99' ) )");
                Object result = tbMapper.invoke("upsertByExampleSelective", tb.getObject(), TbExample.getObject(), columns);
                Assert.assertEquals(result, 0);
                result = tbMapper.invoke("upsertByExampleSelective", tb.getObject(), TbExample.getObject(), columns);
                Assert.assertEquals(result, 1);
            }
        });
    }
}