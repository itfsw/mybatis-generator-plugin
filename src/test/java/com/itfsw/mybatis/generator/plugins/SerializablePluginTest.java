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
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

public class SerializablePluginTest {

    @BeforeClass
    public static void init() throws Exception {
        DBHelper.createDB("scripts/LombokPlugin/init.sql");
    }

    @Test
    public void testGenerate() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/SerializablePlugin/mybatis-generator.xml");
        MyBatisGenerator myBatisGenerator = tool.generate();

        for (GeneratedJavaFile file : myBatisGenerator.getGeneratedJavaFiles()) {
            CompilationUnit compilationUnit = file.getCompilationUnit();
            if (compilationUnit instanceof TopLevelClass) {
                TopLevelClass topLevelClass = (TopLevelClass) compilationUnit;
                String name = topLevelClass.getType().getShortName();
                if ("Tb".equals(name) || "TbBlobs".equals(name)) {
                    Set<FullyQualifiedJavaType> superInterfaceTypes = compilationUnit.getSuperInterfaceTypes();
                    boolean match = superInterfaceTypes.stream()
                            .anyMatch(f -> f.getFullyQualifiedName().equals("java.io.Serializable"));
                    Assert.assertTrue(match);
                }
            }

        }
    }

    @Test
    public void testSerialize() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/SerializablePlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                // get & set
                ObjectUtil objectUtil = new ObjectUtil(loader, packagz + ".Tb");
                objectUtil.invoke("setId", 1l);
                Object object = objectUtil.getObject();
                String path = loader.getResource("").getPath();
                String target = packagz.replaceAll("\\.", "/");
                String fileName = path + target + "/tbSerialize";
                try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(fileName))) {
                    objectOutputStream.writeObject(object);
                }
                try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(fileName))) {
                    Object readObject = objectInputStream.readObject();
                    ObjectUtil objectUtil1 = new ObjectUtil(readObject);
                    Object id = objectUtil1.invoke("getId");
                    Assert.assertEquals(id, 1L);
                }
            }
        });
    }

}