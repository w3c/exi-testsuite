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

package org.w3c.exi.ttf.candidate.xml.jaxp;

import java.io.IOException;
import java.io.InputStream;

import org.w3c.exi.ttf.fragments.FragmentsInputStream;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

public class FragmentsReader implements XMLReader
{
	protected XMLReader reader;

	public FragmentsReader(XMLReader reader)
	{
		this.reader = reader;
	}

	public void parse(InputSource input) throws IOException, SAXException
	{
		InputStream is = input.getByteStream();
		// Only using byte streams in framework.  Check just in case.
		if (is == null)
			throw new SAXException("A byte stream must be supplied");

		is = new FragmentsInputStream(is);
		input.setByteStream(is);
		reader.parse(input);
	}

	public void parse(String systemId) throws IOException, SAXException
	{
		// TODO: does not add FragmentInputStream wrapper.  This method is not used at this time.
		throw new SAXException("Not implemented");
	}

	public ContentHandler getContentHandler()
	{
		return reader.getContentHandler();
	}

	public DTDHandler getDTDHandler()
	{
		return reader.getDTDHandler();
	}

	public EntityResolver getEntityResolver()
	{
		return reader.getEntityResolver();
	}

	public ErrorHandler getErrorHandler()
	{
		return reader.getErrorHandler();
	}

	public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException
	{
		return reader.getFeature(name);
	}

	public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException
	{
		return reader.getProperty(name);
	}

	public void setContentHandler(ContentHandler handler)
	{
		reader.setContentHandler(handler);
	}

	public void setDTDHandler(DTDHandler handler)
	{
		reader.setDTDHandler(handler);
	}

	public void setEntityResolver(EntityResolver resolver)
	{
		reader.setEntityResolver(resolver);
	}

	public void setErrorHandler(ErrorHandler handler)
	{
		reader.setErrorHandler(handler);
	}

	public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException
	{
		reader.setFeature(name, value);
	}

	public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException
	{
		reader.setProperty(name, value);
	}

}
