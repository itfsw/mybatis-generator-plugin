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
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.MyBatisGenerator;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2018/10/29 15:45
 * ---------------------------------------------------------------------------
 */
public class LombokPluginTest {
    /**
     * 初始化
     */
    @BeforeClass
    public static void init() throws Exception{
        DBHelper.createDB("scripts/LombokPlugin/init.sql");
    }

    /**
     * 测试具体生成
     */
    @Test
    public void testGenerate() throws Exception {
        // 全局规则增加 DB, tb2 单独规则增加Tt
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/LombokPlugin/mybatis-generator.xml");
        MyBatisGenerator myBatisGenerator = tool.generate();
        for (GeneratedJavaFile file : myBatisGenerator.getGeneratedJavaFiles()){
            String name = file.getCompilationUnit().getType().getShortName();
        }
    }

    @Test
    public void test() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/LombokPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception{
              System.out.println("xxx");
            }
        });
    }
}
