/*
 * EXI Testing Task Force Measurement Suite: http://www.w3.org/XML/EXI/
 *
 * Copyright © [2006] World Wide Web Consortium, (Massachusetts Institute of
 * Technology, European Research Consortium for Informatics and Mathematics,
 * Keio University). All Rights Reserved. This work is distributed under the
 * W3C® Software License [1] in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
 */

package org.w3c.exi.ttf.sax;

import org.xml.sax.SAXException;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A {@link DefaultHandler} that also implements {@link LexicalHandler} and
 * {@link DeclHandler}.
 *
 * @author AgileDelta
 * @author Sun
 * 
 */
public class EmptyHandler extends DefaultHandler
        implements LexicalHandler, DeclHandler {
    
    public EmptyHandler() {
    }
    
    public void startDTD(String name, String publicId, String systemId) 
    throws SAXException {
    }
    
    public void endDTD() throws SAXException {
    }
    
    public void startEntity(String name) throws SAXException {
    }
    
    public void endEntity(String name) throws SAXException {
    }
    
    public void startCDATA() throws SAXException {
    }
    
    public void endCDATA() throws SAXException {
    }
    
    public void comment(char[] ch, int start, int length) throws SAXException {
    }
    
    public void elementDecl(String name, String model) throws SAXException {
    }
    
    public void attributeDecl(String eName, String aName, String type, 
            String mode, String value) throws SAXException {
    }
    
    public void internalEntityDecl(String name, String value)
    throws SAXException {
    }
    
    public void externalEntityDecl(String name, String publicId, 
            String systemId) throws SAXException {
    }
}