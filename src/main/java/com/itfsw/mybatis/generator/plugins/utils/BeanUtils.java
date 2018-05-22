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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2018/5/3 18:39
 * ---------------------------------------------------------------------------
 */
public class BeanUtils {
    /**
     * 设置属性
     * @param bean
     * @param name
     * @param value
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static void setProperty(final Object bean, final String name, final Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = bean.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(bean, value);
    }

    /**
     * 获取属性
     * @param bean
     * @param name
     * @return
     */
    public static Object getProperty(final Object bean, final String name) throws NoSuchFieldException, IllegalAccessException {
        Field field = bean.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(bean);
    }

    /**
     * 执行无参方法
     * @param bean
     * @param clazz
     * @param name
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static Object invoke(final Object bean, Class clazz, final String name) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = clazz.getDeclaredMethod(name);
        method.setAccessible(true);
        return method.invoke(bean);
    }
}
