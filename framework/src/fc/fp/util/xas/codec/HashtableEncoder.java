package fc.fp.util.xas.codec;

import java.util.Hashtable;
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
 * A typed content encoder for the {@link Hashtable} type.  This class
 * implements the {@link ContentEncoder} interface for encoding {@link
 * Hashtable} objects into XML.
 */
public class HashtableEncoder extends ChainedContentEncoder {

    public HashtableEncoder (ContentEncoder chain) {
	this.chain = chain;
    }

    public boolean encode (Object o, String namespace, String name,
			   TypedXmlSerializer ser)
	throws IOException {
	boolean result = false;
	if (Util.equals(namespace, XasUtil.XAS_NAMESPACE)
	    && Util.equals(name, "hashtable")) {
	    if (o instanceof Hashtable) {
		putTypeAttribute(namespace, name, ser);
		Hashtable h = (Hashtable) o;
		XmlWriter xw = new XmlWriter(ser);
		for (Enumeration e = h.keys(); e.hasMoreElements(); ) {
		    Object k = e.nextElement();
		    Object v = h.get(k);
		    Qname kname = ContentCodecFactory.getXmlName(k.getClass());
		    if (kname != null) {
			xw.typedElement(XasUtil.XAS_NAMESPACE, "key",
					kname.getNamespace(), kname.getName(),
					k);
		    } else {
			throw new IOException("Unknown type of object " + k);
		    }
		    Qname vname = ContentCodecFactory.getXmlName(v.getClass());
		    if (vname != null) {
			xw.typedElement(XasUtil.XAS_NAMESPACE, "value",
					vname.getNamespace(), vname.getName(),
					v);
		    } else {
			throw new IOException("Unknown type of object " + v);
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
// arch-tag: 63d2bd2363708a4ea30927b8240ce768 *-
