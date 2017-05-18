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

package org.w3c.exi.ttf.candidate.canonicalxml;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.ext.LexicalHandler;


public class SAXHandler extends DefaultHandler2
{
	ContentHandler contentHandler;
	
	public SAXHandler( ContentHandler ch )
	{
		this.contentHandler = ch;
	}
	
    public void startDocument() throws SAXException
    {
    	contentHandler.startDocument ( );
    }
    
    public void endDocument() throws SAXException
    {
    	contentHandler.endDocument ( );
    }
    
    public void startElement (String uri, String localName,
		      String qName, Attributes attributes )
	throws SAXException
	{
    	contentHandler.startElement ( uri, localName, qName, attributes );
	}
    
    public void endElement (String uri, String localName, String qName)
	throws SAXException
    {
    	contentHandler.endElement ( uri, localName, qName );
    }
    
    public void characters (char ch[], int start, int length)
	throws SAXException
    {
    	contentHandler.characters ( ch, start, length );
    }

    public void processingInstruction (String target, String data)
	throws SAXException
    {
    	contentHandler.processingInstruction ( target, data );
    }
    
    public void comment (char ch [], int start, int length)
    throws SAXException
	{
    	if ( contentHandler instanceof LexicalHandler )
    	{
    		((LexicalHandler)contentHandler).comment ( ch, start, length );
    	}
	}
    
}
