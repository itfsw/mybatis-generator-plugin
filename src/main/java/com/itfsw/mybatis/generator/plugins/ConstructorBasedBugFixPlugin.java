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

package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import com.itfsw.mybatis.generator.plugins.utils.FormatTools;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

/**
 * ---------------------------------------------------------------------------
 * constructorBased 官方 bug 修正插件
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/7/28 11:05
 * ---------------------------------------------------------------------------
 */
public class ConstructorBasedBugFixPlugin extends BasePlugin {

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        // 有种情况下ModelBaseRecordClass不会生成不包含BLOBs的构造方法
        if (introspectedTable.isConstructorBased() && introspectedTable.hasBLOBColumns() && introspectedTable.getBLOBColumns().size() == 1) {
            // 判断是否已经生成了对应构造函数
            String version = Plugin.class.getPackage().getImplementationVersion();
            if (version != null){
                String[] strs = version.split("\\.");
                if (strs.length == 3 && strs[0].equals("1") && strs[1].equals("3") && Integer.parseInt(strs[2]) < 6){
                    // 添加没有BLOBs的构造方法
                    Method method = new Method();
                    method.setVisibility(JavaVisibility.PUBLIC);
                    method.setConstructor(true);
                    method.setName(topLevelClass.getType().getShortName());
                    commentGenerator.addGeneralMethodComment(method, introspectedTable);

                    // 使用没有blobs的字段
                    List<IntrospectedColumn> constructorColumns = introspectedTable.getNonBLOBColumns();

                    for (IntrospectedColumn introspectedColumn : constructorColumns) {
                        method.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(), introspectedColumn.getJavaProperty()));
                        topLevelClass.addImportedType(introspectedColumn.getFullyQualifiedJavaType());
                    }

                    StringBuilder sb = new StringBuilder();
                    if (introspectedTable.getRules().generatePrimaryKeyClass()) {
                        boolean comma = false;
                        sb.append("super(");
                        for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                            if (comma) {
                                sb.append(", ");
                            } else {
                                comma = true;
                            }
                            sb.append(introspectedColumn.getJavaProperty());
                        }
                        sb.append(");");
                        method.addBodyLine(sb.toString());
                    }

                    List<IntrospectedColumn> introspectedColumns;
                    if (!introspectedTable.getRules().generatePrimaryKeyClass() && introspectedTable.hasPrimaryKeyColumns()) {
                        introspectedColumns = introspectedTable.getNonBLOBColumns();
                    } else {
                        introspectedColumns = introspectedTable.getBaseColumns();
                    }

                    for (IntrospectedColumn introspectedColumn : introspectedColumns) {
                        sb.setLength(0);
                        sb.append("this.");
                        sb.append(introspectedColumn.getJavaProperty());
                        sb.append(" = ");
                        sb.append(introspectedColumn.getJavaProperty());
                        sb.append(';');
                        method.addBodyLine(sb.toString());
                    }

                    FormatTools.addMethodWithBestPosition(topLevelClass, method);
                }
            }
        }

        return true;
    }
}
