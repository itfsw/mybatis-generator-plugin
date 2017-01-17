package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.CommentTools;
import com.itfsw.mybatis.generator.plugins.utils.InnerInterface;
import com.itfsw.mybatis.generator.plugins.utils.InnerInterfaceWrapperToInnerClass;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.internal.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * ---------------------------------------------------------------------------
 * Example 增强插件
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/1/16 16:28
 * ---------------------------------------------------------------------------
 */
public class ExampleEnhancedPlugin  extends PluginAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ExampleEnhancedPlugin.class);
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
        return true;
    }

    /**
     * ModelExample Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        List<InnerClass> innerClasses = topLevelClass.getInnerClasses();
        for (InnerClass innerClass : innerClasses) {
            if ("Criteria".equals(innerClass.getType().getShortName())) {
                // 工厂方法
                addFactoryMethodToCriteria(topLevelClass, innerClass, introspectedTable);
                // andIf
                addAndIfMethodToCriteria(topLevelClass, innerClass, introspectedTable);
            }
        }

        List<Method> methods = topLevelClass.getMethods();
        for (Method method : methods) {
            if (!"createCriteriaInternal".equals(method.getName()))
                continue;
            method.getBodyLines().set(0, "Criteria criteria = new Criteria(this);");
            logger.debug("itfsw(Example增强插件):"+topLevelClass.getType().getShortName()+"修改createCriteriaInternal方法，修改构造Criteria时传入Example对象");
        }

        // orderBy方法
        addOrderByMethodToExample(topLevelClass, introspectedTable);

        return true;
    }

    /**
     * 添加工厂方法
     *
     * @param topLevelClass
     * @param innerClass
     * @param introspectedTable
     */
    private void addFactoryMethodToCriteria(TopLevelClass topLevelClass, InnerClass innerClass, IntrospectedTable introspectedTable) {
        Field f = new Field("example", topLevelClass.getType());
        f.setVisibility(JavaVisibility.PRIVATE);
        innerClass.addField(f);

        // overwrite constructor
        List<Method> methods = innerClass.getMethods();
        for (Method method : methods) {
            if (method.isConstructor()) {
                method.addParameter(new Parameter(topLevelClass.getType(), "example"));
                method.addBodyLine("this.example = example;");
                logger.debug("itfsw(Example增强插件):"+topLevelClass.getType().getShortName()+"修改构造方法，增加example参数");
            }
        }

        // add factory method "example"
        Method method = new Method("example");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(topLevelClass.getType());
        method.addBodyLine("return this.example;");
        CommentTools.addGeneralMethodComment(method, introspectedTable);
        innerClass.addMethod(method);
        logger.debug("itfsw(Example增强插件):"+topLevelClass.getType().getShortName()+"."+innerClass.getType().getShortName()+"增加工厂方法example");
    }


    /**
     * 增强Criteria的链式调用，添加andIf(boolean addIf, CriteriaAdd add)方法，实现链式调用中按条件增加查询语句
     *
     * @param topLevelClass
     * @param innerClass
     * @param introspectedTable
     */
    private void addAndIfMethodToCriteria(TopLevelClass topLevelClass, InnerClass innerClass, IntrospectedTable introspectedTable){
        // 添加接口CriteriaAdd
        InnerInterface criteriaAddInterface = new InnerInterface("ICriteriaAdd");
        criteriaAddInterface.setVisibility(JavaVisibility.PUBLIC);
        CommentTools.addInterfaceComment(criteriaAddInterface, introspectedTable);
        logger.debug("itfsw(Example增强插件):"+topLevelClass.getType().getShortName()+"."+innerClass.getType().getShortName()+"增加接口ICriteriaAdd");

        // ICriteriaAdd增加接口add
        Method addMethod = new Method("add");
        addMethod.setReturnType(innerClass.getType());
        addMethod.addParameter(new Parameter(innerClass.getType(), "add"));
        CommentTools.addGeneralMethodComment(addMethod, introspectedTable);
        criteriaAddInterface.addMethod(addMethod);
        logger.debug("itfsw(Example增强插件):"+topLevelClass.getType().getShortName()+"."+innerClass.getType().getShortName()+"."+criteriaAddInterface.getType().getShortName()+"增加方法add");

        InnerClass innerClassWrapper = new InnerInterfaceWrapperToInnerClass(criteriaAddInterface);
        innerClass.addInnerClass(innerClassWrapper);

        // 添加andIf方法
        Method method = new Method("andIf");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(innerClass.getType());
        method.addParameter(new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "ifAdd"));
        method.addParameter(new Parameter(criteriaAddInterface.getType(), "add"));

        method.addBodyLine("if (ifAdd) {");
        method.addBodyLine("add.add(this);");
        method.addBodyLine("}");
        method.addBodyLine("return this;");
        CommentTools.addGeneralMethodComment(method, introspectedTable);
        innerClass.addMethod(method);
        logger.debug("itfsw(Example增强插件):"+topLevelClass.getType().getShortName()+"."+innerClass.getType().getShortName()+"增加方法andIf");
    }

    /**
     * Example增强了setOrderByClause方法，新增orderBy(String orderByClause)方法直接返回example，增强链式调用，可以一路.下去了。
     *
     * @param topLevelClass
     * @param introspectedTable
     */
    private void addOrderByMethodToExample(TopLevelClass topLevelClass, IntrospectedTable introspectedTable){
        // 添加orderBy
        Method method = new Method("orderBy");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(topLevelClass.getType());
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "orderByClause"));

        method.addBodyLine("this.setOrderByClause(orderByClause);");
        method.addBodyLine("return this;");

        CommentTools.addGeneralMethodComment(method, introspectedTable);
        topLevelClass.addMethod(method);
        logger.debug("itfsw(Example增强插件):"+topLevelClass.getType().getShortName()+"增加方法orderBy");

        // 添加orderBy
        Method mOrderByMore = new Method("orderBy");
        mOrderByMore.setVisibility(JavaVisibility.PUBLIC);
        mOrderByMore.setReturnType(topLevelClass.getType());
        mOrderByMore.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "orderByClauses", true));

        mOrderByMore.addBodyLine("StringBuffer sb = new StringBuffer();");
        mOrderByMore.addBodyLine("for (int i = 0; i < orderByClauses.length; i++) {");
        mOrderByMore.addBodyLine("sb.append(orderByClauses[i]);");
        mOrderByMore.addBodyLine("if (i < orderByClauses.length - 1) {");
        mOrderByMore.addBodyLine("sb.append(\" , \");");
        mOrderByMore.addBodyLine("}");
        mOrderByMore.addBodyLine("}");
        mOrderByMore.addBodyLine("this.setOrderByClause(sb.toString());");
        mOrderByMore.addBodyLine("return this;");

        CommentTools.addGeneralMethodComment(mOrderByMore, introspectedTable);
        topLevelClass.addMethod(mOrderByMore);
        logger.debug("itfsw(Example增强插件):"+topLevelClass.getType().getShortName()+"增加方法orderBy(String ... orderByClauses)");
    }
}
