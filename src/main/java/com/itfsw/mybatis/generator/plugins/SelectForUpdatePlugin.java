package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import com.itfsw.mybatis.generator.plugins.utils.FormatTools;
import com.itfsw.mybatis.generator.plugins.utils.JavaElementGeneratorTools;
import com.itfsw.mybatis.generator.plugins.utils.XmlElementTools;
import com.itfsw.mybatis.generator.plugins.utils.hook.ISelectOneByExamplePluginHook;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.internal.PluginAggregator;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 悲观锁插件
 *
 * @author durenhao
 * @date 2021/4/15 21:33
 **/
public class SelectForUpdatePlugin extends BasePlugin implements ISelectOneByExamplePluginHook {

    public static final String METHOD_SELECT_PRIMARY_KEY_FOR_UPDATE = "selectByPrimaryKeyForUpdate";  // 方法名

    public static final String METHOD_SELECT_BY_EXAMPLE_FOR_UPDATE = "selectByExampleForUpdate";  // 方法名

    public static final String METHOD_SELECT_BY_EXAMPLE_WITH_BLOBS_FOR_UPDATE = "selectByExampleWithBLOBsForUpdate";  // 方法名

    public static final String METHOD_SELECT_ONE_BY_EXAMPLE_FOR_UPDATE = "selectOneByExampleForUpdate";  // 方法名

    public static final String METHOD_SELECT_ONE_BY_EXAMPLE_WITH_BLOBS_FOR_UPDATE = "selectOneByExampleWithBLOBsForUpdate";  // 方法名

    private XmlElement selectByPrimaryKeyForUpdateEle;

    private XmlElement selectByExampleForUpdateEle;

    private XmlElement selectByExampleWithBLOBsForUpdateEle;

    private XmlElement selectOneByExampleForUpdateEle;

    private XmlElement selectOneByExampleWithBLOBsForUpdateEle;


    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        super.initialized(introspectedTable);
        try {
            validatePluginOrder();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.selectByPrimaryKeyForUpdateEle = null;

        this.selectByExampleForUpdateEle = null;
        this.selectByExampleWithBLOBsForUpdateEle = null;

        this.selectOneByExampleForUpdateEle = null;
        this.selectOneByExampleWithBLOBsForUpdateEle = null;
    }


    @Override
    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType baseRecordType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        FullyQualifiedJavaType listType = new FullyQualifiedJavaType("java.util.List");
        listType.addTypeArgument(baseRecordType);

        Method selectMethod = JavaElementGeneratorTools.generateMethod(
                METHOD_SELECT_BY_EXAMPLE_FOR_UPDATE,
                JavaVisibility.DEFAULT,
                listType,
                new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example")
        );
        commentGenerator.addGeneralMethodComment(selectMethod, introspectedTable);

