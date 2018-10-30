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
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
    public static void init() throws Exception {
        DBHelper.createDB("scripts/LombokPlugin/init.sql");
    }

    /**
     * 测试具体生成
     */
    @Test
    public void testGenerate() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/LombokPlugin/mybatis-generator.xml");
        MyBatisGenerator myBatisGenerator = tool.generate();
        for (GeneratedJavaFile file : myBatisGenerator.getGeneratedJavaFiles()) {
            CompilationUnit compilationUnit = file.getCompilationUnit();
            String name = compilationUnit.getType().getShortName();
            if ("TbKeyBlobKey".equals(name)) {
                Assert.assertTrue(checkImportedType(compilationUnit.getImportedTypes(), Arrays.asList(
                        LombokPlugin.EnumLombokAnnotations.DATA,
                        LombokPlugin.EnumLombokAnnotations.SUPER_BUILDER,
                        LombokPlugin.EnumLombokAnnotations.NO_ARGS_CONSTRUCTOR,
                        LombokPlugin.EnumLombokAnnotations.ALL_ARGS_CONSTRUCTOR
                ), true));

                Assert.assertFalse(checkImportedType(compilationUnit.getImportedTypes(), Arrays.asList(
                        LombokPlugin.EnumLombokAnnotations.BUILDER,
                        LombokPlugin.EnumLombokAnnotations.EQUALS_AND_HASH_CODE_CALL_SUPER
                ), false));
            } else if ("TbKeyBlobWithBLOBs".equals(name)) {
                Assert.assertTrue(checkImportedType(compilationUnit.getImportedTypes(), Arrays.asList(
                        LombokPlugin.EnumLombokAnnotations.DATA,
                        LombokPlugin.EnumLombokAnnotations.SUPER_BUILDER,
                        LombokPlugin.EnumLombokAnnotations.NO_ARGS_CONSTRUCTOR,
                        LombokPlugin.EnumLombokAnnotations.ALL_ARGS_CONSTRUCTOR,
                        LombokPlugin.EnumLombokAnnotations.EQUALS_AND_HASH_CODE_CALL_SUPER
                ), true));

                Assert.assertFalse(checkImportedType(compilationUnit.getImportedTypes(), Arrays.asList(
                        LombokPlugin.EnumLombokAnnotations.BUILDER
                ), false));
            }

            // tb 没有继承
            if ("Tb".equals(name)) {
                Assert.assertTrue(checkImportedType(compilationUnit.getImportedTypes(), Arrays.asList(
                        LombokPlugin.EnumLombokAnnotations.DATA,
                        LombokPlugin.EnumLombokAnnotations.BUILDER,
                        LombokPlugin.EnumLombokAnnotations.NO_ARGS_CONSTRUCTOR,
                        LombokPlugin.EnumLombokAnnotations.ALL_ARGS_CONSTRUCTOR
                ), true));

                Assert.assertFalse(checkImportedType(compilationUnit.getImportedTypes(), Arrays.asList(
                        LombokPlugin.EnumLombokAnnotations.SUPER_BUILDER,
                        LombokPlugin.EnumLombokAnnotations.EQUALS_AND_HASH_CODE_CALL_SUPER
                ), false));
            }

            if ("TbKeysKey".equals(name)) {
                Assert.assertTrue(checkImportedType(compilationUnit.getImportedTypes(), Arrays.asList(
                        LombokPlugin.EnumLombokAnnotations.DATA,
                        LombokPlugin.EnumLombokAnnotations.SUPER_BUILDER,
                        LombokPlugin.EnumLombokAnnotations.NO_ARGS_CONSTRUCTOR,
                        LombokPlugin.EnumLombokAnnotations.ALL_ARGS_CONSTRUCTOR
                ), true));

                Assert.assertFalse(checkImportedType(compilationUnit.getImportedTypes(), Arrays.asList(
                        LombokPlugin.EnumLombokAnnotations.BUILDER,
                        LombokPlugin.EnumLombokAnnotations.EQUALS_AND_HASH_CODE_CALL_SUPER
                ), false));
            } else if ("TbKeys".equals(name)) {
                Assert.assertTrue(checkImportedType(compilationUnit.getImportedTypes(), Arrays.asList(
                        LombokPlugin.EnumLombokAnnotations.DATA,
                        LombokPlugin.EnumLombokAnnotations.SUPER_BUILDER,
                        LombokPlugin.EnumLombokAnnotations.NO_ARGS_CONSTRUCTOR,
                        LombokPlugin.EnumLombokAnnotations.ALL_ARGS_CONSTRUCTOR,
                        LombokPlugin.EnumLombokAnnotations.EQUALS_AND_HASH_CODE_CALL_SUPER
                ), true));

                Assert.assertFalse(checkImportedType(compilationUnit.getImportedTypes(), Arrays.asList(
                        LombokPlugin.EnumLombokAnnotations.BUILDER
                ), false));
            }
        }
    }

    /**
     * 测试具体生成
     */
    @Test
    public void testGenerateDefault() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/LombokPlugin/mybatis-generator-default.xml");
        MyBatisGenerator myBatisGenerator = tool.generate();
        for (GeneratedJavaFile file : myBatisGenerator.getGeneratedJavaFiles()) {
            CompilationUnit compilationUnit = file.getCompilationUnit();
            String name = compilationUnit.getType().getShortName();
            if ("TbKeyBlobKey".equals(name)) {
                Assert.assertTrue(checkImportedType(compilationUnit.getImportedTypes(), Arrays.asList(
                        LombokPlugin.EnumLombokAnnotations.DATA

                ), true));

                Assert.assertFalse(checkImportedType(compilationUnit.getImportedTypes(), Arrays.asList(
                        LombokPlugin.EnumLombokAnnotations.BUILDER,
                        LombokPlugin.EnumLombokAnnotations.EQUALS_AND_HASH_CODE_CALL_SUPER,
                        LombokPlugin.EnumLombokAnnotations.SUPER_BUILDER,
                        LombokPlugin.EnumLombokAnnotations.NO_ARGS_CONSTRUCTOR,
                        LombokPlugin.EnumLombokAnnotations.ALL_ARGS_CONSTRUCTOR
                ), false));
            } else if ("TbKeyBlobWithBLOBs".equals(name)) {
                Assert.assertTrue(checkImportedType(compilationUnit.getImportedTypes(), Arrays.asList(
                        LombokPlugin.EnumLombokAnnotations.DATA,
                        LombokPlugin.EnumLombokAnnotations.EQUALS_AND_HASH_CODE_CALL_SUPER
                ), true));

                Assert.assertFalse(checkImportedType(compilationUnit.getImportedTypes(), Arrays.asList(
                        LombokPlugin.EnumLombokAnnotations.BUILDER,
                        LombokPlugin.EnumLombokAnnotations.SUPER_BUILDER,
                        LombokPlugin.EnumLombokAnnotations.NO_ARGS_CONSTRUCTOR,
                        LombokPlugin.EnumLombokAnnotations.ALL_ARGS_CONSTRUCTOR

                ), false));
            }

            // tb 没有继承
            if ("Tb".equals(name)) {
                Assert.assertTrue(checkImportedType(compilationUnit.getImportedTypes(), Arrays.asList(
                        LombokPlugin.EnumLombokAnnotations.DATA
                ), true));

                Assert.assertFalse(checkImportedType(compilationUnit.getImportedTypes(), Arrays.asList(
                        LombokPlugin.EnumLombokAnnotations.SUPER_BUILDER,
                        LombokPlugin.EnumLombokAnnotations.EQUALS_AND_HASH_CODE_CALL_SUPER,
                        LombokPlugin.EnumLombokAnnotations.BUILDER,
                        LombokPlugin.EnumLombokAnnotations.NO_ARGS_CONSTRUCTOR,
                        LombokPlugin.EnumLombokAnnotations.ALL_ARGS_CONSTRUCTOR
                ), false));
            }
        }
    }

    /**
     * 测试 @Data 注解
     * @throws Exception
     */
    @Test
    public void testDataAnnotation() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/LombokPlugin/mybatis-generator-data.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {

                // get & set
                ObjectUtil tbLombok = new ObjectUtil(loader, packagz + ".TbLombok");
                tbLombok.invoke("setField1", "ts1");
                Assert.assertEquals(tbLombok.invoke("getField1"), "ts1");
                // get & set with parent
                tbLombok.invoke("setKey1", "ts2");
                Assert.assertEquals(tbLombok.invoke("getKey1"), "ts2");
                // get & set with Boolean
                tbLombok.invoke("setIsFind", true);
                Assert.assertEquals(tbLombok.invoke("getIsFind"), true);

                // equals & hash & toString
                ObjectUtil tbLombok1 = new ObjectUtil(loader, packagz + ".TbLombok");
                tbLombok1.invoke("setField1", "ts1");
                ObjectUtil tbLombok2 = new ObjectUtil(loader, packagz + ".TbLombok");
                tbLombok2.invoke("setField1", "ts1");
                Assert.assertEquals(tbLombok1.invoke("equals", tbLombok2.getObject()), true);
                Assert.assertEquals(tbLombok1.invoke("hashCode"), tbLombok2.invoke("hashCode"));
                Assert.assertEquals(tbLombok1.invoke("toString"), "TbLombok(super=TbLombokKey(id=null, key1=null), field1=ts1, isFind=null)");

                // equals & hash & toString with parent
                tbLombok1.invoke("setKey1", "ts2");
                tbLombok2.invoke("setKey1", "ts2");
                Assert.assertEquals(tbLombok1.invoke("equals", tbLombok2.getObject()), true);
                Assert.assertEquals(tbLombok1.invoke("hashCode"), tbLombok2.invoke("hashCode"));
                Assert.assertEquals(tbLombok1.invoke("toString"), "TbLombok(super=TbLombokKey(id=null, key1=ts2), field1=ts1, isFind=null)");
                // @EqualsAndHashCode(callSuper = true) @ToString(callSuper = true)
                tbLombok1.invoke("setId", 1L);
                Assert.assertEquals(tbLombok1.invoke("equals", tbLombok2.getObject()), false);
                Assert.assertNotEquals(tbLombok1.invoke("hashCode"), tbLombok2.invoke("hashCode"));
                Assert.assertEquals(tbLombok1.invoke("toString"), "TbLombok(super=TbLombokKey(id=1, key1=ts2), field1=ts1, isFind=null)");
            }
        });
    }

    /**
     * 测试 @Builder 注解
     * @throws Exception
     */
    @Test
    public void testBuilderAnnotation() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/LombokPlugin/mybatis-generator-builder.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // normal builder
                ObjectUtil tbBuilder = new ObjectUtil(loader.loadClass(packagz + ".Tb").getMethod("builder").invoke(null));
                tbBuilder = new ObjectUtil(tbBuilder.invoke("id", 1L));
                tbBuilder.invoke("field1", "ts1");
                ObjectUtil tb = new ObjectUtil(tbBuilder.invoke("build"));
                Assert.assertEquals(tb.invoke("toString"), "Tb(id=1, field1=ts1, field2=null)");

                // super builder
                ObjectUtil tbLombokBuilder = new ObjectUtil(loader.loadClass(packagz + ".TbLombok").getMethod("builder").invoke(null));
                tbLombokBuilder.invoke("field1", "ts1");
                Assert.assertEquals(tbLombokBuilder.invoke("toString"), "TbLombok.TbLombokBuilder(super=TbLombokKey.TbLombokKeyBuilder(id=null, key1=null), field1=ts1, isFind=null)");
                tbLombokBuilder.invoke("id", 100L);
                Assert.assertEquals(tbLombokBuilder.invoke("toString"), "TbLombok.TbLombokBuilder(super=TbLombokKey.TbLombokKeyBuilder(id=100, key1=null), field1=ts1, isFind=null)");
            }
        });
    }

    /**
     * 测试 @NoArgsConstructor @AllArgsConstructor 注解
     * @throws Exception
     */
    @Test
    public void testConstructorAnnotation() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/LombokPlugin/mybatis-generator-constructor.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                Class clazz = loader.loadClass(packagz + ".TbLombok");
                try {
                    // 无参
                    ObjectUtil tbLombok = new ObjectUtil(clazz.newInstance());
                    Assert.assertEquals(tbLombok.invoke("toString"), "TbLombok(super=TbLombokKey(id=null, key1=null), field1=null, isFind=null)");

                    // 有参
                    Constructor constructor = clazz.getConstructor(String.class, Boolean.class);

                    tbLombok = new ObjectUtil(constructor.newInstance("ts1", true));
                    Assert.assertEquals(tbLombok.invoke("toString"), "TbLombok(super=TbLombokKey(id=null, key1=null), field1=ts1, isFind=true)");

                } catch (Exception e) {
                    Assert.assertTrue(false);
                }
            }
        });
    }

    /**
     * 验证引包
     * @param importedTypes
     * @param annotations
     * @param all
     * @return
     */
    private boolean checkImportedType(Set<FullyQualifiedJavaType> importedTypes, List<LombokPlugin.EnumLombokAnnotations> annotations, boolean all) {
        for (LombokPlugin.EnumLombokAnnotations annotation : annotations) {
            boolean find = false;
            for (FullyQualifiedJavaType javaType : importedTypes) {
                if (javaType.getFullyQualifiedName().equals(annotation.getClazz())) {
                    if (all) {
                        find = true;
                    } else {
                        return true;
                    }
                }
            }
            if (find == false) {
                return false;
            }
        }

        return true;
    }
}
