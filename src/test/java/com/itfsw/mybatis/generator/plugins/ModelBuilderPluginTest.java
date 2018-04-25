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

import com.itfsw.mybatis.generator.plugins.tools.AbstractShellCallback;
import com.itfsw.mybatis.generator.plugins.tools.DBHelper;
import com.itfsw.mybatis.generator.plugins.tools.MyBatisGeneratorTool;
import com.itfsw.mybatis.generator.plugins.tools.ObjectUtil;
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
 * @time:2017/7/28 14:36
 * ---------------------------------------------------------------------------
 */
public class ModelBuilderPluginTest {
    /**
     * 初始化数据库
     */
    @BeforeClass
    public static void init() throws SQLException, IOException, ClassNotFoundException {
        DBHelper.createDB("scripts/ModelBuilderPlugin/init.sql");
    }

    /**
     * 测试生成的model
     */
    @Test
    public void test() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/ModelBuilderPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 普通model
                ObjectUtil tbBuilder = new ObjectUtil(loader, packagz + ".Tb$Builder");
                tbBuilder.invoke("field1", "ts1");
                tbBuilder.invoke("incF1", 100l);
                ObjectUtil tb = new ObjectUtil(tbBuilder.invoke("build"));
                Assert.assertEquals(tb.invoke("getField1"), "ts1");
                Assert.assertEquals(tb.invoke("getIncF1"), 100l);

                // 2. withBlobs
                ObjectUtil tbBlobsBuilder = new ObjectUtil(loader, packagz + ".TbBlobs$Builder");
                tbBlobsBuilder.invoke("field1", "ts1");
                ObjectUtil tbBlobs = new ObjectUtil(tbBlobsBuilder.invoke("build"));
                Assert.assertEquals(tbBlobs.invoke("getField1"), "ts1");

                ObjectUtil tbBlobsWithBLOBsBuilder = new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs$Builder");
                tbBlobsWithBLOBsBuilder.invoke("field1", "ts1");
                tbBlobsWithBLOBsBuilder.invoke("field2", "ts2");
                ObjectUtil tbBlobsWithBLOBs = new ObjectUtil(tbBlobsWithBLOBsBuilder.invoke("build"));
                Assert.assertEquals(tbBlobsWithBLOBs.invoke("getField1"), "ts1");
                Assert.assertEquals(tbBlobsWithBLOBs.invoke("getField2"), "ts2");

                // 3. key
                ObjectUtil tbKeysKeyBuilder = new ObjectUtil(loader, packagz + ".TbKeysKey$Builder");
                tbKeysKeyBuilder.invoke("key1", 60l);
                ObjectUtil tbKeysKey = new ObjectUtil(tbKeysKeyBuilder.invoke("build"));
                Assert.assertEquals(tbKeysKey.invoke("getKey1"), 60l);

                ObjectUtil tbKeysBuilder = new ObjectUtil(loader, packagz + ".TbKeys$Builder");
                tbKeysBuilder.invoke("key1", 50l);
                tbKeysBuilder.invoke("field1", "ts2");
                ObjectUtil tbKeys = new ObjectUtil(tbKeysBuilder.invoke("build"));
                Assert.assertEquals(tbKeys.invoke("getKey1"), 50l);
                Assert.assertEquals(tbKeys.invoke("getField1"), "ts2");

                // 4. key and blobs
                ObjectUtil TbKeysBlobsKeyBuilder = new ObjectUtil(loader, packagz + ".TbKeysBlobsKey$Builder");
                TbKeysBlobsKeyBuilder.invoke("key1", 60l);
                ObjectUtil TbKeysBlobsKey = new ObjectUtil(TbKeysBlobsKeyBuilder.invoke("build"));
                Assert.assertEquals(TbKeysBlobsKey.invoke("getKey1"), 60l);

                ObjectUtil TbKeysBlobsWithBLOBsBuilder = new ObjectUtil(loader, packagz + ".TbKeysBlobsWithBLOBs$Builder");
                TbKeysBlobsWithBLOBsBuilder.invoke("key1", 90l);
                TbKeysBlobsWithBLOBsBuilder.invoke("incF2", 70l);
                TbKeysBlobsWithBLOBsBuilder.invoke("field2", "ts3");

