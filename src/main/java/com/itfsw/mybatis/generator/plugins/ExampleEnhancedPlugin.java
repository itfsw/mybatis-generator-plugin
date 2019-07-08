package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import com.itfsw.mybatis.generator.plugins.utils.FormatTools;
import com.itfsw.mybatis.generator.plugins.utils.JavaElementGeneratorTools;
import com.itfsw.mybatis.generator.plugins.utils.PluginTools;
import com.itfsw.mybatis.generator.plugins.utils.enhanced.InnerInterface;
import com.itfsw.mybatis.generator.plugins.utils.enhanced.InnerInterfaceWrapperToInnerClass;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.List;

/**
 * ---------------------------------------------------------------------------
 * Example 增强插件
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/1/16 16:28
 * ---------------------------------------------------------------------------
 */
public class ExampleEnhancedPlugin extends BasePlugin {
    // newAndCreateCriteria 方法
    public static final String METHOD_NEW_AND_CREATE_CRITERIA = "newAndCreateCriteria";
    // 逻辑删除列-Key
    public static final String PRO_ENABLE_AND_IF = "enableAndIf";
    // 是否启用column的操作
    private boolean enableColumnOperate = false;
    // 是否启用了过期的andIf
    private boolean enableAndIf;

    /**
     * {@inheritDoc}
     * @param introspectedTable
     */
    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        super.initialized(introspectedTable);
        this.enableColumnOperate = PluginTools.checkDependencyPlugin(context, ModelColumnPlugin.class);
        String enableAndIf = properties.getProperty(PRO_ENABLE_AND_IF);
        this.enableAndIf = enableAndIf == null ? true : StringUtility.isTrue(enableAndIf);
    }

    /**
     * ModelExample Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        List<InnerClass> innerClasses = topLevelClass.getInnerClasses();
        for (int i = 0; i < innerClasses.size(); i++) {
            InnerClass innerClass = innerClasses.get(i);
            if ("Criteria".equals(innerClass.getType().getShortName())) {
                // 工厂方法
                addFactoryMethodToCriteria(topLevelClass, innerClass, introspectedTable);
                // andIf
                if (this.enableAndIf) {
                    addAndIfMethodToCriteria(topLevelClass, innerClass, introspectedTable);
                }
                // when
                addWhenToCriteria(topLevelClass, innerClass, introspectedTable);
            } else if ("GeneratedCriteria".equals(innerClass.getType().getShortName())) {
                // column 方法
                if (this.enableColumnOperate) {
                    addColumnMethodToCriteria(topLevelClass, innerClass, introspectedTable);
                }
            }
        }

        List<Method> methods = topLevelClass.getMethods();
        for (Method method : methods) {
            if (!"createCriteriaInternal".equals(method.getName())) {
                continue;
            } else {
                method.getBodyLines().set(0, "Criteria criteria = new Criteria(this);");
                logger.debug("itfsw(Example增强插件):" + topLevelClass.getType().getShortName() + "修改createCriteriaInternal方法，修改构造Criteria时传入Example对象");
            }
        }

        // orderBy方法
        this.addOrderByMethodToExample(topLevelClass, introspectedTable);

        // createCriteria 静态方法
        this.addStaticCreateCriteriaMethodToExample(topLevelClass, introspectedTable);

        // when
        addWhenToExample(topLevelClass, introspectedTable);

        return true;
    }

    /**
     * 添加 createCriteria 静态方法
     * @param topLevelClass
     * @param introspectedTable
     */
    private void addStaticCreateCriteriaMethodToExample(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        Method createCriteriaMethod = JavaElementGeneratorTools.generateMethod(
                METHOD_NEW_AND_CREATE_CRITERIA,
                JavaVisibility.PUBLIC,
                FullyQualifiedJavaType.getCriteriaInstance()
        );
        commentGenerator.addGeneralMethodComment(createCriteriaMethod, introspectedTable);

        createCriteriaMethod.setStatic(true);
        createCriteriaMethod.addBodyLine(topLevelClass.getType().getShortName() + " example = new " + topLevelClass.getType().getShortName() + "();");
        createCriteriaMethod.addBodyLine("return example.createCriteria();");

        FormatTools.addMethodWithBestPosition(topLevelClass, createCriteriaMethod);
    }

    /**
     * 添加列操作方法
     * @param topLevelClass
     * @param innerClass
     * @param introspectedTable
     */
    private void addColumnMethodToCriteria(TopLevelClass topLevelClass, InnerClass innerClass, IntrospectedTable introspectedTable) {
        // !!!!! Column import比较特殊引入的是外部类
        topLevelClass.addImportedType(introspectedTable.getRules().calculateAllFieldsClass());
        for (IntrospectedColumn introspectedColumn : introspectedTable.getNonBLOBColumns()) {
            topLevelClass.addImportedType(introspectedColumn.getFullyQualifiedJavaType());
            // EqualTo
            FormatTools.addMethodWithBestPosition(innerClass, this.generateSingleValueMethod(introspectedTable, introspectedColumn, "EqualTo", "="));
            // NotEqualTo
            FormatTools.addMethodWithBestPosition(innerClass, this.generateSingleValueMethod(introspectedTable, introspectedColumn, "NotEqualTo", "<>"));
            // GreaterThan
            FormatTools.addMethodWithBestPosition(innerClass, this.generateSingleValueMethod(introspectedTable, introspectedColumn, "GreaterThan", ">"));
            // GreaterThanOrEqualTo
            FormatTools.addMethodWithBestPosition(innerClass, this.generateSingleValueMethod(introspectedTable, introspectedColumn, "GreaterThanOrEqualTo", ">="));
            // LessThan
            FormatTools.addMethodWithBestPosition(innerClass, this.generateSingleValueMethod(introspectedTable, introspectedColumn, "LessThan", "<"));
            // LessThanOrEqualTo
            FormatTools.addMethodWithBestPosition(innerClass, this.generateSingleValueMethod(introspectedTable, introspectedColumn, "LessThanOrEqualTo", "<="));
        }
    }

    /**
     * 生成column操作的具体方法
     * @param introspectedTable
     * @param introspectedColumn
     * @param nameFragment
     * @param operator
     * @return
     */
    private Method generateSingleValueMethod(IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn, String nameFragment, String operator) {
        // 方法名
        StringBuilder sb = new StringBuilder();
        sb.append(introspectedColumn.getJavaProperty());
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        sb.insert(0, "and");
        sb.append(nameFragment);
        sb.append("Column");

        Method method = JavaElementGeneratorTools.generateMethod(
                sb.toString(),
                JavaVisibility.PUBLIC,
                FullyQualifiedJavaType.getCriteriaInstance(),
                new Parameter(
                        new FullyQualifiedJavaType(introspectedTable.getRules().calculateAllFieldsClass().getShortName() + "." + ModelColumnPlugin.ENUM_NAME),
                        "column"
                )
        );

        // 方法体
        sb.setLength(0);
        sb.append("addCriterion(");
        sb.append("new StringBuilder(\"");
        sb.append(MyBatis3FormattingUtilities.getAliasedActualColumnName(introspectedColumn));
        sb.append(" ");
        sb.append(operator);
        sb.append(" \").append(");
        sb.append("column.");
        sb.append(ModelColumnPlugin.METHOD_GET_ESCAPED_COLUMN_NAME);
        sb.append("()).toString());");

        JavaElementGeneratorTools.generateMethodBody(
                method,
                sb.toString(),
                "return (Criteria) this;"
        );

        return method;
    }

    /**
     * 添加工厂方法
     * @param topLevelClass
     * @param innerClass
     * @param introspectedTable
     */
    private void addFactoryMethodToCriteria(TopLevelClass topLevelClass, InnerClass innerClass, IntrospectedTable introspectedTable) {
        // example field
        Field exampleField = JavaElementGeneratorTools.generateField(
                "example",
                JavaVisibility.PRIVATE,
                topLevelClass.getType(),
                null
        );
        commentGenerator.addFieldComment(exampleField, introspectedTable);
        innerClass.addField(exampleField);

        // overwrite constructor
        List<Method> methods = innerClass.getMethods();
        for (Method method : methods) {
            if (method.isConstructor()) {
                method.addParameter(new Parameter(topLevelClass.getType(), "example"));
                method.addBodyLine("this.example = example;");
                commentGenerator.addGeneralMethodComment(method, introspectedTable);
                logger.debug("itfsw(Example增强插件):" + topLevelClass.getType().getShortName() + "修改构造方法，增加example参数");
            }
        }

        // 添加example工厂方法
        Method exampleMethod = JavaElementGeneratorTools.generateMethod(
                "example",
                JavaVisibility.PUBLIC,
                topLevelClass.getType()
        );
        commentGenerator.addGeneralMethodComment(exampleMethod, introspectedTable);
        exampleMethod = JavaElementGeneratorTools.generateMethodBody(
                exampleMethod,
                "return this.example;"
        );
        FormatTools.addMethodWithBestPosition(innerClass, exampleMethod);
        logger.debug("itfsw(Example增强插件):" + topLevelClass.getType().getShortName() + "." + innerClass.getType().getShortName() + "增加工厂方法example");
    }

    /**
     * 增强Criteria的链式调用(when)
     * @param topLevelClass
     * @param innerClass
     * @param introspectedTable
     */
    private void addWhenToCriteria(TopLevelClass topLevelClass, InnerClass innerClass, IntrospectedTable introspectedTable) {
        this.addWhenToClass(topLevelClass, innerClass, introspectedTable, "criteria");
    }

    /**
     * 增强Example的链式调用(when)
     * @param topLevelClass
     * @param introspectedTable
     */
    private void addWhenToExample(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addWhenToClass(topLevelClass, topLevelClass, introspectedTable, "example");
    }

    /**
     * 增强链式调用(when)
     * @param topLevelClass
     * @param clazz
     * @param introspectedTable
     */
    private void addWhenToClass(TopLevelClass topLevelClass, InnerClass clazz, IntrospectedTable introspectedTable, String type) {
        // 添加接口When
        InnerInterface whenInterface = new InnerInterface("I" + FormatTools.upFirstChar(type) + "When");
        whenInterface.setVisibility(JavaVisibility.PUBLIC);

        // ICriteriaAdd增加接口add
        Method addMethod = JavaElementGeneratorTools.generateMethod(
                type,
                JavaVisibility.DEFAULT,
                null,
                new Parameter(clazz.getType(), type)
        );
        commentGenerator.addGeneralMethodComment(addMethod, introspectedTable);
        whenInterface.addMethod(addMethod);

        InnerClass innerClassWrapper = new InnerInterfaceWrapperToInnerClass(whenInterface);
        // 添加注释
        commentGenerator.addClassComment(innerClassWrapper, introspectedTable);
        topLevelClass.addInnerClass(innerClassWrapper);

        // 添加when方法
        Method whenMethod = JavaElementGeneratorTools.generateMethod(
                "when",
                JavaVisibility.PUBLIC,
                clazz.getType(),
                new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "condition"),
                new Parameter(whenInterface.getType(), "then")
        );
        commentGenerator.addGeneralMethodComment(whenMethod, introspectedTable);
        whenMethod = JavaElementGeneratorTools.generateMethodBody(
                whenMethod,
                "if (condition) {",
                "then." + type + "(this);",
                "}",
                "return this;"
        );
        FormatTools.addMethodWithBestPosition(clazz, whenMethod);
        Method whenOtherwiseMethod = JavaElementGeneratorTools.generateMethod(
                "when",
                JavaVisibility.PUBLIC,
                clazz.getType(),
                new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "condition"),
                new Parameter(whenInterface.getType(), "then"),
                new Parameter(whenInterface.getType(), "otherwise")
        );
        commentGenerator.addGeneralMethodComment(whenOtherwiseMethod, introspectedTable);
        whenOtherwiseMethod = JavaElementGeneratorTools.generateMethodBody(
                whenOtherwiseMethod,
                "if (condition) {",
                "then." + type + "(this);",
                "} else {",
                "otherwise." + type + "(this);",
                "}",
                "return this;"
        );
        FormatTools.addMethodWithBestPosition(clazz, whenOtherwiseMethod);
    }

    /**
     * 增强Criteria的链式调用，添加andIf(boolean addIf, CriteriaAdd add)方法，实现链式调用中按条件增加查询语句
     * @param topLevelClass
     * @param innerClass
     * @param introspectedTable
     */
    @Deprecated
    private void addAndIfMethodToCriteria(TopLevelClass topLevelClass, InnerClass innerClass, IntrospectedTable introspectedTable) {
        // 添加接口CriteriaAdd
        InnerInterface criteriaAddInterface = new InnerInterface("ICriteriaAdd");
        criteriaAddInterface.setVisibility(JavaVisibility.PUBLIC);
        criteriaAddInterface.addAnnotation("@Deprecated");
        logger.debug("itfsw(Example增强插件):" + topLevelClass.getType().getShortName() + "." + innerClass.getType().getShortName() + "增加接口ICriteriaAdd");

        // ICriteriaAdd增加接口add
        Method addMethod = JavaElementGeneratorTools.generateMethod(
                "add",
                JavaVisibility.DEFAULT,
                innerClass.getType(),
                new Parameter(innerClass.getType(), "add")
        );
        commentGenerator.addGeneralMethodComment(addMethod, introspectedTable);
        FormatTools.addMethodWithBestPosition(criteriaAddInterface, addMethod);
        logger.debug("itfsw(Example增强插件):" + topLevelClass.getType().getShortName() + "." + innerClass.getType().getShortName() + "." + criteriaAddInterface.getType().getShortName() + "增加方法add");

        InnerClass innerClassWrapper = new InnerInterfaceWrapperToInnerClass(criteriaAddInterface);
        // 添加注释
        commentGenerator.addClassComment(innerClassWrapper, introspectedTable);
        innerClass.addInnerClass(innerClassWrapper);

        // 添加andIf方法
        Method andIfMethod = JavaElementGeneratorTools.generateMethod(
                "andIf",
                JavaVisibility.PUBLIC,
                innerClass.getType(),
                new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "ifAdd"),
                new Parameter(criteriaAddInterface.getType(), "add")
        );
        andIfMethod.addAnnotation("@Deprecated");
        commentGenerator.addGeneralMethodComment(andIfMethod, introspectedTable);
        andIfMethod = JavaElementGeneratorTools.generateMethodBody(
                andIfMethod,
                "if (ifAdd) {",
                "add.add(this);",
                "}",
                "return this;"
        );
        FormatTools.addMethodWithBestPosition(innerClass, andIfMethod);
        logger.debug("itfsw(Example增强插件):" + topLevelClass.getType().getShortName() + "." + innerClass.getType().getShortName() + "增加方法andIf");
    }

    /**
     * Example增强了setOrderByClause方法，新增orderBy(String orderByClause)方法直接返回example，增强链式调用，可以一路.下去了。
     * @param topLevelClass
     * @param introspectedTable
     */
    private void addOrderByMethodToExample(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 添加orderBy
        Method orderByMethod = JavaElementGeneratorTools.generateMethod(
                "orderBy",
                JavaVisibility.PUBLIC,
                topLevelClass.getType(),
                new Parameter(FullyQualifiedJavaType.getStringInstance(), "orderByClause")
        );
        commentGenerator.addGeneralMethodComment(orderByMethod, introspectedTable);
        orderByMethod = JavaElementGeneratorTools.generateMethodBody(
                orderByMethod,
                "this.setOrderByClause(orderByClause);",
                "return this;"
        );
        FormatTools.addMethodWithBestPosition(topLevelClass, orderByMethod);
        logger.debug("itfsw(Example增强插件):" + topLevelClass.getType().getShortName() + "增加方法orderBy");

        // 添加orderBy
        Method orderByMethod1 = JavaElementGeneratorTools.generateMethod(
                "orderBy",
                JavaVisibility.PUBLIC,
                topLevelClass.getType(),
                new Parameter(FullyQualifiedJavaType.getStringInstance(), "orderByClauses", true)
        );
        commentGenerator.addGeneralMethodComment(orderByMethod1, introspectedTable);
        orderByMethod1 = JavaElementGeneratorTools.generateMethodBody(
                orderByMethod1,
                "StringBuffer sb = new StringBuffer();",
                "for (int i = 0; i < orderByClauses.length; i++) {",
                "sb.append(orderByClauses[i]);",
                "if (i < orderByClauses.length - 1) {",
                "sb.append(\" , \");",
                "}",
                "}",
                "this.setOrderByClause(sb.toString());",
                "return this;"
        );

        FormatTools.addMethodWithBestPosition(topLevelClass, orderByMethod1);
        logger.debug("itfsw(Example增强插件):" + topLevelClass.getType().getShortName() + "增加方法orderBy(String ... orderByClauses)");
    }
}
