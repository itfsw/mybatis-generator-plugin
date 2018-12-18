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
import com.itfsw.mybatis.generator.plugins.utils.FormatTools;
import com.itfsw.mybatis.generator.plugins.utils.IntrospectedTableTools;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.TableConfiguration;

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
@Deprecated
public class TableRenamePlugin extends BasePlugin {
    public static final String PRO_SEARCH_STRING = "searchString";  // 查找 property
    public static final String PRO_REPLACE_STRING = "replaceString";  // 替换 property
    public static final String PRO_TABLE_OVERRIDE = "tableOverride";   // table 重命名 property

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(List<String> warnings) {

        // 如果配置了searchString 或者 replaceString，二者不允许单独存在
        if ((getProperties().getProperty(PRO_SEARCH_STRING) == null && getProperties().getProperty(PRO_REPLACE_STRING) != null)
                || (getProperties().getProperty(PRO_SEARCH_STRING) != null && getProperties().getProperty(PRO_REPLACE_STRING) == null)) {
            warnings.add("itfsw:插件" + this.getClass().getTypeName() + "插件的searchString、replaceString属性需配合使用，不能单独存在！");
            return false;
        }

        // 如果table配置了domainObjectName或者mapperName就不要再启动该插件了
        for (TableConfiguration tableConfiguration : context.getTableConfigurations()) {
            if (tableConfiguration.getDomainObjectName() != null || tableConfiguration.getMapperName() != null) {
                warnings.add("itfsw:插件" + this.getClass().getTypeName() + "插件请不要配合table的domainObjectName或者mapperName一起使用！");
                return false;
            }
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
        if (introspectedTable.getTableConfigurationProperty(PRO_TABLE_OVERRIDE) != null) {
            String override = introspectedTable.getTableConfigurationProperty(PRO_TABLE_OVERRIDE);
            try {
                IntrospectedTableTools.setDomainObjectName(introspectedTable, getContext(), override);
            } catch (Exception e) {
                logger.error("itfsw:插件" + this.getClass().getTypeName() + "使用tableOverride替换时异常！", e);
            }
        } else if (getProperties().getProperty(PRO_SEARCH_STRING) != null) {
            String searchString = getProperties().getProperty(PRO_SEARCH_STRING);
            String replaceString = getProperties().getProperty(PRO_REPLACE_STRING);

            String domainObjectName = introspectedTable.getFullyQualifiedTable().getDomainObjectName();
            Pattern pattern = Pattern.compile(searchString);
            Matcher matcher = pattern.matcher(domainObjectName);
            domainObjectName = matcher.replaceAll(replaceString);
            // 命名规范化 首字母大写
            domainObjectName = FormatTools.upFirstChar(domainObjectName);
            try {
                IntrospectedTableTools.setDomainObjectName(introspectedTable, getContext(), domainObjectName);
            } catch (Exception e) {
                logger.error("itfsw:插件" + this.getClass().getTypeName() + "使用searchString、replaceString替换时异常！", e);
            }
        }
        super.initialized(introspectedTable);
    }
}
