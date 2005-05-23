/*
 * $Id: DOMStreamReader.java,v 1.1 2005-05-23 22:59:34 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.streaming;

import java.util.List;
import java.util.ArrayList;

import org.w3c.dom.*;
import static org.w3c.dom.Node.*;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import static javax.xml.stream.XMLStreamConstants.*;

/**
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
public class DOMStreamReader implements XMLStreamReader {
    
    /**
     * Current DOM node being traversed.
     */
    Node _current;
    
    /**
     * Named mapping for attributes and NS decls for the current node.
     */
    NamedNodeMap _namedNodeMap;
    
    /**
     * List of attributes extracted from <code>_namedNodeMap</code>.
     */
    List<Attr> _currentAttributes = new ArrayList<Attr>();
    
    /**
     * List of namespace declarations extracted from <code>_namedNodeMap</code>
     */
    List<Attr> _currentNamespaces = new ArrayList<Attr>();
    
    /**
     * Flag indicating if <code>_namedNodeMap</code> is already split into
     * <code>_currentAttributes</code> and <code>_currentNamespaces</code>
     */
    boolean _needAttributesSplit;
    
    /**
     * State of this reader. Any of the valid states defined in StAX'
     * XMLStreamConstants class.
     */
    int _state;
    
    public DOMStreamReader() {
    }
    
    public DOMStreamReader(Node node) {
        setCurrentNode(node);
    }
    
    public void setCurrentNode(Node node) {
        _current = node;
        _state = START_DOCUMENT;
    }
    
    public void close() throws javax.xml.stream.XMLStreamException {
    }
    
    private void splitAttributes() {
        if (!_needAttributesSplit) return;
        
        // Clear attribute and namespace lists
        _currentAttributes.clear();
        _currentNamespaces.clear();
        
        _namedNodeMap = _current.getAttributes();
        if (_namedNodeMap != null) {
            final int n = _namedNodeMap.getLength();
            for (int i = 0; i < n; i++) {
                final Attr attr = (Attr) _namedNodeMap.item(i);
                final String attrName = attr.getNodeName();
                if (attrName.startsWith("xmlns:") || attrName.equals("xmlns")) {     // NS decl?
                    _currentNamespaces.add(attr);
                }
                else {
                    _currentAttributes.add(attr);
                }            
            }
        }
        _needAttributesSplit = false;
    }
    
    public int getAttributeCount() {
        if (_state == START_ELEMENT) {
            splitAttributes();
            return _currentAttributes.size();
        }
        throw new IllegalStateException("DOMStreamReader: getAttributeCount() called in illegal state");
    }
    
    public String getAttributeLocalName(int index) {
        if (_state == START_ELEMENT) {
            splitAttributes();
            return _currentAttributes.get(index).getLocalName();
        }
        throw new IllegalStateException("DOMStreamReader: getAttributeLocalName() called in illegal state");
    }
    
    public QName getAttributeName(int index) {
        if (_state == START_ELEMENT) {
            splitAttributes();
            Node attr = _currentAttributes.get(index);
            return new QName(attr.getNamespaceURI(), attr.getPrefix(),
                    attr.getLocalName());
        }
        throw new IllegalStateException("DOMStreamReader: getAttributeName() called in illegal state");
    }
    
    public String getAttributeNamespace(int index) {
        if (_state == START_ELEMENT) {
            splitAttributes();
            return _currentAttributes.get(index).getNamespaceURI();
        }
        throw new IllegalStateException("DOMStreamReader: getAttributeNamespace() called in illegal state");
    }
    
    public String getAttributePrefix(int index) {
        if (_state == START_ELEMENT) {
            splitAttributes();
            return _currentAttributes.get(index).getPrefix();
        }
        throw new IllegalStateException("DOMStreamReader: getAttributePrefix() called in illegal state");
    }
    
    public String getAttributeType(int index) {
        if (_state == START_ELEMENT) {
            return "CDATA";
        }
        throw new IllegalStateException("DOMStreamReader: getAttributeType() called in illegal state");
    }
    
    public String getAttributeValue(int index) {
        if (_state == START_ELEMENT) {
            splitAttributes();
            return _currentAttributes.get(index).getNodeValue();
        }
        throw new IllegalStateException("DOMStreamReader: getAttributeValue() called in illegal state");
    }
    
    public String getAttributeValue(String namespaceURI, String localName) {
        if (_state == START_ELEMENT) {
            splitAttributes();
            return _namedNodeMap.getNamedItemNS(namespaceURI, localName).getNodeValue();
        }
        throw new IllegalStateException("DOMStreamReader: getAttributeValue() called in illegal state");
    }
    
    public String getCharacterEncodingScheme() {
        return null;
    }
    
    public String getElementText() throws javax.xml.stream.XMLStreamException {
        throw new RuntimeException("DOMStreamReader: getElementText() not implemented");
    }
    
    public String getEncoding() {
        return null;
    }
    
    public int getEventType() {
        return _state;
    }
    
    public String getLocalName() {
        if (_state == START_ELEMENT || _state == END_ELEMENT) {
            return _current.getLocalName();
        } else if (_state == ENTITY_REFERENCE) {
            return _current.getNodeName();
        }
        throw new IllegalStateException("DOMStreamReader: getAttributeValue() called in illegal state");
    }
    
    public javax.xml.stream.Location getLocation() {
        throw new RuntimeException("DOMStreamReader: getLocation() not implemented");
    }
    
    public javax.xml.namespace.QName getName() {
        if (_state == START_ELEMENT || _state == END_ELEMENT) {
            String uri = _current.getNamespaceURI();
            String localName = _current.getLocalName();
            String prefix = _current.getPrefix();
            return new QName(uri != null ? uri : "",
                             localName != null ? localName : "",
                             prefix != null ? prefix : "");
        }
        throw new IllegalStateException("DOMStreamReader: getName() called in illegal state");
    }
    
    public javax.xml.namespace.NamespaceContext getNamespaceContext() {
        throw new RuntimeException("DOMStreamReader: getNamespaceContext() not implemented");
    }
    
    public int getNamespaceCount() {
        if (_state == START_ELEMENT || _state == END_ELEMENT) {
            splitAttributes();
            return _currentNamespaces.size();
        }
        throw new IllegalStateException("DOMStreamReader: getNamespaceCount() called in illegal state");
    }
    
    public String getNamespacePrefix(int index) {
        if (_state == START_ELEMENT || _state == END_ELEMENT) {
            splitAttributes();
            return _currentNamespaces.get(index).getLocalName();        // Is this correct?
        }
        throw new IllegalStateException("DOMStreamReader: getNamespacePrefix() called in illegal state");
    }
    
    public String getNamespaceURI() {
        if (_state == START_ELEMENT || _state == END_ELEMENT) {
            return _current.getNamespaceURI();
        }
        return null;
    }
    
    public String getNamespaceURI(int index) {
        if (_state == START_ELEMENT || _state == END_ELEMENT) {
            splitAttributes();
            return _currentNamespaces.get(index).getValue();        // Is this correct?
        }
        throw new IllegalStateException("DOMStreamReader: getNamespaceURI(int) called in illegal state");
    }
    
    public String getNamespaceURI(String str) {
        throw new RuntimeException("DOMStreamReader: getNamespaceURI(String) not implemented");
    }
    
    public String getPIData() {
        if (_state == PROCESSING_INSTRUCTION) {
            return ((ProcessingInstruction) _current).getData();
        }
        return null;
    }
    
    public String getPITarget() {
        if (_state == PROCESSING_INSTRUCTION) {
            return ((ProcessingInstruction) _current).getTarget();
        }
        return null;
    }
    
    public String getPrefix() {
        if (_state == START_ELEMENT || _state == END_ELEMENT) {
            return _current.getPrefix();
        }
        return null;
    }
    
    public Object getProperty(String str) throws IllegalArgumentException {
        return null;
    }
    
    public String getText() {
        if (_state == CHARACTERS || _state == CDATA || _state == COMMENT ||
                _state == ENTITY_REFERENCE) {
            return _current.getNodeValue();
        }
        throw new IllegalStateException("DOMStreamReader: getTextLength() called in illegal state");
    }
    
    public char[] getTextCharacters() {
        return getText().toCharArray();
    }
    
    public int getTextCharacters(int sourceStart, char[] target, int targetStart,
            int targetLength) throws javax.xml.stream.XMLStreamException 
    {
        char[] text = getTextCharacters();
        System.arraycopy(text, sourceStart, target, targetStart, targetLength);
        return Math.min(targetLength, text.length - sourceStart);
    }
    
    public int getTextLength() {
        if (_state == CHARACTERS || _state == CDATA || _state == COMMENT ||
                _state == ENTITY_REFERENCE) {
            return _current.getNodeValue().length();
        }
        throw new IllegalStateException("DOMStreamReader: getTextLength() called in illegal state");
    }
    
    public int getTextStart() {
        if (_state == CHARACTERS || _state == CDATA || _state == COMMENT ||
                _state == ENTITY_REFERENCE) {
            return 0;
        }
        throw new IllegalStateException("DOMStreamReader: getTextStart() called in illegal state");
    }
    
    public String getVersion() {
        return null;
    }
    
    public boolean hasName() {
        return (_state == START_ELEMENT || _state == END_ELEMENT);
    }
    
    public boolean hasNext() throws javax.xml.stream.XMLStreamException {
        return (_state != END_DOCUMENT);
    }
    
    public boolean hasText() {
        if (_state == CHARACTERS || _state == CDATA || _state == COMMENT ||
                _state == ENTITY_REFERENCE) {
            return (_current.getNodeValue().trim().length() > 0);
        }
        return false;
    }
    
    public boolean isAttributeSpecified(int param) {
        return false;
    }
    
    public boolean isCharacters() {
        return (_state == CHARACTERS);
    }
    
    public boolean isEndElement() {
        return (_state == END_ELEMENT);
    }
    
    public boolean isStandalone() {
        return true;
    }
    
    public boolean isStartElement() {
        return (_state == START_ELEMENT);
    }
    
    public boolean isWhiteSpace() {
        final int nodeType = _current.getNodeType();
        if (nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE) {
            return (_current.getNodeValue().trim().length() == 0);
        }
        return false;
    }
    
    private static int mapNodeTypeToState(int nodetype) {
        switch (nodetype) {
            case CDATA_SECTION_NODE:
                return CDATA;
            case COMMENT_NODE:
                return COMMENT;
            case ELEMENT_NODE:
                return START_ELEMENT;
            case ENTITY_NODE:
                return ENTITY_DECLARATION;
            case ENTITY_REFERENCE_NODE:
                return ENTITY_REFERENCE;
            case NOTATION_NODE:
                return NOTATION_DECLARATION;
            case PROCESSING_INSTRUCTION_NODE:
                return PROCESSING_INSTRUCTION;
            case TEXT_NODE:
                return CHARACTERS;
            default:
                throw new RuntimeException("DOMStreamReader: Unexpected node type");
        }
    }
    
    public int next() throws javax.xml.stream.XMLStreamException {
        Node child;
        
        // Indicate that attributes still need processing
        _needAttributesSplit = true;
        
        switch (_state) {
            case END_DOCUMENT:
                throw new IllegalStateException("DOMStreamReader: Calling next() at END_DOCUMENT");
            case START_DOCUMENT:
                child = _current.getFirstChild();
                if (child == null) {
                    return (_state = END_DOCUMENT);
                } 
                else {
                    _current = child;
                    return (_state = mapNodeTypeToState(_current.getNodeType()));
                }
            case START_ELEMENT:
                child = _current.getFirstChild();
                if (child == null) {
                    return (_state = END_ELEMENT);
                } 
                else {
                    _current = child;
                    return (_state = mapNodeTypeToState(_current.getNodeType()));
                }
            case CHARACTERS:
            case COMMENT:
            case CDATA:
            case ENTITY_REFERENCE:
            case PROCESSING_INSTRUCTION:
            case END_ELEMENT:
                Node sibling = _current.getNextSibling();
                if (sibling == null) {
                    _current = _current.getParentNode();
                    _state = (_current.getNodeType() == DOCUMENT_NODE) ? 
                             END_DOCUMENT : END_ELEMENT;
                    return _state;
                } 
                else {
                    _current = sibling;
                    return (_state = mapNodeTypeToState(_current.getNodeType()));
                }
            case DTD:
            case ATTRIBUTE:
            case NAMESPACE:
            default:
                throw new RuntimeException("DOMStreamReader: Unexpected internal state");
        }
    }
    
    public int nextTag() throws javax.xml.stream.XMLStreamException {
        int eventType = next();
        while (eventType == CHARACTERS && isWhiteSpace()
               || eventType == CDATA && isWhiteSpace()
               || eventType == SPACE
               || eventType == PROCESSING_INSTRUCTION
               || eventType == COMMENT) 
        {
            eventType = next();
        }
        if (eventType != START_ELEMENT && eventType != END_ELEMENT) {
            throw new XMLStreamException("DOMStreamReader: Expected start or end tag");
        }
        return eventType;
    }
    
    public void require(int type, String namespaceURI, String localName) 
        throws javax.xml.stream.XMLStreamException 
    {
        if (type != _state) {
            throw new XMLStreamException("DOMStreamReader: Required event type not found");
        }
        if (namespaceURI != null && !namespaceURI.equals(getNamespaceURI())) {
            throw new XMLStreamException("DOMStreamReader: Required namespaceURI not found");
        }
        if (localName != null && !localName.equals(getLocalName())) {
            throw new XMLStreamException("DOMStreamReader: Required localName not found");    
        }
    }
    
    public boolean standaloneSet() {
        return true;
    }    
    
    static public void main(String[] args) throws Exception {
        String sample = "<root attr1='a' xmlns:p='http://ppp' xmlns='http://bbb'><elem1><elem2>bla</elem2></elem1></root>";
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
        Document dd = db.parse(new java.io.ByteArrayInputStream(sample.getBytes("UTF-8")));
        
        DOMStreamReader dsr = new DOMStreamReader(dd);
        while (dsr.hasNext()) {
            System.out.println("dsr.next() = " + dsr.next());
            if (dsr.getEventType() == START_ELEMENT || dsr.getEventType() == END_ELEMENT) {
                System.out.println("dsr.getName = " + dsr.getName());
                if (dsr.getEventType() == START_ELEMENT)
                    System.out.println("dsr.getAttributeCount() = " + dsr.getAttributeCount());
            }
        }
    }
}
