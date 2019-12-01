package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.JavaClientGeneratorConfiguration;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.SqlMapGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 *  Copyright (C), 2010,9 szcport
 *  File name and path: com.itfsw.mybatis.generator.plugins.TargetPackagePlugin
 *  Author : luodexin, Version : 1.0, First complete date:
 *  Description :
 *
 *  Others :
 *  Function List:
 *      1. xxx
 *      2. xxx
 *
 *  History:
 *    1. Date: 2019-11-26
 *       Version:
 *       Author:
 *       Modification:
 *
 **/

public class TargetPackagePlugin extends BasePlugin {
    /**
     * 模型类目标子包
     */
    public static final String PRO_CLIENT_TARGET_PACKAGE = "clientTargetPackage";
    /**
     * mapper接口目标子包
     */
    public static final String PRO_MODEL_TARGET_PACKAGE = "modelTargetPackage";
    /**
     * mapper文件目标子包
     */
    public static final String PRO_SQL_MAP_TARGET_PACKAGE = "sqlMapTargetPackage";

    private static Pattern regex = Pattern.compile("[a-zA-Z]+[0-9a-zA-Z_]*(\\.[a-zA-Z]+[0-9a-zA-Z_]*)*");

    @Override
    public boolean validate(List<String> warnings) {
        // 如果table配置了domainObjectName或者mapperName就不要再启动该插件了
        for (TableConfiguration tableConfiguration : context.getTableConfigurations()) {
            if (tableConfiguration.getDomainObjectName() != null || tableConfiguration.getMapperName() != null) {
                warnings.add("itfsw:插件" + this.getClass().getTypeName() + "插件请不要配合table的domainObjectName或者mapperName一起使用！");
                return false;
            }
        }
        return super.validate(warnings);
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        super.initialized(introspectedTable);
        // 1.设置 Client 的子包
        String clientTargetPackage = getValidTargetPackage(introspectedTable, PRO_CLIENT_TARGET_PACKAGE);
        if (null == clientTargetPackage) {
            logger.warn("itfsw:插件" + this.getClass().getTypeName() + " 设置的clientTargetPackage属性不是有效包名");
        } else {
            JavaClientGeneratorConfiguration javaClientGeneratorConfiguration = context.getJavaClientGeneratorConfiguration();
            String targetPackage = javaClientGeneratorConfiguration.getTargetPackage() + "." + clientTargetPackage;
            javaClientGeneratorConfiguration.setTargetPackage(targetPackage);

            try {
                Method calculateJavaClientAttributes = IntrospectedTable.class.getDeclaredMethod("calculateJavaClientAttributes");
                calculateJavaClientAttributes.setAccessible(true);
                calculateJavaClientAttributes.invoke(introspectedTable);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                logger.error("itfsw:插件" + this.getClass().getTypeName() + "设置clientTargetPackage时异常");
            }
        }

        // 2.设置 Model 的子包
        String modelTargetPackage = getValidTargetPackage(introspectedTable, PRO_MODEL_TARGET_PACKAGE);
        if (null == modelTargetPackage) {
            logger.warn("itfsw:插件" + this.getClass().getTypeName() + " 设置的modelTargetPackage属性不是有效包名");
        } else {
            JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = context.getJavaModelGeneratorConfiguration();
            String targetPackage = javaModelGeneratorConfiguration.getTargetPackage() + "." + modelTargetPackage;
            logger.info("targetPackage:" + targetPackage);
            javaModelGeneratorConfiguration.setTargetPackage(targetPackage);

            try {
                Method calculateModelAttributes = IntrospectedTable.class.getDeclaredMethod("calculateModelAttributes");
                calculateModelAttributes.setAccessible(true);
                calculateModelAttributes.invoke(introspectedTable);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                logger.error("itfsw:插件" + this.getClass().getTypeName() + "设置modelTargetPackage时异常");
            }
        }

        // 3.设置 sqlMap 的子包
        String sqlMapTargetPackage = getValidTargetPackage(introspectedTable, PRO_SQL_MAP_TARGET_PACKAGE);
        if (null == sqlMapTargetPackage) {
            logger.warn("itfsw:插件" + this.getClass().getTypeName() + " 设置的sqlMapTargetPackage属性不是有效包名");
        } else {
            SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration = context.getSqlMapGeneratorConfiguration();
            String targetPackage = sqlMapGeneratorConfiguration.getTargetPackage() + "." + sqlMapTargetPackage;
            sqlMapGeneratorConfiguration.setTargetPackage(targetPackage);

            try {
                Method calculateXmlAttributes = IntrospectedTable.class.getDeclaredMethod("calculateXmlAttributes");
                calculateXmlAttributes.setAccessible(true);
                calculateXmlAttributes.invoke(introspectedTable);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                logger.error("itfsw:插件" + this.getClass().getTypeName() + "设置sqlMapTargetPackage时异常");
            }
        }
    }

    /**
     * 检查包名格式是否正确
     */
    private String getValidTargetPackage(IntrospectedTable introspectedTable, final String PROPERTY_NAME) {
        String targetPacakge = introspectedTable.getTableConfigurationProperty(PROPERTY_NAME);
        if (null == targetPacakge || "".equals(targetPacakge.trim())) {
            return null;
        }

        targetPacakge = targetPacakge.trim();
        Matcher matcher = regex.matcher(targetPacakge);
        return matcher.matches() ? targetPacakge : null;
    }
}
