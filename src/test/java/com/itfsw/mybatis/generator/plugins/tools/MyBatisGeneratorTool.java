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

package com.itfsw.mybatis.generator.plugins.tools;

import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;

import javax.sql.DataSource;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/7/4 16:14
 * ---------------------------------------------------------------------------
 */
public class MyBatisGeneratorTool {
    public final static String DAO_PACKAGE = "com.itfsw.mybatis.generator.plugins.dao";    // dao package
    private List<String> warnings;  // 提示信息
    private Configuration config;   // 配置信息
    private String targetProject;  // 目标
    private String targetPackage; // package

    /**
     * 创建
     * @param resource
     * @return
     */
    public static MyBatisGeneratorTool create(String resource) throws IOException, XMLParserException {
        MyBatisGeneratorTool tool = new MyBatisGeneratorTool();
        tool.warnings = new ArrayList<>();

        // MyBatisGenerator 创建
        ConfigurationParser cp = new ConfigurationParser(tool.warnings);
        tool.config = cp.parseConfiguration(Resources.getResourceAsStream(resource));
        // 修正配置目标
        tool.fixConfigToTarget();
        return tool;
    }

    /**
     * 执行MyBatisGenerator
     * @param before
     * @param callback
     * @return
     * @throws SQLException
     * @throws IOException
     * @throws InterruptedException
     */
    public MyBatisGenerator generate(IBeforeCallback before, AbstractShellCallback callback) throws Exception {
        before.run();
        callback.setTool(this);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null, null, null, true);
        return myBatisGenerator;
    }

    /**
     * 执行MyBatisGenerator
     * @param callback
     * @return
     * @throws SQLException
     * @throws IOException
     * @throws InterruptedException
     */
    public MyBatisGenerator generate(AbstractShellCallback callback) throws Exception {
       return this.generate(() -> {

       }, callback);
    }

    /**
     * 执行MyBatisGenerator
     * @param before
     * @return
     * @throws SQLException
     * @throws IOException
     * @throws InterruptedException
     */
    public MyBatisGenerator generate(IBeforeCallback before) throws Exception {
        before.run();
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, new DefaultShellCallback(true), warnings);
        myBatisGenerator.generate(null, null, null, false);
        return myBatisGenerator;
    }

    /**
     * 执行MyBatisGenerator(不生成文件)
     * @return
     * @throws SQLException
     * @throws IOException
     * @throws InterruptedException
     */
    public MyBatisGenerator generate() throws InvalidConfigurationException, InterruptedException, SQLException, IOException {
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, new DefaultShellCallback(true), warnings);
        myBatisGenerator.generate(null, null, null, false);
        return myBatisGenerator;
    }

    /**
     * 编译项目并返回 SqlSession
     * @return
     */
    public SqlSession compile() throws IOException, ClassNotFoundException {
        // 动态编译java文件
        String target = targetProject + targetPackage.replaceAll("\\.", "/");
        List<File> javaFiles = getGeneratedFiles(new File(target), ".java");
        compileJavaFiles(javaFiles);
        return getSqlSession();
    }

    /**
     * 获取目标目录的ClassLoader
     * @return
     */
    public ClassLoader getTargetClassLoader() throws MalformedURLException {
        return URLClassLoader.newInstance(new URL[]{
                new File(targetProject).toURI().toURL()
        });
    }

    /**
     * 获取SqlSession
     * @return
     * @throws IOException
     */
    public SqlSession getSqlSession() throws IOException, ClassNotFoundException {
        org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration();
        config.setCallSettersOnNulls(true); // 设计null调用setter方法
        config.setMapUnderscoreToCamelCase(true);   // 驼峰命名支持

        // 设置mapper
        config.addMappers(targetPackage);
        // 设置数据源，事务
        PooledDataSourceFactory dataSourceFactory = new PooledDataSourceFactory();
        dataSourceFactory.setProperties(DBHelper.properties);
        DataSource dataSource = dataSourceFactory.getDataSource();
        JdbcTransactionFactory transactionFactory = new JdbcTransactionFactory();

        Environment environment = new Environment("test", transactionFactory, dataSource);
        config.setEnvironment(environment);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
        return sqlSessionFactory.openSession(true);
    }

    /**
     * 动态编译java文件
     * @param files
     */
    private void compileJavaFiles(List<File> files) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        //获取java文件管理类
        StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null);
        //获取java文件对象迭代器
        Iterable<? extends JavaFileObject> it = manager.getJavaFileObjectsFromFiles(files);
        //设置编译参数
        ArrayList<String> ops = new ArrayList<>();
        ops.add("-Xlint:unchecked");
        //获取编译任务
        JavaCompiler.CompilationTask task = compiler.getTask(null, manager, null, ops, null, it);
        //执行编译任务
        task.call();
    }

    /**
     * 获取指定后缀的文件
     * @param file
     * @return
     */
    private List<File> getGeneratedFiles(File file, String ext) {
        List<File> list = new ArrayList<>();
        if (file.exists()) {
            File[] files = file.listFiles();
            for (File childFile : files) {
                if (childFile.isDirectory()) {
                    list.addAll(getGeneratedFiles(childFile, ext));
                } else if (childFile.getName().endsWith(ext)) {
                    list.add(childFile);
                }
            }
        }
        return list;
    }

    /**
     * 修正配置到指定target
     */
    private void fixConfigToTarget() {
        this.targetProject = this.getClass().getClassLoader().getResource("").getPath();
        this.targetPackage = DAO_PACKAGE + ".s" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        for (Context context : config.getContexts()) {
            context.getJavaModelGeneratorConfiguration().setTargetProject(targetProject);
            context.getJavaModelGeneratorConfiguration().setTargetPackage(targetPackage);
            context.getSqlMapGeneratorConfiguration().setTargetProject(targetProject);
            context.getSqlMapGeneratorConfiguration().setTargetPackage(targetPackage);
            context.getJavaClientGeneratorConfiguration().setTargetProject(targetProject);
            context.getJavaClientGeneratorConfiguration().setTargetPackage(targetPackage);
        }
    }

    /**
     * Getter method for property <tt>warnings</tt>.
     * @return property value of warnings
     * @author hewei
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * Getter method for property <tt>config</tt>.
     * @return property value of config
     * @author hewei
     */
    public Configuration getConfig() {
        return config;
    }

    /**
     * Getter method for property <tt>targetPackage</tt>.
     * @return property value of targetPackage
     * @author hewei
     */
    public String getTargetPackage() {
        return targetPackage;
    }
}
