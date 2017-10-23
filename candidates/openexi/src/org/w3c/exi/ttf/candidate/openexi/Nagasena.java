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

package org.w3c.exi.ttf.candidate.openexi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.SAXParserFactory;

import org.w3c.exi.ttf.SAXDriver;

import org.w3c.exi.ttf.parameters.DriverParameters;
import org.w3c.exi.ttf.parameters.MeasureParam;
import org.w3c.exi.ttf.parameters.PreserveParam;
import org.w3c.exi.ttf.parameters.TestCaseParameters;
import org.w3c.exi.ttf.fragments.FragmentsSAXHandler;
import org.w3c.exi.ttf.fragments.FragmentsInputStream;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.openexi.proc.EXISchemaResolver;
import org.openexi.proc.HeaderOptionsOutputType;
import org.openexi.proc.common.AlignmentType;
import org.openexi.proc.common.GrammarOptions;
import org.openexi.proc.common.QName;
import org.openexi.proc.common.SchemaId;
import org.openexi.schema.EmptySchema;
import org.openexi.schema.EXISchema;
import org.openexi.schema.GrammarSchema;
import org.openexi.proc.grammars.GrammarCache;
import org.openexi.scomp.EXISchemaFactory;
import org.openexi.scomp.EXISchemaReader;
import org.openexi.sax.EXIReader;
import org.openexi.sax.SAXTransmogrifier;
import org.openexi.sax.Transmogrifier;

/**
 * @author Takuki Kamiya (Fujitsu Laboratories of America)
 */
public class Nagasena extends SAXDriver
{
  private final EXISchemaFactory m_schemaCompiler;

  private EXISchema m_corpus;
  private SchemaId m_schemaId;  
  
  private URI m_xmlURI; 
  private boolean m_useSchema;
  
  private short m_grammarOptions;
  private boolean m_preserveLexicalValues;
  private boolean m_preserveExternalGeneralEntities;
  private AlignmentType m_alignmentType;
  private int m_blockSize;
  private boolean m_outputCookie;
  private HeaderOptionsOutputType m_outputOptions;

  private boolean m_fragmentSupport;

  private QName[] m_dtrm;

  private Transmogrifier m_streamEncoder;
  private SAXTransmogrifier m_saxEncoder;
  private EXIReader m_decoder;
  
  private SAXParserFactory  m_saxParserFactory;
  
  private int m_valuePartitionCapacity;
  private int m_valueMaxLength;
  
  private boolean m_useUTCTime;
  
  @Override
  protected void initializeParserFactory(SAXParserFactory spf) throws Exception  {
    super.initializeParserFactory(spf);
    m_saxParserFactory = spf;
  }

  public Nagasena() {
    super();
    m_schemaCompiler = new EXISchemaFactory();
  }

