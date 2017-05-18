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

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.exi.ttf.SAXDriver;
import org.w3c.exi.ttf.fragments.FragmentsSAXHandler;
import org.w3c.exi.ttf.parameters.DriverParameters;
import org.w3c.exi.ttf.parameters.PreserveParam;
import org.w3c.exi.ttf.parameters.TestCaseParameters;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Daniel.Peintner.EXT@siemens.com
 * 
 */
public class CanonicalXMLSAXDriver extends SAXDriver {
	SAXParserFactory _spf;
	XMLReader _reader;

	DocumentBuilder documentBuilder;
	Canonicalizer c14n;

	public CanonicalXMLSAXDriver() throws ParserConfigurationException,
			InvalidCanonicalizerException {

		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		dfactory.setNamespaceAware(true);
		dfactory.setValidating(true);

		//	DOM builder
		documentBuilder = dfactory.newDocumentBuilder();

		// this is to throw away all validation warnings
		documentBuilder
				.setErrorHandler(new org.apache.xml.security.utils.IgnoreAllErrorHandler());
		
		// initialize the xml-security library correctly before you using it
		org.apache.xml.security.Init.init();

		//	canonicalizer
		c14n = Canonicalizer
				.getInstance("http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments");

		// init parser
		_spf = SAXParserFactory.newInstance();
		_spf.setNamespaceAware(true);
	}

	@Override
	protected void prepareTestCase(DriverParameters driverParams,
			TestCaseParameters testCaseParams) throws Exception {
		_reader = _spf.newSAXParser().getXMLReader();

		if (!testCaseParams.preserves.contains(PreserveParam.dtds)) {
			// Switch of DTD processing
			// This is a Xerces specific feature
			_reader
					.setFeature(
							"http://apache.org/xml/features/nonvalidating/load-external-dtd",
							false);
		}

		// XML processor by itself does not know how to deal with fragments.
		// Using a reader wrapper that will add the FragmentsInputStream wrapper
		if (testCaseParams.fragments)
			_reader = new FragmentsReader(_reader);
	}

	@Override
	public void transcodeTestCase(InputStream xmlInput,
			OutputStream encodedOutput) throws Exception {
		
		ContentHandler writer = getSAXEncoder(encodedOutput);
		InputSource xmlInSrc = new InputSource(xmlInput);

		SAXParser parser = _spf.newSAXParser();
		DefaultHandler contentHandler = _testCaseParams.fragments ? new FragmentsSAXHandler(
				writer)
				: new SAXHandler(writer);

		parser.parse(xmlInSrc, contentHandler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.exi.ttf.EXISAXDriverBase#getXMLReader()
	 */
	@Override
	protected XMLReader getXMLReader() throws Exception {
		return _reader;
	}

	// writes canonical XML to output stream
	@Override
	protected ContentHandler getSAXEncoder(OutputStream outputStream)
			throws Exception {

		DOMImplementation impl = documentBuilder.getDOMImplementation ( );

		// create document
		Document doc = impl.createDocument ( null, null, null );

		// setup handlers
		SAX2DOMHandler handler = new SAX2DOMHandler ( doc );
		handler.setOutput ( outputStream );
		handler.setCanonicalizer ( c14n );

		return handler;
	}

}
