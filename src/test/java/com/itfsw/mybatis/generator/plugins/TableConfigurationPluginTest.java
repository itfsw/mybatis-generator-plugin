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
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2018/5/22 13:22
 * ---------------------------------------------------------------------------
 */
public class TableConfigurationPluginTest {
    @BeforeClass
    public static void init() throws Exception {
        DBHelper.createDB("scripts/TableConfigurationPlugin/init.sql");
    }

    /**
     * 测试domainObjectRenamingRule
     */
    @Test
    public void testDomainObjectRenamingRule() throws Exception {
        // 规则 ^T 替换成 Test
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/TableConfigurationPlugin/mybatis-generator-with-domainObjectRenamingRule.xml");
        MyBatisGenerator myBatisGenerator = tool.generate();
        for (GeneratedJavaFile file : myBatisGenerator.getGeneratedJavaFiles()){
            String name = file.getCompilationUnit().getType().getShortName();
            Assert.assertTrue(name.matches("Testb.*"));
        }
        // 执行一条语句确认其可用
        tool.generate(() -> DBHelper.resetDB("scripts/TableConfigurationPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TestbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TestbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdLessThan", 4L);

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample.getObject());
                Assert.assertEquals(sql, "select id, field1, inc_f1, inc_f2, inc_f3 from tb WHERE ( id < '4' )");
                // 执行
                List list = (List) tbMapper.invoke("selectByExample", tbExample.getObject());
                Assert.assertEquals(list.size(), 3);
            }
        });
    }

    /**
     * 测试columnRenamingRule
     */
    @Test
    public void testColumnRenamingRule() throws Exception {
        // 规则 ^T 替换成 Test
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/TableConfigurationPlugin/mybatis-generator-with-columnRenamingRule.xml");
        MyBatisGenerator myBatisGenerator = tool.generate();
        for (GeneratedJavaFile file : myBatisGenerator.getGeneratedJavaFiles()){
            if (file.getFileName().equals("Tb.java")){
                int count = 0;
                for (Field field : ((TopLevelClass)(file.getCompilationUnit())).getFields()){
                    if (field.getName().startsWith("increment")){
                        count++;
                    }
                }
                Assert.assertEquals(count, 3);
            }
        }

        // 执行一条语句确认其可用
        tool.generate(() -> DBHelper.resetDB("scripts/TableConfigurationPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 4L);

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                tb.set("id", 4L);
                tb.set("field1", "ts1");
                tb.set("incrementF1", 5L);

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "updateByExample", tb.getObject(), tbExample.getObject());
                Assert.assertEquals(sql, "update tb set id = 4, field1 = 'ts1', inc_f1 = 5, inc_f2 = null, inc_f3 = null WHERE ( id = '4' )");
                // 执行
                int count = (int) tbMapper.invoke("updateByExample", tb.getObject(), tbExample.getObject());
                Assert.assertEquals(count, 1);
                // 执行结果查询
                List list = (List) tbMapper.invoke("selectByExample", tbExample.getObject());
                ObjectUtil result = new ObjectUtil(list.get(0));
                Assert.assertEquals(result.get("id"), 4L);
                Assert.assertEquals(result.get("field1"), "ts1");
                Assert.assertEquals(result.get("incrementF1"), 5L);
            }
        });
    }

    /**
     * 测试clientSuffix
     */
    @Test
    public void testClientSuffix() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/TableConfigurationPlugin/mybatis-generator-with-clientSuffix.xml");
        MyBatisGenerator myBatisGenerator = tool.generate();

        boolean find = false;
        for (GeneratedJavaFile file : myBatisGenerator.getGeneratedJavaFiles()) {
            String name = file.getCompilationUnit().getType().getShortName();
            if (name.equals("TbDao")){
                find = true;
            }
        }
        Assert.assertTrue(find);

        find = false;
        for (GeneratedXmlFile file : myBatisGenerator.getGeneratedXmlFiles()) {
            String name = file.getFileName();
            if (name.equals("TbDao.xml")){
                find = true;
            }
        }
        Assert.assertTrue(find);

        // 执行一条语句确认其可用
        tool.generate(() -> DBHelper.resetDB("scripts/TableConfigurationPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbDao = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbDao")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdLessThan", 4L);

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbDao.getObject(), "selectByExample", tbExample.getObject());
                Assert.assertEquals(sql, "select id, field1, inc_f1, inc_f2, inc_f3 from tb WHERE ( id < '4' )");
                // 执行
                List list = (List) tbDao.invoke("selectByExample", tbExample.getObject());
                Assert.assertEquals(list.size(), 3);
            }
        });
    }


    /**
     * 测试exampleSuffix
     */
    @Test
    public void testExampleSuffix() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/TableConfigurationPlugin/mybatis-generator-with-exampleSuffix.xml");
        MyBatisGenerator myBatisGenerator = tool.generate();

        boolean find = false;
        for (GeneratedJavaFile file : myBatisGenerator.getGeneratedJavaFiles()) {
            String name = file.getCompilationUnit().getType().getShortName();
            if (name.equals("TbQuery")){
                find = true;
            }
        }
        Assert.assertTrue(find);
        // 执行一条语句确认其可用
        tool.generate(() -> DBHelper.resetDB("scripts/TableConfigurationPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbQuery = new ObjectUtil(loader, packagz + ".TbQuery");
                ObjectUtil criteria = new ObjectUtil(tbQuery.invoke("createCriteria"));
                criteria.invoke("andIdLessThan", 4L);

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbQuery.getObject());
                Assert.assertEquals(sql, "select id, field1, inc_f1, inc_f2, inc_f3 from tb WHERE ( id < '4' )");
                // 执行
                List list = (List) tbMapper.invoke("selectByExample", tbQuery.getObject());
                Assert.assertEquals(list.size(), 3);
            }
        });
    }
}