package com.penglecode.codeforce.mybatistiny.examples.test;

import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * @author pengpeng
 * @version 1.0
 */
public class ParseMapperXmlTest {

    public static void main(String[] args) throws Exception {
        test1();
    }

    public static void test1() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setIgnoringComments(false);
        factory.setIgnoringElementContentWhitespace(true);
        //factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
        factory.setFeature(XMLConstants.ACCESS_EXTERNAL_SCHEMA, false);

        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document document = documentBuilder.parse(ParseMapperXmlTest.class.getResourceAsStream("ExampleMapper.xml"));

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(document), new StreamResult(System.out));
    }

}
