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

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import org.mybatis.generator.api.IntrospectedTable;

/**
 * ---------------------------------------------------------------------------
 * table前缀，解决多数据源表重名问题插件（为Model、Mapper、Example、xml等增加前缀）
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/5/18 13:54
 * ---------------------------------------------------------------------------
 */
public class TableSuffixPlugin extends BasePlugin {

    public static final String PRE_SUFFIX = "suffix";  // 前缀 property
    private String suffix;  // 前缀

    /**
     * 初始化阶段
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param introspectedTable
     * @return
     */
    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        // 1. 首先获取全局配置
        this.suffix = getProperties().getProperty(PRE_SUFFIX);
        // 2. 获取每个table 具体的
        if (introspectedTable.getTableConfigurationProperty(PRE_SUFFIX) != null){
            this.suffix = introspectedTable.getTableConfigurationProperty(PRE_SUFFIX);
        }
        // 3. 判断是否配置了前缀
        if (this.suffix != null){
            // 3.1. 为Model增加前缀
            if (introspectedTable.getBaseRecordType() != null){
                introspectedTable.setBaseRecordType(this.renameJavaType(introspectedTable.getBaseRecordType()));
            }

            // 3.2. 为ModelKey添加前缀
            if (introspectedTable.getPrimaryKeyType() != null){
                introspectedTable.setPrimaryKeyType(this.renameJavaType(introspectedTable.getPrimaryKeyType()));
            }

            // 3.3. WithBLOBs Model 添加前缀
            if (introspectedTable.getRecordWithBLOBsType() != null){
                introspectedTable.setRecordWithBLOBsType(this.renameJavaType(introspectedTable.getRecordWithBLOBsType()));
            }

            // 3.4. mapper 添加前缀
            if (introspectedTable.getMyBatis3JavaMapperType() != null){
                introspectedTable.setMyBatis3JavaMapperType(this.renameJavaType(introspectedTable.getMyBatis3JavaMapperType()));
            }

            // 3.5. example 添加前缀
            if (introspectedTable.getExampleType() != null){
                introspectedTable.setExampleType(this.renameJavaType(introspectedTable.getExampleType()));
            }

            // 3.6. 修正xml文件前缀
            if (introspectedTable.getMyBatis3XmlMapperFileName() != null){
                introspectedTable.setMyBatis3XmlMapperFileName(this.suffix + introspectedTable.getMyBatis3XmlMapperFileName());
            }
        }
    }

    /**
     * 为类型添加前缀
     *
     * @param type
     * @return
     */
    private String renameJavaType(String type){
        int lastDot = type.lastIndexOf(".") + 1;
        return type.substring(0, lastDot) + this.suffix + type.substring(lastDot);
    }
}
