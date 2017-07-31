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

    static {
        try {
            // 获取数据库配置信息
            properties = new Properties();
            InputStream inputStream = Resources.getResourceAsStream(DB_CONFIG);
            properties.load(inputStream);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建数据库
     * @param resource
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws IOException
     */
    public static void createDB(String resource) throws ClassNotFoundException, SQLException, IOException {
        String driver = properties.getProperty("driver");
        String url = properties.getProperty("url");
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");
        // 获取connection
        Class.forName(driver);
        Connection connection = DriverManager.getConnection(url, username, password);

        Statement statement = connection.createStatement();
        // 获取建表和初始化sql
        InputStream inputStream = Resources.getResourceAsStream(resource);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        // 读取sql语句执行
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line).append("\n");
            if (line.matches(".*;$")) {
                statement.execute(sb.toString());
                sb.setLength(0);
            }
        }
        bufferedReader.close();
        inputStreamReader.close();
        inputStream.close();
        statement.close();
        connection.close();
    }

    /**
     * 执行sql
     *
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
     *
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
