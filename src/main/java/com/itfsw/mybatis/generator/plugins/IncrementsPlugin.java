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

package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import com.itfsw.mybatis.generator.plugins.utils.PluginTools;

import java.util.List;

/**
 * ---------------------------------------------------------------------------
 * 增量插件
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/6/19 15:20
 * ---------------------------------------------------------------------------
 */
public class IncrementsPlugin extends BasePlugin {
    public static final String PRE_INCREMENTS_COLUMNS = "incrementsColumns";  // incrementsColumns property

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(List<String> warnings) {

        // 插件使用前提是使用了ModelBuilderPlugin插件
        if (!PluginTools.checkDependencyPlugin(getContext(), ModelBuilderPlugin.class)) {
            logger.error("itfsw:插件" + this.getClass().getTypeName() + "插件需配合com.itfsw.mybatis.generator.plugins.ModelBuilderPlugin插件使用！");
            return false;
        }

        return super.validate(warnings);
    }
}
