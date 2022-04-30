package com.penglecode.codeforce.mybatistiny.support;


import com.penglecode.codeforce.mybatistiny.dsl.QueryColumns;
import com.penglecode.codeforce.mybatistiny.exception.MapperXmlParseException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Mybatis的XML-Mapper配置文件辅助类
 *
 * @author pengpeng
 * @version 1.0
 */
public class XmlMapperHelper {

    private XmlMapperHelper() {}

    /**
     * 判断对象是否为空(null，空串，空数组，空集合等)
     * @param paramObj
     * @return
     */
    public static boolean isEmpty(Object paramObj) {
        return ObjectUtils.isEmpty(paramObj);
    }

    /**
     * 判断对象是否不为空(null，空串，空数组，空集合等)
     * @param paramObj
     * @return
     */
    public static boolean isNotEmpty(Object paramObj) {
        return !ObjectUtils.isEmpty(paramObj);
    }

    /**
     * 判断参数是否是数组或集合
     * @param paramObj
     * @return
     */
    public static boolean isArrayOrCollection(Object paramObj) {
        if (paramObj == null) {
            return false;
        }
        return paramObj instanceof Collection || paramObj.getClass().isArray();
    }

    /**
     * columnNames中是否包含指定的列
     * @param columnNames
     * @param columnName
     * @return
     */
    public static boolean containsColumn(Map<String,Object> columnNames, String columnName) {
    	if(columnNames != null) {
    		return columnNames.containsKey(columnName);
    	}
    	return false;
    }

    /**
     * columnArray中是否包含指定的列
     * @param columnArray
     * @param columnName
     * @return
     */
    public static boolean containsColumn(QueryColumns[] columnArray, String columnName) {
        boolean selected = true;
        QueryColumns queryColumns = (columnArray != null && columnArray.length > 0) ? columnArray[0] : null;
        if(queryColumns != null) {
            Set<String> selectColumns = queryColumns.getColumns();
            if(!CollectionUtils.isEmpty(selectColumns)) {
                for(String selectColumn : selectColumns) {
                    if(selectColumn.equals(columnName)) {
                        return true;
                    }
                }
                selected = false;
            }
            if(queryColumns.getPredicate() != null) {
                return queryColumns.getPredicate().test(columnName);
            }
        }
        return selected;
    }

    /**
     * 读取XML-Mapper的内容作为Document
     *
     * @param xmlMapperContent
     * @return
     * @throws Exception
     */
    public static Document readAsDocument(String xmlMapperContent) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setIgnoringComments(false);
            factory.setIgnoringElementContentWhitespace(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            Document document = documentBuilder.parse(new ByteArrayInputStream(xmlMapperContent.getBytes(StandardCharsets.UTF_8)));
            document.setXmlStandalone(true);
            return document;
        } catch (Exception e) {
            throw new MapperXmlParseException(String.format("Read XML-Mapper as Document failed: %s", e.getMessage()), e);
        }
    }

    /**
     * 将指定的Document转成字符串
     *
     * @param document
     * @throws Exception
     */
    public static String writeAsString(Document document) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DocumentType doctype = document.getDoctype();
            if(doctype != null) {
                transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
            }
            StringWriter stringWriter = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
            return stringWriter.toString();
        } catch (Exception e) {
            throw new MapperXmlParseException(String.format("Write Document as XML-Mapper failed: %s", e.getMessage()), e);
        }
    }

    /**
     * 获取指定XML-Mapper文档中的所有顶级元素
     * @param document
     * @return
     */
    public static Map<XmlMapperElementKey,XmlMapperElementNode> getAllXmlMapperElements(Document document) {
        Map<XmlMapperElementKey,XmlMapperElementNode> mapperElements = new LinkedHashMap<>();
        NodeList mapperNodeList = document.getElementsByTagName("mapper");
        if(mapperNodeList != null && mapperNodeList.getLength() == 1) {
            NodeList mapperTopNodes = mapperNodeList.item(0).getChildNodes();
            for(int i = 0, len = mapperTopNodes.getLength(); i < len; i++) {
                Node mapperTopNode = mapperTopNodes.item(i);
                String elementName = mapperTopNode.getNodeName();
                if(XmlMapperElementType.contains(elementName)) { //仅处理XmlMapperElement中定义的元素
                    NamedNodeMap mapperTopNodeAttrs = mapperTopNode.getAttributes();
                    if(mapperTopNodeAttrs != null && mapperTopNodeAttrs.getLength() > 0) {
                        Node elementIdAttr = mapperTopNodeAttrs.getNamedItem("id");
                        Node elementDatabaseIdAttr = mapperTopNodeAttrs.getNamedItem("databaseId");
                        String elementId = elementIdAttr.getNodeValue();
                        String databaseId = elementDatabaseIdAttr != null ? elementDatabaseIdAttr.getNodeValue() : null;
                        XmlMapperElementKey key = new XmlMapperElementKey(elementId, elementName, databaseId);
                        mapperElements.put(key, new XmlMapperElementNode(key, mapperTopNode));
                    }
                }
            }
        }
        return mapperElements;
    }

    /**
     * 清空指定XML-Mapper文档中<mapper/>下的所有顶级元素
     *
     * @param document
     */
    public static void clearXmlMapperElements(Document document) {
        NodeList mapperNodeList = document.getElementsByTagName("mapper");
        if(mapperNodeList != null && mapperNodeList.getLength() > 0) {
            Node mapperNode = mapperNodeList.item(0);
            NodeList tagNodes = mapperNode.getChildNodes();
            List<Node> removeNodes = new ArrayList<>();
            for(int i = 0, len = tagNodes.getLength(); i < len; i++) {
                Node tagNode = tagNodes.item(i);
                removeNodes.add(tagNode);
            }
            for(Node removeNode : removeNodes) {
                removeNode.getParentNode().removeChild(removeNode);
            }
        }
    }

    /**
     * 在指定XML-Mapper文档中<mapper/>下添加指定子元素
     *
     * @param document
     * @param childElements
     */
    public static void appendXmlMapperElements(Document document, List<Node> childElements) {
        NodeList mapperNodeList = document.getElementsByTagName("mapper");
        if(mapperNodeList != null && mapperNodeList.getLength() > 0) {
            Node mapperNode = mapperNodeList.item(0);
            for(Node childElement : childElements) {
                Node nodeToAppend = document.importNode(childElement, true);
                mapperNode.appendChild(document.createTextNode("\n\t\n\t"));
                mapperNode.appendChild(nodeToAppend);
            }
            mapperNode.appendChild(document.createTextNode("\n\n"));
        }
    }

}
