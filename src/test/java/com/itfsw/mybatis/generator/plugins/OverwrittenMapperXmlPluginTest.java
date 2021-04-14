package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.tools.DBHelper;
import com.itfsw.mybatis.generator.plugins.tools.MyBatisGeneratorTool;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mybatis.generator.api.MyBatisGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

public class OverwrittenMapperXmlPluginTest {

    private static String packageDir;

    @BeforeClass
    public static void init() throws Exception {
        DBHelper.createDB("scripts/OverwrittenMapperXmlPlugin/init.sql");
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/OverwrittenMapperXmlPlugin/mybatis-generator.xml");
        MyBatisGenerator generate = tool.generateAndWriteFiles("tmp");
        packageDir = tool.getTargetPackage();
    }


    @Test
    public void test() throws Exception {
        String path = this.getClass().getClassLoader().getResource("").getPath();

        String target = packageDir.replaceAll("\\.", "/");
        String filePath = path + target + "/TbMapper.xml";
        File file = new File(filePath);

        BasicFileAttributes fileAttributes = Files.readAttributes(file.toPath(),
                BasicFileAttributes.class);
        // 文件创建时间
        long millis = fileAttributes.creationTime().toMillis();
        // 文件MD5
        String md5 = "";
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            md5 = DigestUtils.md5Hex(fileInputStream);
        }
        // 重新生成文件
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/OverwrittenMapperXmlPlugin/mybatis-generator.xml");
        MyBatisGenerator generate = tool.generateAndWriteFiles("tmp");
        File newFile = new File(filePath);

        BasicFileAttributes newFileAttributes = Files.readAttributes(file.toPath(),
                BasicFileAttributes.class);
        // 文件创建时间
        long newMillis = newFileAttributes.creationTime().toMillis();
        // 文件MD5
        String nMd5 = "";
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            nMd5 = DigestUtils.md5Hex(fileInputStream);
        }
        // 创建时间不同
        Assert.assertTrue(newMillis != millis);
        // MD5值相同
        Assert.assertTrue(nMd5.equals(md5) && !"".equals(md5));
    }

}