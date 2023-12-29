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

package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.*;
import com.itfsw.mybatis.generator.plugins.utils.hook.ILogicalDeletePluginHook;
import com.itfsw.mybatis.generator.plugins.utils.hook.IModelAnnotationPluginHook;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.internal.util.StringUtility;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * type or status enum 插件
 */
public class EnumTypeStatusPlugin extends BasePlugin implements ILogicalDeletePluginHook {

    /**
     * 自动扫描
     */
    public final static String PRO_AUTO_SCAN = "autoScan";

    /**
     * 需要生成Enum的Column
     */
    public final static String PRO_ENUM_COLUMNS = "enumColumns";
    public final static String REMARKS_PATTERN = ".*\\s*\\[\\s*(\\w+\\s*\\(\\s*[\\u4e00-\\u9fa5_\\-a-zA-Z0-9]+\\s*\\)\\s*:\\s*[\\u4e00-\\u9fa5_\\-a-zA-Z0-9]+\\s*\\,?\\s*)+\\s*\\]\\s*.*";
    public final static String NEED_PATTERN = "\\[\\s*((\\w+\\s*\\(\\s*[\\u4e00-\\u9fa5_\\-a-zA-Z0-9]+\\s*\\)\\s*:\\s*[\\u4e00-\\u9fa5_\\-a-zA-Z0-9]+\\s*\\,?\\s*)+)\\s*\\]";
    public final static String ITEM_PATTERN = "(\\w+)\\s*\\(\\s*([\\u4e00-\\u9fa5_\\-a-zA-Z0-9]+)\\s*\\)\\s*:\\s*([\\u4e00-\\u9fa5_\\-a-zA-Z0-9]+)";
    private Map<IntrospectedTable, Map<String, EnumInfo>> enumColumnsMap = new HashMap<>();

