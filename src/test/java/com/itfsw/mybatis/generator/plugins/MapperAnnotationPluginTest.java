/*
 * Copyright (c) 2019.
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

import com.itfsw.mybatis.generator.plugins.tools.DBHelper;
import com.itfsw.mybatis.generator.plugins.tools.MyBatisGeneratorTool;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2019/7/9 14:46
 * ---------------------------------------------------------------------------
 */
public class MapperAnnotationPluginTest {

    /**
     * 初始化
     */
    @BeforeClass
    public static void init() throws Exception {
        DBHelper.createDB("scripts/MapperAnnotationPlugin/init.sql");
    }

    /**
     * 测试默认配置
     */
    @Test
    public void testDefault() throws Exception{
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/MapperAnnotationPlugin/mybatis-generator.xml");
        MyBatisGenerator myBatisGenerator = tool.generate();

        for (GeneratedJavaFile file : myBatisGenerator.getGeneratedJavaFiles()) {
            CompilationUnit compilationUnit = file.getCompilationUnit();
            if (compilationUnit instanceof Interface && compilationUnit.getType().getShortName().endsWith("Mapper")) {
                Interface interfaze = (Interface) compilationUnit;

                Assert.assertEquals(interfaze.getAnnotations().size(), 1);
                Assert.assertEquals(interfaze.getAnnotations().get(0), "@Mapper");
                Assert.assertTrue(interfaze.getImportedTypes().contains(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Mapper")));
            }
        }
    }

    /**
     * 测试配置Repository
     * @throws Exception
     */
    @Test
    public void testWithRepository() throws Exception{
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/MapperAnnotationPlugin/mybatis-generator-with-repository.xml");
        MyBatisGenerator myBatisGenerator = tool.generate();

        for (GeneratedJavaFile file : myBatisGenerator.getGeneratedJavaFiles()) {
            CompilationUnit compilationUnit = file.getCompilationUnit();
            if (compilationUnit instanceof Interface && compilationUnit.getType().getShortName().endsWith("Mapper")) {
                Interface interfaze = (Interface) compilationUnit;

                Assert.assertEquals(interfaze.getAnnotations().size(), 2);
                Assert.assertEquals(interfaze.getAnnotations().get(0), "@Mapper");
                Assert.assertEquals(interfaze.getAnnotations().get(1), "@Repository");
                Assert.assertTrue(interfaze.getImportedTypes().contains(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Mapper")));
                Assert.assertTrue(interfaze.getImportedTypes().contains(new FullyQualifiedJavaType("org.springframework.stereotype.Repository")));
            }
        }
    }

}