  @Override
  protected final void prepareTestCase(DriverParameters driverParams, TestCaseParameters testCaseParams)
    throws Exception {
    final boolean isStrict = !testCaseParams.schemaDeviations;
    
    m_grammarOptions = isStrict ? GrammarOptions.STRICT_OPTIONS : GrammarOptions.DEFAULT_OPTIONS;
    m_preserveLexicalValues = false;
    m_preserveExternalGeneralEntities = false;
    m_outputCookie = testCaseParams.includeCookie;
    m_outputOptions = testCaseParams.includeOptions ? testCaseParams.includeSchemaId ? 
    		HeaderOptionsOutputType.all : HeaderOptionsOutputType.lessSchemaId : HeaderOptionsOutputType.none;
    
    m_alignmentType = AlignmentType.bitPacked;
    m_blockSize = -1; 
    if (testCaseParams.compression) {
    	if(driverParams.measure == MeasureParam.iot_c14n_encode){
    		m_alignmentType = AlignmentType.preCompress;
    	} else {
    		m_alignmentType = AlignmentType.compress;
    	}
      m_blockSize = testCaseParams.blockSize; 
    }
    else if (testCaseParams.preCompression) {
      m_alignmentType = AlignmentType.preCompress;
      m_blockSize = testCaseParams.blockSize; 
    }
    else if (testCaseParams.byteAlign)
      m_alignmentType = AlignmentType.byteAligned;
    
    for (PreserveParam param : testCaseParams.preserves) {
      switch (param) {
        case comments:
          if (!isStrict)
            m_grammarOptions = GrammarOptions.addCM(m_grammarOptions);
          break;
        case pis:
          if (!isStrict)
            m_grammarOptions = GrammarOptions.addPI(m_grammarOptions);
          break;
        case prefixes:
          if (!isStrict)
            m_grammarOptions = GrammarOptions.addNS(m_grammarOptions);
          break;
        case dtds: 
          if (!isStrict)
            m_grammarOptions = GrammarOptions.addDTD(m_grammarOptions);
          break;
        case entityreferences:
          m_preserveExternalGeneralEntities = true;
          break;
        case lexicalvalues:
          m_preserveLexicalValues = true;
          break;
        case notations:
          break;
        case whitespace:
          break;
      }
    }

    m_fragmentSupport = testCaseParams.fragments;
    m_useUTCTime = testCaseParams.utcTime;
    
    final URI baseURI = new File(System.getProperty("user.dir")).toURI().resolve("whatever");
    m_xmlURI = resolveURI(escapeURI(testCaseParams.xmlFile), baseURI);

//    final SchemaId schemaId;  
    if (m_useSchema = (testCaseParams.useSchemas || driverParams.isSchemaOptimizing)) {
      String schemaLocation;
      if ((schemaLocation = testCaseParams.schemaLocation) == null)
        schemaLocation = "";
      m_schemaId = new SchemaId(schemaLocation);
      URI schemaURI = schemaLocation.length() != 0 ? resolveURI(schemaLocation, baseURI) : null;
//      if (schemaURI != null && schemaURI.toString().endsWith("autoschema.xsd")) {
//        schemaLocation = "";
//        schemaURI = null;
//      }
      assert schemaLocation.length() == 0 && schemaURI == null || schemaLocation.length() != 0 && schemaURI != null;
      m_corpus = schemaURI != null ? m_schemaCompiler.compile(new InputSource(schemaURI.toString())) : EmptySchema.getEXISchema();

      ByteArrayOutputStream baos;
      baos = new ByteArrayOutputStream();
      m_corpus.writeXml(baos, false);
      byte[] grammarXml = baos.toByteArray();
      baos.close();

      Transmogrifier transmogrifier = new Transmogrifier();
      transmogrifier.setGrammarCache(new GrammarCache(GrammarSchema.getEXISchema(), 
          GrammarOptions.STRICT_OPTIONS), new SchemaId("nagasena:grammar"));
      transmogrifier.setOutputOptions(HeaderOptionsOutputType.all);
      baos = new ByteArrayOutputStream();
      transmogrifier.setOutputStream(baos);
      ByteArrayInputStream inputStream = new ByteArrayInputStream(grammarXml);
      transmogrifier.encode(new InputSource(inputStream));
      inputStream.close();
      byte[] grammarBytes = baos.toByteArray();

      m_corpus = new EXISchemaReader().parse(new ByteArrayInputStream(grammarBytes));
    }
    else
      m_schemaId = new SchemaId(null);
    
    GrammarCache grammarCache = new GrammarCache(m_useSchema ? m_corpus : null, m_grammarOptions);

    m_dtrm = null;
    javax.xml.namespace.QName jaxpTypeNames[];
    if ((jaxpTypeNames = testCaseParams.dtrMapTypes) != null) {
      final int n_entries = jaxpTypeNames.length;
      m_dtrm = new QName[2 * n_entries];
      for (int i = 0; i < n_entries; i++) {
        javax.xml.namespace.QName jaxpType = jaxpTypeNames[i];
        m_dtrm[2 * i] = new QName(jaxpType.getLocalPart(), jaxpType.getNamespaceURI());
        javax.xml.namespace.QName jaxpCodec = testCaseParams.dtrMapRepresentations[i];
        m_dtrm[2 * i + 1] = new QName(jaxpCodec.getLocalPart(), jaxpCodec.getNamespaceURI());
      }
    }
    
    m_valuePartitionCapacity = testCaseParams.valuePartitionCapacity;
    m_valueMaxLength = testCaseParams.valueMaxLength;
    
    m_streamEncoder = new Transmogrifier(m_saxParserFactory);
    m_streamEncoder.setObserveC14N(driverParams.measure == MeasureParam.iot_c14n_encode);
    m_streamEncoder.setOutputCookie(m_outputCookie);
    m_streamEncoder.setOutputOptions(m_outputOptions);
    m_streamEncoder.setFragment(m_fragmentSupport);
    m_streamEncoder.setGrammarCache(grammarCache, m_schemaId);
    if (m_dtrm != null)
      m_streamEncoder.setDatatypeRepresentationMap(m_dtrm, m_dtrm.length >> 1);
    m_streamEncoder.setPreserveLexicalValues(m_preserveLexicalValues);
    m_streamEncoder.setResolveExternalGeneralEntities(!m_preserveExternalGeneralEntities);
    m_streamEncoder.setAlignmentType(m_alignmentType);
    if (m_blockSize != -1)
      m_streamEncoder.setBlockSize(m_blockSize);
    if (m_valuePartitionCapacity != -1)
      m_streamEncoder.setValuePartitionCapacity(m_valuePartitionCapacity);
    if (m_valueMaxLength != -1)
      m_streamEncoder.setValueMaxLength(m_valueMaxLength);
    m_streamEncoder.setUseUTCTime(m_useUTCTime);
    m_saxEncoder = m_streamEncoder.getSAXTransmogrifier();
    
    m_decoder = new EXIReader();
    m_decoder.setFragment(m_fragmentSupport);
    m_decoder.setGrammarCache(grammarCache);
    m_decoder.setEXISchemaResolver(new EXISchemaResolver() {
        public GrammarCache resolveSchema(String schemaId, short grammarOptions) {
          try {
            URI schemaURI = resolveURI(schemaId, baseURI);
            EXISchema schema = m_schemaCompiler.compile(new InputSource(schemaURI.toString()));
            return new GrammarCache(schema, grammarOptions);
          }
          catch (Exception e) {
            return null;
          }
        }
      }
    );
    
    if (m_dtrm != null)
      m_decoder.setDatatypeRepresentationMap(m_dtrm, m_dtrm.length >> 1);
    m_decoder.setPreserveLexicalValues(m_preserveLexicalValues);
    m_decoder.setAlignmentType(m_alignmentType);
    if (m_blockSize != -1)
      m_decoder.setBlockSize(m_blockSize);
    if (m_valuePartitionCapacity != -1)
      m_decoder.setValuePartitionCapacity(m_valuePartitionCapacity);
    if (m_valueMaxLength != -1)
      m_decoder.setValueMaxLength(m_valueMaxLength);
  }

//  @Override
//  public final void finish(TestCase testCase) {
//    super.finish(testCase);
//  }
    
