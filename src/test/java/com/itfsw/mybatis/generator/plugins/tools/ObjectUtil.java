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


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/6/28 14:26
 * ---------------------------------------------------------------------------
 */
public class ObjectUtil {
    private Object object;  // 对象
    private Class cls;  // 类

    /**
     * 构造函数(枚举#分隔)
     * @param loader
     * @param cls
     */
    public ObjectUtil(ClassLoader loader, String cls) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        if (cls.indexOf("#") == -1) {
            this.cls = loader.loadClass(cls);
            this.object = this.cls.newInstance();
        } else {
            String[] strs = cls.split("#");
            this.cls = loader.loadClass(strs[0]);
            if (this.cls.isEnum()) {
                Object[] constants = this.cls.getEnumConstants();
                for (Object object : constants) {
                    ObjectUtil eObject = new ObjectUtil(object);
                    if (strs[1].equals(eObject.invoke("name"))) {
                        this.object = object;
                        break;
                    }
                }
            } else {
                throw new ClassNotFoundException("没有找到对应枚举" + strs[0]);
            }
        }
    }

    /**
     * 构造函数
     * @param object
     */
    public ObjectUtil(Object object) {
        this.object = object;
        this.cls = object.getClass();
    }

    /**
     * 设置值
     * @param filedName
     * @param value
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public ObjectUtil set(String filedName, Object value) throws IllegalAccessException {
        String[] strs = filedName.split("\\.");
        if (strs.length > 1) {
            Object obj = this.get(strs[0]);
            new ObjectUtil(obj).set(filedName.replaceFirst("\\w+\\.", ""), value);
        } else {
            Field field = this.getDeclaredField(filedName);
            field.setAccessible(true);
            field.set(this.object, value);
        }

        return this;
    }

    /**
     * 获取值
     * @param filedName
     * @return
     * @throws IllegalAccessException
     */
    public Object get(String filedName) throws IllegalAccessException {
        Field field = this.getDeclaredField(filedName);
        field.setAccessible(true);
        return field.get(this.object);
    }

    /**
     * Getter method for property <tt>object</tt>.
     * @return property value of object
     * @author hewei
     */
    public Object getObject() {
        return object;
    }

    /**
     * 执行方法(mapper动态代理后VarArgs检查有问题)
     * @param methodName
     * @param args
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public Object invoke(String methodName, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<Method> methods = getMethods(methodName);
        for (Method method : methods) {
            if (method.getParameters().length == 1 && args == null) {
                method.setAccessible(true);
                return method.invoke(this.object, new Object[]{null});
            } else if (method.getParameterTypes().length == args.length) {
                boolean flag = true;
                Class[] parameterTypes = method.getParameterTypes();
                // !! mapper动态代理后VarArgs检查有问题
                // 暂时只检查前几位相同就假设为可变参数
                int check = parameterTypes.length > 0 ? parameterTypes.length - (parameterTypes[parameterTypes.length - 1].getName().startsWith("[") ? 1 : 0) : 0;
                for (int i = 0; i < check; i++) {
                    Class parameterType = parameterTypes[i];
                    if (args[i] != null && !(parameterType.isAssignableFrom(args[i].getClass()))) {
                        flag = false;
                    }
                    // 基础类型
                    if (parameterType.isPrimitive()) {
                        switch (parameterType.getTypeName()) {
                            case "boolean":
                                flag = args[i] instanceof Boolean;
                                break;
                            default:
                                flag = false;
                        }
                    }
                }

                if (flag) {
                    method.setAccessible(true);
                    return method.invoke(this.object, args);
                }
            }
        }
        throw new NoSuchMethodError("没有找到方法：" + methodName);
    }

    /**
     * 获取指定名称的方法
     * @param name
     * @return
     */
    public List<Method> getMethods(String name) {
        List<Method> list = new ArrayList<>();
        Class clazz = this.cls;
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(name)) {
                    list.add(method);
                }
            }
        }
        return list;
    }

    /**
     * Getter method for property <tt>cls</tt>.
     * @return property value of cls
     * @author hewei
     */
    public Class getCls() {
        return cls;
    }

    /**
     * 递归获取所有属性
     * @param name
     * @return
     */
    private Field getDeclaredField(String name) {
        Class<?> clazz = this.cls;
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                // 不能操作，递归父类
            }
        }
        return null;
    }
}
