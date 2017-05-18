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

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.exi.ttf.Event;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Record an aproximation of the Infoset derived from 
 * SAX calls to ContentHandler, DTDHandler, LexicalHandler, and DeclHandler.
 * 
 * @author AgileDelta
 * @author Sun
 * 
 */
public class SAXRecorder extends DefaultHandler implements LexicalHandler, DeclHandler
{
    /**
     * Collects Event objects recorded.
     */
    protected ArrayList<Event> _events;
    
    /**
     * indicate whether interning is enabled
     */
    protected boolean _intern;

    // intern-ing strings.
    private HashMap<String, char[]> _charArrayTable = new HashMap<String, char[]>();

    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    
    /**
     * Construct a new recorder, ready to record to the provided ArrayList
     * and interning values if internStrings is true
     */
    public SAXRecorder(ArrayList<Event> events, boolean internStrings)
    {
        _events = events;
        _intern = internStrings;
    }

    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    // SAX Handler methods

    ////////////////////////////////////////////////////////////////////
    // ContentHandler

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        _events.add(Event.newCharacters(toCharArray(ch, start, length)));
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        _events.add(Event.newEndElement(intern(uri), intern(localName), intern(qName)));
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException
    {
        _events.add(Event.newProcessingInstruction(intern(target), intern(data)));
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        _events.add(Event.newStartElement(intern(uri), intern(localName), intern(qName)));

        // Write the attributes
        for(int i = 0; i < attributes.getLength(); i++)
        {
            _events.add(Event.newAttribute(intern(attributes.getURI(i)), intern(attributes.getLocalName(i)), 
                                           intern(attributes.getQName(i)), intern(attributes.getValue(i))));
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException 
    {
        _events.add(Event.newNamespace(intern(prefix), intern(uri)));
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException 
    {
        _events.add(Event.newEndNamespace(intern(prefix)));
    }

    @Override
    public void skippedEntity(String name) throws SAXException 
    {
        _events.add(Event.newUnexpandedEntity(intern(name)));
    }

    ////////////////////////////////////////////////////////////////////
    // DTDHandler

    @Override
    public void notationDecl(String name, String publicId, String systemId)
    {
        _events.add(Event.newNotation(intern(name), intern(publicId), intern(systemId)));
    }

    @Override
    public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName)
    {
        _events.add(Event.newUnparsedEntity(intern(name), intern(publicId), intern(systemId), intern(notationName)));
    }

    ////////////////////////////////////////////////////////////////////
    // LexicalHandler

    public void startDTD(String name, String publicId, String systemId) throws SAXException
    {
        _events.add(Event.newDoctype(intern(name), intern(publicId), intern(systemId)));
    }

    public void endDTD() throws SAXException
    {
        _events.add(Event.newEndDTD());
    }

    public void startEntity(String name) throws SAXException
    {
    }

    public void endEntity(String name) throws SAXException
    {
    }

    public void startCDATA() throws SAXException
    {
    }

    public void endCDATA() throws SAXException
    {
    }

    public void comment(char[] ch, int start, int length) throws SAXException
    {
        _events.add(Event.newComment(toCharArray(ch, start, length)));
    }

    ////////////////////////////////////////////////////////////////////
    // DeclHandler
    
    public void elementDecl(String name, String model) throws SAXException
    {
    }

    public void attributeDecl(String eName, String aName, String type, String mode, String value) throws SAXException
    {
    }

    public void internalEntityDecl(String name, String value) throws SAXException
    {
    }

    // these are needed in case there was a skipped entity
    public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException
    {
        _events.add(Event.newExternalEntity(intern(name), intern(publicId), intern(systemId)));
    }

    ////////////////////////////////////////////////////////////////////
    // internal implementation

    /**
     * intern string if _intern==true
     */
    protected String intern(String s)
    {
        if (_intern && s != null)
            return s.intern();
        return s;
    }

    /**
     * If interning is enabled, intern char data, 
     * otherwise just allocate a new char[]
     * 
     * @param data
     *      source char data
     * @param start
     *      start offset within data
     * @param length
     *      count of characters
     * @return
     *      char[] of the subsequence of data specified.  
     *      Note that returned array should be considered 
     *      read-only as it may be shared.
     */
    protected char[] toCharArray(char[] data, int start, int length)
    {
        char[] result;
        if (!_intern)
        {
            result = new char[length];
            System.arraycopy(data, start, result, 0, length);
        }
        else
        {
            String value = new String(data, start, length);
            result = (char[])_charArrayTable.get(value);
            if (result == null)
            {
                result = value.toCharArray();
                _charArrayTable.put(value, result);
            }
        }
        return result;
    }

}
