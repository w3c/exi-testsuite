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

package org.w3c.exi.ttf.fragments;

import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.AttributesImpl;

public class FragmentsXMLReader extends XMLFilterImpl {

    private static final AttributesImpl EMPTY_ATTS = new AttributesImpl();

    public FragmentsXMLReader (XMLReader parent) {
	super(parent);
    }

    public void startDocument () throws SAXException {
	super.startDocument();
	super.startElement("", "root", "root", EMPTY_ATTS);
    }

    public void endDocument () throws SAXException {
	super.endElement("", "root", "root");
	super.endDocument();
    }

}
