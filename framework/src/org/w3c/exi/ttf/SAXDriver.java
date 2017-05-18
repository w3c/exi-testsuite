/*
 * EXI Testing Task Force Measurement Suite: http://www.w3.org/XML/EXI/
 *
 * Copyright © [2006] World Wide Web Consortium, (Massachusetts Institute of
 * Technology, European Research Consortium for Informatics and Mathematics,
 * Keio University). All Rights Reserved. This work is distributed under the
 * W3C? Software License [1] in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
 */

package org.w3c.exi.ttf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;

import org.apache.xml.serializer.ToXMLStream;
import org.w3c.exi.ttf.fragments.FragmentsInputStream;
import org.w3c.exi.ttf.fragments.FragmentsSAXHandler;
import org.w3c.exi.ttf.parameters.MeasureParam;
import org.w3c.exi.ttf.parameters.PreserveParam;
import org.w3c.exi.ttf.sax.EmptyHandler;
import org.w3c.exi.ttf.sax.SAXRecorder;
import org.w3c.exi.ttf.sax.PrefixConverter;
import org.w3c.exi.ttf.sax.QnameInserter;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.japex.TestCase;


/**
 * Base class for EXI compactness and SAX API read/write performance drivers.
 * The purpose of this class is to simplify the work needed to implement
 * such a driver.
 *
 * <p>A SubClass MUST implement:<pre>
 *      void prepareTestCase(DriverParameters driverParams, TestCaseParameters testCaseParams)
 *      ContentHandler getSAXEncoder(OutputStream output)
 *      XMLReader getXMLReader()
 * </pre></p>
 *
 * <p>A SubClass MAY implement:<pre>
 *      void transcodeTestCase(String xmlfile, OutputStream encodedOutput)
 *      boolean gzipStream()
 * </pre></p>
 *
 * @author AgileDelta
 * @author Sun
 * @author Fujitsu
 *
 */
public abstract class SAXDriver extends BaseDriver {

    /**
     * The XMLReader to use for measuring processing decoding.
     */
    private XMLReader _xmlReader;

    private EmptyHandler _decodeHandler;

    private ContentHandler _encodeContentHandler;
    private LexicalHandler _encodeLexicalHandler;
    private DTDHandler _encodeDtdHandler;
    private DeclHandler _encodeDeclHandler;

    private AttributesImpl _attributeEventsHolder;

    private SAXParser _saxParser;

    private Event[] _events;

    /**
     * Return a serializer implementing at least ContentHandler.
     * (LexicalHandler, DTDHandler, and DeclHandler are also recognized.)
     *
     * @param outputStream
     *      destination OutputStream
     * @throws Exception
     */
    protected abstract ContentHandler getSAXEncoder(OutputStream outputStream)
    throws Exception;

    /**
     * Returns the {@link org.xml.sax.XMLReader} for this driver
     *
     * @return The XMLReader instance.
     *
     * @throws SAXException
     *             If any SAX errors occur during processing.
     */
    protected abstract XMLReader getXMLReader() throws Exception;

