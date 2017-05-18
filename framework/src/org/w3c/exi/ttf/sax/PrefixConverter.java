package org.w3c.exi.ttf.sax;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.AttributesImpl;

public class PrefixConverter extends XMLFilterImpl {

    private LinkedList<List<String>> nsStack = new LinkedList<List<String>>();

    public PrefixConverter (XMLReader parent) {
	super(parent);
    }

    public void startElement (String uri, String localName, String qName,
			      Attributes atts)
	throws SAXException {
	LinkedList<String> nss = new LinkedList<String>();
	if (atts != null && atts.getLength() > 0) {
	    int n = atts.getLength();
	    AttributesImpl newAtts = new AttributesImpl();
	    for (int i = 0; i < n; i++) {
		String aqName = atts.getQName(i);
		if (aqName != null && aqName.startsWith("xmlns")) {
		    String prefix;
		    if (aqName.length() == 5) {
			prefix = "";
		    } else {
			prefix = aqName.substring(6);
		    }
		    nss.addFirst(prefix);
		    startPrefixMapping(prefix, atts.getValue(i));
		} else {
		    newAtts.addAttribute(atts.getURI(i), atts.getLocalName(i),
					 aqName, atts.getType(i),
					 atts.getValue(i));
		}
	    }
	    atts = newAtts;
	}
	nsStack.addLast(nss);
	super.startElement(uri, localName, qName, atts);
    }

    public void endElement (String uri, String localName, String qName)
	throws SAXException {
	super.endElement(uri, localName, qName);
	List<String> nss = nsStack.removeLast();
	for (String prefix : nss) {
	    endPrefixMapping(prefix);
	}
    }

}
