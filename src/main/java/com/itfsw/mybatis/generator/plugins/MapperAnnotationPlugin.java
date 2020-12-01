/*
 * Copyright (c) 2019.
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

package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import com.sun.xml.internal.ws.util.StringUtils;
import freemarker.template.utility.CollectionUtils;
import freemarker.template.utility.StringUtil;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.*;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 *
 * @author: hewei
 * @time:2019/7/9 14:30
 * ---------------------------------------------------------------------------
 */
public class MapperAnnotationPlugin extends BasePlugin {

	/**
	 * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
	 * @param introspectedTable
	 * @return
	 */
	@Override
	public void initialized(IntrospectedTable introspectedTable) {
		super.initialized(introspectedTable);
	}

	/**
	 * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
	 * @param interfaze
	 * @param topLevelClass
	 * @param introspectedTable
	 * @return
	 */
	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass,
			IntrospectedTable introspectedTable) {

		Properties properties = getProperties();
		String annotationName;
		String annotationImport;
		for (Map.Entry<Object, Object> prop : properties.entrySet()) {
			annotationName = (String) prop.getKey();
			annotationImport = (String) prop.getValue();
			interfaze.addImportedType(new FullyQualifiedJavaType(annotationImport));
			interfaze.addAnnotation(annotationName);
		}

		return true;
	}

}
