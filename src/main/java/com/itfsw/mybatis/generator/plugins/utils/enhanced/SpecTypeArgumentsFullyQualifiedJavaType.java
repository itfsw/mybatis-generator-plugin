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

package com.itfsw.mybatis.generator.plugins.utils.enhanced;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2018/11/2 18:21
 * ---------------------------------------------------------------------------
 */
public class SpecTypeArgumentsFullyQualifiedJavaType extends FullyQualifiedJavaType {
    private String fullTypeSpecification;
    /**
     * Use this constructor to construct a generic type with the specified type parameters.
     * @param fullTypeSpecification the full type specification
     */
    public SpecTypeArgumentsFullyQualifiedJavaType(String fullTypeSpecification) {
        super("");

        this.fullTypeSpecification = fullTypeSpecification;
    }

    @Override
    public String getShortName() {
        return this.fullTypeSpecification.substring(1, this.fullTypeSpecification.length() - 1);
    }
}
