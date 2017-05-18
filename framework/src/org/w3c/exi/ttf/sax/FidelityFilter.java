/*
 * EXI Testing Task Force Measurement Suite: http://www.w3.org/XML/EXI/
 * 
 * Copyright ? [2006] World Wide Web Consortium, (Massachusetts Institute of
 * Technology, European Research Consortium for Informatics and Mathematics,
 * Keio University). All Rights Reserved. This work is distributed under the
 * W3C? Software License [1] in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.
 * 
 * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
 */

package org.w3c.exi.ttf.sax;

import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;

import org.w3c.exi.ttf.parameters.PreserveParam;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

public class FidelityFilter extends XMLFilterImpl implements LexicalHandler,
	DeclHandler {

    private boolean hasComments = false;
    private boolean hasDtds = false;
    private boolean hasErs = false;
    private boolean hasNotations = false;
    private boolean hasPis = false;
    private boolean hasPrefixes = false;
    private boolean hasWhitespace = false;

    private boolean filterPrefixes = false;
    private boolean externalDtd = false;

    private boolean currentXmlSpace = false;
    private LinkedList<Boolean> xmlSpaceStack = new LinkedList<Boolean>();
    private StringBuilder contentBuffer = new StringBuilder();

    private Stack<PrefixUriBindings> prefixUriBindingsStack = new Stack<PrefixUriBindings>();
    private PrefixUriBindings prefixUriBindingsDefault = new PrefixUriBindings();
    private PrefixUriBindings currentPrefixUriBindings;

    private static final int INITIAL = 0;
    private static final int AFTER_START = 1;
    private static final int AFTER_END = 2;
    private static final int CONTENT = 3;
    private int state = INITIAL;

    private DefaultHandler2 handler = new DefaultHandler2();
    private DeclHandler declHandler = handler;
    private LexicalHandler lexicalHandler = handler;
    
    private final boolean isIot;
    
    public static String PI_ENTITY_REFERENCE = "_Entity_Reference_";
    
    // this should be removed future
    private static final boolean iotWhitespace = false; 
    
    

    public FidelityFilter (XMLReader parent, Set<PreserveParam> preserves, boolean isIot)
	    throws SAXException {
	this(parent, preserves, isIot, false);
    }

    public FidelityFilter (XMLReader parent, Set<PreserveParam> preserves, boolean isIot,
	    boolean filterPrefixes) throws SAXException {
	super(parent);
	for (PreserveParam param : preserves) {
	    switch (param) {
	    case comments:
		hasComments = true;
		break;
	    case dtds:
		hasDtds = true;
		break;
	    case entityreferences:
		hasErs = true;
		break;
	    case notations:
		hasNotations = true;
		break;
	    case pis:
		hasPis = true;
		break;
	    case prefixes:
		hasPrefixes = true;
		break;
	    case whitespace:
		hasWhitespace = true;
		break;
	    }
	}
	this.filterPrefixes = filterPrefixes;
	parent.setProperty("http://xml.org/sax/properties/declaration-handler",
	    this);
	parent.setProperty("http://xml.org/sax/properties/lexical-handler",
	    this);
	this.isIot = isIot;
	if (isIot) {
		hasWhitespace = true;
	}
    }

    public void setProperty (String name, Object value)
	    throws SAXNotRecognizedException, SAXNotSupportedException {
	if (name.equals("http://xml.org/sax/properties/declaration-handler")) {
	    declHandler = (DeclHandler) value;
	} else if (name.equals("http://xml.org/sax/properties/lexical-handler")) {
	    lexicalHandler = (LexicalHandler) value;
	} else {
	    super.setProperty(name, value);
	}
    }

    public void setDeclHandler (DeclHandler declHandler) {
	this.declHandler = declHandler;
    }

    public void setLexicalHandler (LexicalHandler lexicalHandler) {
	this.lexicalHandler = lexicalHandler;
    }

    public void attributeDecl (String eName, String aName, String type,
	    String mode, String value) throws SAXException {
	if (hasDtds && !externalDtd) {
	    declHandler.attributeDecl(eName, aName, type, mode, value);
	}
    }

    public void comment (char[] ch, int start, int length) throws SAXException {
    	if (hasComments && !externalDtd) {
    		outputContent(true);
    		state = CONTENT;
    		lexicalHandler.comment(ch, start, length);
    	} else if (isIot) {
    		outputContent(true);
    		state = CONTENT;
    	}
    }

    public void elementDecl (String name, String model) throws SAXException {
	if (hasDtds && !externalDtd) {
	    declHandler.elementDecl(name, model);
	}
    }

    public void endCDATA () throws SAXException {
	lexicalHandler.endCDATA();
    }

    public void endDTD () throws SAXException {
	if (hasDtds) {
	    lexicalHandler.endDTD();
	}
    }

    public void endEntity (String name) throws SAXException {
	lexicalHandler.endEntity(name);
	if (name.equals("[dtd]")) {
	    externalDtd = false;
	}
    }

    public void externalEntityDecl (String name, String publicId,
	    String systemId) throws SAXException {
	if (hasDtds && !externalDtd) {
	    declHandler.externalEntityDecl(name, publicId, systemId);
	}
    }

    public void internalEntityDecl (String name, String value)
	    throws SAXException {
	if (hasDtds && !externalDtd) {
	    declHandler.internalEntityDecl(name, value);
	}
    }

    public void startCDATA () throws SAXException {
	lexicalHandler.startCDATA();
    }

    @Override
    public void startDocument () throws SAXException {
    	currentPrefixUriBindings = prefixUriBindingsStack.push(prefixUriBindingsDefault);
    	super.startDocument();
    }

    public void startDTD (String name, String publicId, String systemId)
	    throws SAXException {
        if (hasDtds) {
          lexicalHandler.startDTD(name, publicId, systemId);
        }
    }

    public void startEntity (String name) throws SAXException {
	lexicalHandler.startEntity(name);
	if (name.equals("[dtd]")) {
	    externalDtd = true;
	}
    }

    private final static String WHITE_SPACES = " \t\r\n";
    
    private boolean isNonWhiteSpace(char ch) {
    	return (WHITE_SPACES.indexOf(ch) < 0);
    }
    
    private boolean isNonWhiteSpaces(char[] ch, int start, int length) {
    	for (int i = start + length; --i >= start;) {
    		if (isNonWhiteSpace(ch[i])) {
    			return true;
    		}
    	}
    	return false;
    }

    private void outputContent(boolean flush) throws SAXException {
     	if (flush || hasWhitespace) {
    		final int count = contentBuffer.length();
    		if (count > 0) {
    		    char[] ch = contentBuffer.toString().toCharArray();
    		    if (isIot) {
    		    	int begin;
    		    	for (begin = 0; begin < count; begin++) {
    		    		if (isNonWhiteSpace(ch[begin])) {
    		    			break;
    		    		}
    		    	}
    		    	if (begin < count) { 
    		    		int end = count;
    		    		while (--end >= begin) {
    		    			if (isNonWhiteSpace(ch[end])) {
    		    				break;
    		    			}
    		    		}
    		    		super.characters(ch, begin, end - begin + 1);
    		    	}
    		    } else {
    		    	super.characters(ch, 0, count);
    		    }
    		}
      	}
	    contentBuffer.setLength(0);
    }

    public void characters (char[] ch, int start, int length)
	    throws SAXException {
    	/*
    	 * Xerces parser included in JDK somehow reports multiple characters events even
    	 * if a chunk of character data is small.
    	 * The routine below removes white spaces at the beginning and the end from each
    	 * reported character data. If characters events are divided, those white spaces
    	 * are removed even if they are in the middle of a single chunk. So, this routine
    	 * is disabled. However, leave the code as is and should be removed future.
    	 * Note: {@code iotWhitespace} is always {@code false}.  
    	 */
        if (iotWhitespace) { 
            // collapse whitespaces
            int i;
            for (i = start; i < start + length && !Character.isWhitespace(ch[i]); i++);
            if (i < start + length) {
              char[] ch2 = new char[length];
              boolean initState = true;
              boolean whiteSpaceDeposited = false;
              int pos = 0;
              for (i = start; i < start + length; i++) {
                final char c;
                switch (c = ch[i]) {
                  case '\t':
                  case '\n':
                  case '\r':
                  case ' ':
                    if (!initState) {
                      whiteSpaceDeposited = true;
                    }
                    break;
                  default:
                    if (initState) {
                      assert !whiteSpaceDeposited;
                      initState = false;
                    }
                    else if (whiteSpaceDeposited) {
                      ch2[pos++] = ' ';
                      whiteSpaceDeposited = false;
                    }
                    ch2[pos++] = c;
                    break;
                }
              }
              if ((length = pos) == 0)
                return;
              ch = ch2;
              start = 0;  
            }
        }
    if (isIot) {
    	contentBuffer.append(ch, start, length);
    	state = CONTENT;
    } else if (currentXmlSpace || state == CONTENT || isNonWhiteSpaces(ch, start, length)) {
	    outputContent(true);
	    super.characters(ch, start, length);
	    state = CONTENT;
	} else {
	    contentBuffer.append(ch, start, length);
	}
    }
    

    @Override
    public void endElement (String uri, String localName, String qName)
	    throws SAXException {
	if (state == AFTER_START || isIot) {
	    outputContent(true);
	} else if (state == AFTER_END) {
	    outputContent(false);
	}
	currentXmlSpace = xmlSpaceStack.removeLast();
	if (xmlSpaceStack.size() == 0) {
	    state = INITIAL;
	} else {
	    state = AFTER_END;
	}
	prefixUriBindingsStack.pop();
	currentPrefixUriBindings = prefixUriBindingsStack.peek();
	super.endElement(uri, localName, qName);
    }

    @Override
    public void startElement (String uri, String localName, String qName,
	    Attributes atts) throws SAXException {
	prefixUriBindingsStack.push(currentPrefixUriBindings);
	if (state == AFTER_START || state == AFTER_END || isIot) {
	    outputContent(isIot);
	}
	xmlSpaceStack.add(currentXmlSpace);
	String xmlSpace = atts.getValue("http://www.w3.org/XML/1998/namespace",
	    "space");
	if (xmlSpace != null) {
	    if (xmlSpace.equals("preserve")) {
		currentXmlSpace = true;
	    } else {
		currentXmlSpace = false;
	    }
	}
	state = AFTER_START;
	final AttributesImpl attrsImpl = new AttributesImpl(atts);
	if (isIot) {
		final int n_attrs = attrsImpl.getLength();
		for (int i = 0; i < n_attrs; i++) {
			if ("http://www.w3.org/2001/XMLSchema-instance".equals(attrsImpl.getURI(i)) && "type".equals(attrsImpl.getLocalName(i))) {
				final String qname = attrsImpl.getValue(i);
				final String typeUri, localPart;
				final int colonIndex;
				if ((colonIndex = qname.indexOf(':')) != -1) {
					final String prefix = qname.substring(0, colonIndex);
					typeUri = currentPrefixUriBindings.getUri(prefix);
					localPart = qname.substring(colonIndex + 1);
				}
				else {
					typeUri = currentPrefixUriBindings.getDefaultUri();
					localPart = qname;
				}
				if ("http://www.w3.org/2001/XMLSchema".equals(typeUri) && "anyType".equals(localPart)) {
					attrsImpl.removeAttribute(i);
					break;
				}
			}
		}
	}
	super.startElement(uri, localName, qName, attrsImpl);
    }

    public void endPrefixMapping (String prefix) throws SAXException {
	if (!filterPrefixes || hasPrefixes) {
          if (!"xml".equals(prefix))
	    super.endPrefixMapping(prefix);
	}
    }

    public void ignorableWhitespace (char[] ch, int start, int length)
	    throws SAXException {
      if (!isIot) {
	if (currentXmlSpace || hasWhitespace) {
	    super.ignorableWhitespace(ch, start, length);
	}
      }
    }

    public void notationDecl (String name, String publicId, String systemId)
	    throws SAXException {
	if (hasNotations) {
	    super.notationDecl(name, publicId, systemId);
	}
    }

    public void processingInstruction (String target, String data)
    		throws SAXException {
    	if (hasPis && !externalDtd) {
    		outputContent(true);
    		state = CONTENT;
    		super.processingInstruction(target, data);
    	} else if (isIot) {
    		outputContent(true);
    		state = CONTENT;
    	}
    }

    public void skippedEntity (String name) throws SAXException {
	if (hasErs) {
            // serializer seems to ignore skippedEntity.
            // super.skippedEntity(name);
            super.processingInstruction(PI_ENTITY_REFERENCE, name);
	}
    }

    public void startPrefixMapping (String prefix, String uri)
	    throws SAXException {
	if (prefix.length() != 0)
		currentPrefixUriBindings = currentPrefixUriBindings.bind(prefix, uri);
	else
		currentPrefixUriBindings = currentPrefixUriBindings.bindDefault(uri);
	if (!filterPrefixes || hasPrefixes) {
          if (!"xml".equals(prefix)) // diff is agnostic about namespace declaration for "xml" prefix
            super.startPrefixMapping(prefix, uri);
	}
    }

    public void unparsedEntityDecl (String name, String publicId,
	    String systemId, String notationName) throws SAXException {
	if (hasDtds && !externalDtd) {
	    super.unparsedEntityDecl(name, publicId, systemId, notationName);
	}
    }

    
    private static class PrefixUriBindings {
    	  final private String[] prefixes;
    	  final private String[] uris;
    	  final private String defaultUri;

    	  public PrefixUriBindings() {
    	    this(new String[0], new String[0], "");
    	  }

    	  private PrefixUriBindings(String[] prefixes, String[] uris, String defaultUri) {
    	    this.prefixes = prefixes;
    	    this.uris = uris;
    	    this.defaultUri = defaultUri;
    	  }

    	  public final int getSize() {
    	    return prefixes.length;
    	  }

    	  public final String getPrefix(int i) {
    	    return prefixes[i];
    	  }

    	  public final String getUri(int i) {
    	    return uris[i];
    	  }

    	  public final String getPrefix(String uri) {
    	    final int length = uris.length;
    	    for (int i = 0; i < length; i++) {
    	      if (uri.equals(uris[i]))
    	        return prefixes[i];
    	    }
    	    return null;
    	  }

    	  public final String getUri(String prefix) {
    	    final int length = prefixes.length;
    	    for (int i = 0; i < length; i++) {
    	      if (prefix.equals(prefixes[i]))
    	        return uris[i];
    	    }
    	    return null;
    	  }

    	  public final String getDefaultUri() {
    	    return defaultUri;
    	  }

    	  public PrefixUriBindings unbind(String prefix) {
    	    final int length = prefixes.length;
    	    for (int i = 0; i < length; i++) {
    	      if (prefix.equals(prefixes[i])) {
    	        String[] newPrefixes = new String[length - 1];
    	        System.arraycopy(prefixes, 0, newPrefixes, 0, i);
    	        System.arraycopy(prefixes, i + 1, newPrefixes, i, length - i - 1);
    	        String[] newUris = new String[length - 1];
    	        System.arraycopy(uris, 0, newUris, 0, i);
    	        System.arraycopy(uris, i + 1, newUris, i, length - i - 1);
    	        return new PrefixUriBindings(newPrefixes, newUris, defaultUri);
    	      }
    	    }
    	    return this;
    	  }

    	  public PrefixUriBindings bind(String prefix, String uri) {
    	    int i;
    	    for (i = 0; i < prefixes.length; i++) {
    	      final int res;
    	      if ((res = prefix.compareTo(prefixes[i])) < 0)
    	        break;
    	      if (res == 0) {
    	        if (uri.equals(uris[i]))
    	          return this;
    	        String[] newUris = (String[])uris.clone();
    	        newUris[i] = uri;
    	        return new PrefixUriBindings(prefixes, newUris, defaultUri);
    	      }
    	    }
    	    String[] newPrefixes = new String[prefixes.length + 1];
    	    System.arraycopy(prefixes, 0, newPrefixes, 0, i);
    	    String[] newUris = new String[uris.length + 1];
    	    System.arraycopy(uris, 0, newUris, 0, i);
    	    newPrefixes[i] = prefix;
    	    newUris[i] = uri;
    	    System.arraycopy(prefixes, i, newPrefixes, i + 1, prefixes.length - i);
    	    System.arraycopy(uris, i, newUris, i + 1, uris.length - i);
    	    return new PrefixUriBindings(newPrefixes, newUris, defaultUri);
    	  }

    	  public PrefixUriBindings bindDefault(String uri) {
    	    if (uri.equals(defaultUri))
    	      return this;
    	    return new PrefixUriBindings(prefixes, uris, uri);
    	  }

    	  public PrefixUriBindings unbindDefault() {
    	    if (defaultUri == "")
    	      return this;
    	    return new PrefixUriBindings(prefixes, uris, "");
    	  }
    }
}
