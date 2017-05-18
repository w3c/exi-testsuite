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

import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.security.c14n.Canonicalizer;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class SAX2DOM
{
	
	protected XMLReader xmlReader;
	protected InputSource xmlInput;

	public SAX2DOM( XMLReader reader, InputSource input )
	{
		xmlReader = reader;
		xmlInput = input ;
	}

	public Document createDom()
	{
		Document doc = null;
		try
		{
			// setup factory
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			DOMImplementation impl = builder.getDOMImplementation();

			// create document
			doc = impl.createDocument(null, null, null);

			// setup handlers
			SAX2DOMHandler handlers = new SAX2DOMHandler(doc);
			xmlReader.setContentHandler(handlers);
			xmlReader.setErrorHandler(handlers);
			xmlReader.parse(xmlInput);
		}
		catch (Exception e)
		{
			System.err.println(e);
		}
		return doc;
	}
}

class SAX2DOMHandler extends DefaultHandler
{

	protected Document document;
	protected Node currentNode;

	protected Canonicalizer c14n;
	protected OutputStream os;

	public SAX2DOMHandler(Document doc)
	{
		document = doc;
		currentNode = document;
	}

	public void setOutput(OutputStream os)
	{
		this.os = os;
	}

	public void setCanonicalizer(Canonicalizer c14n)
	{
		this.c14n = c14n;
	}

	/*
	 * add element to the right place of the DOM
	 * 
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String name, String qName,
			Attributes attrs)
	{	
		// create new element
		Element elem = document.createElementNS(uri, qName);
		
		// add all attributes
		for (int i = 0; i < attrs.getLength(); ++i)
		{
			String ns_uri = attrs.getURI(i);
			String qname = attrs.getQName(i);
			String value = attrs.getValue(i);
			Attr attr = document.createAttributeNS(ns_uri, qname);
			attr.setValue(value);
			elem.setAttributeNodeNS(attr);
		}
		
		// add element to the tree, and update current node
		currentNode.appendChild(elem);
		currentNode = elem;
	}

	@Override
	public void endDocument() throws SAXException 
	{
		// finally canonicalize
		try
		{
			byte outputBytes[] = c14n.canonicalizeSubtree(document);

			if (os != null)
			{
				os.write(outputBytes, 0, outputBytes.length);
			}
		} catch (Exception e)
		{
			throw new SAXException(e);
		}
	}

	// update current node for subsequent additions
	public void endElement(String uri, String name, String qName)
	{
		currentNode = currentNode.getParentNode();
	}

	// add new text node 
	public void characters(char[] ch, int start, int length)
	{
		String str = new String(ch, start, length);
		Text text = document.createTextNode(str);
		currentNode.appendChild(text);
	}

	// add new text node 
	public void ignorableWhitespace(char[] ch, int start, int length)
	{
		String str = new String(ch, start, length);
		Text text = document.createTextNode(str);
		currentNode.appendChild(text);
	}

	// add PI to the DOM tree
	public void processingInstruction(String target, String data)
	{
		ProcessingInstruction pi = document.createProcessingInstruction(target,
				data);
		currentNode.appendChild(pi);
	}

	// use system out error handlers
	public void error(SAXParseException e)
	{
		System.err.println("[ERROR]  (line " + e.getLineNumber()
				+ ", col " + e.getColumnNumber() + ") : " + e.getMessage());
	}

	public void fatalError(SAXParseException e)
	{
		System.err.println("[FATAL ERROR] " + e.getMessage());
	}

	public void warning(SAXParseException e)
	
	{
		System.err.println("[WARNING] " + e.getMessage());
	}
}
