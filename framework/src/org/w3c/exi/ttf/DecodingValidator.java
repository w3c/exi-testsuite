package org.w3c.exi.ttf;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.xml.serializer.ToXMLStream;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.exi.ttf.fragments.FragmentsInputStream;
import org.w3c.exi.ttf.fragments.FragmentsSAXHandler;
import org.w3c.exi.ttf.fragments.FragmentsXMLReader;
import org.w3c.exi.ttf.parameters.DriverParameters;
import org.w3c.exi.ttf.parameters.PreserveParam;
import org.w3c.exi.ttf.parameters.TestCaseParameters;
import org.w3c.exi.ttf.sax.EmptyHandler;
import org.w3c.exi.ttf.sax.FidelityFilter;
import org.w3c.exi.ttf.sax.PrefixConverter;
import org.w3c.exi.ttf.sax.QnameInserter;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.org.apache.xerces.internal.dom.AttrImpl;
import com.sun.org.apache.xerces.internal.dom.ElementImpl;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
import com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory;
import com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar;
import com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import com.sun.org.apache.xerces.internal.parsers.XMLDocumentParser;
import com.sun.org.apache.xerces.internal.parsers.XMLGrammarPreparser;
import com.sun.org.apache.xerces.internal.util.DefaultErrorHandler;
import com.sun.org.apache.xerces.internal.util.SymbolHash;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.util.XMLGrammarPoolImpl;
import com.sun.org.apache.xerces.internal.xni.Augmentations;
import com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import com.sun.org.apache.xerces.internal.xni.QName;
import com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import com.sun.org.apache.xerces.internal.xni.XMLLocator;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;
import com.sun.org.apache.xerces.internal.xs.AttributePSVI;
import com.sun.org.apache.xerces.internal.xs.ElementPSVI;
import com.sun.org.apache.xerces.internal.xs.ItemPSVI;
import com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration;
import com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition;
import com.sun.org.apache.xerces.internal.xs.XSConstants;
import com.sun.org.apache.xerces.internal.xs.XSElementDeclaration;
import com.sun.org.apache.xerces.internal.xs.XSModel;
import com.sun.org.apache.xerces.internal.xs.XSNamedMap;
import com.sun.org.apache.xerces.internal.xs.XSObject;
import com.sun.org.apache.xerces.internal.xs.XSObjectList;
import com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition;
import com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;

import fc.fp.syxaw.util.Log;
import fc.fp.util.xas.EventSequence;
import fc.fp.util.xas.EventSerializer;
import fc.fp.util.xas.SaxWriter;
import fc.fp.util.xas.XasExtUtil;
import fc.fp.util.xas.Event.MapURI;
import java.util.Locale;

/**
 * This class is responsible to diff the output of a decoding with the initial
 * input file
 * 
 * 
 */
public class DecodingValidator implements XasExtUtil.TypedValueHelper {

	protected DriverParameters _driverParams;
	public TestCaseParameters _testCaseParams;

	/**
	 * Schema Information
	 */
	private String schemaInfo = null;
	/**
	 * Strict mode
	 */
	private boolean strictMode = false;
	/**
	 * Fragment mode
	 */
	private boolean fragmentMode = false;
	/**
	 * Preservation mode
	 */
	private Set<PreserveParam> preservation = null;

	/**
	 * Preserve prefixes
	 */
	private boolean _preserve_prefixes;

	/**
	 * Canonicalize values if preserve.lexicalValues is not set
	 */
	private boolean _preserve_values;

	/**
	 * Information of the give schema
	 */
	private XSModel grammar = null;

	/**
	 * Method of diff
	 */
	private static java.lang.reflect.Method diffTool = null;
	/**
	 * Class of diff formatter
	 */
	private static Class<?> diffCode = null;
	/**
	 * Need diff ?
	 */
	private static final boolean needDiff = Boolean
			.getBoolean("org.w3c.exi.ttf.validateDiff");

	/**
	 * Type filed for Attributes
	 */
	private static Field attrTypeField = null;

	/**
	 * Type field for Elements
	 */
	private static Field elemTypeField = null;

	/**
	 * Type factory
	 */
	private static final SchemaDVFactory typeFactory
			= SchemaDVFactory.getInstance();

	/*
	 * Make sure to use JAXP factory classes come with JDK This is a nasty
	 * implementation to resolve following issues; 1) unintentional use of JAXP
	 * factories bound with the test target, 2) inconsistent instance versions
	 * among factories, 3) different factory class names among JDK versions.
	 */

	private static final boolean _use_jaxp;

	private final static int SCHEMA_FACTORY = 0;
	private final static int SAX_PARSER_FACTORY = 1;
	private final static int DOCUMENT_BUILDER_FACTORY = 2;
	private final static int NUMBER_OF_FACTORIES = 3;
	private final static Class<?>[] factoryClass = new Class<?>[NUMBER_OF_FACTORIES];

	private static boolean factoryNotFound = true;

	/**
	 * An {@code OutputStream} does not output anything
	 */
	private static PrintStream nullPrintStream = new PrintStream(
			new OutputStream() {
				@Override
				public void write(int b) throws IOException {}
			});

	/*
	 * Default framework encoding
	 */
	// private static String FRAMEWORK_ENCODING = "UTF-8";

	/**
	 * Always use decoding validator if {@code true}
	 */
	private static final boolean _use_decoder;
	/**
	 * Framework options
	 */
	private static final String _check;

	/**
	 * Framework full check option
	 */
	private static final boolean _full_check;

	/**
	 * Check schema
	 */
	private static final boolean _check_schema;

	/**
	 * Add value type if (@code true}
	 */
	private static final boolean _add_value_type;

	/**
	 * Show diff sequence if {@code true}
	 */
	private static final boolean _show_diff_sequence;
	
	/**
	 * Need extra check if {@code true} (experimental)
	 */
	private static final boolean _extra_effort;
	
	/**
	 * Check QName prefixes if (@code true}
	 */
	private static final boolean _qname_prefixes;

	/**
	 * Framework debug options
	 */
	private static final String _debug;

	/**
	 * Full debug
	 */
	private static final boolean _debug_full;

	/**
	 * Debug schema if {@code true}
	 */
	private static final boolean _debug_schema;

	/**
	 * Dump sequence switch
	 */
	private static enum DumpSequence {
		SEQ, XML
	}

	/**
	 * Debug diff sequences
	 */
	private static final DumpSequence _dump_sequence;
	/**
	 * Warn unexpected values
	 */
	private static final boolean _warn_unexpected;
	/**
	 * Debug typed value
	 */
	private static final boolean _debug_typed_item;
	/**
	 * Debug canonical value
	 */
	private static final boolean _debug_canonicalize;
	/**
	 * Inform ignored items
	 */
	private static final boolean _warn_ignored;
	/**
	 * Debug QName
	 */
	private static final boolean _debug_qname;

	/**
	 * Cause when diff class is not found
	 */
	private static Exception _diff_not_found;

	/**
	 * Remove runtime environment dependency
	 */
	static {
		/*
		 * Retrieve the class for the diff tool
		 */
		try {
			Class<?> diffClass = Class.forName("faxma.Diff");
			diffTool = diffClass.getDeclaredMethod("diff", EventSequence.class,
					fc.fp.util.xas.TypedXmlParser.class, EventSequence.class,
					fc.fp.util.xas.TypedXmlParser.class, OutputStream.class,
					Class.class, java.util.Map.class, Boolean.TYPE);
		} catch (Exception cause) {
			_diff_not_found = new RuntimeException(
					"Diff tool is not available (no diff class)", cause);
			diffTool = null;
		}

		if (diffTool != null) {
			try {
				EventSequence nullSeq = XasExtUtil.nullSequence();
				ByteArrayOutputStream nullOS = new ByteArrayOutputStream();
				diffCode = Class.forName("faxma.Diff$AlignEncoder");
				Log.setOut(new PrintStream(nullOS));
				diffTool.invoke(null, nullSeq, null, nullSeq, null, nullOS,
						diffCode, null, false);
			} catch (Exception cause) {
				_diff_not_found = new RuntimeException(
						"Diff tool is not available (no diff formatter)", cause);
				diffTool = null;
				diffCode = null;
			} finally {
				Log.setOut(System.out);
			}
			if (diffCode != null) {
				// remove the first log message from diff tool
				fc.fp.syxaw.util.Log.setOut(nullPrintStream);
				fc.fp.syxaw.util.Log.time("");
				fc.fp.syxaw.util.Log.setOut(System.out);
			}
		}

		/*
		 * Retrieve the "type" field for Attributes
		 */
		try {
			attrTypeField = AttrImpl.class.getDeclaredField("type");
			attrTypeField.setAccessible(true);
		} catch (Exception _) {
			attrTypeField = null;
			System.err
					.println("Warning: Validator could not find the type field for Attributes");
		}

		/*
		 * Retrieve the "type" field for Elements
		 */
		try {
			// First try Sun JDK 1.5
			elemTypeField = ElementImpl.class.getDeclaredField("type");
			elemTypeField.setAccessible(true);
		} catch (Exception _S15) {
			try {
				// Second type Sun JDK 1.6
				elemTypeField = ElementNSImpl.class.getDeclaredField("type");
				elemTypeField.setAccessible(true);
			} catch (Exception _S16) {
				elemTypeField = null;
				System.err
						.println("Warning: Validator could not get find the type field for Elements");
			}
		}

		/*
		 * Retrieve JDK specific JAXP factory classes
		 */

		// Class name prefix for Sun JDK
		final String sunJDK = "com.sun.org.apache.xerces.internal.jaxp.";

		// JDK version specific class names
		// [0] SchemaFactory
		// [1] SAXParserFactory
		// [2] DocumentBuilderFactory
		final String[][] factoryClassNames = {
				{ // Sun JDK 1.6
					sunJDK + "validation.XMLSchemaFactory",
					sunJDK + "SAXParserFactoryImpl",
					sunJDK + "DocumentBuilderFactoryImpl",
				},
				{ // Sun JDK  1.5
					sunJDK + "validation.xs.SchemaFactoryImpl",
					sunJDK + "SAXParserFactoryImpl",
					sunJDK + "DocumentBuilderFactoryImpl",
				},
				};

		// Check if JDK specific factory classes exist
		for (String[] className : factoryClassNames) {
			try {
				setFactoryClassByName(className, SCHEMA_FACTORY);
				setFactoryClassByName(className, SAX_PARSER_FACTORY);
				setFactoryClassByName(className, DOCUMENT_BUILDER_FACTORY);
			} catch (ClassNotFoundException _) {
				continue;
			}
			factoryNotFound = false;
			break;
		}

		/*
		 * Framework debug option
		 */
		_debug = DriverParameters._frameworkDebug;
		_debug_full = (_debug.indexOf(",full.debug,") >= 0);
		_debug_schema = (_debug_full || _debug.indexOf(",schema,") >= 0);

		if (_debug_full || _debug.indexOf(",diff.seq,") >= 0) {
			_dump_sequence = DumpSequence.SEQ;
		} else if (_debug.indexOf(",diff.xml,") >= 0) {
			_dump_sequence = DumpSequence.XML;
		} else {
			_dump_sequence = null;
		}

		_warn_ignored = (_debug_full || _debug.indexOf(",ignored.item,") >= 0);
		_debug_canonicalize = (_debug_full || _debug.indexOf(",canonicalize,") >= 0);
		_debug_typed_item = (_debug_canonicalize || _debug
				.indexOf(",typed.item,") >= 0);
		_debug_qname = (_debug_canonicalize || _debug.indexOf(",qname,") >= 0);
		_warn_unexpected = (_debug_full || _debug.indexOf(",verbose,") >= 0);

		/*
		 * Framework options
		 */
		_check = DriverParameters._frameworkCheck;
		_full_check = (_debug_full || (_check.indexOf(",full.check,") >= 0));
		_add_value_type = (_full_check || _check.indexOf(",value.typees,") >= 0);
		_check_schema = (_full_check || _check.indexOf(",schema,") >= 0);
		_show_diff_sequence = (_full_check || _check.indexOf(",diff.sequence,") >= 0);
		_qname_prefixes = (_check.indexOf(",qname.prefixes") >= 0);
		_use_decoder = (_check.indexOf(",use.decoder,") >= 0);
		_use_jaxp = (_check.indexOf(",use.jaxp,") >=0);
		_extra_effort = (_check.indexOf(",extra.effort,") >= 0);

		if (_check.indexOf(",full.URI,") < 0) {
			MapURI.enableMapping();
			MapURI.setDefaultMapping();
		}
	}

