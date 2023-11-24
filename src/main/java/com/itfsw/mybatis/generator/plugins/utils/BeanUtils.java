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

import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.JDBCConnectionConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BeanUtils {
    /**
     * 设置属性
     */
    public static void setProperty(final Object bean, final String name, final Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = bean.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(bean, value);
    }

    /**
     * 获取属性
     */
    public static Object getProperty(final Object bean, final String name) throws NoSuchFieldException, IllegalAccessException {
        Field field = bean.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(bean);
    }

    /**
     * 获取属性
     */
    public static Object getProperty(final Class clazz, final Object bean, final String name) throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(bean);
    }

    /**
     * 执行无参方法
     */
    public static Object invoke(final Object bean, Class clazz, final String name) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = clazz.getDeclaredMethod(name);
        method.setAccessible(true);
        return method.invoke(bean);
    }

    public static JDBCConnectionConfiguration getJdbcConnectionConfiguration(Context context) {
        try {
            return (JDBCConnectionConfiguration) getProperty(context, "jdbcConnectionConfiguration");
        } catch (Exception e) {
            throw new RuntimeException("无法获取到jdbcConnectionConfiguration", e);
        }
    }
}
