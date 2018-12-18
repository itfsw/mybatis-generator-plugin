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

package com.itfsw.mybatis.generator.plugins.utils.enhanced;

import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.*;

import java.util.Iterator;
import java.util.Set;

import static org.mybatis.generator.api.dom.OutputUtilities.*;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * ---------------------------------------------------------------------------
 * 内部接口
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/1/12 17:53
 * ---------------------------------------------------------------------------
 */
public class InnerInterface extends Interface{
    public InnerInterface(FullyQualifiedJavaType type) {
        super(type);
    }

    public InnerInterface(String type) {
        super(type);
    }

    /**
     * 格式化后内容，因为是内部接口，需要增加缩进
     *
     * @param indentLevel the indent level
     * @param compilationUnit the compilation unit
     * @return the formatted content
     */
    @Override
    public String getFormattedContent(int indentLevel, CompilationUnit compilationUnit) {
        StringBuilder sb = new StringBuilder();

        for (String commentLine : getFileCommentLines()) {
            sb.append(commentLine);
            newLine(sb);
        }

        if (stringHasValue(getType().getPackageName())) {
            sb.append("package "); 
            sb.append(getType().getPackageName());
            sb.append(';');
            newLine(sb);
            newLine(sb);
        }

        for (String staticImport : getStaticImports()) {
            sb.append("import static "); 
            sb.append(staticImport);
            sb.append(';');
            newLine(sb);
        }

        if (getStaticImports().size() > 0) {
            newLine(sb);
        }

        Set<String> importStrings = calculateImports(getImportedTypes());
        for (String importString : importStrings) {
            sb.append(importString);
            newLine(sb);
        }

        if (importStrings.size() > 0) {
            newLine(sb);
        }

        addFormattedJavadoc(sb, indentLevel);
        addFormattedAnnotations(sb, indentLevel);

        OutputUtilities.javaIndent(sb, indentLevel);

        sb.append(getVisibility().getValue());

        if (isFinal()) {
            sb.append("final "); 
        }

        sb.append("interface "); 
        sb.append(getType().getShortName());

        if (getSuperInterfaceTypes().size() > 0) {
            sb.append(" extends "); 

            boolean comma = false;
            for (FullyQualifiedJavaType fqjt : getSuperInterfaceTypes()) {
                if (comma) {
                    sb.append(", "); 
                } else {
                    comma = true;
                }

                sb.append(JavaDomUtils.calculateTypeName(this, fqjt));
            }
        }

        sb.append(" {"); 
        indentLevel++;

        Iterator<Method> mtdIter = getMethods().iterator();
        while (mtdIter.hasNext()) {
            newLine(sb);
            Method method = mtdIter.next();
            sb.append(method.getFormattedContent(indentLevel, true, this));
            if (mtdIter.hasNext()) {
                newLine(sb);
            }
        }

        indentLevel--;
        newLine(sb);
        javaIndent(sb, indentLevel);
        sb.append('}');

        return sb.toString();
    }
}
