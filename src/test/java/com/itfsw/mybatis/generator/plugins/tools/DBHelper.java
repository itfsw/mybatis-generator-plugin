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

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/6/26 16:52
 * ---------------------------------------------------------------------------
 */
public class DBHelper {
    private static final String MYBATIS_CONFIG = "mybatis-config.xml";    // 配置文件
    private static DBHelper helper; // helper

    /**
     * 构造函数
     */
    private DBHelper() {
    }

    /**
     * 获取数据库操作工具
     *
     * @param initSql
     * @return
     */
    public static DBHelper getHelper(String initSql) throws IOException, SQLException {
        if (helper == null){
            helper = new DBHelper();
            helper.initDB(initSql);
        }
        cleanDao();
        return helper;
    }

    /**
     * 获取SqlSession
     *
     * @return
     * @throws IOException
     */
    public SqlSession getSqlSession() throws IOException {
        InputStream inputStream = Resources.getResourceAsStream(MYBATIS_CONFIG);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        inputStream.close();
        return sqlSessionFactory.openSession(true);
    }

    /**
     * 初始化数据库
     *
     * @param initSql
     * @throws IOException
     * @throws SQLException
     */
    private void initDB(String initSql) throws IOException, SQLException {
        SqlSession sqlSession = this.getSqlSession();
        Connection connection = sqlSession.getConnection();
        Statement statement = connection.createStatement();
        // 获取建表和初始化sql
        InputStream inputStream = Resources.getResourceAsStream(initSql);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        // 读取sql语句执行
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = bufferedReader.readLine()) != null){
            sb.append(line).append("\n");
            if (line.matches(".*;$")){
                statement.execute(sb.toString());
                sb.setLength(0);
            }
        }
        bufferedReader.close();
        inputStreamReader.close();
        inputStream.close();
        statement.close();
        connection.close();
        sqlSession.close();
    }

    /**
     * 重置
     */
    public static void reset(){
        helper = null;
        cleanDao();
    }

    /**
     * 清理Dao空间
     */
    public static void cleanDao(){
        delDir(new File("src/test/java/com/itfsw/mybatis/generator/plugins/dao"));
    }

    /**
     * 清理工作区间
     *
     * @param file
     */
    private static void delDir(File file) {
        if (file.exists()){
            if (file.isFile()){
                file.delete();
            } else if (file.isDirectory()){
                File[] files = file.listFiles();
                for (File file1: files) {
                    delDir(file1);
                }

                file.delete();
            }
        }
    }
}