  /**
   * Override default transcode implementation to preserve exact bytes and size.
   * Round-tripping through JAXP writer seems to expand empty element tags,
   * among other things which change the size.
   */
  @Override
  public final void transcodeTestCase(InputStream xmlInput, OutputStream encodedOutput) 
    throws Exception {
    
    if (m_fragmentSupport) {
      xmlInput = new FragmentsInputStream(xmlInput);
    }
    
    Transmogrifier streamEncoder = new Transmogrifier();
    streamEncoder.setOutputCookie(m_outputCookie);
    streamEncoder.setOutputOptions(m_outputOptions);
    streamEncoder.setFragment(m_fragmentSupport);
    streamEncoder.setGrammarCache(new GrammarCache(m_useSchema ? m_corpus : null, m_grammarOptions), m_schemaId);
    if (m_dtrm != null)
      streamEncoder.setDatatypeRepresentationMap(m_dtrm, m_dtrm.length >> 1);
    streamEncoder.setPreserveLexicalValues(m_preserveLexicalValues);
    streamEncoder.setResolveExternalGeneralEntities(!m_preserveExternalGeneralEntities);
    streamEncoder.setAlignmentType(m_alignmentType);
    if (m_blockSize != -1)
      streamEncoder.setBlockSize(m_blockSize);
    if (m_valuePartitionCapacity != -1)
      streamEncoder.setValuePartitionCapacity(m_valuePartitionCapacity);
    if (m_valueMaxLength != -1)
      streamEncoder.setValueMaxLength(m_valueMaxLength);

    InputSource inputSource = new InputSource(xmlInput);
    inputSource.setSystemId(m_xmlURI.toString());

    streamEncoder.setOutputStream(encodedOutput);

    if (m_fragmentSupport) {
      SAXTransmogrifier saxTransmogrifier = streamEncoder.getSAXTransmogrifier();
      FragmentsSAXHandler fragmentsSAXHandler = new FragmentsSAXHandler(saxTransmogrifier);
      XMLReader xmlReader = m_saxParserFactory.newSAXParser().getXMLReader();
      xmlReader.setContentHandler(fragmentsSAXHandler);
      xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", fragmentsSAXHandler);
      xmlReader.parse(inputSource);
    }
    else
      streamEncoder.encode(inputSource);
  }
    
