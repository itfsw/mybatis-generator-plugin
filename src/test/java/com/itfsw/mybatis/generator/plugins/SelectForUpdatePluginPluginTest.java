package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.tools.*;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

/**
 * @author durenhao
 * @date 2021/4/14 23:42
 **/
public class SelectForUpdatePluginPluginTest {

    @BeforeClass
    public static void init() throws Exception {
        DBHelper.createDB("scripts/SelectForUpdatePlugin/init.sql");
    }

    @Test
    public void testSelectByPrimaryKeyForUpdate() throws Exception {

        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/SelectForUpdatePlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(),
                        "selectByPrimaryKeyForUpdate", 1L);
                Assert.assertEquals(sql, "select id, field1, field2 from tb where id = 1 for update");
                Object result = tbMapper.invoke("selectByPrimaryKeyForUpdate", 1L);

                ObjectUtil Tb = new ObjectUtil(result);
                Assert.assertEquals(Tb.get("id"), 1L);
                Assert.assertEquals(Tb.get("field1"), "fd1");
                Assert.assertNull(Tb.get("field2"));
            }
        });
    }


    @Test
    public void testSelectByExampleForUpdate() throws Exception {

        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/SelectForUpdatePlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil TbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(TbExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 1l);

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExampleForUpdate", TbExample.getObject());
                Assert.assertEquals(sql, "select id, field1, field2 from tb WHERE ( id = '1' ) for update");
                Object result = tbMapper.invoke("selectByExampleForUpdate", TbExample.getObject());

                ObjectUtil Tb = new ObjectUtil(((List) result).get(0));
                Assert.assertEquals(Tb.get("id"), 1l);
                Assert.assertEquals(Tb.get("field1"), "fd1");
                Assert.assertNull(Tb.get("field2"));
            }
        });
    }


    @Test
    public void testSelectByExampleWithBLOBsForUpdate() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/SelectForUpdatePlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil TbBlobsMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbBlobsMapper")));

                ObjectUtil TbBlobsExample = new ObjectUtil(loader, packagz + ".TbBlobsExample");
                ObjectUtil criteria = new ObjectUtil(TbBlobsExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 1l);

                // sql
                String sql = SqlHelper.getFormatMapperSql(TbBlobsMapper.getObject(), "selectByExampleWithBLOBsForUpdate", TbBlobsExample.getObject());
                Assert.assertEquals(sql, "select id, field1 , field2, field3 from tb_blobs WHERE ( id = '1' ) for update");
                Object result = TbBlobsMapper.invoke("selectByExampleWithBLOBsForUpdate", TbBlobsExample.getObject());
                ObjectUtil Tb = new ObjectUtil(((List) result).get(0));
                Assert.assertEquals(Tb.get("id"), 1l);
                Assert.assertEquals(Tb.get("field1"), "fd1");
                Assert.assertEquals(Tb.get("field2"), "fd2");
            }
        });
    }


    @Test
    public void testSelectOneByExampleForUpdate() throws Exception {

        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/SelectForUpdatePlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil TbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(TbExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 1l);

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectOneByExampleForUpdate", TbExample.getObject());
                Assert.assertEquals(sql, "select id, field1, field2 from tb WHERE ( id = '1' ) limit 1 for update");
                Object result = tbMapper.invoke("selectOneByExampleForUpdate", TbExample.getObject());

                ObjectUtil Tb = new ObjectUtil(result);
                Assert.assertEquals(Tb.get("id"), 1l);
                Assert.assertEquals(Tb.get("field1"), "fd1");
                Assert.assertNull(Tb.get("field2"));
            }
        });
    }


    @Test
    public void testSelectOneByExampleWithBLOBsForUpdate() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/SelectForUpdatePlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil TbBlobsMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbBlobsMapper")));

                ObjectUtil TbBlobsExample = new ObjectUtil(loader, packagz + ".TbBlobsExample");
                ObjectUtil criteria = new ObjectUtil(TbBlobsExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 1l);

                // sql
                String sql = SqlHelper.getFormatMapperSql(TbBlobsMapper.getObject(), "selectOneByExampleWithBLOBsForUpdate", TbBlobsExample.getObject());
                Assert.assertEquals(sql, "select id, field1 , field2, field3 from tb_blobs WHERE ( id = '1' ) limit 1 for update");
                Object result = TbBlobsMapper.invoke("selectOneByExampleWithBLOBsForUpdate", TbBlobsExample.getObject());
                ObjectUtil Tb = new ObjectUtil(result);
                Assert.assertEquals(Tb.get("id"), 1l);
                Assert.assertEquals(Tb.get("field1"), "fd1");
                Assert.assertEquals(Tb.get("field2"), "fd2");
            }
        });
    }

}
