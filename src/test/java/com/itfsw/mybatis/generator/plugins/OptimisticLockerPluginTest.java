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

import java.io.IOException;
import java.sql.SQLException;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2018/5/2 17:05
 * ---------------------------------------------------------------------------
 */
public class OptimisticLockerPluginTest {
    /**
     * 初始化数据库
     */
    @BeforeClass
    public static void init() throws SQLException, IOException, ClassNotFoundException {
        DBHelper.createDB("scripts/OptimisticLockerPlugin/init.sql");
    }

    /**
     * 测试 updateWithVersionByExampleSelective
     */
    @Test
    public void testUpdateWithVersionByExampleSelective() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/OptimisticLockerPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 1l);

                ObjectUtil orCriteria = new ObjectUtil(tbExample.invoke("or"));
                orCriteria.invoke("andField1EqualTo", "ts1");

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "deleteWithVersionByExample", 1L, tbExample.getObject());
                Assert.assertEquals(sql, "update tb SET field_1 = 'null' , inc_f2 = 5 WHERE ( id = '1' )");
            }
        });
    }
}