  /* (non-Javadoc)
   * @see org.w3c.exi.ttf.EXISAXDriverBase#getXMLReader()
   */
  @Override
  protected final XMLReader getXMLReader() throws Exception {
    return m_decoder;
  }

  @Override
  protected final ContentHandler getSAXEncoder(OutputStream outputStream)
    throws Exception {
    m_streamEncoder.setOutputStream(outputStream);
    return m_saxEncoder;
  }

  /**
   * Resolve a string representing an uri into an absolute URI given a base URI.
   * Null is returned if the uri is null or the uri seems to be a relative one
   * with baseURI being null.
   * @param uri
   * @param baseURI
   * @return absolute URI
   * @throws URISyntaxException
   */
  public static URI resolveURI(String uri, URI baseURI)
      throws URISyntaxException {
    URI resolved = null;
    if (uri != null) {
      int pos;
      if ((pos = uri.indexOf(':')) <= 1) {
        if (pos == 1) {
          char firstChar = uri.charAt(0);
          if ('A' <= firstChar && firstChar <= 'Z' ||
              'a' <= firstChar && firstChar <= 'z') {
            resolved = new File(uri).toURI();
          }
        }
        else { // relative URI
          if (baseURI != null)
            resolved = baseURI.resolve(uri);
          else
            return null;
        }
      }
      if (resolved == null)
        resolved = new URI(uri); // cross your fingers
    }
    return resolved;
  }

  public static final String escapeURI(String uri) {
    StringBuffer buf = new StringBuffer();
    int i, len;
    byte[] bts = null;
    try {
      bts = uri.getBytes("UTF8");
    }
    catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      throw new RuntimeException(
          "System needs to support UTF8 encoding.", e);
    }
    for (i = 0, len = bts.length; i < len; i++) {
      final byte bt = bts[i];
      char ch = bt >= 0 ? (char)bt : (char)(256+bt);
      if (isURIChar(ch))
        buf.append(ch);
      else {
        buf.append('%');
        byte upper = (byte)(ch >> 4);
        byte lower = (byte)(ch & 0x0F);
        buf.append(URIC_HEX.charAt(upper));
        buf.append(URIC_HEX.charAt(lower));
      }
    }
    return buf.toString();
  }

  /**
   * Checks if a character is an URI char.
   * See http://www.ietf.org/rfc/rfc2396.txt
   *     http://www.w3.org/TR/REC-xml
   */
  private static boolean isURIChar(char ch) {
    if (ch < 0 || ch > 127)
      return false;

    return (ASCIITABLE[ch] & (int)ASCII_IS_URIC) != 0;
  }

  private static final String URIC_ALPHANUM =
    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static final String URIC_MARK = "-_.!~*'()";

  private static final String URIC_RESERVED   = ";/?:@&=+$,[]#%";
  private static final String URIC_UNRESERVED = URIC_ALPHANUM + URIC_MARK;

  private static final String URIC = URIC_UNRESERVED + URIC_RESERVED;

  private static final byte[] ASCIITABLE;
  private static final byte   ASCII_IS_URIC          = (byte)0x01;
  private static final byte   ASCII_IS_URIC_RESERVED = (byte)0x02;

  static {
    int i, len;
    ASCIITABLE = new byte[256];

    for (i = 0, len = URIC.length(); i < len; i++)
      ASCIITABLE[URIC.charAt(i)] |= ASCII_IS_URIC;

    for (i = 0, len = URIC_RESERVED.length(); i < len; i++)
      ASCIITABLE[URIC.charAt(i)] |= ASCII_IS_URIC_RESERVED;
  }

  private static final String URIC_HEX = "0123456789ABCDEF";


}