    /**
     * <a href="http://www.mybatis.org/generator/reference/pluggingIn.html">具体执行顺序</a>
     */
    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        super.initialized(introspectedTable);
        Map<String, EnumInfo> enumColumns = new LinkedHashMap<>();
        String autoScan = properties.getProperty(PRO_AUTO_SCAN);
        // 是否开启了自动扫描
        if (StringUtility.stringHasValue(autoScan) && !StringUtility.isTrue(autoScan)) {
            // 获取全局配置
            String enumColumnsPro = properties.getProperty(EnumTypeStatusPlugin.PRO_ENUM_COLUMNS);
            // 如果有局部配置，则附加上去
            String tableEnumColumns = introspectedTable.getTableConfigurationProperty(EnumTypeStatusPlugin.PRO_ENUM_COLUMNS);
            if (tableEnumColumns != null) {
                enumColumnsPro = enumColumnsPro == null ? "" : (enumColumnsPro + ",");

                enumColumnsPro += introspectedTable.getTableConfigurationProperty(EnumTypeStatusPlugin.PRO_ENUM_COLUMNS);
            }

            if (StringUtility.stringHasValue(enumColumnsPro)) {
                // 切分
                String[] enumColumnsStrs = enumColumnsPro.split(",");
                for (String enumColumnsStr : enumColumnsStrs) {
                    IntrospectedColumn column = IntrospectedTableTools.safeGetColumn(introspectedTable, enumColumnsStr);
                    if (column != null) {
                        try {
                            EnumInfo enumInfo = new EnumInfo(column);
                            // 解析注释
                            enumInfo.parseRemarks(column.getRemarks());
                            if (enumInfo.hasItems()) {
                                enumColumns.put(column.getJavaProperty(), enumInfo);
                            }
                        } catch (EnumInfo.CannotParseException e) {
                            warnings.add("itfsw:插件" + EnumTypeStatusPlugin.class.getTypeName() + "没有找到column为" + enumColumnsStr.trim() + "对应格式的注释的字段！");
                        } catch (EnumInfo.NotSupportTypeException e) {
                            warnings.add("itfsw:插件" + EnumTypeStatusPlugin.class.getTypeName() + "找到column为" + enumColumnsStr.trim() + "对应Java类型不在支持范围内！");
                        }
                    }
                }
            }
        } else {
            for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
                try {
                    EnumInfo enumInfo = new EnumInfo(column);
                    // 解析注释
                    enumInfo.parseRemarks(column.getRemarks());
                    if (enumInfo.hasItems()) {
                        enumColumns.put(column.getJavaProperty(), enumInfo);
                    }
                } catch (Exception e) {
                    // nothing
                }
            }
        }

        enumColumnsMap.put(introspectedTable, enumColumns);
    }

    /**
     * <a href="http://www.mybatis.org/generator/reference/pluggingIn.html">具体执行顺序</a>
     */
    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.generateModelEnum(topLevelClass, introspectedTable);
        return super.modelPrimaryKeyClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * Model 生成
     * <a href="http://www.mybatis.org/generator/reference/pluggingIn.html">具体执行顺序</a>
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.generateModelEnum(topLevelClass, introspectedTable);
        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    // ======================================= ILogicalDeletePluginHook ======================================


    @Override
    public boolean clientLogicalDeleteByExampleMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean clientLogicalDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapLogicalDeleteByExampleElementGenerated(Document document, XmlElement element, IntrospectedColumn logicalDeleteColumn, String logicalDeleteValue, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapLogicalDeleteByPrimaryKeyElementGenerated(Document document, XmlElement element, IntrospectedColumn logicalDeleteColumn, String logicalDeleteValue, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean logicalDeleteEnumGenerated(IntrospectedTable introspectedTable, IntrospectedColumn logicalDeleteColumn) {
        return this.enumColumnsMap.get(introspectedTable).containsKey(logicalDeleteColumn.getJavaProperty());
    }

    /**
     * 生成对应enum
     */
    private void generateModelEnum(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 枚举跟随字段走
        for (Field field : topLevelClass.getFields()) {
            EnumInfo enumInfo = this.enumColumnsMap.get(introspectedTable).get(field.getName());
            if (enumInfo != null) {
                InnerEnum innerEnum = enumInfo.generateEnum(commentGenerator, introspectedTable);
                topLevelClass.addInnerEnum(innerEnum);
                // set Enum 方法
                Method method = JavaElementGeneratorTools.generateMethod(
                        "set" + FormatTools.upFirstChar(field.getName()),
                        JavaVisibility.PUBLIC,
                        null,
                        new Parameter(innerEnum.getType(), field.getName())
                );
                JavaElementGeneratorTools.generateMethodBody(method,
                        "this." + field.getName() + " = " + field.getName() + ".value();"
                );
                commentGenerator.addSetterComment(method, introspectedTable, enumInfo.column);
                FormatTools.addMethodWithBestPosition(topLevelClass, method);
                // 解决整合lombok后 setter字段没有生成问题
                PluginTools.getHook(IModelAnnotationPluginHook.class)
                        .modelSetterGenerated(introspectedTable, enumInfo.column, topLevelClass, field);
            }
        }
    }

    public static class EnumInfo {
        private final List<EnumItemInfo> items = new ArrayList<>();
        private final IntrospectedColumn column;

        public EnumInfo(IntrospectedColumn column) throws NotSupportTypeException, CannotParseException {
            String javaType = column.getFullyQualifiedJavaType().getFullyQualifiedName();
            if (!(Short.class.getTypeName().equals(javaType)
                    || Integer.class.getTypeName().equals(javaType)
                    || Long.class.getTypeName().equals(javaType)
                    || Boolean.class.getTypeName().equals(javaType)
                    || Double.class.getTypeName().equals(javaType)
                    || Float.class.getTypeName().equals(javaType)
                    || BigDecimal.class.getTypeName().equals(javaType)
                    || Byte.class.getTypeName().equals(javaType)
                    || String.class.getTypeName().equals(javaType))) {
                throw new NotSupportTypeException();
            } else {
                this.column = column;
            }
        }

        /**
         * 添加Enum Item
         */
        public void addItem(String name, String comment, String value) {
            items.add(new EnumItemInfo(this.column, name, comment, value));
        }

        /**
         * 判断是否有节点
         */
        public boolean hasItems() {
            return !items.isEmpty();
        }

        /**
         * 解析注释
         */
        public void parseRemarks(String remarks) throws CannotParseException {
            if (!StringUtility.stringHasValue(remarks) || !remarks.matches(REMARKS_PATTERN)) {
                throw new CannotParseException();
            } else {
                // 提取信息
                Pattern pattern = Pattern.compile(NEED_PATTERN);
                Matcher matcher = pattern.matcher(remarks);
                if (matcher.find() && matcher.groupCount() == 2) {
                    String enumInfoStr = matcher.group(1);
                    // 根据逗号切分
                    String[] enumInfoStrs = enumInfoStr.split(",");

                    // 提取每个节点信息
                    for (String enumInfoItemStr : enumInfoStrs) {
                        pattern = Pattern.compile(ITEM_PATTERN);
                        matcher = pattern.matcher(enumInfoItemStr.trim());
                        if (matcher.find() && matcher.groupCount() == 3) {
                            this.addItem(matcher.group(1), matcher.group(3), matcher.group(2));
                        }
                    }
                }
            }
        }

        /**
         * Getter method for property <tt>items</tt>.
         *
         * @return property value of items
         */
        public List<EnumItemInfo> getItems() {
            return items;
        }

        public static class NotSupportTypeException extends Exception {
            private static final long serialVersionUID = 2283026936991058746L;
        }

        public static class CannotParseException extends Exception {
            private static final long serialVersionUID = -8307246050506041126L;
        }

        public InnerEnum generateEnum(CommentGenerator commentGenerator, IntrospectedTable introspectedTable) {
            String enumName = FormatTools.upFirstChar(column.getJavaProperty());

            InnerEnum innerEnum = new InnerEnum(new FullyQualifiedJavaType(enumName));
            commentGenerator.addEnumComment(innerEnum, introspectedTable);
            innerEnum.setVisibility(JavaVisibility.PUBLIC);
            innerEnum.setStatic(true);

            // 生成枚举
            for (EnumItemInfo item : this.items) {
                innerEnum.addEnumConstant(item.getName() + "(" + item.getValue() + ", " + item.getComment() + ")");
            }

            // 生成属性和构造函数
            Field fValue = new Field("value", column.getFullyQualifiedJavaType());
            fValue.setVisibility(JavaVisibility.PRIVATE);
            fValue.setFinal(true);
            commentGenerator.addFieldComment(fValue, introspectedTable);
            innerEnum.addField(fValue);

            Field fName = new Field("name", FullyQualifiedJavaType.getStringInstance());
            fName.setVisibility(JavaVisibility.PRIVATE);
            fName.setFinal(true);
            commentGenerator.addFieldComment(fName, introspectedTable);
            innerEnum.addField(fName);

            Method mInc = new Method(enumName);
            mInc.setConstructor(true);
            mInc.addBodyLine("this.value = value;");
            mInc.addBodyLine("this.name = name;");
            mInc.addParameter(new Parameter(fValue.getType(), "value"));
            mInc.addParameter(new Parameter(fName.getType(), "name"));
            commentGenerator.addGeneralMethodComment(mInc, introspectedTable);
            FormatTools.addMethodWithBestPosition(innerEnum, mInc);

            // 获取value的方法
            Method mValue = JavaElementGeneratorTools.generateGetterMethod(fValue);
            commentGenerator.addGeneralMethodComment(mValue, introspectedTable);
            FormatTools.addMethodWithBestPosition(innerEnum, mValue);

            Method mValue1 = JavaElementGeneratorTools.generateGetterMethod(fValue);
            mValue1.setName("value");
            commentGenerator.addGeneralMethodComment(mValue1, introspectedTable);
            FormatTools.addMethodWithBestPosition(innerEnum, mValue1);

            // 获取name的方法
            Method mName = JavaElementGeneratorTools.generateGetterMethod(fName);
            commentGenerator.addGeneralMethodComment(mName, introspectedTable);
            FormatTools.addMethodWithBestPosition(innerEnum, mName);

            // parseValue 方法
            Method mParseValue = JavaElementGeneratorTools.generateMethod(
                    "parseValue",
                    JavaVisibility.PUBLIC,
                    innerEnum.getType(),
                    new Parameter(fValue.getType(), "value")
            );
            mParseValue.setStatic(true);
            mParseValue.addBodyLine("if (value != null) {");
            mParseValue.addBodyLine("for (" + innerEnum.getType().getShortName() + " item : values()) {");
            mParseValue.addBodyLine("if (item.value.equals(value)) {");
            mParseValue.addBodyLine("return item;");
            mParseValue.addBodyLine("}");
            mParseValue.addBodyLine("}");
            mParseValue.addBodyLine("}");
            mParseValue.addBodyLine("return null;");
            commentGenerator.addGeneralMethodComment(mParseValue, introspectedTable);
            FormatTools.addMethodWithBestPosition(innerEnum, mParseValue);

            // parseName 方法
            Method mParseName = JavaElementGeneratorTools.generateMethod(
                    "parseName",
                    JavaVisibility.PUBLIC,
                    innerEnum.getType(),
                    new Parameter(fName.getType(), "name")
            );
            mParseName.setStatic(true);
            mParseName.addBodyLine("if (name != null) {");
            mParseName.addBodyLine("for (" + innerEnum.getType().getShortName() + " item : values()) {");
            mParseName.addBodyLine("if (item.name.equals(name)) {");
            mParseName.addBodyLine("return item;");
            mParseName.addBodyLine("}");
            mParseName.addBodyLine("}");
            mParseName.addBodyLine("}");
            mParseName.addBodyLine("return null;");
            commentGenerator.addGeneralMethodComment(mParseName, introspectedTable);
            FormatTools.addMethodWithBestPosition(innerEnum, mParseName);

            return innerEnum;
        }


        public static class EnumItemInfo {
            private final IntrospectedColumn column;
            private final String name;
            private final String comment;
            private final String value;

            public EnumItemInfo(IntrospectedColumn column, String name, String comment, String value) {
                this.column = column;
                this.name = name.trim();
                this.comment = comment.trim();
                this.value = value.trim();
            }

            /**
             * Getter method for property <tt>comment</tt>.
             *
             * @return property value of comment
             * @author hewei
             */
            public String getComment() {
                return "\"" + comment + "\"";
            }

            /**
             * Getter method for property <tt>name</tt>.
             *
             * @return property value of name
             * @author hewei
             */
            public String getName() {
                return name.toUpperCase();
            }

            /**
             * Getter method for property <tt>value</tt>.
             *
             * @return property value of value
             * @author hewei
             */
            public String getValue() {
                String javaType = this.column.getFullyQualifiedJavaType().getShortName();
                if ("NULL".equalsIgnoreCase(value)) {
                    return "null";
                } else {
                    return javaType + ".valueOf(\"" + value + "\")";
                }
            }

            /**
             * Getter method for property <tt>value</tt>.
             *
             * @return property value of value
             * @author hewei
             */
            public String getOriginalValue() {
                return value;
            }
        }
    }
}
