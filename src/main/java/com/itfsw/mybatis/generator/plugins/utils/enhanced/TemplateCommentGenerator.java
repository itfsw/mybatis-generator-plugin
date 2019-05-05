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

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.CommentGeneratorConfiguration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.MergeConstants;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.StringWriter;
import java.util.*;

import static org.mybatis.generator.internal.util.StringUtility.isTrue;

/**
 * ---------------------------------------------------------------------------
 * 模板注释生成工具
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/6/8 13:21
 * ---------------------------------------------------------------------------
 */
public class TemplateCommentGenerator implements CommentGenerator {
    protected static final Logger logger = LoggerFactory.getLogger(TemplateCommentGenerator.class);

    private Map<EnumNode, Template> templates = new HashMap<>();

    private boolean suppressDate = false;

    private boolean suppressAllComments = false;

    /**
     * 构造函数
     * @param context
     * @param templatePath  模板路径
     */
    public TemplateCommentGenerator(Context context, String templatePath) {
        try {
            Document doc = null;
            File file = new File(templatePath);
            if (file.exists()) {
                doc = new SAXReader().read(file);
            } else {
                logger.error("没有找到对应注释模板:" + templatePath);
            }

            // 遍历comment 节点
            if (doc != null) {
                for (EnumNode node : EnumNode.values()) {
                    Element element = doc.getRootElement().elementByID(node.value());
                    if (element != null) {
                        Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
                        // 字符串清理
                        Template template = new Template(node.value(), element.getText(), cfg);
                        templates.put(node, template);
                    }
                }
            }

            // 解析mybatis generator 注释配置
            CommentGeneratorConfiguration config = context.getCommentGeneratorConfiguration();
            if (config != null) {
                this.addConfigurationProperties(config.getProperties());
            }
        } catch (Exception e) {
            logger.error("注释模板XML解析失败！", e);
        }
    }


    /**
     * 获取评论
     * @param map  模板参数
     * @param node 节点ID
     * @return
     */
    private String[] getComments(Map<String, Object> map, EnumNode node) {
        // 1. 模板引擎解析
        try {
            StringWriter stringWriter = new StringWriter();
            Template template = templates.get(node);
            if (template != null) {
                template.process(map, stringWriter);

                String comment = stringWriter.toString();
                stringWriter.close();

                // 需要先清理字符串
                return comment.replaceFirst("^[\\s\\t\\r\\n]*", "").replaceFirst("[\\s\\t\\r\\n]*$", "").split("\n");
            }

        } catch (Exception e) {
            logger.error("freemarker 解析失败！", e);
        }

        return null;
    }

    /**
     * 添加评论
     * @param javaElement
     * @param map
     * @param node
     */
    private void addJavaElementComment(JavaElement javaElement, Map<String, Object> map, EnumNode node) {
        if (this.suppressAllComments) {
            return;
        }
        // 获取评论
        String[] comments = getComments(map, node);
        if (comments != null) {
            // 去除空评论
            if (comments.length == 1 && !StringUtility.stringHasValue(comments[0])) {
                return;
            }
            // 添加评论
            for (String comment : comments) {
                javaElement.addJavaDocLine(comment);
            }
        }
    }

    /**
     * 添加评论
     * @param compilationUnit
     * @param map
     * @param node
     */
    private void addCompilationUnitComment(CompilationUnit compilationUnit, Map<String, Object> map, EnumNode node) {
        if (this.suppressAllComments) {
            return;
        }
        // 获取评论
        String[] comments = getComments(map, node);
        if (comments != null) {
            // 去除空评论
            if (comments.length == 1 && !StringUtility.stringHasValue(comments[0])) {
                return;
            }
            // 添加评论
            for (String comment : comments) {
                compilationUnit.addFileCommentLine(comment);
            }
        }
    }

    /**
     * 添加评论
     * @param xmlElement
     * @param map
     * @param node
     */
    private void addXmlElementComment(XmlElement xmlElement, Map<String, Object> map, EnumNode node) {
        if (this.suppressAllComments) {
            return;
        }
        // 获取评论
        String[] comments = getComments(map, node);
        if (comments != null) {
            // 去除空评论
            if (comments.length == 1 && !StringUtility.stringHasValue(comments[0])) {
                return;
            }
            // 添加评论
            for (String comment : comments) {
                xmlElement.addElement(new TextElement(comment));
            }
        }
    }

