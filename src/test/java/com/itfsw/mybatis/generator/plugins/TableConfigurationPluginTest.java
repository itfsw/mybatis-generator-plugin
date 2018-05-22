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
import org.apache.ibatis.session.SqlSession;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2018/5/22 13:22
 * ---------------------------------------------------------------------------
 */
public class TableConfigurationPluginTest {
    @BeforeClass
    public static void init() throws Exception {
        DBHelper.createDB("scripts/TableConfigurationPlugin/init.sql");
    }

    @Test
    public void test() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/TableConfigurationPlugin/mybatis-generator.xml");
        tool.generate(() -> DBHelper.resetDB("scripts/TableConfigurationPlugin/init.sql"), new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {

            }
        });
    }
}