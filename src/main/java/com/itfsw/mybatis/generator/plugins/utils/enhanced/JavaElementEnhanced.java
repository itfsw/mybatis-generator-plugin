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

import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.Method;

import java.util.List;

/**
 * ---------------------------------------------------------------------------
 * JavaElement 增强
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/6/19 16:34
 * ---------------------------------------------------------------------------
 */
public class JavaElementEnhanced {
    /**
     * 在最佳位置添加方法
     *
     * @param innerClass
     * @param method
     */
    public static void addMethodWithBestPosition(InnerClass innerClass, Method method){
        List<Method> methods = innerClass.getMethods();
        int index = -1;
        for (int i = 0; i < methods.size(); i++){
            Method m = methods.get(i);
            if (m.getName().equals(method.getName())){
                if (m.getParameters().size() <= method.getParameters().size() || index == -1){
                    index = i;
                }
            }
        }

        if (index == -1){
            innerClass.addMethod(method);
        } else {
            methods.add(index, method);
        }
    }
}
