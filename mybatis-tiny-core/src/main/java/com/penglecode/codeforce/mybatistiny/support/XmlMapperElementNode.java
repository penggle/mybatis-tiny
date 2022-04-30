package com.penglecode.codeforce.mybatistiny.support;

import org.w3c.dom.Node;

/**
 * BaseEntityMapper的XML-Mapper中的顶级标签元素节点
 *
 * @author pengpeng
 * @version 1.0
 */
public class XmlMapperElementNode {

    private final XmlMapperElementKey key;

    private final Node node;

    public XmlMapperElementNode(XmlMapperElementKey key, Node node) {
        this.key = key;
        this.node = node;
    }

    public XmlMapperElementKey getKey() {
        return key;
    }

    public Node getNode() {
        return node;
    }

}
