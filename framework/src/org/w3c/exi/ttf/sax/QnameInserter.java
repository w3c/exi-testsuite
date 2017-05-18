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

package org.w3c.exi.ttf.sax;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.AttributesImpl;

public class QnameInserter extends XMLFilterImpl {

    private Map<String,Stack<String>> prefixToUri
	= new HashMap<String,Stack<String>>();
    private Map<String,List<String>> uriToPrefix
	= new HashMap<String,List<String>>();

    public QnameInserter (XMLReader parent) {
	super(parent);
    }

    private String convertToQname (String uri, String localName)
	throws SAXException {
	if (uri == null || uri.length() == 0) {
	    return localName;
	}
	if (uri.equals("http://www.w3.org/XML/1998/namespace")) {
	    return "xml:" + localName;
	}
	List<String> list = uriToPrefix.get(uri);
	if (list != null) {
	    for (String cand : list) {
		Stack<String> stack = prefixToUri.get(cand);
		if (stack != null && !stack.empty()) {
		    if (stack.peek().equals(uri)) {
			if (cand.length() > 0) {
			    return cand + ":" + localName;
			}
		    }
		}
	    }
	    return localName;
	}
	throw new SAXException("URI " + uri + " not mapped to a prefix");
    }

    public void startElement (String uri, String localName, String qName,
			      Attributes atts)
	throws SAXException {
	if (qName == null || qName.length() == 0) {
	    qName = convertToQname(uri, localName);
	}
	if (atts != null && atts.getLength() > 0) {
	    AttributesImpl newAtts = new AttributesImpl();
	    for (int i = 0; i < atts.getLength(); i++) {
		String aqName = atts.getQName(i);
		if (aqName == null || aqName.length() == 0) {
		    aqName = convertToQname(atts.getURI(i),
					    atts.getLocalName(i));
		}
		newAtts.addAttribute(atts.getURI(i), atts.getLocalName(i),
				     aqName, atts.getType(i),
				     atts.getValue(i));
	    }
	    atts = newAtts;
	}
	super.startElement(uri, localName, qName, atts);
    }

    public void endElement (String uri, String localName, String qName)
	throws SAXException {
	if (qName == null || qName.length() == 0) {
	    qName = convertToQname(uri, localName);
	}
	super.endElement(uri, localName, qName);
    }

    public void startPrefixMapping (String prefix, String uri)
	throws SAXException {
	super.startPrefixMapping(prefix, uri);
	Stack<String> stack = prefixToUri.get(prefix);
	if (stack == null) {
	    stack = new Stack<String>();
	    prefixToUri.put(prefix, stack);
	}
	stack.push(uri);
	List<String> list = uriToPrefix.get(uri);
	if (list == null) {
	    list = new ArrayList<String>();
	    uriToPrefix.put(uri, list);
	}
	list.add(prefix);
    }

    public void endPrefixMapping (String prefix) throws SAXException {
	super.endPrefixMapping(prefix);
	Stack<String> stack = prefixToUri.get(prefix);
	if (stack != null && !stack.empty()) {
	    String uri = stack.pop();
	    List<String> list = uriToPrefix.get(uri);
	    if (list != null) {
		list.remove(prefix);
	    }
	}
    }

}
