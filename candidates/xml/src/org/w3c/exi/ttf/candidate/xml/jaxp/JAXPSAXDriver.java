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

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.w3c.exi.ttf.SAXDriver;

import org.w3c.exi.ttf.parameters.DriverParameters;
import org.w3c.exi.ttf.parameters.PreserveParam;
import org.w3c.exi.ttf.parameters.TestCaseParameters;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

/**
 * @author AgileDelta
 * @author Sun
 *
 */
public class JAXPSAXDriver extends SAXDriver
{
    SAXParserFactory _spf;
    XMLReader _reader;
    SAXTransformerFactory _stf;
    
    public JAXPSAXDriver() {
        // init parser
        _spf = SAXParserFactory.newInstance();
        _spf.setNamespaceAware(true);
        
        // init writer
        _stf = (SAXTransformerFactory)SAXTransformerFactory.newInstance();        
    }

    @Override
    protected void prepareTestCase(DriverParameters driverParams, 
            TestCaseParameters testCaseParams) throws Exception
    {
        _reader = _spf.newSAXParser().getXMLReader();        
        
        if (!testCaseParams.preserves.contains(PreserveParam.dtds)) {
            // Switch of DTD processing
            // This is a Xerces specific feature
            _reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        }

        // XML processor by itself does not know how to deal with fragments.
        // Using a reader wrapper that will add the FragmentsInputStream wrapper
        if (testCaseParams.fragments)
        	_reader = new FragmentsReader(_reader);
    }

    /**
     * Override default transcode implementation to preserve exact bytes and size.
     * Round-tripping through JAXP writer seems to expand empty element tags,
     * among other things which change the size.
     */
    @Override
    public void transcodeTestCase(InputStream xmlInput, OutputStream encodedOutput) 
    throws Exception
    {
        int bytes;
        byte[] buffer = new byte[4096];
        while (0 < (bytes = xmlInput.read(buffer)))
        {
            encodedOutput.write(buffer, 0, bytes);
        }
    }
    
    /* (non-Javadoc)
     * @see org.w3c.exi.ttf.EXISAXDriverBase#getXMLReader()
     */
    @Override
    protected XMLReader getXMLReader() throws Exception
    {
        return _reader;
    }

    @Override
    protected ContentHandler getSAXEncoder(OutputStream outputStream) throws Exception
    {
        TransformerHandler handler = _stf.newTransformerHandler();
        handler.setResult(new StreamResult(outputStream));
        return handler;
    }

}