    /**
     * Adds properties for this instance from any properties configured in the
     * CommentGenerator configuration.
     *
     * This method will be called before any of the other methods.
     * @param properties All properties from the configuration
     */
    @Override
    public void addConfigurationProperties(Properties properties) {
        suppressDate = isTrue(properties
                .getProperty(PropertyRegistry.COMMENT_GENERATOR_SUPPRESS_DATE));

        suppressAllComments = isTrue(properties
                .getProperty(PropertyRegistry.COMMENT_GENERATOR_SUPPRESS_ALL_COMMENTS));
    }

    /**
     * This method should add a Javadoc comment to the specified field. The field is related to the specified table and
     * is used to hold the value of the specified column.
     * <p>
     *
     * <b>Important:</b> This method should add a the nonstandard JavaDoc tag "@mbg.generated" to the comment. Without
     * this tag, the Eclipse based Java merge feature will fail.
     * @param field              the field
     * @param introspectedTable  the introspected table
     * @param introspectedColumn the introspected column
     */
    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        Map<String, Object> map = new HashMap<>();
        map.put("mgb", MergeConstants.NEW_ELEMENT_TAG);
        map.put("field", field);
        map.put("introspectedTable", introspectedTable);
        map.put("introspectedColumn", introspectedColumn);

        // 添加评论
        addJavaElementComment(field, map, EnumNode.ADD_FIELD_COMMENT);
    }

    /**
     * Adds the field comment.
     * @param field             the field
     * @param introspectedTable the introspected table
     */
    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable) {
        Map<String, Object> map = new HashMap<>();
        map.put("mgb", MergeConstants.NEW_ELEMENT_TAG);
        map.put("field", field);
        map.put("introspectedTable", introspectedTable);

        // 添加评论
        addJavaElementComment(field, map, EnumNode.ADD_FIELD_COMMENT);
    }

    /**
     * Adds a comment for a model class.  The Java code merger should
     * be notified not to delete the entire class in case any manual
     * changes have been made.  So this method will always use the
     * "do not delete" annotation.
     *
     * Because of difficulties with the Java file merger, the default implementation
     * of this method should NOT add comments.  Comments should only be added if
     * specifically requested by the user (for example, by enabling table remark comments).
     * @param topLevelClass     the top level class
     * @param introspectedTable the introspected table
     */
    @Override
    public void addModelClassComment(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        Map<String, Object> map = new HashMap<>();
        map.put("mgb", MergeConstants.NEW_ELEMENT_TAG);
        map.put("topLevelClass", topLevelClass);
        map.put("introspectedTable", introspectedTable);

        // 添加评论
        addJavaElementComment(topLevelClass, map, EnumNode.ADD_MODEL_CLASS_COMMENT);
    }

    /**
     * Adds the inner class comment.
     * @param innerClass        the inner class
     * @param introspectedTable the introspected table
     */
    @Override
    public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable) {
        if (innerClass instanceof InnerInterfaceWrapperToInnerClass) {
            InnerInterface innerInterface = ((InnerInterfaceWrapperToInnerClass) innerClass).getInnerInterface();

            Map<String, Object> map = new HashMap<>();
            map.put("mgb", MergeConstants.NEW_ELEMENT_TAG);
            map.put("innerInterface", innerInterface);
            map.put("introspectedTable", introspectedTable);

            // 添加评论
            addJavaElementComment(innerInterface, map, EnumNode.ADD_INTERFACE_COMMENT);
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("mgb", MergeConstants.NEW_ELEMENT_TAG);
            map.put("innerClass", innerClass);
            map.put("introspectedTable", introspectedTable);

            // 添加评论
            addJavaElementComment(innerClass, map, EnumNode.ADD_CLASS_COMMENT);
        }
    }

    /**
     * Adds the inner class comment.
     * @param innerClass        the inner class
     * @param introspectedTable the introspected table
     * @param markAsDoNotDelete the mark as do not delete
     */
    @Override
    public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable, boolean markAsDoNotDelete) {
        Map<String, Object> map = new HashMap<>();
        map.put("mgb", MergeConstants.NEW_ELEMENT_TAG);
        map.put("innerClass", innerClass);
        map.put("introspectedTable", introspectedTable);
        map.put("markAsDoNotDelete", markAsDoNotDelete);

        // 添加评论
        addJavaElementComment(innerClass, map, EnumNode.ADD_CLASS_COMMENT);
    }

    /**
     * Adds the enum comment.
     * @param innerEnum         the inner enum
     * @param introspectedTable the introspected table
     */
    @Override
    public void addEnumComment(InnerEnum innerEnum, IntrospectedTable introspectedTable) {
        Map<String, Object> map = new HashMap<>();
        map.put("mgb", MergeConstants.NEW_ELEMENT_TAG);
        map.put("innerEnum", innerEnum);
        map.put("introspectedTable", introspectedTable);

        // 添加评论
        addJavaElementComment(innerEnum, map, EnumNode.ADD_ENUM_COMMENT);
    }

    /**
     * Adds the getter comment.
     * @param method             the method
     * @param introspectedTable  the introspected table
     * @param introspectedColumn the introspected column
     */
    @Override
    public void addGetterComment(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        Map<String, Object> map = new HashMap<>();
        map.put("mgb", MergeConstants.NEW_ELEMENT_TAG);
        map.put("method", method);
        map.put("introspectedTable", introspectedTable);
        map.put("introspectedColumn", introspectedColumn);

        // 添加评论
        addJavaElementComment(method, map, EnumNode.ADD_GETTER_COMMENT);
    }

    /**
     * Adds the setter comment.
     * @param method             the method
     * @param introspectedTable  the introspected table
     * @param introspectedColumn the introspected column
     */
    @Override
    public void addSetterComment(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        Map<String, Object> map = new HashMap<>();
        map.put("mgb", MergeConstants.NEW_ELEMENT_TAG);
        map.put("method", method);
        map.put("introspectedTable", introspectedTable);
        map.put("introspectedColumn", introspectedColumn);

        // 添加评论
        addJavaElementComment(method, map, EnumNode.ADD_SETTER_COMMENT);
    }

    /**
     * Adds the general method comment.
     * @param method            the method
     * @param introspectedTable the introspected table
     */
    @Override
    public void addGeneralMethodComment(Method method, IntrospectedTable introspectedTable) {
        Map<String, Object> map = new HashMap<>();
        map.put("mgb", MergeConstants.NEW_ELEMENT_TAG);
        map.put("method", method);
        map.put("introspectedTable", introspectedTable);

        // 添加评论
        addJavaElementComment(method, map, EnumNode.ADD_GENERAL_METHOD_COMMENT);
    }

    /**
     * This method is called to add a file level comment to a generated java file. This method could be used to add a
     * general file comment (such as a copyright notice). However, note that the Java file merge function in Eclipse
     * does not deal with this comment. If you run the generator repeatedly, you will only retain the comment from the
     * initial run.
     * <p>
     *
     * The default implementation does nothing.
     * @param compilationUnit the compilation unit
     */
    @Override
    public void addJavaFileComment(CompilationUnit compilationUnit) {
        Map<String, Object> map = new HashMap<>();
        map.put("mgb", MergeConstants.NEW_ELEMENT_TAG);
        map.put("compilationUnit", compilationUnit);

        // 添加评论
        addCompilationUnitComment(compilationUnit, map, EnumNode.ADD_JAVA_FILE_COMMENT);
    }

    /**
     * This method should add a suitable comment as a child element of the specified xmlElement to warn users that the
     * element was generated and is subject to regeneration.
     * @param xmlElement the xml element
     */
    @Override
    public void addComment(XmlElement xmlElement) {
        Map<String, Object> map = new HashMap<>();
        map.put("mgb", MergeConstants.NEW_ELEMENT_TAG);
        map.put("xmlElement", xmlElement);

        // 添加评论
        addXmlElementComment(xmlElement, map, EnumNode.ADD_COMMENT);
    }

    /**
     * This method is called to add a comment as the first child of the root element. This method could be used to add a
     * general file comment (such as a copyright notice). However, note that the XML file merge function does not deal
     * with this comment. If you run the generator repeatedly, you will only retain the comment from the initial run.
     * <p>
     *
     * The default implementation does nothing.
     * @param rootElement the root element
     */
    @Override
    public void addRootComment(XmlElement rootElement) {
        Map<String, Object> map = new HashMap<>();
        map.put("mgb", MergeConstants.NEW_ELEMENT_TAG);
        map.put("rootElement", rootElement);

        // 添加评论
        addXmlElementComment(rootElement, map, EnumNode.ADD_ROOT_COMMENT);
    }

    @Override
    public void addGeneralMethodAnnotation(Method method, IntrospectedTable introspectedTable,
                                           Set<FullyQualifiedJavaType> imports) {
        imports.add(new FullyQualifiedJavaType("javax.annotation.Generated"));
        String comment = "Source Table: " + introspectedTable.getFullyQualifiedTable().toString();
        method.addAnnotation(getGeneratedAnnotation(comment));
    }

    @Override
    public void addGeneralMethodAnnotation(Method method, IntrospectedTable introspectedTable,
                                           IntrospectedColumn introspectedColumn, Set<FullyQualifiedJavaType> imports) {
        imports.add(new FullyQualifiedJavaType("javax.annotation.Generated"));
        String comment = "Source field: "
                + introspectedTable.getFullyQualifiedTable().toString()
                + "."
                + introspectedColumn.getActualColumnName();
        method.addAnnotation(getGeneratedAnnotation(comment));
    }

    @Override
    public void addFieldAnnotation(Field field, IntrospectedTable introspectedTable,
                                   Set<FullyQualifiedJavaType> imports) {
        imports.add(new FullyQualifiedJavaType("javax.annotation.Generated"));
        String comment = "Source Table: " + introspectedTable.getFullyQualifiedTable().toString();
        field.addAnnotation(getGeneratedAnnotation(comment));
    }

    @Override
    public void addFieldAnnotation(Field field, IntrospectedTable introspectedTable,
                                   IntrospectedColumn introspectedColumn, Set<FullyQualifiedJavaType> imports) {
        imports.add(new FullyQualifiedJavaType("javax.annotation.Generated"));
        String comment = "Source field: "
                + introspectedTable.getFullyQualifiedTable().toString()
                + "."
                + introspectedColumn.getActualColumnName();
        field.addAnnotation(getGeneratedAnnotation(comment));
    }

    @Override
    public void addClassAnnotation(InnerClass innerClass, IntrospectedTable introspectedTable,
                                   Set<FullyQualifiedJavaType> imports) {
        imports.add(new FullyQualifiedJavaType("javax.annotation.Generated"));
        String comment = "Source Table: " + introspectedTable.getFullyQualifiedTable().toString();
        innerClass.addAnnotation(getGeneratedAnnotation(comment));
    }

    private String getGeneratedAnnotation(String comment) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("@Generated(");
        if (suppressAllComments) {
            buffer.append('\"');
        } else {
            buffer.append("value=\"");
        }

        buffer.append(MyBatisGenerator.class.getName());
        buffer.append('\"');

        if (!suppressDate && !suppressAllComments) {
            buffer.append(", date=\"");
            buffer.append(DatatypeConverter.printDateTime(Calendar.getInstance()));
            buffer.append('\"');
        }

        if (!suppressAllComments) {
            buffer.append(", comments=\"");
            buffer.append(comment);
            buffer.append('\"');
        }

        buffer.append(')');
        return buffer.toString();
    }

    /**
     * 评论模板节点ID
     */
    public static enum EnumNode {
        ADD_COMMENT("addComment"),  // Xml 节点注释
        ADD_ROOT_COMMENT("addRootComment"),   // xml root 节点注释
        ADD_JAVA_FILE_COMMENT("addJavaFileComment"),   // java 文件注释
        ADD_GENERAL_METHOD_COMMENT("addGeneralMethodComment"), // java 方法注释
        ADD_SETTER_COMMENT("addSetterComment"), // setter 方法注释
        ADD_GETTER_COMMENT("addGetterComment"), // getter 方式注释
        ADD_ENUM_COMMENT("addEnumComment"), // 枚举 注释
        ADD_CLASS_COMMENT("addClassComment"),   // 类 注释
        ADD_INTERFACE_COMMENT("addInterfaceComment"),   // 接口 注释
        ADD_MODEL_CLASS_COMMENT("addModelClassComment"),    // model 类注释
        ADD_FIELD_COMMENT("addFieldComment"),   // 字段 注释
        ;

        private final String value; // 值

        /**
         * 构造方法
         * @param value
         */
        EnumNode(String value) {
            this.value = value;
        }

        /**
         * 值
         * @return
         */
        public String value() {
            return value;
        }
    }
}
