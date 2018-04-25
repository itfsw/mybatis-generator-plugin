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
 * @time:2017/7/28 15:15
 * ---------------------------------------------------------------------------
 */
public class ModelColumnPluginTest {
    /**
     * 初始化数据库
     */
    @BeforeClass
    public static void init() throws SQLException, IOException, ClassNotFoundException {
        DBHelper.createDB("scripts/ModelColumnPlugin/init.sql");
    }

    /**
     * 测试生成的model
     */
    @Test
    public void test() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/ModelColumnPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // 1. 普通model
                ObjectUtil TbColumnField1 = new ObjectUtil(loader, packagz + ".Tb$Column#field1");
                Assert.assertEquals(TbColumnField1.invoke("value"), "field_1");
                Assert.assertEquals(TbColumnField1.invoke("getValue"), "field_1");
                Assert.assertEquals(TbColumnField1.invoke("asc"), "field_1 ASC");
                Assert.assertEquals(TbColumnField1.invoke("desc"), "field_1 DESC");

                // 2. columnOverride
                ObjectUtil TbColumnTsIncF2 = new ObjectUtil(loader, packagz + ".Tb$Column#tsIncF2");
                Assert.assertEquals(TbColumnTsIncF2.invoke("value"), "inc_f2");

                // 3. withBlobs
                ObjectUtil TbBlobsColumnField1 = new ObjectUtil(loader, packagz + ".TbBlobs$Column#field1");
                Assert.assertEquals(TbBlobsColumnField1.invoke("value"), "field_1");
                ObjectUtil TbBlobsWithBLOBsColumnField2 = new ObjectUtil(loader, packagz + ".TbBlobsWithBLOBs$Column#field2");
                Assert.assertEquals(TbBlobsWithBLOBsColumnField2.invoke("value"), "field_2");

                // 4. key
                ObjectUtil TbKeysKeyColumnKey1 = new ObjectUtil(loader, packagz + ".TbKeysKey$Column#key1");
                Assert.assertEquals(TbKeysKeyColumnKey1.invoke("value"), "key_1");
                ObjectUtil TbKeysColumnKey1 = new ObjectUtil(loader, packagz + ".TbKeys$Column#key1");
                Assert.assertEquals(TbKeysColumnKey1.invoke("value"), "key_1");
                ObjectUtil TbKeysColumnField1 = new ObjectUtil(loader, packagz + ".TbKeys$Column#field1");
                Assert.assertEquals(TbKeysColumnField1.invoke("value"), "field_1");
            }
        });
    }
}
