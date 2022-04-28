package com.penglecode.codeforce.mybatistiny.examples.util;

import org.springframework.util.CollectionUtils;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pengpeng
 * @version 1.0
 */
public class ParseMapperXmlTest {

    public static void main(String[] args) throws Exception {
        test1();
        //test2();
    }

    public static void test1() throws Exception {
        InputStream in = ParseMapperXmlTest.class.getResourceAsStream("ExampleMapper.xml");
        Document document = readXmlDocument(in);

        NodeList mapperNodeList = document.getElementsByTagName("mapper");
        if(mapperNodeList != null && mapperNodeList.getLength() > 0) {
            Node mapperNode = mapperNodeList.item(0);
            NodeList tagNodes = mapperNode.getChildNodes();

            List<Node> removeNodes = new ArrayList<>();
            for(int i = 0, len = tagNodes.getLength(); i < len; i++) {
                Node tagNode = tagNodes.item(i);
                NamedNodeMap nodeAttrs = tagNode.getAttributes();
                if(nodeAttrs != null && nodeAttrs.getLength() > 0) {
                    Node tagIdNode = nodeAttrs.getNamedItem("id");
                    System.out.println(tagNode.getNodeName() + " >>> " + tagIdNode.getNodeValue());
                    if("update".equals(tagNode.getNodeName()) && "updateById".equals(tagIdNode.getNodeValue())) {
                        removeNodes.add(tagNode);
                    }
                    if("delete".equals(tagNode.getNodeName()) && "deleteById".equals(tagIdNode.getNodeValue())) {
                        removeNodes.add(tagNode);
                    }
                }
            }

            for(Node removeNode : removeNodes) {
                removeNode.getParentNode().removeChild(removeNode);
            }

        }

        writeXmlDocument(document, new File("d:/ExampleMapper1.xml"));
    }

    public static void test2() throws Exception {
        InputStream in = ParseMapperXmlTest.class.getResourceAsStream("ExampleMapper.xml");
        Document document = readXmlDocument(in);

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        XPathExpression expression = xpath.compile("/mapper/update[@id='updateById']");
        Node updateByIdNode = (Node) expression.evaluate(document, XPathConstants.NODE);
        updateByIdNode.getParentNode().removeChild(updateByIdNode);

        writeXmlDocument(document, new File("d:/ExampleMapper1.xml"));
    }

    public static Document readXmlDocument(InputStream in) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setIgnoringComments(false);
        factory.setIgnoringElementContentWhitespace(false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document document = documentBuilder.parse(in);
        document.setXmlStandalone(true);
        return document;
    }

    public static void writeXmlDocument(Document document, File file) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DocumentType doctype = document.getDoctype();
        if(doctype != null) {
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
        }
        transformer.transform(new DOMSource(document), new StreamResult(file));
    }

}
