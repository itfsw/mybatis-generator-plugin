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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ---------------------------------------------------------------------------
 * table 重命名插件
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/6/6 15:18
 * ---------------------------------------------------------------------------
 */
public class TableRenamePlugin extends BasePlugin {
    public static final String PRE_SEARCH_STRING = "searchString";  // 查找 property
    public static final String PRE_REPLACE_STRING = "replaceString";  // 替换 property
    public static final String PRE_TABLE_OVERRIDE = "tableOverride";   // table 重命名 property

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(List<String> warnings) {

        // 如果配置了searchString 或者 replaceString，二者不允许单独存在
        if ((getProperties().getProperty(PRE_SEARCH_STRING) == null && getProperties().getProperty(PRE_REPLACE_STRING) != null)
                || (getProperties().getProperty(PRE_SEARCH_STRING) != null && getProperties().getProperty(PRE_REPLACE_STRING) == null)) {
            logger.warn("itfsw:插件" + this.getClass().getTypeName() + "插件的searchString、replaceString属性需配合使用，不能单独存在！");
            return false;
        }

        return super.validate(warnings);
    }

    /**
     * 初始化阶段
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param introspectedTable
     * @return
     */
    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        // 1. 获取表单独配置
        if (introspectedTable.getTableConfigurationProperty(PRE_TABLE_OVERRIDE) != null) {
            String override = introspectedTable.getTableConfigurationProperty(PRE_TABLE_OVERRIDE);

            // 3.1. Model修正名称
            if (introspectedTable.getBaseRecordType() != null){
                introspectedTable.setBaseRecordType(this.renameJavaType(introspectedTable.getBaseRecordType(), override, ""));
            }

            // 3.2. ModelKey修正名称
            if (introspectedTable.getPrimaryKeyType() != null){
                introspectedTable.setPrimaryKeyType(this.renameJavaType(introspectedTable.getPrimaryKeyType(), override, "Key"));
            }

            // 3.3. WithBLOBs Model 修正名称
            if (introspectedTable.getRecordWithBLOBsType() != null){
                introspectedTable.setRecordWithBLOBsType(this.renameJavaType(introspectedTable.getRecordWithBLOBsType(), override, "WithBLOBs"));
            }

            // 3.4. mapper 修正名称
            if (introspectedTable.getMyBatis3JavaMapperType() != null){
                introspectedTable.setMyBatis3JavaMapperType(this.renameJavaType(introspectedTable.getMyBatis3JavaMapperType(), override, "Mapper"));
            }

            // 3.5. example 修正名称
            if (introspectedTable.getExampleType() != null){
                introspectedTable.setExampleType(this.renameJavaType(introspectedTable.getExampleType(), override, "Example"));
            }

            // 3.6. Dao 添加前缀
            if (introspectedTable.getDAOInterfaceType() != null){
                introspectedTable.setDAOInterfaceType(this.renameJavaType(introspectedTable.getDAOInterfaceType(), override, "Dao"));
            }

            // 3.7. DAOImpl 添加前缀
            if (introspectedTable.getDAOImplementationType() != null){
                introspectedTable.setDAOImplementationType(this.renameJavaType(introspectedTable.getDAOImplementationType(), override, "DAOImpl"));
            }

            // 3.8. 修正xml文件名称
            if (introspectedTable.getMyBatis3XmlMapperFileName() != null){
                introspectedTable.setMyBatis3XmlMapperFileName(override + "Mapper.xml");
            }
        } else if (getProperties().getProperty(PRE_SEARCH_STRING) != null){
            String searchString = getProperties().getProperty(PRE_SEARCH_STRING);
            String replaceString = getProperties().getProperty(PRE_REPLACE_STRING);

            // 3.1. Model修正名称
            if (introspectedTable.getBaseRecordType() != null){
                introspectedTable.setBaseRecordType(this.renameJavaType(introspectedTable.getBaseRecordType(), searchString, replaceString, ""));
            }

            // 3.2. ModelKey修正名称
            if (introspectedTable.getPrimaryKeyType() != null){
                introspectedTable.setPrimaryKeyType(this.renameJavaType(introspectedTable.getPrimaryKeyType(), searchString, replaceString, "Key"));
            }

            // 3.3. WithBLOBs Model 修正名称
            if (introspectedTable.getRecordWithBLOBsType() != null){
                introspectedTable.setRecordWithBLOBsType(this.renameJavaType(introspectedTable.getRecordWithBLOBsType(), searchString, replaceString, "WithBLOBs"));
            }

            // 3.4. mapper 修正名称
            if (introspectedTable.getMyBatis3JavaMapperType() != null){
                introspectedTable.setMyBatis3JavaMapperType(this.renameJavaType(introspectedTable.getMyBatis3JavaMapperType(), searchString, replaceString, "Mapper"));
            }

            // 3.5. example 修正名称
            if (introspectedTable.getExampleType() != null){
                introspectedTable.setExampleType(this.renameJavaType(introspectedTable.getExampleType(), searchString, replaceString, "Example"));
            }

            // 3.6. Dao 添加前缀
            if (introspectedTable.getDAOInterfaceType() != null){
                introspectedTable.setDAOInterfaceType(this.renameJavaType(introspectedTable.getDAOInterfaceType(), searchString, replaceString, "Dao"));
            }

            // 3.7. DAOImpl 添加前缀
            if (introspectedTable.getDAOImplementationType() != null){
                introspectedTable.setDAOImplementationType(this.renameJavaType(introspectedTable.getDAOImplementationType(), searchString, replaceString, "DAOImpl"));
            }

            // 3.8. 修正xml文件名称
            if (introspectedTable.getMyBatis3XmlMapperFileName() != null){
                Pattern pattern = Pattern.compile(searchString);
                Matcher matcher = pattern.matcher(introspectedTable.getMyBatis3XmlMapperFileName());
                String fileName =  matcher.replaceAll(replaceString);
                introspectedTable.setMyBatis3XmlMapperFileName(fileName);
            }
        }
    }

    /**
     * 重命名类型
     *
     * @param type
     * @param override
     * @return
     */
    private String renameJavaType(String type, String override, String suffix) {
        int lastDot = type.lastIndexOf(".");
        return type.substring(0, lastDot) + "." + override + suffix;
    }

    /**
     * 重命名类型
     *
     * @param type
     * @param searchString
     * @param replaceString
     * @return
     */
    private String renameJavaType(String type, String searchString, String replaceString, String suffix) {
        int lastDot = type.lastIndexOf(".");
        String shortName = type.substring(lastDot + 1, type.length() - 1);


        Pattern pattern = Pattern.compile(searchString);
        Matcher matcher = pattern.matcher(shortName);
        logger.warn("===============================================================");
        logger.warn(shortName);
        logger.warn(type.substring(0, lastDot) + "." + matcher.replaceAll(replaceString) + suffix);

        return type.substring(0, lastDot) + "." + matcher.replaceAll(replaceString) + suffix;
    }
}
