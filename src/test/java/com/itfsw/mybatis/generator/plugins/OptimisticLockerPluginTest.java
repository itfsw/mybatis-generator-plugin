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
import java.sql.ResultSet;
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
     * 测试 deleteWithVersionByExample
     */
    @Test
    public void testDeleteWithVersionByExample() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/OptimisticLockerPlugin/mybatis-generator.xml");
        tool.generate(() -> DBHelper.createDB("scripts/OptimisticLockerPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 1l);

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "deleteWithVersionByExample", 100L, tbExample.getObject());
                Assert.assertEquals(sql, "delete from tb WHERE inc_f1 = 100 and ( ( id = '1' ) )");

                // 执行一次，因为版本号100不存在所以删除不了
                Object result = tbMapper.invoke("deleteWithVersionByExample", 100L, tbExample.getObject());
                Assert.assertEquals(result, 0);

                // id = 1 的版本号应该是0
                result = tbMapper.invoke("deleteWithVersionByExample", 0L, tbExample.getObject());
                Assert.assertEquals(result, 1);

                // 删除成功
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select * from tb where id = 1");
                Assert.assertEquals(rs.first(), false);
            }
        });
    }

    /**
     * 测试 deleteWithVersionByPrimaryKey
     */
    @Test
    public void testDeleteWithVersionByPrimaryKey() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/OptimisticLockerPlugin/mybatis-generator.xml");

        // 测试只有单一主键的情况，这种情况下key使用@Param("key")注解
        tool.generate(() -> DBHelper.createDB("scripts/OptimisticLockerPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "deleteWithVersionByPrimaryKey", 100L, 1L);
                Assert.assertEquals(sql, "delete from tb where inc_f1 = 100 and id = 1");

                // 执行一次，因为版本号100不存在所以删除不了
                Object result = tbMapper.invoke("deleteWithVersionByPrimaryKey", 100L, 1L);
                Assert.assertEquals(result, 0);

                // id = 1 的版本号应该是0
                result = tbMapper.invoke("deleteWithVersionByPrimaryKey", 0L, 1L);
                Assert.assertEquals(result, 1);

                // 删除成功
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select * from tb where id = 1");
                Assert.assertEquals(rs.first(), false);
            }
        });

        // 测试多个主键的情况，这种情况下key使用@Param("record")注解
        tool.generate(() -> DBHelper.createDB("scripts/OptimisticLockerPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbKeysMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbKeysMapper")));

                ObjectUtil tbKeysKey = new ObjectUtil(loader, packagz + ".TbKeysKey");
                tbKeysKey.set("key1", 1L);
                tbKeysKey.set("key2", "k1");

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbKeysMapper.getObject(), "deleteWithVersionByPrimaryKey", 100L, tbKeysKey.getObject());
                Assert.assertEquals(sql, "delete from tb_keys where inc_f1 = 100 and key1 = 1 and key2 = 'k1'");

                // 执行一次，因为版本号100不存在所以删除不了
                Object result = tbKeysMapper.invoke("deleteWithVersionByPrimaryKey", 100L, tbKeysKey.getObject());
                Assert.assertEquals(result, 0);

                // key1 = 1, key2 = k1 的版本号应该是1
                result = tbKeysMapper.invoke("deleteWithVersionByPrimaryKey", 1L, tbKeysKey.getObject());
                Assert.assertEquals(result, 1);

                // 删除成功
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select * from tb_keys where key1 = 1 and key2 = 'k1'");
                Assert.assertEquals(rs.first(), false);
            }
        });
    }

    /**
     * 测试 updateWithVersionByExampleSelective
     */
    @Test
    public void testUpdateWithVersionByExampleSelective() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/OptimisticLockerPlugin/mybatis-generator.xml");
        tool.generate(() -> DBHelper.createDB("scripts/OptimisticLockerPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 1l);

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                tb.set("id", 1L);
                tb.set("incF1", 152L);  // 这个不会在sql才为正常
                tb.set("incF2", 10L);
                tb.set("incF3", 5L);

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "updateWithVersionByExampleSelective", 100L, tb.getObject(), tbExample.getObject());
                Assert.assertEquals(sql, "update tb SET inc_f1 = inc_f1 + 1, id = 1, inc_f2 = 10, inc_f3 = 5 WHERE inc_f1 = 100 and ( ( id = '1' ) )");

                // 执行一次，因为版本号100不存在所以应该返回0
                Object result = tbMapper.invoke("updateWithVersionByExampleSelective", 100L, tb.getObject(), tbExample.getObject());
                Assert.assertEquals(result, 0);

                // id = 1 的版本号应该是0
                result = tbMapper.invoke("updateWithVersionByExampleSelective", 0L, tb.getObject(), tbExample.getObject());
                Assert.assertEquals(result, 1);

                // 执行完成后版本号应该加1
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select * from tb where id = 1");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 1);
            }
        });
    }

    /**
     * 测试 updateWithVersionByExample
     */
    @Test
    public void testUpdateWithVersionByExample() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/OptimisticLockerPlugin/mybatis-generator.xml");
        // 测试不带or的更新
        tool.generate(() -> DBHelper.createDB("scripts/OptimisticLockerPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 1l);

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                tb.set("id", 1L);
                tb.set("incF1", 152L);  // 这个不会在sql才为正常
                tb.set("incF2", 10L);
                tb.set("incF3", 5L);

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "updateWithVersionByExample", 100L, tb.getObject(), tbExample.getObject());
                Assert.assertEquals(sql, "update tb set inc_f1 = inc_f1 + 1, id = 1, field1 = 'null', inc_f2 = 10, inc_f3 = 5 WHERE inc_f1 = 100 and ( ( id = '1' ) )");

                // 执行一次，因为版本号100不存在所以应该返回0
                Object result = tbMapper.invoke("updateWithVersionByExample", 100L, tb.getObject(), tbExample.getObject());
                Assert.assertEquals(result, 0);

                // id = 1 的版本号应该是0
                result = tbMapper.invoke("updateWithVersionByExample", 0L, tb.getObject(), tbExample.getObject());
                Assert.assertEquals(result, 1);

                // 执行完成后版本号应该加1
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select * from tb where id = 1");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 1);
            }
        });

        // 测试一下 byExample 对于or 是否正常生成了括号
        tool.generate(() -> DBHelper.createDB("scripts/OptimisticLockerPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 1l);

                ObjectUtil orCriteria = new ObjectUtil(tbExample.invoke("or"));
                orCriteria.invoke("andField1EqualTo", "ts1");

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                tb.set("id", 1L);
                tb.set("incF1", 152L);  // 这个不会在sql才为正常
                tb.set("incF2", 10L);
                tb.set("incF3", 5L);

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "updateWithVersionByExample", 100L, tb.getObject(), tbExample.getObject());
                Assert.assertEquals(sql, "update tb set inc_f1 = inc_f1 + 1, id = 1, field1 = 'null', inc_f2 = 10, inc_f3 = 5 WHERE inc_f1 = 100 and ( ( id = '1' ) or( field1 = 'ts1' ) )");

                // 执行一次，因为版本号100不存在所以应该返回0
                Object result = tbMapper.invoke("updateWithVersionByExample", 100L, tb.getObject(), tbExample.getObject());
                Assert.assertEquals(result, 0);
            }
        });
    }

    /**
     * 测试 updateWithVersionByExampleWithBLOBs
     */
    @Test
    public void testUpdateWithVersionByExampleWithBLOBs() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/OptimisticLockerPlugin/mybatis-generator.xml");

        // 测试执行withoutBLOBs
        tool.generate(() -> DBHelper.createDB("scripts/OptimisticLockerPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbBlobsMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbBlobsMapper")));

                ObjectUtil tbBlobsExample = new ObjectUtil(loader, packagz + ".TbBlobsExample");
                ObjectUtil criteria = new ObjectUtil(tbBlobsExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 1l);

                ObjectUtil tbBlobs = new ObjectUtil(loader, packagz + ".TbBlobs");
                tbBlobs.set("id", 1L);
                tbBlobs.set("incF1", 152L);  // 这个不会在sql才为正常
                tbBlobs.set("incF2", 10L);
                tbBlobs.set("incF3", 5L);

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbBlobsMapper.getObject(), "updateWithVersionByExample", 100L, tbBlobs.getObject(), tbBlobsExample.getObject());
                Assert.assertEquals(sql, "update tb_blobs set inc_f1 = inc_f1 + 1, id = 1, field1 = 'null', inc_f2 = 10, inc_f3 = 5 WHERE inc_f1 = 100 and ( ( id = '1' ) )");

                // 执行一次，因为版本号100不存在所以应该返回0
                Object result = tbBlobsMapper.invoke("updateWithVersionByExample", 100L, tbBlobs.getObject(), tbBlobsExample.getObject());
                Assert.assertEquals(result, 0);

                // id = 1 的版本号应该是1
                result = tbBlobsMapper.invoke("updateWithVersionByExample", 1L, tbBlobs.getObject(), tbBlobsExample.getObject());
                Assert.assertEquals(result, 1);

                // 执行完成后版本号应该加1
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select * from tb_blobs where id = 1");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 2);
            }
        });

        // 测试执行withBLOBs
        tool.generate(() -> DBHelper.createDB("scripts/OptimisticLockerPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbBlobsMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbBlobsMapper")));

                ObjectUtil tbBlobsExample = new ObjectUtil(loader, packagz + ".TbBlobsExample");
                ObjectUtil criteria = new ObjectUtil(tbBlobsExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 1l);

                ObjectUtil tbBlobsWithBLOBs = new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs");
                tbBlobsWithBLOBs.set("id", 1L);
                tbBlobsWithBLOBs.set("incF1", 152L);  // 这个不会在sql才为正常
                tbBlobsWithBLOBs.set("incF2", 10L);
                tbBlobsWithBLOBs.set("incF3", 5L);
                tbBlobsWithBLOBs.set("field1", "ts1");

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbBlobsMapper.getObject(), "updateWithVersionByExampleWithBLOBs", 100L, tbBlobsWithBLOBs.getObject(), tbBlobsExample.getObject());
                Assert.assertEquals(sql, "update tb_blobs set inc_f1 = inc_f1 + 1, id = 1, field1 = 'ts1', inc_f2 = 10, inc_f3 = 5, field2 = 'null', field3 = 'null' WHERE inc_f1 = 100 and ( ( id = '1' ) )");

                // 执行一次，因为版本号100不存在所以应该返回0
                Object result = tbBlobsMapper.invoke("updateWithVersionByExampleWithBLOBs", 100L, tbBlobsWithBLOBs.getObject(), tbBlobsExample.getObject());
                Assert.assertEquals(result, 0);

                // id = 1 的版本号应该是1
                result = tbBlobsMapper.invoke("updateWithVersionByExampleWithBLOBs", 1L, tbBlobsWithBLOBs.getObject(), tbBlobsExample.getObject());
                Assert.assertEquals(result, 1);

                // 执行完成后版本号应该加1
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select * from tb_blobs where id = 1");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 2);
                Assert.assertEquals(rs.getString("field1"), "ts1");
            }
        });
    }

    /**
     * 测试 updateWithVersionByPrimaryKeySelective
     */
    @Test
    public void testUpdateWithVersionByPrimaryKeySelective() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/OptimisticLockerPlugin/mybatis-generator.xml");
        tool.generate(() -> DBHelper.createDB("scripts/OptimisticLockerPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                tb.set("id", 1L);
                tb.set("incF1", 152L);  // 这个不会在sql才为正常
                tb.set("incF2", 10L);
                tb.set("incF3", 5L);

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "updateWithVersionByPrimaryKeySelective", 100L, tb.getObject());
                Assert.assertEquals(sql, "update tb SET inc_f1 = inc_f1 + 1, inc_f2 = 10, inc_f3 = 5 where inc_f1 = 100 and id = 1");

                // 执行一次，因为版本号100不存在所以应该返回0
                Object result = tbMapper.invoke("updateWithVersionByPrimaryKeySelective", 100L, tb.getObject());
                Assert.assertEquals(result, 0);

                // id = 1 的版本号应该是0
                result = tbMapper.invoke("updateWithVersionByPrimaryKeySelective", 0L, tb.getObject());
                Assert.assertEquals(result, 1);

                // 执行完成后版本号应该加1
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select * from tb where id = 1");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 1);
            }
        });
    }

    /**
     * 测试 updateWithVersionByPrimaryKey
     */
    @Test
    public void testUpdateWithVersionByPrimaryKey() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/OptimisticLockerPlugin/mybatis-generator.xml");
        tool.generate(() -> DBHelper.createDB("scripts/OptimisticLockerPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                tb.set("id", 1L);
                tb.set("incF1", 152L);  // 这个不会在sql才为正常
                tb.set("incF2", 10L);
                tb.set("incF3", 5L);

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "updateWithVersionByPrimaryKey", 100L, tb.getObject());
                Assert.assertEquals(sql, "update tb set inc_f1 = inc_f1 + 1, field1 = 'null', inc_f2 = 10, inc_f3 = 5 where inc_f1 = 100 and id = 1");

                // 执行一次，因为版本号100不存在所以应该返回0
                Object result = tbMapper.invoke("updateWithVersionByPrimaryKey", 100L, tb.getObject());
                Assert.assertEquals(result, 0);

                // id = 1 的版本号应该是0
                result = tbMapper.invoke("updateWithVersionByPrimaryKey", 0L, tb.getObject());
                Assert.assertEquals(result, 1);

                // 执行完成后版本号应该加1
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select * from tb where id = 1");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 1);
            }
        });
    }

    /**
     * 测试 updateWithVersionByPrimaryKeyWithBLOBs
     */
    @Test
    public void testUpdateWithVersionByPrimaryKeyWithBLOBs() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/OptimisticLockerPlugin/mybatis-generator.xml");

        // 测试执行withoutBLOBs
        tool.generate(() -> DBHelper.createDB("scripts/OptimisticLockerPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbBlobsMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbBlobsMapper")));

                ObjectUtil tbBlobs = new ObjectUtil(loader, packagz + ".TbBlobs");
                tbBlobs.set("id", 1L);
                tbBlobs.set("incF1", 152L);  // 这个不会在sql才为正常
                tbBlobs.set("incF2", 10L);
                tbBlobs.set("incF3", 5L);

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbBlobsMapper.getObject(), "updateWithVersionByPrimaryKey", 100L, tbBlobs.getObject());
                Assert.assertEquals(sql, "update tb_blobs set inc_f1 = inc_f1 + 1, field1 = 'null', inc_f2 = 10, inc_f3 = 5 where inc_f1 = 100 and id = 1");

                // 执行一次，因为版本号100不存在所以应该返回0
                Object result = tbBlobsMapper.invoke("updateWithVersionByPrimaryKey", 100L, tbBlobs.getObject());
                Assert.assertEquals(result, 0);

                // id = 1 的版本号应该是1
                result = tbBlobsMapper.invoke("updateWithVersionByPrimaryKey", 1L, tbBlobs.getObject());
                Assert.assertEquals(result, 1);

                // 执行完成后版本号应该加1
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select * from tb_blobs where id = 1");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 2);
            }
        });

        // 测试执行withBLOBs
        tool.generate(() -> DBHelper.createDB("scripts/OptimisticLockerPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbBlobsMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbBlobsMapper")));

                ObjectUtil tbBlobsWithBLOBs = new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs");
                tbBlobsWithBLOBs.set("id", 1L);
                tbBlobsWithBLOBs.set("incF1", 152L);  // 这个不会在sql才为正常
                tbBlobsWithBLOBs.set("incF2", 10L);
                tbBlobsWithBLOBs.set("incF3", 5L);
                tbBlobsWithBLOBs.set("field1", "ts1");

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbBlobsMapper.getObject(), "updateWithVersionByPrimaryKeyWithBLOBs", 100L, tbBlobsWithBLOBs.getObject());
                Assert.assertEquals(sql, "update tb_blobs set inc_f1 = inc_f1 + 1, field1 = 'ts1', inc_f2 = 10, inc_f3 = 5, field2 = 'null', field3 = 'null' where inc_f1 = 100 and id = 1");

                // 执行一次，因为版本号100不存在所以应该返回0
                Object result = tbBlobsMapper.invoke("updateWithVersionByPrimaryKeyWithBLOBs", 100L, tbBlobsWithBLOBs.getObject());
                Assert.assertEquals(result, 0);

                // id = 1 的版本号应该是1
                result = tbBlobsMapper.invoke("updateWithVersionByPrimaryKeyWithBLOBs", 1L, tbBlobsWithBLOBs.getObject());
                Assert.assertEquals(result, 1);

                // 执行完成后版本号应该加1
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select * from tb_blobs where id = 1");
                rs.first();
                Assert.assertEquals(rs.getInt("inc_f1"), 2);
                Assert.assertEquals(rs.getString("field1"), "ts1");
            }
        });
    }
}