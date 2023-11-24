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

package com.itfsw.mybatis.generator.plugins.utils;


import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TypeParameter;

public class JavaElementTools {
    /**
     * clone
     */
    public static Method clone(Method method) {
        Method dest = new Method(method.getName());
        // 注解
        for (String javaDocLine : method.getJavaDocLines()) {
            dest.addJavaDocLine(javaDocLine);
        }
        if (method.getReturnType().isPresent()){
            dest.setReturnType(method.getReturnType().get());
        }
        for (Parameter parameter : method.getParameters()) {
            dest.addParameter(JavaElementTools.clone(parameter));
        }
        for (FullyQualifiedJavaType exception : method.getExceptions()) {
            dest.addException(exception);
        }
        for (TypeParameter typeParameter : method.getTypeParameters()) {
            dest.addTypeParameter(typeParameter);
        }
        dest.addBodyLines(method.getBodyLines());
        dest.setConstructor(method.isConstructor());
        dest.setNative(method.isNative());
        dest.setSynchronized(method.isSynchronized());
        dest.setDefault(method.isDefault());
        dest.setFinal(method.isFinal());
        dest.setStatic(method.isStatic());
        dest.setVisibility(method.getVisibility());
        return dest;
    }

    /**
     * clone
     */
    public static Parameter clone(Parameter parameter) {
        Parameter dest = new Parameter(parameter.getType(), parameter.getName(), parameter.isVarargs());
        for (String annotation : parameter.getAnnotations()) {
            dest.addAnnotation(annotation);
        }
        return dest;
    }
}