                ObjectUtil TbKeysBlobsWithBLOBs = new ObjectUtil(TbKeysBlobsWithBLOBsBuilder.invoke("build"));
                Assert.assertEquals(TbKeysBlobsWithBLOBs.invoke("getKey1"), 90l);
                Assert.assertEquals(TbKeysBlobsWithBLOBs.invoke("getIncF2"), 70l);
                Assert.assertEquals(TbKeysBlobsWithBLOBs.invoke("getField2"), "ts3");
            }
        });
    }

    /**
     * 测试静态builder方法
     */
    @Test
    public void testBuilderMethod() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/ModelBuilderPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 普通model
                ObjectUtil Tb = new ObjectUtil(loader, packagz + ".Tb");
                ObjectUtil tbBuilder = new ObjectUtil(Tb.invoke("builder"));
                tbBuilder.invoke("field1", "ts1");
                tbBuilder.invoke("incF1", 100l);
                ObjectUtil tb = new ObjectUtil(tbBuilder.invoke("build"));
                Assert.assertEquals(tb.invoke("getField1"), "ts1");
                Assert.assertEquals(tb.invoke("getIncF1"), 100l);

                // 2. withBlobs
                ObjectUtil TbBlobs = new ObjectUtil(loader, packagz + ".TbBlobs");
                ObjectUtil tbBlobsBuilder = new ObjectUtil(TbBlobs.invoke("builder"));
                tbBlobsBuilder.invoke("field1", "ts1");
                ObjectUtil tbBlobs = new ObjectUtil(tbBlobsBuilder.invoke("build"));
                Assert.assertEquals(tbBlobs.invoke("getField1"), "ts1");

                ObjectUtil TbBlobsWithBLOBs= new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs");
                ObjectUtil tbBlobsWithBLOBsBuilder = new ObjectUtil(TbBlobsWithBLOBs.invoke("builder"));
                tbBlobsWithBLOBsBuilder.invoke("field1", "ts1");
                tbBlobsWithBLOBsBuilder.invoke("field2", "ts2");
                ObjectUtil tbBlobsWithBLOBs = new ObjectUtil(tbBlobsWithBLOBsBuilder.invoke("build"));
                Assert.assertEquals(tbBlobsWithBLOBs.invoke("getField1"), "ts1");
                Assert.assertEquals(tbBlobsWithBLOBs.invoke("getField2"), "ts2");

                // 3. key
                ObjectUtil TbKeysKey = new ObjectUtil(loader, packagz + ".TbKeysKey");
                ObjectUtil tbKeysKeyBuilder = new ObjectUtil(TbKeysKey.invoke("builder"));
                tbKeysKeyBuilder.invoke("key1", 60l);
                ObjectUtil tbKeysKey = new ObjectUtil(tbKeysKeyBuilder.invoke("build"));
                Assert.assertEquals(tbKeysKey.invoke("getKey1"), 60l);

                ObjectUtil TbKeys = new ObjectUtil(loader, packagz + ".TbKeys");
                ObjectUtil tbKeysBuilder = new ObjectUtil(TbKeys.invoke("builder"));
                tbKeysBuilder.invoke("key1", 50l);
                tbKeysBuilder.invoke("field1", "ts2");
                ObjectUtil tbKeys = new ObjectUtil(tbKeysBuilder.invoke("build"));
                Assert.assertEquals(tbKeys.invoke("getKey1"), 50l);
                Assert.assertEquals(tbKeys.invoke("getField1"), "ts2");

                // 4. key and blobs
                ObjectUtil TbKeysBlobsKey = new ObjectUtil(loader, packagz + ".TbKeysBlobsKey");
                ObjectUtil TbKeysBlobsKeyBuilder = new ObjectUtil(TbKeysBlobsKey.invoke("builder"));
                TbKeysBlobsKeyBuilder.invoke("key1", 60l);
                ObjectUtil tbKeysBlobsKey = new ObjectUtil(TbKeysBlobsKeyBuilder.invoke("build"));
                Assert.assertEquals(tbKeysBlobsKey.invoke("getKey1"), 60l);

                ObjectUtil TbKeysBlobsWithBLOBs = new ObjectUtil(loader, packagz + ".TbKeysBlobsWithBLOBs");
                ObjectUtil TbKeysBlobsWithBLOBsBuilder = new ObjectUtil(TbKeysBlobsWithBLOBs.invoke("builder"));
                TbKeysBlobsWithBLOBsBuilder.invoke("key1", 90l);
                TbKeysBlobsWithBLOBsBuilder.invoke("incF2", 70l);
                TbKeysBlobsWithBLOBsBuilder.invoke("field2", "ts3");

                TbKeysBlobsWithBLOBs = new ObjectUtil(TbKeysBlobsWithBLOBsBuilder.invoke("build"));
                Assert.assertEquals(TbKeysBlobsWithBLOBs.invoke("getKey1"), 90l);
                Assert.assertEquals(TbKeysBlobsWithBLOBs.invoke("getIncF2"), 70l);
                Assert.assertEquals(TbKeysBlobsWithBLOBs.invoke("getField2"), "ts3");
            }
        });
    }
}
