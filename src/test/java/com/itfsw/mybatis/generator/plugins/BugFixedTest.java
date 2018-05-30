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
                ObjectUtil result1 = new ObjectUtil(((List)tbMapper.invoke("selectByExampleSelective", tbExample.getObject(), columns)).get(0));
                Assert.assertEquals(result1.get("table"), "tb");
            }
        });
    }
}
