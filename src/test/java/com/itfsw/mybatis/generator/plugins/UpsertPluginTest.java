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

import com.itfsw.mybatis.generator.plugins.tools.*;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/7/31 14:45
 * ---------------------------------------------------------------------------
 */
public class UpsertPluginTest {
    /**
     * 初始化数据库
     */
    @BeforeClass
    public static void init() throws SQLException, IOException, ClassNotFoundException {
        DBHelper.createDB("scripts/UpsertPlugin/init.sql");
    }

    /**
     * 测试配置异常
     */
    @Test
    public void testWarnings() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        // 1. 没有使用mysql数据库
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/UpsertPlugin/mybatis-generator-with-error-driver.xml");
        tool.generate();
        Assert.assertEquals(tool.getWarnings().get(1), "itfsw:插件com.itfsw.mybatis.generator.plugins.UpsertPlugin插件使用前提是数据库为MySQL！");

        // 2. 普通提示
        tool = MyBatisGeneratorTool.create("scripts/UpsertPlugin/mybatis-generator.xml");
        tool.generate();
        Assert.assertEquals(tool.getWarnings().get(0), "itfsw:插件com.itfsw.mybatis.generator.plugins.UpsertPlugin插件您开启了allowMultiQueries支持，注意在jdbc url 配置中增加“allowMultiQueries=true”支持（不怎么建议使用该功能，开启多sql提交会增加sql注入的风险，请确保你所有sql都使用MyBatis书写，请不要使用statement进行sql提交）！");
    }

    /**
     * 测试 upsert
     */
    @Test
    public void testUpsert() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/UpsertPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tb = new ObjectUtil(loader, packagz + ".Tb");
                tb.set("id", 10l);
                tb.set("field1", "ts1");
                tb.set("field2", 5);

                // sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "upsertSelective", tb.getObject());
                Assert.assertEquals(sql, "insert into tb ( id, field1, field2 )  values ( 10, 'ts1', 5 )  on duplicate key update  id = 10, field1 = 'ts1', field2 = 5");
                Object result = tbMapper.invoke("upsertSelective", tb.getObject());
                Assert.assertEquals(result, 1);

                tb.set("field2", 20);
                tbMapper.invoke("upsertSelective", tb.getObject());

                ResultSet rs = DBHelper.execute(sqlSession, "select * from tb where id = 10");
                rs.first();
                Assert.assertEquals(rs.getInt("field2"), 20);
            }
        });
    }
}
