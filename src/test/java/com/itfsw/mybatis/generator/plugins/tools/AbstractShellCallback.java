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

import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.exception.ShellException;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/6/27 11:46
 * ---------------------------------------------------------------------------
 */
public abstract class AbstractShellCallback implements ShellCallback {
    /**
     * The overwrite.
     */
    private boolean overwrite;

    /**
     * Instantiates a new default shell callback.
     * @param overwrite the overwrite
     */
    public AbstractShellCallback(boolean overwrite) {
        super();
        this.overwrite = overwrite;
    }

    /**
     * 动态编译
     * @param project
     */
    @Override
    public void refreshProject(String project) {
        List<File> files = getJavaFiles(new File(project + "/com/itfsw/mybatis/generator/plugins/dao"));
        if (!files.isEmpty()) {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

            //获取java文件管理类
            StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null);
            //获取java文件对象迭代器
            Iterable<? extends JavaFileObject> it = manager.getJavaFileObjectsFromFiles(files);
            //设置编译参数
            ArrayList<String> ops = new ArrayList<>();
            ops.add("-Xlint:unchecked");
            // 设置输出目录
            ops.add("-d");
            ops.add(this.getClass().getClassLoader().getResource("").getPath());
            //获取编译任务
            JavaCompiler.CompilationTask task = compiler.getTask(null, manager, null, ops, null, it);
            //执行编译任务
            task.call();

        }
        reloadProject(this.getClass().getClassLoader());
    }

    public abstract void reloadProject(ClassLoader loader);

    /**
     * 获取JAVA 文件
     * @param file
     * @return
     */
    private List<File> getJavaFiles(File file) {
        List<File> list = new ArrayList<>();
        if (file.exists()) {
            File[] files = file.listFiles();
            for (File childFile : files) {
                if (childFile.isDirectory()) {
                    list.addAll(getJavaFiles(childFile));
                } else if (childFile.getName().endsWith(".java")) {
                    list.add(childFile);
                }
            }
        }
        return list;
    }

    /* (non-Javadoc)
         * @see org.mybatis.generator.api.ShellCallback#getDirectory(java.lang.String, java.lang.String)
         */
    public File getDirectory(String targetProject, String targetPackage)
            throws ShellException {
        // targetProject is interpreted as a directory that must exist
        //
        // targetPackage is interpreted as a sub directory, but in package
        // format (with dots instead of slashes). The sub directory will be
        // created
        // if it does not already exist

        File project = new File(targetProject);
        if (!project.isDirectory()) {
            throw new ShellException(getString("Warning.9", //$NON-NLS-1$
                    targetProject));
        }

        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(targetPackage, "."); //$NON-NLS-1$
        while (st.hasMoreTokens()) {
            sb.append(st.nextToken());
            sb.append(File.separatorChar);
        }

        File directory = new File(project, sb.toString());
        if (!directory.isDirectory()) {
            boolean rc = directory.mkdirs();
            if (!rc) {
                throw new ShellException(getString("Warning.10", //$NON-NLS-1$
                        directory.getAbsolutePath()));
            }
        }

        return directory;
    }

    /* (non-Javadoc)
     * @see org.mybatis.generator.api.ShellCallback#isMergeSupported()
     */
    public boolean isMergeSupported() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.mybatis.generator.api.ShellCallback#isOverwriteEnabled()
     */
    public boolean isOverwriteEnabled() {
        return overwrite;
    }

    /* (non-Javadoc)
     * @see org.mybatis.generator.api.ShellCallback#mergeJavaFile(java.lang.String, java.lang.String, java.lang.String[], java.lang.String)
     */
    public String mergeJavaFile(String newFileSource,
                                String existingFileFullPath, String[] javadocTags, String fileEncoding)
            throws ShellException {
        throw new UnsupportedOperationException();
    }
}
