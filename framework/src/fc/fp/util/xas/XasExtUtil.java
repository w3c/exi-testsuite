package fc.fp.util.xas;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xmlpull.v1.XmlPullParserException;

import fc.fp.message.encoding.XebuConstants;
import fc.fp.util.Base64;
import fc.fp.util.MagicInputStream;
import fc.fp.util.Util;

import com.sun.org.apache.xerces.internal.xs.XSConstants;
import com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition;
import com.sun.org.apache.xerces.internal.impl.dv.xs.FullDVFactory;

/**
 * General utilities often needed in connection with the XAS API. This class
 * collects (as static methods) several utility methods that are typically
 * needed by applications using or extending the XAS API.
 * 
 * <p>
 * The difference between this class and the {@link XasUtil} class is that this
 * class is allowed to contain methods depending on libraries outside the MIDP
 * and CLDC specifications. This relationship parallels that between
 * {@link fc.fp.util.Util} and {@link fc.fp.util.ExtUtil}.
 */
public class XasExtUtil {

    private static final String LIST_NAMESPACE = "http://www.hiit.fi/fuego/fc/list-type";
    private static final FullDVFactory dvFactory = new FullDVFactory();

    /*
         * Private constructor to prevent instantiation.
         */
    private XasExtUtil () {
    }

    private static String searchPrefix (String namespace, Stack<EventList> nss) {
	for (int i = nss.size() - 1; i >= 0; i--) {
	    EventList el = nss.elementAt(i);
	    for (int j = 0; j < el.size(); j++) {
		Event ev = el.get(j);
		if (Util.equals(namespace, ev.getNamespace())) {
		    return (String) ev.getValue();
		}
	    }
	}
	return null;
    }

	private static Node readerToDom (Document doc, XmlReader xr, Stack<EventList> nss) {
	Node result = null;
	Event ev = xr.getCurrentEvent();
	// System.out.println("Read event " + ev);
	EventList pms = new EventList();
	switch (ev.getType()) {
	case Event.START_DOCUMENT: {
	    xr.advance();
	    Node node;
	    while ((node = readerToDom(doc, xr)) != null) {
		doc.appendChild(node);
	    }
	    result = doc;
	    break;
	}
	case Event.END_DOCUMENT:
	    xr.advance();
	    break;
	case Event.NAMESPACE_PREFIX:
	    do {
		pms.add(xr.advance());
	    } while ((ev = xr.getCurrentEvent()).getType() == Event.NAMESPACE_PREFIX);
	case Event.START_ELEMENT: {
	    nss.push(pms);
	    String ns = ev.getNamespace();
	    String name = ev.getName();
	    String prefix = searchPrefix(ns, nss);
	    if (prefix != null) {
		name = prefix + ":" + name;
	    }
	    Element elem = doc.createElementNS(ns, name);
	    for (Enumeration<?> e = pms.events(); e.hasMoreElements();) {
		ev = (Event) e.nextElement();
		elem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:"
			+ ev.getValue(), ev.getNamespace());
	    }
	    xr.advance();
	    while ((ev = xr.getCurrentEvent()).getType() == Event.ATTRIBUTE) {
		ns = ev.getNamespace();
		name = ev.getName();
		prefix = searchPrefix(ns, nss);
		if (prefix != null) {
		    name = prefix + ":" + name;
		}
		elem.setAttributeNS(ns, name, (String) ev.getValue());
		xr.advance();
	    }
	    Node node;
	    while ((node = readerToDom(doc, xr, nss)) != null) {
		elem.appendChild(node);
	    }
	    nss.pop();
	    result = elem;
	    break;
	}
	case Event.END_ELEMENT:
	    xr.advance();
	    break;
	case Event.CONTENT:
	    result = doc.createTextNode((String) ev.getValue());
	    xr.advance();
	    break;
	case Event.COMMENT:
	    result = doc.createComment((String) ev.getValue());
	    xr.advance();
	    break;
	case Event.PROCESSING_INSTRUCTION: {
	    String target = (String) ev.getValue();
	    String data = null;
	    int i = target.indexOf(' ');
	    if (i >= 0) {
		data = target.substring(i + 1);
		target = target.substring(0, i);
	    }
	    result = doc.createProcessingInstruction(target, data);
	    xr.advance();
	    break;
	}
	case Event.ENTITY_REFERENCE:
	    result = doc.createEntityReference(ev.getName());
	    xr.advance();
	    break;
	default:
	    if ((ev.getType() & Event.FLAG_BITMASK) == Event.TYPE_EXTENSION_FLAG) {
		EventSequence es = (EventSequence) ev.getValue();
		if (es != null) {
		    result = sequenceToDom(doc, es);
		}
	    } else {
		throw new IllegalArgumentException("Unhandled event type: "
			+ ev.getType());
	    }
	}
	// System.out.println("Returning " + result);
	return result;
    }

    /**
         * Convert XML from an XML reader to a DOM tree. This method takes a
         * {@link XmlReader} and returns a {@link Node} representing the same
         * XML document or a fragment of it. The XML represented by the
         * underlying event sequence of the reader needs to be well-formed and
         * balanced, and must not contain {@link Event#TYPED_CONTENT} events. A
         * DOM {@link Document} object needs to be provided to act as a factory
         * for DOM {@link Node} objects.
         * 
         * <p>
         * If the underlying event sequence of the reader represents a complete
         * XML document, the passed in DOM {@link Document} needs to be empty,
         * since the resulting document will be built into that.
         * 
         * @param doc the DOM document to use for creating DOM nodes
         * @param xr the reader from which to read the converted event sequence
         * @return the DOM node representing <code>es</code>
         * 
         * @throws IllegalArgumentException if the XML represented by
         *         <code>xr</code>'s underlying event sequence is not
         *         convertible to DOM
         */
    public static Node readerToDom (Document doc, XmlReader xr) {
	return readerToDom(doc, xr, new Stack<EventList>());
    }

    /**
         * Convert XML from an event sequence to a DOM tree. This method takes a
         * {@link EventSequence} and returns a {@link Node} representing the
         * same XML document or a fragment of it. The XML represented by the
         * event sequence needs to be well-formed and balanced, and must not
         * contain {@link Event#TYPED_CONTENT} events. A DOM {@link Document}
         * object needs to be provided to act as a factory for DOM {@link Node}
         * objects.
         * 
         * <p>
         * If the event sequence represents a complete XML document, the passed
         * in DOM {@link Document} needs to be empty, since the resulting
         * document will be built into that.
         * 
         * @param doc the DOM document to use for creating DOM nodes
         * @param es the event sequence to convert
         * @return the DOM node representing <code>es</code>
         * 
         * @throws IllegalArgumentException if the XML represented by
         *         <code>es</code> is not convertible to DOM
         */
    public static Node sequenceToDom (Document doc, EventSequence es) {
	XmlReader reader = new XmlReader(es);
	return readerToDom(doc, reader);
    }

