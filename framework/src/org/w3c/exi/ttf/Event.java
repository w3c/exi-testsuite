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

package org.w3c.exi.ttf;

import java.util.Calendar;

/**
 * Common class to record a reading/writing 'event'.
 * 
 * @author AgileDelta
 * @author Sun
 * 
 */
public class Event
{
    public static final short START_ELEMENT = 1;
    public static final short END_ELEMENT = 2;
    public static final short CHARACTERS = 3;
    public static final short NAMESPACE = 4;
    public static final short END_NAMESPACE = 5;
    public static final short ATTRIBUTE = 6;
    public static final short COMMENT = 7;
    public static final short PROCESSING_INSTRUCTION = 8;
    public static final short UNEXPANDED_ENTITY = 9;
    public static final short DOCTYPE = 10;
    public static final short END_DTD = 12;
    public static final short NOTATION = 13;
    public static final short UNPARSED_ENTITY = 14;
    public static final short EXTERNAL_ENTITY = 15;

    public static final short DATATYPE_NONE = 0;
    public static final short DATATYPE_STRING = 1;
    public static final short DATATYPE_CHAR_ARRAY = 2;
    public static final short DATATYPE_BOOLEAN = 3;
    public static final short DATATYPE_INT = 4;
    public static final short DATATYPE_LONG = 5;
    public static final short DATATYPE_FLOAT = 6;
    public static final short DATATYPE_DOUBLE = 7;
    public static final short DATATYPE_CALENDAR = 8;
    public static final short DATATYPE_BYTE_ARRAY = 9;
    
    /**
     * event type (START_ELEMENT, END_ELEMENT...)
     */
    public short type;
    
    /**
     * datatype for the value (if there is a value)
     */
    public short datatype;
    
    /**
     * name/QName/target depending on event
     */
    public String name; // name/qname/target
    
    /**
     * local-name or public-id, depending on event
     */
    public String localName; // local-name/public-id
    
    /**
     * namespace-uri or system-id, depending on event
     */
    public String namespace; // namespace/system-id

    // Value fields.  Which field depends on datatype value.
    public String stringValue; // value/notation
    public char[] charValue;
    public long longValue;
    public double doubleValue;
    public Calendar calendarValue;
    public byte[] binaryValue;

    /**
     * Construct a new Event object representing a start element 
     * with the given namespace, local-name, and qualified-name
     * 
     * @param namespace
     * @param localName
     * @param qName
     */
    public static Event newStartElement(String namespace, String localName, String qName)
    {
        Event e = new Event();
        e.type = START_ELEMENT;
        e.name = qName;
        e.localName = localName;
        e.namespace = namespace;
        return e;
    }

    /**
     * Construct a new Event object representing the end of an element
     * with the given namespace, local-name, and qualified-name.
     * 
     * @param namespace
     * @param localName
     * @param qName
     */
    public static Event newEndElement(String namespace, String localName, String qName)
    {
        Event e = new Event();
        e.type = END_ELEMENT;
        e.name = qName;
        e.localName = localName;
        e.namespace = namespace;
        return e;
    }

    /**
     * Construct a new Event object representing an attribute
     * with the given namespace, local-name, qualified-name, and value.
     * 
     * @param namespace
     * @param localName
     * @param qName
     * @param value
     */
    public static Event newAttribute(String namespace, String localName, String qName, String value)
    {
        Event e = new Event();
        e.type = ATTRIBUTE;
        e.datatype = DATATYPE_STRING;
        e.name = qName;
        e.localName = localName;
        e.namespace = namespace;
        e.stringValue = value;
        return e;
    }

    /**
     * Construct a new Event object representing the start of a namespace mapping
     * for the given prefix and namespace-uri.
     * 
     * @param prefix
     * @param namespace
     */
    public static Event newNamespace(String prefix, String namespace)
    {
        Event e = new Event();
        e.type = NAMESPACE;
        e.datatype = DATATYPE_STRING;
        e.name = prefix;
        e.namespace = namespace;
        return e;
    }

    /**
     * Construct a new Event object representing the end of a namespace mapping
     * for the given prefix.
     * 
     * @param prefix
     */
    public static Event newEndNamespace(String prefix)
    {
        Event e = new Event();
        e.type = END_NAMESPACE;
        e.datatype = DATATYPE_STRING;
        e.name = prefix;
        return e;
    }

    /**
     * Construct a new Event object representing some character data.
     * 
     * @param data
     */
    public static Event newCharacters(char[] data)
    {
        Event e = new Event();
        e.type = CHARACTERS;
        e.datatype = DATATYPE_CHAR_ARRAY;
        e.charValue = data;
        return e;
    }

