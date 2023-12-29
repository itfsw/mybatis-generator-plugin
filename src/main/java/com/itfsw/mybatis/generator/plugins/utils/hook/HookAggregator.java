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

import com.itfsw.mybatis.generator.plugins.ModelAnnotationPlugin;
import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import com.itfsw.mybatis.generator.plugins.utils.BeanUtils;
import org.mybatis.generator.api.CompositePlugin;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.PluginAggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class HookAggregator implements IUpsertPluginHook,
        IModelBuilderPluginHook,
        IIncrementPluginHook,
        IOptimisticLockerPluginHook,
        ISelectOneByExamplePluginHook,
        ITableConfigurationHook,
        ILogicalDeletePluginHook,
        IModelColumnPluginHook,
        IModelAnnotationPluginHook,
        ISelectSelectivePluginHook {

    protected static final Logger logger = LoggerFactory.getLogger(BasePlugin.class);
    private final static HookAggregator instance = new HookAggregator();
    private Context context;

    /**
     * constructor
     */
    public HookAggregator() {
    }

    public static HookAggregator getInstance() {
        return instance;
    }

    /**
     * Setter method for property <tt>context</tt>.
     *
     * @param context value to be assigned to property context
     * @author hewei
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * 获取插件
     */
    private <T> List<T> getPlugins(Class<T> clazz) {
        List list = new ArrayList();
        // 反射获取插件列表，不能用单例去弄，不然因为类释放的问题而导致测试用例出问题
        try {
            PluginAggregator pluginAggregator = (PluginAggregator) context.getPlugins();
            List<Plugin> plugins = (List<Plugin>) BeanUtils.getProperty(CompositePlugin.class, pluginAggregator, "plugins");
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

    // ============================================= IModelAnnotationPluginHook ==============================================

    /**
     * Model Setter 生成
     */
    @Override
    public void modelSetterGenerated(IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn, TopLevelClass topLevelClass, Field field) {
        if (!this.getPlugins(ModelAnnotationPlugin.class).isEmpty()) {
            this.getPlugins(IModelAnnotationPluginHook.class).get(0).modelSetterGenerated(introspectedTable, introspectedColumn, topLevelClass, field);
        }
    }

    // ============================================= IIncrementPluginHook ==============================================

    @Override
    public XmlElement generateIncrementSet(IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn, String prefix, boolean hasComma) {
        if (this.getPlugins(IIncrementPluginHook.class).isEmpty()) {
            return null;
        } else {
            return this.getPlugins(IIncrementPluginHook.class).get(0).generateIncrementSet(introspectedTable, introspectedColumn, prefix, hasComma);
        }
    }

    @Override
    public XmlElement generateIncrementSetSelective(IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn, String prefix) {
        if (this.getPlugins(IIncrementPluginHook.class).isEmpty()) {
            return null;
        } else {
            return this.getPlugins(IIncrementPluginHook.class).get(0).generateIncrementSetSelective(introspectedTable, introspectedColumn, prefix);
        }
    }

    @Override
    public boolean supportIncrement(IntrospectedTable introspectedTable, IntrospectedColumn column) {
        if (!this.getPlugins(IIncrementPluginHook.class).isEmpty()) {
            return this.getPlugins(IIncrementPluginHook.class).get(0).supportIncrement(introspectedTable, column);
        }
        return false;
    }

    @Override
    public List<XmlElement> generateIncrementSetForSelectiveEnhancedPlugin(IntrospectedTable introspectedTable, List<IntrospectedColumn> columns) {
        if (!this.getPlugins(IIncrementPluginHook.class).isEmpty()) {
            return this.getPlugins(IIncrementPluginHook.class).get(0).generateIncrementSetForSelectiveEnhancedPlugin(introspectedTable, columns);
        }
        return null;
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

    @Override
    public boolean modelBuilderSetterMethodGenerated(Method method, TopLevelClass topLevelClass, InnerClass builderClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        for (IModelBuilderPluginHook plugin : this.getPlugins(IModelBuilderPluginHook.class)) {
            if (!plugin.modelBuilderSetterMethodGenerated(method, topLevelClass, builderClass, introspectedColumn, introspectedTable)) {
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

    // ================================================= IOptimisticLockerPluginHook ===============================================

    @Override
    public boolean clientUpdateWithVersionByExampleSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        for (IOptimisticLockerPluginHook plugin : this.getPlugins(IOptimisticLockerPluginHook.class)) {
            if (!plugin.clientUpdateWithVersionByExampleSelectiveMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean clientUpdateWithVersionByPrimaryKeySelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        for (IOptimisticLockerPluginHook plugin : this.getPlugins(IOptimisticLockerPluginHook.class)) {
            if (!plugin.clientUpdateWithVersionByPrimaryKeySelectiveMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean generateSetsSelectiveElement(IntrospectedTable introspectedTable, List<IntrospectedColumn> columns, IntrospectedColumn versionColumn, XmlElement setsElement) {
        if (this.getPlugins(IOptimisticLockerPluginHook.class).isEmpty()) {
            return false;
        } else {
            return this.getPlugins(IOptimisticLockerPluginHook.class).get(0).generateSetsSelectiveElement(introspectedTable, columns, versionColumn, setsElement);
        }
    }

    // ============================================= ISelectOneByExamplePluginHook ==============================================

    @Override
    public boolean clientSelectOneByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        for (ISelectOneByExamplePluginHook plugin : this.getPlugins(ISelectOneByExamplePluginHook.class)) {
            if (!plugin.clientSelectOneByExampleWithBLOBsMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean clientSelectOneByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        for (ISelectOneByExamplePluginHook plugin : this.getPlugins(ISelectOneByExamplePluginHook.class)) {
            if (!plugin.clientSelectOneByExampleWithoutBLOBsMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean sqlMapSelectOneByExampleWithoutBLOBsElementGenerated(Document document, XmlElement element, IntrospectedTable introspectedTable) {
        for (ISelectOneByExamplePluginHook plugin : this.getPlugins(ISelectOneByExamplePluginHook.class)) {
            if (!plugin.sqlMapSelectOneByExampleWithoutBLOBsElementGenerated(document, element, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean sqlMapSelectOneByExampleWithBLOBsElementGenerated(Document document, XmlElement element, IntrospectedTable introspectedTable) {
        for (ISelectOneByExamplePluginHook plugin : this.getPlugins(ISelectOneByExamplePluginHook.class)) {
            if (!plugin.sqlMapSelectOneByExampleWithBLOBsElementGenerated(document, element, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    // ============================================= ITableConfigurationHook ==============================================

    @Override
    public void tableConfiguration(IntrospectedTable introspectedTable) {
        if (!this.getPlugins(ITableConfigurationHook.class).isEmpty()) {
            this.getPlugins(ITableConfigurationHook.class).get(0).tableConfiguration(introspectedTable);
        }
    }

    // ============================================= ILogicalDeletePluginHook ==============================================

    @Override
    public boolean clientLogicalDeleteByExampleMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        for (ILogicalDeletePluginHook plugin : this.getPlugins(ILogicalDeletePluginHook.class)) {
            if (!plugin.clientLogicalDeleteByExampleMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean clientLogicalDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        for (ILogicalDeletePluginHook plugin : this.getPlugins(ILogicalDeletePluginHook.class)) {
            if (!plugin.clientLogicalDeleteByPrimaryKeyMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean sqlMapLogicalDeleteByExampleElementGenerated(Document document, XmlElement element, IntrospectedColumn logicalDeleteColumn, String logicalDeleteValue, IntrospectedTable introspectedTable) {
        for (ILogicalDeletePluginHook plugin : this.getPlugins(ILogicalDeletePluginHook.class)) {
            if (!plugin.sqlMapLogicalDeleteByExampleElementGenerated(document, element, logicalDeleteColumn, logicalDeleteValue, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean sqlMapLogicalDeleteByPrimaryKeyElementGenerated(Document document, XmlElement element, IntrospectedColumn logicalDeleteColumn, String logicalDeleteValue, IntrospectedTable introspectedTable) {
        for (ILogicalDeletePluginHook plugin : this.getPlugins(ILogicalDeletePluginHook.class)) {
            if (!plugin.sqlMapLogicalDeleteByPrimaryKeyElementGenerated(document, element, logicalDeleteColumn, logicalDeleteValue, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean logicalDeleteEnumGenerated(IntrospectedTable introspectedTable, IntrospectedColumn logicalDeleteColumn) {
        for (ILogicalDeletePluginHook plugin : this.getPlugins(ILogicalDeletePluginHook.class)) {
            if (plugin.logicalDeleteEnumGenerated(introspectedTable, logicalDeleteColumn)) {
                return true;
            }
        }
        return false;
    }

    // ============================================= ISelectSelectivePluginHook ==============================================

    @Override
    public boolean sqlMapSelectByExampleSelectiveElementGenerated(Document document, XmlElement element, IntrospectedTable introspectedTable) {
        for (ISelectSelectivePluginHook plugin : this.getPlugins(ISelectSelectivePluginHook.class)) {
            if (!plugin.sqlMapSelectByExampleSelectiveElementGenerated(document, element, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    // ============================================= IModelColumnPluginHook ==============================================

    @Override
    public boolean modelColumnEnumGenerated(InnerEnum innerEnum, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (IModelColumnPluginHook plugin : this.getPlugins(IModelColumnPluginHook.class)) {
            if (!plugin.modelColumnEnumGenerated(innerEnum, topLevelClass, introspectedTable)) {
                return false;
            }
        }
        return true;
    }
}