	/**
	 * Need newline before output message
	 */
	private boolean _need_flush_output = true;

	/**
	 * Constructor
	 * 
	 * @param dp
	 *            driver parameters
	 * @param tp
	 *            test case parameters
	 */
	public DecodingValidator(DriverParameters dp, TestCaseParameters tp) {
		this();
		setParameters(dp, tp);
	}

	public DecodingValidator() {
		if (factoryNotFound) {
			throw new DiffException("FATAL ERROR: "
					+ "DecodingValidator could not find JAXP factory classes");
		}
	}

	/**
	 * Set the test parameters
	 * 
	 * @param dp
	 *            driver parameters
	 * @param tp
	 *            test case parameters
	 */
	public void setParameters(DriverParameters dp, TestCaseParameters tp) {
		this._driverParams = dp;
		this._testCaseParams = tp;

		schemaInfo = tp._frameworkSchemaID;
		strictMode = tp._frameworkFaithful;
		fragmentMode = tp._frameworkFragment;
		preservation = tp._frameworkPreserve;

		_preserve_prefixes = preservation.contains(PreserveParam.prefixes);
		_preserve_values = preservation.contains(PreserveParam.lexicalvalues);
	}

	/**
	 * Test whether to use diff tool to validate the decoding output
	 * 
	 * @param isIot
	 *            true if interop testing
	 * @return
	 */
	public boolean checkDoingDiff(boolean isIot) {
		// force diff in iot, otherwise check system property
		if (isIot || needDiff) {
			if (diffTool == null || diffCode == null) {
				beginDebug(true);
				if (_diff_not_found != null) {
					System.err.println(_diff_not_found.getMessage());
					Throwable cause = _diff_not_found.getCause();
					if (cause != null) {
						cause.printStackTrace(System.err);
					}
					_diff_not_found = null;
				}
				throw new DiffException(
						"DiffNotFoundException: Diff tool is not available");
			}
			return true;
		}
		return false;
	}

	/**
	 * Parse the XML decoded stream
	 * 
	 * @param xmlInput
	 * @throws Exception
	 */
	public void parseEncodedStreamAsXML(InputStream xmlInput) throws Exception {
		// serialize encoded stream as XML
		if (fragmentMode)
			xmlInput = new FragmentsInputStream(xmlInput);
		InputSource is = new InputSource(xmlInput);
		is.setSystemId(_testCaseParams.xmlSystemId);
		SAXParserFactory spf = getSAXParserFactory();
		spf.setNamespaceAware(true);
		SAXParser _parser = spf.newSAXParser();
		XMLReader xmlReader = _parser.getXMLReader();
		xmlReader.setContentHandler(new EmptyHandler());
		xmlReader.parse(is);
	}

	/**
	 * Apply diff on a XML decoded input against the original input
	 * 
	 * @param decodedInput
	 *            the XML file generated by the decoding process
	 * @param originalInput
	 *            the original XML file used by the encoding process
	 * @param isIot
	 *            true if interop testing
	 * @throws Exception
	 */
	public void diffXMLDecodedStream(InputStream decodedInput,
			InputStream originalInput, boolean isIot) throws Exception {
		SAXParserFactory spf = getSAXParserFactory();
		spf.setNamespaceAware(true);
		SAXParser _parser = spf.newSAXParser();
		XMLReader xmlReader = _parser.getXMLReader();
		diffXMLReaderStream(xmlReader, decodedInput, originalInput, isIot);
	}

	/**
	 * Apply diff on a XML decoded input against the original input
	 * 
	 * @param testedReader
	 *            the SAX reader decoding the encoded data
	 * @param encodedInput
	 *            the encoded data
	 * @param originalInput
	 *            the
	 * @param originalInput
	 *            the original XML file used by the encoding process
	 * @param isIot
	 *            true if interop testing
	 * @throws Exception
	 */
	public void diffXMLReaderStream(XMLReader testedReader,
			InputStream encodedInput, InputStream originalInput, boolean isIot)
			throws Exception {
		if (diffTool == null) {
			beginDebug(true);
			System.err.println("Warning: Diff tool is not available");
			endDebug(false);
			return;
		}
 
		SAXParserFactory spf = getSAXParserFactory();
		spf.setNamespaceAware(true);
		SAXParser _parser = spf.newSAXParser();
		XMLReader xmlReader = _parser.getXMLReader();
		/**
		 * The following lines of code was inadvertently carried over from SAXDriver.java.
		 * There is no involvement of SAXRecorder here. Therefore, there is not a need to
		 * turn off loading external DTDs. (2011-09-22 by taki)
		 *
		 * // avoid having external dtds affect whitespace preservation policy.
		 * // (2006-09-20 by taki)
		 * xmlReader.setFeature(
		 *   "http://apache.org/xml/features/nonvalidating/load-external-dtd",
		 *   false);
		 */
		// These are the event sequences to diff
		EventSequence originalSequence = null;
		EventSequence candidateSequence = null;
		if (schemaInfo != null || isIot || _use_decoder) {
			originalSequence = convertValidatingDom(
					xmlReader, originalInput, isIot, true);
			candidateSequence = convertValidatingDom(
					testedReader, encodedInput, isIot, false);
		} else {
			originalSequence = convert(
					xmlReader, originalInput, isIot, true);
			candidateSequence = convert(
					testedReader, encodedInput, isIot, false);
		}
		// originalSequence = XasExtUtil.canonicalSequence(originalSequence,
		// isIot);
		// candidateSequence = XasExtUtil.canonicalSequence(candidateSequence,
		// isIot);

		/*
		 * if (!_need_flush_output) { try { System.err.flush(); Thread.sleep(5);
		 * } catch (Exception _) { } }
		 */
		endDebug(false);
		if (_dump_sequence == DumpSequence.SEQ) {
			beginDebug(true);
			dumpSequence(System.out, originalSequence, candidateSequence);
			endDebug(true);
		}
		ByteArrayOutputStream diffos = new ByteArrayOutputStream();

		// Changed this invocation to a dynamic one so that
		// the code can be distributed as such without faxma

		// if (faxma.Diff.diff(originalSequence, null,
		// candidateSequence, null,
		// diffos,
		// faxma.Diff.AlignEncoder.class, null, false)) {
		if ((Boolean) diffTool.invoke(null, originalSequence, null,
				candidateSequence, null, diffos, diffCode, null, false)) {
			if (_show_diff_sequence) {
				diffos.flush();
				dumpSequence(new PrintStream(diffos, false, "UTF-8"),
						originalSequence, candidateSequence);
			}
			diffos.close();
			beginDebug(false);
			DiffException diff = new DiffException(diffos.toByteArray());
			if (isIot) {
				throw diff;
			} else {
				diff.printStackTrace(System.out);
			}
		} else {
			diffos.close();
		}
		endDebug(false);
	}

	/**
	 * Reports decode only test cases
	 */
	public static void validateDecodeOnly() {
		fc.fp.syxaw.util.Log.log(
				"Documents identical  (decode only test case).",
				fc.fp.syxaw.util.Log.INFO);
		System.out.flush();
	}

