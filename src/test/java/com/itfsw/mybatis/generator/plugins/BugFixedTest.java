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
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.List;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2018/5/29 15:56
 * ---------------------------------------------------------------------------
 */
public class BugFixedTest {
    /**
     * 在使用 ModelColumnPlugin 插件时遇到关键词column或者table定义了alias属性，插件没有正确取值
     */
    @Test
    public void bug0001() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/BugFixedTest/bug-0001.xml");
        tool.generate(() -> DBHelper.createDB("scripts/BugFixedTest/bug-0001.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                tb.set("id", 121L);
                tb.set("inc", "inc_001");
                tb.set("table", "tb");
                // selective
                ObjectUtil TbColumnId = new ObjectUtil(loader, packagz + ".Tb$Column#id");
                ObjectUtil TbColumnInc = new ObjectUtil(loader, packagz + ".Tb$Column#inc");
                ObjectUtil TbColumnTable = new ObjectUtil(loader, packagz + ".Tb$Column#table");
                Object columns = Array.newInstance(TbColumnInc.getCls(), 3);
                Array.set(columns, 0, TbColumnId.getObject());
                Array.set(columns, 1, TbColumnInc.getObject());
                Array.set(columns, 2, TbColumnTable.getObject());

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "insertSelective", tb.getObject(), columns);
                Assert.assertEquals(sql, "insert into tb ( id , field1 , `table` ) values ( 121 , 'inc_001' , 'tb' )");
                Object result = tbMapper.invoke("insertSelective", tb.getObject(), columns);
                Assert.assertEquals(result, 1);

                // 执行查询
                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdLessThan", 160l);
                tbExample.set("orderByClause", TbColumnTable.invoke("asc"));

                sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExampleSelective", tbExample.getObject(), columns);
                Assert.assertEquals(sql, "select id , field1 , `table` from tb Test WHERE ( Test.id < '160' ) order by `table` ASC");
                ObjectUtil result1 = new ObjectUtil(((List) tbMapper.invoke("selectByExampleSelective", tbExample.getObject(), columns)).get(0));
                Assert.assertEquals(result1.get("table"), "tb");
            }
        });
    }

    /**
     * insertSelective 因为集成SelectiveEnhancedPlugin，传入参数变成map,自增ID返回要修正
     * @throws Exception
     */
    @Test
    public void bug0002() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/BugFixedTest/bug-0002.xml");
        tool.generate(() -> DBHelper.createDB("scripts/BugFixedTest/bug-0002.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                tb.set("field1", "ts1");

                // selective
                ObjectUtil TbColumnField1 = new ObjectUtil(loader, packagz + ".Tb$Column#field1");
                Object columns = Array.newInstance(TbColumnField1.getCls(), 1);
                Array.set(columns, 0, TbColumnField1.getObject());

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "insertSelective", tb.getObject(), columns);
                Assert.assertEquals(sql, "insert into tb ( field1 ) values ( 'ts1' )");
                Object result = tbMapper.invoke("insertSelective", tb.getObject(), columns);
                Assert.assertEquals(result, 1);
                // 自增ID
                Assert.assertEquals(tb.get("id"), 1L);
            }
        });
    }

    /**
     * 乐观锁插件好像变量作用域问题导致，前一个表的配置会影响后一个表配置
     */
    @Test
    public void issues39() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/BugFixedTest/issues-39.xml");
        tool.generate(() -> DBHelper.createDB("scripts/BugFixedTest/issues-39.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));
                // 第一个生成对应方法
                Assert.assertEquals(tbMapper.getMethods(OptimisticLockerPlugin.METHOD_DELETE_WITH_VERSION_BY_EXAMPLE).size(), 1);

                ObjectUtil tb1Mapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".Tb1Mapper")));
                // 第一个生成对应方法
                Assert.assertEquals(tb1Mapper.getMethods(OptimisticLockerPlugin.METHOD_DELETE_WITH_VERSION_BY_EXAMPLE).size(), 0);
            }
        });
    }
}
