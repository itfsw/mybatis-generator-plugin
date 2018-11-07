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

import com.itfsw.mybatis.generator.plugins.tools.AbstractShellCallback;
import com.itfsw.mybatis.generator.plugins.tools.DBHelper;
import com.itfsw.mybatis.generator.plugins.tools.MyBatisGeneratorTool;
import com.itfsw.mybatis.generator.plugins.tools.ObjectUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2018/11/7 15:43
 * ---------------------------------------------------------------------------
 */
public class ModelCloneablePluginTest {
    /**
     * 初始化
     */
    @BeforeClass
    public static void init() throws Exception {
        DBHelper.createDB("scripts/ModelCloneablePlugin/init.sql");
    }

    /**
     * 测试生成的model
     */
    @Test
    public void testModel() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/ModelCloneablePlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                tb.set("id", 100L);
                tb.set("field1", "ts1");

                ObjectUtil tbClone = new ObjectUtil(tb.invoke("clone"));
                Assert.assertEquals(tbClone.get("id"), 100L);
                Assert.assertEquals(tbClone.get("field1"), "ts1");

                tbClone.set("field1", "ts2");
                Assert.assertEquals(tb.get("field1"), "ts1");
                Assert.assertEquals(tbClone.get("field1"), "ts2");
            }
        });
    }

}