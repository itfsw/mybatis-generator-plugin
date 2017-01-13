/*
 *
 *  * Copyright (c) 2017.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.itfsw.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.internal.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

/**
 * ---------------------------------------------------------------------------
 * Example类生成位置修改
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/1/12 12:36
 * ---------------------------------------------------------------------------
 */
public class ExampleTargetPlugin extends PluginAdapter {
    public static final String TARGET_PACKAGE_KEY = "targetPackage";  // 配置targetPackage名
    private static final Logger logger = LoggerFactory.getLogger(ExampleTargetPlugin.class);

    private String targetPackage;   // 目标包

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(List<String> warnings) {
        // 插件使用前提是targetRuntime为MyBatis3
        if (StringUtility.stringHasValue(getContext().getTargetRuntime()) && "MyBatis3".equalsIgnoreCase(getContext().getTargetRuntime()) == false ){
            logger.warn("itfsw:插件"+this.getClass().getTypeName()+"要求运行targetRuntime必须为MyBatis3！");
            return false;
        }
        // 获取配置的目标package
        Properties properties = getProperties();
        this.targetPackage = properties.getProperty(TARGET_PACKAGE_KEY);
        if (this.targetPackage == null){
            logger.warn("请配置com.itfsw.mybatis.generator.plugins.ExampleTargetPlugin插件的目标包名(targetPackage)！");
            return false;
        }
        return true;
    }

    /**
     * 初始化阶段
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param introspectedTable
     */
    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        String exampleType = introspectedTable.getExampleType();
        // 修改包名
        Context context = getContext();
        JavaModelGeneratorConfiguration configuration = context.getJavaModelGeneratorConfiguration();
        String targetPackage = configuration.getTargetPackage();
        String newExampleType = exampleType.replace(targetPackage, this.targetPackage);

        introspectedTable.setExampleType(newExampleType);

        logger.debug("itfsw:修改"+exampleType+"的包到"+this.targetPackage);
    }

}
