package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.io.Serializable;
import java.util.Set;

/**
 * @author durenhao
 * @date 2021/4/16 20:51
 **/
public class SerializablePlugin extends BasePlugin implements Serializable {

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addSerializableInterface(topLevelClass, introspectedTable);
        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addSerializableInterface(topLevelClass, introspectedTable);
        return super.modelPrimaryKeyClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addSerializableInterface(topLevelClass, introspectedTable);
        return super.modelRecordWithBLOBsClassGenerated(topLevelClass, introspectedTable);
    }


    private void addSerializableInterface(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        Set<FullyQualifiedJavaType> superInterfaceTypes = topLevelClass.getSuperInterfaceTypes();
        boolean match = superInterfaceTypes.stream()
                .anyMatch(f -> f.getFullyQualifiedName().equals("java.io.Serializable"));

        if (!match) {
            FullyQualifiedJavaType listType = new FullyQualifiedJavaType("java.io.Serializable");
            topLevelClass.addSuperInterface(listType);
            topLevelClass.addImportedType("java.io.Serializable");
        }

    }

}
