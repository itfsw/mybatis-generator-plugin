package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 重写 mapper.xml 插件
 *
 * @author durenhao
 * @date 2020/5/30 22:03
 **/
public class OverwrittenMapperXmlPlugin extends BasePlugin {

    private static final Logger log = LoggerFactory.getLogger(OverwrittenMapperXmlPlugin.class);

    /**
     * 测试提交
     *
     * @param sqlMap
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapGenerated(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
        String dir = sqlMap.getTargetPackage();
        if (dir.indexOf('.') != -1) {
            dir = dir.replace(".", File.separator);
        }

        String fileName = sqlMap.getTargetProject() + File.separator + dir + File.separator + sqlMap.getFileName();
        File file = new File(fileName);

        if (file.exists()) {
            if (!file.delete()) {
                log.warn("覆盖原有xml文件: {} 失败!", fileName);
            }
            log.warn("Existing file {}  was overwritten ", file);
        }

        super.sqlMapGenerated(sqlMap, introspectedTable);
        return true;
    }
}
