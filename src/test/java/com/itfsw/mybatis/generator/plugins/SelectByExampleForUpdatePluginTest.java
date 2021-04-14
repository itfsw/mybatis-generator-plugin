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
public class SelectByExampleForUpdatePluginTest {


    @BeforeClass
    public static void init() throws Exception {
        DBHelper.createDB("scripts/SelectByExampleForUpdatePlugin/init.sql");
    }


    @Test
    public void testForUpdate() throws Exception {

        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/SelectByExampleForUpdatePlugin/mybatis-generator.xml");
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

                ObjectUtil Tb = new ObjectUtil(((List)result).get(0));
                Assert.assertEquals(Tb.get("id"), 1l);
                Assert.assertEquals(Tb.get("field1"), "fd1");
                Assert.assertNull(Tb.get("field2"));
            }
        });
    }

    @Test
    public void testForUpdateWithBLOBs() throws Exception{
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/SelectByExampleForUpdatePlugin/mybatis-generator.xml");
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
                ObjectUtil Tb = new ObjectUtil(((List)result).get(0));
                Assert.assertEquals(Tb.get("id"), 1l);
                Assert.assertEquals(Tb.get("field1"), "fd1");
                Assert.assertEquals(Tb.get("field2"), "fd2");
            }
        });
    }

}