        // interface 增加方法
        FormatTools.addMethodWithBestPosition(interfaze, selectMethod);
        logger.debug("itfsw(forUpdate插件):" + interfaze.getType().getShortName() + "增加selectByExampleForUpdate方法。");
        return super.clientSelectByExampleWithoutBLOBsMethodGenerated(selectMethod, interfaze, introspectedTable);

    }

    @Override
    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType baseRecordType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        FullyQualifiedJavaType listType = new FullyQualifiedJavaType("java.util.List");
        listType.addTypeArgument(baseRecordType);

        Method selectMethod = JavaElementGeneratorTools.generateMethod(
                METHOD_SELECT_BY_EXAMPLE_WITH_BLOBS_FOR_UPDATE,
                JavaVisibility.DEFAULT,
                listType,
                new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example")
        );
        commentGenerator.addGeneralMethodComment(selectMethod, introspectedTable);

        // interface 增加方法
        FormatTools.addMethodWithBestPosition(interfaze, selectMethod);
        logger.debug("itfsw(forUpdate插件):" + interfaze.getType().getShortName() + "增加selectByExampleWithBLOBsForUpdate方法。");
        return super.clientSelectByExampleWithBLOBsMethodGenerated(method, interfaze, introspectedTable);

    }

    @Override
    public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
        if (primaryKeyColumns.size() != 1) {
            logger.warn("itfsw(forUpdate插件):" + "主键个数不为1,无法增加selectByPrimaryKeyForUpdate方法。");
            return true;
        }
        FullyQualifiedJavaType fullyQualifiedJavaType = primaryKeyColumns.get(0).getFullyQualifiedJavaType();
        Method selectMethod = JavaElementGeneratorTools.generateMethod(
                METHOD_SELECT_PRIMARY_KEY_FOR_UPDATE,
                JavaVisibility.DEFAULT,
                JavaElementGeneratorTools.getModelTypeWithBLOBs(introspectedTable),
                new Parameter(fullyQualifiedJavaType, "id")
        );

        commentGenerator.addGeneralMethodComment(selectMethod, introspectedTable);

        // interface 增加方法
        FormatTools.addMethodWithBestPosition(interfaze, selectMethod);
        logger.debug("itfsw(forUpdate插件):" + interfaze.getType().getShortName() + "增加selectByPrimaryKeyForUpdate方法。");
        return super.clientSelectByExampleWithBLOBsMethodGenerated(method, interfaze, introspectedTable);

    }


    @Override
    public boolean clientSelectOneByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        Method selectOneMethod = JavaElementGeneratorTools.generateMethod(
                METHOD_SELECT_ONE_BY_EXAMPLE_WITH_BLOBS_FOR_UPDATE,
                JavaVisibility.DEFAULT,
                JavaElementGeneratorTools.getModelTypeWithBLOBs(introspectedTable),
                new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example")
        );
        commentGenerator.addGeneralMethodComment(selectOneMethod, introspectedTable);

        // interface 增加方法
        FormatTools.addMethodWithBestPosition(interfaze, selectOneMethod);
        logger.debug("itfsw(forUpdate插件):" + interfaze.getType().getShortName() + "增加selectOneByExampleWithBLOBsForUpdate方法。");
        return super.clientSelectByExampleWithBLOBsMethodGenerated(method, interfaze, introspectedTable);
    }

    @Override
    public boolean clientSelectOneByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        // 方法生成 selectOneByExample
        Method selectOneMethod = JavaElementGeneratorTools.generateMethod(
                METHOD_SELECT_ONE_BY_EXAMPLE_FOR_UPDATE,
                JavaVisibility.DEFAULT,
                JavaElementGeneratorTools.getModelTypeWithoutBLOBs(introspectedTable),
                new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example")
        );
        commentGenerator.addGeneralMethodComment(selectOneMethod, introspectedTable);

        // interface 增加方法
        FormatTools.addMethodWithBestPosition(interfaze, selectOneMethod);
        logger.debug("itfsw(forUpdate插件):" + interfaze.getType().getShortName() + "增加selectOneByExampleForUpdate方法。");
        return super.clientSelectByExampleWithoutBLOBsMethodGenerated(selectOneMethod, interfaze, introspectedTable);
    }


    @Override
    public boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        XmlElement xmlElement = cloneAndAddForUpdateElement(element);
        xmlElement.addAttribute(new Attribute("id", METHOD_SELECT_PRIMARY_KEY_FOR_UPDATE));
        commentGenerator.addComment(xmlElement);
        this.selectByPrimaryKeyForUpdateEle = xmlElement;
        return super.sqlMapSelectByPrimaryKeyElementGenerated(element, introspectedTable);
    }

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param element
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {

        XmlElement xmlElement = cloneAndAddForUpdateElement(element);
        xmlElement.addAttribute(new Attribute("id", METHOD_SELECT_BY_EXAMPLE_FOR_UPDATE));
        commentGenerator.addComment(xmlElement);
        this.selectByExampleForUpdateEle = xmlElement;
        return super.sqlMapSelectByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
    }


    private void validatePluginOrder() throws Exception {
        PluginAggregator plugins = (PluginAggregator) getContext().getPlugins();
        Field field = PluginAggregator.class.getDeclaredField("plugins");
        field.setAccessible(true);
        List<Plugin> pluginList = (List<Plugin>) field.get(plugins);
        int forUpdateOrder = 0;
        int limitOrder = -1;
        for (int i = 0; i < pluginList.size(); i++) {
            if (pluginList.get(i) instanceof SelectForUpdatePlugin) {
                forUpdateOrder = i;
            }
            if (pluginList.get(i) instanceof LimitPlugin) {
                limitOrder = i;
            }
        }
        if (limitOrder == -1) {
            return;
        }
        if (limitOrder > forUpdateOrder) {
            throw new IllegalStateException("SelectForUpdatePlugin的顺序必须在LimitPlugin之后,请调整插件顺序");
        }
    }

    @Override
    public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        XmlElement xmlElement = cloneAndAddForUpdateElement(element);
        xmlElement.addAttribute(new Attribute("id", METHOD_SELECT_BY_EXAMPLE_WITH_BLOBS_FOR_UPDATE));
        commentGenerator.addComment(xmlElement);
        this.selectByExampleWithBLOBsForUpdateEle = xmlElement;
        return super.sqlMapSelectByExampleWithBLOBsElementGenerated(element, introspectedTable);
    }


    @Override
    public boolean sqlMapSelectOneByExampleWithoutBLOBsElementGenerated(Document document, XmlElement element, IntrospectedTable introspectedTable) {
        XmlElement xmlElement = cloneAndAddForUpdateElement(element);
        xmlElement.addAttribute(new Attribute("id", METHOD_SELECT_ONE_BY_EXAMPLE_FOR_UPDATE));
        commentGenerator.addComment(xmlElement);
        this.selectOneByExampleForUpdateEle = xmlElement;
        return true;
    }


    @Override
    public boolean sqlMapSelectOneByExampleWithBLOBsElementGenerated(Document document, XmlElement element, IntrospectedTable introspectedTable) {
        XmlElement xmlElement = cloneAndAddForUpdateElement(element);
        xmlElement.addAttribute(new Attribute("id", METHOD_SELECT_ONE_BY_EXAMPLE_WITH_BLOBS_FOR_UPDATE));
        commentGenerator.addComment(xmlElement);
        this.selectOneByExampleWithBLOBsForUpdateEle = xmlElement;
        return true;
    }

    private XmlElement cloneAndAddForUpdateElement(XmlElement element) {
        XmlElement clone = XmlElementTools.clone(element);
        clone.getAttributes().removeIf(a -> "id".equals(a.getName()));
        clone.addElement(new TextElement("for update"));
        return clone;
    }


    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {

        if (selectByPrimaryKeyForUpdateEle != null) {
            // 添加到根节点
            FormatTools.addElementWithBestPosition(document.getRootElement(), selectByPrimaryKeyForUpdateEle);
            logger.debug("itfsw(forUpdate插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加selectByPrimaryKeyForUpdate方法。");
        }
        if (selectByExampleForUpdateEle != null) {
            // 添加到根节点
            FormatTools.addElementWithBestPosition(document.getRootElement(), selectByExampleForUpdateEle);
            logger.debug("itfsw(forUpdate插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加selectByExampleForUpdate方法。");
        }

        if (selectByExampleWithBLOBsForUpdateEle != null) {
            // 添加到根节点
            FormatTools.addElementWithBestPosition(document.getRootElement(), selectByExampleWithBLOBsForUpdateEle);
            logger.debug("itfsw(forUpdate插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加selectOneByExampleWithBLOBsForUpdate方法。");
        }


        if (selectOneByExampleForUpdateEle != null) {
            // 添加到根节点
            FormatTools.addElementWithBestPosition(document.getRootElement(), selectOneByExampleForUpdateEle);
            logger.debug("itfsw(forUpdate插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加selectOneByExample方法。");
        }

        if (selectOneByExampleWithBLOBsForUpdateEle != null) {
            // 添加到根节点
            FormatTools.addElementWithBestPosition(document.getRootElement(), selectOneByExampleWithBLOBsForUpdateEle);
            logger.debug("itfsw(forUpdate插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加selectOneByExampleWithBLOBsForUpdate方法。");
        }

        return true;
    }


}
