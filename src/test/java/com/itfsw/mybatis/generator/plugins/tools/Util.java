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

import java.io.*;
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
     * @param type
     * @return
     */
    public static String getListActualType(Type type) {
        if (type instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            if (actualTypeArguments.length == 1) {
                return actualTypeArguments[0].getTypeName();
            }
        }
        return null;
    }

    /**
     * 文件拷贝
     * @param src
     * @param dist
     * @param overwrite
     * @return
     */
    public static int copyFile(File src, File dist, boolean overwrite) throws IOException {
        if (src.exists() && src.isFile()) {
            if (dist.exists()){
                if (overwrite){
                    dist.delete();
                } else {
                    throw new IOException("目标文件已经存在：" + dist.getPath());
                }
            }

            // 创建目标文件夹
            if (!dist.getParentFile().exists() || dist.getParentFile().isFile()) {
                dist.mkdirs();
            }
            // 创建目标文件
            dist.createNewFile();

            // 进行拷贝操作
            int byteCount = 0;
            InputStream in = null;
            OutputStream out = null;
            try {
                in = new FileInputStream(src);
                out = new FileOutputStream(dist);

                byte[] buffer = new byte[4096];
                int bytesRead1;
                for (boolean bytesRead = true; (bytesRead1 = in.read(buffer)) != -1; byteCount += bytesRead1) {
                    out.write(buffer, 0, bytesRead1);
                }

                out.flush();
            } catch (Exception e){
                out.close();
                dist.delete();
            }finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }

            return byteCount;
        }
        throw new IOException("没有找到对应文件：" + src);
    }
}