	/**
	 * Dump raw sequences
	 * @param out output destination
	 * @param oSeq original xml sequence
	 * @param cSeq candidate sequence
	 */
	private void dumpSequence(PrintStream out, EventSequence oSeq,
			EventSequence cSeq) {
		dumpSequence(out, "Original", oSeq);
		dumpSequence(out, "Candidate", cSeq);
	}

	/**
	 * Dump a raw sequence
	 * @param out output destination
	 * @param caption caption before the sequence
	 * @param seq event sequence
	 */
	private static void dumpSequence(PrintStream out, String caption,
			EventSequence seq) {
		if (seq == null) {
			return;
		}
		out.print(caption);
		out.println(" Sequence:");
		final char id = caption.charAt(0);
		int x = 0;
		final int last = seq.getLargestActiveIndex();
		for (int i = seq.getSmallestActiveIndex(); i <= last; i++) {
			out.printf("%c[%d]: %s%n", id, x++, seq.get(i).toString());
		}
	}

	/**
	 * An exception represents diff errors, which prints diff results instead of
	 * the stack trace
	 */
	public static class DiffException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private static final String NL = System.getProperty("line.separator");
		private static final String caption = "Difference detected:";

		private final byte[] contents;

		private DiffException(byte[] contents) {
			super(caption + NL + bytesToString(contents));
			this.contents = contents;
		}

		private DiffException(String message) {
			super(message);
			this.contents = null;
		}

		// Print diff results instead of the stack trace
		@Override
		public void printStackTrace(PrintStream s) {
			synchronized (s) {
				if (contents == null) {
					s.println(getMessage());
				} else {
					s.println(caption);
					try {
						s.write(contents);
					} catch (IOException e) {
					}
				}
				s.flush();
			}
		}

		// Print diff results instead of the stack trace
		@Override
		public void printStackTrace(PrintWriter w) {
			synchronized (w) {
				if (contents == null) {
					w.println(getMessage());
				} else {
					w.println(caption);
					w.print(bytesToString(contents));
				}
				w.flush();
			}
		}

