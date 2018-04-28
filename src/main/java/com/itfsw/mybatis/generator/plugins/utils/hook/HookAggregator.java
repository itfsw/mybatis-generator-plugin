/*
 * Copyright (c) 2018.
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

package com.itfsw.mybatis.generator.plugins.utils.hook;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2018/4/27 11:33
 * ---------------------------------------------------------------------------
 */
public class HookAggregator implements IUpsertPluginHook, IModelBuilderPluginHook {
    protected static final Logger logger = LoggerFactory.getLogger(BasePlugin.class); // 日志
    private final static HookAggregator instance = new HookAggregator();
    private Context context;

    /**
     * constructor
     */
    public HookAggregator() {
    }

    /**
     * Getter method for property <tt>instance</tt>.
     * @return property value of instance
     * @author hewei
     */
    public static HookAggregator getInstance() {
        return instance;
    }

    /**
     * Setter method for property <tt>context</tt>.
     * @param context value to be assigned to property context
     * @author hewei
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * 获取插件
     * @param clazz
     * @param <T>
     * @return
     */
    private <T> List<T> getPlugins(Class<T> clazz) {
        List list = new ArrayList();
        // 反射获取插件列表，不能用单例去弄，不然因为类释放的问题而导致测试用例出问题
        try {
            java.lang.reflect.Field field = this.context.getPlugins().getClass().getDeclaredField("plugins");
            field.setAccessible(true);
            List<Plugin> plugins = (List<Plugin>) field.get(this.context.getPlugins());
            for (Plugin plugin : plugins) {
                if (clazz.isInstance(plugin)) {
                    list.add(plugin);
                }
            }
        } catch (Exception e) {
            logger.error("获取插件列表失败！", e);
        }
        return list;
    }

    // ============================================ IModelBuilderPluginHook =============================================
    @Override
    public boolean modelBuilderClassGenerated(TopLevelClass topLevelClass, InnerClass builderClass, List<IntrospectedColumn> columns, IntrospectedTable introspectedTable) {
        for (IModelBuilderPluginHook plugin : this.getPlugins(IModelBuilderPluginHook.class)) {
            if (!plugin.modelBuilderClassGenerated(topLevelClass, builderClass, columns, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    // ================================================= IUpsertPluginHook ===============================================
    @Override
    public boolean clientUpsertSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        for (IUpsertPluginHook plugin : this.getPlugins(IUpsertPluginHook.class)) {
            if (!plugin.clientUpsertSelectiveMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean clientUpsertByExampleSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        for (IUpsertPluginHook plugin : this.getPlugins(IUpsertPluginHook.class)) {
            if (!plugin.clientUpsertByExampleSelectiveMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean sqlMapUpsertSelectiveElementGenerated(XmlElement element, List<IntrospectedColumn> columns, XmlElement insertColumnsEle, XmlElement insertValuesEle, XmlElement setsEle, IntrospectedTable introspectedTable) {
        for (IUpsertPluginHook plugin : this.getPlugins(IUpsertPluginHook.class)) {
            if (!plugin.sqlMapUpsertSelectiveElementGenerated(element, columns, insertColumnsEle, insertValuesEle, setsEle, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean sqlMapUpsertByExampleSelectiveElementGenerated(XmlElement element, List<IntrospectedColumn> columns, XmlElement insertColumnsEle, XmlElement insertValuesEle, XmlElement setsEle, IntrospectedTable introspectedTable) {
        for (IUpsertPluginHook plugin : this.getPlugins(IUpsertPluginHook.class)) {
            if (!plugin.sqlMapUpsertByExampleSelectiveElementGenerated(element, columns, insertColumnsEle, insertValuesEle, setsEle, introspectedTable)) {
                return false;
            }
        }
        return true;
    }
}