    /**
     * Construct a new Event object representing a Comment.
     * 
     * @param data
     */
    public static Event newComment(char[] data)
    {
        Event e = new Event();
        e.type = COMMENT;
        e.datatype = DATATYPE_CHAR_ARRAY;
        e.charValue = data;
        return e;
    }

    /**
     * Construct a new Event object representing a Processing-Instruction
     * with the given target and data.
     * 
     * @param target
     * @param data
     */
    public static Event newProcessingInstruction(String target, String data)
    {
        Event e = new Event();
        e.type = PROCESSING_INSTRUCTION;
        e.datatype = DATATYPE_STRING;
        e.name = target;
        e.stringValue = data;
        return e;
    }

    /**
     * Construct a new Event object representing an unexpanded Entity
     * with the given name.
     * 
     * @param name
     */
    public static Event newUnexpandedEntity(String name)
    {
        Event e = new Event();
        e.type = UNEXPANDED_ENTITY;
        e.name = name;
        return e;
    }

    /**
     * Construct a new Event object representing a Doctype declaration.
     * 
     * @param name
     * @param publicId
     * @param systemId
     */
    public static Event newDoctype(String name, String publicId, String systemId)
    {
        Event e = new Event();
        e.type = DOCTYPE;
        e.name = name;
        e.localName = publicId;
        e.namespace = systemId;
        return e;
    }

    /**
     * Construct a new Event object representing the end of a DTD.
     */
    public static Event newEndDTD()
    {
        Event e = new Event();
        e.type = END_DTD;
        return e;
    }

    /**
     * Construct a new Event object representing a Notation declaration.
     * 
     * @param name
     * @param publicId
     * @param systemId
     */
    public static Event newNotation(String name, String publicId, String systemId)
    {
        Event e = new Event();
        e.type = NOTATION;
        e.name = name;
        e.localName = publicId;
        e.namespace = systemId;
        return e;
    }

    /**
     * Construct a new Event object representing an Unparsed Entity declaration
     * 
     * @param name
     * @param publicId
     * @param systemId
     * @param notationName
     */
    public static Event newUnparsedEntity(String name, String publicId, String systemId, String notationName)
    {
        Event e = new Event();
        e.type = UNPARSED_ENTITY;
        e.name = name;
        e.localName = publicId;
        e.namespace = systemId;
        e.stringValue = notationName;
        return e;
    }

    /**
     * Construct a new Event object representing an external Entity declaration.
     * (Note: this is needed to support serializing unexpanded entities.)
     * 
     * @param name
     * @param publicId
     * @param systemId
     */
    public static Event newExternalEntity(String name, String publicId, String systemId)
    {
        Event e = new Event();
        e.type = EXTERNAL_ENTITY;
        e.name = name;
        e.localName = publicId;
        e.namespace = systemId;
        return e;
    }
    
    //////////////////////////////////////////////////////////////////////
    // value accessors

    /**
     * Return value as String.
     * Error if datatype != DATATYPE_STRING.
     */
    public String getValueString()
    {
        if (datatype != DATATYPE_STRING)
            throw new IllegalStateException();
        return stringValue;
    }

    /**
     * Return value as char[].
     * Error if datatype != DATATYPE_CHAR_ARRAY.
     */
    public char[] getValueCharArray()
    {
        if (datatype != DATATYPE_CHAR_ARRAY)
            throw new IllegalStateException();
        return charValue;
    }

    /**
     * Return value as boolean.
     * Error if datatype != DATATYPE_BOOLEAN.
     */
    public boolean getValueBoolean()
    {
        if (datatype != DATATYPE_BOOLEAN)
            throw new IllegalStateException();
        return (0 != longValue);
    }

    /**
     * Return value as int.
     * Error if datatype != DATATYPE_INT.
     */
    public int getValueInt()
    {
        if (datatype != DATATYPE_INT)
            throw new IllegalStateException();
        return (int) longValue;
    }

    /**
     * Return value as long.
     * Error if datatype != DATATYPE_LONG.
     */
    public long getValueLong()
    {
        if (datatype != DATATYPE_LONG)
            throw new IllegalStateException();
        return longValue;
    }

    /**
     * Return value as float.
     * Error if datatype != DATATYPE_FLOAT.
     */
    public float getValueFloat()
    {
        if (datatype != DATATYPE_FLOAT)
            throw new IllegalStateException();
        return (float) doubleValue;
    }

    /**
     * Return value as double.
     * Error if datatype != DATATYPE_DOUBLE.
     */
    public double getValueDouble()
    {
        if (datatype != DATATYPE_DOUBLE)
            throw new IllegalStateException();
        return doubleValue;
    }

    /**
     * Return value as byte[].
     * Error if datatype != DATATYPE_BYTE_ARRAY.
     */
    public byte[] getValueByteArray()
    {
        if (datatype != DATATYPE_BYTE_ARRAY)
            throw new IllegalStateException();
        return binaryValue;
    }
}