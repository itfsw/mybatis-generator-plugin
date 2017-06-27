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

package com.itfsw.mybatis.generator.plugins.tools;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/6/27 16:47
 * ---------------------------------------------------------------------------
 */
public class Util {

    /**
     * 获取List 泛型参数
     *
     * @param type
     * @return
     */
    public static String getListActualType(Type type){
        if(type instanceof ParameterizedType){
            Type[] actualTypeArguments = ((ParameterizedType)type).getActualTypeArguments();
            if (actualTypeArguments.length == 1){
                return actualTypeArguments[0].getTypeName();
            }
        }
        return null;
    }
}
