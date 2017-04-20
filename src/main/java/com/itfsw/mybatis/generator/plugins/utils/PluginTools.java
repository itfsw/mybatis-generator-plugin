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

package com.itfsw.mybatis.generator.plugins.utils;

import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.PluginConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;

/**
 * ---------------------------------------------------------------------------
 * 插件工具集
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/4/20 15:43
 * ---------------------------------------------------------------------------
 */
public class PluginTools {
    private static final Logger logger = LoggerFactory.getLogger(PluginTools.class);

    /**
     * 检查插件依赖
     *
     * @param plugin 插件
     * @param ctx 上下文
     * @return
     */
    public static boolean checkDependencyPlugin(Class plugin, Context ctx){
        try {
            // 利用反射获取pluginConfigurations属性
            Field field = Context.class.getDeclaredField("pluginConfigurations");
            field.setAccessible(true);
            List<PluginConfiguration> list = (List<PluginConfiguration>) field.get(ctx);
            // 检查是否配置了ModelColumnPlugin插件
            for (PluginConfiguration config: list) {
                if (plugin.getName().equals(config.getConfigurationType())){
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("插件检查反射异常", e);
        }

        return false;
    }
}