    public final void initializeDriver() {
        super.initializeDriver();
        try {
            SAXParserFactory spf = DecodingValidator.getSAXParserFactory();
            initializeParserFactory(spf);
            _saxParser = spf.newSAXParser();
        } catch (Exception e) {
            System.err.println("Error initializing driver");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    protected void initializeParserFactory(SAXParserFactory spf) throws Exception {
      spf.setNamespaceAware(true);
    }

    public final void prepare(TestCase testCase) {
        super.prepare(testCase);
        try {
        	OutputStream ostream;
            switch(_driverParams.measure) {
                case encode:
                    _events = createEventsFromXMLTestCase(
                            _testCaseParams.getXmlInputStream(), _testCaseParams.xmlSystemId);
                    _attributeEventsHolder = new AttributesImpl(_events);
                    break;
                case decode:
                    _decodeHandler = new EmptyHandler();

                    _xmlReader = getXMLReader();
                    _xmlReader.setContentHandler(_decodeHandler);
                    _xmlReader.setErrorHandler(_decodeHandler);
                    _xmlReader.setDTDHandler(_decodeHandler);
                    _xmlReader.setEntityResolver(_decodeHandler);
                    _xmlReader.setProperty(
                            "http://xml.org/sax/properties/lexical-handler",
                            _decodeHandler);
                    _xmlReader.setProperty(
                            "http://xml.org/sax/properties/declaration-handler",
                            _decodeHandler);
                    break;
                case iot_decode:
                  ostream = _dataSink.getOutputStream(); 
                  XMLReader reader = getWritingXMLReader(ostream);
                  InputStream istream = _dataSource.getInputStream();
                  // serialize encoded stream as XML
                  reader.parse(new InputSource(istream));
                  istream.close();
                  ostream.close();
                  validateStream(_dataSource.getInputStream(), 
                      _testCaseParams.getXmlInputStream(), true);
                  break;
                case iot_c14n_encode:
                  if (_testCaseParams.decodeOnly) {
                 	InputStream encodedStream = _testCaseParams.getEncodedInputStream();
                    ostream = _dataSink.getOutputStream(); 
                	byte[] bts = new byte[8192];
                	int n_bytes;
                	while ((n_bytes = encodedStream.read(bts)) >= 0) {
                	  ostream.write(bts, 0, n_bytes);
                	}
                	DecodingValidator.validateDecodeOnly();
                  }
                  else {
	                _events = createEventsFromXMLTestCase(
	                          _testCaseParams.getXmlInputStream(), _testCaseParams.xmlSystemId);
	                 _attributeEventsHolder = new AttributesImpl(_events);
	                 writeEventsToHandler(_events, _dataSink.getOutputStream());
                  }
                  _dataSink.finish();
                  break;
                default:
            }
        } catch (Exception e) {
        	throw getTestException("preparing", testCase, e);
        }
    }

    /**
     * Validate that driver can (re)parse input
     *
     * @param encodedInput
     *            the input stream of the encoded xml
     */
    @Override
    protected void validateStream(InputStream encodedInput,
            InputStream originalInput, boolean isIot) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XMLReader reader = getWritingXMLReader(outputStream);
        if (_driverParams.isXmlProcessor) {
            InputSource source = new InputSource(encodedInput);
            source.setSystemId(_testCaseParams.xmlSystemId);
            reader.parse(source);
        } else {
        	DecodingValidator validator = new DecodingValidator(_driverParams, _testCaseParams);
		    if (validator.checkDoingDiff(isIot)) {
		    	validator.diffXMLReaderStream(getXMLReader(), encodedInput,originalInput,isIot);
		    } 
		    else {
				// serialize encoded stream as XML
				reader.parse(new InputSource(encodedInput));
				InputStream xmlInput = new ByteArrayInputStream(outputStream.toByteArray());
				// parse decoded result
				validator.parseEncodedStreamAsXML(xmlInput);
		    }
        }
    }

    public final void run(TestCase testCase) {
    	try {
            if (_driverParams.measure == MeasureParam.decode) {
                final InputSource inputSource = new InputSource(_dataSource.getInputStream());
                if (_driverParams.isXmlProcessor) {
                    inputSource.setSystemId(_testCaseParams.xmlSystemId);
                }
                _xmlReader.parse(inputSource);
                _dataSource.finish();
            } else if (_driverParams.measure == MeasureParam.encode) {
                writeEventsToHandler(_events, _dataSink.getOutputStream());
                _dataSink.finish();
            }
        } catch (Exception e) {
            try {
                if (_dataSource != null) _dataSource.close();
                if (_dataSink != null) _dataSink.close();
            } catch (Exception ex) { }
            throw getTestException("running", testCase, e);
        }
    }

    /**
     * Parse text-xml and record Events for later playback.
     */
    private Event[] createEventsFromXMLTestCase(InputStream xmlInput, String systemId)
    throws Exception {
        // parse document and record parse events for replay
        ArrayList<Event> eventList = new ArrayList<Event>();
    	if (_testCaseParams._frameworkFragment)
    		xmlInput = new FragmentsInputStream(xmlInput);
        final InputSource inputSource = new InputSource(xmlInput);
        inputSource.setSystemId(systemId);

        SAXRecorder recorder = new SAXRecorder(eventList, true);
        DefaultHandler contentHandler = _testCaseParams._frameworkFragment
        		? new FragmentsSAXHandler(recorder) : recorder;
        _saxParser.setProperty(
                "http://xml.org/sax/properties/lexical-handler",
                recorder);
        _saxParser.setProperty(
                "http://xml.org/sax/properties/declaration-handler",
                recorder);
        if (!_testCaseParams._frameworkPreserve.contains(PreserveParam.dtds)) {
          // Avoid having external dtds affect whitespace preservation policy. (2006-10-05 by taki)
          // Note that SAXRecorder skips ignorable whitespaces (in terms of dtd).
          _saxParser.getXMLReader().setFeature(
              "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        }
        _saxParser.parse(inputSource, contentHandler);

        return eventList.toArray(new Event[eventList.size()]);
    }

    /**
     * Iterate over the events (as constructed by parseText()) and invoke
     * the appropriate methods on a serializer returned from getSAXEncoder().
     */
    private void writeEventsToHandler(Event[] inputEvents,
            OutputStream outputStream) throws Exception {
        ContentHandler encodeContentHandler = getSAXEncoder(outputStream);
        if (encodeContentHandler == null)
          return;
        if (encodeContentHandler != _encodeContentHandler) {
            _encodeContentHandler = encodeContentHandler;
            _encodeLexicalHandler = (encodeContentHandler instanceof LexicalHandler)
            ? (LexicalHandler)encodeContentHandler : null;
            _encodeDtdHandler = (encodeContentHandler instanceof DTDHandler)
            ? (DTDHandler)encodeContentHandler : null;
            _encodeDeclHandler = (encodeContentHandler instanceof DeclHandler)
            ? (DeclHandler)encodeContentHandler : null;
        }

        _encodeContentHandler.startDocument();
        for (int i=0; i < inputEvents.length; i++) {
            Event e = inputEvents[i];
            switch (e.type) {
                case Event.START_ELEMENT:
                    int elemIdx = i;
                    while (inputEvents[i+1].type == Event.ATTRIBUTE)
                        i++;
                    _attributeEventsHolder.init(elemIdx + 1, i - elemIdx);
                    _encodeContentHandler.startElement(e.namespace, e.localName, e.name,
                            _attributeEventsHolder);
                    break;
                case Event.END_ELEMENT:
                    _encodeContentHandler.endElement(e.namespace, e.localName, e.name);
                    break;
                case Event.CHARACTERS:
                    _encodeContentHandler.characters(e.charValue, 0, e.charValue.length);
                    break;
                case Event.NAMESPACE:
                    _encodeContentHandler.startPrefixMapping(e.name, e.namespace);
                    break;
                case Event.END_NAMESPACE:
                    _encodeContentHandler.endPrefixMapping(e.name);
                    break;
                case Event.COMMENT:
                    if (_encodeLexicalHandler != null)
                        _encodeLexicalHandler.comment(e.charValue, 0, e.charValue.length);
                    break;
                case Event.PROCESSING_INSTRUCTION:
                    _encodeContentHandler.processingInstruction(e.name, e.stringValue);
                    break;
                case Event.UNEXPANDED_ENTITY:
                    _encodeContentHandler.skippedEntity(e.name);
                    break;
                case Event.DOCTYPE:
                    if (_encodeLexicalHandler != null)
                        _encodeLexicalHandler.startDTD(e.name, e.localName/*SystemId*/, e.namespace/*PublicId*/);
                    break;
                case Event.END_DTD:
                    if (_encodeLexicalHandler != null)
                        _encodeLexicalHandler.endDTD();
                    break;
                case Event.NOTATION:
                    if (_encodeDtdHandler != null)
                        _encodeDtdHandler.notationDecl(e.name, e.localName/*SystemId*/, e.namespace/*PublicId*/);
                    break;
                case Event.UNPARSED_ENTITY:
                    if (_encodeDtdHandler != null)
                        _encodeDtdHandler.unparsedEntityDecl(e.name, e.localName/*SystemId*/, e.namespace/*PublicId*/, e.stringValue/*NotationName*/);
                    break;
                case Event.EXTERNAL_ENTITY:
                    if (_encodeDeclHandler != null)
                        _encodeDeclHandler.externalEntityDecl(e.name, e.localName/*SystemId*/, e.namespace/*PublicId*/);
                    break;
                default:
                    throw new RuntimeException("unexpected");
            }
        }
        _encodeContentHandler.endDocument();
    }

    /**
     * Implement SAX Attributes interface over an Event array.  Used by the encode() method.
     */
    private static class AttributesImpl implements Attributes {
        Event[] _events;
        int _idxStart;
        int _length;

        /**
         * Initialize providing event[] that will contain attribute information
         * @param events
         */
        public AttributesImpl(Event[] events) {
            _events = events;
        }

        /**
         * Prepare for use, attributes will be found sequencially
         * in the Event[] passed to the constructor starting at
         * index 'start' and continuing for 'attrCount' Events.
         * Each attribute is represented by a single Event object.
         * @param start
         *      index of the first attribute
         * @param attrCount
         *      count of attributes
         */
        public void init(int start, int attrCount) {
            this._idxStart = start;
            this._length = attrCount;
        }

        public int getLength() {
            return _length;
        }

        public String getLocalName(int index) {
            if (index < _length) {
                Event e = _events[_idxStart + index];
                return e.localName;
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        public String getQName(int index) {
            if (index < _length) {
                Event e = _events[_idxStart + index];
                return e.name;
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        public String getURI(int index) {
            if (index < _length) {
                Event e = _events[_idxStart + index];
                return e.namespace;
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        public String getValue(int index) {
            if (index < _length) {
                Event e = _events[_idxStart + index];
                return e.getValueString();
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        public int getIndex(String uri, String localName) {
            throw new RuntimeException("Not Implemented");
        }

        public int getIndex(String qName) {
            throw new RuntimeException("Not Implemented");
        }

        public String getType(int index) {
            return "CDATA";
        }

        public String getType(String uri, String localName) {
            throw new RuntimeException("Not Implemented");
        }

        public String getType(String qName) {
            throw new RuntimeException("Not Implemented");
        }

        public String getValue(String uri, String localName) {
            throw new RuntimeException("Not Implemented");
        }

        public String getValue(String qName) {
            throw new RuntimeException("Not Implemented");
        }
    }
    
//    private static class DTDGrabber implements LexicalHandler {
//      public String name, publicId, systemId;
//      DTDGrabber() {
//        name = null;
//        publicId = null;
//        systemId = null;
//      }
//      public void comment(char[] ch, int start, int length) {} 
//      public void endCDATA() {}
//      public void endDTD() {}
//      public void endEntity(String name) {} 
//      public void startCDATA()  {}
//      public void startDTD(String name, String publicId, String systemId) {
//        this.name = name;
//        this.publicId = publicId;
//        this.systemId = systemId;
//      } 
//      public void startEntity(String name) {}
//    }
//    
//    private String grabDTD(XMLReader reader, InputStream inputStream) throws Exception {
//      LexicalHandler origLexicalHandler = (LexicalHandler)reader.getProperty("http://xml.org/sax/properties/lexical-handler");
//      try {
//        DTDGrabber lexicalHandler = new DTDGrabber();
//        reader.setProperty("http://xml.org/sax/properties/lexical-handler", lexicalHandler);
//        InputSource is = new InputSource(inputStream);
//        is.setSystemId(_testCaseParams.xmlSystemId);
//        reader.setContentHandler(new DefaultHandler());
//        reader.parse(is);
//        String dtd = null;
//        if (lexicalHandler.name != null && lexicalHandler.name.length() != 0) {
//          dtd = "<!DOCTYPE " + lexicalHandler.name;
//          String systemId = lexicalHandler.systemId;
//          if (systemId != null && systemId.length() != 0) {
//            dtd += " ";
//            String publicId = lexicalHandler.publicId;
//            if (publicId != null && publicId.length() != 0) {
//              dtd += publicId + " ";
//            }
//            dtd += systemId;
//          }
//          dtd += ">\n";
//        }
//        return dtd; 
//      }
//      finally {
//        reader.setProperty("http://xml.org/sax/properties/lexical-handler", origLexicalHandler);
//      }
//    }

    /**
     * Acquire 
     * @param ostream
     * @return
     * @throws Exception
     */
    private XMLReader getWritingXMLReader(OutputStream ostream) throws Exception {
	ToXMLStream handler = new ToXMLStream();
	handler.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");
	handler.setOutputProperty(OutputKeys.ENCODING, "utf-8");
	handler.setOutputProperty(OutputKeys.INDENT, "no");
	handler.setOutputProperty(OutputKeys.METHOD, "xml");
	handler.setOutputStream(ostream);
    	
      XMLReader reader = getXMLReader();
      if (!_driverParams.isXmlProcessor) {
        reader = new QnameInserter(new PrefixConverter(reader));
      }
      reader.setContentHandler(handler);
      reader.setDTDHandler(handler);
      reader.setProperty("http://xml.org/sax/properties/declaration-handler",
                         handler);
      reader.setProperty("http://xml.org/sax/properties/lexical-handler",
                         handler);
      return reader;
    }
    
}
