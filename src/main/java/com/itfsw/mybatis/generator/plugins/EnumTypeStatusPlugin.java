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

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import com.itfsw.mybatis.generator.plugins.utils.FormatTools;
import com.itfsw.mybatis.generator.plugins.utils.IntrospectedTableTools;
import com.itfsw.mybatis.generator.plugins.utils.JavaElementGeneratorTools;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ---------------------------------------------------------------------------
 * type or status enum 插件
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2018/11/27 20:36
 * ---------------------------------------------------------------------------
 */
public class EnumTypeStatusPlugin extends BasePlugin {
    /**
     * 需要生成Enum的Column
     */
    public final static String PRO_ENUM_COLUMNS = "enumColumns";
    public final static String REMARKS_PATTERN = ".*\\[(\\w+\\([\\u4e00-\\u9fa5_a-zA-Z0-9]+\\):[\\u4e00-\\u9fa5_a-zA-Z0-9]+\\,?\\s*)+\\].*";
    public final static String NEED_PATTERN = "\\[((\\w+\\([\\u4e00-\\u9fa5_a-zA-Z0-9]+\\):[\\u4e00-\\u9fa5_a-zA-Z0-9]+\\,?\\s*)+)\\]";
    public final static String ITEM_PATTERN = "(\\w+)\\(([\\u4e00-\\u9fa5_a-zA-Z0-9]+)\\):([\\u4e00-\\u9fa5_a-zA-Z0-9]+)";
    private Map<IntrospectedColumn, List<EnumItemInfo>> enumColumns;

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param introspectedTable
     */
    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        super.initialized(introspectedTable);
        this.enumColumns = new HashMap<>();

        String enumColumns = introspectedTable.getTableConfigurationProperty(EnumTypeStatusPlugin.PRO_ENUM_COLUMNS);
        if (StringUtility.stringHasValue(enumColumns)) {
            // 切分
            String[] enumColumnsStrs = enumColumns.split(",");
            for (String enumColumnsStr : enumColumnsStrs) {
                IntrospectedColumn column = IntrospectedTableTools.safeGetColumn(introspectedTable, enumColumnsStr);
                String remarks = column.getRemarks();
                String javaType = column.getFullyQualifiedJavaType().getFullyQualifiedName();

                if (column == null) {
                    warnings.add("itfsw:插件" + EnumTypeStatusPlugin.class.getTypeName() + "没有找到column为" + enumColumnsStr.trim() + "的字段！");
                } else if (!StringUtility.stringHasValue(remarks) || !remarks.matches(REMARKS_PATTERN)) {
                    warnings.add("itfsw:插件" + EnumTypeStatusPlugin.class.getTypeName() + "没有找到column为" + enumColumnsStr.trim() + "对应格式的注释的字段！");
                } else if (!(Short.class.getTypeName().equals(javaType)
                        || Integer.class.getTypeName().equals(javaType)
                        || Long.class.getTypeName().equals(javaType)
                        || String.class.getTypeName().equals(javaType))) {
                    warnings.add("itfsw:插件" + EnumTypeStatusPlugin.class.getTypeName() + "找到column为" + enumColumnsStr.trim() + "对应Java类型不是Short、Integer、Long、String！");
                } else {
                    // 提取信息
                    Pattern pattern = Pattern.compile(NEED_PATTERN);
                    Matcher matcher = pattern.matcher(remarks);
                    List<EnumItemInfo> itemInfos = new ArrayList<>();
                    if (matcher.find() && matcher.groupCount() == 2) {
                        String enumInfoStr = matcher.group(1);
                        // 根据逗号切分
                        String[] enumInfoStrs = enumInfoStr.split(",");

                        // 提取每个节点信息
                        for (String enumInfo : enumInfoStrs) {
                            pattern = Pattern.compile(ITEM_PATTERN);
                            matcher = pattern.matcher(enumInfo.trim());
                            if (matcher.find() && matcher.groupCount() == 3) {
                                itemInfos.add(new EnumItemInfo(column, matcher.group(1), matcher.group(3), matcher.group(2)));
                            }
                        }
                    }

                    if (itemInfos.size() > 0) {
                        this.enumColumns.put(column, itemInfos);
                    }
                }
            }
        }
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.generateModelEnum(topLevelClass, introspectedTable);
        return super.modelPrimaryKeyClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * 生成对应enum
     * @param topLevelClass
     * @param introspectedTable
     */
    private void generateModelEnum(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.enumColumns.forEach((introspectedColumn, enumItemInfos) -> {
            // 枚举跟随字段走
            for (Field field : topLevelClass.getFields()) {
                if (introspectedColumn.getJavaProperty().equals(field.getName())) {
                    String enumName = this.upperCaseFirst(field.getName());

                    InnerEnum innerEnum = new InnerEnum(new FullyQualifiedJavaType(enumName));
                    commentGenerator.addEnumComment(innerEnum, introspectedTable);
                    innerEnum.setVisibility(JavaVisibility.PUBLIC);
                    innerEnum.setStatic(true);

                    // 生成枚举
                    for (EnumItemInfo item : enumItemInfos) {
                        // TODO 没有增加枚举注释的方法，hack
                        innerEnum.addEnumConstant(item.getComment() + item.getName() + "(" + item.getValue() + ")");
                    }

                    // 生成属性和构造函数
                    Field fValue = new Field("value", field.getType());
                    fValue.setVisibility(JavaVisibility.PRIVATE);
                    fValue.setFinal(true);
                    commentGenerator.addFieldComment(fValue, introspectedTable);
                    innerEnum.addField(fValue);

                    Method mInc = new Method(enumName);
                    mInc.setConstructor(true);
                    mInc.addBodyLine("this.value = value;");
                    mInc.addParameter(new Parameter(field.getType(), "value"));
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

                    topLevelClass.addInnerEnum(innerEnum);
                }
            }
        });
    }

    /**
     * Model 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.generateModelEnum(topLevelClass, introspectedTable);
        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * 首字母大写
     * @param s
     * @return
     */
    private String upperCaseFirst(String s) {
        if (Character.isUpperCase(s.charAt(0))) {
            return s;
        } else {
            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
        }
    }


    private class EnumItemInfo {
        private IntrospectedColumn column;
        private String name;
        private String comment;
        private String value;

        public EnumItemInfo(IntrospectedColumn column, String name, String comment, String value) {
            this.column = column;
            this.name = name;
            this.comment = comment;
            this.value = value;
        }

        /**
         * Getter method for property <tt>comment</tt>.
         * @return property value of comment
         * @author hewei
         */
        public String getComment() {
            return "// " + comment + "\r\n        ";
        }

        /**
         * Getter method for property <tt>name</tt>.
         * @return property value of name
         * @author hewei
         */
        public String getName() {
            return name.trim().toUpperCase();
        }

        /**
         * Getter method for property <tt>value</tt>.
         * @return property value of value
         * @author hewei
         */
        public String getValue() {
            String javaType = this.column.getFullyQualifiedJavaType().getFullyQualifiedName();
            if (Short.class.getTypeName().equals(javaType)) {
                return "(short)" + value;
            } else if (Long.class.getTypeName().equals(javaType)) {
                return value + "L";
            } else if (String.class.getTypeName().equals(javaType)) {
                return "\"" + value + "\"";
            }
            return value;
        }
    }
}
