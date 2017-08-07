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

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/8/2 16:55
 * ---------------------------------------------------------------------------
 */
public class InnerTypeFullyQualifiedJavaType extends FullyQualifiedJavaType {
    private final static Logger logger = LoggerFactory.getLogger(InnerTypeFullyQualifiedJavaType.class);
    private String outerType;   // 内部类
    /**
     * Use this constructor to construct a generic type with the specified type parameters.
     * @param fullTypeSpecification the full type specification
     */
    public InnerTypeFullyQualifiedJavaType(String fullTypeSpecification){
        super(fullTypeSpecification);

        try{
            // 修正package
            java.lang.reflect.Field packageName = this.getClass().getSuperclass().getDeclaredField("packageName");
            packageName.setAccessible(true);
            String oldPackageName = getPackageName();
            packageName.set(this, oldPackageName.substring(0, oldPackageName.lastIndexOf(".")));

            outerType = oldPackageName.substring(oldPackageName.lastIndexOf(".") + 1);
        } catch (Exception e){
            logger.error("InnerTypeFullyQualifiedJavaType 赋值失败！", e);
        }
    }

    /**
     * This method returns the fully qualified name - including any generic type parameters.
     *
     * @return Returns the fullyQualifiedName.
     */
    @Override
    public String getFullyQualifiedName() {
        String fullyQualifiedName = super.getFullyQualifiedName();
        String before = fullyQualifiedName.substring(0, fullyQualifiedName.lastIndexOf("."));
        String end = fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf("."));
        return before + "." + outerType + end;
    }

    /**
     * Gets the short name.
     *
     * @return Returns the shortName - including any type arguments.
     */
    @Override
    public String getShortName() {
        return outerType + "." + super.getShortName();
    }
}
