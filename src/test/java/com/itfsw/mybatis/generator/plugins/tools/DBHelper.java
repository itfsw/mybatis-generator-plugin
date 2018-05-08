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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Properties;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/6/26 16:52
 * ---------------------------------------------------------------------------
 */
public class DBHelper {
    private static final String DB_CONFIG = "db.properties";
    public static Properties properties; // 数据库信息
    private static Connection connection;   // 数据库连接
    private static String dbLock;

    static {
        try {
            // 获取数据库配置信息
            properties = new Properties();
            try (InputStream inputStream = Resources.getResourceAsStream(DB_CONFIG)) {
                properties.load(inputStream);
            }
            // 数据库连接
            String driver = properties.getProperty("driver");
            String url = properties.getProperty("url");
            String username = properties.getProperty("username");
            String password = properties.getProperty("password");
            Class.forName(driver);
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建数据库
     * @param resource
     * @throws SQLException
     * @throws IOException
     */
    public static void createDB(String resource) throws SQLException, IOException {
        try (
                Statement statement = connection.createStatement();
                // 获取建表和初始化sql
                InputStream inputStream = Resources.getResourceAsStream(resource);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        ) {
            // 读取sql语句执行
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.startsWith("--")) {
                    sb.append(line).append("\n");
                }
            }
            statement.execute(sb.toString());

            dbLock = resource;
        }
    }

    /**
     * 重置数据库
     * @param resource
     * @throws SQLException
     * @throws IOException
     */
    public static void resetDB(String resource) throws Exception {
        if (dbLock == null || !dbLock.equals(resource)) {
            throw new Exception("重置数据库只能重置已锁定的！");
        }

        try (
                Statement statement = connection.createStatement();
                // 获取建表和初始化sql
                InputStream inputStream = Resources.getResourceAsStream(resource);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        ) {
            // 读取sql语句执行
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.startsWith("--")) {
                    sb.append(line).append("\n");

                    if (line.matches(".*;$\\s*")) {
                        String sql = sb.toString().trim();

                        if (sql.startsWith("DROP")) {
                            statement.execute(sql.replace("DROP TABLE IF EXISTS", "TRUNCATE TABLE"));
                        } else if (!sql.startsWith("CREATE")) {
                            statement.execute(sql);
                        }

                        sb.setLength(0);
                    }
                }
            }
        }
    }

    /**
     * 执行sql
     * @param sqlSession
     * @param sql
     * @return
     * @throws SQLException
     */
    public static ResultSet execute(SqlSession sqlSession, String sql) throws SQLException {
        return execute(sqlSession.getConnection(), sql);
    }

    /**
     * 执行sql
     * @param connection
     * @param sql
     * @return
     * @throws SQLException
     */
    public static ResultSet execute(Connection connection, String sql) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute(sql);
        ResultSet resultSet = statement.getResultSet();
        return resultSet;
    }

}
