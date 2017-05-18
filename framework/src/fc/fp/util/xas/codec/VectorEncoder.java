package fc.fp.util.xas.codec;

import java.util.Vector;
import java.util.Enumeration;
import java.io.IOException;

import fc.fp.util.xas.ContentEncoder;
import fc.fp.util.xas.ChainedContentEncoder;
import fc.fp.util.xas.TypedXmlSerializer;
import fc.fp.util.xas.ContentCodecFactory;
import fc.fp.util.xas.XmlWriter;
import fc.fp.util.xas.Qname;
import fc.fp.util.xas.XasUtil;
import fc.fp.util.Util;

/**
 * A typed content encoder for the {@link Vector} type.  This class
 * implements the {@link ContentEncoder} interface for encoding {@link
 * Vector} objects into XML.
 */
public class VectorEncoder extends ChainedContentEncoder {

    public VectorEncoder (ContentEncoder chain) {
	this.chain = chain;
    }

    public boolean encode (Object o, String namespace, String name,
			   TypedXmlSerializer ser)
	throws IOException {
	boolean result = false;
	if (Util.equals(namespace, XasUtil.XAS_NAMESPACE)
	    && Util.equals(name, "vector")) {
	    if (o instanceof Vector) {
		putTypeAttribute(namespace, name, ser);
		Vector v = (Vector) o;
		XmlWriter xw = new XmlWriter(ser);
		for (Enumeration e = v.elements(); e.hasMoreElements(); ) {
		    Object i = e.nextElement();
		    Qname qname = ContentCodecFactory.getXmlName(i.getClass());
		    if (qname != null) {
			xw.typedElement(XasUtil.XAS_NAMESPACE, "item",
					qname.getNamespace(), qname.getName(),
					i);
		    } else {
			throw new IOException("Unknown type of object " + i);
		    }
		}
		result = true;
	    }
	} else if (chain != null) {
	    result = chain.encode(o, namespace, name, ser);
	}
	return result;
    }

}
// arch-tag: 91664e4fdfca2d584fedbce754d6f35b *-
