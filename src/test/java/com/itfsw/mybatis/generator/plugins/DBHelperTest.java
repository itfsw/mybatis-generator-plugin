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

package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.tools.DBHelper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.sql.*;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/6/26 17:19
 * ---------------------------------------------------------------------------
 */
public class DBHelperTest {
    /**
     * 测试 getSqlSession
     *
     * @throws IOException
     * @throws SQLException
     */
    @Test
    public void testGetSqlSession() throws Exception {
        DBHelper.createDB("scripts/test_init.sql");

        String driver = DBHelper.properties.getProperty("driver");
        String url = DBHelper.properties.getProperty("url");
        String username = DBHelper.properties.getProperty("username");
        String password = DBHelper.properties.getProperty("password");
        // 获取connection
        Class.forName(driver);
        Connection connection = DriverManager.getConnection(url, username, password);

        // 执行查询
        ResultSet resultSet = DBHelper.execute(connection, "SELECT COUNT(*) as total FROM tb");

        resultSet.first();
        Assert.assertEquals(resultSet.getInt("total"), 4);

        connection.close();
    }
}
