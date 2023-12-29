package com.itfsw.mybatis.generator.plugins.utils.hook;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;

/**
 * @author heweisc@dingtalk.com
 */
public interface IModelAnnotationPluginHook {
    /**
     * Model Setter 生成
     */
    void modelSetterGenerated(IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn, TopLevelClass topLevelClass, Field field);
}