		private static String bytesToString(byte[] contents) {
			String message;
			try {
				message = new String(contents, "UTF-8");
			} catch (UnsupportedEncodingException _) {
				// this should not happen
				final int size = contents.length;
				char[] chars = new char[contents.length];
				for (int i = size; --i >= 0;) {
					chars[i] = (char) (contents[i] & 0xff);
				}
				message = new String(chars);
			}
			return message;
		}
	}

	/**
	 * Convert a document to a serialized event sequence with canonicalization
	 * @param reader a document reader
	 * @param input an input stream for the reader
	 * @param isIot running interoperability test if {@code true}
	 * @param isXml the input is the original XML document if {@code true}
	 * @return a serialized event sequence
	 * @throws Exception 
	 */
	private EventSequence convertValidatingDom(XMLReader reader,
			InputStream input, boolean isIot, boolean isXml) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] bts = new byte[1024];
		int n_bytes;
		while ((n_bytes = input.read(bts)) != -1) {
			if (n_bytes != 0)
				baos.write(bts, 0, n_bytes);
		}
		baos.close();
		bts = baos.toByteArray();
		input = new ByteArrayInputStream(bts);

		if (isXml && preservation.contains(PreserveParam.dtds)) {
			reader.setFeature(
					"http://xml.org/sax/features/external-general-entities",
					false);
		}

		ToXMLStream tf = new ToXMLStream();
		tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		tf.setOutputProperty(OutputKeys.INDENT, "no");
		tf.setOutputProperty(OutputKeys.METHOD, "xml");
		PrefixConverter superKludger = new PrefixConverter(reader);
		QnameInserter kludger = new QnameInserter(superKludger);
		FidelityFilter filter = new FidelityFilter(kludger, preservation, isIot);
		XMLReader realReader = ((fragmentMode && !isXml)
				? new FragmentsXMLReader(filter)
				: filter);
		if (fragmentMode && isXml) {
			input = new FragmentsInputStream(input);
		}
		InputSource is = new InputSource(input);
		is.setSystemId(_testCaseParams.xmlSystemId);
		is.setEncoding("UTF-8");
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		tf.setOutputStream(bout);
		realReader.setContentHandler(tf);
		realReader.setDTDHandler(tf);
		realReader.setProperty(
				"http://xml.org/sax/properties/lexical-handler", tf);
		realReader.parse(is);
		bts = bout.toByteArray();

		if (_debug_full || _dump_sequence == DumpSequence.XML) {
			beginDebug(true);
			System.err.println(isXml ? "O.xml: " : "C.xml: ");
			System.err.println(new String(bts, "UTF-8"));
		}

		Document doc;

		if (_use_jaxp) {
			DocumentBuilderFactory dbf = getDocumentBuilderFactory();
			Schema schema = null;

			dbf.setNamespaceAware(true);
			dbf.setIgnoringElementContentWhitespace(isIot ? strictMode
					: !preservation.contains(PreserveParam.whitespace));
			dbf.setAttribute(
					"http://apache.org/xml/features/dom/defer-node-expansion",
					Boolean.FALSE);

			if (schemaInfo != null) {
				SchemaFactory scf = getSchemaFactory();

				Source[] sources;
				if (fragmentMode) {
					sources = new Source[2];
					sources[1] = getSchemaSource(ROOT_SCHEMA);
				} else {
					sources = new Source[1];
				}
				sources[0] = getSchemaSource();
				schema = scf.newSchema(sources);
				dbf.setSchema(schema);
			}

			DocumentBuilder db = dbf.newDocumentBuilder();
			// Set error handler regardless
			db.setErrorHandler(new ErrorHandler() {
				private void print(String type, SAXParseException ex) {
					System.err.println(type + ": " + ex.getMessage());
					// ex.printStackTrace();
				}
				public void warning(SAXParseException ex) {
					// print("WARNING", ex);
				}
				public void error(SAXParseException ex) {
					// print("ERROR", ex);
				}
				public void fatalError(SAXParseException ex)
						throws SAXException {
					print("FATAL", ex);
					throw ex;
				}
			});
			doc = db.parse(new ByteArrayInputStream(bts),
					_testCaseParams.xmlSystemId);
			grammar = null;
		} else {
			final boolean debug = _debug_schema;
			final boolean verbose = _check_schema;

			final String XF = "http://apache.org/xml/features/";
			final String XP = "http://apache.org/xml/properties/";
			final String SF = "http://xml.org/sax/features/";
			// final String SP = "http://xml.org/sax/properties/";

			XMLGrammarPoolImpl grammarPool = null;
			SymbolTable sym = null;
			grammar = null;

			if (schemaInfo != null) {
				sym = new SymbolTable(2039);
				grammarPool = new XMLGrammarPoolImpl();
				XMLGrammarPreparser preparser = new XMLGrammarPreparser(sym);
				try {
					preparser.registerPreparser(
							XMLGrammarDescription.XML_SCHEMA, null);

					preparser.setProperty(XP + "internal/grammar-pool",
							grammarPool);

					preparser.setFeature(SF + "namespaces", true);
					preparser.setFeature(SF + "validation", true);
					preparser.setFeature(XF + "validation/schema", true);
					preparser.setFeature(
							XF + "validation/schema-full-checking", true);

					preparser.setErrorHandler(new DefaultErrorHandler() {
						@Override
						public void warning(String d, String k,
								XMLParseException e) throws XNIException {
							// ignore
							if (verbose) {
								System.out.printf(
										"Schema: PW.d=%s%nPW.k=%s%nPW.e=%s%n",
										d, k, e);
							}
						}

						@Override
						public void error(String d, String k,
								XMLParseException e) throws XNIException {
							// ignore
							if (verbose) {
								System.out.printf(
										"Schema: PE.d=%s%nPE.k=%s%nPE.e=%s%n",
										d, k, e);
							}
						}
					});

					SchemaGrammar[] sg = new SchemaGrammar[1];
					sg[0] = (SchemaGrammar) preparser.preparseGrammar(
							XMLGrammarDescription.XML_SCHEMA,
							getGrammarSource());
					if (fragmentMode) {
						grammar = ((SchemaGrammar) preparser.preparseGrammar(
								XMLGrammarDescription.XML_SCHEMA,
								getGrammarSource(ROOT_SCHEMA))).toXSModel(sg);
					} else {
						grammar = sg[0].toXSModel();
					}
				} catch (Exception e) {
					throw new RuntimeException("Failed to process the schema",
							e);
				}

				if (debug) {
					Grammar[] grammars = grammarPool
							.retrieveInitialGrammarSet(XMLGrammarDescription.XML_SCHEMA);
					int k = 0;
					for (Grammar g : grammars) {
						System.out.printf("Grammar[%d]=%s%n", k, g.getClass());
						SchemaGrammar sg = (SchemaGrammar) g;
						XSNamedMap ma = sg
								.getComponents(XSConstants.ATTRIBUTE_DECLARATION);
						if (ma != null) {
							for (int i = ma.getLength(); i >= 0; i--) {
								XSObject o = ma.item(i);
								if (o != null) {
									String n = o.getName();
									String s = o.getNamespace();
									System.out.printf("A[%d:%d]={%s}%s %s%n",
											k, i, (s == null ? "" : s),
											(n == null ? "" : n), o.getClass());
								}
							}
						}
						XSNamedMap me = sg
								.getComponents(XSConstants.ELEMENT_DECLARATION);
						if (me != null) {
							for (int i = me.getLength(); i >= 0; i--) {
								XSObject o = me.item(i);
								if (o != null) {
									String n = o.getName();
									String s = o.getNamespace();
									System.out.printf("E[%d:%d]={%s}%s %s%n",
											k, i, (s == null ? "" : s),
											(n == null ? "" : n), o.getClass());
								}
							}
						}
						k++;
					}
				}
			}

			DOMParser parser = new DOMParser(sym, grammarPool);

			try {
				// parser.setProperty(XP+"internal/grammar-pool", grammarPool);

				parser.setFeature(SF + "namespaces", true);
				if (schemaInfo != null) {
					parser.setFeature(SF + "validation", true);
					parser.setFeature(XF + "validation/schema", true);
					parser.setFeature(XF + "validation/schema-full-checking", false);
				}
				parser.setFeature(XF + "dom/defer-node-expansion", false);
				parser.setFeature(XF + "dom/include-ignorable-whitespace",
						(isIot ? !strictMode
								: preservation.contains(PreserveParam.whitespace)));
				parser.setProperty(XP + "dom/document-class-name",
						"com.sun.org.apache.xerces.internal.dom.PSVIDocumentImpl");
			} catch (Exception e) {
				System.err.println("Unable to configure the parser");
				throw e;
			}

			parser.setErrorHandler(new ErrorHandler() {
				private void print(String type, SAXParseException e) {
					System.err.println(type + ": " + e.getMessage());
				}

				public void warning(SAXParseException e) {
					if (verbose)
						System.err.printf("Parser: DW.e=%s%n", e);
				}

				public void error(SAXParseException e) {
					// print("ERROR", e);
					if (verbose)
						System.err.printf("Parser: DE.e=%s%n", e);
				}

				public void fatalError(SAXParseException e) throws SAXException {
					print("FATAL", e);
					throw e;
				}
			});

			InputSource src = new InputSource(new ByteArrayInputStream(bts));
			src.setSystemId(_testCaseParams.xmlSystemId);
			parser.parse(src);
			doc = parser.getDocument();
		}
		return XasExtUtil.domToSequence(doc, this);

		/*
		 * QnameInserter kludger = new QnameInserter(reader); FidelityFilter
		 * filter = new FidelityFilter(kludger, preservation);
		 * filter.trimWhitespace(); Transformer tf =
		 * TransformerFactory.newInstance().newTransformer(); InputSource is =
		 * new InputSource(input); is.setSystemId(_testCaseParams.xmlSystemId);
		 * Source source = new SAXSource(filter, is); DocumentBuilderFactory dbf
		 * = DocumentBuilderFactory.newInstance(); dbf.setNamespaceAware(true);
		 * DocumentBuilder db = dbf.newDocumentBuilder(); Document d =
		 * db.newDocument(); Result result = new DOMResult(d);
		 * tf.transform(source, result); return XasExtUtil.domToSequence(d);
		 */
	}

	private EventSequence convertValidatingSax(XMLReader reader,
			InputStream input, boolean isIot) throws Exception {
		PrefixConverter superKludger = new PrefixConverter(reader);
		QnameInserter kludger = new QnameInserter(superKludger);
		FidelityFilter filter = new FidelityFilter(kludger, preservation, isIot);
		EventSerializer ser = new EventSerializer();
		SaxWriter writer = new SaxWriter(ser);
		SchemaFactory scf = getSchemaFactory();
		Schema schema = scf.newSchema(getSchemaSource());
		Validator validator = schema.newValidator();
		ErrorHandler errorHandler = new ErrorHandler() {
			private void print(String type, SAXParseException ex) {
				System.err.println(type + ": " + ex.getMessage());
				ex.printStackTrace();
			}

			public void warning(SAXParseException ex) {
				print("WARNING", ex);
			}

			public void error(SAXParseException ex) {
				print("ERROR", ex);
			}

			public void fatalError(SAXParseException ex) {
				print("FATAL", ex);
			}
		};
		// validator.setErrorHandler(errorHandler);
		InputSource is = new InputSource(input);
		is.setSystemId(_testCaseParams.xmlSystemId);
		Source source = new SAXSource(filter, is);
		SAXResult result = new SAXResult(writer);
		result.setLexicalHandler(writer);
		validator.validate(source, result);
		return ser.getCurrentSequence();
	}

	private EventSequence convert(XMLReader reader, InputStream input,
			boolean isIot, boolean isXml) throws Exception {
		PrefixConverter superKludger = new PrefixConverter(reader);
		QnameInserter kludger = new QnameInserter(superKludger);
		FidelityFilter filter = new FidelityFilter(kludger, preservation,
				isIot, true);
		EventSerializer ser = new EventSerializer();
		SaxWriter writer = new SaxWriter(ser);
		if (fragmentMode && isXml) {
			input = new FragmentsInputStream(input);
			FragmentsSAXHandler handler = new FragmentsSAXHandler(writer);
			filter.setContentHandler(handler);
			filter.setDTDHandler(handler);
			filter.setLexicalHandler(handler);
			filter.setDeclHandler(handler);
		} else {
			filter.setContentHandler(writer);
			filter.setDTDHandler(writer);
			filter.setLexicalHandler(writer);
			filter.setDeclHandler(writer);
		}
		InputSource is = new InputSource(input);
		is.setSystemId(_testCaseParams.xmlSystemId);
		filter.parse(is);
		return ser.getCurrentSequence();
	}

	protected class StreamSaver extends Thread {
		InputStream input;
		String msg;

		StreamSaver(InputStream is) {
			this.input = is;
			this.msg = "";
		}

		public void run() {
			try {
				InputStreamReader reader = new InputStreamReader(input);
				BufferedReader br = new BufferedReader(reader);
				this.msg = "";
				String line = "";
				while ((line = br.readLine()) != null) {
					msg += line;
				}

			} catch (IOException ioe) {
				msg = ioe.toString();
			}
		}
	}

	private static class DTDGrabber implements LexicalHandler {
		public String name, publicId, systemId;

		DTDGrabber() {
			name = null;
			publicId = null;
			systemId = null;
		}

		public void comment(char[] ch, int start, int length) {
		}

		public void endCDATA() {
		}

		public void endDTD() {
		}

		public void endEntity(String name) {
		}

		public void startCDATA() {
		}

		public void startDTD(String name, String publicId, String systemId) {
			this.name = name;
			this.publicId = publicId;
			this.systemId = systemId;
		}

		public void startEntity(String name) {
		}
	}

	private String grabDTD(XMLReader reader, InputStream inputStream)
			throws Exception {
		LexicalHandler origLexicalHandler = (LexicalHandler) reader
				.getProperty("http://xml.org/sax/properties/lexical-handler");
		try {
			DTDGrabber lexicalHandler = new DTDGrabber();
			reader.setProperty("http://xml.org/sax/properties/lexical-handler",
					lexicalHandler);
			InputSource is = new InputSource(inputStream);
			is.setSystemId(_testCaseParams.xmlSystemId);
			reader.setContentHandler(new DefaultHandler());
			reader.parse(is);
			String dtd = null;
			if (lexicalHandler.name != null
					&& lexicalHandler.name.length() != 0) {
				dtd = "<!DOCTYPE " + lexicalHandler.name;
				String systemId = lexicalHandler.systemId;
				if (systemId != null && systemId.length() != 0) {
					dtd += " ";
					String publicId = lexicalHandler.publicId;
					if (publicId != null && publicId.length() != 0) {
						dtd += publicId + " ";
					}
					dtd += systemId;
				}
				dtd += ">\n";
			}
			return dtd;
		} finally {
			reader.setProperty("http://xml.org/sax/properties/lexical-handler",
					origLexicalHandler);
		}
	}

	/**
	 * Find the JAXP factory class specified by <code>className</code> and store
	 * it at <code>factoryId</code>.
	 * 
	 * @param className
	 *            JAXP factory class to find
	 * @param factoryId
	 *            Index of the internal holder location
	 * @throws ClassNotFoundException
	 */
	private static void setFactoryClassByName(String[] className, int factoryId)
			throws ClassNotFoundException {
		factoryClass[factoryId] = Class.forName(className[factoryId]);
	}

	/**
	 * Get the JDK version specific <code>SchemaFactory</code> instance
	 * 
	 * @return JDK version specific <code>SchemaFactory</code> instance
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static SchemaFactory getSchemaFactory()
			throws InstantiationException, IllegalAccessException {
		final String target = "SchemaFactory: ";
		Object factory;
		try {
			factory = factoryClass[SCHEMA_FACTORY].newInstance();
		} catch (InstantiationException ie) {
			throw new InstantiationException(target + ie);
		} catch (IllegalAccessException ia) {
			throw new IllegalAccessException(target + ia);
		}
		if (factory == null || !(factory instanceof SchemaFactory)) {
			throw new InstantiationException(target + "unexpected class: "
					+ factory.getClass());
		}
		return (SchemaFactory) factory;
	}

	/**
	 * Get the JDK version specific <code>SAXParserFactory</code> instance
	 * 
	 * @return JDK version specific <code>SAXParserFactory</code> instance
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static SAXParserFactory getSAXParserFactory()
			throws InstantiationException, IllegalAccessException {
		final String target = "SAXParserFactory: ";
		Object factory;
		try {
			factory = factoryClass[SAX_PARSER_FACTORY].newInstance();
		} catch (InstantiationException ie) {
			throw new InstantiationException(target + ie);
		} catch (IllegalAccessException ia) {
			throw new IllegalAccessException(target + ia);
		}
		if (factory == null || !(factory instanceof SAXParserFactory)) {
			throw new InstantiationException(target + "unexpected class "
					+ factory.getClass());
		}
		return (SAXParserFactory) factory;
	}

	/**
	 * Get the JDK version specific <code>DocumentBuilderFactory</code> instance
	 * 
	 * @return JDK version specific <code>DocumentBuilderFactory</code> instance
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static DocumentBuilderFactory getDocumentBuilderFactory()
			throws InstantiationException, IllegalAccessException {
		final String target = "DocumentBuilderFactory: ";
		Object factory;
		try {
			factory = factoryClass[DOCUMENT_BUILDER_FACTORY].newInstance();
		} catch (InstantiationException ie) {
			throw new InstantiationException(target + ie);
		} catch (IllegalAccessException ia) {
			throw new IllegalAccessException(target + ia);
		}
		if (factory == null || !(factory instanceof DocumentBuilderFactory)) {
			throw new InstantiationException(target + "unexpected class "
					+ factory.getClass());
		}
		return (DocumentBuilderFactory) factory;
	}

	/**
	 * Schema prolog
	 */
	private static final String SCHEMA_PROLOG = "<schema xmlns='http://www.w3.org/2001/XMLSchema'>";

	/**
	 * Schema epilog
	 */
	private static final String SCHEMA_EPILOG = "</schema>";

	/**
	 * Extra schema for Fragment mode
	 */
	private static final String ROOT_SCHEMA = SCHEMA_PROLOG
			+ "<element name='root'/>" + SCHEMA_EPILOG;

	/**
	 * Schema when schemaID is empty
	 */
	private static final String NULL_SCHEMA = SCHEMA_PROLOG + SCHEMA_EPILOG;

	/**
	 * Get a source for the schema
	 * 
	 * @return a {@code StreamSource} for the schema
	 */
	private StreamSource getSchemaSource() {
		return getSchemaSource(getGrammarSource());
	}

	/**
	 * Get a source for the string
	 * 
	 * @param src
	 *            an input string
	 * @return a source
	 */
	private StreamSource getSchemaSource(String src) {
		return getSchemaSource(getGrammarSource(src));

	}

	/**
	 * Get a source for the input
	 * 
	 * @param is
	 *            an input source
	 * @return a source
	 */
	private StreamSource getSchemaSource(XMLInputSource is) {
		return new StreamSource(is.getCharacterStream(), is.getSystemId());
	}

	/**
	 * Get the schema input source
	 * 
	 * @return an input source for the schema
	 */
	private XMLInputSource getGrammarSource() {
		final String schema = schemaInfo;
		if (schema == null) {
			throw new DiffException(
					"FATAL ERROR: Try to get a schema without schema information");
		}
		if (schema.length() == 0) {
			return getGrammarSource(NULL_SCHEMA);
		}
		return getGrammarSource(new File(schema));

	}

	/**
	 * Get an input source for a string
	 * 
	 * @param src
	 *            an input string
	 * @return an XMLInputSource
	 */
	private XMLInputSource getGrammarSource(String src) {
		return new XMLInputSource("", "", "", new StringReader(src), "UTF-8");
	}

	/**
	 * Get an input source for a file
	 * 
	 * @param src
	 *            an input fine
	 * @return an input source
	 */
	private XMLInputSource getGrammarSource(File src) {
		try {
			final String systemId = src.toURI().toURL().toString();
			return new XMLInputSource("", systemId, "",
					new FileInputStream(src), "UTF-8");
		} catch (Exception e) {
			throw new RuntimeException("Failed to get schema file", e);
		}
	}

	/**
	 * Returns an encoding name for the input
	 * 
	 * @param input
	 *            a byte sequence
	 * @return a detected encoding name
	 */
	private String findInputEncoding(byte[] input) {
		XMLInputSource source = new XMLInputSource("", "", "");
		source.setByteStream(new ByteArrayInputStream(input));
		return findInputEncoding(source);
	}

	/**
	 * Returns an encoding name for the input
	 * 
	 * @param source
	 *            an input source
	 * @return a detected encoding name
	 */
	private String findInputEncoding(XMLInputSource source) {
		XMLEncodingDetector detector = new XMLEncodingDetector();
		try {
			detector.parse(source);
		} catch (XNIException e) {
			return detector.getDetectedEncoding();
		} catch (IOException e) {
			throw new RuntimeException(
					"Unexpeced internal error in findEncoding()", e);
		}
		return null;
	}

	/**
	 * A class to detected input encoding
	 */
	private static class XMLEncodingDetector extends XMLDocumentParser {
		private String actual = "unknown";
		private String declared = "none";

		// keep a detected encoding name
		@Override
		public void startDocument(XMLLocator locator, String encoding,
				NamespaceContext namespaceContext, Augmentations augs)
				throws XNIException {
			this.actual = encoding;
			this.declared = "none"; // reset
		}

		// this method is not called if there's no XML declaration
		@Override
		public void xmlDecl(String version, String encoding, String standalone,
				Augmentations augs) throws XNIException {
			this.declared = encoding;
		}

		// stop parsing
		@Override
		public void startElement(QName element, XMLAttributes attributes,
				Augmentations augs) throws XNIException {
			throw new XNIException("Intentional early termination");
		}

		/**
		 * Returns the detected input encoding name
		 * 
		 * @return a detected input encoding name
		 */
		public String getDetectedEncoding() {
			if (actual.equals(declared) || !declared.equals("none")) {
				return declared;
			}
			if (actual.equals("unknown")) {
				return "UTF-8";
			}
			return actual;
		}
	}

	/**
	 * XML Namaspace URI
	 */
	public static final String URI_XML = "http://www.w3.org/XML/1998/namespace";
	/**
	 * XML Schema URI
	 */
	public static final String URI_XSD = "http://www.w3.org/2001/XMLSchema";
	/**
	 * XML Schema Instance URI
	 */
	public static final String URI_XSI = "http://www.w3.org/2001/XMLSchema-instance";
	/**
	 * WSDL URI
	 */
	public static final String URI_WSDL = "http://schemas.xmlsoap.org/wsdl/";

	/**
	 * Local name "type" for xsi:type
	 */
	public static final String XSI_TYPE = "type";
	/**
	 * Local name "nil" for xsi:nil
	 */
	public static final String XSI_NIL = "nil";
	/**
	 * Local name "schemaLocation" for xsi:schemaLocation
	 */
	public static final String XSI_SCHEMA_LOCATION = "schemaLocation";
	/**
	 * Local name "noNamespaceSchemaLocation" for xsi:noNamespaceSchemaLocation
	 */
	public static final String XSI_NO_NAMESPACE_SCHEMA_LOCATION = "npNamespaceSchemaLocation";

	/**
	 * QName type
	 */
	private static final XSSimpleType VALUE_TYPE_QNAME
			= typeFactory.getBuiltInType(SchemaSymbols.ATTVAL_QNAME);
	/**
	 * boolean type
	 */
	private static final XSSimpleType VALUE_TYPE_BOOLEAN
			= typeFactory.getBuiltInType(SchemaSymbols.ATTVAL_BOOLEAN);
	/**
	 * anyURI type
	 */
	private static final XSSimpleType VALUE_TYPE_ANYURI
			= typeFactory.getBuiltInType(SchemaSymbols.ATTVAL_ANYURI);
	/**
	 * anySimpleType type
	 */
	private static final XSSimpleType VALUE_TYPE_ANY_SIMPLE_TYPE
			= typeFactory.getBuiltInType(SchemaSymbols.ATTVAL_ANYSIMPLETYPE);
	
	/**
	 * Initialize EXI datatypes
	 */
	static {
		SymbolHash entries = typeFactory.getBuiltInTypes();
		int size = entries.getLength();
		Object types[] = new Object[size];
		Map<Integer,XSSimpleType> decls = new HashMap<Integer,XSSimpleType>(size);
		entries.getValues(types, 0);
		for (int i = 0; i < size; i++) {
			XSSimpleType type = (XSSimpleType)types[i];
			int kind = type.getBuiltInKind();
			decls.put(kind, type);
			switch (kind) {
			
			}
		}
	}

	/**
	 * Retrieve the type definition of the attribute node
	 * 
	 * @param attr
	 *            an attribute node to examine
	 * @return type of the attribute node
	 */
	public XSSimpleTypeDefinition getAttrValueType(Attr attr) {
		if (isXsiType(attr)) {
			return VALUE_TYPE_QNAME;
		}
		return (XSSimpleTypeDefinition) getValueType((Node) attr);
	}

	/**
	 * Retrieve the type definition of the node
	 * 
	 * @param node
	 *            a node to examine
	 * @return type of the node
	 */
	public XSTypeDefinition getValueType(Node node) {
		if (_preserve_values || schemaInfo == null) {
			return null;
		}
		final boolean isAttr = (node.getNodeType() == Node.ATTRIBUTE_NODE);

		XSTypeDefinition type = ((node instanceof ItemPSVI) ? ((ItemPSVI) node)
				.getTypeDefinition() : getTypeDefinition(node, isAttr));

		if (_debug_typed_item) {
			beginDebug(true);
			System.err.print("Typed.Item: ");
			System.err.print(isAttr ? "A(" : "E(");
			System.err.print(node.getNodeName());
		}
		if (type != null) {
			if (_debug_typed_item) {
				System.err.print(") [schema] -> ");
				if (type == null) {
					System.err.println("null");
				} else if (!type.getAnonymous()) {
					System.err.println(type);
				} else {
					System.err.println(getFacetName(type) + ","
							+ getBaseTypeName(type));
				}
			}
			return type;
		}

		/*
		 * Check attribute specific type
		 */
		if (isAttr) {
			String ns = node.getNamespaceURI();
			String ln = node.getLocalName();
			/*
			 * Check if XSI attributes
			 */
			if (URI_XSI.equals(ns)) {
				if (XSI_TYPE.equals(ln)) {
					if (_debug_typed_item) {
						System.err.print(") [xsi:type] -> ");
						System.err.println(VALUE_TYPE_QNAME);
					}
					return VALUE_TYPE_QNAME;
				} else if (XSI_NIL.equals(ln)) {
					if (_debug_typed_item) {
						System.err.print(") [xsi:nil] -> ");
						System.err.println(VALUE_TYPE_BOOLEAN);
					}
					return VALUE_TYPE_BOOLEAN;
				} else if (XSI_SCHEMA_LOCATION.equals(ln)
						|| XSI_NO_NAMESPACE_SCHEMA_LOCATION.equals(ln)) {
					if (_debug_typed_item) {
						System.err.print(") [xsi:");
						System.err.print(ln);
						System.err.print("] -> ");
						System.err.println(VALUE_TYPE_ANYURI);
					}
					return VALUE_TYPE_ANYURI;
				}
			}
			/*
			 * Schema Deviation
			 */
			if (!strictMode && grammar != null) {
				XSAttributeDeclaration decl = grammar.getAttributeDeclaration(
						ln, ns);
				if (decl != null) {
					// Use type of the corresponding global attribute
					if (_debug_typed_item) {
						System.err.print(") [deviation] -> ");
						System.err.println(decl.getTypeDefinition());
					}
					return decl.getTypeDefinition();
				}
			}
		}
		/*
		 * No type found
		 */
		if (_debug_typed_item) {
			System.err.print(") -> ");
			System.err.println("NONE");
		}
		return null;
	}

	/**
	 * Check if the attribute <code>attr</code> of the element node
	 * <code>elem</code> can be ignored.
	 * 
	 * @param attr
	 *            The attribute to examine
	 * @param elem
	 *            The element node which has the attribute <code>attr</code>
	 * @return True if ignorable; false otherwise
	 */
	public boolean ignorableAttribute(Attr attr, Element elem) {
		if (!_extra_effort || schemaInfo == null) {
			return false;
		}
		String value = attr.getValue();
		if (value == null || value.length() == 0) {
			return false;
		}

		String ln = attr.getLocalName();
		String ns = attr.getNamespaceURI();

		if (URI_XSI.equals(ns)) {
			if (XSI_NIL.equals(ln)) {
				// check if xsi:nil="false"
				return ignorableAttrNil(value, elem, attr);
			} else if (XSI_TYPE.equals(ln)) {
				// check if xsi:type="cast" is a cast to the default type
				return ignorableAttrType(elem, attr);
			}
			return false;
		}
		// check if attr is a default value
		return ignorableAttrValue(value, attr);
	}

	/**
	 * Check if xsi:nil="false" for the nillable element
	 * 
	 * @param value
	 *            value of xsi:nil
	 * @param elem
	 *            element which has xsi:nil
	 * @return true if xsi:nil="false" for the nillable element; otherwise false
	 */
	private boolean ignorableAttrNil(String nil, Element elem, Attr attr) {
		String value = nil.trim();
		if (value.equals("false")) {
			value = "0";
		}
		if ("0".equals(value) && elem instanceof ElementPSVI) {
			XSElementDeclaration decl = ((ElementPSVI) elem)
					.getElementDeclaration();
			if (decl != null && decl.getNillable()) {
				warnIgnoredAttr(attr, "xsi:nil=false");
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if xsi:type="cast" is a cast of the defined type
	 * 
	 * @param elem
	 *            Element to examine
	 * @return true if "cast" is the default type
	 */
	private boolean ignorableAttrType(Element elem, Attr attr) {
		if (attr instanceof AttributePSVI) {
			AttributePSVI at = (AttributePSVI) attr;
			Object value = at.getActualNormalizedValue();
			if (value == null) {
				if (_debug_full) {
					System.err.println("Ignored.Attr: Cast is a null");
				}
				return false;
			}
			if (at.getActualNormalizedValueType() != XSConstants.QNAME_DT) {
				if (_debug_full) {
					System.err.println("Ignored.Attr: Cast is not a QName");
					System.err.print("Ignored.Attr: Cast to '");
					System.err.print(attr.getNodeValue());
					System.err.print("' ");
					System.err.println(value.getClass());
					System.err.print("Ignored.Attr: Cast value type ");
					System.err.println(at.getActualNormalizedValueType());
				}
				return false;
			}
			QName qn = (QName) value;
			if (_debug_full) {
				System.err.print("Ignored.Attr: Cast to {");
				System.err.print(qn.uri);
				System.err.print('}');
				System.err.println(qn.localpart);
			}
			if (elem instanceof ElementPSVI) {
				ElementPSVI psvi = (ElementPSVI) elem;
				XSElementDeclaration decl = psvi.getElementDeclaration();
				if (decl != null) {
					XSTypeDefinition type = decl.getTypeDefinition();
					if (type != null) {
						if (_debug_full) {
							System.err.print("Ignored.Attr: Type=");
							System.err.println(type);
						}
						if (qn.localpart.equals(type.getName())
								&& qn.uri.equals(type.getNamespace())) {
							warnIgnoredAttr(attr, "xsi:type=cast");
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Check if the attribute has a default value and its value is the default
	 * 
	 * @param value
	 *            value of the attribute
	 * @param attr
	 *            attribute to examine
	 * @return
	 */
	private boolean ignorableAttrValue(Object value, Attr attr) {
		if (grammar == null) {
			return false;
		}
		if (attr instanceof AttributePSVI) {
			AttributePSVI psvi = (AttributePSVI) attr;
			XSAttributeDeclaration decl = psvi.getAttributeDeclaration();
			if (decl == null) {
				if (strictMode) {
					return false;
				}
				// schema deviation
				decl = grammar.getAttributeDeclaration(attr.getLocalName(),
						attr.getNamespaceURI());
				if (decl != null
						&& decl.getConstraintType() == XSConstants.VC_DEFAULT) {
					if (value.equals(decl.getActualVC())) {
						warnIgnoredAttr(attr, "default=exact");
						return true;
					}
					if (value.toString().equals(decl.getConstraintValue())) {
						warnIgnoredAttr(attr, "default=equiv");
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * A convenient function: reports the attribute being ignored
	 * @param attr an attribute being ignored
	 * @param reason a brief explanation of why ignored
	 */
	private void warnIgnoredAttr(Attr attr, String reason) {
		if (_warn_ignored) {
			beginDebug(true);
			System.err.print("Ignored.Attr: ");
			System.err.print(reason);
			System.err.print("; ");
			System.err.print(attr.getNodeName());
			System.err.print('=');
			System.err.println(attr.getValue());
		}
	}

	/**
	 * Get a content type
	 * 
	 * @param itemType
	 *            Original type
	 * @param needType
	 *            Determine if type is needed
	 * @return a content type
	 */
	public XSSimpleTypeDefinition getContentType(XSTypeDefinition itemType) {
		if (_preserve_values || itemType == null || schemaInfo == null) {
			return null;
		}
		if (itemType instanceof XSSimpleTypeDefinition) {
			return getSimpleType((XSSimpleTypeDefinition) itemType);
		} else if (itemType instanceof XSComplexTypeDefinition) {
			XSComplexTypeDefinition complexType = (XSComplexTypeDefinition) itemType;
			switch (complexType.getContentType()) {
			case XSComplexTypeDefinition.CONTENTTYPE_EMPTY:
				return VALUE_TYPE_ANY_SIMPLE_TYPE;
			case XSComplexTypeDefinition.CONTENTTYPE_SIMPLE:
				return getSimpleType(complexType.getSimpleType());
			case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT:
				return VALUE_TYPE_ANY_SIMPLE_TYPE;
			case XSComplexTypeDefinition.CONTENTTYPE_MIXED:
				return VALUE_TYPE_ANY_SIMPLE_TYPE;
			}
			throw new RuntimeException("getContentType: should not happen ... "
					+ complexType);
		}
		if (_warn_unexpected) {
			beginDebug(true);
			System.err.println("getContentType: unexpected type");
			System.err.println(itemType.getClass());
			System.err.print("value ");
			System.err.println(itemType.toString());
		}
		return null;
	}

	/**
	 * A convenient function: Gets a SimpleType
	 * @param type a type to examine
	 * @return a SimpleType of {@code null}
	 */
	private XSSimpleTypeDefinition getSimpleType(XSSimpleTypeDefinition type) {
		return (isDerivedFromQName(type) ? null : type);
	}

	/**
	 * Retrieve a value type definition out of Xerces implementation.
	 * 
	 * @param node
	 *            Node to retrieve its value type
	 * @return Value type of the node or null if not found
	 */
	private static XSTypeDefinition getTypeDefinition(Node node, boolean isAttr) {
		Object type = null;
		try {
			type = (isAttr ? attrTypeField.get(node) : elemTypeField.get(node));
		} catch (Exception ex) {
			throw new RuntimeException("Failed to get the type field for the "
					+ (isAttr ? "A(" : "E(") + node.getNodeName() + ")", ex);
		}
		if (type == null || type instanceof XSTypeDefinition) {
			return (XSTypeDefinition) type;
		}
		if (_warn_unexpected) {
			throw new DiffException("ValueTypeException: "
					+ "Unexpected class for the value type of the "
					+ (isAttr ? "A(" : "E(") + node.getNodeName() + "): "
					+ type.getClass());
		}
		return null;
	}

	/**
	 * Requents to merge contents if {@code true}
	 */
	private static boolean mergeContent = false;

	/**
	 * {@inheritDoc}
	 */
	public boolean mergeContent(boolean children) {
		if (children) {
			return mergeContent;
		}
		if (mergeContent && _warn_unexpected) {
			beginDebug(true);
			throw new DiffException(
					"INTERNAL ERROR: met unmerged TEXT/CDATA nodes");
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean preservePrefixes() {
		return _preserve_prefixes;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean keepQNamePrefixes() {
		return _qname_prefixes;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean needExtraEffort() {
		return _extra_effort;
	}

	/**
	 * {@inheritDoc}
	 */
	public String skippedEntity(Node node) {
		if ((node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE)
				&& FidelityFilter.PI_ENTITY_REFERENCE
						.equals(node.getNodeName())) {
			return node.getNodeValue();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTrimmedValue(String text) {
		return trimWhiteSpaces(text);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLexicalValue(String text, boolean collapse) {
		return normalizeWhiteSpaces(trimWhiteSpaces(text), collapse);
	}

	/**
	 * {@inheritDoc}
	 */
	private boolean isXsiType(Node node) {
		return (node.getNodeType() == Node.ATTRIBUTE_NODE
				&& XSI_TYPE.equals(node.getLocalName()) && URI_XSI.equals(node
				.getNamespaceURI()));

	}

	/**
	 * {@inheritDoc}
	 */
	public Object getQNameValue(Node node, String rawName) {
		final boolean isXsiType = isXsiType(node);
		if (_qname_prefixes && !isXsiType) {
			return rawName;
		}

		if (_debug_qname && !isXsiType) {
			beginDebug(true);
			System.err.print("QName: ");
			switch (node.getNodeType()) {
			case Node.ATTRIBUTE_NODE:
				System.err.print("A(");
				break;
			case Node.ELEMENT_NODE:
				System.err.print("E(");
				break;
			case Node.CDATA_SECTION_NODE:
			case Node.TEXT_NODE:
				System.err.print("C(");
				break;
			default:
				System.err.print("O(");
				break;
			}
			System.err.print(node.getNodeName());
			System.err.print(")=");
			System.err.println(node.getNodeValue());
		}

		final String prefix;
		int colon = rawName.indexOf(':');
		if (colon > 0 && rawName.lastIndexOf(':') == colon) {
			prefix = rawName.substring(0, colon);
		} else {
			prefix = null;
			colon = -1;
		}
		return new QNameValue(node.lookupNamespaceURI(prefix),
				rawName.substring(colon + 1), prefix, rawName,
				(isXsiType && _qname_prefixes && _preserve_prefixes));
	}

	/**
	 * A class represents a QName value
	 */
	public static class QNameValue {
		public final String uri;
		public final String name;
		public final String prefix;
		public final String qualified;
		public final boolean relax;

		private QNameValue(String uri, String name, String prefix,
				String qualified, boolean check3) {
			this.uri = uri;
			this.name = ((name == null) ? "" : name);
			this.prefix = prefix;
			this.qualified = qualified;
			this.relax = !check3;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof QNameValue) {
				QNameValue q = (QNameValue) o;
				return (equals(name, q.name)
						&& equals(uri, q.uri)
						&& (relax || equals(prefix, q.prefix)));
			}
			return false;
		}

		private boolean equals(String a, String b) {
			if (a == b) {
				return true;
			}
			if (a == null) {
				return false;
			}
			return a.equals(b);
		}

		@Override
		public String toString() {
			if (prefix != null && (relax || uri == null)) {
				if (prefix.length() > 0 || uri != null) {
					return prefix + ":" + name;
				}
			}
			if (uri != null) {
				return "{" + MapURI.getURI(uri) + "}" + name;
			}
			return name;
		}

		private int hashCode = 0;

		@Override
		public int hashCode() {
			int h = hashCode;
			if (h == 0) {
				if (name != null) {
					h = name.hashCode() << 8;
				}
				if (prefix != null) {
					h ^= prefix.hashCode();
				} else if (uri != null) {
					h ^= uri.hashCode();
				}
				h = (h << 1) + 1;
				hashCode = h;
			}
			return h;
		}
	}

	/**
	 * Get a canonical representation object
	 * 
	 * @param node
	 *            a node to examine
	 * @param type
	 *            a specified type
	 * @param text
	 *            an input
	 * @return a canonical representation object or {@code null} if {@code type}
	 *         is {@code null} or type vailidation fails
	 */
	public Object getCanonicalValue(Node node, XSTypeDefinition type,
			String text) {
		if (schemaInfo == null) {
			return null;
		}
		if (_debug_typed_item) {
			beginDebug(true);
			System.err.print("Typed.Item: Node ");
			System.err.println(node.getClass());
			System.err.print("Typed.Item: Type ");
			System.err.println(((type == null) ? "NONE" : type.getClass()));
			System.err.print("Typed.Item: raw value '");
			System.err.print(node.getNodeValue());
			System.err.println("'");
		}
		if (type instanceof XSSimpleType) {
			XSSimpleType stype = (XSSimpleType) type;
			Object value = canonicalizeValue(node, stype, text);
			if (_debug_typed_item) {
				System.err.print("Typed.Item: SympleType ");
				System.err.println(stype.toString());
				if (value == null) {
					System.err.println("Typed.Item: validation failed");
				} else {
					System.err.print("Typed.Item: value ");
					System.err.println(value.getClass());
					System.err.print("Typed.Item: value ");
					System.err.println(value.toString());
				}
			}
			if (value != null) {
				return value;
			}
		}
		if (_debug_qname) {
			beginDebug(true);
			System.err.print("Value: ");
			switch (node.getNodeType()) {
			case Node.ATTRIBUTE_NODE:
				System.err.print("A(");
				break;
			case Node.ELEMENT_NODE:
				System.err.print("E(");
				break;
			default:
				System.err.print("O(");
				break;
			}
			System.err.print(node.getNodeName());
			System.err.print(")=");
			System.err.println(node.getNodeValue());
		}
		return null;
	}

	/**
	 * An implementation of ValidationContext
	 */
	private static class TypedContext implements ValidationContext {
		private static final String URI_PREFIX = "http://exi.w3c.org/ttf/prefix/";
		private static final String UNKNOWN_URI = URI_PREFIX + "unknown";
		private static final String INVALID_URI = URI_PREFIX + "invalid";
		private final Node node;

		private TypedContext(Node node) {
			super();
			this.node = node;
		}

		public void addId(String name) {
		}

		public void addIdRef(String name) {
		}

		public String getSymbol(String symbol) {
			return symbol;
		}

		public String getURI(String prefix) {
			if (node == null) {
				if (_debug_full) {
					System.err.println("TypedContext: null node for " + prefix);
				}
				return UNKNOWN_URI;
			}
			String uri = node.lookupNamespaceURI(prefix);
			return ((uri != null) ? uri : INVALID_URI);
		}

		public boolean isEntityDeclared(String name) {
			return false;
		}

		public boolean isEntityUnparsed(String name) {
			return false;
		}

		public boolean isIdDeclared(String name) {
			return false;
		}

		public boolean needExtraChecking() {
			return true;
		}

		public boolean needFacetChecking() {
			return true;
		}

		public boolean needToNormalize() {
			return true;
		}

		public boolean useNamespaces() {
			return true;
		}
		
		public Locale getLocale() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
	};

	/**
	 * Gets a validated value
	 * @param node a node to examine
	 * @param type a type of the value
	 * @param text a text to validate
	 * @return a validated value or {@code null} if validation failed
	 */
	private Object validateValue(Node node, XSSimpleType type, String text) {
		Object value = null;
		try {
			value = type.validate(text, new TypedContext(node), null);
		} catch (InvalidDatatypeValueException e) {
			if (_debug_canonicalize) {
				System.err.println("Typed.Item: " + e.getMessage());
			}
		}
		return value;
	}

	/**
	 * Gets a canonicalized value
	 * @param node a node to examine
	 * @param type a type of the value
	 * @param text a text to validate
	 * @return a validated value or {@code null) if validation failed
	 */
	private Object canonicalizeValue(Node node, XSSimpleType type, String text) {
		Object value = validateValue(node, type, text);
		switch (type.getVariety()) {
		case XSSimpleType.VARIETY_ATOMIC:
			value = canonicalizeAtomicValue(node, type, text);
			break;
		case XSSimpleType.VARIETY_LIST:
			value = canonicalizeListValue(node, type, text);
			break;
		case XSSimpleType.VARIETY_UNION:
			value = canonicalizeUnionValue(node, type, text);
			break;
		}
		return value;
	}

	/**
	 * Check if the type is derived from QName
	 * @param type a type to examine
	 * @return {@code true} if the type is derived from QName; otherwise {@code false}
	 */
	private boolean isDerivedFromQName(XSTypeDefinition type) {
		return type.derivedFromType(VALUE_TYPE_QNAME,
				XSConstants.DERIVATION_NONE);
	}

	/**
	 * Gets a canonical value for an atomic type
	 * @param node a node to examine
	 * @param type a type of the value
	 * @param text a value to canonicalized
	 * @return a canonicalized value or {@code null} if validation failed
	 */
	private Object canonicalizeAtomicValue(Node node, XSSimpleType type,
			String text) {
		if (_debug_canonicalize) {
			System.err.print("Typed.Item[ATOMIC]: ");
			System.err.println(type.toString());
			System.err.print("Typed.Item[ATOMIC]: text ");
			System.err.println(text);
		}
		Object value = null;
		if (isDerivedFromQName(type)) {
			value = getQNameValue(node, text);
		} else {
			value = validateValue(node, type, text);
		}
		return value;
	}

	/**
	 * Gets a canonical value for a list type
	 * @param node a node to examine
	 * @param type a type of the value
	 * @param text a value to canonicalized
	 * @return a canonicalized value or {@code null} if validation failed
	 */
	private Object canonicalizeListValue(Node node, XSSimpleType type,
			String text) {
		if (_debug_canonicalize) {
			System.err.print("Typed.Item[LIST]: ");
			System.err.println(type.toString());
			System.err.print("Typed.Item[LIST]: text ");
			System.err.println(text);
		}
		XSSimpleType itemType = (XSSimpleType) type.getItemType();
		StringTokenizer st = new StringTokenizer(text, " \t\r\n");
		LinkedList<Object> list = new LinkedList<Object>();
		while (st.hasMoreTokens()) {
			String item = st.nextToken();
			Object value = canonicalizeValue(node, itemType, item);
			if (value == null) {
				return null;
			}
			list.add(value);
		}
		return new ListValue(list);
	}

	/**
	 * Gets a canonical value for a list type
	 * @param node a node to examine
	 * @param type a type of the value
	 * @param text a value to canonicalized
	 * @return a canonicalized value or {@code null} if validation failed
	 */
	private Object canonicalizeUnionValue(Node node, XSSimpleType type,
			String text) {
		if (_debug_canonicalize) {
			System.err.print("Typed.Item[LIST]: ");
			System.err.println(type.toString());
			System.err.print("Typed.Item[LIST]: text ");
			System.err.println(text);
		}
		XSObjectList typeList = type.getMemberTypes();
		int size = typeList.getLength();
		Object value = null;
		for (int i = 0; i < size; i++) {
			XSSimpleType memberType = (XSSimpleType) typeList.item(i);
			value = validateValue(node, memberType, text);
			if (value != null) {
				if (_extra_effort) {
					return ((size > 1) ? typedValue(memberType, value) : value);
				}
				return text;
			}
		}
		return null;
	}

	/**
	 * A class represents a value of a list type
	 */
	public static class ListValue {
		/**
		 * Values of the list
		 */
		private final List<Object> values;
		/**
		 * Size of the list
		 */
		private final int size;

		/**
		 * String representation
		 */
		private String string;
		/**
		 * Hash code
		 */
		private int hashCode;

		/**
		 * Constructor
		 * 
		 * @param list
		 *            an array which represents values of the list
		 */
		public ListValue(List<Object> list) {
			this.values = list;
			this.string = null;
			this.size = list.size();
		}

		public List<Object> getValues() {
			return this.values;
		}

		@Override
		public String toString() {
			if (string == null) {
				int size = values.size();
				if (size == 0) {
					string = "[]";
				} else {
					List<Object> v = values;
					StringBuilder sb = new StringBuilder(size * 16);
					sb.append('[');
					sb.append(toString(v.get(0)));
					ListIterator<Object> i = v.listIterator(1);
					while (i.hasNext()) {
						sb.append(' ');
						sb.append(toString(i.next()));
					}
					sb.append(']');
					string = sb.toString();
				}
			}
			return string;
		}

		private String toString(Object o) {
			return ((o == null) ? "<null>" : o.toString());
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof ListValue) {
				List<Object> v1 = this.values;
				List<Object> v2 = ((ListValue) o).values;
				int size = this.size;
				if (size != v2.size()) {
					return false;
				}
				ListIterator<Object> i1 = v1.listIterator();
				ListIterator<Object> i2 = v2.listIterator();
				while (size-- > 0) {
					Object o1 = i1.next();
					Object o2 = i2.next();
					if (o1 == null) {
						if (o2 == null) {
							continue;
						}
					} else if (o1.equals(o2)) {
						continue;
					}
					return false;
				}
				return true;
			}
			return false;
		}

		@Override
		public int hashCode() {
			if (string == null) {
				toString();
				hashCode = string.hashCode();
			}
			return hashCode;
		}
	}

	/**
	 * A convenient method: Gets a base type name of the given type
	 * @param type a type to examine
	 * @return a base type name
	 */
	private static String getBaseTypeName(XSTypeDefinition type) {
		XSTypeDefinition base = type.getBaseType();
		return ((base == null) ? "none" : base.getName());
	}

	/**
	 * A covenient method: Gets a facet name of the given type
	 * @param type a type to examine
	 * @return a facet name
	 */
	private static String getFacetName(XSTypeDefinition type) {
		if (type instanceof XSSimpleType) {
			short flag = ((XSSimpleType) type).getDefinedFacets();
			if ((flag & XSSimpleType.FACET_ENUMERATION) != 0) {
				return "enum";
			} else if ((flag & XSSimpleType.FACET_PATTERN) != 0) {
				return "pattern";
			}
		}
		return "anon";
	}

	/**
	 * A wrapper method: Gets a typed value
	 * @param type a type of the value
	 * @param value a value
	 * @return a typed value
	 */
	private Object typedValue(XSSimpleType type, Object value) {
		if (value != null && _add_value_type) {
			return new TypedValue(type, value);
		}
		return value;
	}

	/**
	 * A class represents a typed value
	 * Main purpose of this class is to provide a type name of the value
	 */
	private static class TypedValue {
		final String uri;
		final String name;
		final Object value;
		int hashCode;

		public TypedValue(XSSimpleType type, Object value) {
			String name = type.getName();
			String uri;
			if (name != null) {
				uri = type.getNamespace();
			} else {
				XSTypeDefinition base = type.getBaseType();
				uri = getFacetName(base);
				name = base.getName();
			}
			this.uri = (URI_XSD.equals(uri) ? null : uri);
			this.name = name;
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (o instanceof TypedValue) {
				TypedValue v = (TypedValue) o;
				return (equals(value, v.value) && equals(name, v.name) && equals(
						uri, v.uri));
			}
			return false;
		}

		private static boolean equals(Object a, Object b) {
			if (a == b) {
				return true;
			}
			return (a != null && a.equals(b));
		}

		@Override
		public int hashCode() {
			int h = hashCode;
			if (h == 0) {
				h += hashCode(value, 21);
				h += hashCode(name, 11);
				h += hashCode(uri, 1);
				h += 1;
			}
			return h;

		}

		private static int hashCode(Object o, int shift) {
			return ((o == null) ? 0 : (o.hashCode() << shift));
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(100);
			if (this.name != null) {
				sb.append('(');
				if (this.uri != null) {
					sb.append('{');
					sb.append(MapURI.getURI(this.uri));
					sb.append('}');
				}
				sb.append(this.name);
				sb.append(')');
			}
			sb.append((value == null) ? "<null>" : value);
			return sb.toString();
		}
	}
	

	/**
	 * {@inheritDoc}
	 */
	public boolean isTypedValue(Object value) {
		return (value instanceof TypedValue);
	}

	/**
	 * White spaces defined in XML specification
	 */
	private static final String WHITE_SPACES = " \t\r\n";

	/**
	 * Gets a trimmed string by removing beginning and ending white spaces
	 * @param text a string to trim
	 * @return a trimmed string
	 */
	private static String trimWhiteSpaces(String text) {
		final int len = text.length();
		int head;
		int tail;
		for (head = 0; head < len; head++) {
			if (WHITE_SPACES.indexOf(text.charAt(head)) < 0) {
				break;
			}
		}
		if (head >= len) {
			return "";
		}
		for (tail = len; --tail > head;) {
			if (WHITE_SPACES.indexOf(text.charAt(tail)) < 0) {
				break;
			}
		}
		tail++;
		return ((head > 0 || tail < len) ? text.substring(head, tail) : text);
	}

	/**
	 * Gets a normalized string by converting white spaces based on the arguments
	 * @param text a string to normalize
	 * @param collapse convert a sequence of white space characters into a single
	 * space character if {@code true}; otherwise convert a white space character
	 * into a space character 
	 * @return a nomalized string
	 */
	private static String normalizeWhiteSpaces(final String text,
			boolean collapse) {
		final int len = text.length();
		char ch;
		int i;
		for (i = 0; i < len; i++) {
			ch = text.charAt(i);
			if (WHITE_SPACES.indexOf(ch) >= 0) {
				break;
			}
		}
		if (i >= len) {
			// no whitespace found.
			return text;
		}

		final StringBuilder buf = new StringBuilder(len);
		buf.append(text, 0, i);
		buf.append(' ');
		boolean skip = collapse;

		while (++i < len) {
			ch = text.charAt(i);
			if (WHITE_SPACES.indexOf(ch) < 0) {
				buf.append(ch);
				skip = false;
			} else if (!skip) {
				buf.append(' ');
				skip = collapse;
			}
		}
		return buf.toString();
	}

	/*
	 * static { System.out.println(); System.err.println("Test WhiteSpaces:");
	 * String s = "    "; System.err.printf("Null: '%s' -> '%s'%n", s,
	 * trimWhiteSpaces(s)); s = "a"; System.err.printf("Trim: '%s' -> '%s'%n",
	 * s, trimWhiteSpaces(s)); System.err.printf("Norm: '%s' -> '%s'%n", s,
	 * normalizeWhiteSpaces(s, false));
	 * System.err.printf("Comp: '%s' -> '%s'%n", s, normalizeWhiteSpaces(s,
	 * true)); s = "  a\t b \tc  "; String t = trimWhiteSpaces(s);
	 * System.err.printf("Trim: '%s' -> '%s'%n", s, t);
	 * System.err.printf("Norm: '%s' -> '%s'%n", s, normalizeWhiteSpaces(s,
	 * false)); System.err.printf("Comp: '%s' -> '%s'%n", s,
	 * normalizeWhiteSpaces(s, true)); System.err.printf("Norm: '%s' -> '%s'%n",
	 * t, normalizeWhiteSpaces(t, false));
	 * System.err.printf("Comp: '%s' -> '%s'%n", t, normalizeWhiteSpaces(t,
	 * true)); System.exit(1); }
	 */

	/**
	 * Indicates there was one or more debug messages if {@code true}
	 */
	private boolean _debugging = false;

	/**
	 * Prepare for a debug message
	 * @param newline a newline is required if {@code true}; otherwise not.
	 */
	private void beginDebug(boolean newline) {
		_debugging = true;
		if (_need_flush_output) {
			try {
				if (newline) {
					System.out.println();
				}
				System.out.flush();
				Thread.sleep(30);
			} catch (Exception _) {
				// ignore
			}
			_need_flush_output = false;
		}
	}

	/**
	 * Finish a debug message
	 * @param stdout a destination of the message was the standard output if {@code true};
	 * otherwise not.
	 */
	private void endDebug(boolean stdout) {
		if (_debugging) {
			_debugging = false;
			try {
				if (stdout) {
					System.out.flush();
				} else {
					System.err.flush();
				}
				Thread.sleep(30);
			} catch (Exception _) {
				// ignore
			}
		}
	}
}
