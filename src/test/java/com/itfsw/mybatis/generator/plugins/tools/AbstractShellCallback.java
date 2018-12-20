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

import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.exception.ShellException;

import java.io.File;
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
    private MyBatisGeneratorTool tool;  // MyBatisGenerator 工具

    /**
     * Setter method for property <tt>tool</tt>.
     * @param tool value to be assigned to property tool
     * @author hewei
     */
    public void setTool(MyBatisGeneratorTool tool) {
        this.tool = tool;
    }

    /**
     * 动态编译
     * @param project
     */
    @Override
    public void refreshProject(String project) {
        // 编译项目
        try (SqlSession sqlSession = tool.compile()){
            reloadProject(sqlSession, tool.getTargetClassLoader(), tool.getTargetPackage());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    /**
     * 重载项目
     * @param sqlSession
     * @param loader
     * @param packagz
     * @throws Exception
     */
    public abstract void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception;

    /* (non-Javadoc)
         * @see org.mybatis.generator.api.ShellCallback#getDirectory(java.lang.String, java.lang.String)
         */
    public File getDirectory(String targetProject, String targetPackage) throws ShellException {
        // targetProject is interpreted as a directory that must exist
        //
        // targetPackage is interpreted as a sub directory, but in package
        // format (with dots instead of slashes). The sub directory will be
        // created
        // if it does not already exist

        File project = new File(targetProject);
        if (!project.isDirectory()) {
            throw new ShellException(getString("Warning.9", targetProject));
        }

        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(targetPackage, ".");
        while (st.hasMoreTokens()) {
            sb.append(st.nextToken());
            sb.append(File.separatorChar);
        }

        File directory = new File(project, sb.toString());
        if (!directory.isDirectory()) {
            boolean rc = directory.mkdirs();
            if (!rc) {
                throw new ShellException(getString("Warning.10", directory.getAbsolutePath()));
            }
        }

        return directory;
    }

    /* (non-Javadoc)
     * @see org.mybatis.generator.api.ShellCallback#isMergeSupported()
     */
    @Override
    public boolean isMergeSupported() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.mybatis.generator.api.ShellCallback#isOverwriteEnabled()
     */
    @Override
    public boolean isOverwriteEnabled() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.mybatis.generator.api.ShellCallback#mergeJavaFile(java.lang.String, java.lang.String, java.lang.String[], java.lang.String)
     */
    @Override
    public String mergeJavaFile(String newFileSource, File existingFile,
                                String[] javadocTags, String fileEncoding) throws ShellException {
        throw new UnsupportedOperationException();
    }
}
