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

import java.io.IOException;
import java.sql.SQLException;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/7/31 17:52
 * ---------------------------------------------------------------------------
 */
public class SelectOneByExamplePluginTest {
    /**
     * 初始化数据库
     */
    @BeforeClass
    public static void init() throws SQLException, IOException, ClassNotFoundException {
        DBHelper.createDB("scripts/SelectOneByExamplePlugin/init.sql");
    }

    /**
     * 测试 selectOneByExample
     */
    @Test
    public void testSelectOneByExample() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/SelectOneByExamplePlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil TbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(TbExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 1l);

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectOneByExample", TbExample.getObject());
                Assert.assertEquals(sql, "select id, field1, field2 from tb WHERE ( id = '1' ) limit 1");
                Object result = tbMapper.invoke("selectOneByExample", TbExample.getObject());
                ObjectUtil Tb = new ObjectUtil(result);
                Assert.assertEquals(Tb.get("id"), 1l);
                Assert.assertEquals(Tb.get("field1"), "fd1");
                Assert.assertNull(Tb.get("field2"));
            }
        });
    }

    /**
     * 测试 selectOneByExampleWithBLOBs
     */
    @Test
    public void testSelectOneByExampleWithBLOBs() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/SelectOneByExamplePlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil TbBlobsMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbBlobsMapper")));

                ObjectUtil TbBlobsExample = new ObjectUtil(loader, packagz + ".TbBlobsExample");
                ObjectUtil criteria = new ObjectUtil(TbBlobsExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 1l);

                // sql
                String sql = SqlHelper.getFormatMapperSql(TbBlobsMapper.getObject(), "selectOneByExampleWithBLOBs", TbBlobsExample.getObject());
                Assert.assertEquals(sql, "select id, field1 , field2, field3 from tb_blobs WHERE ( id = '1' ) limit 1");
                Object result = TbBlobsMapper.invoke("selectOneByExampleWithBLOBs", TbBlobsExample.getObject());
                ObjectUtil Tb = new ObjectUtil(result);
                Assert.assertEquals(Tb.get("id"), 1l);
                Assert.assertEquals(Tb.get("field1"), "fd1");
                Assert.assertEquals(Tb.get("field2"), "fd2");
            }
        });
    }
}