//    private static int getTwoInt (String string, int index, char last) {
//	int i = index + 2;
//	if (last != '$') {
//	    i = string.indexOf(last, index);
//	}
//	if (i - index == 2) {
//	    return Integer.parseInt(string.substring(index, i));
//	} else {
//	    throw new NumberFormatException(string + ":" + index + ":" + last);
//	}
//    }

    private static final String[] XSD_RESTRICTION_TYPES = { "boolean", "int",
	    "integer", "normalizedString", "string", "token", "date",
	    "dateTime", "base64Binary", "long", "short", "byte", "float",
	    "double", "decimal" };

    private static final String[] INVOICE_TYPE_MAP = { "AmountType", "decimal",
	    "CodeType", "token", "DateTimeType", "dateTime", "DateType",
	    "date", "IdentifierType", "normalizedString", "NumericType",
	    "decimal", "QuantityType", "decimal" };

    private static final String INVOICE_TYPE_NAMESPACE = "urn:oasis:names:tc:ubl:CommonLeafTypes:1.0:0.65";

    private static String mapNamespace (XSTypeDefinition ti) {
	for (String type : XSD_RESTRICTION_TYPES) {
	    if (ti.derivedFromType(dvFactory.getBuiltInType(type), XSConstants.DERIVATION_RESTRICTION)) {
		return XasUtil.XSD_NAMESPACE;
	    }
	}
	if (Util.equals(ti.getNamespace(), INVOICE_TYPE_NAMESPACE)) {
	    for (int i = 0; i < INVOICE_TYPE_MAP.length; i += 2) {
		if (INVOICE_TYPE_MAP[i].equals(ti.getName())) {
		    return XasUtil.XSD_NAMESPACE;
		}
	    }
	}
	if (ti.derivedFromType(dvFactory.getBuiltInType("base64Binary"), XSConstants.DERIVATION_EXTENSION)) {
	    return XasUtil.XSD_NAMESPACE;
	} else if (ti.getNamespace() == null
		&& Util.equals(ti.getName(), "xcrdtype")) {
	    return LIST_NAMESPACE;
	} else {
	    return ti.getNamespace();
	}
    }

    private static String mapName (XSTypeDefinition ti) {
	for (String type : XSD_RESTRICTION_TYPES) {
	    if (ti.derivedFromType(dvFactory.getBuiltInType(type), XSConstants.DERIVATION_RESTRICTION)) {
		return type;
	    }
	}
	if (Util.equals(ti.getNamespace(), INVOICE_TYPE_NAMESPACE)) {
	    for (int i = 0; i < INVOICE_TYPE_MAP.length; i += 2) {
		if (INVOICE_TYPE_MAP[i].equals(ti.getName())) {
		    return INVOICE_TYPE_MAP[i + 1];
		}
	    }
	}
	if (ti.derivedFromType(dvFactory.getBuiltInType("base64Binary"), XSConstants.DERIVATION_EXTENSION)) {
	    return "base64Binary";
	} else if (ti.getNamespace() == null
		&& Util.equals(ti.getName(), "xcrdtype")) {
	    return "decimal";
	} else {
	    return ti.getName();
	}
    }

    /**
     * An interface for the typed value handler
     */
    public interface TypedValueHelper {
    	/**
    	 * Check if some more efforts for canonicalization are advised
    	 * @return an advice 
    	 */
    	public boolean needExtraEffort();
    	/**
    	 * Check if preserve prefixes during canonicalization
    	 * @return prefixes should be preserved if {@code true}; otherwise not
    	 */
    	public boolean preservePrefixes();
    	/**
    	 * Check if treat QName as a pair of a prefix and a local part other than a pair of
    	 * a namespace URI and a local name
    	 * @return keep QName as is instead of URI and local name if {@code true}
    	 */
		public boolean keepQNamePrefixes();
		/**
		 * Check if ignore the attribute during canonicalization
		 * @param attr an attribute to check
		 * @param elem an element which the attribute belongs
		 * @return ignore the attribute if {@code true}
		 */
    	public boolean ignorableAttribute(Attr attr, Element elem);
    	/**
    	 * Check if merge contents are required during canonicalization
    	 * @param children {@code true} if the caller is processing contents as children;
    	 * otherwise as a single node
    	 * @return the caller is required to merge contents if {@code true}; otherwise not.
    	 */
    	public boolean mergeContent(boolean children);
    	/**
    	 * Check if the value is a typed value
    	 * @param value a value to check
    	 * @return {@code true} if the value is a typed valued; otherwise not.
    	 */
    	public boolean isTypedValue(Object value);
    	/**
    	 * Check if the item is a skipped entity
    	 * @param node an item to check
    	 * @return {@code true} if the item is a skipped entity; otherwise not. 
    	 */
    	public String skippedEntity(Node node);
    	/**
    	 * Gets the type of the value
    	 * @param node an item to check
    	 * @return a type of the value or {@code null} if the value has no type.
    	 */
    	public XSTypeDefinition getValueType(Node node);
    	/**
    	 * Gets the type of the attribute value
    	 * @param attr an attribute to check
    	 * @return a type of the value or {@code null} if the value has no type.
    	 */
    	public XSSimpleTypeDefinition getAttrValueType(Attr attr);
    	/**
    	 * Gets the type of the content
    	 * @param type a type of the parent 
    	 * @return a type of the content or {@code null} if the content has no type.
    	 */
    	public XSSimpleTypeDefinition getContentType(XSTypeDefinition type);
    	/**
    	 * Gets the trimmed value of the content by removing beginning and ending white spaces
    	 * @param content a content to trim
    	 * @return a trimmed value
    	 */
    	public String getTrimmedValue(String content);
    	/**
    	 * Gets the lexical value of the content
    	 * @param content a content to check
    	 * @param collapse convert a sequence of white spaces into a single space character
    	 * if {@code true}; otherwise convert s white space character to a space character 
    	 * @return
    	 */
    	public String getLexicalValue(String content, boolean collapse);
    	/**
    	 * Gets the value of of a given QName
    	 * @param node a node to check
    	 * @param rawName a qualified name of the QName
    	 * @return a QName
    	 */
    	public Object getQNameValue(Node node, String rawName);
    	/**
    	 * Gets the canonical representation of the value
    	 * @param node a node to check
    	 * @param type a type to check
    	 * @param input an input to canonicalized value
    	 * @return a canonicalized value or {@code null} if validation failed
    	 */
    	public Object getCanonicalValue(Node node, XSTypeDefinition type, String input);
    }


    private static final Float minusZeroFloat = Float.parseFloat("-0.0");
    private static final Float plusZeroFloat = Float.parseFloat("0.0");
    private static final Double minusZeroDouble = Double.parseDouble("-0.0");
    private static final Double plusZeroDouble = Double.parseDouble("0.0");

    /**
     * Convert a value to a canonical form
     * @param n a node to convert
     * @param ti a type of the content
     * @param content a content to convert
     * @param typeHelper an instance of TypedValueHelper
     * @return a caninicalized value or {@code null} if the content has no type
     */
    private static Object decodeXml (Node n, XSSimpleTypeDefinition ti,
    		String content, TypedValueHelper typeHelper) {
    if (ti == null) {
    	return null;
    }
    Object result = typeHelper.getCanonicalValue(n, ti, content);
    if (result != null) {
    	return result;
    }
    String trimmed = typeHelper.getTrimmedValue(content);
	// System.out.println("decodeXml(" + n + "," + typeNs + "," + typeName
	// + "," + content + ")");
	result = trimmed;
	
	// System.out.println("decodeXml(" + typeNs + "," + typeName + ")");
	final short variety = ti.getVariety();
	if (variety == XSSimpleTypeDefinition.VARIETY_ATOMIC) {
	  try {
  	  switch (ti.getBuiltInKind()) {
            case XSConstants.BOOLEAN_DT:
                if (trimmed.equals("true") || trimmed.equals("1")) {
                  result = Boolean.TRUE;
                } else if (trimmed.equals("false") || trimmed.equals("0")) {
                  result = Boolean.FALSE;
                }
                break;
  	    case XSConstants.STRING_DT:
                result = content;
  	      break;
  	    case XSConstants.FLOAT_DT:
                Float f = Float.parseFloat(trimmed);
                if (f.equals(minusZeroFloat)) {
                    f = plusZeroFloat;
                }
                result = f;
                break;
  	    case XSConstants.DOUBLE_DT:
                Double d = Double.parseDouble(trimmed);
                if (d.equals(minusZeroDouble)) {
                    d = plusZeroDouble;
                }
                result = d;
                break;
  	    case XSConstants.DECIMAL_DT:
                BigDecimal bd = new BigDecimal(trimmed).stripTrailingZeros();
                if (bd.compareTo(BigDecimal.ZERO) == 0) {
                    bd = BigDecimal.ZERO;
                }
                result = bd;
                break;
  	    case XSConstants.DATETIME_DT: {
  	      XSDateTime dateTime = new XSDateTimeParser().parseDateTime(trimmed);
  	      result = dateTime != null ? dateTime : trimmed;
              break;
  	    }
  	    case XSConstants.TIME_DT: {
              XSDateTime dateTime = new XSDateTimeParser().parseTime(trimmed);
              result = dateTime != null ? dateTime : trimmed;
              break;
  	    }
  	    case XSConstants.DATE_DT: {
              XSDateTime dateTime = new XSDateTimeParser().parseDate(trimmed);
              result = dateTime != null ? dateTime : trimmed;
              break;
            }
  	    case XSConstants.GYEARMONTH_DT: {
              XSDateTime dateTime = new XSDateTimeParser().parseGYearMonth(trimmed);
              result = dateTime != null ? dateTime : trimmed;
              break;
  	    }
  	    case XSConstants.GYEAR_DT: {
              XSDateTime dateTime = new XSDateTimeParser().parseGYear(trimmed);
              result = dateTime != null ? dateTime : trimmed;
              break;
  	    }
  	    case XSConstants.GMONTHDAY_DT: {
              XSDateTime dateTime = new XSDateTimeParser().parseGMonthDay(trimmed);
              result = dateTime != null ? dateTime : trimmed;
              break;
  	    }
  	    case XSConstants.GDAY_DT: {
              XSDateTime dateTime = new XSDateTimeParser().parseGDay(trimmed);
              result = dateTime != null ? dateTime : trimmed;
              break;
  	    }
  	    case XSConstants.GMONTH_DT: {
              XSDateTime dateTime = new XSDateTimeParser().parseGMonth(trimmed);
              result = dateTime != null ? dateTime : trimmed;
              break;
  	    }
  	    case XSConstants.HEXBINARY_DT:
  	    	result = hexBinary(trimmed);
  	    	break;
  	    case XSConstants.BASE64BINARY_DT:
              result = new Base64(trimmed);
              break;
  	    case XSConstants.ANYURI_DT:
              result = trimmed;
              break;
  	    case XSConstants.QNAME_DT:
        case XSConstants.NOTATION_DT:
        	  result = typeHelper.getQNameValue(n, trimmed);
        	  break;
  	    case XSConstants.NORMALIZEDSTRING_DT:
              result = typeHelper.getLexicalValue(content, false); 
              break;
  	    case XSConstants.TOKEN_DT:
            case XSConstants.LANGUAGE_DT:
            case XSConstants.NMTOKEN_DT:
            case XSConstants.NAME_DT:
            case XSConstants.NCNAME_DT:
            case XSConstants.ID_DT:
            case XSConstants.IDREF_DT:
            case XSConstants.ENTITY_DT:
              result = typeHelper.getLexicalValue(content, true); 
              break;
  	    case XSConstants.INTEGER_DT:
            case XSConstants.NONPOSITIVEINTEGER_DT:
            case XSConstants.NEGATIVEINTEGER_DT:
            case XSConstants.NONNEGATIVEINTEGER_DT:
            case XSConstants.POSITIVEINTEGER_DT:
            case XSConstants.UNSIGNEDLONG_DT:
              if (trimmed.charAt(0) == '+') {
                trimmed = trimmed.substring(1);
              }
              result = new BigInteger(trimmed);
              break;
  	    case XSConstants.LONG_DT:
            case XSConstants.UNSIGNEDINT_DT:
              if (trimmed.charAt(0) == '+') {
                trimmed = trimmed.substring(1);
              }
              result = new Long(Long.parseLong(trimmed));
              break;
  	    case XSConstants.INT_DT:
            case XSConstants.UNSIGNEDSHORT_DT:
              if (trimmed.charAt(0) == '+') {
                trimmed = trimmed.substring(1);
              }
              result = new Integer(Integer.parseInt(trimmed));
              break;
            case XSConstants.SHORT_DT:
            case XSConstants.UNSIGNEDBYTE_DT:
              if (trimmed.charAt(0) == '+') {
                trimmed = trimmed.substring(1);
              }
              result = new Short(Short.parseShort(trimmed));
              break;
            case XSConstants.BYTE_DT:
              if (trimmed.charAt(0) == '+') {
                trimmed = trimmed.substring(1);
              }
              result = new Byte(Byte.parseByte(trimmed));
              break;
            case XSConstants.DURATION_DT:
              result = trimmed;
              break;
  	    case XSConstants.ANYSIMPLETYPE_DT:
              result = trimmed;
              break;
  	    default:
              result = trimmed;
              break;
    	  }
	}
        catch (NumberFormatException ex) {
          result = trimmed;
        }
        catch (Base64.Base64Exception ex) {
          result = trimmed;
        }
      }
      else if (variety == XSSimpleTypeDefinition.VARIETY_LIST) {
    	  LinkedList<Object> list = new LinkedList<Object>();
    	  StringTokenizer st = new StringTokenizer(trimmed, " \t\r\n");
    	  while (st.hasMoreTokens()) {
    		  String v = st.nextToken();
    		  Object o = decodeXml(n, ti.getItemType(), v, typeHelper);
    		  list.add(o == null ? v : o);
    	  }
    	  result = list;
      } else if (variety == XSSimpleTypeDefinition.VARIETY_UNION) {
    	  result = trimmed;
      }
      return result;
    }
    
    /**
     * Gets a canonical form of hexBinary's
     * @param input a content which represents a hexBinary
     * @return a canonicalized value
     */
    private static final String hexBinary(String input) {
    	boolean lowerCase = false;
    	for (int i = input.length(); --i >= 0;) {
    		final char c = input.charAt(i);
    		if ("0123456789ABCDEF".indexOf(c) >= 0) {
    			continue;
    		}
    		if ("abcdef".indexOf(c) >= 0) {
    			lowerCase = true;
    			continue;
    		}
    		return input;
    	}
    	return (lowerCase ? input.toUpperCase() : input);
    }

    /**
     * Convenient function: convert a null string to an emtpy string
     * @param s
     * @return
     */
    private static String nullToEmpty(String s) {
		return ((s == null) ? "" : s);
	}
    
    /**
         * Convert XML from a DOM tree to an event sequence. This method takes a
         * DOM tree as a {@link Node} and returns a {@link EventSequence}
         * representing the same XML document or a fragment of it. Some DOM
         * features (such as document types) are not supported by this method,
         * since {@link EventSequence} does not support them.
         * 
         * @param n the DOM node to convert
         * @return the event sequence representing <code>n</code>
         * 
         * @throws IllegalArgumentException if the XML represented by
         *         <code>n</code> contains unsupported features
         */
    public static EventSequence domToSequence (Node n, TypedValueHelper typeHelper) {
    	return domToSequence(n, null, typeHelper);
    }

    public static EventSequence domToSequence (Node n, XSTypeDefinition ti,
    		TypedValueHelper typeHelper) {
	// System.out.println("domToSequence(" + n + "," + typeNs + "," +
	// typeName
	// + "," + preservePrefix + "," + decodeTypes + ")");
	EventList el = new EventList();
	String ent = typeHelper.skippedEntity(n);
	if (ent != null) {
		el.add(Event.createEntityReference(ent));
		return el;
	}
	switch (n.getNodeType()) {
	case Node.DOCUMENT_NODE: {
	    el.add(Event.createStartDocument());
	    NodeList nodes = n.getChildNodes();
	    if (nodes != null) {
		for (int i = 0; i < nodes.getLength(); i++) {
		    el.addAll(domToSequence(nodes.item(i), ti, typeHelper));
		}
	    }
	    el.add(Event.createEndDocument());
	    break;
	}
	case Node.DOCUMENT_TYPE_NODE: {
	    DocumentType node = (DocumentType) n;
	    String name = node.getName();
	    String publicId = node.getPublicId();
	    String systemId = node.getSystemId();
	    StringBuffer text = new StringBuffer(" " + name + " ");
	    if (systemId != null && systemId.length() != 0) {
	        if (publicId != null && publicId.length() != 0) {
	            text.append("PUBLIC " + publicId + " " + systemId);
	        }
	        else {
	            text.append("SYSTEM " + systemId);
	        }
	    }
	    el.add(Event.createDoctypeDeclaration(text.toString()));
	    break;
	}
	case Node.ELEMENT_NODE: {
	    EventList pms = new EventList();
	    EventList atts = new EventList();
	    NamedNodeMap attMap = n.getAttributes();
	    boolean preservePrefix = typeHelper.preservePrefixes();
	    boolean keepPrefixes = (preservePrefix && typeHelper.keepQNamePrefixes());
	    for (int i = 0; i < attMap.getLength(); i++) {
	    	Attr att = (Attr) attMap.item(i);
	    	String name = att.getNodeName();
	    	if (name.startsWith("xmlns:")) {
	    		pms.add(Event.createNamespacePrefix(att.getValue(),
	    				name.substring(6)));
	    	} else if (name.equals("xmlns")) {
	    		pms.add(Event.createNamespacePrefix(att.getValue(), ""));
	    	} else if (!typeHelper.ignorableAttribute(att, (Element)n)) {
	    		final String ans;
	    		final String aln;
	    		if (keepPrefixes) {
		    		ans = null;
		    		aln = name;
	    		} else {
		    		ans = nullToEmpty(att.getNamespaceURI());
		    		aln = att.getLocalName();
		    	}
	    		final String value = att.getValue();
	    		Object o = decodeXml(att,
	    				typeHelper.getAttrValueType(att),
	    				value, typeHelper);
	    		if (o != null) {
	    			atts.add(Event.createTypedAttribute(ans, aln, o));
	    		} else {
	    			atts.add(Event.createAttribute(ans, aln, value));
	    		}
	    	}
	    }
	    if (preservePrefix) {
	    	if (typeHelper.needExtraEffort()) {
	    		pms = sortPrefixes(pms);
	    	}
	    	el.addAll(pms);
	    }
	    final String ns;
	    final String ln;
	    if (keepPrefixes) {
	    	ns = null;
	    	ln = n.getNodeName();
	    } else {
	    	ns = nullToEmpty(n.getNamespaceURI());
	    	ln = n.getLocalName();
	    }
	    el.add(Event.createStartElement(ns, ln));
	    el.addAll(atts);
	    NodeList children = n.getChildNodes();
	    if (children != null) {
	    	XSTypeDefinition _ti = typeHelper.getValueType(n);
	    	// System.out.println("{" + ns + "}" + n.getLocalName() + ": "
	    	// + ti);
	    	int max = children.getLength();
	    	if (typeHelper.mergeContent(true)) {
	    		StringBuilder sb = null;
	    		for (int i = 0; i < max; i++) {
	    			Node cn = children.item(i);
	    			int nt = cn.getNodeType();
	    			if (nt == Node.TEXT_NODE || nt == Node.CDATA_SECTION_NODE) {
	    				if (sb == null) {
	    					sb = new StringBuilder(cn.getNodeValue());
	    				} else {
	    					sb.append(cn.getNodeValue());
	    				}
	    			} else {
	    				contentToSequence(sb, el, n, _ti, typeHelper);
	    				el.addAll(domToSequence(cn, _ti, typeHelper));
	    			}
	    		}
	    		contentToSequence(sb, el, n, _ti, typeHelper);
	    	} else {
	    		for (int i = 0; i < max; i++) {
	    			el.addAll(domToSequence(children.item(i), _ti, typeHelper));
	    		}
	    	}
	    }
	    el.add(Event.createEndElement(ns, ln));
	    break;
	}
	case Node.TEXT_NODE:
	case Node.CDATA_SECTION_NODE:
		typeHelper.mergeContent(false);
		contentToSequence(n.getNodeValue(), el, n, ti, typeHelper);
		break;
	case Node.ENTITY_REFERENCE_NODE:
	    el.add(Event.createEntityReference(n.getNodeName()));
	    break;
	case Node.PROCESSING_INSTRUCTION_NODE: {
	    el.add(Event.createProcessingInstruction(n.getNodeName()
	    		+ " " + n.getNodeValue()));
	    break;
		
	}
	case Node.COMMENT_NODE:
	    el.add(Event.createComment(n.getNodeValue()));
	    break;
	default:
	    throw new IllegalArgumentException("Unhandled node type: "
		    + n.getNodeType());
	}
	return el;
    }
    
    /**
     * A wrapper to convert contents to a canonicalized event sequence 
     * @param sb a contents to examine
     * @param el an event list to add
     * @param n a parent node for the contents
     * @param ti a type of the contents
     * @param typeHelper an instance of the helper
     */
    private static void contentToSequence(StringBuilder sb, EventList el,
    		Node n, XSTypeDefinition ti, TypedValueHelper typeHelper) {
    	if (sb != null && sb.length() > 0) {
			contentToSequence(sb.toString(), el, n, ti, typeHelper);
			sb.setLength(0);
    	}
    }
    
    /**
     * Convert contents to a canonicalized event sequence
     * @param value a content to convert
     * @param el an event list to add
     * @param n a parent node for the contents
     * @param ti a type of the contents
     * @param typeHelper an instance of the helper
     */
    private static void contentToSequence(String value, EventList el,
    		Node n, XSTypeDefinition ti, TypedValueHelper typeHelper) {
    	if (value.length() == 0) {
    		return;
    	}
    	XSSimpleTypeDefinition _ti = typeHelper.getContentType(ti);
    	Object o = decodeXml(n, _ti, value, typeHelper);	        	
    	if (o != null && !typeHelper.isTypedValue(o)) {
    		el.add(Event.createTypedContent(_ti.getNamespace(), _ti.getName(), o));
    	} else {
    		el.add(Event.createContent(typeHelper.getLexicalValue(value, true)));
    	}
    }
    
    /**
     * Sorts namespace declarations
     * @param el a list of the namespace declarations
     * @return a sorted event list
     */
    private static EventList sortPrefixes(EventList el) {
    	final int last = el.getLargestActiveIndex();
    	final int first = el.getSmallestActiveIndex();

    	final TreeMap<String,Event> sorter = new TreeMap<String,Event>();
    	for (int i = first; i <= last; i++) {
    		Event ev = el.get(i);
    		sorter.put(ev.toString(), ev);
    	}
    	EventList uniq = new EventList();
    	for (Event ev : sorter.values()) {
    		uniq.add(ev);
    	}
    	return uniq;
    }

    
    /**
     * Get the canonical sequence.
     * Canonicalization is done by combining contiguous context events
     * into a single context event.
     * @param seq an original sequence
     * @return a canonical sequence
     */
    public static EventSequence canonicalSequence(EventSequence seq, boolean isIot,
    		TypedValueHelper typeHelper) {
    	EventList canon = new EventList();
    	final int last = seq.getLargestActiveIndex();
    	final int first = seq.getSmallestActiveIndex();

    	StringBuilder sb = null;
    	Event ev;
    	for (int i = first; i <= last; i++) {
    		ev = seq.get(i);
    		if (ev.getType() == Event.CONTENT) {
    			if (sb == null) {
    				sb = new StringBuilder();
    			}
    			sb.append(ev.getValue().toString());
    			continue;
    		}
    		if (sb != null) {
    			String text = sb.toString();
    			if (isIot) {
    				text = typeHelper.getLexicalValue(text, true);
    				if (text.length() > 0) {
    					canon.add(Event.createContent(text));
    				}
    			} else {
    				canon.add(Event.createContent(text));
    			}
    			sb.setLength(0);
    		}
    		canon.add(ev);
    	}
    	return seq;
    }

    /**
         * Get an XML parser suitable for the given input stream. Uses a
         * {@link XasExtUtil.AggressiveXmlIdentifier} to determine the mime
         * type.
         * @param in input stream that will be attached to the parser
         * @param enc input character encoding, e.g. <code>utf-8</code>
         * @return a parser suitable for the input stream, or null if none
         *         available
         * @throws IOException if an I/O error occured when determining the type
         *         of the stream.
         */
    public static TypedXmlParser getParser (InputStream in, String enc)
	    throws IOException {
	return getParser(new AggressiveXmlIdentifier(in), enc);
    }

    /**
         * Get a parser suitable for the given input stream.
         * @param in input stream that will be attached to the parser
         * @param enc input character encoding, e.g. <code>utf-8</code>
         * @return a parser suitable for the input stream, or null if none
         *         available
         * @throws IOException if an I/O error occured when determining the type
         *         of the stream.
         */

    public static TypedXmlParser getParser (MagicInputStream in, String enc)
	    throws IOException {
	String type = in.getMimeType();
	if (type == null)
	    return null; // Can't determine parser
	CodecFactory cf = CodecIndustry.getFactory(type);
	if (cf == null)
	    return null;
	TypedXmlParser p = cf.getNewDecoder(new Object());
	try {
	    boolean isBinary = !(p instanceof DefaultXmlParser);
	    p.setInput(in, isBinary ? p.getInputEncoding() : enc);
	} catch (XmlPullParserException ex) {
	    IOException iox = new IOException("XmlPullParserException " + ex);
	    throw iox;
	}
	return p;
    }

    /**
         * Get XML serializer for an output stream. The serializer uses the
         * recommended MIME type for XML on the platform (usually a type
         * belongin to the <code>application/x-ebu</code> family)
         * @return a serializer, or <code>null</code> if none available
         * 
         * @param out output stream for serializer
         * @param enc character encoding
         * @return serializer
         * @throws IOException if the serializer causes an IOException
         */
    public static TypedXmlSerializer getSerializer (OutputStream out, String enc)
	    throws IOException {
	return getSerializer(out, "application/x-ebu+item", enc);
    }

    /**
         * Get XML serializer for an output stream. Use <code>text/xml</code>
         * to get textual, idented output.
         * @param out target stream for the serializer
         * @param mimeType MIME type of output
         * @param enc Output encoding (ignored for Xebu formats)
         * @return a serializer, or <code>null</code> if none available
         * @throws IOException if the serializer causes an IOException
         */

    public static TypedXmlSerializer getSerializer (OutputStream out,
	    String mimeType, String enc) throws IOException {
	CodecFactory cf = CodecIndustry.getFactory(mimeType);
	if (cf == null)
	    return null;
	TypedXmlSerializer ser = cf.getNewEncoder(new Object());
	boolean isBinary = !(ser instanceof DefaultXmlSerializer);
	ser.setOutput(out, isBinary ? "ISO-8859-1" : enc);
	if (!isBinary)
	    ser.setFeature(
		"http://xmlpull.org/v1/doc/features.html#indent-output", true);
	return ser;
    }

    /**
         * Identifies textual and Xebu encoded XML files. Identification is done
         * by inspecting the first 4 bytes of the file. The case insensitive
         * string <code>&lt;?xm</code> identifies the file as
         * <code>text/xml</code>. A Xebu <code>START_DOCUMENT</code> event
         * identifies the content as Xebu encoded; the correct subtype is
         * determined from the document start flags.
         */

    public static class XebuXmlIdentifier extends MagicInputStream {

	public XebuXmlIdentifier (InputStream in) throws IOException {
	    super(in);
	}

	protected String identify (byte[] magic, int len) {
	    final String[] SUBTYPES = { "none", "item", null, "data", "elem",
		    "elit", null, "elid" };
	    String ms = new String(magic, 0, len);
	    if (len == 4 && ms.toLowerCase().startsWith("<?xm"))
		return "text/xml";
	    if (len < 1)
		return null;
	    if ((magic[0] & XebuConstants.TOKEN_SPACE) == XebuConstants.DOCUMENT) {
		int subtype = ((magic[0] & XebuConstants.FLAG_SEQUENCE_CACHING) != 0 ? 4
			: 0)
			+ ((magic[0] & XebuConstants.FLAG_CONTENT_CACHING) != 0 ? 2
				: 0)
			+ ((magic[0] & XebuConstants.FLAG_ITEM_CACHING) != 0 ? 1
				: 0);
		return SUBTYPES[subtype] == null ? null : "application/x-ebu+"
			+ SUBTYPES[subtype];
	    }
	    return null;
	}

    }

    /**
         * Identifies textual and Xebu encoded XML files. Same as a
         * {@link XasExtUtil.XebuXmlIdentifier}, but also recognizes '<*'
         * where * is a letter as <code>text/xml</code>. This identifier is
         * useful when we really expect the content to be XML, and pretty much
         * want to try a forced parse.
         * 
         */

    public static class AggressiveXmlIdentifier extends XebuXmlIdentifier {

	public AggressiveXmlIdentifier (InputStream in) throws IOException {
	    super(in);
	}

	protected String identify (byte[] magic, int len) {
	    String type = super.identify(magic, len);
	    if (type == null) {
		String ms = new String(magic, 0, len).trim();
		type = ms.startsWith("<") && ms.length() > 1
			&& Character.isLetter(ms.charAt(1)) ? "text/xml" : null;
	    }
	    return type;
	}

    }

    /**
     * Gets an empty event sequence
     * @return an empty event sequence
     */
    public static EventSequence nullSequence() {
    	EventList list = new EventList();
	    list.add(Event.createStartDocument());
	    list.add(Event.createEndDocument());
	    return list;
    }

}
// arch-tag: b481a37420320fbb5b08f564dd3c05f9 *-
