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

package org.w3c.exi.ttf.candidate.exificient;

import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.exi.ttf.SAXDriver;
import org.w3c.exi.ttf.fragments.FragmentsInputStream;
import org.w3c.exi.ttf.fragments.FragmentsSAXHandler;
import org.w3c.exi.ttf.parameters.DriverParameters;
import org.w3c.exi.ttf.parameters.TestCaseParameters;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.siemens.ct.exi.EXIFactory;
import com.siemens.ct.exi.api.sax.SAXFactory;
import com.siemens.ct.exi.api.sax.SAXHandler;
import com.siemens.ct.exi.api.sax.SAXEncoder;

/**
 * @author Daniel.Peintner.EXT@siemens.com
 */
public class EXIficientSAXDriver extends SAXDriver {

	EXIFactory _exiFactory;
	SAXFactory _saxFactory;
	XMLReader _exiReader;
	SAXEncoder _exiWriter;
	
	String _inputFilename;

	@Override
	protected void prepareTestCase(DriverParameters driverParams,
			TestCaseParameters testCaseParams) throws Exception {
		
		_exiFactory = getParameterParser().createFactory(driverParams,
				testCaseParams);
		_saxFactory = new SAXFactory(_exiFactory);
		_exiReader = _saxFactory.createEXIReader();
		_exiWriter = _saxFactory.createEXIWriter();
		_inputFilename = testCaseParams.xmlFile;
		
		// System.out.println("EXIficientFactory: " + _exiFactory);
	}

	protected DriverParametersParser getParameterParser() {
		return new DriverParametersParser();
	}

	@Override
	public void transcodeTestCase(InputStream xmlInput,
			OutputStream encodedOutput) throws Exception {
		
		if (_testCaseParams.fragments) {
			xmlInput = new FragmentsInputStream(xmlInput);
		}
		
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setFeature("http://xml.org/sax/features/resolve-dtd-uris", false);
		spf.setFeature("http://xml.org/sax/features/validation", false);
		spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		// spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
		// spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		spf.setNamespaceAware(true);
		SAXParser parser = spf.newSAXParser();
        
        // ContentHandler
        SAXEncoder writer = _saxFactory.createEXIWriter();
        writer.setOutputStream(encodedOutput);
        InputSource xmlInSrc = new InputSource(xmlInput);
        xmlInSrc.setSystemId(_inputFilename);
        parser.setProperty("http://xml.org/sax/properties/lexical-handler", writer);
        DefaultHandler contentHandler = _testCaseParams.fragments ? new FragmentsSAXHandler(writer) : new SAXHandler(writer);
        parser.parse(xmlInSrc, contentHandler);
	}

	// reads EXI stream again
	@Override
	protected XMLReader getXMLReader() throws Exception {
		return _exiReader;
	}

	// writes EXI format to output stream
	@Override
	protected ContentHandler getSAXEncoder(OutputStream outputStream)
			throws Exception {
		_exiWriter.setOutputStream(outputStream);
		return _exiWriter;
	}

}
