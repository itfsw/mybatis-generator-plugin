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

import java.util.List;

/**
 * ---------------------------------------------------------------------------
 * table前缀，解决多数据源表重名问题插件（为Model、Mapper、Example、xml等增加前缀）
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/5/18 13:54
 * ---------------------------------------------------------------------------
 */
@Deprecated
public class TableSuffixPlugin extends BasePlugin {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(List<String> warnings) {
        logger.error("itfsw:插件" + this.getClass().getTypeName() + "插件已经过期，请使用TablePrefixPlugin插件替换(请原谅我蹩脚的英文水平)！");

        return false;
    }
}
