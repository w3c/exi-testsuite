package fc.fp.util.xas;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;

/**
 * A wrapper class to write with SAX events with XmlPull. This class wraps a
 * <code>XmlSerializer</code> and implements the SAX {@link ContentHandler}
 * interface, which is useful for output. The intent is to allow legacy SAX
 * applications to use an XmlPull-based serializer without altering their own
 * code. New classes are recommended to use the {@link EventSequence} interface
 * and its implementing classes to handle XML.
 */
public class SaxWriter implements ContentHandler, DTDHandler, LexicalHandler,
	DeclHandler {

    protected TypedXmlSerializer target;
    private Map<String, String> prefixes = new HashMap<String, String>();

    public SaxWriter (TypedXmlSerializer target) {
	this(target, null);
    }

    public SaxWriter (TypedXmlSerializer target, OutputStream os) {
	this.target = target;
	if (os != null) {
	    try {
		target.setOutput(os, "ISO-8859-1");
	    } catch (IOException ex) {
		/*
                 * It is nonsensical for this method to throw exceptions, since
                 * this should only prepare for output, not actually output
                 * anything.
                 */
	    }
	}
    }

    public void setDocumentLocator (Locator locator) {
    }

    public void startDocument () throws SAXException {
	if (target != null) {
	    try {
		target.startDocument("ISO-8859-1", null);
	    } catch (IOException ex) {
		throw new SAXException(ex);
	    }
	}
    }

    public void endDocument () throws SAXException {
	if (target != null) {
	    try {
		target.endDocument();
		target.flush();
	    } catch (IOException ex) {
		throw new SAXException(ex);
	    }
	}
    }

    public void startPrefixMapping (String prefix, String uri)
	    throws SAXException {
	if (prefix == null) {
	    prefix = "";
	}
	prefixes.put(prefix, uri);
	if (target != null) {
	    try {
		target.setPrefix(prefix, uri);
	    } catch (IOException ex) {
		throw new SAXException(ex);
	    }
	}
    }

    public void endPrefixMapping (String prefix) throws SAXException {
	prefixes.remove(prefix);
    }

    public void startElement (String namespaceURI, String localName,
	    String qName, Attributes attributes) throws SAXException {
	if (target != null) {
	    try {
		if (localName == null || localName.equals("")) {
		    namespaceURI = "";
		    int index = qName.indexOf(':');
		    if (index >= 0) {
			String uri = prefixes.get(qName.substring(0, index));
			if (uri != null) {
			    namespaceURI = uri;
			}
			localName = qName.substring(index + 1);
		    } else {
			String uri = prefixes.get("");
			if (uri != null) {
			    namespaceURI = uri;
			}
			localName = qName;
		    }
		}
		target.startTag(namespaceURI, localName);
		if (attributes != null) {
		    int length = attributes.getLength();
		    for (int i = 0; i < length; i++) {
			String localAttName = attributes.getLocalName(i);
			String attUri = attributes.getURI(i);
			String attQName = attributes.getQName(i);
			if (localAttName == null || localAttName.length() == 0) {
			    attUri = "";
			    int index = attQName.indexOf(':');
			    if (index >= 0) {
				String uri = prefixes.get(attQName.substring(0,
				    index));
				if (uri != null) {
				    attUri = uri;
				}
				localAttName = attQName.substring(index + 1);
			    } else {
				localAttName = attQName;
			    }
			}
			if (attUri.equals(XasUtil.XSI_NAMESPACE)
				&& localAttName.equals("type")) {
			    String value = attributes.getValue(i).trim();
			    String prefix = "";
			    int index = value.indexOf(':');
			    if (index > 0) {
				prefix = value.substring(0, index);
				value = value.substring(index + 1);
			    }
			    String namespace = prefixes.get(prefix);
			    if (namespace == null) {
				namespace = "";
			    }
			    target.typedAttribute(attUri, localAttName,
				new Qname(namespace, value));
			} else {
			    target.attribute(attUri, localAttName, attributes
				.getValue(i));
			}
		    }
		}
	    } catch (IOException ex) {
		throw new SAXException(ex);
	    }
	}
    }

    public void endElement (String namespaceURI, String localName, String qName)
	    throws SAXException {
	if (target != null) {
	    try {
		if (localName == null || localName.equals("")) {
		    namespaceURI = "";
		    int index = qName.indexOf(':');
		    if (index >= 0) {
			String uri = prefixes.get(qName.substring(0, index));
			if (uri != null) {
			    namespaceURI = uri;
			}
			localName = qName.substring(index + 1);
		    } else {
			String uri = prefixes.get("");
			if (uri != null) {
			    namespaceURI = uri;
			}
			localName = qName;
		    }
		}
		target.endTag(namespaceURI, localName);
	    } catch (IOException ex) {
		throw new SAXException(ex);
	    }
	}
    }

    public void characters (char[] ch, int start, int length)
	    throws SAXException {
	if (target != null) {
	    try {
		target.text(ch, start, length);
	    } catch (IOException ex) {
		throw new SAXException(ex);
	    }
	}
    }

    public void ignorableWhitespace (char[] ch, int start, int length)
	    throws SAXException {
	if (target != null) {
	    try {
		target.text(ch, start, length);
	    } catch (IOException ex) {
		throw new SAXException(ex);
	    }
	}
    }

    public void processingInstruction (String target, String data)
	    throws SAXException {
	if (this.target != null) {
	    try {
		this.target.processingInstruction(target + " " + data);
	    } catch (IOException ex) {
		throw new SAXException(ex);
	    }
	}
    }

    public void skippedEntity (String name) throws SAXException {
	if (target != null) {
	    try {
		target.entityRef(name);
	    } catch (IOException ex) {
		throw new SAXException(ex);
	    }
	}
    }

    public void notationDecl (String name, String publicId, String systemId)
	    throws SAXException {
	// TODO Auto-generated method stub

    }

    public void unparsedEntityDecl (String name, String publicId,
	    String systemId, String notationName) throws SAXException {
	// TODO Auto-generated method stub

    }

    public void comment (char[] ch, int start, int length) throws SAXException {
	if (target != null) {
	    try {
		target.comment(new String(ch, start, length));
	    } catch (IOException ex) {
		throw new SAXException(ex);
	    }
	}
    }

    public void endCDATA () throws SAXException {
	// TODO Auto-generated method stub

    }

    public void endDTD () throws SAXException {
	// TODO Auto-generated method stub

    }

    public void endEntity (String name) throws SAXException {
	// TODO Auto-generated method stub

    }

    public void startCDATA () throws SAXException {
	// TODO Auto-generated method stub

    }

    public void startDTD (String name, String publicId, String systemId)
	    throws SAXException {
	if (target != null) {
	    try {
		StringBuilder s = new StringBuilder(" ");
		s.append(name);
		if (publicId != null) {
		    s.append(" PUBLIC ");
		    s.append(publicId);
		    s.append(" ");
		    s.append(systemId);
		} else if (systemId != null) {
		    s.append(" SYSTEM ");
		    s.append(systemId);
		}
		target.docdecl(s.toString());
	    } catch (IOException ex) {
		throw new SAXException(ex);
	    }
	}
    }

    public void startEntity (String name) throws SAXException {
	// TODO Auto-generated method stub

    }

    public void attributeDecl (String eName, String aName, String type,
	    String mode, String value) throws SAXException {
	// TODO Auto-generated method stub

    }

    public void elementDecl (String name, String model) throws SAXException {
	// TODO Auto-generated method stub

    }

    public void externalEntityDecl (String name, String publicId,
	    String systemId) throws SAXException {
	// TODO Auto-generated method stub

    }

    public void internalEntityDecl (String name, String value)
	    throws SAXException {
	// TODO Auto-generated method stub

    }

}
// arch-tag: a8d7aa9d3940f5b96c9da9a2286c9832 *-
