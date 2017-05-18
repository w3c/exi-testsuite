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

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.w3c.dom.Document;
import org.w3c.exi.ttf.SAXDriver;
import org.w3c.exi.ttf.fragments.FragmentsSAXHandler;
import org.w3c.exi.ttf.parameters.DriverParameters;
import org.w3c.exi.ttf.parameters.TestCaseParameters;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Daniel.Peintner.EXT@siemens.com
 * 
 */
public class CanonicalXMLDOMDriver extends SAXDriver {

	/* XML file is parsed to a DOM once and just passed when encoded several times */
	private static final boolean PARSE_DOM_ONCE = false;

	SAXParserFactory _spf;
	XMLReader _reader;

	DocumentBuilder documentBuilder;
	Canonicalizer c14n;

	String xmlFile;
	Document document;

	public CanonicalXMLDOMDriver() throws ParserConfigurationException,
			InvalidCanonicalizerException {

		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		dfactory.setNamespaceAware(true);
		dfactory.setValidating(true);

		// DOM builder
		documentBuilder = dfactory.newDocumentBuilder();

		// this is to throw away all validation warnings
		documentBuilder
				.setErrorHandler(new org.apache.xml.security.utils.IgnoreAllErrorHandler());

		// initialize the xml-security library correctly before you using it
		org.apache.xml.security.Init.init();

		// canonicalizer
		c14n = Canonicalizer
				.getInstance("http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments");

		// init parser
		_spf = SAXParserFactory.newInstance();
		_spf.setNamespaceAware(true);
	}

	@Override
	protected void prepareTestCase(DriverParameters driverParams,
			TestCaseParameters testCaseParams) throws Exception {
		// save xml file
		xmlFile = testCaseParams.xmlFile;

		if (PARSE_DOM_ONCE) {
			document = documentBuilder.parse(new FileInputStream(xmlFile));
		}
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
		// parse xml to DOM
		if (!PARSE_DOM_ONCE) {
			document = documentBuilder.parse(new FileInputStream(xmlFile));
		}

		// canonicalize DOM to byte array
		byte outputBytes[] = c14n.canonicalizeSubtree(document);
		// write byte-array to output stream
		outputStream.write(outputBytes, 0, outputBytes.length);

		// // return default handler which doesn't do anything
		// return new DefaultHandler();

		// return null to indicate that SAX events are not of interest
		return null;
	}

}
