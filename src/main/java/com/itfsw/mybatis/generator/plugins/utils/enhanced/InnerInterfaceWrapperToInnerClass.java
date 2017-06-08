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

import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;

/**
 * ---------------------------------------------------------------------------
 * 把InnerInterface包装成InnerClass(Mybatis Generator 没有提供内部接口实现)
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/1/12 17:40
 * ---------------------------------------------------------------------------
 */
public class InnerInterfaceWrapperToInnerClass extends InnerClass{
    private InnerInterface innerInterface;  // 内部接口


    public InnerInterfaceWrapperToInnerClass(FullyQualifiedJavaType type) {
        super(type);
    }

    public InnerInterfaceWrapperToInnerClass(String typeName) {
        super(typeName);
    }

    public InnerInterfaceWrapperToInnerClass(InnerInterface innerInterface){
        super(innerInterface.getType());
        this.innerInterface = innerInterface;
    }

    /**
     * 重写获取Java内容方法，调用InnerInterface的实现
     *
     * @param indentLevel
     * @param compilationUnit
     * @return
     */
    @Override
    public String getFormattedContent(int indentLevel, CompilationUnit compilationUnit) {
        return this.innerInterface.getFormattedContent(indentLevel, compilationUnit);
    }

    /**
     * Getter method for property <tt>innerInterface</tt>.
     * @return property value of innerInterface
     * @author hewei
     */
    public InnerInterface getInnerInterface() {
        return innerInterface;
    }

    /**
     * Setter method for property <tt>innerInterface</tt>.
     * @param innerInterface value to be assigned to property innerInterface
     * @author hewei
     */
    public void setInnerInterface(InnerInterface innerInterface) {
        this.innerInterface = innerInterface;
    }
}
