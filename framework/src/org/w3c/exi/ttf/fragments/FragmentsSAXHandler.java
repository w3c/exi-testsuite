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

package org.w3c.exi.ttf.fragments;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

public class FragmentsSAXHandler extends DefaultHandler implements ContentHandler, LexicalHandler, DTDHandler, EntityResolver, DeclHandler, ErrorHandler
{
	protected ContentHandler contentHandler;
	protected LexicalHandler lexicalHandler;
	protected DTDHandler dtdHandler;
	protected EntityResolver entityResolver;
	protected DeclHandler declHandler;
	protected ErrorHandler errorHandler;
	protected int elementCount = 0;

	public FragmentsSAXHandler(ContentHandler contentHandler)
	{
		// Wrap ContentHandler, and optionally wrap other SAX interfaces to allow forwarding of all events
		this.contentHandler = contentHandler;
		if (contentHandler instanceof LexicalHandler)
			lexicalHandler = (LexicalHandler)contentHandler;
		if (contentHandler instanceof DTDHandler)
			dtdHandler = (DTDHandler)contentHandler;
		if (contentHandler instanceof EntityResolver)
			entityResolver = (EntityResolver)contentHandler;
		if (contentHandler instanceof DeclHandler)
			declHandler = (DeclHandler)contentHandler;
		if (contentHandler instanceof ErrorHandler)
			errorHandler = (ErrorHandler)contentHandler;
	}

	public void startElement(String uri, String localName, String qName, Attributes atts)
		throws SAXException
	{
		// strip the root start element and forward all other elements
		if (elementCount > 0)
		{
			contentHandler.startElement(uri, localName, qName, atts);
		}
		elementCount++;
	}

	public void endElement(String uri, String localName, String qName)
		throws SAXException
	{
		// strip the root end element and forward all other elements
		elementCount--;
		if (elementCount > 0)
		{
			contentHandler.endElement(uri, localName, qName);
		}
	}

	public void characters(char[] ch, int start, int length)
		throws SAXException
	{
		contentHandler.characters(ch, start, length);
	}

	public void endDocument() throws SAXException
	{
		contentHandler.endDocument();
	}

	public void endPrefixMapping(String prefix) throws SAXException
	{
		contentHandler.endPrefixMapping(prefix);
	}

	public void ignorableWhitespace(char[] ch, int start, int length)
		throws SAXException
	{
		contentHandler.ignorableWhitespace(ch, start, length);
	}

	public void processingInstruction(String target, String data)
		throws SAXException
	{
		contentHandler.processingInstruction(target, data);
	}

	public void setDocumentLocator(Locator locator)
	{
		contentHandler.setDocumentLocator(locator);
	}

	public void skippedEntity(String name) throws SAXException
	{
		contentHandler.skippedEntity(name);
	}

	public void startDocument() throws SAXException
	{
		contentHandler.startDocument();
	}

	public void startPrefixMapping(String prefix, String uri)
		throws SAXException
	{
		contentHandler.startPrefixMapping(prefix, uri);
	}

	public void comment(char[] ch, int start, int length) throws SAXException
	{
		if (lexicalHandler != null)
			lexicalHandler.comment(ch, start, length);
		
	}

	public void endCDATA() throws SAXException
	{
		if (lexicalHandler != null)
			lexicalHandler.endCDATA();
	}

	public void endDTD() throws SAXException
	{
		if (lexicalHandler != null)
			lexicalHandler.endDTD();
	}

	public void endEntity(String name) throws SAXException
	{
		if (lexicalHandler != null)
			lexicalHandler.endEntity(name);
	}

	public void startCDATA() throws SAXException
	{
		if (lexicalHandler != null)
			lexicalHandler.startCDATA();
	}

	public void startDTD(String name, String publicId, String systemId) throws SAXException
	{
		if (lexicalHandler != null)
			lexicalHandler.startDTD(name, publicId, systemId);
	}

	public void startEntity(String name) throws SAXException
	{
		if (lexicalHandler != null)
			lexicalHandler.startEntity(name);
	}

	public void notationDecl(String name, String publicId, String systemId) throws SAXException
	{
		if (dtdHandler != null)
			dtdHandler.notationDecl(name, publicId, systemId);
	}

	public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException
	{
		if (dtdHandler != null)
			dtdHandler.unparsedEntityDecl(name, publicId, systemId, notationName);
	}

	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
	{
		if (entityResolver != null)
			return entityResolver.resolveEntity(publicId, systemId);
		else
			return null;
	}

	public void attributeDecl(String eName, String aName, String type, String mode, String value) throws SAXException
	{
		if (declHandler != null)
			declHandler.attributeDecl(eName, aName, type, mode, value);
	}

	public void elementDecl(String name, String model) throws SAXException
	{
		if (declHandler != null)
			declHandler.elementDecl(name, model);
	}

	public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException
	{
		if (declHandler != null)
			declHandler.externalEntityDecl(name, publicId, systemId);
	}

	public void internalEntityDecl(String name, String value) throws SAXException
	{
		if (declHandler != null)
			declHandler.internalEntityDecl(name, value);
	}

	public void error(SAXParseException exception) throws SAXException
	{
		if (errorHandler != null)
			errorHandler.error(exception);
	}

	public void fatalError(SAXParseException exception) throws SAXException
	{
		if (errorHandler != null)
			errorHandler.fatalError(exception);
	}

	public void warning(SAXParseException exception) throws SAXException
	{
		if (errorHandler != null)
			errorHandler.warning(exception);
	}
}
